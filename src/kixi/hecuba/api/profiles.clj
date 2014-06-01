(ns kixi.hecuba.api.profiles
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values update-stringified-lists sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn index-exists? [querier ctx]
  (let [request       (:request ctx)
        method        (:request-method request)
        route-params  (:route-params request)
        entity_id     (:entity_id route-params)
        entity        (hecuba/item querier :entity entity_id)]
    (case method
      :post (not (nil? entity))
      :get (let [items (hecuba/items querier :profile [[= :entity_id entity_id]])]
             {::items items}))))

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

(defn index-post! [commander querier ctx]
  (let [request       (-> ctx :request)
        profile       (-> ctx :body)
        entity_id     (-> profile :entity_id)
        timestamp     (-> profile :timestamp)
        [username _]  (sec/get-username-password request querier)
        user_id       (-> (hecuba/items querier :user [[= :username username]]) first :id)
        ]
    (when (and entity_id timestamp)
      (when-not (empty? (hecuba/item querier :entity entity_id))
        {:profile-id (hecuba/upsert!
                        commander
                        :profile (-> profile
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
                                     ))}))))

(defn add-profile-keys [& pairs]
  (->> pairs
       (map-indexed
         (fn [index pair]
           (let [k (first pair)
                 v (last  pair)]
           {(str "profile_" index "_key")   k
            (str "profile_" index "_value") v})))
       (into {})))

(defn attribute-type [attr]
  (if (coll? attr)
       :associated-items
       (try
         (if (map? (json/decode attr))
           :nested-item
           :attribute)
          (catch Exception e
                 :attribute))))

(defn explode-nested-item [item-name item-string]
  (let [item (json/decode item-string)]
    (->> item
         (map
           (fn [pair]
             { (str item-name "_" (key pair)) (val pair) }))
         (into {}))))

(defn explode-associated-items [association-name items]
  (->> items
       (map-indexed
         (fn [index item]
           (let [item-name (str association-name "_" index)]
             (case (attribute-type item)
               :nested-item (explode-nested-item (str association-name "_" index) item)
               :attribute   { item-name item }))))
       (into {})))

(defn explode-items [profile]
  (->> profile
       (map
         (fn [attribute]
           (let [attr-name   (name (key attribute))
                 attr-value  (val attribute)
                 _ (println "Considering\n\tKey: " attr-name "\n\tVal: " attr-value)]
             (case (attribute-type attr-value)
               :associated-items (do
                                   (println "Found associated-items for " attr-name)
                                   (explode-associated-items attr-name attr-value))
               :nested-item      (do
                                   (println "Found nested-item for " attr-name)
                                   (explode-nested-item attr-name attr-value))
               :attribute        (do
                                   (println "Found standard attribute " attr-name)
                                   { attr-name attr-value})))))

       (into {})))

(defn index-handle-ok [ctx]
  (let [{items ::items
         {mime :media-type} :representation
         {routes :modular.bidi/routes
          route-params :route-params} :request} ctx
        userless-items (->> items
                            (map #(dissoc % :user_id)))
        exploded-items (->> items
                            (map #(dissoc % :user_id))
                            (map #(explode-items %)))
        formatted-items (if (= "text/csv" mime)
                          ; serving tall csv style profiles
                          (apply util/map-longest add-profile-keys ["" ""] exploded-items)
                          ; serving json profiles
                          userless-items)
        ]

        (util/render-items ctx formatted-items)))

(defn index-handle-created [handlers ctx]
  (let [{{routes :modular.bidi/routes {entity_id :entity_id} :route-params} :request
         profile-id :profile-id} ctx]
    (if-not (empty? profile-id)
      (let [location
            (bidi/path-for routes (:profile @handlers)
                           :entity_id entity_id
                           :profile-id profile-id)]
        (when-not location
          (throw (ex-info "No path resolved for Location header"
                          {:entity_id entity_id
                           :profile-id profile-id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422
                      :body "Provide valid entityId and timestamp."}))))

(defn resource-exists? [querier ctx]
  (let [{{{:keys [entity_id profile-id]} :route-params} :request} ctx
        item (hecuba/item querier :profile profile-id)]
    (if-not (empty? item)
      {::item (-> item
                  (assoc :profile-id profile-id)
                  (dissoc :id))}
      false)))

(defn resource-delete-enacted? [commander ctx]
  (let [{item ::item} ctx
        device_id (:device_id item)
        entity_id (:entity_id item)
        response1 (hecuba/delete! commander :device [[= :id device_id]])
        response2 (hecuba/delete! commander :sensor [[= :device_id device_id]])
        response3 (hecuba/delete! commander :sensor_metadata [[= :device_id device_id]])
        response4 (hecuba/delete! commander :entity {:devices device_id} [[= :id entity_id]])]
    (every? empty? [response1 response2 response3 response4])))

(defn resource-put! [commander querier ctx]
  (let [{request :request} ctx]
    (if-let [item (::item ctx)]
      (let [body          (decode-body request)
            entity_id     (-> item :entity_id)
            [username _]  (sec/get-username-password request querier)
            user_id       (-> (hecuba/items querier :user [[= :username username]]) first :id)
            profile-id     (-> item :profile-id)]
        (hecuba/upsert! commander :profile (-> body
                                               (assoc :id profile-id)
                                               (assoc :user_id user_id)
                                               ;; TODO: add storeys, walls, etc.
                                               stringify-values)))
      (ring-response {:status 404 :body "Please provide valid entityId and timestamp"}))))

(defn resource-handle-ok [querier ctx]
  (let [request       (:request ctx)
        route-params  (:route-params request)
        item          (::item ctx)]
    (util/render-item request (-> item
                                  (dissoc :user_id)))))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types #{"text/csv" "application/json"}
  :known-content-type? #{"text/csv" "application/json"}
  :authorized? (authorized? querier :profile)
  :exists? (partial index-exists? querier)
  :malformed? index-malformed?
  :post! (partial index-post! commander querier)
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
