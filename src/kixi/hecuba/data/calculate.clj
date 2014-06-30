(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.periodic :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.datasets :as datasets]
            [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.storage.db :as db]
            [qbits.hayt :as hayt]
            [kixi.hecuba.queue :as q]))

;; Helpers

(defn round [x]
  (with-precision 3 x))

(def conversion-factors {"vol2kwh" {"gasConsumption" {"m^3" 10.97222
                                                      "ft^3" (* 2.83 10.9722)}
                                    "oilConsumption" {"m^3" 10308.34
                                                      "ft^3" (* 2.83 10308.34)}}
                         "kwh2co2" {"electricityConsumption" {"kWh" 0.517}
                                    "gasConsumption" {"kWh" 0.185}
                                    "oilConsumption" {"kWh" 0.246}}})

(defn conversion-fn [{:keys [type unit]} operation]
  (let [typ   (first (str/split type #"_"))
        factor (get-in conversion-factors [operation typ unit])]
    (fn [m]
      (cond-> m (m/metadata-is-number? m)
              (assoc :value (str (round (* factor (edn/read-string (:value m)))))
                     :type (m/output-type-for type operation))))))

(defmulti quantize-timestamp (fn [m resolution] resolution))

(defmethod quantize-timestamp 60
  [m resolution]
  (let [t       (:timestamp m)
        rounded (m/truncate-seconds t)]
    (assoc-in m [:timestamp] rounded)))

(defmethod quantize-timestamp 300
  [m resolution]
  (let [t       (:timestamp m)
        round-fn (fn [t] (tc/to-date (tc/from-long (long (* (quot (+ (tc/to-long t) 150000) 300000) 300000)))))]
    (update-in m [:timestamp] round-fn)))

(defmethod quantize-timestamp 1800
  [m resolution]
  (let [t       (:timestamp m)
        round-fn (fn [t] (tc/to-date (tc/from-long (long (* (quot (+ (tc/to-long t) 300000) (* 6 300000)) (* 6 300000))))))]
    (update-in m [:timestamp] round-fn)))

(defn timestamp-seq-inclusive
  ([start end] (timestamp-seq-inclusive start end 60))
  ([start end resolution] (map (fn [t] (tc/to-date t)) (take-while #(not (t/after? % end)) (p/periodic-seq start (t/seconds resolution))))))

(defn all-timestamps-for-range
  "Takes a start date, end date and resolution (in seconds) and creates a sequence
  of timestamps (inclusive). Seconds are truncated. "
  [start end resolution]
  (let [start-date (m/truncate-seconds start)
        end-date   (m/truncate-seconds end)]
    (map #(hash-map :timestamp %) (timestamp-seq-inclusive start-date end-date resolution))))

(defn nearest-resolution [resolution]
  (if-not (some #{resolution} [60 300 1800])
    (cond (< resolution 150) 60
          (< resolution 900) 300
          :else 1800)
    resolution))

(defn pad-measurements
  "Takes a sequence of measurements, expected timestamps and resolution in seconds and pads it with template readings."
  [measurements expected-timestamps resolution]
  (let []
    (let [template-reading (fn [t] (hash-map :value "N/A" :month (m/get-month-partition-key (:timestamp t))))
          resolution       (nearest-resolution resolution)
          quantized        (map #(quantize-timestamp % resolution) measurements)
          grouped-readings (into {} (map #(vector (:timestamp %) %) quantized))]
      (map #(merge (template-reading %) (get grouped-readings (:timestamp %) %)) expected-timestamps))))

;;;;;;; Difference series ;;;;;;;;;

(defn- ext-type [type] (str type "_differenceSeries"))

(defn get-difference [m n]
  (let [value (if (every? m/metadata-is-number? [m n]) (str (round (- (edn/read-string (:value n)) (edn/read-string (:value m))))) "N/A")]
    {:timestamp (:timestamp n)
     :value value
     :reading_metadata {"is-number" (if (number? (edn/read-string value)) "true" "false") "median-spike" "n-a"}
     :device_id (:device_id m)
     :type (ext-type (:type m))
     :month (:month m)}))

(defn diff-seq [coll]
  (assert (not (empty? coll)) "No measurements passed to diff-seq")
  (map #(get-difference %1 %2) coll (rest coll)))



(defn timestamp-seq-inclusive
  ([start end] (timestamp-seq-inclusive start end 60))
  ([start end resolution] (map (fn [t] (tc/to-date t)) (take-while #(not (t/after? % end)) (p/periodic-seq start (t/seconds resolution))))))

(defn difference-series
  "Takes store, sensor and a range of dates and calculates difference series using resolution
  stored in the sensor data. If resolution is not specified, default 60 seconds is used.
  Measurements have gaps filled in."
  [store {:keys [sensor range]}]
  (let [{:keys [start-date end-date]} range
        measurements                  (measurements/measurements-for-range store sensor range (t/hours 1))]
    (when (> (count (take 2 measurements)) 1)
      (let [{:keys [device_id type]} sensor
            new-type                 (ext-type type)
            calculated               (diff-seq measurements)]
        (m/insert-measurements store {:device_id device_id :type new-type} calculated 100)))))

(defn kWh->co2
  "Converts measurements from kWh to co2."
  [store {:keys [sensor range]}]
  (let [{:keys [device_id type]} sensor
        new-type                (m/output-type-for type "kwh2co2")
        get-fn-and-measurements (fn [s] [(conversion-fn s "kwh2co2")
                                         (measurements/measurements-for-range store s range (t/hours 1))])
        convert                 (fn [[f xs]] (map f xs))
        calculated              (->> sensor
                                     get-fn-and-measurements
                                     convert)]
    (m/insert-measurements store {:device_id device_id :type new-type} calculated 100)))

(defn gas-volume->kWh
  "Converts measurements from m^3 and ft^3 to kWh."
  [store {:keys [sensor range]}]
  (let [{:keys [device_id type]} sensor
        get-fn-and-measurements  (fn [s] [(conversion-fn s "vol2kwh")
                                          (measurements/measurements-for-range store s range (t/hours 1))])
        convert                  (fn [[f xs]] (map f xs))
        new-type                 (m/output-type-for type "vol2kwh")
        calculated               (->> sensor
                                      get-fn-and-measurements
                                      convert)]
    (m/insert-measurements store {:device_id device_id :type new-type} calculated 100)))

;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn average-reading [measurements]
  (let [measurements-sum   (reduce + measurements)
        measurements-count (count measurements)]
    (if-not (zero? measurements-count)
      (/ measurements-sum measurements-count)
      "N/A")))

(defn daily-batch
  [store {:keys [device_id type period]} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/days 1))
          year         (m/get-year-partition-key start-date)
          where        [[= :device_id device_id] [= :type type] [= :year year] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (m/parse-measurements (db/execute session (hayt/select :hourly_rollups (hayt/where where))))
          filtered     (filter #(number? %) (map :value measurements))
          funct        (fn [measurements] (case period
                                            "CUMULATIVE" (:value (last measurements))
                                            "INSTANT"    (average-reading measurements)
                                            "PULSE"      (reduce + measurements)))]
      (when-not (empty? filtered)
        (db/execute session
                    (hayt/insert :daily_rollups (hayt/values {:value (str (round (funct filtered)))
                                                              :timestamp (tc/to-date start-date)
                                                              :device_id device_id
                                                              :type type}))))
      end-date)))

(defn daily-rollups
  "Calculates daily rollups for given sensor and date range."
  [store {:keys [sensor range]}]
  (let [{:keys [start-date end-date]} range]
    (loop [start  start-date]
      (when-not (t/before? end-date start)
        (recur (daily-batch store sensor start))))))

(defn hour-batch
  [store {:keys [device_id type period]} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/hours 1))
          month        (m/get-month-partition-key start-date)
          where        [[= :device_id device_id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (m/parse-measurements (db/execute session (hayt/select :partitioned_measurements (hayt/where where))))
          filtered     (filter #(number? %) (map :value measurements))
          funct        (fn [measurements] (case period
                                            "CUMULATIVE" (last measurements)
                                            "INSTANT"    (average-reading measurements)
                                            "PULSE"      (reduce + measurements)))]
      (when-not (empty? filtered)
        (let [invalid (/ (count filtered) (count measurements))]
          (when-not (and (not= invalid 1) (> invalid 0.10))
            (db/execute session
                        (hayt/insert :hourly_rollups (hayt/values
                                                      {:value (str (round (funct filtered)))
                                                       :timestamp (tc/to-date end-date)
                                                       :year (m/get-year-partition-key start-date)
                                                       :device_id device_id
                                                       :type type}))))))
      end-date)))

(defn hourly-rollups
  "Calculates hourly rollups for given sensor and date range.
  Example of item: {:sensor {:device_id \"f11a21b8e5e6b97eacba2632db4a2037a43f4791\" :type \"temperatureGround\"
                   :period \"CUMULATIVE\"} :range {:start-date \"Sat Mar 01 00:00:00 UTC 2014\"
                   :end-date \"Sun Mar 02 23:00:00 UTC 2014\"}}"
  [store {:keys [sensor range]}]
  (let [{:keys [start-date end-date]} range
        period (:period sensor)]
    (loop [start  (m/truncate-seconds start-date)]
      (when-not (t/before? end-date start)
        (recur (hour-batch store sensor start))))))

;;;;;;;;; Resolution calculation ;;;;;;;;;;

(defn- diff [coll]
  (map - coll (rest coll)))

(defn- mode [coll]
  (first (last (sort-by second (frequencies coll)))))

(defn find-resolution [measurements]
  (let [differences (diff (map #(/ (.getTime (m/truncate-seconds (:timestamp %))) 1000) measurements))]
    (java.lang.Math/abs (mode differences))))

(defn resolution
  "Updates resolution if it's missing. Infers it from last 100 measurements.
  Depending on the order of data in the database, it might not always return last 100 of measurements."
  [store item]
  (db/with-session [session (:hecuba-session store)]
    (let [sensors-to-update (filter #(empty? (:resolution %)) (db/execute session (hayt/select :sensors)))]
      (doseq [sensor sensors-to-update]
        (let [measurements (db/execute session (hayt/select :partitioned_measurements
                                                            (hayt/where [[= :device_id (:device_id sensor)]
                                                                         [= :type (:type sensor)]])
                                                            (hayt/order-by [:type :desc])
                                                            (hayt/limit 100)))
              resolution  (str (find-resolution measurements))]
          (db/execute session (hayt/update :sensors
                                           (hayt/set-columns [[:resolution resolution]])
                                           (hayt/where [[= :device_id (:device_id sensor)]
                                                        [= :type (:type sensor)]]))))))))
(defn update-resolution [store sensor resolution]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session (hayt/update :sensors (hayt/set-columns {:resolution (str resolution)})
                                     (hayt/where (m/where-from sensor))))))

(defn get-resolution [store sensor measurements]
  (let [existing-resolution (:resolution sensor)
        calculated-resolution (find-resolution measurements)]
    (log/debugf "Checking resolution for id: %s type: %s. Existing resolution is: %s. Calculated resolution is: %s"
                (:device_id sensor) (:type sensor) existing-resolution calculated-resolution)
    (if (or (nil? existing-resolution)
            (zero? (edn/read-string existing-resolution))
            (not= existing-resolution calculated-resolution))
      (do (update-resolution store sensor calculated-resolution)
          calculated-resolution)
      existing-resolution)))

;;;;;; Calculated datasets ;;;;;;;;;;;

(defn- sum
  "Adds all numeric values in a sequence of measurements. Some measurements might contain
  error messages instead of readings."
  [m]
  (apply + (map :value (filter #(number? (:value %)) m))))

(defmulti calculation (fn [operation & datasets] operation))

(defmethod calculation :sum [_ & datasets]
  (sum datasets))

(defmethod calculation :subtract [_ & datasets]
  (let [m (map :value datasets)]
    (if (every? number? m)
      (round (apply - m))
      "N/A")))

(defmethod calculation :divide [_ & datasets]
  (let [m (map :value datasets)]
    (if (and (every? number? m)
             (not (zero? (second m))))
      (round (apply / m))
      "N/A")))

(defmethod calculation :multiply [_ & datasets]
  (let [m (map :value datasets)]
    (if (every? number? m)
      (if (some #{0} m)
        0
        (round (apply * m)))
      "N/A")))

(defn compute-datasets [operation device_id type & datasets]
  (apply map (fn [& args]
               (let [value (apply calculation operation args)]
                 (hash-map :value (str value)
                           :device_id device_id
                           :type type
                           :reading_metadata {"is-number" (str (number? value)) "median-spike" "n/a"}
                           :timestamp (:timestamp (first args))
                           :month (:month (first args)))))
         datasets))

;; Padding ;;;

(defn even-all-collections
  "Takes a vector containg lists of measurements, a sequence of required timestamps and resolution
                in seconds and pads the measurements."
  [all-colls timestamps resolution]
  (map #(pad-measurements % timestamps resolution) all-colls))

(defn padded-measurements
  "Takes a sequence of a variable number of sequences of measurements
   and makes them of even length by padding."
  [sensors measurements-seq resolution]
  (let [[start end]  (m/range-for-all-sensors sensors)
        expected-ts  (all-timestamps-for-range start end resolution)]
     (even-all-collections measurements-seq expected-ts resolution)))

;;; Decisions around sensors ;;;

(defmulti should-calculate? (fn [ds sensors] (keyword (:operation ds))))

(defmethod should-calculate? :sum [ds sensors]
  (let [{:keys [period unit]} (first sensors)]
    (every? #(and (= period (:period %))
                  (= unit   (:unit %))) sensors)))

(defmethod should-calculate? :divide [ds sensors]
  (let [{:keys [period unit]} (first sensors)]
    (every? #(and (= period (:period %))
                  (= unit (:unit %))) sensors)))

(defmethod should-calculate? :subtract [ds sensors]
  (let [{:keys [period]} (first sensors)]
    (every? #(= period (:period %)) sensors)))

(defn calculate-dataset [store ds sensors range]

  (let [{:keys [operation members device_id]} ds
        operation (keyword operation)]

    (log/info "Calculating datasets for sensors: " members "and operation: " operation "and range: " range)

    (if (and (> (count sensors) 1)
             (should-calculate? ds sensors))

      (let [measurements (into [] (map #(m/parse-measurements
                                         (measurements/measurements-for-range store % range (t/hours 1)))
                                       sensors))]

        (if (every? #(> (count (take 2 %)) 1) measurements)
          (let [all-resolutions (map #(get-resolution store %1 (take 100 %2)) sensors measurements)
                resolution      (first all-resolutions)]

            (if (every? #(= resolution %) all-resolutions)
              (let [padded     (padded-measurements sensors measurements resolution)
                    new-type   (:name ds)
                    sensor     {:device_id device_id :type new-type}
                    calculated (apply compute-datasets operation device_id new-type padded)
                    {:keys [start-date end-date]} range]

                (m/insert-measurements store sensor calculated 100)
                (m/reset-date-range store sensor :calculated_datasets start-date end-date)
                (log/info "Finished calculation for sensors: " (:members ds) "and operation: " operation))
              (log/info "Sensors are not of the same resolution.")))
          (log/info "Sensors do not have enough measurements to calculate.")))
      (log/info "Sensors do not meet requirements for calculation."))))
