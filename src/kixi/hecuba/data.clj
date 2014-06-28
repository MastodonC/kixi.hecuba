(ns kixi.hecuba.data
  (:require [cheshire.core :as json]))

;; Some utility funcitons

(defn parse-list [profile key]
  (if-let [items (get profile key)]
    (assoc profile key (mapv #(json/parse-string % keyword) items))
    profile))

(defn parse-item [profile key]
  (if-let [item (get profile key)]
    (assoc profile key (json/parse-string item keyword))
    profile))


