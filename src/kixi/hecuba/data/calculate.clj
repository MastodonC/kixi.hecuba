(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.periodic :as p]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.datasets :as datasets]
            [kixi.hecuba.api.measurements :as measurements]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.storage.db :as db]
            [qbits.hayt :as hayt]
            [kixi.hecuba.queue :as q]))

;; Helpers

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


(defn insert-measurement [store m ]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session
                (hayt/insert :measurements
                             (hayt/values m)))))

(def conversions {"vol2kwh" {"gasConsumption" {"m^3" 10.97222
                                              "ft^3" (* 2.83 10.9722)}
                            "oilConsumption" {"m^3" 10308.34
                                              "ft^3" (* 2.83 10308.34)}}
                  "kwh2co2" {"electricityConsumption" {"kWh" 0.517}
                             "gasConsumption" {"kWh" 0.185}
                             "oilConsumption" {"kWh" 0.246}}})

(defn conversion-fn [{:keys [type unit]} operation]
  (let [factor (get-in conversions [operation type unit])]
    (fn [m]
      (cond-> m (m/metadata-is-number? m)
              (assoc :value (str (* factor (read-string (:value m))))
                     :type (m/output-type-for type operation))))))

;;;;;;; Difference series ;;;;;;;;;

(defn- ext-type [type] (str type "_differenceSeries"))

(defn get-difference [m n]
  (let [value (if (every? m/metadata-is-number? [m n]) (str (- (read-string (:value n)) (read-string (:value m)))) "N/A")]
    [[:timestamp (:timestamp m)]
     [:value value]
     [:metadata {"is-number" (if (number? (read-string value)) "true" "false") "median-spike" "n-a"}]
     [:device_id (:device_id m)]
     [:type (ext-type (:type m))]
     [:month (:month m)]]))

(defn do-map
  "Map with side effects."
  [f & lists]
  (apply mapv f lists) nil)

(defn diff-and-insert [store coll]
  (db/with-session [session (:hecuba-session store)]
    (do-map #(db/execute session
                         (hayt/insert :measurements
                                      (hayt/values (get-difference %1 %2)))) coll (rest coll))))

(defn measurements-for-range
  "Returns a lazy sequence of measurements for a sensor matching type and device_id for a specified
  datetime range."
  [store sensor {:keys [start-date end-date]} page]
  (let [device_id  (:device_id sensor)
        type       (:type sensor)
        next-start (t/plus start-date page)]
    (db/with-session [session (:hecuba-session store)]
      (lazy-cat (db/execute session
                               (hayt/select :measurements
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

(defn difference-series
  "Takes store, sensor and a range of dates and calculates difference series using resolution
  stored in the sensor data. If resolution is not specified, default 60 seconds is used."
  [store {:keys [sensor range]}]
  (let [to-datetime                 (fn [s] (tf/parse (tf/formatter "yyyyMMddHHmmss") s))
        resolution                  (if-let [r (:resolution sensor)] (read-string r) 60)
        {:keys [start-date end-date]} range
        expected-timestamps         (map #(hash-map :timestamp %) (timestamp-seq-inclusive start-date end-date resolution))
        measurements                (measurements-for-range store sensor range (t/hours 1))
        template-reading            (-> (first measurements)
                                        (assoc :value "n/a")
                                        (dissoc :timestamp :metadata))]
    (when-not (empty? measurements)
      (let [quantized           (map #(quantize-timestamp % resolution) measurements)
            grouped-readings    (into {} (map #(vector (:timestamp %) %) quantized))
            filled-measurements (map #(merge template-reading (get grouped-readings (:timestamp %) %)) expected-timestamps)]
        (diff-and-insert store filled-measurements)))))


;;;; Convert kWh to co2

(defn convert-to-co2 [store {:keys [sensor range]}]
  (let [get-fn-and-measurements (fn [s] [(conversion-fn s "kwh2co2") (measurements-for-range store s range (t/hours 1))])
        convert                 (fn [[f xs]] (map f xs))
        topic (get-in (:queue store) [:queue "measurements"])
        {:keys [device_id]} sensor]
    (doseq [m  (->> sensor
                    get-fn-and-measurements
                    convert)]
      (q/put-on-queue topic m)
      (insert-measurement store m))))

;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn daily-batch
  [store {:keys [device_id type period]} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/days 1))
          year         (m/get-year-partition-key start-date)
          where        [[= :device_id device_id] [= :type type] [= :year year] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (m/parse-measurements (db/execute session (hayt/select :hourly_rollups (hayt/where where))))
          funct        (case period
                         "CUMULATIVE" (fn [measurements] (reduce + (map :value measurements)))
                         "INSTANT"    (fn [measurements] (/ (reduce + (map :value measurements)) (count measurements)))
                         "PULSE"      (fn [measurements] (reduce + (map :value measurements))))]
      (when-not (empty? measurements)
        (db/execute session
                    (hayt/insert :daily_rollups (hayt/values {:value (str (funct measurements))
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
          measurements (m/parse-measurements (db/execute session (hayt/select :measurements (hayt/where where))))
          filtered     (filter #(number? %) (map :value measurements))
          funct        (case period
                         "CUMULATIVE" (fn [measurements] (reduce + measurements))
                         "INSTANT"    (fn [measurements] (/ (reduce + measurements) (count measurements)))
                         "PULSE"      (fn [measurements] (reduce + measurements)))]
      (when-not (empty? filtered)
        (let [invalid (/ (count filtered) (count measurements))]
          (when-not (and (not= invalid 1) (> invalid 0.10))
            (db/execute session
                        (hayt/insert :hourly_rollups (hayt/values
                                                      {:value (str (funct filtered))
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
        period (:period sensor)
        sensor (if (= "CUMULATIVE" period) (assoc-in sensor [:type] (ext-type (:type sensor))) sensor)]
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
    (Math/abs (mode differences))))

(defn resolution
  "Updates resolution if it's missing. Infers it from last 100 measurements.
  Depending on the order of data in the database, it might not always return last 100 of measurements."
  [store item]
  (db/with-session [session (:hecuba-session store)]
    (let [sensors-to-update (filter #(empty? (:resolution %)) (db/execute session (hayt/select :sensors)))]
      (doseq [sensor sensors-to-update]
        (let [measurements (db/execute session (hayt/select :measurements
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

(defmethod calculate-data-set :vol2kwh [ds store]
  (let [get-fn-and-measurements  (fn [s] [(conversion-fn s (:operation ds)) (measurements/all-measurements store s)])
        convert                  (fn [[f xs]] (map f xs))
        topic (get-in (:queue store) [:queue "measurements"])
        {:keys [operation device_id]} ds]
    (doseq [m  (->> (sensors-for-dataset ds store)
                    (map get-fn-and-measurements)
                    (mapcat convert)
                    (map #(assoc % :device_id device_id)))]
      (q/put-on-queue topic m)
      (insert-measurement store m))))

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
    (let [template-reading (fn [t] (hash-map :value "n/a" :month (m/get-month-partition-key (:timestamp t))))
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
  (let [topic      (get-in (:queue store) [:queue "measurements"])
        sensors    (sensors-for-dataset ds store)
        {:keys [resolution period unit]} (first sensors)
        {:keys [device_id]} ds]
    (when (every? #(and (= period (:period %))
                        (= unit (:unit %))
                        (= resolution (:resolution %))) sensors)
      (let [measurements        (into [] (map #(m/parse-measurements (take 25 (measurements/all-measurements store %))) sensors))
            [start end]         (range-for-padding measurements)
            resolution (if resolution (read-string resolution) (find-resolution (take 100 (first measurements))))
            expected-timestamps (all-timestamps-for-range start end resolution)
            padded       (even-all-collections measurements expected-timestamps resolution)]
        (doseq [m (apply map (fn [& args] (hash-map :value (str (sum args))
                                                    :device_id device_id
                                                    :timestamp (:timestamp (first args))
                                                    :month (:month (first args))
                                                    :type "total_kWh")) padded)]
          (q/put-on-queue topic m)
          (insert-measurement store m))))))

(defn generate-synthetic-readings [store item]
  (let [data-sets (datasets/all-datasets store)]
    ;; TODO This recalculates existing datasets as well - should we only recalcualate when new measurements are inserted?
    (doseq [ds data-sets]
      (log/info "Calculating dataset for: " ds)
      (calculate-data-set ds store))))
