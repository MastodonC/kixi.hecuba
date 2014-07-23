(ns kixi.hecuba.data
  (:require [cheshire.core :as json]
            [clojure.tools.logging :as log]))

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


