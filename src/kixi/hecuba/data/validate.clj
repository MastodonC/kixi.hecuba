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
  (db/execute session
   (hayt/update :sensors
                (hayt/set-columns {:events 0
                                   :errors 0})
                (hayt/where where))))

(defn- where-from [m]
  [[= :device_id (:device_id m)] [= :type (:type m)]])

(defn- update-sensor [session sensor delta]
  (db/execute session
                 (hayt/update :sensors
                              (hayt/set-columns delta)
                              (hayt/where (where-from sensor)))))
(defn- update-sensor-metadata [session sensor-metadata delta]
  (db/execute session
                 (hayt/update :sensors
                              (hayt/set-columns delta)
                              (hayt/where (where-from sensor-metadata)))))


;; TODO this is duplicated in misc.  - resolve.
(defn update-metadata
  [metadata-str new-map-entry]
  (let [metadata (read-string metadata-str)]
    (str (conj metadata new-map-entry))))



(defn- status-from-measurement-errors
  "If 10% of measurements are invalid, device is broken."
  [sensor]
  (let [errors (-> sensor first :errors)
        events (-> sensor first :events)]
    (if (and (not (empty? errors))
             (not (empty? events)))
      (if (and (not (zero? errors))
               (not (zero? events))
               (> (/ errors events) 0.1))
        "Broken"
        "Ok"))
    "N/A"))

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
  (db/with-session [session (:hecuba-session store)]
    (let [errors (-> sensor first :errors)]
      (update-sensor session sensor {:errors (if-not (empty? errors) (inc errors) 1)})
      (assoc-in m [:metadata "median-spike"] "true"))))

(defn larger-than-median
  "Find readings that are larger than median."
  ([median measurement] (larger-than-median median measurement 200))
  ([median measurement n] (>= (-> measurement :value read-string) (* n median))))

(defn median-check
  "Checks if a measurement is 200x median. If so, increments error counter.
  Updates metadata accordingly."
  [m session sensor]
  (let [median (-> sensor first :median)]
    (cond
     (or (empty? median) (zero? median)) (assoc-in m [:metadata "median-spike"] "n/a")
     (larger-than-median median m) (label-spike session m)
     :else (assoc-in m [:metadata "median-spike"] "false"))))

(defn- label-invalid-value
  "Labels measurement as having invalid value.
   Increments error counter of the sensor."
  [session sensor where m]
  (let [errors (-> sensor first :errors)]
    (update-sensor session sensor {:errors (if-not (empty? errors) (inc errors) 1)})
    (assoc-in m [:metadata "is-number"] "false")))

(defn- number-check
  "Checks if value is a number. If not, increments error counter.
  Updates metadata accordingly."
  [m session sensor]
  (let [value     (:value m)]
    (if (and (not (empty? value)) (m/numbers-as-strings? value))
      (assoc-in m [:metadata "is-number"] "true")
      (label-invalid-value session sensor (where-from m) m))))

(defn- sensor-exists? [session m]
  (first (db/execute session
                  (hayt/select :sensors
                               (hayt/where (where-from m))))))

(defn validate
  "Measurement map is pipelines through a number of validation
  functions. Returns map of measurement with updated metadata."
  [store m]
  (db/with-session [session (:hecuba-session store)]
    (let [sensor (sensor-exists? session m)
          events (-> sensor first :events)
          where  (where-from m)]
      (when (= events 1440) (reset-counters! session where))
      (update-sensor session sensor {:events (if-not (empty? events) (inc events) 1)})
      (-> m
          (number-check session sensor)
          (median-check session sensor)
          (broken-sensor-check session sensor)))))

;; Sensor metadata functions that are triggered by core.async queue worker.
(defn- get-sensor-metadata [session where]
  (first (db/execute session
                     (hayt/select :sensor_metadata
                                  (hayt/where where)))))

(defn- update-date-range [t metadata column]
  (let [existing-range (get metadata column)]
    (cond
     (empty? existing-range) {"start" t "end" t}
     (.before t (get existing-range "start")) {"start" t} 
     (.after t (get existing-range "end")) {"end" t})))

(defn update-sensor-metadata
  "Updates start and end dates when new measurement is received."
  [m store]
  (db/with-session [session (:hecuba-session store)]
    (let [where     (where-from m)
          metadata  (get-sensor-metadata session where)
          t         (:timestamp m)]
      (db/execute session
                  (hayt/update :sensor_metadata
                               (hayt/set-columns {:rollups [+ (update-date-range t metadata :rollups)]
                                                  :mislabelled_sensors_check [+ (update-date-range t metadata :mislabelled_sensors_check)]
                                                  :difference_series [+ (update-date-range t metadata :difference_series)]
                                                  :median_calc_check [+ (update-date-range t metadata :median_calc_check)]
                                                  :spike_check [+ (update-date-range t metadata :spike_check)]})
                               (hayt/where where))))))
