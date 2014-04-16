(ns kixi.hecuba.api.sensors
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defresource metadata [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"text/html" "application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :device)

  :exists? (fn [{{{:keys [entity-id device-id]} :route-params} :request}]
             (when-let [items (hecuba/items querier :sensor-metadata {:device-id device-id})]
               {::items items}))

  :handle-ok
  (fn [{items ::items {mime :media-type} :representation {routes :modular.bidi/routes route-params :route-params} :request :as request}]
    items
         ;downcast-to-json
         ;camelify
         ;encode
         ))

(defresource index-by-property [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)
  :handle-ok (fn [{{{:keys [entity-id]} :route-params} :request
                  {mime :media-type} :representation :as req}]
               (let [devices (hecuba/items querier :device {:entity-id entity-id})
                     sensors (mapcat (fn [{:keys [id location]}]
                                       (map #(assoc % :location (json/decode location)) (hecuba/items querier :sensor {:device-id id}))) devices)]
                 (util/render-items req sensors))))
