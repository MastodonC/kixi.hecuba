(ns kixi.hecuba.data.validate
 "Data quality assurance and validation."
 (:require [clj-time.core :as t]
           [clj-time.coerce :as tc]
           [kixi.hecuba.data.misc :as m]
           [kixi.hecuba.storage.db :as db]
           [qbits.hayt :as hayt]))

(defn larger-than-median
  "Find readings that are larger than median."
  ([median measurement] (larger-than-median median measurement 200))
  ([median measurement n] (>= (-> measurement :value read-string) (* n median))))

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

(defn- sensor-exists? [session m]
  (first (db/execute session
                  (hayt/select :sensors
                               (hayt/where (m/where-from m))))))

(defn validate
  "Measurement map is pipelines through a number of validation
  functions. Returns map of measurement with updated metadata."
  [m sensor]
  (-> m
      number-check
      (median-check sensor)))

(defn- update-date-range [sensor column min-date max-date]
  (let [existing-range (get sensor column)]
    (cond
     (empty? existing-range) {"start" min-date "end" max-date}
     (.before min-date (get existing-range "start")) {"start" min-date} 
     (.after max-date (get existing-range "end")) {"end" max-date})))

(defn- update-bounds [min-date max-date {:keys [lower_ts upper_ts]}]
  (cond
   (and (nil? lower_ts) (nil? upper_ts)) {:upper_ts max-date :lower_ts min-date}
   (.before min-date lower_ts) {:lower_ts min-date}
   (.after max-date upper_ts) {:upper_ts max-date}))

(defn update-sensor-metadata
  "Updates start and end dates when new measurement is received."
  [store sensor min-date max-date]
  (db/with-session [session (:hecuba-session store)]
    (let [start      (tc/to-date min-date)
          end        (tc/to-date max-date)
          new-bounds (update-bounds start end sensor)
          where      (m/where-from sensor)]
      (db/execute session
                  (hayt/update :sensor_metadata
                               (hayt/set-columns (merge  {:rollups [+ (update-date-range sensor :rollups start end)]
                                                          :mislabelled_sensors_check [+ (update-date-range sensor :mislabelled_sensors_check start end)]
                                                          :difference_series [+ (update-date-range sensor :difference_series start end)]
                                                          :median_calc_check [+ (update-date-range sensor :median_calc_check start end)]
                                                          :spike_check [+ (update-date-range sensor :spike_check start end)]
                                                          :co2 [+ (update-date-range sensor :co2 start end)]
                                                          :kwh [+ (update-date-range sensor :kwh start end)]}
                                                         (when-let [lower (:lower_ts new-bounds)] {:lower_ts lower})
                                                         (when-let [upper (:upper_ts new-bounds)] {:upper_ts upper})))
                               (hayt/where where))))))
