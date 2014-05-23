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

(defn- entity_id-from [ctx]
  (get-in ctx [:request :route-params :entity_id]))

(defn- device_id-from [ctx]
  (get-in ctx [:request :route-params :device_id]))

(defn metadata-exists? [querier ctx]
  (when-let [items (hecuba/items querier :sensor_metadata [[= :device_id (device_id-from ctx)]])]
    {::items items}))

(defn metadata-handle-ok [ctx]
    {::items ctx})

(defn index-by-property-handle-ok [querier ctx]
  (let [request (:request ctx)
        devices (hecuba/items querier
                              :device
                              [[= :entity_id (entity_id-from ctx)]])
        sensors (mapcat (fn [{:keys [id location]}]
                          (map #(assoc % :location (json/decode location))
                               (hecuba/items querier
                                             :sensor [[= :device_id id]])))
                        devices)]
    (util/render-items request sensors)))

(defresource metadata [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"text/html" "application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :device)
  :exists? (partial metadata-exists? querier)
  :handle-ok (partial metadata-handle-ok))

(defresource index-by-property [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)
  :handle-ok (partial index-by-property-handle-ok querier))
