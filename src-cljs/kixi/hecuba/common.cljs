(ns kixi.hecuba.common
  (:require [clojure.string   :as str]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [goog.userAgent :as agent]))

(when (not agent/IE)
  (enable-console-print!))

(defn find-first [pred coll]
  (first (filter pred coll)))

(defn find-first-index [pred coll]
  (first (keep-indexed #(when (pred %2) %1) coll)))

(defn index-of
  "Find index of the first occurrence of v in s"
  [v s]
  (find-first-index #(= v %) s))

(defn map-replace
  "Take a string template containing :keyword elements and replace those keywords
   with the value corresponding to the looking of that keyword in the supplied m

   e.g.

   (map-replace \"/foo/:k1/bar/:k2\" {:k1 \"10\" :k2 \"20\"})
   ;=> \"/foo/10/bar/20\""
  [template m]
  (let [s (reduce-kv
           (fn [ret k v]
             (str/replace ret
                          (str k)
                          v))
           template m)]
    (when (not (index-of \: s)) s)))

(defn interval
  [start-date end-date]
  (let [formatter (tf/formatter "yyyy-MM-dd HH:mm:ss")
        start     (tf/parse formatter start-date)
        end       (tf/parse formatter end-date)
        interval  (t/in-minutes (t/interval start end))]
    (cond
     (<= interval 1440) :raw
     (and (> interval 1440) (< interval 20160)) :hourly_rollups
     (>= interval 20160) :daily_rollups)))

(defn unparse-date [timestamp f]
  (when-not (nil? timestamp)
    (let [date (tc/from-date timestamp)]
      (tf/unparse (tf/formatter f) date))))

(defn unparse-date-str [timestamp f]
  (when-not (nil? timestamp)   
    (let [parsed (tf/parse (tf/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ") timestamp)
          date   (tc/to-date parsed)]
      (unparse-date date f))))

(defn log [& msgs]
  (when (or (and agent/GECKO
                 (agent/isVersionOrHigher 30))
            (and agent/WEBKIT
                 (agent/isVersionOrHigher 537)))
    (apply println msgs)))
