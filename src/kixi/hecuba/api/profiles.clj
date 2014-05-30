(ns kixi.hecuba.api.profiles
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values update-stringified-lists sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.dbnew :as db]
   [kixi.hecuba.storage.sha1 :as sha1]))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request       (:request ctx)
          method        (:request-method request)
          route-params  (:route-params request)
          entity_id     (:entity_id route-params)
          entity        (-> (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]]))) first)]
      (case method
        :post (not (nil? entity))
        :get (let [items (db/execute session (hayt/select :profiles (hayt/where [[= :entity_id entity_id]])))]
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
                true                  ; it's malformed, game over
                [false {:body body}]  ; it's not malformed, return the body now we've read it
                ))
      false)))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request       (-> ctx :request)
          profile       (-> ctx :body)
          entity_id     (-> profile :entity_id)
          timestamp     (-> profile :timestamp)
          [username _]  (sec/get-username-password request store)
          user_id       (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
          profile_id    (sha1/gen-key :profile profile)]
      (when (and entity_id timestamp)
        (when-not (empty? (-> (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]]))) first))
          (db/execute session (hayt/insert :profiles (-> profile
                                                         (assoc :user_id user_id)
                                                         (update-stringified-lists
                                                          [:airflow_measurements :chps
                                                           :conservatories :door_sets
                                                           :extensions :floors :heat_pumps
                                                           :heating_systems :hot_water_systems
                                                           :low_energy_lights :photovoltaics
                                                           :roof_rooms :roofs :small_hydros
                                                           :solar_thermals :storeys :thermal_images
                                                           :ventilation_systems :walls
                                                           :wind_turbines :window_sets])
                                                         (update-in [:profile_data] json/encode)
                                                         )))
          {:profile_id profile_id})))))

(defn index-handle-ok [ctx]
  (let [{items ::items
         {mime :media-type} :representation
         {routes :modular.bidi/routes
          route-params :route-params} :request} ctx]
    (util/render-items ctx (->> items
                                (map #(dissoc % :user_id))
                                (map #(update-in % [:thermal_images] json/decode))
                                (map #(update-in % [:storeys] json/decode))
                                (map #(update-in % [:walls] json/decode))
                                (map #(update-in % [:roofs] json/decode))))))

(defn index-handle-created [handlers ctx]
  (let [{{routes :modular.bidi/routes {entity_id :entity_id} :route-params} :request
         profile_id :profile_id} ctx]
    (if-not (empty? profile_id)
      (let [location
            (bidi/path-for routes (:profile @handlers)
                           :entity_id entity_id
                           :profile_id profile_id)]
        (when-not location
          (throw (ex-info "No path resolved for Location header"
                          {:entity_id entity_id
                           :profile_id profile_id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422
                      :body "Provide valid entity_id and timestamp."}))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{{{:keys [entity_id profile_id]} :route-params} :request} ctx
          item (-> (db/execute session :profiles (hayt/where [[= :id profile_id]])) first)]
      (if-not (empty? item)
        {::item (-> item
                    (assoc :profile_id profile_id)
                    (dissoc :id))}
        false))))

(defn resource-delete-enacted? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item} ctx
          profile_id (:profile_id item)
          response (db/execute session (hayt/delete :profiles (hayt/where [[= :id profile_id]])))]
      (empty? response))))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{request :request} ctx]
      (if-let [item (::item ctx)]
        (let [body          (decode-body request)
              entity_id     (-> item :entity_id)
              [username _]  (sec/get-username-password request store)
              user_id       (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
              profile_id     (-> item :profile-id)]
          (db/execute session (hayt/insert :profiles (hayt/values (-> body
                                                                      (assoc :id profile_id)
                                                                      (assoc :user_id user_id)
                                                                      ;; TODO: add storeys, walls, etc.
                                                                      stringify-values)))))
        (ring-response {:status 404 :body "Please provide valid entityId and timestamp"})))))

(defn resource-handle-ok [store ctx]
  (let [{item ::item} ctx]
    (-> item
        (dissoc :user_id))))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defresource index [store handlers]
  :allowed-methods #{:get :post}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store :profile)
  :exists? (partial index-exists? store)
  :malformed? index-malformed?
  :post! (partial index-post! store)
  :handle-ok (partial index-handle-ok)
  :handle-created (partial index-handle-created handlers))

(defresource resource [store handlers]
  :allowed-methods #{:get :delete :putj}
  :available-media-types #{"application/json"}
  :authorized? (authorized? store :profile)
  :exists? (partial resource-exists? store)
  :delete-enacted? (partial resource-delete-enacted? store)
  :respond-with-entity? (partial resource-respond-with-entity)
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok store))
