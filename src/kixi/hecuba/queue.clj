(ns kixi.hecuba.queue
  "Messaging queue."
  (:require [com.stuartsierra.component :as component]
            [clojure.core.async :refer [<! >! chan put! sliding-buffer close! go]]))

(defn put-on-queue [q message]
  (go (>! q message)))

(defn create-channels [config]
  (let [topics (:topics config)]
    (into {} (map (fn [topic] {topic (chan (sliding-buffer 10))}) topics))))

(defrecord Queue [config]
  component/Lifecycle
  (start [this]
    (assoc this :queue (create-channels config)))
  (stop [this]
    this))

(defn new-queue [config]
  (->Queue config))
