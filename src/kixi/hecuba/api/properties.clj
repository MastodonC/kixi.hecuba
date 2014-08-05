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
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixipipe.storage.s3 :as s3]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Properties for entity

(def ^:private entity-resource (p/resource-path-string :entity-resource))

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

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-allowed? [store ctx]
  (let [{:keys [body request-method session params]} (:request ctx)
        {:keys [projects programmes roles]} (sec/current-authentication session)
        project_id (project_id-from ctx)
        programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
    (if (and project_id programme_id)
      [(allowed?* programme_id project_id programmes projects roles request-method)
       {:editable (allowed?* programme_id project_id programmes projects roles :put)}]
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

(defn upload->uri [store s3-key]
  (s3/generate-presigned-url (:s3 store) s3-key))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          editable (:editable ctx)
          ;; TOFIX use k.h.d.entities
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
                                     :photos (map (partial upload->uri store) (:photos %))
                                     :documents (map (partial upload->uri store) (:documents %))
                                     :devices (devices/->clojure (:id %) session)
                                     :profiles (profiles/->clojure (:id %) session)
                                     :href (format entity-resource (:id %)))
                                 (cond-> editable (assoc :editable editable)))))]
      coll)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; All properties

(defn allowed-all?* [allowed-programmes allowed-projects roles request-method session]
  (log/infof "allowed-all?* allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s" allowed-programmes allowed-projects roles request-method)
  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (has-project-manager? roles)
          (has-user? roles)
          request-method]

         [true _ _ _ _] [true {:projects
                               (into #{} (map :id (projects/get-all session)))}]
         [_ true _ _ _] [true {:projects
                               (into #{} (map :id (mapcat #(projects/get-all session %)
                                                          allowed-programmes)))}]
         [_ _ true _ _] [true {:projects allowed-projects}]
         [_ _ _ true :get] [true {:projects allowed-projects}]
         :else false))

(defn index-all-allowed? [store ctx]
  (let [{:keys [body request-method session params]} (:request ctx)
        {:keys [programmes projects roles]} (sec/current-authentication session)]
    (allowed-all?* programmes projects roles request-method (:hecuba-session store))))

(defn index-all-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [projects (:projects ctx)
          coll     (->> (entities/get-all session)
                        (map #(assoc :devices (devices/->clojure (:id %) session))))]
      coll)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resources

(defresource index [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (partial index-allowed? store)
  :handle-ok (partial index-handle-ok store))

(defresource index-all [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (partial index-all-allowed? store)
  :handle-ok (partial index-all-handle-ok store))
