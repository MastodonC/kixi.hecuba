(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]))


(defn hourly-timestamp [t] 
  (tc/to-date (tf/unparse (tf/formatters :date-hour) (tc/from-date t))))
(defn daily-timestamp [t]
  (tc/to-date (tf/unparse (tf/formatters :date) (tc/from-date t))))

(defn add-hour [t] (java.util.Date. (+ (.getTime t) (* 60 60 1000))))
(defn add-day [t]  (java.util.Date. (+ (.getTime t) (* 24 60 60 1000))))


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
  (let [end-date     (add-hour start-date)
        month        (m/get-month-partition-key start-date)
        where        [:device-id device-id :type type :month month :timestamp [>= start-date] :timestamp [< end-date]]
        measurements (m/decassandraify-measurements (items querier :measurement where))]
    (when-not (empty? measurements) 
      (loop [m measurements]
        (when-not (empty? m)
          (update! commander :sensor-metadata :difference-series (m/last-check-int-format start-date)
                   {:device-id device-id :type type})
          (recur (diff-and-insert commander (first m) (rest m))))))
    end-date))

(defn difference-series
  "Takes commander, querier, sensor and date range and calculates difference series for that range."
  [commander querier {:keys [sensor range]}]
  (let [start  (m/to-timestamp (:start-date range))
        end    (m/to-timestamp (:end-date range))]
    (loop [start-date start]
      (when-not (.before end start-date)
        (recur (calculate-difference-series commander querier sensor start-date))))))


;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn get-year-partition-key
  [timestamp]
  (Long/parseLong (format "%4d" (t/year (tc/from-date timestamp)))))

(defn daily-batch
  [commander querier {:keys [device-id type period]} start-date]
  (let [end-date     (add-day start-date)
        year         (get-year-partition-key start-date)
        where        [:device-id device-id :type type :year year :timestamp [>= start-date] :timestamp [< end-date]]
        measurements (m/decassandraify-measurements (items querier :hourly-rollups where))
        funct        (case period
                       "CUMULATIVE" (fn [measurements] (reduce + (map :value measurements)))
                       "INSTANT"    (fn [measurements] (/ (reduce + (map :value measurements)) (count measurements)))
                       "PULSE"      (fn [measurements] (reduce + (map :value measurements))))]
    (when-not (empty? measurements)
      (upsert! commander :daily-rollups {:value (str (funct measurements))
                                         :timestamp start-date
                                         :device-id device-id
                                         :type type})
      (update! commander :sensor-metadata :daily-rollups (m/last-check-int-format start-date) {:device-id device-id :type type}))
    end-date))


(defn daily-rollups
  "Calculates daily rollups for given sensor and date range."
  [commander querier {:keys [sensor range]}]
  (let [start         (daily-timestamp (m/to-timestamp (:start-date range)))
        end           (daily-timestamp (m/to-timestamp (:end-date range)))]
    (loop [start-date  start]
      (when-not (.before end start-date)
        (recur (daily-batch commander querier sensor start-date))))))

(defn hour-batch 
  [commander querier {:keys [device-id type period]} start-date table]
  (let [end-date     (add-hour start-date)
        month        (m/get-month-partition-key start-date)
        where        [:device-id device-id :type type :month month :timestamp [>= start-date] :timestamp [< end-date]]
        measurements (m/decassandraify-measurements (items querier table where))
        filtered     (filter #(number? %) (map :value measurements))
        funct        (case period
                       "CUMULATIVE" (fn [measurements] (reduce + measurements))
                       "INSTANT"    (fn [measurements] (/ (reduce + measurements) (count measurements)))
                       "PULSE"      (fn [measurements] (reduce + measurements)))]
    (when-not (and (empty? measurements) (empty? filtered))
      (let [invalid (/ (count filtered) (count measurements))]
        (when-not (and (not= invalid 1) (> invalid 0.10))
          (upsert! commander :hourly-rollups {:value (str (funct filtered))
                                              :timestamp end-date
                                              :year (get-year-partition-key start-date)
                                              :device-id device-id
                                              :type type})
          (update! commander :sensor-metadata :hourly-rollups (m/last-check-int-format end-date) {:device-id device-id :type type}))))
    end-date))

(defn hourly-rollups
  "Calculates hourly rollups for given sensor and date range.
  Example of item: {:sensor {:device-id \"f11a21b8e5e6b97eacba2632db4a2037a43f4791\" :type \"temperatureGround\"
                   :period \"CUMULATIVE\"} :range {:start-date \"Sat Mar 01 00:00:00 UTC 2014\" 
                   :end-date \"Sun Mar 02 23:00:00 UTC 2014\"}}"
  [commander querier {:keys [sensor range]}]
  (let [start         (m/to-timestamp (:start-date range))
        end           (m/to-timestamp (:end-date range))
        period        (:period sensor)
        table         (case period
                        "CUMULATIVE" :difference-series
                        "INSTANT"    :measurement
                        "PULSE"      :measurement)]
    (loop [start-date  start]
      (when-not (.before end start-date)
        (recur (hour-batch commander querier sensor start-date table))))))



;; TODO These are helper functions. Rollups and difference series should be triggered by kafka/core.async + scheduler. 

(defn difference-series-batch
  "Retrieves all sensors that need to have difference series calculated and performs calculations."
  [commander querier item]
  (let [sensors (filter #(= (:period %) "CUMULATIVE") (m/sensors-to-check querier :difference-series))]
    (doseq [s sensors]
      (let [device-id   (:device-id s)
            type        (:type s)
            last-check  (:difference-series s)
            start-date  (if-not (empty? last-check)
                          (tf/unparse m/db-date-formatter (tf/parse m/int-time-formatter last-check))
                          (when-let [t (:timestamp (first (items querier :measurement {:device-id device-id :type type} 1)))]
                            (tf/unparse m/db-date-formatter (tc/from-date (m/to-timestamp t)))))
            today       (tf/unparse m/db-date-formatter (t/now))]
        (when start-date
          (difference-series commander querier (assoc item :sensor s :range {:start-date start-date
                                                                             :end-date today})))))))


(defn rollups
  "Retrieves all sensors that need to have hourly measurements rolled up and performs calculations."
  [commander querier item]
  (let [sensors (m/sensors-to-check querier :hourly-rollups)]
    (doseq [s sensors]
      (let [device-id  (:device-id s)
            type       (:type s)
            period     (:period s)
            table      (case period
                         "CUMULATIVE" :difference-series
                         "INSTANT"    :measurement
                         "PULSE"      :measurement)
            last-check (:hourly-rollups s)
            start-date (if-not (empty? last-check)
                         (tf/unparse m/db-date-formatter (tf/parse m/int-time-formatter last-check))
                         (when-let [t (:timestamp (first (items querier table {:device-id device-id :type type} 1)))]
                           (tf/unparse m/db-date-formatter (tc/from-date (m/to-timestamp t)))))
            today      (tf/unparse m/db-date-formatter (t/now))]
        (when start-date
          (hourly-rollups commander querier (assoc item :sensor s :range {:start-date start-date
                                                                          :end-date today}))))))

  (let [sensors (m/sensors-to-check querier :daily-rollups)]
    (doseq [s sensors]
      (let [device-id  (:device-id s)
            type       (:type s)
            period     (:period s)
            last-check (:daily-rollups s)
            start-date (if-not (empty? last-check)
                         (tf/unparse m/db-date-formatter (tf/parse m/int-time-formatter last-check))
                         (when-let [t (:timestamp (first (items querier :hourly-rollups {:device-id device-id :type type} 1)))]
                           (tf/unparse m/db-date-formatter (tc/from-date (m/to-timestamp t)))))
            today      (tf/unparse m/db-date-formatter (t/now))]
        (when start-date
          (daily-rollups commander querier (assoc item :sensor s :range {:start-date start-date
                                                                         :end-date today})))))))
