(ns kixi.hecuba.db
  "Database connectivity."
  (:require [clojurewerkz.cassaforte.client :as client]
            [clojurewerkz.cassaforte.cql    :as cql]
            [clojurewerkz.cassaforte.query  :as query]))

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
  [session query]
  (client/execute session query))

(deftype Database [config]
  Lifecycle
  (init [_ system]
    system)
  (start [_ system]
    (let [session (create-db-session config)]
      (update-in system [(:jig/id config) ::db-session] (constantly session))))
  (stop [_ system]
    system))

(defn select-measurements
  "Retrieves measurements for specified device, sensor and month.
   Returns vector of maps."
  [session device-id sensor-type month]
  (cql/select session "measurements" (query/where :device_id device-id
                                                  :type sensor-type
                                                  :month month)))

(defn get-counter
  "Returns value of a specified counter (number)."
  [session device-id type counter]
  (binding [client/*default-session* session]
    (first (cql/select "sensors" (query/columns counter) (query/where :device_id device-id
                                                                      :type type)))))

(defn update-counter
  "Increments event counter"
  [session device-id type counter value]
  (binding [client/*default-session* session]
    (cql/update "sensors" {counter (int value)}
                (query/where :device_id device-id
                             :type type))))

(defn get-sensor-status
  "Returns device's status."
  [session type device-id]
  (binding [client/*default-session* session]
    (first (cql/select "sensors" (query/columns :status) (query/where :device_id device-id
                                                                     :type type)))))

(defn update-sensor-status
  "Update status in device's metadata."
  [session device-id type status]
  (binding [client/*default-session* session]
    (cql/update "sensors" {:status status}
                (query/where :device_id device-id
                             :type type))))
