(ns kixi.hecuba.api.devices
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          method       (:request-method request)
          route-params (:route-params request)
          entity_id    (:entity_id route-params)
          entity       (-> (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]]))) first)]
      (case method
        :post (not (nil? entity))
        :get (let [items (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))]
               {::items items})))))

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request
        entity_id (:entity_id route-params)]
    (case request-method
      :post (let [body (decode-body request)]
              ;; We need to assert a few things
              (if
                  (or
                   (not= (:entity_id body) entity_id))
                true                      ; it's malformed, game over
                [false {:body body}] ; it's not malformed, return the body now we've read it
                ))
      false)))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request body]} ctx
          entity_id     (-> request :route-params :entity_id)
          [username _]  (sec/get-username-password request store)
          user_id       (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)]

      (when-not (empty? (first (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]])))))
        (let [device    (-> body
                            (assoc :user_id user_id)
                            (update-in [:metadata] json/encode)
                            (update-in [:location] json/encode)
                            (dissoc :readings))
              device_id (sha1/gen-key :device device)]
          (db/execute session (hayt/insert :devices (hayt/values (assoc device :id device_id))))
          (db/execute session (hayt/update :entities
                                           (hayt/set-columns {:devices [+ {device_id (str body)}]})
                                           (hayt/where [[= :id entity_id]])))

          (doseq [reading (:readings body)]
            (let [sensor (-> reading
                             stringify-values
                             (assoc :device_id device_id)
                             (assoc :errors 0)
                             (assoc :events 0))]
              (db/execute session (hayt/insert :sensors (hayt/values sensor)))
              (db/execute session (hayt/insert :sensor_metadata (hayt/values {:device_id device_id :type (:type reading)})))))
          {:device_id device_id})))))

(defn index-handle-ok [ctx]
  (let [{items ::items {mime :media-type} :representation {routes :modular.bidi/routes route-params :route-params} :request} ctx]
    (util/render-items ctx (->> items
                                (map #(dissoc % :user_id))
                                (map #(update-in % [:location] json/decode))
                                (map #(update-in % [:metadata] json/decode))))))

(defn index-handle-created [handlers ctx]
  (let [{{routes :modular.bidi/routes {entity_id :entity_id} :route-params} :request device_id :device_id} ctx]
    (if-not (empty? device_id)
      (let [location
            (bidi/path-for routes (:device @handlers)
                           :entity_id entity_id
                           :device_id device_id)]
        (when-not location (throw (ex-info "No path resolved for Location header"
                                           {:entity_id entity_id
                                            :device_id device_id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422 :body "Provide valid entity_id."}))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{{{:keys [entity_id device_id]} :route-params} :request} ctx
          item (-> (db/execute session (hayt/select :devices (hayt/where [[= :id device_id]]))) first)]
      (if-not (empty? item)
        {::item (-> item
                    (assoc :device_id device_id)
                    (dissoc :id))}
        false))))

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

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{request :request} ctx]
      (if-let [item (::item ctx)]
        (let [body          (decode-body request)
              entity_id     (-> item :entity_id)
              [username _]  (sec/get-username-password request store)
              user_id       (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
              device_id     (-> item :device_id)]
          (db/execute session (hayt/insert :devices (hayt/values (-> body
                                                                     (assoc :id device_id)
                                                                     (assoc :user_id user_id)
                                                                     (dissoc :readings)
                                                                     (update-in [:location] json/encode)
                                                                     (update-in [:metadata] json/encode)
                                                                     stringify-values))))
          ;; TODO when new sensors are created they do not necessarilly overwrite old sensors (unless their type is the same)
          ;; We should probably allow to delete sensors through the API/UI
          (doseq [reading (:readings body)]
            (let [sensor (-> reading
                             stringify-values
                             (assoc :device_id device_id)
                             (assoc :errors 0)
                             (assoc :events 0))]
              (db/execute session (hayt/insert :sensors (hayt/values sensor)))
              (db/execute session (hayt/insert :sensor_metadata (hayt/values {:device_id device_id :type (:type reading)}))))))
        (ring-response {:status 404 :body "Please provide valid entity_id and device_id"})))))

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

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defresource index [store handlers]
  :allowed-methods #{:get :post}
  :available-media-types #{"text/html" "application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store :device)
  :exists? (partial index-exists? store)
  :malformed? index-malformed?
  :post! (partial index-post! store)
  :handle-ok (partial index-handle-ok)
  :handle-created (partial index-handle-created handlers))

(defresource resource [store handlers]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json"}
  :authorized? (authorized? store :device)
  :exists? (partial resource-exists? store)
  :delete-enacted? (partial resource-delete-enacted? store)
  :respond-with-entity? (partial resource-respond-with-entity)
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok store))
