(ns kixi.hecuba.data.sensors
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.webutil :refer (stringify-values)]))

(defn encode [sensor]
  (-> sensor
      (merge (stringify-values (dissoc sensor :synthetic :actual_annual)))))

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

(defn insert [session sensor]
  (db/execute session (hayt/insert :sensors
                                   (hayt/values (encode sensor))))
  (db/execute session (hayt/insert :sensor_metadata
                                   (hayt/values {:device_id (:device_id sensor) :type (:type sensor)}))))

(defn update [session device_id sensor]
  (db/execute session (hayt/update :sensors
                                   (hayt/set-columns (dissoc (encode sensor) :type :device_id))
                                   (hayt/where [[= :device_id device_id] 
                                                [= :type (:type sensor)]]))))

(defn ->clojure
  "Sensors are called readings in the API."
  [device_id session]
  (let [sensors (get-sensors device_id session)]
    (mapv #(enrich-sensor % session) sensors)))
