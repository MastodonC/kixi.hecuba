(ns kixi.hecuba.data.validate
 "Data quality assurance and validation."
 (:require [clj-time.core :as t]
           [clj-time.coerce :as tc]
           [kixi.hecuba.data.misc :as m]
           [kixi.hecuba.storage.db :as db]
           [clojure.tools.logging :as log]
           [qbits.hayt :as hayt]
           [clojure.edn :as edn]))

(defn larger-than-median
  "Find readings that are larger than median."
  ([median measurement] (larger-than-median median measurement 200))
  ([median measurement n] (>= (-> measurement :value edn/read-string) (* n median))))

(defn median-check
  "Checks if a measurement is 200x median and updates metadata accordingly."
  [m sensor]
  (let [median (-> sensor first :median)]
    (cond
     (or (empty? median) (zero? median)) (assoc-in m [:reading_metadata "median-spike"] "n/a")
     (larger-than-median median m) (assoc-in m [:reading_metadata "median-spike"] "true")
     :else (assoc-in m [:reading_metadata "median-spike"] "false"))))

(defn- number-check
  "Checks if value is a number and updates metadata accordingly."
  [m]
  (let [value (:value m)]
    (assoc-in m [:reading_metadata "is-number"] (if (and (not (empty? value)) (m/numbers-as-strings? value)) "true" "false"))))

(defn validate
  "Measurement map is pipelines through a number of validation
  functions. Returns map of measurement with updated metadata."
  [m sensor]
  (-> m
      number-check
      (median-check sensor)))
