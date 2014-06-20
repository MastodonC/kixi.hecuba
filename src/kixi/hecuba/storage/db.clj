(ns kixi.hecuba.storage.db
  "Cassandra cluster, session and queries."
  (:require [qbits.alia :as alia]
            [qbits.hayt :as hayt]
            [qbits.alia.codec.joda-time] ; necessary to get the codec installed.
            [kixi.hecuba.hash :refer (sha1)]
            [kixi.hecuba.protocols :as hecuba]
            [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]))

(require 'qbits.alia.codec.joda-time)

(extend-protocol qbits.hayt.cql/CQLEntities
   org.joda.time.ReadableInstant
   (cql-value [x]
     (.getMillis x)))

(defrecord Cluster [opts]
  component/Lifecycle
  (start [this]
    (assoc this :instance (alia/cluster opts)))
  (stop [this]
    (when-let [instance (:instance this)]
      (alia/shutdown instance)
      this)))

(def ClusterDefaults
  {:contact-points ["127.0.0.1"]
   :port 9042})

(defn new-cluster [opts]
  (->Cluster (merge ClusterDefaults opts)))

(defn ->raw-with-logging
  "Log all CQL - because it's always useful.
   This logs the raw CQL to a special kixi.hecuba.storage.db.CQL logger"
  [x]
  (let [raw-cql (hayt/->raw x)]
    (log/log 'kixi.hecuba.storage.db.CQL :debug  nil raw-cql)
    raw-cql))

(defrecord CassandraSession [opts]
  component/Lifecycle
  (start [this]
    (assoc this :session
           (alia/connect (get-in this [:cluster :instance])
                         (:keyspace opts))))
  (stop [this]
    (when-let [session (:session this)]
      (alia/shutdown session)
      this))
  hecuba/Cassandra
  (hecuba/-execute [this query opts]
    (log/infof "Trying query: %s with opts: %s" (hayt/->raw query) opts)
    (try
      (alia/execute (:session this) (->raw-with-logging query) opts)
      (catch Throwable t (log/errorf t "Query execution failed: %s opts: %s" (hayt/->raw query) opts)
             (throw t))))
  (hecuba/-execute-async [this query opts]
    (try
      (alia/execute-async (:session this) (->raw-with-logging query) opts)
      (catch Throwable t (log/errorf t "Query async execution failed: %s opts: %s" (hayt/->raw query) opts)
             (throw t))))
  (hecuba/-execute-chan [this query opts]
    (try
      (alia/execute-chan (:session this) (->raw-with-logging query) opts)
      (catch Throwable t
        (log/errorf t "Query channel execution failed: %s opts: %s" (hayt/->raw query) opts)
        (throw t)))))

(defrecord NewStore []
  component/Lifecycle
  (start [this] this)
  (stop [this] this))

(defn execute [session query & [opts]]
  (hecuba/-execute session query opts))
(defn execute-async [session query & [opts]]
  (hecuba/-execute-async session query opts))
(defn execute-chan [session query & [opts]]
  (hecuba/-execute-chan session query opts))

;; copied from clojure.core
(defmacro ^{:private true} assert-args
  [& pairs]
  `(do (when-not ~(first pairs)
         (throw (IllegalArgumentException.
                  (str (first ~'&form) " requires " ~(second pairs) " in " ~'*ns* ":" (:line (meta ~'&form))))))
     ~(let [more (nnext pairs)]
        (when more
          (list* `assert-args more)))))

(defmacro with-session
   "
   Example usage:
   ```
      (with-session [session keyspace1-session]
          (execute (hayt/select :sensor
                                (hayt/where [= [:id 1]]))))
   ```"
   [binding & body]
   (assert-args
    (vector? binding) "a vector for binding"
    (= 2 (count binding)) "exactly 2 forms in binding vector")
   `(let [~(binding 0) ~(binding 1)]
      ~@body))

(defn new-session [opts]
  (->CassandraSession opts))

(defn new-store []
  (->NewStore))
