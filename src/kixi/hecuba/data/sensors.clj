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

(defn enrich-sensor [sensor session]
  (-> sensor
      (dissoc :user_id)
      (add-metadata session)))

(defn get-sensors [device_id session]
  (db/execute session
              (hayt/select :sensors
                           (hayt/where [[= :device_id device_id]]))))

(defn ->clojure
  "Sensors are called readings in the API."
  [device_id session]
  (let [sensors (get-sensors device_id session)]
    (mapv #(enrich-sensor % session) sensors)))

(defn sensor-exists? [session m]
  (first (db/execute session
                     (hayt/select :sensors
                                  (hayt/where [[= :device_id (:device_id m)]
                                               [= :type (:type m)]])))))

(defn sensor-metadata [session m]
  (first (db/execute session
                     (hayt/select
                      :sensor_metadata
                      (hayt/where [[= :device_id (:device_id m)]
                                   [= :type (:type m)]])))))

(defn sensor-and-metadata [session m]
  (some-> session
   (sensor-exists? m)
   (merge (sensor-metadata session m))))
