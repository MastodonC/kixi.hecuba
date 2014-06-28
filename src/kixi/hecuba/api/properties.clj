(ns kixi.hecuba.api.properties
  (:require
   [cheshire.core :as json]
   [hickory.core :as hickory]
   [clojure.edn :as edn]
   [clojure.tools.logging :as log]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex) :as util]
   [liberator.core :refer (defresource)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.data.profiles :as profiles]
   [kixi.hecuba.web-paths :as p]))

(def ^:private entity-resource (p/resource-path-string :entity-resource))

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

(defn- property-devices [entity_id session]
  (if-let [devices (db/execute session
                               (hayt/select
                                :devices
                                (hayt/where [[= :entity_id entity_id]])))]
    (mapv (fn [d]
            (assoc d :readings
                   (map (fn [sensor] (dissoc sensor :user_id))
                        (db/execute session
                                    (hayt/select :sensors
                                                 (hayt/where [[= :device_id (:id d)]]))))))
          devices)
    []))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          coll    (->> (db/execute session
                                   (if-let [project_id (project_id-from ctx)]
                                     (hayt/select :entities (hayt/where [[= :project_id project_id]]))
                                     (hayt/select :entities)))
                       (map #(assoc %
                               :property_data (if-let [property_data (:property_data %)]
                                                (-> property_data
                                                    parse-property-data
                                                    tech-icons)
                                                {})
                               :photos (if-let [photos (:photos %)] (mapv (fn [p] (json/parse-string p keyword)) photos) [])
                               :documents (if-let [docs (:documents %)] (mapv (fn [d] (json/parse-string d keyword)) docs) [])
                               :devices (if-let [devices (:devices %)]
                                          (property-devices (:id %) session))
                               :profiles (profiles/->clojure (:id %) session)
                               :href (format entity-resource (:id %)))))
          scoll   (sort-by :property_code coll)]
      ;; (log/debugf "Properties: %s" scoll)
      scoll)))

(defresource index [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial index-handle-ok store))
