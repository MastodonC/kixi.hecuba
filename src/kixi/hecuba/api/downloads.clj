(ns kixi.hecuba.api.downloads
  (:require [clojure.tools.logging :as log]
            [liberator.core :refer (defresource)]
            [kixi.hecuba.webutil :refer (authorized?)]
            [kixipipe.storage.s3 :as s3]
            [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
            [kixi.hecuba.data.projects :as projects]
            [kixi.hecuba.data.entities :as entities]
            [kixi.hecuba.data.measurements.core :as mc]
            [clojure.core.match :refer (match)]
            [clojure.string :as str]
            [aws.sdk.s3 :as aws]
            [clj-time.coerce :as tc]
            [kixi.hecuba.webutil :as util]
            [cheshire.core :as json]
            [kixipipe.storage.s3 :as s3]
            [liberator.representation :refer (ring-response)])) 

;; List of files is retrieved for a username (read from the current session) so only users who can upload files can also GET those files. Other users will get an empty list.
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
;; Downloads for entity

(defn downloads-for-entity-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes roles]}     (sec/current-authentication session)
          {:keys [programme_id project_id]} params]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects roles request-method)
        true))))

(defn status-from-object [store s3-key]
  (with-open [in (s3/get-object-by-metadata (:s3 store) {:key s3-key})]
    (get (json/parse-string (slurp in) keyword) :status)))

(defn merge-downloads-status-with-metadata [store s3-key entity_id]
  (let [{:keys [auth file-bucket]} (:s3 store)
        metadata (aws/get-object-metadata auth file-bucket s3-key) ;; FIXME this call to aws/get-object-metadata should be to a fn in kixipipe, passed an item map with uuid set to the generated string.
        {:keys [downloads-timestamp downloads-filename]} (:user metadata)]
    (hash-map :filename downloads-filename
              :timestamp (tc/to-string downloads-timestamp)
              :link (str "/4/download/" entity_id "/data")
              :status (status-from-object store s3-key))))

;; TOFIX We currently store one file per entity only.
(defn downloads-for-entity-status-handle-ok [store ctx]
  (let [{:keys [params session]} (:request ctx)
        {:keys [entity_id]} params
        files    (take 2 (s3/list-objects-seq (:s3 store) {:prefix (str "downloads/" entity_id)}))
        statuses (map #(merge-downloads-status-with-metadata store (:key %) entity_id)
                      (filter #(re-find #"status" (:key %)) files))]
    (util/render-items ctx statuses)))

(defn downloads-for-entity-data-resource-handle-ok [store ctx]
  (let [{:keys [params session]} (:request ctx)
        {:keys [entity_id]} params
        {:keys [auth file-bucket]} (:s3 store)
        data-files (filter #(re-find #"data" (:key %)) (s3/list-objects-seq (:s3 store) {:max-keys 100 :prefix (str "downloads/" entity_id)}))
        file (s3/get-object-by-metadata (:s3 store) {:key (:key (first data-files))})]
    (ring-response {:headers  {"Content-Disposition" (str "attachment; filename=" entity_id "_measurements.csv")}
                    :body (util/render-item ctx file)})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES

(defresource downloads-data-resource [store]
  :allowed-methods #{:get}
  :available-media-types #{"text/csv"}
  :authorized? (authorized? store)
  :allowed? (downloads-for-entity-allowed? store)
  :handle-ok (partial downloads-for-entity-data-resource-handle-ok store))

(defresource downloads-for-entity [store]
  :allowed-methods #{:get}
  :allowed? (downloads-for-entity-allowed? store)
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial downloads-for-entity-status-handle-ok store))
