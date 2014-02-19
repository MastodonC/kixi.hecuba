(ns kixi.hecuba.data.validate
 "Data quality assurance and validation."
 (:require [clj-time.core :as t]   
           [clj-time.format :as tf]
           [com.stuartsierra.frequencies :as freq]
           [clojure.data.json :as json]
           [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
           [kixi.hecuba.db :as db]))

;;; Helper functions ;;;

(def custom-formatter (tf/formatter "yyyy-MM-dd HH:mm:ss"))

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
  [value median]
  (cond (> value median) (inc median)
        (< value median) (dec median)
        :else median))

(defn median
  "Find median of a lazy sequency of measurements.
  Assumes measurement values are numbers and are valid."
  [measurements]
  (freq/median (frequencies (map :value measurements))))

(defn larger-than-median?
  "Find readings that are larger than median."
  ([median value] (larger-than-median? median value 200))
  ([median value n] (>= value (* n median))))

(defn update-median
  "Updates median for a given sensor."
  [querier commander sensor measurement]
  (let [device-id    (-> sensor first :device-id)
        type         (-> sensor first :type)
        old-median   (-> sensor first :median read-string)
        new-median   (median-stream measurement old-median)]
    (when-not (= old-median new-median)
      (update! commander :sensor :median new-median {:device-id device-id :type type}))
    new-median))

(defn is-errored?
  [measurement median events]
  (let [value (-> measurement :value read-string)
        error (-> measurement :error)]
    (or
     (= "true" error)
     (not (number? value))
     (larger-than-median? median value))))

(defn is-broken?
  "If 10% of measurements are invalid, device is broken."
  [errors events]
  (when (and (not (zero? errors)) (not (zero? events)))
    (> (/ events errors) 0.1)))

(defn reset-counters!
  "Reset the counters and mark as ok."
  [querier commander device-id type events]
  (update! commander :sensor :events 0 {:device-id device-id :type type})
  (update! commander :sensor :errors 0 {:device-id device-id :type type}))

(defn validate-measurement
  "Called before inserting the measurement. Increments appropriate counters"
  [querier commander measurement]
  (let [device-id (:device-id measurement)
        type      (:type measurement)
        sensor    (items querier :sensor {:device-id device-id :type type})
        errors    (-> sensor first :errors read-string)
        events    (-> sensor first :events read-string)
        median    (update-median querier commander sensor (-> measurement :value read-string))]
    (do
      (when (= 1440 events)
        (reset-counters! querier commander device-id type events))
      (update! commander :sensor :events (inc events) {:device-id device-id :type type})
      (when (is-errored? measurement median events)
        (update! commander :sensor :errors (inc errors) {:device-id device-id :type type}))
      (if (is-broken? errors events)
        (update! commander :sensor :status "Broken" {:device-id device-id :type type})
        (update! commander :sensor :status "OK" {:device-id device-id :type type})))))




