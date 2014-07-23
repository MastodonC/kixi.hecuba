(ns kixi.hecuba.api.uploads
  (:require [clojure.tools.logging :as log]
            [liberator.core :refer (defresource)]
            [kixi.hecuba.webutil :refer (authorized?)]
            [kixipipe.storage.s3 :as s3]
            [kixi.hecuba.web-paths :as p]
            [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
            [kixi.hecuba.data.projects :as projects]
            [kixi.hecuba.data.entities :as entities]
            [kixi.hecuba.data.measurements.core :as mc]
            [clojure.core.match :refer (match)]
            [clojure.string :as str]
            [cheshire.core :as json]
            [aws.sdk.s3 :as aws]
            [clj-time.coerce :as tc]
            [kixi.hecuba.webutil :as util]
            [kixipipe.storage.s3 :as s3]
            [liberator.representation :refer (ring-response)]))

(def ^:private uploads-status-path (p/resource-path-string :uploads-status-resource))
(def ^:private uploads-data-path (p/resource-path-string :uploads-data-resource))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; status-resource-exists?

(defn uploads-status-resource-exists? [{session :s3} ctx]
  (let [upload_id (-> ctx :request :route-params :upload_id)
        user_id (-> ctx :request :route-params :user_id)
        s3-key    (s3/s3-key-from {:src-name "uploads"
                                   :uuid (format uploads-status-path user_id upload_id)})]
    (when (s3/item-exists? session s3-key)
      {::item (slurp
               (s3/get-object-by-metadata session {:key s3-key}))})))

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
          {:keys [projects programmes roles]}     (sec/current-authentication session)
          {:keys [programme_id project_id]} params]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects roles request-method)
        true))))

(defn merge-uploads-status-with-metadata [store s3-key]
  (let [[_ _ uuid _] (str/split s3-key #"/")
        {:keys [auth file-bucket]} (:s3 store)
        metadata (aws/get-object-metadata auth file-bucket (str/replace s3-key #"status" "data")) ;; FIXME this call to aws/get-object-metadata should be to a fn in kixipipe, passed an item map with uuid set to the generated string.
        {:keys [uploads-timestamp uploads-filename]} (:user metadata)]
    (hash-map :filename uploads-filename
              :timestamp (tc/to-string uploads-timestamp)
              :uuid uuid
              :status (status-from-object store s3-key))))

(defn uploads-for-username-handle-ok [store ctx]
  (let [{:keys [params session]} (:request ctx)
        {:keys [entity_id]} params
        username (sec/session-username session)
        files    (s3/list-objects-seq (:s3 store) {:max-keys 100 :prefix (str "uploads/" username "/" entity_id)})
        statuses (map #(merge-uploads-status-with-metadata store (:key %))
                      (filter #(re-find #"status" (:key %)) files))]
    (util/render-items ctx statuses)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES

(defresource uploads-status-resource [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
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
