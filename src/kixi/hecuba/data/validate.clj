(ns kixi.hecuba.data.validate
 "Data quality assurance and validation."
 (:require [clj-time.core :as t]
           [kixi.hecuba.data.misc :as m]
           [kixi.hecuba.storage.dbnew :as db]
           [qbits.hayt :as hayt]))


;;;;;;;;;; Validation on insert ;;;;;;;;;;;;

;; Sensor status and counters ;;;

(defn- reset-counters!
  "Resets the counters for a given sensor."
  [session where]
  (dbnew/execute session
   (hayt/update :sensors
                (hayt/set-columns {:events 0
                                   :errors 0})
                (hayt/where where))))

(defn- where-from [m]
  [[= :device_id (:device_id m)] [= :type (:type m)]])

(defn- update-sensor [session sensor delta]
  (dbnew/execute session
                 (hayt/update :sensors
                              (hayt/set-columns delta)
                              (hayt/where (where-from sensor)))))

(defn- status-from-measurement-errors
  "If 10% of measurements are invalid, device is broken."
  [sensor]
  (let [errors (-> sensor first :errors)
        events (-> sensor first :events)]
    (if (and (not (zero? errors))
             (not (zero? events))
             (> (/ errors events) 0.1))
      "Broken"
      "Ok")))

(defn- broken-sensor-check
  "Validates and inserts measurments. Updates metadata when errors are doscovered.
  Updates counters."
  [m session sensor]
  (update-sensor session sensor
                 {:status (status-from-measurement-errors sensor)})
  m)


(defn label-spike
  "Increment error counter and label median spike in metadata."
  [store sensor m]
  (let [metadata (-> m :metadata)]
    (dbnew/with-session [session (:hecuba-session store)]
      (update-sensor session (where-from m) {:errors (hayt/inc-by 1)})
      (assoc-in m [:metadata] (m/update-metadata metadata {:median-spike "true"})))))

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
  [m session sensor]
  (let [metadata (-> m :metadata)
        median   (-> sensor first :median)]
    (cond
     (nil? median) (assoc-in m [:metadata] (m/update-metadata metadata {:median-spike "n/a"}))
     (larger-than-median median m) (label-spike session m)
     :else (assoc-in m [:metadata] (m/update-metadata metadata {:median-spike "false"})))))

(defn- label-invalid-value
  "Labels measurement as having invalid value.
   Increments error counter of the sensor."
  [session sensor where m]
  (let [metadata (-> m :metadata)]
    (update-sensor session where {:errors (hayt/inc-by 1)})
    (assoc-in m [:metadata] (m/update-metadata metadata {:is-number "false"}))))

(defn- number-check
  "Checks if value is a number. If not, increments error counter.
  Updates metadata accordingly."
  [m session sensor]
  (let [metadata  (-> m :metadata)
        value     (:value m)]
    (if (and (not (empty? value)) (m/numbers-as-strings? value))
      (assoc-in m [:metadata] (m/update-metadata metadata {:is-number "true"}))
      (label-invalid-value session sensor (where-from m)))))

(defn- sensor-exists? [session m]
  (first (dbnew/execute session
                  (hayt/select :sensors
                               (hayt/where (where-from m))))))

(defn validate
  "Measurement map is pipelines through a number of validation
  functions. Returns map of measurement with updated metadata."
  [store m]
  (dbnew/with-session [session (:hecuba-session store)]
    (let [sensor (sensor-exists? session m)
          events (-> sensor first :events)
          where  (where-from m)
          ]
      (when (= events 1440) (reset-counters! session where))
      (update-sensor session where {:events (hayt/inc-by 1)})
      (-> m
          (number-check session sensor)
          (median-check session sensor)
          (broken-sensor-check session sensor)))))

;; Sensor metadata functions that are triggered by core.async queue worker.

(defn update-sensor-metadata
  "Updates start and end dates when new measurement is received."
  [m commander querier]
  (let [device_id         (:device_id m)
        type              (:type m)
        where             [[= :device_id device_id] [= :type type]]
        sensor_metadata   (items querier :sensor_metadata where)
        timestamp         (m/last-check-int-format (:timestamp m))]

    (m/update-date-range commander :rollups where timestamp (:rollups (first sensor_metadata)))
    (m/update-date-range commander :mislabelled_sensors_check where timestamp (:mislabelled_sensors_check (first sensor_metadata)))
    (m/update-date-range commander :difference_series where timestamp (:difference_series (first sensor_metadata)))
    (m/update-date-range commander :median_calc_check where timestamp (:median_calc_check (first sensor_metadata)))
    (m/update-date-range commander :spike_check where timestamp (:spike_check (first sensor_metadata)))))
