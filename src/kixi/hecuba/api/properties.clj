(ns kixi.hecuba.api.properties
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex routes-from ) :as util]
   [liberator.core :refer (defresource)]))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-handle-ok [querier handlers ctx]
  (let [request (:request ctx)]
    (let [coll (->> (if (project_id-from ctx)
                      (hecuba/items querier :entity [[= :project_id (-> (:route-params request) :project_id)]])
                      (hecuba/items querier :entity))
                    (map #(-> %
                              (assoc :device_ids (map :id (hecuba/items querier :device [[= :entity_id (:id %)]]))
                                     :href (bidi/path-for (routes-from ctx)
                                                          (:entity @handlers) :entity_id (:id %))))))
          scoll (sort-by :address_street_two coll)]

      (case (get-in ctx [:representation :media-type])
        "text/html" (util/render-items request scoll)
        "application/json" scoll
        scoll))))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn" "text/html"}
  :authorized? (authorized? querier :property)
  :handle-ok (partial index-handle-ok querier handlers))
