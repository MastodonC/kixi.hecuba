(ns calculated-sample-data
 (:require [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.periodic :as p]))

(def pulse-one-minute-no-gaps
  [{:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 1 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 2 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 3 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 4 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 5 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 6 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 7 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 9 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 10 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 11 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 12 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 13 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 14 0) :type "electricityConsumption"}
   {:value 1 :device_id "adb" :timestamp (t/date-time 2014 1 1 0 15 0) :type "electricityConsumption"}])


(def pulse-one-minute-gaps
  [{:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 1 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 2 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 3 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 4 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 7 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 9 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 10 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 11 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 14 0) :type "electricityConsumption"}
   {:value 1 :device_id "adc" :timestamp (t/date-time 2014 1 1 0 15 0) :type "electricityConsumption"}])

(def pulse-two-minutes-no-gaps
  [{:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 2 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 4 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 6 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 10 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 12 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 14 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 16 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 18 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 20 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 22 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 24 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 26 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 28 0) :type "electricityConsumption"}
   {:value 1 :device_id "add" :timestamp (t/date-time 2014 1 1 0 30 0) :type "electricityConsumption"}])

(def pulse-two-minutes-gaps
  [{:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 6 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 10 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 12 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 20 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 22 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 24 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 26 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 28 0) :type "electricityConsumption"}
   {:value 1 :device_id "ade" :timestamp (t/date-time 2014 1 1 0 30 0) :type "electricityConsumption"}])


(def cumulative-one-minute-no-gaps
  [{:value 1 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 2 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 1 0) :type "electricityConsumption"}
   {:value 3 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 2 0) :type "electricityConsumption"}
   {:value 4 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 3 0) :type "electricityConsumption"}
   {:value 5 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 4 0) :type "electricityConsumption"}
   {:value 6 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 5 0) :type "electricityConsumption"}
   {:value 7 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 6 0) :type "electricityConsumption"}
   {:value 8 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 7 0) :type "electricityConsumption"}
   {:value 9 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 10 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 9 0) :type "electricityConsumption"}
   {:value 11 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 10 0) :type "electricityConsumption"}
   {:value 12 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 11 0) :type "electricityConsumption"}
   {:value 13 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 12 0) :type "electricityConsumption"}
   {:value 14 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 13 0) :type "electricityConsumption"}
   {:value 15 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 14 0) :type "electricityConsumption"}
   {:value 16 :device_id "adf" :timestamp (t/date-time 2014 1 1 0 15 0) :type "electricityConsumption"}])

(def cumulative-one-minute-gaps
  [{:value 1 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 2 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 1 0) :type "electricityConsumption"}
   {:value 6 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 5 0) :type "electricityConsumption"}
   {:value 7 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 6 0) :type "electricityConsumption"}
   {:value 8 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 7 0) :type "electricityConsumption"}
   {:value 9 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 12 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 11 0) :type "electricityConsumption"}
   {:value 13 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 12 0) :type "electricityConsumption"}
   {:value 14 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 13 0) :type "electricityConsumption"}
   {:value 15 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 14 0) :type "electricityConsumption"}
   {:value 16 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 15 0) :type "electricityConsumption"}])

(def cumulative-two-minutes-no-gaps
  [{:value 1 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 2 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 2 0) :type "electricityConsumption"}
   {:value 3 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 4 0) :type "electricityConsumption"}
   {:value 4 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 6 0) :type "electricityConsumption"}
   {:value 5 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 6 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 10 0) :type "electricityConsumption"}
   {:value 7 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 12 0) :type "electricityConsumption"}
   {:value 8 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 14 0) :type "electricityConsumption"}
   {:value 9 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 16 0) :type "electricityConsumption"}
   {:value 10 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 18 0) :type "electricityConsumption"}
   {:value 11 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 20 0) :type "electricityConsumption"}
   {:value 12 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 22 0) :type "electricityConsumption"}
   {:value 13 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 24 0) :type "electricityConsumption"}
   {:value 14 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 26 0) :type "electricityConsumption"}
   {:value 15 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 28 0) :type "electricityConsumption"}
   {:value 16 :device_id "adg" :timestamp (t/date-time 2014 1 1 0 30 0) :type "electricityConsumption"}])

(def cumulative-two-minutes-gaps
  [{:value 1 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 0 0) :type "electricityConsumption"}
   {:value 2 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 2 0) :type "electricityConsumption"}
   {:value 3 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 4 0) :type "electricityConsumption"}
   {:value 4 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 6 0) :type "electricityConsumption"}
   {:value 5 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 8 0) :type "electricityConsumption"}
   {:value 9 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 16 0) :type "electricityConsumption"}
   {:value 10 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 18 0) :type "electricityConsumption"}
   {:value 11 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 20 0) :type "electricityConsumption"}
   {:value 12 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 22 0) :type "electricityConsumption"}
   {:value 13 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 24 0) :type "electricityConsumption"}
   {:value 16 :device_id "adh" :timestamp (t/date-time 2014 1 1 0 30 0) :type "electricityConsumption"}])

(defn- timestamp-seq-inclusive [period start end]
  (take-while #(not (t/after? % end)) (p/periodic-seq start period)))

(defn generate-difference-series [start end measurements period]
  (let [expected-timestamps (map (fn [t] (hash-map :timestamp t)) 
                                 (timestamp-seq-inclusive period start end)) 
        grouped-readings    (into {} (map #(vector (:timestamp %) %) measurements))
        template-reading    (-> (first measurements) 
                                (assoc :value 0)
                                (dissoc :timestamp))]
    (map #(merge template-reading (get grouped-readings (:timestamp  %) %)) 
         expected-timestamps)))

(defn alter-resolution [start end measurements period]
  ;; 1. create intervals - get timestamps for given period
  ;; 2. get measurements for each interval
  ;; 3. do something with those measurements
  (let [expected-timestamps (map #(hash-map :timestamp %) (timestamp-seq-inclusive period start end))]
    (map (fn [t1 t2]
           (let [interval (filter #(t/within? (t/interval (:timestamp t1) (:timestamp t2)) (:timestamp %))
                                  measurements)]))
         expected-timestamps (rest expected-timestamps))))
