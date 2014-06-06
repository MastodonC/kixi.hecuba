(ns kixi.hecuba.api.properties
  (:require
   [cheshire.core :as json]
   [clojure.edn :as edn]
   [clojure.tools.logging :as log]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex routes-from ) :as util]
   [liberator.core :refer (defresource)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.web-paths :as p]))

(def entity-resource (p/resource-path-string :entity-resource))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)]
      (let [coll (->> (db/execute session                                  
                                  (if (project_id-from ctx)
                                    (hayt/select :entities (hayt/where [[= :project_id (-> (:route-params request) :project_id)]]))
                                    (hayt/select :entities)))
                      (map #(assoc %
                              :property_data (if-let [property_data (:property_data %)]
                                               (json/parse-string property_data)
                                               {})
                              :photos (if-let [photos (:photos %)] (map (fn [p] (json/parse-string p keyword)) photos) [])
                              ;; TODO: parse documents key
                              ;; FIXME: This should work, as all stringified maps should be json
                              ;; :devices (if-let [devices (:devices %)]
                              ;;            (into {} (map (fn [[k v]] [(keyword k) (json/parse-string v)]) (:devices %))) {})
                              :href (format entity-resource (:id %)))))
            scoll (sort-by :property_code coll)]
        ;; (log/debugf "Properties: %s" scoll)
        scoll))))

(defresource index [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial index-handle-ok store))
