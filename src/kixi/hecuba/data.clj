(ns kixi.hecuba.data
  "Component that reads messages off the queue
  and performs appropriate validation/updates metadata."
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [clojure.core.async :refer [<! >! chan put! sliding-buffer close! go]]
            [kixi.hecuba.data.validate :as v]))

(defn measurements-trigger [q store]
  (go (loop [msg (<! q)]
        (v/update-sensor-metadata msg store)
        (recur (<! q)))))

(defn configure-triggers [queue store]
  (let [measurements-q (get-in queue ["measurements"])]
    (measurements-trigger measurements-q store)))

(defrecord QueueWorker []
  component/Lifecycle
  (start [this]
    (log/info "QueueWorker starting")
    (let [queue (get-in this [:queue :queue])
          store (:store-new this)]
      (configure-triggers queue store)
      this))
  (stop [this]
    (log/info "QueueWorker stopping")
    this))

(defn new-queue-worker []
  (->QueueWorker))
