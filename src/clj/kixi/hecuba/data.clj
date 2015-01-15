(ns kixi.hecuba.data
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [clojure.edn :as edn]))

;; Some utility funcitons

(defn parse-list
  "Add a json list to a map with the given key if the list exists,
  otherwise return the map unchanged."
  [m key]
  (if-let [items (get m key)]
    (assoc m key (mapv #(json/parse-string % keyword) items))
    m))

(defn parse-item
  "Add a json object to a map with the given key if the list exists,
  otherwise return the map unchanged."
  [m key]
  (if-let [item (get m key)]
    (try
      (assoc m key (json/parse-string item keyword))
      (catch Throwable t
        (log/error "Unparsable string: " item)
        m))
    m))

(defn assoc-if [m k v]
  (if v
    (assoc m k v)
    m))

(defn assoc-encode-item-if [m k v]
  (if v
    (assoc m k (json/encode v))
    m))

(defn assoc-encode-list-if [m k v]
  (if (seq v)
    (assoc m k (mapv json/encode v))
    m))

(defn assoc-encode-list-update-if [m k v]
  (if (seq v)
    (assoc m k [+ (json/encode v)])))

(defn map-longest
  [f default & colls]
  (lazy-seq
    (when (some seq colls)
      (cons
        (apply f (map #(if (seq %) (first %) default) colls))
        (apply map-longest f default (map rest colls))))))

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defn numbers-as-strings? [& strings]
  (every? #(re-find #"^-?\d+(?:\.\d+)?$" %) strings))

(defn where-from
  "Takes measurement or sensor and returns where clause"
  [m]
  [[= :device_id (:device_id m)] [= :sensor_id (:sensor_id m)]])
