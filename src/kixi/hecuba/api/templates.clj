(ns kixi.hecuba.api.templates
  (:require [cheshire.core :as json]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.entities :as entities]
            [kixipipe.pipeline :as pipe]
            [kixi.hecuba.security :as sec]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.storage.sha1 :as sha1]
            [kixi.hecuba.web-paths :as p]
            [kixi.hecuba.webutil :as util]
            [kixi.hecuba.webutil :refer (request-method-from-context decode-body authorized? stringify-values update-stringified-lists sha1-regex uuid)]
            [liberator.core :refer (defresource)]
            [liberator.representation :refer (ring-response)]
            [qbits.hayt :as hayt]
            [kixi.hecuba.data.measurements.download :as measurements-download]))

(def ^:private templates-resource (p/resource-path-string :templates-resource))
(def ^:private entity-templates-resource (p/resource-path-string :entity-templates-resource))
(def ^:private uploads-status-resource-path (p/resource-path-string :uploads-status-resource))

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

(defn entity-resource-handle-ok [store pipe ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [entity_id (-> ctx :kixi.hecuba.api.entities/item :id)
          data?     (-> ctx :request :params "data")
          user_id   (-> ctx :auth :user_id)
          uuid      (uuid)
          item      {:dest :downloads :type :measurements}
          location  (format uploads-status-resource-path user_id uuid)]
      (if data?
        (do  (pipe/submit-item pipe (assoc item
                                      :uuid uuid
                                      :auth (:auth ctx)))
             {:response {:status 202
                         :headers {"Location" location}
                         :body "Accepted"}})
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
  :available-media-types #{"text/csv"}
  :known-content-type? #{"text/csv"}
  :authorized? (authorized? store)
  :exists? (partial entities/resource-exists? store)
  :handle-ok (partial entity-resource-handle-ok store pipeline))
