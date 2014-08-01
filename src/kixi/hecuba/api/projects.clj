(ns kixi.hecuba.api.projects
  (:require
   [clojure.core.match :refer (match)]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex) :as util]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.data.projects :as projects]))

(def ^:private programme-projects-index (p/index-path-string :programme-projects-index))
(def ^:private programme-projects-resource (p/resource-path-string :programme-projects-resource))
(def ^:private projects-index (p/index-path-string :projects-index))
(def ^:private project-resource (p/resource-path-string :project-resource))
(def ^:private project-properties-index (p/index-path-string :project-properties-index))

(defn- programme_id-from [ctx]
  (get-in ctx [:request :route-params :programme_id]))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects roles request-method]
    (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
               programme-id project-id allowed-programmes allowed-projects roles request-method)

  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (some #(= % programme-id) allowed-programmes)
          (has-project-manager? roles)
          (some #(= % project-id) allowed-projects)
          (has-user? roles)
          request-method]

         [true _ _ _ _ _ _] true
         [_ true true _ _ _ _] true
         [_ _ _ true true _ _] true
         [_ _ true _ _ true :get] true
         [_ _ _ _ true true :get] true
         :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Index

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [programmes projects roles]} (sec/current-authentication session)
          programme_id (:programme_id (:project ctx))]
      (if programme_id
        (allowed?* programme_id nil programmes projects roles request-method)
        true))))

(defn editable-projects [projects allowed-programmes allowed-projects roles]
  (map #(let [editable (allowed?* (:programme_id %) (:project_id %)
                                  allowed-programmes allowed-projects roles
                                  :put)]
          (if editable 
            (assoc % :editable editable)
            %)) projects))

(defn items->authz-items [session items]
  (let [{:keys [roles programmes projects]} (sec/current-authentication session)]
    (log/debugf "Roles: %s Programmes: %s Projects: %s" roles programmes projects)
    (if (some #(isa? % ::sec/admin) roles)
      (map #(assoc % :editable true) items)
      (editable-projects (filter #(or (programmes (:programme_id %)) 
                                      (projects (:id %)))
                                 items) programmes projects roles))))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          web-session  (-> ctx :request :session)
          programme_id (-> (:route-params request) :programme_id)
          items        (projects/get-all session programme_id)]
      (->> items
           (items->authz-items web-session)
           (map #(-> %
                     (assoc :href (format programme-projects-resource (:programme_id %) (:id %))
                            :properties (format project-properties-index (:id %)))))
           (util/render-items request)))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          username  (sec/session-username (-> ctx :request :session))
          user_id       (:id (users/get-by-username session username))
          project       (-> request decode-body stringify-values)
          project_id    (if-let [id (:id project)] id (sha1/gen-key :project project))]
      (projects/insert session (assoc project :user_id user_id :id project_id))
      {::project_id project_id})))

;; FIXME: Should return programmes/%s/projects/%s
(defn index-handle-created [ctx]
  (let [request  (:request ctx)
        location (format project-resource (::project_id ctx))]
    (ring-response {:headers {"Location" location}
                    :body (json/encode {:location location
                                        :status "OK"
                                        :version "4"})})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource

(defn resource-allowed? [store ctx]
  (let [{:keys [body request-method session params]} (:request ctx)
        {:keys [programmes projects roles]} (sec/current-authentication session)
        programme_id (programme_id-from ctx)
        project_id (project_id-from ctx)]
    (if programme_id
      [(allowed?* programme_id project_id programmes projects roles request-method)
       {:editable (allowed?* programme_id project_id programmes projects roles :put)}]
      true)))

(defn resource-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request]
    (case request-method
      :post (let [project (decode-body request)
                  {:keys [name]} project]
              (if name
                [false {:project project}]
                true))
      :put (let [project (decode-body request)
                 {:keys [programme_id project_id]} route-params]
             (if (and programme_id project_id)
               [false {:project (assoc project :id project_id)}]
               true))
      false)))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request project]} ctx
          username (sec/session-username (-> ctx :request :session))]
      (when project
        (projects/update session (:id project) (assoc project :user_id username))))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (when-let [item (projects/get-by-id session (project_id-from ctx))]
      {::item item})))

(defn resource-handle-ok [ctx]
  (let [request (:request ctx)]
    (util/render-item request
                      (as-> (::item ctx) item
                            (assoc item
                              :properties (format project-properties-index :project_id (:id item)))
                            (dissoc item :user_id)))))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/edn" "application/json"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :handle-ok (partial index-handle-ok store)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created))

(defresource resource [store]
  :allowed-methods #{:get :put}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/edn" "application/json"}
  :authorized? (authorized? store)
  :allowed? (partial resource-allowed? store)
  :exists? (partial resource-exists? store)
  :malformed? resource-malformed?
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok))
