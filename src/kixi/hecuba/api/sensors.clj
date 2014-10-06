(ns kixi.hecuba.api.sensors
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [clojure.core.match :refer (match)]))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects role request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects role request-method)
  (match  [(has-admin? role)
           (has-programme-manager? programme-id allowed-programmes)
           (has-project-manager? project-id allowed-projects)
           (has-user? programme-id allowed-programmes project-id allowed-projects)
           request-method]

          [true _ _ _ _]    true
          [_ true _ _ _]    true
          [_ _ true _ _]    true
          [_ _ _ true :get] true
          :else false))

(defn index-allowed? [store]
  (fn [ctx]
    (let [request (:request ctx)
          {:keys [request-method session params]} request
          {:keys [projects programmes role]} (sec/current-authentication session)
          {:keys [programme_id project_id]} params]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {:request request}]
        true))))

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

(defresource metadata [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :exists? (partial metadata-exists? store)
  :handle-ok (partial metadata-handle-ok))

(defresource index-by-property [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :handle-ok (partial index-by-property-handle-ok store))
