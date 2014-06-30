(ns kixi.hecuba.data.sensors
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn sensor-time-range [device_id type session]
  (first
   (db/execute session
               (hayt/select :sensor_metadata
                            (hayt/columns :lower_ts :upper_ts)
                            (hayt/where [[= :device_id device_id]
                                         [= :type type]])))))

(defn add-metadata [sensor session]
  (let [{:keys [device_id type]}    sensor
        {:keys [lower_ts upper_ts]} (sensor-time-range device_id type session)]
    (-> sensor
        (assoc :lower_ts lower_ts)
        (assoc :upper_ts upper_ts))))

(defn enrich-sensor [session sensor]
  (-> sensor
      (dissoc :user_id)
      (add-metadata session)))

(defn get
  "Returns a single sensor matching m or nil if not found."
  [session m]
  (->> (db/execute session
                     (hayt/select :sensors
                                  (hayt/where [[= :device_id (:device_id m)]
                                               [= :type (:type m)]])))
       (mapv (partial enrich-sensor session))
       first))

(defn get-all
  ([session]
     (db/execute session
                 (hayt/select :sensors)))
  ([session device_id]
     (->>  (db/execute session
                       (hayt/select :sensors
                                    (hayt/where [[= :device_id device_id]])))
           (mapv (partial enrich-sensor session)))))

(defn sensor-metadata [session m]
  (first (db/execute session
                     (hayt/select
                      :sensor_metadata
                      (hayt/where [[= :device_id (:device_id m)]
                                   [= :type (:type m)]])))))

(defn sensor-and-metadata [session m]
  (some-> session
   (get m)
   (merge (sensor-metadata session m))))
