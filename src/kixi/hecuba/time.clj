(ns kixi.hecuba.time
  (:require [clj-time.core     :as t]
            [clj-time.coerce   :as tc]
            [clj-time.periodic :as tp]
            [clj-time.format   :as tf]))


(defn hourly-timestamp [t]
  (tc/to-date (tf/unparse (tf/formatters :date-hour) (tc/from-date t))))
(defn daily-timestamp [t]
  (tc/to-date (tf/unparse (tf/formatters :date) (tc/from-date t))))

(defn get-year-partition-key [timestamp] (Long/parseLong (format "%4d" (t/year timestamp))))

(defmulti get-month-partition-key type)
(defmethod get-month-partition-key java.util.Date [t]
  (let [timestamp (tc/from-date t)] (Long/parseLong (format "%4d%02d" (t/year timestamp) (t/month timestamp)))))
(defmethod get-month-partition-key org.joda.time.DateTime [t] (Long/parseLong (format "%4d%02d" (t/year t) (t/month t))))

(defmulti truncate-seconds type)
(defmethod truncate-seconds java.util.Date [t]
  (let [time-str (tf/unparse (tf/formatter "yyyy-MM-dd'T'HH:mm") (tc/from-date t))]
    (tc/to-date  (tf/parse (tf/formatter "yyyy-MM-dd'T'HH:mm") time-str))))
(defmethod truncate-seconds org.joda.time.DateTime [t]
  (let [time-str (tf/unparse (tf/formatter "yyyy-MM-dd'T'HH:mm") t)]
    (tf/parse (tf/formatter "yyyy-MM-dd'T'HH:mm") time-str)))

(defn dates-overlap?
  "Takes a start/end range from sensor_metadata \"dirty dates\" and a period,
  and returns range to calculate if it overlaps. Otherwise it returns nil."
  [{:keys [start-date end-date]} period]
  (let [end   (t/now)
        start (t/minus end period)]
    (when (t/overlaps? (t/interval start end) (t/interval start-date end-date))
      {:start-date start :end-date end})))

(defn time-range
  "Return a lazy sequence of DateTime's from start to end, incremented
  by 'step' units of time."
  [start end step]
  (let [start-date (t/first-day-of-the-month start)
        end-date   (t/last-day-of-the-month end)
        in-range-inclusive? (complement (fn [t] (t/after? t end-date)))]
    (take-while in-range-inclusive? (tp/periodic-seq start-date step))))

(defn range->months [start-date end-date]
  (->> (time-range start-date end-date (t/months 1))
       (map #(get-month-partition-key (tc/to-date %)))))
