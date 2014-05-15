(ns kixi.hecuba.storage.dbnew
    "Implementations of commander and querier."
  (:require  [camel-snake-kebab :refer (->snake_case_keyword ->kebab-case-keyword ->camelCaseString ->snake_case_string)]
             [qbits.alia :as alia]
             [qbits.hayt :as hayt]
             [qbits.alia.codec.joda-time] ; necessary to get the codec installed.
             [kixi.hecuba.hash :refer (sha1)]
             [kixi.hecuba.protocols :as hecuba]
             [com.stuartsierra.component :as component]
             [clojure.tools.logging :as log]))

(require 'qbits.alia.codec.joda-time)

; Tweak keyword handling. We want to support keywords with '-' in
; them. quoted identifiers are case-sensitive in CQL, so lowercase them.
;
; mpenet thinks this might cause performance problems
(extend-protocol qbits.hayt.cql/CQLEntities
   clojure.lang.Keyword
   (cql-identifier [x] (if (= x :*)
                         "*"
                         (->snake_case_string x)))
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

(defn- to-clj
  "Convert to kebab form after reading from Cassandra."
  [payload]
  (when payload
    (reduce-kv (fn [s k v]
                 (try
                   (conj s [(->kebab-case-keyword k)
                            (str v)])
                   (catch Exception e (do (println "exception on keyword: " k v) s))
                   )) {} payload)))

(defmulti ->clj #(if (seq? %) :multiple :single))

(defmethod ->clj :multiple [xs]
  (map to-clj xs))

(defmethod ->clj :single [x]
  (to-clj x))

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
    (alia/execute (:session this) (->raw-with-logging query) opts))
  (hecuba/-execute-async [this query opts]
    (alia/execute-async (:session this) (->raw-with-logging query) opts))
  (hecuba/-execute-chan [this query opts]
    (alia/execute-chan (:session this) (->raw-with-logging query) opts)))

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
