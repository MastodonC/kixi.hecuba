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
            [kixi.hecuba.api.measurements :as measurements]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.storage.db :as db]
            [qbits.hayt :as hayt]
            [kixi.hecuba.queue :as q]))

;; Helpers

(defn round [x]
  (with-precision 3 x))

(defn sensors-for-dataset
  "Returns all the sensors for the given dataset."
  [{:keys [members]} store]
  (db/with-session [session (:hecuba-session store)]
    (let [parsed-sensors (map (fn [s] (into [] (next (re-matches #"(\w+)-(\w+)" s)))) members)
          sensor (fn [[type device_id]]
                   (db/execute session
                               (hayt/select :sensors
                                            (hayt/where [[= :type type]
                                                         [= :device_id device_id]]))))]
      (mapcat sensor parsed-sensors))))

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

(defn measurements-for-range
  "Returns a lazy sequence of measurements for a sensor matching type and device_id for a specified
  datetime range."
  [store sensor {:keys [start-date end-date]} page]
  (let [device_id  (:device_id sensor)
        type       (:type sensor)
        next-start (t/plus start-date page)]
    (db/with-session [session (:hecuba-session store)]
      (lazy-cat (db/execute session
                               (hayt/select :partitioned_measurements
                                            (hayt/where [[= :device_id device_id]
                                                         [= :type type]
                                                         [= :month (m/get-month-partition-key start-date)]
                                                         [>= :timestamp start-date]
                                                         [< :timestamp next-start]]))
                               nil)
                (when (t/before? next-start end-date)
                  (measurements-for-range store sensor {:start-date next-start :end-date end-date} page))))))

(defmulti quantize-timestamp (fn [m resolution] resolution) :default 60)

(defmethod quantize-timestamp 60
  [m resolution]
  (let [t       (:timestamp m)
        rounded (m/truncate-seconds t)]
    (assoc-in m [:timestamp] rounded)))

(defn timestamp-seq-inclusive
  ([start end] (timestamp-seq-inclusive start end 60))
  ([start end resolution] (map (fn [t] (tc/to-date t)) (take-while #(not (t/after? % end)) (p/periodic-seq start (t/seconds resolution))))))

(defn grouped-readings [measurements]
  (assert (not (empty? measurements)) "No measurements passed to grouped-readings")
  (into {} (map #(vector (:timestamp %) %) measurements)))

(defn quantized-measurements [resolution measurements]
  (assert (not (empty? measurements)) "No measurements passed to quantized-measurements")
  (map #(quantize-timestamp % resolution) measurements))

(defn filled-measurements [template-reading grouped-readings expected-timestamps]
  (assert template-reading "No expecte-timestamps passed to filled-measurements")
  (assert (not (empty? grouped-readings)) "No grouped-readings passed to filled-measurements")
  (assert (not (empty? expected-timestamps)) "No expecte-timestamps passed to filled-measurements")
  (map #(merge template-reading (get grouped-readings (:timestamp %) %)) expected-timestamps))

(defn expected-timestamps [start-date end-date resolution]
  (assert (and start-date end-date) "No start and end dates passed to expected-timestamps")
  (assert resolution "No resolution passed to expected-tiemstamps")
  (map #(hash-map :timestamp %) (timestamp-seq-inclusive start-date end-date resolution)))

(defn difference-series
  "Takes store, sensor and a range of dates and calculates difference series using resolution
  stored in the sensor data. If resolution is not specified, default 60 seconds is used."
  [store {:keys [sensor range]}]
  (log/debugf "Sensor: %s Range: %s" sensor range)
  (let [resolution                  (if-let [r (:resolution sensor)] (edn/read-string r) 60)
        {:keys [start-date end-date]} range
        timestamps                  (expected-timestamps start-date end-date resolution)
        measurements                (measurements-for-range store sensor range (t/hours 1))
        template-reading            (-> (first measurements)
                                        (assoc :value "n/a")
                                        (dissoc :timestamp :reading_metadata))]
    (log/debugf "Retrieved Measurements: %s" (vec (take 10 measurements)))
    (when (> (count (take 2 measurements)) 1)
      (let [{:keys [device_id type]} sensor
            quantized            (quantized-measurements resolution measurements)
            grouped-measurements (grouped-readings quantized)
            new-type             (ext-type type)
            padded-measurements  (filled-measurements template-reading grouped-measurements timestamps)
            calculated           (diff-seq padded-measurements)]
        (m/insert-measurements store {:device_id device_id :type new-type} calculated 100)))))

(defn kWh->co2 
  "Converts measurements from kWh to co2."
  [store {:keys [sensor range]}]
  (let [{:keys [device_id type]} sensor
        new-type                (m/output-type-for type "kwh2co2")
        get-fn-and-measurements (fn [s] [(conversion-fn s "kwh2co2") (measurements-for-range store s range (t/hours 1))])
        convert                 (fn [[f xs]] (map f xs))
        calculated              (->> sensor
                                     get-fn-and-measurements
                                     convert)]
    (m/insert-measurements store {:device_id device_id :type new-type} calculated 100)))

(defn gas-volume->kWh 
  "Converts measurements from m^3 and ft^3 to kWh."
  [store {:keys [sensor range]}]
  (let [{:keys [device_id type]} sensor
        get-fn-and-measurements  (fn [s] [(conversion-fn s "vol2kwh") (measurements-for-range store s range (t/hours 1))])
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

;;;;;; Calculated datasets ;;;;;;;;;;;

(defmulti calculate-data-set (comp keyword :operation))

;;;;; Total kwh ;;;;;

(defn- sum 
  "Adds all numeric values in a sequence of measurements. Some measurements might contain
  error messages instead of readings."
  [m]
  (apply + (map :value (filter #(number? (:value %)) m))))

(defn- range-for-padding 
  "Takes a sequence of sequences of measurements and returns min and max dates."
  [measurements]
  (let [all-starts (map #(tc/from-date (:timestamp (first %))) measurements)
        all-ends   (map #(tc/from-date (:timestamp (last %))) measurements)
        min-date   (fn [coll] (reduce (fn [t1 t2] (if (t/before? t1 t2) t1 t2)) coll))
        max-date   (fn [coll] (reduce (fn [t1 t2] (if (t/after? t1 t2) t1 t2)) coll))]
    [(min-date all-starts) (max-date all-ends)]))

(defn- pad-measurements
  "Takes a sequence of measurements, expected timestamps and resolution in seconds and pads it with template readings."
  [measurements expected-timestamps resolution]
  (let []
    (let [template-reading (fn [t] (hash-map :value "N/A" :month (m/get-month-partition-key (:timestamp t))))
          quantized        (map #(quantize-timestamp % resolution) measurements)
          grouped-readings (into {} (map #(vector (:timestamp %) %) quantized))
          padded (map #(merge (template-reading %) (get grouped-readings (:timestamp %) %)) expected-timestamps)]
      padded)))

(defn- all-timestamps-for-range 
  "Takes a start date, end date and resolution (in seconds) and creates a sequence
  of timestamps (inclusive). Seconds are truncated. "
  [start end resolution]
  (let [start-date (m/truncate-seconds start)
        end-date   (m/truncate-seconds end)]
    (map #(hash-map :timestamp %) (timestamp-seq-inclusive start-date end-date resolution))))

(defn- even-all-collections
  "Takes a vector containg lists of measurements, a sequence of required timestamps and resolution
                in seconds and pads the measurements."
  [all-colls timestamps resolution]
  (map #(pad-measurements % timestamps resolution) all-colls))

(defmethod calculate-data-set :total-kwh [ds store]
  (let [sensors    (sensors-for-dataset ds store)
        {:keys [resolution period unit]} (first sensors)
        {:keys [device_id operation]} ds]
    (when (every? #(and (= period (:period %))
                        (= unit (:unit %))
                        (= resolution (:resolution %))) sensors)
      (db/with-session [session (:hecuba-session store)]
        (let [measurements        (into [] (map #(m/parse-measurements (measurements/all-measurements store %)) sensors))
              [start end]         (range-for-padding measurements)
              resolution          (if resolution (edn/read-string resolution) 60)
              new-type            (m/output-type-for nil operation)
              expected-timestamps (all-timestamps-for-range start end resolution)
              padded              (even-all-collections measurements expected-timestamps resolution)
              calculated          (apply map (fn [& args] (hash-map :value (str (round (sum args)))
                                                                    :device_id device_id
                                                                    :reading_metadata {"is-number" "true" "median-spike" "n-a"}
                                                                    :timestamp (:timestamp (first args))
                                                                    :month (:month (first args))
                                                                    :type new-type)) padded)]
          (m/insert-measurements store {:device_id device_id :type new-type} calculated 100))))))

(defn divide-datasets 
  "Divides one dataset by another. "
  [d1 d2]
  (if (and (every? #(number? %) [d1 d2])
           (not (zero? d2)))
    (round (/ d1 d2))
    "N/A"))

(defn add-keywords [m-seq]
  (into {} (map (fn [m] {(:type (first m)) m}) m-seq)))

(defn filter-type [type-regex measurements]
  (flatten (vals (into {} (filter (fn [[k v]] (re-matches type-regex k)) measurements)))))

(defmethod calculate-data-set :system-efficiency-overall [ds store]
  (log/info "System efficiency overall.")
  (let [sensors    (sensors-for-dataset ds store)
        topic      (get-in (:queue store) [:queue "measurements"])
        {:keys [resolution period unit]} (first sensors)
        {:keys [device_id operation]} ds]
    
    (when (every? #(and 
                    (some (fn [typ] (= (:type %) typ)) ["interpolatedHeatConsumption" "interpolatedElectricityConsumption"])
                    (= resolution (:resolution %))
                    (= period (:period %))) sensors)
      (let [sensors      (if (= "CUMULATIVE" period)
                           (map #(update-in % [:type] (fn [type] (str type "_differenceSeries"))) sensors)
                           sensors)
            measurements (into [] (map #(m/parse-measurements (measurements/all-measurements store %)) sensors))
            [start end]  (range-for-padding measurements)
            resolution   (if resolution (edn/read-string resolution) 60)
            expected-timestamps (all-timestamps-for-range start end resolution)
            padded       (add-keywords (even-all-collections measurements expected-timestamps resolution))
            new-type     (m/output-type-for nil operation)
            calculated   (map (fn [d1 d2]
                                (let [value (divide-datasets (:value d1) (:value d2))]
                                  (hash-map :value (str value)
                                            :device_id device_id
                                            :type new-type
                                            :reading_metadata {"is-number" (str (number? value)) "median-spike" "n/a"}
                                            :timestamp (:timestamp d1)
                                            :month (:month d1))))
                              (filter-type #"interpolatedHeatConsumption.*" padded)
                              (filter-type #"interpolatedElectricityConsumption.*" padded))]
        (m/insert-measurements store {:device_id device_id :type new-type} calculated 100)))))

(defn generate-synthetic-readings [store item]
  (let [data-sets (datasets/all-datasets store)]
    ;; TODO This recalculates existing datasets as well - should we only recalcualate when new measurements are inserted?
    (doseq [ds data-sets]
      (log/info "Calculating dataset for: " ds)
      (calculate-data-set ds store))))
