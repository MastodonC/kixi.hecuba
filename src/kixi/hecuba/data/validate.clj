(ns kixi.hecuba.data.validate
 "Data quality assurance and validation."
 (:require [clj-time.core :as t]   
           [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
           [kixi.hecuba.data.misc :as m]))


;;;;;;;;;; Validation on insert ;;;;;;;;;;;;

;; Sensor status and counters ;;;

(defn reset-counters!
  "Resets the counters for a given sensor."
  [commander where]
  (update! commander :sensor {:events 0} where)
  (update! commander :sensor {:errors 0} where))

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
        where  [[= :device-id (:device-id m)] [= :type (:type m)]]]
    (if (is-broken? errors events)
      (update! commander :sensor {:status "Broken"} where)
      (update! commander :sensor {:status "OK"} where))
    m))

(defn label-spike
  "Increment error counter and label median spike in metadata."
  [commander sensor m]
  (let [metadata (-> m :metadata)
        where    [[= :device-id (:device-id m)] [= :type (:type m)]]
        errors   (-> sensor first :errors read-string)]
    (update! commander :sensor {:errors (inc errors)} where)
    (assoc-in m [:metadata] (m/update-metadata metadata {:median-spike "true"}))))

(defn larger-than-median
  "Find readings that are larger than median."
  ([median measurement] (larger-than-median median measurement 200))
  ([median measurement n] (when (and
                                 (not (zero? median))
                                 (not (nil? median))
                                 (number? (:value measurement))) (>= (-> measurement :value read-string) (* n median)))))

(defn median-check
  "Checks if a measurement is 200x median. If so, increments error counter.
  Updates metadata accordingly."
  [m commander querier sensor]
  (let [metadata (-> m :metadata)
        errors   (-> sensor first :errors read-string)
        median   (-> sensor first :median)]
    (cond 
     (empty? median) (assoc-in m [:metadata] (m/update-metadata metadata {:median-spike "n/a"}))
     (nil? median) (assoc-in m [:metadata] (m/update-metadata metadata {:median-spike "n/a"}))
     (larger-than-median (read-string median) m) (label-spike commander sensor m)
     :else (assoc-in m [:metadata] (m/update-metadata metadata {:median-spike "false"})))))

(defn label-invalid-value
  "Labels measurement as having invalid value.
   Increaments error counter of the sensor."
  [commander querier sensor where m]
  (let [metadata (-> m :metadata)   
        errors   (-> sensor first :errors read-string)]  
    (update! commander :sensor {:errors (inc errors)} where)
    (assoc-in m [:metadata] (m/update-metadata metadata {:is-number "false"}))))

(defn number-check
  "Checks if value is a number. If not, increments error counter.
  Updates metadata accordingly."
  [m commander querier sensor]
  (let [metadata  (-> m :metadata)
        where     [[= :device-id (:device-id m)] [= :type (:type m)]]
        value     (:value m)]
    (if (and (not (empty? value)) (m/numbers-as-strings?) value)
      (assoc-in m [:metadata] (m/update-metadata metadata {:is-number "true"}))
      (label-invalid-value commander querier sensor where m))))

(defn validate
  "Measurement map is pipelines through a number of validation
  functions. Returns map of measurement with updated metadata."
  [commander querier m]
  (let [device-id (-> m :device-id)
        type      (-> m :type)
        where     [[= :device-id device-id] [= :type type]]
        sensor    (items querier :sensor where)
        events    (-> sensor first :events read-string)]
    
    (if (= events 1440) (reset-counters! commander where))
    (update! commander :sensor {:events (inc events)} where)

    (-> m
        (number-check commander querier sensor)
        (median-check commander querier sensor)
        (broken-sensor-check commander querier sensor))))


;; Sensor metadata functions that are triggered by core.async queue worker.

(defn update-sensor-metadata
  "Updates start and end dates when new measurement is received."
  [m commander querier]
  (let [device-id         (:device-id m)
        type              (:type m)
        where             [[= :device-id device-id] [= :type type]]
        sensor-metadata   (items querier :sensor-metadata where)
        timestamp         (m/last-check-int-format (:timestamp m))]

    (m/update-date-range commander :rollups where timestamp (:rollups (first sensor-metadata)))
    (m/update-date-range commander :mislabelled-sensors-check where timestamp (:mislabelled-sensors-check (first sensor-metadata)))
    (m/update-date-range commander :difference-series where timestamp (:difference-series (first sensor-metadata)))
    (m/update-date-range commander :median-calc-check where timestamp (:median-calc-check (first sensor-metadata)))
    (m/update-date-range commander :spike-check where timestamp (:spike-check (first sensor-metadata)))))





