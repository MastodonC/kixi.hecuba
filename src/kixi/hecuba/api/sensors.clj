(ns kixi.hecuba.api.sensors
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.data.sensors :as sensors]))

(defn- entity_id-from [ctx]
  (get-in ctx [:request :route-params :entity_id]))

(defn metadata-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [device_id  (-> ctx :request :route-params :device_id)]
      (when-let [items (sensors/sensor-metadata session device_id)]
        {::items items}))))

(defn metadata-handle-ok [ctx]
  {::items ctx})

(defn index-by-property-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          devices (db/execute session (hayt/select :devices (hayt/where [[= :entity_id (entity_id-from ctx)]])))
          sensors (mapcat (fn [{:keys [id location]}]
                            (map #(assoc % :location (json/decode location))
                                 (sensors/get-all session id)))
                          devices)]
      (util/render-items request sensors))))

(defresource metadata [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :exists? (partial metadata-exists? store)
  :handle-ok (partial metadata-handle-ok))

(defresource index-by-property [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial index-by-property-handle-ok store))
