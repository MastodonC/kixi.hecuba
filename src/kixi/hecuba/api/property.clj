(ns kixi.hecuba.api.property
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   ))

(defresource properties [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn" "text/html"}
  :authorized? (authorized? querier :property)

  :handle-ok
  (fn [{{mime :media-type} :representation {routes :modular.bidi/routes route-params :route-params} :request :as req}]
      (let [coll (->> (if (:project-id route-params)
                      (hecuba/items querier :entity route-params)
                      (hecuba/items querier :entity))
                      (map #(-> %
                                (assoc :device-ids (map :id (hecuba/items querier :device {:entity-id (:id %)})))
                                (assoc :href (bidi/path-for routes (:entity @handlers) :entity-id (:id %))))))
            scoll (sort-by :address-street-two coll)]

        (case mime
          "text/html" (util/render-items req scoll)
          "application/json" (->> scoll
                                  (map util/downcast-to-json)
                                  (map util/camelify)
                                  json/encode)
          scoll))))
