(ns kixi.hecuba.cassandra
  (:require [clojurewerkz.cassaforte.client :as client])
  (:use clojurewerkz.cassaforte.cql
        clojurewerkz.cassaforte.query)
  (:import
   (jig Lifecycle)
   (kixi.hecuba.protocols Commander)))


(defn create-cassandra-connections
  [system config]
  (let [session (client/connect (client/build-cluster
                                 {:contact-points (:nodes (:cassandra-config config))
                                  :port (:port (:cassandra-config config))
                                  :credentials (:credentials (:cassandra-config config))}))]
  (update-in system [(:jig/id config) ::cluster-session] (constantly session))))

(deftype Cassandra [config]
  Lifecycle
  (init [_ system]
    system)
  (start [_ system]
    (create-cassandra-connections system config))
  (stop [_ system]
    system))
