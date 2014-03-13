(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.data.paginate :as p]
            [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]))


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
  (when (and (not (nil? first-coll)) ;; Sequence might not have an equal number of measurements
             (not (nil? (first rest-coll))))
    (let [d         (get-difference rest-coll first-coll)
          device-id (:device-id first-coll)
          type      (:type first-coll)
          timestamp (m/to-timestamp (:timestamp first-coll))
          month     (Integer/parseInt (:month first-coll))]
      (upsert! commander :difference-series {:timestamp timestamp
                                             :value d
                                             :device-id device-id 
                                             :type type
                                             :month month})))
  rest-coll)

(defn calculate-difference-series
  "Takes a commander, querier, sensor and a batch of measurements and calculates the difference
  series. Difference series is inserted into another table."
  [commander querier sensor measurements]
  (let [parsed-m (m/sort-measurments (m/decassandraify-measurements measurements))]
    (loop [m parsed-m]
      (when (not (empty? m))
        (recur (diff-and-insert commander (first m) (rest m)))))))

(defn difference-series
  "Calculates difference in measurements between period n-1 and period n.
  Cumulative sensors only. Includes all measurements, errored too."
  [commander querier item]
  (let [sensors (filter #(= (:period %) "CUMULATIVE") (m/sensors-to-check querier :difference-series))]
    (doseq [s sensors]
      (let [device-id  (:device-id s)
            type       (:type s)
            last-check (:difference-series s)
            where      (m/last-check-where-clause device-id type last-check)
            today      (m/last-check-int-format (t/now))]
        (if (p/paginate commander querier s :measurement where calculate-difference-series)
          (update! commander :sensor-metadata :difference_series today {:device-id device-id :type type}))))))


;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn next-start [m] (m/to-timestamp (:timestamp (last m))))

(defn add-hour [t] (java.util.Date. (+ (.getTime t) (* 60 60 1000))))

(defn hour-batch 
  [commander querier sensor where]
  (let [measurements (m/decassandraify-measurements (items querier :difference-series where))]
    (when-not (empty? measurements)
      (let [timestamp (m/to-timestamp (:timestamp (last measurements)))
            device-id (:device-id sensor)
            type      (:type sensor)]
        (upsert! commander :hourly-rollups {:value (str (reduce + (map :value measurements)))
                                            :timestamp timestamp
                                            :month (m/get-month-partition-key timestamp)
                                            :device-id device-id
                                            :type type})
        (update! commander :sensor-metadata :hourly_rollups (m/last-check-int-format timestamp) {:device-id device-id :type type})
        [:device-id device-id
         :type type 
         :month (m/get-month-partition-key timestamp)
         :timestamp [> timestamp]
         :timestamp [<= (add-hour timestamp)]]))))

(defn hourly-rollups
  "Calculates hourly rollups for cumulative sensors."
  [commander querier item]
  (let [period  (:period item)
        sensors (filter #(= (:period %) period) (m/sensors-to-check querier :hourly-rollups))]
    (doseq [s sensors]
      (let [device-id  (:device-id s)
            type       (:type s)
            last-check (:hourly-rollups s)
            timestamp  (if-not (= "" last-check)
                         (.parse (java.text.SimpleDateFormat. "yyyyMMddHHmmss") last-check)
                         (when-let [t (:timestamp (first (items querier :difference-series {:device-id device-id
                                                                                            :type type} 1)))]
                           (m/to-timestamp t)))]

        (when timestamp
          (loop [where  [:device-id device-id
                         :type type
                         :month (m/get-month-partition-key timestamp)
                         :timestamp [<= (add-hour timestamp)]]]
            (when-not (nil? where)
              (recur (hour-batch commander querier s where)))))))))
