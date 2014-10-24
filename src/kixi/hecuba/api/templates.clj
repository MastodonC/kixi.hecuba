(ns kixi.hecuba.api.templates
  (:require [cheshire.core :as json]
            [clj-time.core :as t]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.entities :as entities]
            [kixi.hecuba.data.measurements.core :refer (get-status write-status)]
            [kixi.hecuba.data.measurements.download :as measurements-download]
            [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.storage.uuid :refer (uuid)]
            [kixi.hecuba.web-paths :as p]
            [kixi.hecuba.webutil :as util]
            [kixi.hecuba.webutil :refer (request-method-from-context decode-body authorized?)]
            [kixipipe.pipeline :as pipe]
            [kixipipe.storage.s3 :as s3]
            [liberator.core :refer (defresource)]
            [liberator.representation :refer (ring-response)]
            [qbits.hayt :as hayt]
            [clojure.core.match :refer (match)]
            [kixi.hecuba.data.entities.search :as search]))

(def ^:private templates-resource (p/resource-path-string :templates-resource))
(def ^:private entity-templates-resource (p/resource-path-string :entity-templates-resource))
(def ^:private uploads-status-resource-path (p/resource-path-string :uploads-status-resource))
(def ^:private downloads-status-resource-path (p/resource-path-string :downloads-status-resource))

(defmethod kixipipe.storage.s3/s3-key-from "downloads" downloads-s3-key-from [item]
  (let [suffix (get item :suffix "data")]
    (str "downloads/"(:entity_id item) "/" suffix)))

(defmethod kixipipe.storage.s3/item-from-s3-key "downloads" downloads-item-from-s3-key [key]
  (when-let [[src-name entity_id] (next (re-matches #"^([^/]+)/([^/]+)$" key))]
    {:src-name src-name :entity_id entity_id}))

(defn- payload-from
  ([multipart] (payload-from (uuid) multipart))
  ([id multipart]
     (let [{:strs [name template]} multipart
           {:keys [size tempfile content-type filename]} template]
       (cond-> {:name name
                :filename filename
                :template (slurp tempfile)}
               id (assoc :id id)))))

(defn- valid-template-request? [name {:keys [size tempfile content-type filename]}]
  (and (pos? size)
       tempfile
       content-type
       filename
       name))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects role request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects role request-method)
  (match  [(has-admin? role)
           (has-programme-manager? programme-id allowed-programmes)
           (has-project-manager? project-id allowed-projects)
           (has-user? programme-id allowed-programmes project-id allowed-projects)
           request-method]

          [true _ _ _ _]    true
          [_ true _ _ _]    true
          [_ _ true _ _]    true
          [_ _ _ true :get] true
          :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INDEX FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn index-allowed? [store]
  (fn [ctx]
    (let [request (:request ctx)
          {:keys [request-method session params]} request
          {:keys [projects programmes role]} (sec/current-authentication session)
          {:keys [programme_id project_id entity_id]} params]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {:request request}]
        true))))

(defmulti index-exists? request-method-from-context)

(defmethod index-exists? :get [store ctx]
  (db/with-session [session (:hecuba-session store)]
    {::items (db/execute session (hayt/select :csv_templates))}))

(defmethod index-exists? :default [store ctx] true)

(defmulti malformed? request-method-from-context :default :default)

(defmethod malformed? :post [ctx]
  (let [{:strs [name template]} (-> ctx :request :multipart-params)]
    (not (valid-template-request? name template))))

(defmethod malformed? :default [_] false)

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [payload (payload-from (-> ctx :request :multipart-params))
          result  (db/execute session
                              (hayt/insert :csv_templates
                                           payload))]
      (log/info "Created new template with name " (:name result))
      {::location (format templates-resource (:id result))})))

(defn index-handle-ok [ctx]
    (let [items (::items ctx)]
    (util/render-items ctx items)))

(defn index-handle-created [ctx]
  (let [location (:location_id ctx)]
    {:headers {"Location" location}
     :body    (json/encode {:location location
                            :status "OK"
                            :version "4"})}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCE FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn resource-allowed? [store]
  (fn [ctx]
    (let [request (:request ctx)
          {:keys [request-method session params]} request
          {:keys [projects programmes role]} (sec/current-authentication session)
          {:keys [programme_id project_id entity_id]} params]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {:request request}]
        true))))

(defmulti resource-exists? request-method-from-context)

(defmethod resource-exists? :get [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [template_id (-> ctx :request :route-params :template_id)
          item (first (db/execute session
                           (hayt/select :csv_templates
                                        (hayt/where [[= :id template_id]]))))]
      (when item
        {::item item}))))

(defmethod resource-exists? :default [_ _] true)

(defmulti resource-respond-with-entity? request-method-from-context)

(defmethod resource-respond-with-entity? :default [_] true)
(defmethod resource-respond-with-entity? :delete [_] false)

(defn resource-put! [store {request :request}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [multipart-params route-params]} request
          template_id                             (:template_id route-params)]
      (log/info "executing...")
      (let [result (db/execute session (hayt/update :csv_templates
                                                    (hayt/set-columns (payload-from nil multipart-params))
                                                    (hayt/where [[= :id template_id]])))]
        {::location (format templates-resource (:id result))}))))


(defn resource-handle-ok [ctx]
  (let [{:keys [filename name template]} (::item ctx)]
    (ring-response {:headers {"Content-Disposition" (str "attachment; filename=" filename)}
                    :body template})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ENTITY RESOURCE FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn entity-resource-allowed? [store]
  (fn [ctx]
    (let [request (:request ctx)
          {:keys [request-method session params]} request
          {:keys [projects programmes role]} (sec/current-authentication session)
          {:keys [entity_id]} params
          entity (search/get-by-id entity_id (:search-session store))
          {:keys [project_id programme_id]} entity]
      (when (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {:request request :entity entity}]))))

(defn entity-resource-exists? [store ctx]
  (when-let [entity (seq (:entity ctx))]
    {:request (:request ctx)}))

(defn queue-data-generation [store pipe username item]
  (let [entity_id (:entity_id item)
        location (format downloads-status-resource-path username entity_id)
        status   (get-status store item)]
    (if (= status "PENDING")
      {:response {:status 303
                  :headers {"Location" location}
                  :body "In Progress"}}
      (do
        (write-status store (assoc (-> item
                                       (assoc :metadata  {:timestamp (t/now)
                                                          :content-type "text/csv"
                                                          :filename "measurements.csv"}
                                              :suffix "status"))
                              :status "PENDING"))
        (pipe/submit-item pipe item)
        {:response {:status 202
                    :headers {"Location" location}
                    :body "Accepted"}}))))

(defn entity-resource-handle-ok [store pipe ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [entity_id (-> ctx :request :params :entity_id)
          data?     (= "true" (-> ctx :request :query-params (get "data")))
          session   (-> ctx :request :session)
          username  (sec/session-username session)
          auth      (sec/current-authentication session)
          item      {:src-name "downloads" :dest :download :type :measurements :entity_id entity_id}]
      (if data?
        (queue-data-generation store pipe username item)
        (ring-response {:headers {"Content-Disposition" (str "attachment; filename=" entity_id "_template.csv")}
                        :body (util/render-items ctx (measurements-download/get-header store entity_id))})))))


(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types #{"text/csv"}
  :known-content-type? #{"text/csv"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :exists? (partial index-exists? store)
  :malformed? malformed?
  :post! (partial index-post! store)
  :handle-ok index-handle-ok
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"text/csv"}
  :authorized? (authorized? store)
  :allowed? (resource-allowed? store)
  :exists? (partial resource-exists? store)
  :new? (constantly false)
  :malformed? malformed?
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok store))

(defresource entity-resource [store pipeline]
  :allowed-methods #{:get}
  :available-media-types #{"text/csv"}
  :known-content-type? #{"text/csv"}
  :authorized? (authorized? store)
  :allowed? (entity-resource-allowed? store)
  :exists? (partial entity-resource-exists? store)
  :handle-ok (partial entity-resource-handle-ok store pipeline))
