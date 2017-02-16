(ns kixi.hecuba.api.rollups
  (:require
   [clojure.string :as string]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.api :as api :refer (decode-body authorized? stringify-values)]
   [kixi.hecuba.time :as time]
   [kixi.hecuba.data.measurements :as measurements]
   [kixi.hecuba.data.sensors :as sensors]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [clojure.core.match :refer (match)]))

(defn get-measurements-keys [sensor-keys session]
  (let [sensor (sensors/get-by-type sensor-keys session)]
    (if-let [alias-sensor (:alias-sensor sensor)]
      alias-sensor
      {:sensor_id (:sensor_id sensor) :device_id (:device_id sensor)})))

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

(defn rollups-allowed? [store]
  (fn [ctx]
    (let [request (:request ctx)
          {:keys [request-method session params]} request
          {:keys [projects programmes role]} (sec/current-authentication session)
          {:keys [programme_id project_id]} params]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {:request request}]
        true))))

(defn retrieve-hourly-measurements
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [session start-date end-date device_id reading_type]
  (let [range     (time/time-range start-date end-date (t/years 1))
        {:keys [device_id sensor_id]} (get-measurements-keys {:device_id device_id :type reading_type} session)
        months    (map #(time/get-year-partition-key (tc/to-date %)) range)
        where     [[= :device_id device_id]
                   [= :sensor_id sensor_id]
                   [>= :timestamp (tc/to-date start-date)]
                   [<= :timestamp (tc/to-date end-date)]]]
    (mapcat (fn [month] (db/execute session (hayt/select :hourly_rollups (hayt/where (conj where [= :year month]))))) months)))

(defresource annual_rollups [store]
  )

(defresource hourly_rollups [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json"  "application/edn"}
  :authorized? (authorized? store)
  :allowed? (rollups-allowed? store)
  :handle-ok (fn [ctx]
               (db/with-session [session (:hecuba-session store)]
                 (let [request        (:request ctx)
                       {:keys [route-params
                               query-string]} request
                       {:keys [device_id
                               type]} route-params
                       decoded-params (api/decode-query-params query-string)
                       start-date     (time/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
                       end-date       (time/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
                       measurements   (retrieve-hourly-measurements session start-date end-date device_id type)]
                   {:measurements (->> measurements
                                       (map (fn [m]
                                              (-> m
                                                  measurements/parse-value
                                                  (update-in [:timestamp] time/db-to-iso)
                                                  (dissoc :year :metadata :device_id)))))}))))

(defresource daily_rollups [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)

  :handle-ok (fn [{{{:keys [device_id type]} :route-params query-string :query-string}
                   :request {mime :media-type} :representation :as req}]
               (db/with-session [session (:hecuba-session store)]
                 (let [{:keys [device_id sensor_id]} (get-measurements-keys {:device_id device_id :type type} session)
                       decoded-params (api/decode-query-params query-string)
                       start-date     (time/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
                       end-date       (time/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
                       measurements   (db/execute session (hayt/select :daily_rollups
                                                                       (hayt/where [[= :device_id device_id]
                                                                                    [= :sensor_id sensor_id]
                                                                                    [>= :timestamp (tc/to-date start-date)]
                                                                                    [<= :timestamp (tc/to-date end-date)]])))]
                   {:measurements (->> measurements
                                       (map (fn [m]
                                              (-> m
                                                  measurements/parse-value
                                                  (update-in [:timestamp] time/db-to-iso)
                                                  (dissoc :metadata :device_id)))))}))))
