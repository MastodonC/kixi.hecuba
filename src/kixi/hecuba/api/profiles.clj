(ns kixi.hecuba.api.profiles
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn index-exists? [querier ctx]
  (let [request       (:request ctx)
        method        (:request-method request)
        route-params  (:route-params request)
        entity-id     (:entity-id route-params)
        entity        (hecuba/item querier :entity entity-id)]
    (case method
      :post (not (nil? entity))
      :get (let [items (hecuba/items querier :profile [[= :entity-id entity-id]])]
             {::items items}))))

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request
        entity-id (:entity-id route-params)]
    (case request-method
      :post (let [body (decode-body request)]
              ;; We need to assert a few things
              (if
                (or
                  (not= (:entity-id body) entity-id))
                true                  ; it's malformed, game over
                [false {:body body}]  ; it's not malformed, return the body now we've read it
                ))
      false)))

(defn json-list [items]
  (map json/encode items))

(defn update-when-available [body [selector]]
  (if
    (selector body)
    (update-in body [selector] json-list)
    (assoc body selector nil)))

(defn index-post! [querier commander ctx]
  (let [{:keys [request body]} ctx
        entity-id     (-> request :route-params :entity-id)
        [username _]  (sec/get-username-password request querier)
        user-id       (-> (hecuba/items querier :user [[= :username username]]) first :id)]

    (when-not (empty? (first (hecuba/items querier :entity [[= :id entity-id]])))
      (let [profile   (-> body
                          (assoc :user-id user-id)
                          (update-in [:profile-data] json/encode)
                          ;; here goes the list of "stuff" associated with profiles
                          (update-when-available [:airflow_measurements])
                          (update-when-available [:chps])
                          (update-when-available [:conservatories])
                          (update-when-available [:door_sets])
                          (update-when-available [:extensions])
                          (update-when-available [:floors])
                          (update-when-available [:heat_pumps])
                          (update-when-available [:heating_systems])
                          (update-when-available [:hot_water_systems])
                          (update-when-available [:low_energy_lights])
                          (update-when-available [:photovoltaics])
                          (update-when-available [:roof_rooms])
                          (update-when-available [:roofs])
                          (update-when-available [:small_hydros])
                          (update-when-available [:solar_thermals])
                          (update-when-available [:storeys])
                          (update-when-available [:thermal-images])
                          (update-when-available [:walls])
                          (update-when-available [:wind_turbines])
                          (update-when-available [:window_sets]))
            profile-id (hecuba/upsert! commander :profile profile)]
        {:profile-id profile-id}))))

(defn index-handle-ok [ctx]
  (let [{items ::items
         {mime :media-type} :representation
         {routes :modular.bidi/routes
          route-params :route-params} :request} ctx]
    (util/render-items ctx (->> items
                                (map #(dissoc % :user-id))
                                (map #(update-in % [:thermal-images] json/decode))
                                (map #(update-in % [:storeys] json/decode))
                                (map #(update-in % [:walls] json/decode))
                                (map #(update-in % [:roofs] json/decode))
                                (map util/downcast-to-json)
                                (map util/camelify)
                                json/encode))))

(defn index-handle-created [handlers ctx]
  (let [{{routes :modular.bidi/routes {entity-id :entity-id} :route-params} :request
         profile-id :profile-id} ctx]
    (if-not (empty? profile-id)
      (let [location
            (bidi/path-for routes (:profile @handlers)
                           :entity-id entity-id
                           :profile-id profile-id)]
        (when-not location
          (throw (ex-info "No path resolved for Location header"
                          {:entity-id entity-id
                           :profile-id profile-id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422
                      :body "Provide valid projectId and propertyCode."}))))

(defn resource-exists? [querier ctx]
  (let [{{{:keys [entity-id profile-id]} :route-params} :request} ctx
        item (hecuba/item querier :profile profile-id)]
    (if-not (empty? item)
      {::item (-> item
                  (assoc :profile-id profile-id)
                  (dissoc :id))}
      false)))

(defn resource-delete-enacted? [commander ctx]
  (let [{item ::item} ctx
        device-id (:device-id item)
        entity-id (:entity-id item)
        response1 (hecuba/delete! commander :device [[= :id device-id]])
        response2 (hecuba/delete! commander :sensor [[= :device-id device-id]])
        response3 (hecuba/delete! commander :sensor-metadata [[= :device-id device-id]])
        response4 (hecuba/delete! commander :entity {:devices device-id} [[= :id entity-id]])]
    (every? empty? [response1 response2 response3 response4])))

(defn resource-put! [commander querier ctx]
  (let [{request :request} ctx]
    (if-let [item (::item ctx)]
      (let [body          (decode-body request)
            entity-id     (-> item :entity-id)
            [username _]  (sec/get-username-password request querier)
            user-id       (-> (hecuba/items querier :user [[= :username username]]) first :id)
            profile-id     (-> item :profile-id)]
        (hecuba/upsert! commander :profile (-> body
                                               (assoc :id profile-id)
                                               (assoc :user-id user-id)
                                               ;; TODO: add storeys, walls, etc.
                                               stringify-values)))
      (ring-response {:status 404 :body "Please provide valid entityId and timestamp"}))))

(defn resource-handle-ok [querier ctx]
  (let [{item ::item} ctx]
    (-> item
        (dissoc :user-id)
        ;; TODO add storeys, walls, etc.
        util/downcast-to-json
        util/camelify
        json/encode)))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :profile)
  :exists? (partial index-exists? querier)
  :malformed? index-malformed?
  :post! (partial index-post! querier commander)
  :handle-ok (partial index-handle-ok)
  :handle-created (partial index-handle-created handlers))

(defresource resource [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :delete :putj}
  :available-media-types #{"application/json"}
  :authorized? (authorized? querier :profile)
  :exists? (partial resource-exists? querier)
  :delete-enacted? (partial resource-delete-enacted? commander)
  :respond-with-entity? (partial resource-respond-with-entity)
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! commander querier)
  :handle-ok (partial resource-handle-ok querier))
