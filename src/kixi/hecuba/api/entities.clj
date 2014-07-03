(ns kixi.hecuba.api.entities
  (:require
   [clojure.core.match :refer (match)]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values sha1-regex update-stringified-lists)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.data.programmes :as programmes]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]))

(def ^:private entities-index-path (p/index-path-string :entities-index))
(def ^:private entity-resource-path (p/resource-path-string :entity-resource))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects roles request-method]
  (match [(some #(isa? % :kixi.hecuba.security/admin) roles)
          (some #(isa? % :kixi.hecuba.security/programme-manager) roles)
          (some #(= % programme-id) allowed-programmes)
          (some #(isa? % :kixi.hecuba.security/project-manager) roles)
          (some #(= % project-id) allowed-projects)
          (some #(isa? % :kixi.hecuba.security/user) roles)
          request-method]
         ;; super-admin - do everything
         [true _ _ _ _ _ _] true
         ;; programme-manager for this programme - do everything
         [_ true true _ _ _ _] true
         ;; project-manager for this project - do everything
         [_ _ _ true true _ _] true
         ;; user with this programme - get allowed
         [_ _ true _ _ true :get] true
         ;; user with this project - get allowed
         [_ _ _ _ true true :get] true
         :else false))

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [body request-method session params]} (:request ctx)
          {:keys [projects programmes roles]} (sec/current-authentication session)
          project_id (:project_id body)
          programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id projects programmes roles request-method)
        true))))

(defn resource-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes roles]}     (sec/current-authentication session)
          entity_id (:entity_id params)
          project_id (when entity_id (:project_id (entities/get-by-id (:hecuba-session store) entity_id)))
          programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id projects programmes roles request-method)
        true))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request       (-> ctx :request)
          entity        (-> request decode-body)
          project_id    (-> entity :project_id)
          property_code (-> entity :property_code)
          username      (sec/session-username (-> ctx :request :session))
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

(defn index-handle-created [ctx]
  (let [request (:request ctx)
        id      (::entity_id ctx)]
    (if id
      (let [location (format entity-resource-path id)]
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
          username  (sec/session-username (-> ctx :request :session))
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

(defresource index [store]
  :allowed-methods #{:post}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :post! (partial index-post! store)
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (resource-allowed? store)
  :exists? (partial resource-exists? store)
  :handle-ok (partial resource-handle-ok store)
  :put! (partial resource-put! store)
  :respond-with-entity? (partial resource-respond-with-entity)
  :delete! (partial resource-delete! store))
