(ns kixi.hecuba.api.properties
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex routes-from ) :as util]
   [liberator.core :refer (defresource)]))

(defn- project-id-from [ctx]
  (get-in ctx [:request :route-params :project-id]))

(defn index-handle-ok [querier handlers ctx]
  (let [request (:request ctx)]
    (let [coll (->> (if (project-id-from ctx)
                      (hecuba/items querier :entity [[= :project-id (-> (:route-params request) :project-id)]])
                      (hecuba/items querier :entity))
                    (map #(-> %
                              (assoc :device-ids (map :id (hecuba/items querier :device [[= :entity-id (:id %)]]))
                                     :href (bidi/path-for (routes-from ctx)
                                                          (:entity @handlers) :entity-id (:id %))))))
          scoll (sort-by :address-street-two coll)]

      (case (get-in ctx [:representation :media-type])
        "text/html" (util/render-items request scoll)
        "application/json" (->> scoll
                                (map util/downcast-to-json)
                                (map util/camelify)
                                json/encode)
        scoll))))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn" "text/html"}
  :authorized? (authorized? querier :property)
  :handle-ok (partial index-handle-ok querier handlers))
