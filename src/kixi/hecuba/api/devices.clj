(ns kixi.hecuba.api.devices
  (:require
   [clojure.core.match :refer (match)]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.data.misc :as m]
   [kixi.hecuba.webutil :refer (decode-body authorized?)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.uuid :refer (uuid)]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.devices :as devices]
   [kixi.hecuba.data.sensors :as sensors]
   [kixi.hecuba.data.entities.search :as search]
   [schema.core :as s]
   [kixi.amon-schema :as schema]
   [clojure.data :refer (diff)]))

(def ^:private device-resource (p/resource-path-string :entity-device-resource))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects role request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s role: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects role request-method)
  (match  [(has-admin? role)
           (has-programme-manager? programme-id allowed-programmes)
           (has-project-manager? project-id allowed-projects)
           (has-user? programme-id allowed-programmes project-id allowed-projects)
           request-method]

          [true _ _ _ _]    [true {:editable true}]
          [_ true _ _ _]    [true {:editable true}]
          [_ _ true _ _]    [true {:editable true}]
          [_ _ _ true :get] true
          :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INDEX

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [body request-method session params]} (:request ctx)
          {:keys [projects programmes role]} (sec/current-authentication session)
          entity_id (:entity_id params)
          {:keys [project_id programme_id]} (when entity_id
                                              (search/get-by-id entity_id (:search-session store)))]
      (when (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects role request-method)))))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request        (:request ctx)
          method         (:request-method request)
          route-params   (:route-params request)
          entity_id      (:entity_id route-params)
          entity         (search/get-by-id entity_id (:search-session store))]
      (case method
        :post (seq entity)
        :get (let [items (devices/get-devices session entity_id)
                   editable (:editable ctx)]
               {::items (mapv #(assoc % :editable editable) items)})))))

(defn should-calculate-fields? [sensors]
  (not (some #(and (= (:period %) "CUMULATIVE")
                   (or (= (:actual_annual_calculation %) true)
                       (= (:normalised_annual_calculation %) true))) sensors)))

(def user-editable-keys [:device_id :type :sensor_id :accuracy :alias :actual_annual
                         :corrected_unit :correction
                         :correction_factor :correction_factor_breakdown
                         :frequency :max :min :period
                         :resolution :unit :user_metadata])

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request
        entity_id (:entity_id route-params)]
    (case request-method
      :post (let [body (update-in (decode-body request) [:readings]
                                  (fn [readings] (map #(select-keys % user-editable-keys)
                                                      readings)))]
              (if (or
                   (s/check schema/BaseDevice body)
                   (not= (:entity_id body) entity_id)
                   (not (should-calculate-fields? (:readings body))))
                true
                [false {:body body}]))
      false)))
(defn- ext-type [sensor type-ext]
  (-> sensor
      (update-in [:sensor_id] #(str % "_" type-ext))
      (assoc :type (str (:type sensor) "_" type-ext))
      (assoc :period "PULSE" :synthetic true)))

(defmulti calculated-sensor (fn [sensor] (when-let [unit (:unit sensor)]
                                           (.toUpperCase unit))))

(defmethod calculated-sensor "KWH" [sensor]
  (-> sensor
      (assoc :unit "co2" :synthetic true)
      (update-in [:sensor_id] #(str % "_" "KWH2CO2"))
      (assoc :type (str (:type sensor) "_" "KWH2CO2"))))

(defmethod calculated-sensor "M^3" [sensor]
  (let [kwh-sensor (-> sensor
                       (assoc :unit "kWh" :synthetic true)
                       (update-in [:sensor_id] #(m/output-type-for % "VOL2KWH"))
                       (assoc :type (str (:type sensor) "_" "VOL2KWH")))]
    [kwh-sensor (calculated-sensor kwh-sensor)]))

(defmethod calculated-sensor "FT^3" [sensor]
  (let [kwh-sensor (-> sensor
                       (assoc :unit "kWh" :synthetic true)
                       (update-in [:sensor_id] #(m/output-type-for % "VOL2KWH"))
                       (assoc :type (str (:type sensor) "_" "VOL2KWH")))]
    [kwh-sensor (calculated-sensor kwh-sensor)]))

(defmethod calculated-sensor :default [sensor])

(defn create-default-sensors
  "Creates default sensors whenever new device is added: *_differenceSeries for CUMULATIVE,
   and *_co2 for kwh PULSE, etc."
  [body]
  (let [sensors        (:readings body)
        new-sensors    (map #(case (:period %)
                               "CUMULATIVE" (ext-type % "differenceSeries")
                               "PULSE"      (calculated-sensor %)
                               "INSTANT"    nil
                               nil) sensors)]
    (update-in body [:readings] (fn [readings] (into [] (remove nil? (flatten (concat readings new-sensors))))))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request body]} ctx
          entity_id              (-> request :route-params :entity_id)
          username               (sec/session-username (-> ctx :request :session))
          user_id                (:id (users/get-by-username session username))]

      (let [device_id    (uuid)
            new-body     (create-default-sensors (assoc body :readings (mapv #(assoc % :sensor_id (uuid)) (:readings body))))]
        (devices/insert session entity_id (assoc new-body :device_id device_id :user_id user_id))
        (doseq [reading (:readings new-body)]
          (sensors/insert session (assoc reading :device_id device_id :user_id user_id)))
        (-> (search/searchable-entity-by-id entity_id session)
            (search/->elasticsearch (:search-session store)))
        {:device_id device_id :entity_id entity_id :readings (:readings new-body)}))))

(defn index-handle-ok [ctx]
  (util/render-items ctx (::items ctx)))

(defn index-handle-created [ctx]
  (let [entity_id (-> ctx :entity_id)
        device_id (-> ctx :device_id)
        readings  (remove #(:synthetic %) (:readings ctx)) ;; Don't return synthetic sensor_ids to the user
        location  (format device-resource entity_id device_id)]
    (ring-response {:headers {"Location" location}
                    :body (json/encode {:location location
                                        :status "OK"
                                        :version "4"
                                        :readings (into {} (mapv #(hash-map (:type %) (:sensor_id %)) readings))})})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCE

(defn resource-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes role]}     (sec/current-authentication session)
          entity_id (:entity_id params)
          {:keys [project_id programme_id]} (when entity_id
                                              (search/get-by-id entity_id (:search-session store)))]
      (when (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects role request-method)))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [device_id (-> ctx :request :route-params :device_id)
          item      (devices/get-by-id session device_id)]
      (if-not (empty? item)
        {::item item}
        false))))

;; Should be device-response etc and it should do the delete in delete!,
;; that should put something in the context which is then checked here.
(defn resource-delete-enacted? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item}   ctx
          device_id       (:device_id item)
          entity_id       (:entity_id item)
          response        (devices/delete device_id false session)
          search-response (-> (search/searchable-entity-by-id entity_id session)
                              (search/->elasticsearch (:search-session store)))]
      "Delete Accepted")))

(defn recreate-sensor [device_id user_id old-sensor new-sensor session]
  ;; Deleting old
  (let [old-synthetic-sensors (create-default-sensors {:readings [old-sensor]})]
    (doseq [s (:readings old-synthetic-sensors)]
      (sensors/delete s session)))
  ;; Inserting new
  (let [new-sensors (create-default-sensors {:readings [(dissoc (m/deep-merge old-sensor new-sensor)
                                                                              :lower_ts :upper_ts :median :status)]})]
    (doseq [s (:readings new-sensors)]
      (sensors/insert session (assoc s :device_id device_id :user_id user_id)))))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{request :request} ctx]
      (if-let [item (::item ctx)]
        (let [body          (decode-body request)
              entity_id     (-> item :entity_id)
              username      (sec/session-username (-> ctx :request :session))
              user_id       (:id (users/get-by-username session username))
              device_id     (-> item :device_id)
              readings      (map #(assoc % :sensor_id (or (:sensor_id %) (uuid))) (:readings body))
              new-body      (create-default-sensors (assoc body :readings readings))]
          (devices/update session entity_id device_id (assoc new-body :user_id user_id))
          (doseq [new-sensor   (:readings body)]
            (let [old-sensor   (first (filter #(= (:sensor_id %) (:sensor_id new-sensor)) (:readings item)))
                  updated-keys (keys new-sensor)]
              (if (and old-sensor (some (into #{} updated-keys) #{:unit :period :type}))
                ;; sensor already exists - need to update and recreate synthetic sensors
                (recreate-sensor device_id user_id old-sensor new-sensor session)
                ;; sensor doesn't exist - adding new sensor, or sensor exists but doesn't need to be recreated
                (let [merged      (m/deep-merge old-sensor new-sensor)
                      new-sensors (create-default-sensors
                                   {:readings [(-> merged
                                                   (dissoc :lower_ts :upper_ts
                                                           :median :status)
                                                   (cond-> (nil? (:sensor_id merged))
                                                           (assoc :sensor_id (uuid))))]})]
                  (doseq [s (:readings new-sensors)]
                    (sensors/insert session (assoc s :device_id device_id :user_id user_id)))))))
          (-> (search/searchable-entity-by-id entity_id session)
              (search/->elasticsearch (:search-session store)))
          (ring-response {:status 404 :body "Please provide valid entity_id and device_id"}))))))

(defn resource-handle-ok [ctx]
  (let [req (:request ctx)]
    (util/render-item ctx (::item ctx))))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :exists? (partial index-exists? store)
  :malformed? index-malformed?
  :can-post-to-missing? (constantly false)
  :post! (partial index-post! store)
  :handle-ok index-handle-ok
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (resource-allowed? store)
  :exists? (partial resource-exists? store)
  :delete-enacted? (partial resource-delete-enacted? store)
  :respond-with-entity? resource-respond-with-entity
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok resource-handle-ok)
