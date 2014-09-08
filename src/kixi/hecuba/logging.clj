(ns kixi.hecuba.logging
  (:require [clojure.tools.logging :as log]))

(defn debugf-> [x msg]
  (log/infof msg x)
  x)

(defn debugf->> [msg xs]
  (log/infof msg (vec (take 10 xs)))
  xs)
