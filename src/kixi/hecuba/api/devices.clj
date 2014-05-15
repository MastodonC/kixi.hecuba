(ns kixi.hecuba.api.devices
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn index-exists? [querier ctx]
  (let [request      (:request ctx)
        method       (:request-method request)
        route-params (:route-params request)
        entity-id    (:entity-id route-params)
        entity       (hecuba/item querier :entity entity-id)]
    (case method
      :post (not (nil? entity))
      :get (let [items (hecuba/items querier :device [[= :entity-id entity-id]])]
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
                true                      ; it's malformed, game over
                [false {:body body}] ; it's not malformed, return the body now we've read it
                ))
      false)))

(defn index-post! [querier commander ctx]
  (let [{:keys [request body]} ctx
        entity-id     (-> request :route-params :entity-id)
        [username _]  (sec/get-username-password request querier)
        user-id       (-> (hecuba/items querier :user [[= :username username]]) first :id)]

    (when-not (empty? (first (hecuba/items querier :entity [[= :id entity-id]])))
      (let [device    (-> body
                          (assoc :user-id user-id)
                          (update-in [:metadata] json/encode)
                          (update-in [:location] json/encode)
                          (dissoc :readings)
                          stringify-values)
            device-id (hecuba/upsert! commander :device device)]

        (hecuba/update! commander :entity {:devices [+ {device-id (str body)}]} [[= :id entity-id]])

        (doseq [reading (:readings body)]
          (let [sensor (-> reading
                           stringify-values
                           (assoc :device-id device-id)
                           (assoc :errors 0)
                           (assoc :events 0))]
            (hecuba/upsert! commander :sensor (util/->shallow-kebab-map sensor))
            (hecuba/upsert! commander :sensor-metadata (util/->shallow-kebab-map {:device-id device-id :type (get-in reading ["type"])}))))
        {:device-id device-id}))))

(defn index-handle-ok [ctx]
  (let [{items ::items {mime :media-type} :representation {routes :modular.bidi/routes route-params :route-params} :request} ctx]
    (util/render-items ctx (->> items
                                (map #(dissoc % :user-id))
                                (map #(update-in % [:location] json/decode))
                                (map #(update-in % [:metadata] json/decode))
                                (map util/downcast-to-json)
                                (map util/camelify)
                                json/encode))))

(defn index-handle-created [handlers ctx]
  (let [{{routes :modular.bidi/routes {entity-id :entity-id} :route-params} :request device-id :device-id} ctx]
    (if-not (empty? device-id)
      (let [location
            (bidi/path-for routes (:device @handlers)
                           :entity-id entity-id
                           :device-id device-id)]
        (when-not location (throw (ex-info "No path resolved for Location header"
                                           {:entity-id entity-id
                                            :device-id device-id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422 :body "Provide valid entityId."}))))

(defn resource-exists? [querier ctx]
  (let [{{{:keys [entity-id device-id]} :route-params} :request} ctx
        item (hecuba/item querier :device device-id)]
    (if-not (empty? item)
      {::item (-> item
                  (assoc :device-id device-id)
                  (dissoc :id))}
      false)))

;; Should be device-response etc and it should do the delete in delete!,
;; that should put something in the context which is then checked here.
(defn resource-delete-enacted? [commander ctx]
  (let [{item ::item} ctx
        device-id (:device-id item)
        entity-id (:entity-id item)
        response1 (hecuba/delete! commander :device [[= :id device-id]])
        response2 (hecuba/delete! commander :sensor [[= :device-id device-id]])
        response3 (hecuba/delete! commander :sensor-metadata [[= :device-id device-id]])
        response4 (hecuba/delete! commander :entity {:devices device-id} [[= :id entity-id]])]
    (every? empty? [response1 response2 response3 response4])))

(defn resource-put! [querier commander ctx]
  (let [{request :request} ctx]
    (if-let [item (::item ctx)]
      (let [body          (decode-body request)
            entity-id     (-> item :entity-id)
            [username _]  (sec/get-username-password request querier)
            user-id       (-> (hecuba/items querier :user [[= :username username]]) first :id)
            device-id     (-> item :device-id)]
        (hecuba/upsert! commander :device (-> body
                                              (assoc :id device-id)
                                              (assoc :user-id user-id)
                                              (dissoc :readings)
                                              (update-in [:location] json/encode)
                                              (update-in [:metadata] json/encode)
                                              stringify-values))
        ;; TODO when new sensors are created they do not necessarilly overwrite old sensors (unless their type is the same)
        ;; We should probably allow to delete sensors through the API/UI
        (doseq [reading (:readings body)]
          (let [sensor (-> reading
                           stringify-values
                           (assoc :device-id device-id)
                           (assoc :errors 0)
                           (assoc :events 0))]
            (hecuba/upsert! commander :sensor (util/->shallow-kebab-map sensor))
            (hecuba/upsert! commander :sensor-metadata
                            (util/->shallow-kebab-map {:device-id device-id :type (get-in reading ["type"])})))))
      (ring-response {:status 404 :body "Please provide valid entityId and deviceId"}))))

(defn resource-handle-ok [querier ctx]
  (let [{item ::item} ctx]
    (-> item
        (assoc :readings (map #(dissoc % :user-id) (hecuba/items querier :sensor [[= :device-id (:device-id item)]])))
        ;; (assoc :measurements (hecuba/items querier :measurement {:device-id (:id item)}))
        (update-in [:location] json/decode)
        (update-in [:metadata] json/decode)
        (dissoc :user-id)
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
  :available-media-types #{"text/html" "application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :device)
  :exists? (partial index-exists? querier)
  :malformed? index-malformed?
  :post! (partial index-post! querier commander)
  :handle-ok (partial index-handle-ok)
  :handle-created (partial index-handle-created handlers))

(defresource resource [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json"}
  :authorized? (authorized? querier :device)
  :exists? (partial resource-exists? querier)
  :delete-enacted? (partial resource-delete-enacted? commander)
  :respond-with-entity? (partial resource-respond-with-entity)
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! querier commander)
  :handle-ok (partial resource-handle-ok querier))
