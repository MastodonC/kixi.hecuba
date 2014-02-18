(ns kixi.hecuba.data.validate
 "Data quality assurance and validation."
 (:require [clj-time.core :as t]   
           [clj-time.format :as tf]
           [com.stuartsierra.frequencies :as freq]
           [clojure.data.json :as json]
           [kixi.hecuba.db :as db]))

;;; Helper functions ;;;

(def custom-formatter (tf/formatter "yyyy-MM-dd HH:mm:ss"))

(defn transform-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m]
         (assoc-in m [:value] (read-string (:value m))))
       measurements))


;;; Data validation ;;;

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

(defn median-stream
  "Frugal way to find the median of a stream.
  Assumes that measurement values are numbers
  and are valid."
  [measurements]
  (reduce (fn [n m] (cond (> (:value m) n) (inc n)
                          (< (:value m) n) (dec n)
                          :else n))
          0 measurements))

(defn median
  "Find median of a lazy sequency of measurements.
  Assumes measurement values are numbers and are valid."
  [measurements]
  (freq/median (frequencies (map :value measurements))))

(defn larger-than-median
  "Find readings that are larger than median."
  ([median measurements] (larger-than-median median measurements (* 200 median)))
  ([median measurements n] (filter (fn [m] (>= (:value m) n)) measurements)))

;; TODO Measurements will be converted to maps by this point. Invalid ones should already have
;; error set to true.
(defn is-errored?
  [measurement]
  (= "true" (:error measurement)))

(defn is-broken?
  "If 10% of measurements are invalid, device is broken."
  [errors events]
  (when (and (not (zero? errors)) (not (zero? events)))
    (> (/ events errors) 0.1)))

(defn validate-measurement
  "Called before inserting the measurement. Increments appropriate counters"
  [session measurement]
  (let [device-id (:device_id measurement)
        type      (:type measurement)
        errors    (:errors (db/get-counter session device-id type :errors))
        events    (:events (db/get-counter session device-id type :events))]
    (db/update-counter session device-id type :events (inc events))
    (when (is-errored? measurement)
      (db/update-counter session device-id type :errors (inc errors)))
    (when (is-broken? errors events)
      (db/update-sensor-status session device-id type "broken"))))




