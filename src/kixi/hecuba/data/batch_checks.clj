(ns kixi.hecuba.data.batch-checks
  "Batch validation jobs scheduled using quartz scheduler."
  (:require [clj-time.core :as t]
            [com.stuartsierra.frequencies :as freq]
            [kixi.hecuba.storage.db :as db]
            [qbits.hayt :as hayt]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.data.validate :as v]))

;;; Check for mislabelled sensors ;;;

(defn going-up?
  "Check if all measurements are going up. Assumes the measurements
  are sorted by their timestamp."
  [measurements]
  (apply <= (map :value measurements)))

(defn neg-or-not-int?
  "Check for negative numbers or non integers."
 [measurement]
  (or (not (integer? (:value measurement))) (neg? (:value measurement))))

(defmulti labelled-correctly?
  "Dispatches a call to a specific device period, where
   appropriate checks are performed."
  (fn [sensor measurements] (:period sensor)))

(defmethod labelled-correctly? "INSTANT" [sensor measurements] true)
(defmethod labelled-correctly? "CUMULATIVE" [sensor measurements] (going-up? (m/sort-measurments measurements)))
(defmethod labelled-correctly? "PULSE" [sensor measurements] (empty? (filter neg-or-not-int? measurements)))

(defn mislabelled-check
  "Takes an hour worth of measurements and checks if the sensor is labelled correctly according to
  rules in labelled-correctly?"
  [store {:keys [device_id type period] :as sensor} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/hours 1))
          month        (m/get-month-partition-key start-date)
          where        [[= :device_id device_id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (filter #(number? (:value %)) (m/parse-measurements (db/execute session
                                                                                       (hayt/select :measurements
                                                                                                    (hayt/where where)))))]
      (when-not (empty? measurements)
        (db/execute session
                    (hayt/update :sensor_metadata
                                 (hayt/set-columns {:mislabelled (if (labelled-correctly? sensor measurements)
                                                                   "false"
                                                                   "true")})
                                 (hayt/where [[= :device_id device_id]
                                              [= :type type]]))))
      end-date)))

(defn mislabelled-sensors
  "Checks for mislabelled sensors."
  [store {:keys [sensor range]}]
  (let [{:keys [start-date end-date]} range]
    (loop [start start-date]
      (when-not (t/before? end-date start)
        (recur (mislabelled-check store sensor start))))))

;;;;;;;;;;;; Batch check for spiked measurements ;;;;;;;;;

(defn label-spikes
  "Checks a sequence of measurement against the most recent recorded median. Overwrites the measurement with updated
  metadata."
  [store {:keys [device_id type median] :as sensor} start-date]
  (when-not (nil? median)
    (db/with-session [session (:hecuba-session store)]
      (let [end-date     (t/plus start-date (t/hours 1))
            month        (m/get-month-partition-key start-date)
            where        [[= :device_id device_id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
            measurements (filter #(m/metadata-is-number? %) (db/execute session (hayt/select :measurements (hayt/where where))))]
        (doseq [m measurements]
          (let [spike    (str (v/larger-than-median median m))
                where    [[= :device_id device_id]
                          [= :type type]
                          [= :month month]
                          [= :timestamp (:timestamp m)]]]
            (db/execute session
                        (hayt/update :measurements (hayt/set-columns {:metadata [+ {"median-spike" spike}]})
                                     (hayt/where where)))))
        end-date))))

(defn median-spike-check
  "Check of median spikes. It re-checks all measurements that have had median calculated."
  [store {:keys [sensor range]}]
  (let [{:keys [start-date end-date]} range]
    (loop [start start-date]
      (when-not (t/before? end-date start)
        (recur (label-spikes store sensor start))))))

;;;;;;;;;;;;; Batch median calculation ;;;;;;;;;;;;;;;;;

(defn remove-bad-readings
  "Filters out errored measurements."
  [m]
  (and (= "true" (get-in m [:metadata "is-number"]))
       (not (zero? (get-in m [:value])))
       (not= "true" (get-in m [:metadata "median-spike"]))))

(defn median
  "Find median of a lazy sequency of measurements.
  Filters out 0s and invalid measurements (non-numbers)."
  [measurements]
  (when (not (empty? measurements))
    (freq/median (frequencies (map :value measurements)))))

(defn update-median
  "Calculates and updates median for a given sensor."
  [store table {:keys [device_id type period]} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/hours 1))
          month        (m/get-month-partition-key start-date)
          where        [[= :device_id device_id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (m/parse-measurements (db/execute session (hayt/select table (hayt/where where))))
          median       (cond
                        (= "CUMULATIVE" period) (median (filter #(number? (:value %)) measurements))
                        (= "INSTANT" period) (median (filter #(remove-bad-readings %) measurements)))]
      (when (number? median)
        (db/execute session
                    (hayt/update :sensors
                                 (hayt/set-columns {:median median})
                                 (hayt/where [[= :device_id device_id] [= :type type]]))))
      end-date)))

(defn median-calculation
  "Retrieves all sensors that either have not had median calculated or the calculation took place over a week ago.
  It iterates over the list of sensors, returns measurements for each and performs calculation.
  Measurements are retrieved in batches."
  [store table {:keys [sensor range]}]
  (let [period (-> sensor :period)
        {:keys [start-date end-date]} range]
    (loop [start start-date]
      (when-not (t/before? end-date start)
        (recur (update-median store table sensor start))))))
