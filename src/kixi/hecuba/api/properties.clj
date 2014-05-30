(ns kixi.hecuba.api.properties
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex routes-from ) :as util]
   [liberator.core :refer (defresource)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.dbnew :as db]))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-handle-ok [store handlers ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)]
      (let [coll (->> (db/execute session                                  
                                  (if (project_id-from ctx)
                                    (hayt/select :entities (hayt/where [[= :project_id (-> (:route-params request) :project_id)]]))
                                    (hayt/select :entities)))
                      (map #(-> %
                                (assoc :device_ids (map :id (db/execute session 
                                                                        (hayt/select :devices
                                                                                     (hayt/where [[= :entity_id (:id %)]]))))
                                       :href (bidi/path-for (routes-from ctx)
                                                            (:entity @handlers) :entity_id (:id %))))))
            scoll (sort-by :address_street_two coll)]

        (case (get-in ctx [:representation :media-type])
          "text/html" (util/render-items request scoll)
          "application/json" scoll
          scoll)))))

(defresource index [store handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn" "text/html"}
  :authorized? (authorized? store :property)
  :handle-ok (partial index-handle-ok store handlers))
