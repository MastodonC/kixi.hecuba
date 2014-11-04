(ns kixi.hecuba.api.projects
  (:require
   [clojure.core.match :refer (match)]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.data.api :refer (decode-body authorized?) :as api]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.uuid :refer (uuid)]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.programmes :as programmes]
   [schema.core :as s]
   [kixi.amon-schema :as schema]
   [kixi.hecuba.data.entities.search :as search]))

(def ^:private programme-projects-index (p/index-path-string :programme-projects-index))
(def ^:private programme-projects-resource (p/resource-path-string :programme-projects-resource))
(def ^:private projects-index (p/index-path-string :projects-index))
(def ^:private project-resource (p/resource-path-string :project-resource))
(def ^:private project-properties-index (p/index-path-string :project-properties-index))

(defn- programme_id-from [ctx]
  (get-in ctx [:request :route-params :programme_id]))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn filter-by-allowed-ids [allowed-programme-ids allowed-project-ids projects]
  (filter #(or (some #{(:programme_id %)} allowed-programme-ids)
               (some #{(:project_id %)} allowed-project-ids)) projects))

(defmulti enrich-by-role (fn [role resource] role))
(defmethod enrich-by-role :kixi.hecuba.security/user [_ resource]
  (assoc resource :editable false))
(defmethod enrich-by-role :kixi.hecuba.security/programme-manager [_ resource]
  (assoc resource :editable true))
(defmethod enrich-by-role :kixi.hecuba.security/project-manager [_ resource]
  (assoc resource :editable true))

(defn filter-by-ids-and-roles
  ([allowed-programmes allowed-projects projects]
     (let [allowed-programme-ids (into #{} (keys allowed-programmes))
           allowed-project-ids   (into #{} (keys allowed-projects))]
       (->> projects
            (map (fn [project]
                   (let [programme-id (:programme_id project)
                         project-id   (:project_id project)]
                     (cond
                      (some #{programme-id} allowed-programme-ids) (enrich-by-role (get allowed-programmes programme-id) project)
                      (some #{project-id} allowed-project-ids) (enrich-by-role (get allowed-projects project-id) project)))))
            (remove nil?))))
  ([allowed-programmes allowed-projects projects store]
     (db/with-session [session (:hecuba-session store)]
       (let [allowed-programme-ids (into #{} (keys allowed-programmes))
             allowed-project-ids   (into #{} (keys allowed-projects))
             all-programmes        (programmes/get-all session)]
         (->> projects
              (map (fn [project]
                     (let [programme-id (:programme_id project)
                           project-id   (:project_id project)]
                       (cond
                        (some #{programme-id} allowed-programme-ids) (enrich-by-role (get allowed-programmes programme-id) project)
                        (some #{project-id} allowed-project-ids) (enrich-by-role (get allowed-projects project-id) project)
                        (-> (filter #(= programme-id (:programme_id %)) all-programmes) first :public_access) (assoc project :editable false)))))
              (remove nil?))))))

(defn allowed?*
  ([programme-id project-id allowed-programmes allowed-projects role request-method store]
     ;; Specific project
     (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s role: %s request-method: %s"
                programme-id project-id allowed-programmes allowed-projects role request-method)
     (db/with-session [session (:hecuba-session store)]
       (let [programme    (programmes/get-by-id session programme-id)
             project      (projects/get-by-id session project-id)]
         (match [(has-admin? role)
                 (has-programme-manager? programme-id allowed-programmes)
                 (has-project-manager? project-id allowed-projects)
                 (has-user? programme-id allowed-programmes project-id allowed-projects)
                 request-method]

                [true _ _ _ _]   [true {::item (assoc project :editable true)}]
                [_ true _ _ _]   [true {::item (assoc project :editable true)}]
                [_ _ true _ _]   [true {::item (assoc project :editable true)}]
                [_ _ _ true :get][true {::item (if (:public_access programme)
                                                 project
                                                 (filter-by-ids-and-roles allowed-programmes allowed-projects [project] store))}]
                :else false))))
  ([programme-id allowed-programmes allowed-projects role request-method store]
     ;; All projects for a programme-id
     (log/infof "allowed?* programme-id: %s allowed-programmes: %s allowed-projects: %s role: %s request-method: %s"
                programme-id allowed-programmes allowed-projects role request-method)
     (db/with-session [session (:hecuba-session store)]
       (let [programme    (programmes/get-by-id session programme-id)
             all-projects (projects/get-all session programme-id)]
         (match [(has-admin? role)
                 (has-programme-manager? programme-id allowed-programmes)
                 request-method]

                [true _ _]    [true {::items (mapv #(assoc % :editable true) all-projects)}]
                [_ true _]    [true {::items (mapv #(assoc % :editable true) all-projects)}]
                [_ _ :get]    [true {::items (if (:public_access programme)
                                               all-projects
                                               (filter-by-ids-and-roles allowed-programmes allowed-projects all-projects store))}]
                :else false))))
  ([allowed-programmes allowed-projects role request-method store]
     ;; All projects
     (log/infof "allowed?* allowed-programmes: %s allowed-projects: %s role: %s request-method: %s"
                allowed-programmes allowed-projects role request-method)
     (db/with-session [session (:hecuba-session store)]
       (let [all-projects (projects/get-all session)]
         (match [(has-admin? role)
                 request-method]

                [true _]   [true {::items (mapv #(assoc % :editable true) all-projects)}]
                [_ :get]   [true {::items (filter-by-ids-and-roles allowed-programmes allowed-projects all-projects store)}]
                :else false)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Index

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [programmes projects role]} (sec/current-authentication session)
          programme_id (programme_id-from ctx)]
      (if programme_id
        (allowed?* programme_id programmes projects role request-method store)
        (allowed?* programmes projects role request-method store)))))

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request]
    (case request-method
      :post (let [project (decode-body request)]
              (if (s/check schema/BaseProject project)
                true
                [false {:project project}]))
      false)))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          web-session  (-> ctx :request :session)
          programme_id (-> (:route-params request) :programme_id)
          items        (::items ctx)]
      (->> items
           (map #(-> %
                     (assoc :href (format programme-projects-resource (:programme_id %) (:project_id %))
                            :properties (format project-properties-index (:project_id %)))))
           (api/render-items request)))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [username  (sec/session-username (-> ctx :request :session))
          user_id       (:id (users/get-by-username session username))
          project       (:project ctx)
          project_id    (uuid)]
      (projects/insert session (assoc project :user_id user_id :project_id project_id))
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
        {:keys [programmes projects role]} (sec/current-authentication session)
        programme_id (programme_id-from ctx)
        project_id (project_id-from ctx)]
    (if (and programme_id project_id)
      (allowed?* programme_id project_id programmes projects role request-method store)
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
  (api/render-item ctx
                    (as-> (::item ctx) item
                          (assoc item
                            :properties (format project-properties-index (:project_id item)))
                          (dissoc item :user_id))))

(defn resource-delete! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [existing-project (::item ctx)
          {:keys [project_id]} existing-project
          response (projects/delete project_id session)
          {:keys [entities]} response]
      (doseq [entity_id entities]
        (search/delete-by-id entity_id (:search-session store)))
      "Delete Accepted")))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/edn" "application/json"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :malformed? index-malformed?
  :handle-ok (partial index-handle-ok store)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created))

(defresource resource [store]
  :allowed-methods #{:get :put :delete}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/edn" "application/json"}
  :authorized? (authorized? store)
  :allowed? (partial resource-allowed? store)
  :exists? (partial resource-exists? store)
  :malformed? resource-malformed?
  :put! (partial resource-put! store)
  :delete! (partial resource-delete! store)
  :handle-ok (partial resource-handle-ok))
