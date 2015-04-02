(ns kixi.hecuba.data.tariff-calculations
  "Collection of functions that support tariff calculations."
  (:require [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.time              :as time]
            [kixi.hecuba.data.calculate    :as c]
            [clj-time.coerce               :as tc]
            [clj-time.core                 :as t]
            [kixi.hecuba.data.sensors      :as sensors]
            [kixi.hecuba.storage.db        :as db]
            [clojure.tools.logging         :as log]))

(defn min-value
  "Finds the least of a sequence of numbers.
  Returns a numerical value."
  [xs]
  (when (seq xs)
    (apply min xs)))

(defn max-value
  "Finds the largest of a sequence of numbers.
  Returns a numerical value."
  [xs]
  (when (seq xs)
    (apply max xs)))

(defn avg-value
  "Finds the average of a sequence of numbers.
  Returns a numerical value."
  [xs]
  (when (seq xs)
    (c/average-reading xs)))

(defn calculate-reading-from-seq
  "Gets a sequence of data for a period of time.
  Filters out numerical values and calculates
  them using provided function."
  [calculation-fn xs]
  (->> xs
       (measurements/parse-measurements)
       (keep :value)
       (calculation-fn)))

(defmulti calculation (fn [calculation data] calculation))

(defmethod calculation :min-for-day [_ data]
  (calculate-reading-from-seq min-value data))

(defmethod calculation :max-for-day [_ data]
  (calculate-reading-from-seq max-value data))

(defmethod calculation :avg-for-day [_ data]
  (calculate-reading-from-seq avg-value data))

(defmethod calculation :min-rolling-4-weeks [_ data]
  (calculate-reading-from-seq min-value data))

(defmethod calculation :max-rolling-4-weeks [_ data]
  (calculate-reading-from-seq max-value data))

(defmethod calculation :avg-rolling-4-weeks [_ data]
  (calculate-reading-from-seq avg-value data))

(defn calculate-batch
  [store {:keys [device_id type sensor_id period]} start-date end-date calculation-type]
  (db/with-session [session (:hecuba-session store)]
    (let [month            (time/get-month-partition-key start-date)
          where            [[= :device_id device_id] [= :sensor_id sensor_id] [= :month month]
                            [>= :timestamp start-date] [< :timestamp end-date]]
          measurements     (measurements/fetch-measurements store where)
          new-type         (sensors/output-type-for type calculation-type)
          output-sensor_id (:sensor_id (sensors/get-by-type {:device_id device_id :type new-type} session))]
      (when (seq measurements)
        (if output-sensor_id
          (let [calculated-sensor {:device_id device_id :sensor_id output-sensor_id}
                calculated-data   [{:value (str (c/round (calculation calculation-type measurements)))
                                    :timestamp (tc/to-date end-date)
                                    :month (time/get-month-partition-key start-date)
                                    :device_id device_id
                                    :sensor_id sensor_id}]]
            (measurements/insert-measurements store calculated-sensor 10 calculated-data)
            (sensors/update-sensor-metadata session calculated-sensor start-date end-date))
          (log/errorf "Could not find the output sensor_id for device_id %s and new type %s" device_id new-type))))))

(defn reading-for-a-day
  "Performs calculation for a single day.
  Works in batches of one day. Result of each batch is
  inserted onto C*."
  [store {:keys [sensor range calculation-type]}]
  (let [{:keys [start-date end-date]} range]
    (doseq [timestamp (time/seq-dates start-date end-date (t/days 1))]
      (calculate-batch store sensor timestamp (t/plus timestamp (t/days 1)) calculation-type))))

(defn reading-rolling-for-4-weeks
  "Performs calculation for a rolling 4 week window.
  Works in batches of a day, and inserts found value into C*."
  [store {:keys [sensor range calculation-type]}]
  (let [{:keys [start-date end-date]} range]
    (doseq [timestamp (time/seq-dates start-date end-date (t/days 1))]
      (calculate-batch store sensor (t/minus timestamp (t/weeks 4)) (t/plus timestamp (t/days 1)) calculation-type))))
