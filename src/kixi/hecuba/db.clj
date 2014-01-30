(ns kixi.hecuba.db
  "Database connectivity."
  (:require [clojurewerkz.cassaforte.client :as client])
  (:use clojurewerkz.cassaforte.cql
        clojurewerkz.cassaforte.query)
  (:import
   (jig Lifecycle)
   (kixi.hecuba.protocols Commander)))


(defn execute-query
  "Executes raw CQL query."
  [session query]
  (client/execute session query))

(defn create-db-session
  "Connects to a cluster, creates a session and binds it to system."
  [system config]
  (let [host        (:hosts config)
        ks          (:keyspace config)
        port        (:port config)
        credentials (:credentials config)
        cluster     (client/build-cluster
                     {:contact-points host
                      :port port
                      :credentials credentials})]
    (client/connect cluster ks)))

(deftype Database [config]
  Lifecycle
  (init [_ system]
    system)
  (start [_ system]
    (let [session (create-db-session system config)]  
      (update-in system [(:jig/id config) ::db-session] (constantly session))))
  (stop [_ system]
    system))
