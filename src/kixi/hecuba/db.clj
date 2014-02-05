(ns kixi.hecuba.db
  "Database connectivity."
  (:require [clojurewerkz.cassaforte.client :as client])
  (:use clojurewerkz.cassaforte.cql
        clojurewerkz.cassaforte.query)
  (:import
   (jig Lifecycle)
   (kixi.hecuba.protocols Commander)))

(defn create-db-session
  "Connects to a cluster, creates a session and binds it to system."
  [config]
  (let [host        (:hosts config)
        ks          (:keyspace config)
        port        (:port config)
        credentials (:credentials config)
        cluster     (client/build-cluster
                     {:contact-points host
                      :port port
                      :credentials credentials})]
    (client/connect cluster ks)))

(defn execute-query
  "Executes raw CQL query."
  [config query]
  (let [session (create-db-session config)]
    (client/execute session query)
   ; (client/disconnect!)
    ))

(deftype Database [config]
  Lifecycle
  (init [_ system]
    system)
  (start [_ system]
    (update-in system [(:jig/id config) ::cluster] (constantly (select-keys config [:hosts :keyspace :port :credentials]))))
  (stop [_ system]
    system))
