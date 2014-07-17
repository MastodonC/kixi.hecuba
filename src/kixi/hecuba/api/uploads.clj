(ns kixi.hecuba.api.uploads
  (:require [clojure.tools.logging :as log]
            [liberator.core :refer (defresource)]
            [kixi.hecuba.webutil :refer (authorized?)]
            [kixipipe.storage.s3 :as s3]
            [kixi.hecuba.web-paths :as p]
            [kixi.hecuba.security :as sec]
            [kixi.hecuba.data.projects :as projects]
            [kixi.hecuba.data.entities :as entities]
            [clojure.core.match :refer (match)]
            [clojure.string :as str]
            [cheshire.core :as json]
            [aws.sdk.s3 :as aws]
            [clj-time.coerce :as tc]
            [kixi.hecuba.webutil :as util]
            [kixipipe.storage.s3 :as s3])) ;; TODO move to k.h.d.uploads.clj

(def ^:private entities-index-path (p/resource-path-string :uploads-resource))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; status-resource-exists?

(defn status-resource-exists? [{session :s3} ctx]
  (let [upload_id (-> ctx :request :route-params :upload_id)
        s3-key    (s3/s3-key-from {:src-name "uploads"
                                   :uuid (str upload_id "/status")})]
    (when (s3/item-exists? session s3-key)
      {::item (slurp
               (s3/get-object-by-metadata session {:key s3-key}))})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; status-resource-exists?

(defn status-resource-handle-ok [store ctx]
  (::item ctx))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; data-resource-exists?

(defn data-resource-exists? [store ctx])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; data-resource-handle-ok

(defn data-resource-handle-ok [store ctx]
  "NOT IMPLEMENTED")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; uploads-for-username

;; List of files is retrieved for a username (read from the current session) so only users who can upload files can also GET those files. Other users will get an empty list.
(defn allowed?* [programme-id project-id allowed-programmes allowed-projects roles request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects roles request-method)
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

(defn uploads-for-username-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes roles]}     (sec/current-authentication session)
          {:keys [programme_id project_id]} params]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects roles request-method)
        true))))

(defn uploads-for-username-exists? [store ctx]
  (let [request     (:request ctx)
        username    (sec/session-username (:session request))
        uploads-seq (s3/list-objects-seq (:s3 store) {:max-keys 100 :prefix (str "uploads/" username)})]
    (if (seq uploads-seq)
      [true {:files uploads-seq}]
      false)))

(defn status-from-object [store s3-key]
  (get (json/parse-string (slurp (s3/get-object-by-metadata (:s3 store) {:key s3-key})))
       "status"))

(defn merge-status-with-metadata [store s3-key]
  (let [[src-name username uuid typ] (str/split s3-key #"/")
        {:keys [auth file-bucket]} (:s3 store)
        metadata (aws/get-object-metadata auth file-bucket (str src-name "/" username "/" uuid "/data"))
        {:keys [uploads-timestamp uploads-filename]} (:user metadata)]
    (hash-map :id uuid
              :filename uploads-filename
              :timestamp (tc/to-string uploads-timestamp)
              :link ""
              :status (status-from-object store s3-key))))

(defn uploads-for-username-handle-ok [store ctx]
  (let [files    (:files ctx)
        statuses (map #(merge-status-with-metadata store (:key %))
                      (filter #(re-find #"status" (:key %)) files))]
    (util/render-items ctx statuses)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES

(defresource status-resource [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :exists? (partial status-resource-exists? store)
  :handle-ok (partial status-resource-handle-ok store))

(defresource data-resource [store]
  :allowed-methods #{:get}
  :available-media-types #{"text/csv"}
  :authorized? (authorized? store)
  :exists? (partial data-resource-exists? store)
  :handle-ok (partial data-resource-handle-ok store))

(defresource uploads-for-username [store]
  :allowed-methods #{:get}
  :allowed? (uploads-for-username-allowed? store)
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :exists? (partial uploads-for-username-exists? store)
  :handle-ok (partial uploads-for-username-handle-ok store))
