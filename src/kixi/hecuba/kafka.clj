(ns kixi.hecuba.kafka
  (:require
   jig
   [clj-kafka.producer    :as producer]
   [clj-kafka.zk          :as zk]
   [clj-kafka.core        :as kcore]
   [clj-kafka.consumer.zk :as consumer])
  (:import (jig Lifecycle)))

(def producer-config {"metadata.broker.list" ""
                      "serializer.class" "kafka.serializer.DefaultEncoder"
                      "Partitioner.Class" "kafka.producer.DefaultPartitioner"})
(def consumer-config {"zookeeper.connect" ""
                      "group.id" "clj-kafka.consumer"
                      "auto.offset.reset" "smallest"
                      "auto.commit.enable" "true"})

(defn string-value
  [m]
  (String. (:value m)))

(defn create-msg
 [str]
 (producer/message "test" (.getBytes str)))

(defn send-msg 
  [message]
  (let [p (producer/producer producer-config)]
    (producer/send-message p (create-msg message))))

(defn receive
  []
  (kcore/with-resource [c (consumer/consumer consumer-config)]
    consumer/shutdown
    (doall (take 10 (consumer/messages c ["test"])))))

(deftype Kafka [config]
  Lifecycle
  (init [_ system]
    (assoc system :producer (atom {}) :consumer (atom {})))
  (start [_ system]
    (println "Starting Kafka Session")
    (assoc producer-config "metadata.broker.list" (:producer config))
    (assoc consumer-config  "zookeeper.connect" (:consumer config)))
  (stop [_ system] system))
