(ns kixi.hecuba.data
  "Component that reads messages off the queue
  and performs appropriate validation/updates metadata."
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [<! >! chan put! sliding-buffer close! go]]
            [kixi.hecuba.data.validate :as v]))

(defn measurements-trigger [q commander querier]
  (go (loop [msg (<! q)]
        (v/update-sensor-metadata msg commander querier)
        (recur (<! q)))))

(defn configure-triggers [queue commander querier]
  (let [measurements-q (get-in queue ["measurements"])]
    (measurements-trigger measurements-q commander querier)))

(defrecord QueueWorker []
  component/Lifecycle
  (start [this]
    (log/info "QueueWorker starting")
    (let [queue     (get-in this [:queue :queue])
          commander (get-in this [:store :commander])
          querier   (get-in this [:store :querier])]
      (configure-triggers queue commander querier)
      this))
  (stop [this]
    (log/info "QueueWorker stopping")
    this))

(defn new-queue-worker []
  (->QueueWorker))
