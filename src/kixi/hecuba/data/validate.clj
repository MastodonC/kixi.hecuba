(ns kixi.hecuba.data.validate
 "Data quality assurance and validation."
 (:require [clj-time.core :as t]   
           [clj-time.format :as tf]
           [clj-time.coerce :as tc]
           [com.stuartsierra.frequencies :as freq]
           [clojure.data.json :as json]
           [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
           [kixi.hecuba.db :as db]))

;;; Helper functions ;;;

(def int-time-formatter (tf/formatter "yyyyMMdd"))

(defn to-timestamp
  [t]
  (tc/to-date (tf/parse (tf/formatter "EEE MMM dd HH:mm:ss z yyyy") t)))

(defn numbers-as-strings? [& strings]
  (every? #(re-find #"^-?\d+(?:\.\d+)?$" %) strings))

(defn parse-double [txt]
  (Double/parseDouble txt))

(defn update-metadata
  [metadata-str new-map-entry]
  (let [metadata (read-string metadata-str)]
    (str (conj metadata new-map-entry))))

(defn parse-value
  [m]
  (assoc-in m [:value] (parse-double (:value m))))

(defmulti get-month-partition-key
  "Returns integer representation of a month part of timestamp."
  type)

(defmethod get-month-partition-key java.util.Date
  [t]
  (let [timestamp (tc/from-date t)]
    (Integer/parseInt (format "%4d%02d" (t/year timestamp) (t/month timestamp)))))

(defmethod get-month-partition-key org.joda.time.DateTime
  [t]
  (Integer/parseInt (format "%4d%02d" (t/year t) (t/month t))))

(defmethod get-month-partition-key java.lang.String
  [t]
  (let [timestamp (tf/parse int-time-formatter t)]
    (Integer/parseInt (format "%4d%02d" (t/year timestamp) (t/month timestamp)))))

(defn last-check-where-clause
  [device-id type last-check]
  (conj {:device-id device-id :type type}
         (if (= last-check "")
           {:month [<= (get-month-partition-key (t/now))]}
           {:month (get-month-partition-key last-check) :timestamp [>= last-check]})))

(defn parse-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m]
         (-> m
             (assoc-in [:value] (read-string (:value m)))
             (assoc-in [:metadata] (read-string (:metadata m)))))
       measurements))

(defn sensors-to-check
  "Finds sensors that needs to have data quality checked: with no check performed
  or with a check older than a week."
  [querier validation-type]
  (let [last-week        (Integer/parseInt (tf/unparse int-time-formatter (t/minus (t/now) (t/weeks 1))))
        sensors-metadata (items querier :sensor-metadata)
        sensors-to-check (filter #(or (= "" (validation-type %))
                                       (<= (Integer/parseInt (validation-type %)) last-week)) sensors-metadata)]
    (map #(merge (first (items querier :sensor {:device-id (:device-id %) :type (:type  %)})) %)sensors-to-check)))


(defn measurement-batch
  [commander querier where paginate-key per-page last-key sensor funct]
  (let [measurements (items querier :measurement where paginate-key per-page last-key)]
    (when-not (empty? measurements)
      (funct commander querier sensor measurements)
      (to-timestamp (:timestamp (last measurements))))))

(defn paginate
  [commander querier sensor check funct]
  (let [device-id       (-> sensor :device-id)
        type            (-> sensor :type)
        last-check      (-> sensor check)
        where           (last-check-where-clause device-id type last-check)
        first-result    (items querier :measurement where :timestamp 10)
        last-timestamp  (to-timestamp (:timestamp (last first-result)))
        where           (last-check-where-clause device-id type last-check)]
    (loop [last-key last-timestamp]
      (when (not (nil? last-key))
        (recur (measurement-batch commander querier (conj where {:month (get-month-partition-key last-key)})
                                 :timestamp 10 last-key sensor funct))))))

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
(defmethod labelled-correctly? "CUMULATIVE" [sensor measurements] (going-up? measurements))
(defmethod labelled-correctly? "PULSE" [sensor measurements] (empty? (filter neg-or-not-int? measurements)))

(defn label-mislabelled
  ""
  [commander sensor errors where]
  (update! commander :sensor-metadata :mislabelled "true" where)
  (update! commander :sensor :errors errors where))

(defn mislabelled-check
  ""
  [commander querier sensor measurements]
  (let [today  (Integer/parseInt (tf/unparse int-time-formatter (t/now)))
        errors (-> sensor :errors read-string)
        where  {:device-id (:device-id sensor) :type (:type sensor)}]
    (if (labelled-correctly? sensor (parse-measurements measurements))
      (label-mislabelled commander sensor (inc errors) where)
      (update! commander :sensor-metadata :mislabelled "false" where))
    (update! commander :sensor-metadata :mislabelled_sensors_check today where)))

(defn mislabelled-sensors
  "Checks for mislabelled sensors in batches."
  [commander querier item]
  (let [sensors (sensors-to-check querier :mislabelled-sensors-check)]
    (doseq [s sensors]
      (paginate commander querier s :mislabelled-sensors-check mislabelled-check))))

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
  (let [m (filter #(remove-bad-readings %) measurements)]
    (when (not (empty? m))
      (freq/median (frequencies (map :value m))))))

(defn update-median
  "Calculates and updates median for a given sensor."
  [commander querier sensor measurements]
  (let [today          (Integer/parseInt (tf/unparse int-time-formatter (t/now)))
        errors         (-> sensor :errors read-string)
        where          {:device-id (:device-id sensor) :type (:type sensor)}
        median         (median (parse-measurements measurements))]
    (when (number? median)
      (update! commander :sensor :median median where)
      (update! commander :sensor-metadata :median_calc_check today where))))

(defn median-calculation
  "Retrieves all sensors that either have not had median calculated or the calculation took place over a week ago.
  It iterates over the list of sensors, returns measurements for each and performs calculation.
  Measurements are retrieved in batches."
  [commander querier item]
  (let [sensors (sensors-to-check querier :median-calc-check)]
    (doseq [sensor sensors]
      (paginate commander querier sensor :median-calc-check update-median))))


;;;;;;;;;; Validation on insert ;;;;;;;;;;;;

;; Sensor status and counters ;;;

(defn reset-counters!
  "Resets the counters for a given sensor."
  [commander where]
  (update! commander :sensor :events 0 where)
  (update! commander :sensor :errors 0 where))

(defn is-broken?
  "If 10% of measurements are invalid, device is broken."
  [errors events]
  (when (and (not (zero? errors)) (not (zero? events)))
    (> (/ errors events) 0.1)))

(defn broken-sensor-check
  "Validates and inserts measurments. Updates metadata when errors are doscovered.
  Updates counters."
  [m commander querier sensor]
  (let [errors (-> sensor first :errors read-string)
        events (-> sensor first :events read-string)
        where  {:device-id (:device-id m) :type (:type m)}]
    (if (is-broken? errors events)
      (update! commander :sensor :status "Broken" where)
      (update! commander :sensor :status "OK" where))
    m))

(defn label-spike
  "Increment error counter and label median spike in metadata."
  [commander sensor m]
  (let [metadata (-> m :metadata)
        where    {:device-id (:device-id m) :type (:type m)}
        errors   (-> sensor first :errors read-string)]
    (update! commander :sensor :errors (inc errors) where)
    (assoc-in m [:metadata] (update-metadata metadata {:median-spike "true"}))))

(defn larger-than-median
  "Find readings that are larger than median."
  ([median measurement] (larger-than-median median measurement 200))
  ([median measurement n] (when (and
                                 (not (zero? median))
                                 (not (nil? median))) (>= (:value measurement) (* n median)))))

(defn median-check
  "Checks if measurements is 200x median. If so, increments error counter.
  Updates metadata accordingly."
  [m commander querier sensor] 
  (let [metadata (-> m :metadata)
        errors   (-> sensor first :errors read-string)
        median   (-> sensor first :median read-string)
        ]
    (if (larger-than-median median m)
      (label-spike commander sensor m)
      (assoc-in m [:metadata] (update-metadata metadata (if (and (not (nil? median))
                                                                (not (zero? median)))  
                                                          {:median-spike "false"}
                                                          {:median-spike "n/a"}))))))

(defn label-invalid-value
  "Labels measurement as having invalid value.
   Increaments error counter of the sensor."
  [m commander querier sensor]
  (let [metadata (-> m :metadata)
        where    {:device-id (:device-id m) :type (:type m)}
        errors   (-> sensor first :errors read-string)]
    (update! commander :sensor :errors (inc errors) where)
    (assoc-in m [:metadata] (update-metadata metadata {:is-number "false"}))))

(defn number-check
  "Checks if value is a number. If not, increments error counter.
  Updates metadata accordingly."
  [m commander querier sensor]
  (let [metadata  (-> m :metadata)
        where     {:device-id (:device-id m) :type (:type m)}]
    (if (numbers-as-strings? (:value m))
      (assoc-in m [:metadata] (update-metadata metadata {:is-number "true"}))
      (label-invalid-value commander querier where m))))

(defn validate
  "Measurement map is pipelines through a number of validation
  functions. Returns map of measurement with updated metadata."
  [commander querier m]
  (let [device-id (-> m :device-id)
        type      (-> m :type)
        where     {:device-id device-id :type type}
        sensor    (items querier :sensor where)
        events    (-> sensor first :events read-string)]
    
    (= events 1440) (reset-counters! commander where)
    (update! commander :sensor :events (inc events) where)

    (-> m
        (number-check commander querier sensor)
        (median-check commander querier sensor)
        (broken-sensor-check commander querier sensor))
    ))





