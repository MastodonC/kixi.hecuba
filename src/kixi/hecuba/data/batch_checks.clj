(ns kixi.hecuba.data.batch-checks
  "Batch validation jobs scheduled using quartz scheduler."
  (:require [clj-time.core :as t]
            [com.stuartsierra.frequencies :as freq]
            [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.data.paginate :as p]
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
  ""
  [commander querier sensor measurements]
  (let [errors (-> sensor :errors read-string)
        where  {:device-id (:device-id sensor) :type (:type sensor)}
        m      (filter #(m/numbers-as-strings? (:value %)) measurements)]
    (update! commander :sensor-metadata :mislabelled 
             (if (labelled-correctly? sensor (m/decassandraify-measurements m)) "false" "true") where)))

(defn mislabelled-sensors
  "Checks for mislabelled sensors in batches."
  [commander querier item]
  (let [sensors (m/sensors-to-check querier :mislabelled-sensors-check)]
    (doseq [s sensors]
      (let [device-id  (:device-id s)
            type       (:type s)
            last-check (:mislabelled-sensors-check s)
            where      (m/last-check-where-clause device-id type last-check)
            today      (m/last-check-int-format (t/now))]
        (if (p/paginate commander querier s :measurement where mislabelled-check)
          (update! commander :sensor-metadata :mislabelled_sensors_check today {:device-id (:device-id s) :type (:type s)}))))))

;;;;;;;;;;;; Batch check for spiked measurements ;;;;;;;;;

(defn label-spikes
  "Checks a sequence of measurement against the most recent recorded median. Overwrites the measurement with updated
  metadata."
  [commander querier sensor measurements]
  (let [median (-> sensor :median read-string)]
    (doseq [m measurements]
      (let [spike (str (v/larger-than-median median (m/parse-value m)))
            metadata (-> m :metadata)]
        (upsert! commander :measurement (m/cassandraify-measurement (assoc-in m [:metadata] 
                                                                              (m/update-metadata 
                                                                               metadata
                                                                               {:median-spike spike}))))))))

(defn median-spike-batch-check
  "Batch check of median spikes. It re-checks all measurements that have had median calculated
  and marks sensors as bootstrapped."
  [commander querier item]
  (let [sensor-metadata  (items querier :sensor-metadata)
        sensors          (filter #(and (not= "" (:median-calc-check %))
                                       (= "" (:bootstrapped %))) sensor-metadata)
        sensors-to-check (map #(merge (first (items querier :sensor {:device-id (:device-id %) :type (:type %)})) %) sensors)]
    (doseq [sensor sensors-to-check]
      (let [device-id  (:device-id sensor)
            type       (:type sensor)
            where      (m/last-check-where-clause device-id type "")]
        (when (p/paginate commander querier sensor :measurement where label-spikes)
          (update! commander :sensor-metadata :bootstrapped "true" {:device-id device-id :type type}))))))



;;;;;;;;;;;;; Batch median calculation ;;;;;;;;;;;;;;;;;

(defn remove-bad-readings
  "Filters out errored measurements."
  [m]
  (and (not (zero? (get-in m [:value])))
       (= "true" (get-in m [:metadata :is-number]))
       (not= "true" (get-in m [:metadata :median-spike]))))

(defn median
  "Find median of a lazy sequency of measurements.
  Filters out 0s and invalid measurements (non-numbers)."
  [measurements]
  (when (not (empty? measurements))
    (freq/median (frequencies (map :value measurements)))))

(defn update-median
  "Calculates and updates median for a given sensor."
  [commander querier sensor measurements]
  (let [errors   (-> sensor :errors read-string)
        period   (-> sensor :period)
        where    {:device-id (:device-id sensor) :type (:type sensor)}
        median   (cond
                  (= "CUMULATIVE" period) (median (filter #(number? (:value %)) (m/decassandraify-measurements measurements)))
                  (= "INSTANT" period) (median (filter #(remove-bad-readings %) (m/decassandraify-measurements measurements))))]
    (when (number? median)
      (update! commander :sensor :median median where))))

(defn median-calculation
  "Retrieves all sensors that either have not had median calculated or the calculation took place over a week ago.
  It iterates over the list of sensors, returns measurements for each and performs calculation.
  Measurements are retrieved in batches."
  [commander querier item]
  (let [period  (-> item :period)
        sensors (filter #(= period (:period %)) (m/sensors-to-check querier :median-calc-check))
        table   (cond
                 (= "CUMULATIVE" period) :difference-series
                 (= "INSTANT" period) :measurement)]
    (doseq [sensor sensors]
      (let [device-id  (:device-id sensor)
            type       (:type sensor)
            last-check (:median-calc-check sensor)
            where      (m/last-check-where-clause device-id type last-check)
            today      (m/last-check-int-format (t/now))]
        (when (p/paginate commander querier sensor table where update-median)
          (update! commander :sensor-metadata :median_calc_check today {:device-id device-id :type type}))))))
