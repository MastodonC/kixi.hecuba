(ns kixi.hecuba.api.templates
  (:require [cheshire.core :as json]
            [clj-time.core :as t]
            [clojure.set :as set]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.entities :as entities]
            [kixi.hecuba.data.measurements.core :refer (get-status write-status)]
            [kixi.hecuba.data.measurements.download :as measurements-download]
            [kixi.hecuba.security :as sec]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.storage.sha1 :as sha1]
            [kixi.hecuba.web-paths :as p]
            [kixi.hecuba.webutil :as util]
            [kixi.hecuba.webutil :refer (request-method-from-context decode-body authorized? stringify-values update-stringified-lists sha1-regex uuid)]
            [kixipipe.pipeline :as pipe]
            [kixipipe.storage.s3 :as s3]
            [liberator.core :refer (defresource)]
            [liberator.representation :refer (ring-response)]
            [qbits.hayt :as hayt]))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCE FUNCTIONS
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-exists?

(defmulti index-exists? request-method-from-context)

(defmethod index-exists? :get [store ctx]
  (db/with-session [session (:hecuba-session store)]
    {::items (db/execute session (hayt/select :csv_templates))}))

(defmethod index-exists? :default [store ctx] true)


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; malformed?

(defmulti malformed? request-method-from-context :default :default)

(defmethod malformed? :post [ctx]
  (let [{:strs [name template]} (-> ctx :request :multipart-params)]
    (not (valid-template-request? name template))))

(defmethod malformed? :default [_] false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-post!

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [payload (payload-from (-> ctx :request :multipart-params))
          result  (db/execute session
                              (hayt/insert :csv_templates
                                           payload))]
      (log/info "Created new template with name " (:name result))
      {::location (format templates-resource (:id result))})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-ok?

(defn index-handle-ok [ctx]
    (let [items (::items ctx)]
    (util/render-items ctx items)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-created?

(defn index-handle-created [ctx]
  (let [location (:location_id ctx)]
    {:headers {"Location" location}
     :body    (json/encode {:location location
                            :status "OK"
                            :version "4"})}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-exists?

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-respond-with-entity?

(defmulti resource-respond-with-entity? request-method-from-context)

(defmethod resource-respond-with-entity? :default [_] true)
(defmethod resource-respond-with-entity? :delete [_] false)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-put!

(defn resource-put! [store {request :request}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [multipart-params route-params]} request
          template_id                             (:template_id route-params)]
      (log/info "executing...")
      (let [result (db/execute session (hayt/update :csv_templates
                                                    (hayt/set-columns (payload-from nil multipart-params))
                                                    (hayt/where [[= :id template_id]])))]
        {::location (format templates-resource (:id result))}))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; resource-handle-ok

(defn resource-handle-ok [ctx]
  (let [{:keys [filename name template]} (::item ctx)]
    (ring-response {:headers {"Content-Disposition" (str "attachment; filename=" filename)}
                    :body template})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; entity-resource-handle-ok

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
          data?     (-> ctx :request :query-params (get "data"))
          session   (-> ctx :request :session)
          username  (sec/session-username session)
          auth      (sec/current-authentication session)
          item      {:src-name "downloads" :dest :download :type :measurements :entity_id entity_id}]
      (if data?
        (queue-data-generation store pipe username item)
        (ring-response {:headers {"Content-Disposition" (str "attachment; filename=" entity_id "_template.csv")}
                        :body (util/render-items ctx (measurements-download/get-header store entity_id))})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types #{"text/csv"}
  :known-content-type? #{"text/csv"}
  :authorized? (authorized? store)
  :exists? (partial index-exists? store)
  :malformed? malformed?
  :post! (partial index-post! store)
  :handle-ok index-handle-ok
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"text/csv"}
  :authorized? (authorized? store)
  :exists? (partial resource-exists? store)
  :new? (constantly false)
  :malformed? malformed?
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok store))

(defresource entity-resource [store pipeline]
  :allowed-methods #{:get}
  :available-media-types #{"text/csv" "application/edn"}
  :known-content-type? #{"text/csv"}
  :authorized? (authorized? store)
  :exists? (partial entities/resource-exists? store)
  :handle-ok (partial entity-resource-handle-ok store pipeline))
