(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.storage.dbnew :as dbnew]
            [kixi.hecuba.api.measurements :as measurements]
            [clj-time.coerce :as tc]
            [qbits.hayt :as hayt]))

(defn get-difference
  "Returns difference if both values are numbers, otherwise returns N/A."
  [rest-coll first-coll]
  (let [m (first rest-coll)
        n first-coll]
    (if (and (= "true" (get-in m [:metadata :is-number]))
             (= "true" (get-in n [:metadata :is-number])))
      (str (- (:value m) (:value n)))
      "N/A")))

(defn diff-and-insert
  "Calculates difference and inserts it to differece_series table.
  Column's header is the end of the calculated interval."
  [commander first-coll rest-coll]
  (when (and (not (nil? first-coll)) ;; Sequence might not have an even number of measurements
             (not (nil? (first rest-coll))))
    (let [d         (get-difference rest-coll first-coll)
          device-id (:device-id first-coll)
          type      (:type first-coll)
          timestamp (m/to-timestamp (:timestamp (first rest-coll)))
          month     (Integer/parseInt (:month first-coll))]
      (upsert! commander :difference-series {:timestamp timestamp
                                             :value d
                                             :device-id device-id
                                             :type type
                                             :month month})))
  rest-coll)

(defn calculate-difference-series
  "Takes a commander, querier, sensor and a start date. Retrieves an hour worth of data and calculates the difference
  series. Difference series is inserted into another table."
  [commander querier {:keys [device-id type period]} start-date]
  (let [end-date     (m/add-hour start-date)
        month        (m/get-month-partition-key start-date)
        where        [[= :device-id device-id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
        measurements (m/decassandraify-measurements (items querier :measurement where))]
    (when-not (empty? measurements)
      (loop [m measurements]
        (when-not (empty? m)
          (recur (diff-and-insert commander (first m) (rest m))))))
    end-date))

(defn difference-series
  "Takes commander, querier, sensor and date range and calculates difference series for that range."
  [commander querier {:keys [sensor range]}]
  (let [start  (m/int-format-to-timestamp (:start-date range))
        end    (m/int-format-to-timestamp (:end-date range))]
    (loop [start-date start]
      (when-not (.before end start-date)
        (recur (calculate-difference-series commander querier sensor start-date))))))


;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn daily-batch
  [commander querier {:keys [device-id type period]} start-date]
  (let [end-date     (m/add-day start-date)
        year         (m/get-year-partition-key start-date)
        where        [[= :device-id device-id] [= :type type] [= :year year] [>= :timestamp start-date] [< :timestamp end-date]]
        measurements (m/decassandraify-measurements (items querier :hourly-rollups where))
        funct        (case period
                       "CUMULATIVE" (fn [measurements] (reduce + (map :value measurements)))
                       "INSTANT"    (fn [measurements] (/ (reduce + (map :value measurements)) (count measurements)))
                       "PULSE"      (fn [measurements] (reduce + (map :value measurements))))]
    (when-not (empty? measurements)
      (upsert! commander :daily-rollups {:value (str (funct measurements))
                                         :timestamp start-date
                                         :device-id device-id
                                         :type type}))
    end-date))


(defn daily-rollups
  "Calculates daily rollups for given sensor and date range."
  [commander querier {:keys [sensor range]}]
  (let [start         (m/int-format-to-timestamp (:start-date range))
        end           (m/int-format-to-timestamp (:end-date range))]
    (loop [start-date  start]
      (when-not (.before end start-date)
        (recur (daily-batch commander querier sensor start-date))))))

(defn hour-batch
  [commander querier {:keys [device-id type period]} start-date table]
  (let [end-date     (m/add-hour start-date)
        month        (m/get-month-partition-key start-date)
        where        [[= :device-id device-id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
        measurements (m/decassandraify-measurements (items querier table where))
        filtered     (filter #(number? %) (map :value measurements))
        funct        (case period
                       "CUMULATIVE" (fn [measurements] (reduce + measurements))
                       "INSTANT"    (fn [measurements] (/ (reduce + measurements) (count measurements)))
                       "PULSE"      (fn [measurements] (reduce + measurements)))]
    (when-not (empty? filtered)
      (let [invalid (/ (count filtered) (count measurements))]
        (when-not (and (not= invalid 1) (> invalid 0.10))
          (upsert! commander :hourly-rollups {:value (str (funct filtered))
                                              :timestamp end-date
                                              :year (m/get-year-partition-key start-date)
                                              :device-id device-id
                                              :type type}))))
    end-date))

(defn hourly-rollups
  "Calculates hourly rollups for given sensor and date range.
  Example of item: {:sensor {:device-id \"f11a21b8e5e6b97eacba2632db4a2037a43f4791\" :type \"temperatureGround\"
                   :period \"CUMULATIVE\"} :range {:start-date \"Sat Mar 01 00:00:00 UTC 2014\"
                   :end-date \"Sun Mar 02 23:00:00 UTC 2014\"}}"
  [commander querier {:keys [sensor range]}]
  (let [start         (m/int-format-to-timestamp (:start-date range))
        end           (m/int-format-to-timestamp (:end-date range))
        period        (:period sensor)
        table         (case period
                        "CUMULATIVE" :difference-series
                        "INSTANT"    :measurement
                        "PULSE"      :measurement)]
    (loop [start-date  start]
      (when-not (.before end start-date)
        (recur (hour-batch commander querier sensor start-date table))))))

(defn normalize-dataset[m]
  (-> m
      (update-in [:members] #(str/split % #"\s*,\s*"))))

(defn resolve-sensors
  "Returns all the sensors (with metadata) for the given dataset."
  [{:keys [members]} querier]

  (let [parse-sensor (comp next (partial re-matches #"(\w+)-(\w+)"))
        sensor-with-metadata (fn [[type device-id]]
                               (->> [:sensor :sensor-metadata]
                                    (mapcat #(items querier % [[= :device-id device-id]
                                                               [= :type type]]))
                                    (apply merge)))]
    (map sensor-with-metadata (->> members
                                   (keep parse-sensor)
                                   (into (hash-set))))))

(defmulti calculate-data-set (comp keyword :type))

(defmethod calculate-data-set :vol2kwh [ds store]
  ;; (let [sensors (sensors-for-dataset ds querier)
  ;;       ms (map (fn [m] (measurements/all-measurements querier
  ;;                                        (select-keys m [:type :device-id])) ))
  ;;       ]
  ;;   (first sensors)
  ;;   )
  )

(defn generate-synthetic-readings [store item]
  ;; (let [data-sets]
  ;;   (doseq [ds data-sets]
  ;;     (calculate-data-set (normalize-dataset ds)
  ;;                         querier)))
  )
