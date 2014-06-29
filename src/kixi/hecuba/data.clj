(ns kixi.hecuba.data
  (:require [cheshire.core :as json]))

;; Some utility funcitons

(defn parse-list [m key]
  (if-let [items (get m key)]
    (assoc m key (mapv #(json/parse-string % keyword) items))
    m))

(defn parse-item [m key]
  (if-let [item (get m key)]
    (assoc m key (json/parse-string item keyword))
    m))


