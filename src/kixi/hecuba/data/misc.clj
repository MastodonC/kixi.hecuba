(ns kixi.hecuba.data.misc
  "Common functions"
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [kixi.hecuba.protocols :refer (items)]))

;;;;; Time conversion functions ;;;;;

(def db-date-formatter
  (tf/formatter "EEE MMM dd HH:mm:ss z yyyy"))

(def int-time-formatter (tf/formatter "yyyyMMdd"))

(defn to-timestamp
  "Cassaforte returns timestamps as strings. This is not convert them back to java.util.Date."
  [t]
  (.parse (java.text.SimpleDateFormat. "EEE MMM dd HH:mm:ss z yyyy") t)
  ;(tc/to-date (tf/parse db-date-formatter t))
  )

(defmulti get-month-partition-key
  "Returns integer representation of a month part of timestamp." type)
(defmethod get-month-partition-key java.util.Date
  [t] (let [timestamp (tc/from-date t)](Integer/parseInt (format "%4d%02d" (t/year timestamp) (t/month timestamp)))))
(defmethod get-month-partition-key org.joda.time.DateTime
  [t](Integer/parseInt (format "%4d%02d" (t/year t) (t/month t))))
(defmethod get-month-partition-key java.lang.String
  [t](let [timestamp (tf/parse int-time-formatter t)](Integer/parseInt (format "%4d%02d" (t/year timestamp) (t/month timestamp)))))

(defmulti last-check-int-format "Returns integer representation of last check timestamp." type)
(defmethod last-check-int-format org.joda.time.DateTime [t] (Integer/parseInt (tf/unparse int-time-formatter t)))
(defmethod last-check-int-format java.lang.String [t] 
  (let [timestamp (tf/parse db-date-formatter t)]
    (Integer/parseInt (tf/unparse int-time-formatter timestamp))))


;;;;; Last check functions ;;;;;

(defn last-check-where-clause
  "Creates a where clause map that retrieves all measurements
  since the last check. Cassaforte returns all fields as strings, null values included -
  hence null last check is an empty string."
  [device-id type last-check]
  (conj {:device-id device-id :type type}
         (if (= last-check "")
           {:month [<= (get-month-partition-key (t/now))]}
           {:month (get-month-partition-key last-check) :timestamp [>= last-check]})))

(defn sensors-to-check
  "Finds sensors that needs to have data quality checked: with no check performed
  or with a check older than a week."
  [querier validation-type]
  (let [last-week        (Integer/parseInt (tf/unparse int-time-formatter (t/minus (t/now) (t/weeks 1))))
        sensors-metadata (items querier :sensor-metadata)
        sensors          (filter #(or (= "" (validation-type %))
                                      (<= (Integer/parseInt (validation-type %)) last-week)) sensors-metadata)]
    (map #(merge (first (items querier :sensor {:device-id (:device-id %) :type (:type  %)})) %) sensors)))


;;;;; Parsing of measurements ;;;;;

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

(defn sort-measurments
  [m]
  (sort-by :timestamp m) m)

(defn cassandraify-measurement
  "Converts values into formats expected by the measurements table."
  [m]
  (-> m
      (assoc-in [:month] (read-string (:month m)))
      (assoc-in [:timestamp] (to-timestamp (:timestamp m)))
      (assoc-in [:metadata] (str (:metadata m)))
      (assoc-in [:value] (str (:value m)))))

(defn decassandraify-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [m]
  (map (fn [m]
         (-> m
             (assoc-in [:value] (when-let [value (:value m)] (read-string value)))
             (assoc-in [:metadata] (when-let [metadata (:metadata m)] (read-string metadata)))))
       m))

(defn find-broken-sensors
  "Finds sensors with bad metadata and label as broken.
  Returns sequence of maps."
  [querier]
  (let [where {:status "Broken"}]
    (items querier :sensor where)))

(defn find-mislabelled-sensors
  "Finds sensots that are marked as mislabelled.
  Returns sequence of maps."
  [querier]
  (let [where {:mislabelled "true"}]
    (items querier :sensor-metadata where)))
