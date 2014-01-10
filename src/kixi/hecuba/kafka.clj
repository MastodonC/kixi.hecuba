(ns kixi.hecuba.kafka
  (:require
   jig

   [clj-kafka.producer    :as producer]
   [clj-kafka.zk          :as zk]
   [clj-kafka.core        :as kcore]
   [clj-kafka.consumer.zk :as consumer])
  (:import
   (jig Lifecycle)
   (kixi.hecuba.protocols Commander)))

(defn string-value
  [m]
  (String. (:value m)))

(defn create-msg
 [msg topic]
 (producer/message topic (.getBytes (str msg))))

(defn send-msg
  [message topic producer-config]
  (let [p (producer/producer producer-config)]
    (producer/send-message p (create-msg message topic))))

(defn create-kafka-connections
  [system config]
  (update-in system [(:jig/id config) ::producer-config] conj (:producer config)))

(deftype KafkaCommander [producer-config]
  Commander
  (upsert! [_ payload]
    (send-msg payload)
    )
  )

(deftype Kafka [config]
  Lifecycle
  (init [_ system]
    system)
  (start [_ system]
    (create-kafka-connections system config))
  (stop [_ system] system))
