(ns kixi.hecuba.api.sensors
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.dbnew :as db]))

(defn- entity_id-from [ctx]
  (get-in ctx [:request :route-params :entity_id]))

(defn- device_id-from [ctx]
  (get-in ctx [:request :route-params :device_id]))

(defn metadata-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (when-let [items (db/execute session (hayt/select :sensor_metadata (hayt/where [[= :device_id (device_id-from ctx)]])))]
      {::items items})))

(defn metadata-handle-ok [ctx]
  {::items ctx})

(defn index-by-property-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          devices (db/execute session (hayt/select :devices (hayt/where [[= :entity_id (entity_id-from ctx)]])))
          sensors (mapcat (fn [{:keys [id location]}]
                            (map #(assoc % :location (json/decode location))
                                 (db/execute session (hayt/select :sensors (hayt/where [[= :device_id id]])))))
                          devices)]
      (util/render-items request sensors))))

(defresource metadata [store handlers]
  :allowed-methods #{:get}
  :available-media-types #{"text/html" "application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store :device)
  :exists? (partial metadata-exists? store)
  :handle-ok (partial metadata-handle-ok))

(defresource index-by-property [store handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store :measurement)
  :handle-ok (partial index-by-property-handle-ok store))
