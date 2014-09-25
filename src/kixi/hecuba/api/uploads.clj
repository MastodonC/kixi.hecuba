(ns kixi.hecuba.api.uploads
  (:require [cheshire.core :as json]
            [clj-time.coerce :as tc]
            [clojure.core.match :refer (match)]
            [clojure.tools.logging :as log]
            [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
            [kixi.hecuba.web-paths :as p]
            [kixi.hecuba.webutil :as util]
            [kixi.hecuba.webutil :refer (authorized?)]
            [kixipipe.storage.s3 :as s3]
            [liberator.core :refer (defresource)]))

(def ^:private uploads-status-path (p/resource-path-string :uploads-status-resource))
(def ^:private uploads-data-path (p/resource-path-string :uploads-data-resource))

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
;; status-resource-exists?

(defn uploads-status-resource-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes role]} (sec/current-authentication session)
          {:keys [programme_id project_id]} params]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {::item {:upload_id (-> ctx :request :route-params :upload_id)
                  :username (-> ctx :request :route-params :user_id)}}]
        true))))

(defn uploads-status-resource-exists? [{session :s3} ctx]
  (let [{:keys [upload_id username]} (::item ctx)
        s3-key (s3/s3-key-from {:src-name "uploads" :username username :uuid upload_id})]
    (when (s3/item-exists? session s3-key)
      {::item (with-open [in (s3/get-object-by-metadata session {:key s3-key})] (slurp in))})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; status-resource-exists?

(defn uploads-status-resource-handle-ok [store ctx]
  (::item ctx))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; data-resource-exists?

(defn uploads-data-resource-exists? [store ctx])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; data-resource-handle-ok

(defn uploads-data-resource-handle-ok [store ctx]
  "NOT IMPLEMENTED")

(defn status-from-object [store s3-key]
  (with-open [in (s3/get-object-by-metadata (:s3 store) {:key s3-key})]
    (get (json/parse-string (slurp in) keyword) :status)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; uploads-for-username

(defn uploads-for-username-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes role]} (sec/current-authentication session)
          {:keys [programme_id project_id entity_id]} params]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {::item {:upload_id (-> ctx :request :route-params :upload_id)
                  :username (-> ctx :request :route-params :user_id)
                  :entity_id entity_id}}]
        true))))

(defn merge-uploads-status-with-metadata [store s3-object]
  (let [session (:s3 store)
        metadata (s3/get-user-metadata-from-s3-object session s3-object)
        {:keys [uploads-timestamp uploads-filename]} metadata]
    (hash-map :filename uploads-filename
              :timestamp (tc/to-string uploads-timestamp)
              :status (status-from-object store (:key s3-object)))))

(defn uploads-for-username-handle-ok [store ctx]
  (let [{:keys [username entity_id]} (::item ctx)
        files    (s3/list-objects-seq (:s3 store) {:max-keys 100 :prefix (str "uploads/" username "/" entity_id)})
        statuses (map #(merge-uploads-status-with-metadata store %)
                      (filter #(re-find #"status" (:key %)) files))]
    (util/render-items ctx statuses)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES

(defresource uploads-status-resource [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :allowed? (uploads-status-resource-allowed? store)
  :authorized? (authorized? store)
  :exists? (partial uploads-status-resource-exists? store)
  :handle-ok (partial uploads-status-resource-handle-ok store))

(defresource uploads-for-username [store]
  :allowed-methods #{:get}
  :allowed? (uploads-for-username-allowed? store)
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial uploads-for-username-handle-ok store))

(defresource uploads-data-resource [store]
  :allowed-methods #{:get}
  :available-media-types #{"text/csv"}
  :authorized? (authorized? store)
  :exists? (partial uploads-data-resource-exists? store)
  :handle-ok (partial uploads-data-resource-handle-ok store))
