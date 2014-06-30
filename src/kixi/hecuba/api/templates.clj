(ns kixi.hecuba.api.templates
  (:require [cheshire.core :as json]
            [clojure.set :as set]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.entities :as entities]
            [kixi.hecuba.security :as sec]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.storage.sha1 :as sha1]
            [kixi.hecuba.web-paths :as p]
            [kixi.hecuba.webutil :as util]
            [kixi.hecuba.webutil :refer (request-method-from-context decode-body authorized? stringify-values update-stringified-lists sha1-regex uuid)]
            [liberator.core :refer (defresource)]
            [liberator.representation :refer (ring-response)]
            [qbits.hayt :as hayt])
  )
(def ^:private templates-resource (p/resource-path-string :templates-resource))
(def ^:private entity-templates-resource (p/resource-path-string :entity-templates-resource))

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

(defn- relocate-customer-ref [m]
  (-> m
      (dissoc :metadata)
      (assoc :customer_ref (get-in m [:metadata :customer_ref]))))

(defn- relocate-location [m]
  (assoc m :location (get-in m [:location :name])))

(def ^:private extract-columns-in-order (juxt
                                         :device_id
                                         :type
                                         :customer_ref
                                         :description
                                         :location
                                         :accuracy
                                         :resolution
                                         :frequency
                                         :period
                                         :parent_id
                                         :max
                                         :min))

(def ^:private headers-in-order ["Device UUID"
                                 "Reading Type"
                                 "Customer Ref"
                                 "Description"
                                 "Location"
                                 "Accuracy (percent)"
                                 "Sample Interval (seconds)"
                                 "Frequency"
                                 "Period"
                                 "Parent UUID"
                                 "Sensor Range Max"
                                 "Sensor Range Min"])

(defn- join-to-device [devices x]
  (let [device_id (:device_id x)]
    (merge (get devices device_id) x)))

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

(defn entity-resource-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [entity_id       (:id (:kixi.hecuba.api.entities/item ctx))
          devices         (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
                               (map (fn [x] [(:id x) x]))
                               (into {}))
          sensors         (db/execute session (hayt/select :sensors (hayt/where [[:in :device_id (keys devices)]])))
          device-and-type #(str (:device_id %) "-" (:type %))
          items           (->> sensors
                               (map (partial join-to-device devices))
                               (sort-by device-and-type)
                               (map relocate-customer-ref)
                               (map relocate-location)
                               (map extract-columns-in-order)
                               (apply map vector)
                               (map #(apply vector %1 %2) headers-in-order))]
      (ring-response {:headers {"Content-Disposition" (str "attachment; filename=" entity_id "_template.csv")}
                      :body (util/render-items ctx items)}))))

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

(defresource entity-resource [store]
  :allowed-methods #{:get}
  :available-media-types #{"text/csv"}
  :known-content-type? #{"text/csv"}
  :authorized? (authorized? store)
  :exists? (partial entities/resource-exists? store)
  :handle-ok (partial entity-resource-handle-ok store))
