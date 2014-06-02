(ns kixi.hecuba.api.entities
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values sha1-regex update-stringified-lists)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request       (-> ctx :request)
          entity        (-> request decode-body)
          project_id    (-> entity :project_id)
          property_code (-> entity :property_code)
          username      (sec/get-username ctx)
          ;; FIXME: Why user_id?
          user_id       (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)]
      (when (and project_id property_code)
        (when-not (empty? (-> (db/execute session (hayt/select :projects (hayt/where [[= :id project_id]]))) first))
          (let [entity_id (sha1/gen-key :entity entity)]
            (db/execute session
                        (hayt/insert :entities (hayt/values (-> entity
                                                                (assoc :user_id user_id :id entity_id)
                                                                (dissoc :device_ids)
                                                                (update-stringified-lists [:documents
                                                                                           :photos
                                                                                           :notes])
                                                                (update-in [:metering_point_ids] str)
                                                                (update-in [:property_data] json/encode)))))
            {::entity_id entity_id}))))))

(defn index-handle-created [handlers ctx]
  (let [request (:request ctx)
        routes  (:modular.bidi/routes request)
        id      (::entity_id ctx)]
    (if id
      (let [location (bidi/path-for routes (:entity @handlers) :entity_id id)]
        (when-not location
          (throw (ex-info "No path resolved for Location header"
                          {:entity_id id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422
                      :body "Provide valid projectId and propertyCode."}))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [id (get-in ctx [:request :route-params :entity_id])]
      (when-let [item (-> (db/execute session (hayt/select :entities (hayt/where [[= :id id]]))) first)]
        {::item item}))))

(defn resource-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          route-params (:route-params request)
          ids          (map :id (db/execute session (hayt/select :devices (hayt/where [[= :entity_id (:entity_id route-params)]]))))
          item         (::item ctx)]
      (util/render-item request (-> item
                                    (assoc :device_ids ids)
                                    (dissoc :user_id :devices))))))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request   (:request ctx)
          entity    (-> request decode-body stringify-values)
          entity_id (-> (::item ctx) :id)
          username  (sec/get-username ctx)
          ;; FIXME: Why user_id?
          user_id   (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)]
      (db/execute session (hayt/insert :entities (hayt/values (-> entity
                                                                  (assoc :user_id user_id :id entity_id)
                                                                  (dissoc :device_ids))))))))

(defn resource-delete! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session (hayt/delete :entities (hayt/where [[= :id (get-in ctx [::item :id])]])))))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defresource index [store handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store :entity)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created handlers))

(defresource resource [store handlers]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json"}
  :authorized? (authorized? store :entity)
  :exists? (partial resource-exists? store)
  :handle-ok (partial resource-handle-ok store)
  :put! (partial resource-put! store)
  :respond-with-entity? (partial resource-respond-with-entity)
  :delete! (partial resource-delete! store))
