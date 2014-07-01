(ns kixi.hecuba.api.devices
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.data.misc :as m]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.devices :as devices]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex request-method-from-context)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]))

(def ^:private device-resource (p/resource-path-string :entity-device-resource))

(defn- ext-type [sensor type-ext]
  (-> sensor
      (update-in [:type] #(str % "_" type-ext))
      (assoc :period "PULSE")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-exists?
(defmulti index-exists? request-method-from-context)

(defmethod index-exists? :post [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (entities/get-by-map (-> ctx :request :route-params))))

(defmethod index-exists? :get [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [entity_id  (:id (entities/get-by-map (-> ctx :request :route-params)))]
      {::items (devices/get-all session entity_id)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-malformed?
(defmulti index-malformed? request-method-from-context)

(defmethod index-malformed? :default [_] false)

(defmethod index-malformed? :post [{request :request}]
  (let [entity_id (-> request :route-params :entity_id)]
    (let [body (decode-body request)]
      ;; We need to assert a few things
      (if (not= (:entity_id body) entity_id)
        true                      ; it's malformed, game over
        [false {:body body}] ; it's not malformed, return the body now we've read it
        ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defmulti calculated-sensor (fn [sensor] (.toUpperCase (:unit sensor))))

(defmethod calculated-sensor "KWH" [sensor]
  (-> sensor
      (assoc :unit "co2" :synthetic true)
      (update-in [:type] #(m/output-type-for % "KWH2CO2"))))

(defmethod calculated-sensor "M^3" [sensor]
  (let [kwh-sensor (-> sensor
                       (assoc :unit "kWh" :synthetic true)
                       (update-in [:type] #(m/output-type-for % "VOL2KWH")))]
    [kwh-sensor (calculated-sensor kwh-sensor)]))

(defmethod calculated-sensor "FT^3" [sensor]
  (let [kwh-sensor (-> sensor
                       (assoc :unit "kWh" :synthetic true)
                       (update-in [:type] #(m/output-type-for % "VOL2KWH")))]
    [kwh-sensor (calculated-sensor kwh-sensor)]))

(defmethod calculated-sensor :default [sensor])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn create-default-sensors
  "Creates default sensors whenever new device is added: *_differenceSeries for CUMULATIVE,
   and *_co2 for kwh PULSE, etc."
  [body]
  (let [sensors        (:readings body)
        new-sensors    (map #(case (:period %)
                               "CUMULATIVE" (ext-type % "differenceSeries")
                               "PULSE"      (calculated-sensor %)
                               "INSTANT"    nil) sensors)]
    (update-in body [:readings] (fn [readings] (into [] (remove nil? (flatten (concat readings new-sensors))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-post!

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request body]} ctx
          entity_id              (-> request :route-params :entity_id)
          username               (sec/session-username (-> ctx :request :session))
          user_id                (-> (users/get-by-username session username) :id)]

      (when-not (entities/get-by-id session entity_id)
        (let [device       (-> body
                               (assoc :user_id user_id)
                               (update-in [:metadata] json/encode)
                               (update-in [:location] json/encode)
                               (dissoc :readings))
              device_id    (sha1/gen-key :device device)
              new-body     (create-default-sensors body)]
          (db/execute session (hayt/insert :devices (hayt/values (assoc device :id device_id))))
          (db/execute session (hayt/update :entities
                                           (hayt/set-columns {:devices [+ {device_id (str new-body)}]})
                                           (hayt/where [[= :id entity_id]])))

          (doseq [reading (:readings new-body)]
            (let [sensor (-> reading
                             (merge (stringify-values (dissoc reading :synthetic))) ;; synthetic is a boolean so we don't stringify
                             (assoc :device_id device_id))]
              (db/execute session (hayt/insert :sensors (hayt/values sensor)))
              (db/execute session (hayt/insert :sensor_metadata (hayt/values {:device_id device_id :type (:type reading)})))))
          {:device_id device_id
           :entity_id entity_id})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-ok

(defn index-handle-ok [ctx]
  (let [items (::items ctx)]
    (util/render-items ctx (->> items
                                (map #(dissoc % :user_id))
                                (map #(update-in % [:location] json/decode))
                                (map #(update-in % [:metadata] json/decode))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-created

(defn index-handle-created [ctx]
  (let [entity_id (-> ctx :entity_id)
        device_id (-> ctx :device_id)
        location  (format device-resource entity_id device_id)]
    (ring-response {:headers {"Location" location}
                    :body (json/encode {:location location
                                        :status "OK"
                                        :version "4"})})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-exists?

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [device_id (-> ctx :request :route-params :device_id)
          item (devices/get-by-id session device_id)]
      (if-not (empty? item)
        {::item (-> item
                    (assoc :device_id device_id)
                    (dissoc :id))}
        false))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-exists?

;; Should be device-response etc and it should do the delete in delete!,
;; that should put something in the context which is then checked here.
(defn resource-delete-enacted? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item} ctx
          device_id (:device_id item)
          entity_id (:entity_id item)
          response1 (db/execute session (hayt/delete :devices (hayt/where [[= :id device_id]])))
          response2 (db/execute session (hayt/delete :sensors (hayt/where [[= :device_id device_id]])))
          response3 (db/execute session (hayt/delete :sensor_metadata (hayt/where [[= :device_id device_id]])))
          response4 (db/execute session (hayt/delete :entities (hayt/columns {:devices device_id}) (hayt/where [[= :id entity_id]])))]
      (every? empty? [response1 response2 response3 response4]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-put!

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{request :request} ctx]
      (if-let [item (::item ctx)]
        (let [body          (decode-body request)
              entity_id     (-> item :entity_id)
              username      (sec/session-username (-> ctx :request :session))
              user_id       (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
              device_id     (-> item :device_id)
              new-sensors   (map #(when (= "CUMULATIVE" (:period %)) (ext-type % "differenceSeries")) (:readings body))
              new-body      (update-in body [:readings] (fn [readings] (into [] (remove nil? (concat readings new-sensors)))))]
          (db/execute session (hayt/insert :devices (hayt/values (-> body
                                                                     (assoc :id device_id)
                                                                     (assoc :user_id user_id)
                                                                     (dissoc :readings)
                                                                     (update-in [:location] json/encode)
                                                                     (update-in [:metadata] json/encode)
                                                                     stringify-values))))
          (db/execute session (hayt/update :entities
                                           (hayt/set-columns {:devices [+ {device_id (str new-body)}]})
                                           (hayt/where [[= :id entity_id]])))
          ;; TODO when new sensors are created they do not necessarilly overwrite old sensors (unless their type is the same)
          ;; We should probably allow to delete sensors through the API/UI
          (doseq [reading (:readings new-body)]
            (let [sensor (-> reading
                             stringify-values
                             (assoc :device_id device_id))]
              (db/execute session (hayt/insert :sensors (hayt/values sensor)))
              (db/execute session (hayt/insert :sensor_metadata (hayt/values {:device_id device_id :type (:type reading)}))))))
        (ring-response {:status 404 :body "Please provide valid entity_id and device_id"})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-handle-ok

(defn resource-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item} ctx]
      (-> item
          (assoc :readings (map #(dissoc % :user_id) (db/execute session
                                                                 (hayt/select :sensors
                                                                              (hayt/where [[= :device_id (:device_id item)]])))))
          ;; (assoc :measurements (hecuba/items querier :measurement {:device_id (:id item)}))
          (update-in [:location] json/decode)
          (update-in [:metadata] json/decode)
          (dissoc :user_id)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-respond-with-entity

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store)
  :exists? (partial index-exists? store)
  :malformed? index-malformed?
  :post! (partial index-post! store)
  :handle-ok index-handle-ok
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :exists? (partial resource-exists? store)
  :delete-enacted? (partial resource-delete-enacted? store)
  :respond-with-entity? resource-respond-with-entity
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok store))
