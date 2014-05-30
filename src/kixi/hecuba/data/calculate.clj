(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.datasets :as datasets]
            [kixi.hecuba.api.measurements :as measurements]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.storage.db :as db]
            [qbits.hayt :as hayt]
            [kixi.hecuba.queue :as q]))

;;;;;;; Difference series using resolution ;;;;;;;;;

(defn get-difference [m n]
  [[:timestamp (:timestamp m)]
   [:value (if (every? m/metadata-is-number? [m n]) (str (- (read-string (:value n)) (read-string (:value m)))) "N/A")]
   [:device_id (:device_id m)]
   [:type (:type m)]
   [:month (:month m)]])

(defn do-map [f & lists] (apply mapv f lists) nil)

(defn diff-and-insert [store coll]
  (db/with-session [session (:hecuba-session store)]
    (do-map #(db/execute session
                         (hayt/insert :difference_series
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

(defmulti quantize-timestamp (fn [m resolution] resolution))

(defmethod quantize-timestamp 60
  [m resolution]
  (let [t       (:timestamp m)
        rounded (m/truncate-seconds t)]
    (assoc-in m [:timestamp] rounded)))

(defmethod quantize-timestamp :default
  [m resolution]
  (quantize-timestamp m 60))

;; TODO Should we trigger resolution calculation if it's not present? At the moment it's a separate job.
(defn difference-series-from-resolution
  "Takes store, sensor and a range of dates and calculates difference series using resolution
  stored in the sensor data. If resolution is not specified, calculation is not done."
  [store {:keys [sensor range]}]
  (let [to-datetime  (fn [s] (tf/parse (tf/formatter "yyyyMMddHHmmss") s))
        measurements (measurements-for-range store sensor range (t/hours 1))
        resolution   (:resolution sensor)]
    (when (and (not (empty? measurements)) (not (nil? resolution)))
      (let [quantized (map #(quantize-timestamp % resolution) measurements)]
        (diff-and-insert store quantized)))))


;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn daily-batch
  [store {:keys [device_id type period]} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/days 1))
          year         (m/get-year-partition-key start-date)
          where        [[= :device_id device_id] [= :type type] [= :year year] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (m/decassandraify-measurements (db/execute session (hayt/select :hourly_rollups (hayt/where where))))
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
  [store {:keys [device_id type period]} start-date table]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/hours 1))
          month        (m/get-month-partition-key start-date)
          where        [[= :device_id device_id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (m/decassandraify-measurements (db/execute session (hayt/select table (hayt/where where))))
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
        table  (case period
                 "CUMULATIVE" :difference_series
                 "INSTANT"    :measurements
                 "PULSE"      :measurements)]
    (loop [start  start-date]
      (when-not (t/before? end-date start)
        (recur (hour-batch store sensor start table))))))

;;;;;;;;; Resolution calculation ;;;;;;;;;;

(defn diff [coll]
  (map - coll (rest coll)))

(defn mode [coll]
  (first (last (sort-by second (frequencies coll)))))

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
              differences  (diff (map #(/ (.getTime (m/truncate-seconds (:timestamp %))) 1000) measurements))
              resolution  (str (mode differences))]
          (db/execute session (hayt/update :sensors
                                           (hayt/set-columns [[:resolution resolution]])
                                           (hayt/where [[= :device_id (:device_id sensor)]
                                                        [= :type (:type sensor)]]))))))))

;;;;;; Calculated datasets ;;;;;;;;;;;

(defn sensors-for-dataset
  "Returns all the sensors for the given dataset."
  [{:keys [members]} store]
  (db/with-session [session (:hecuba-session store)]
    (let [parse-sensor (comp next (partial re-matches #"(\w+)-(\w+)"))
          sensor (fn [[type device_id]]
                   (db/execute session
                               (hayt/select :sensors
                                            (hayt/where [[= :type type]
                                                         [= :device_id device_id]]))))]
      (mapcat sensor (->> members
                          (keep parse-sensor)
                          (into (hash-set)))))))


(defn insert-measurement [store m ]
  (prn "m: " m)
  (db/with-session [session (:hecuba-session store)]
    (db/execute session
                (hayt/insert :measurements
                             (hayt/values m)))))

(defn output-unit-for [t]
  (log/error t)
  (case t
    "vol2kwh" "kWh"
    "total-vol2kwh" "kWh"))

(defn output-type-for [t]
  (str "converted_" t))

(def conversions {"gasConsumption" {"m^3" 10.97222
                                    "ft^3" (* 2.83 10.9722)}
                  "oilConsumption" {"m^3" 10308.34
                                    "ft^3" (* 2.83 10308.34)}})

(defn conversion-fn [{:keys [type unit]}]
  (let [factor (get-in conversions [type unit])]
    (fn [m]
      (cond-> m (m/metadata-is-number? m)
              (assoc :value (str (* factor (read-string (:value m))))
                     :type (output-type-for type))))))

(defmulti calculate-data-set (comp keyword :operation))

(defmethod calculate-data-set :vol2kwh [ds store]

  (let [get-fn-and-measurements  (fn [s] [(conversion-fn s) (measurements/all-measurements store s)])
        convert                  (fn [[f xs]] (map f xs))
        topic (get-in (:queue store) [:queue "measurements"])
        {:keys [operation device_id]} ds]
    (doseq [m  (->> (sensors-for-dataset ds store)
                    (map get-fn-and-measurements)
                    (mapcat convert)
                    (map #(assoc % :device_id device_id)))]
      (q/put-on-queue topic m)
      (insert-measurement store m))))

;; TODO Should we get all kWh sensors for an entity or should the user provide those?
(defmethod calculate-data-set :total-vol2kwh [ds store]
  (let [topic               (get-in (:queue store) [:queue "measurements"])
        sensors             (sensors-for-dataset ds store)
        get-measurements    (fn [s] (prn "getting measurements for: " s) (measurements/all-measurements store s))
        {:keys [device_id]} ds]
    (map (fn [& m] (->> {:value (reduce + m)
                         :device_id device_id
                         :type (output-type-for "total-vol2kwh")
                         :timestamp (:timestamp m)}
                        (q/put-on-queue topic)
                        (insert-measurement store))) (doseq [s sensors] (get-measurements s)))))

(defn generate-synthetic-readings [store item]
  (let [data-sets (datasets/all-datasets store)]
    (doseq [ds data-sets]
      (log/info "Calculating dataset for: " ds)
      (calculate-data-set ds store))))
