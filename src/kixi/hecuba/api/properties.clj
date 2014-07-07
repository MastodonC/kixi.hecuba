(ns kixi.hecuba.api.properties
  (:require
   [clojure.core.match :refer (match)]
   [cheshire.core :as json]
   [hickory.core :as hickory]
   [clojure.edn :as edn]
   [clojure.tools.logging :as log]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex) :as util]
   [liberator.core :refer (defresource)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.data.profiles :as profiles]
   [kixi.hecuba.data.devices :as devices]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.data.projects :as projects]))

(def ^:private entity-resource (p/resource-path-string :entity-resource))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects roles request-method]
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

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-allowed? [store ctx]
  (let [{:keys [body request-method session params]} (:request ctx)
        {:keys [projects programmes roles]} (sec/current-authentication session)
        project_id (project_id-from ctx)
        programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
    (if (and project_id programme_id)
      [(allowed?* programme_id project_id programmes projects roles request-method)
       {:editable (allowed?* programme_id project_id projects programmes roles :put)}]
      true)))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn- tech-icons [property_data]
  (if-let [icons (:technology_icons property_data)]
    (assoc
        property_data
      :technology_icons (->> (hickory/parse-fragment icons)
                             (map (fn [ti] (-> ti hickory/as-hickory :attrs :src)))
                             (keep identity)
                             (map #(clojure.string/replace % ".jpg" ".png"))))
    property_data))

;; FIXME: This is only here because the data is currently dirty and
;; some of the property data is in edn rather than json.
(defn parse-property-data [property_data]
  (try
    (json/parse-string property_data keyword)
    (catch Throwable t
      (log/errorf "Got edn property data when we expected json: %s" property_data)
      (edn/read-string property_data))))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          editable (:editable ctx)
          coll    (->> (db/execute session
                                   (if-let [project_id (project_id-from ctx)]
                                     (hayt/select :entities (hayt/where [[= :project_id project_id]]))
                                     (hayt/select :entities)))
                       (map #(-> %
                                 (assoc
                                     :property_data (if-let [property_data (:property_data %)]
                                                      (-> property_data
                                                          parse-property-data
                                                          tech-icons)
                                                      {})
                                     :photos (if-let [photos (:photos %)] (mapv (fn [p] (json/parse-string p keyword)) photos) [])
                                     :documents (if-let [docs (:documents %)] (mapv (fn [d] (json/parse-string d keyword)) docs) [])
                                     :devices (devices/->clojure (:id %) session)
                                     :profiles (profiles/->clojure (:id %) session)
                                     :href (format entity-resource (:id %)))
                                 (cond-> editable (assoc :editable editable)))))]
      coll)))

(defresource index [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (partial index-allowed? store)
  :handle-ok (partial index-handle-ok store))
