(ns kixi.hecuba.api.uploads
  (:require [clojure.tools.logging :as log]
            [liberator.core :refer (defresource)]
            [kixi.hecuba.webutil :refer (authorized?)]
            [kixipipe.storage.s3 :as s3]
            [kixi.hecuba.web-paths :as p]))

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
