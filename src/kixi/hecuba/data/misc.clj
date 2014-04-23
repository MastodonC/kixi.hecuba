(ns kixi.hecuba.data.misc
  "Common functions"
  (:require [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [kixi.hecuba.protocols :refer (items item update!)]))

;;;;; Time conversion functions ;;;;;

(defn hourly-timestamp [t] 
  (tc/to-date (tf/unparse (tf/formatters :date-hour) (tc/from-date t))))
(defn daily-timestamp [t]
  (tc/to-date (tf/unparse (tf/formatters :date) (tc/from-date t))))

(defn add-hour [t] (java.util.Date. (+ (.getTime t) (* 60 60 1000))))
(defn add-day [t]  (java.util.Date. (+ (.getTime t) (* 24 60 60 1000))))


(def int-time-formatter (tf/formatter "yyyyMMddHHmmss"))
(defn int-format-to-timestamp [t] (tc/to-date (tf/parse int-time-formatter t)))

(def db-date-formatter (tf/formatter "EEE MMM dd HH:mm:ss z yyyy"))
(defn to-timestamp [t] (.parse (java.text.SimpleDateFormat. "EEE MMM dd HH:mm:ss z yyyy") t))

(defn get-year-partition-key [timestamp] (Long/parseLong (format "%4d" (t/year (tc/from-date timestamp)))))

;; Return int representation of month partition key
(defmulti get-month-partition-key type)
(defmethod get-month-partition-key java.util.Date [t]
  (let [timestamp (tc/from-date t)] (Long/parseLong (format "%4d%02d" (t/year timestamp) (t/month timestamp)))))
(defmethod get-month-partition-key org.joda.time.DateTime [t] (Long/parseLong (format "%4d%02d" (t/year t) (t/month t))))
(defmethod get-month-partition-key java.lang.String [t]
 (let [timestamp (tf/parse int-time-formatter t)] (Long/parseLong (format "%4d%02d" (t/year timestamp) (t/month timestamp)))))

;; Returns integer representation of last check timestamp. 
(defmulti last-check-int-format type)
(defmethod last-check-int-format org.joda.time.DateTime [t] (Long/parseLong (tf/unparse int-time-formatter t)))
(defmethod last-check-int-format java.lang.String [t] 
  (let [timestamp (tf/parse db-date-formatter t)] (Long/parseLong (tf/unparse int-time-formatter timestamp))))
(defmethod last-check-int-format java.util.Date [t]
  (let [timestamp (tc/from-date t)] (Long/parseLong (tf/unparse int-time-formatter timestamp))))


;;;;; Last check functions ;;;;;

(defn sensors-to-check
  "Finds sensors that needs to have data quality checked: with no check performed
  or with a check older than a week."
  [querier validation-type]
  (let [last-week        (Long/parseLong (tf/unparse int-time-formatter (t/minus (t/now) (t/weeks 1))))
        sensors-metadata (items querier :sensor-metadata)
        sensors          (filter #(or (= "" (validation-type %))
                                      (<= (Long/parseLong (validation-type %)) last-week)) sensors-metadata)]
    (map #(merge (first (items querier :sensor [[= :device-id (:device-id %)] [= :type (:type  %)]])) %) sensors)))

(defn all-sensors
  "Given a querier, retrieces all sensors data joined with their metadata."
  [querier]
  (let [all-sensors-metadata (items querier :sensor-metadata)]
    (map #(merge (first (items querier :sensor [[= :device-id (:device-id %)] [= :type (:type %)]])) %) all-sensors-metadata)))

(defn start-end-dates
  "Given a sensor, table and where clause, returns start and end dates for (re)calculations."
  [querier table column sensor where]
  (let [range      (-> sensor column)]
    (when-not (empty? range)
      {:start-date (:start (read-string range)) :end-date (:end (read-string range))})))


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
             (assoc-in [:value] (let [value (:value m)] (if-not (empty? value) (read-string value) nil)))
             (assoc-in [:metadata] (when-let [metadata (:metadata m)] (read-string metadata)))))
       m))

(defn find-broken-sensors
  "Finds sensors with bad metadata and label as broken.
  Returns sequence of maps."
  [querier]
  (let [where [= :status "Broken"]]
    (items querier :sensor where)))

(defn find-mislabelled-sensors
  "Finds sensots that are marked as mislabelled.
  Returns sequence of maps."
  [querier]
  (let [where [= :mislabelled "true"]]
    (items querier :sensor-metadata where)))

(defn reset-date-range
  "Given querier, commander, sensor, column and start/end dates, update these dates in sensor metadata."
  [querier commander {:keys [device-id type period]} col start-date end-date]
  (let [where               [[= :device-id device-id] [= :type type]]
        current-metadata    (first (items querier :sensor-metadata where))
        current-range       (-> current-metadata col read-string)
        current-start       (-> current-range :start (Long/parseLong))
        current-end         (-> current-range :end (Long/parseLong))]
    (when-not (and (< current-start (Long/parseLong start-date))
                   (> current-end (Long/parseLong end-date))) 
      (update! commander :sensor-metadata {col nil} where))))

(defn update-date-range [commander col where t existing-range]
  (cond
   (empty? existing-range) (update! commander :sensor-metadata {col (str {:start (str t) :end (str t)})} where)
   (< t (Long/parseLong (:start (read-string existing-range)))) (update! commander :sensor-metadata {col (update-metadata existing-range {:start (str t)})} where)
   (> t (Long/parseLong (:end (read-string existing-range)))) (update! commander :sensor-metadata {col (update-metadata existing-range {:end (str t)})} where)))
