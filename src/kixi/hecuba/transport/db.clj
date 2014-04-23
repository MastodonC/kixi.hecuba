(ns kixi.hecuba.transport.db
  "Implementations of commander and querier."
  (:require  [camel-snake-kebab :refer (->snake_case_keyword ->kebab-case-keyword ->camelCaseString)]
             [qbits.alia :as alia]
             [qbits.hayt :as hayt]
             [kixi.hecuba.hash :refer (sha1)]
             [kixi.hecuba.protocols :as proto]
             [com.stuartsierra.component :as component]
             [clojure.tools.logging :as log]
             ))

(defn sha1-keyfn
  "From a given payload, compute an id that is a SHA1 dependent on the given types."
  [& types]
  (fn [payload]
    (assert payload "payload cannot be nil")
    (assert (every? payload (set types))
            (format "Cannot form a SHA1 because required types (%s) are missing from the payload: %s"
                    (apply str (interpose ", " (map ->camelCaseString (clojure.set/difference (set types) (set (keys payload))))))
                    payload))
    (-> payload ((apply juxt types)) pr-str sha1)))


(defmulti gen-key (fn [typ payload] typ))
(defmethod gen-key :programme [typ payload] ((sha1-keyfn :name) payload))
(defmethod gen-key :project [typ payload] ((sha1-keyfn :name :programme-id) payload))
(defmethod gen-key :entity [typ payload] ((sha1-keyfn :property-code :project-id) payload))
(defmethod gen-key :device [typ payload] ((sha1-keyfn :description :entity-id) payload))

(defmethod gen-key :sensor [typ payload] nil)
(defmethod gen-key :sensor-metadata [typ payload] nil)
(defmethod gen-key :measurement [typ payload] nil)
(defmethod gen-key :difference-series [typ payload] nil)
(defmethod gen-key :hourly-rollups [typ payload] nil)
(defmethod gen-key :daily-rollups [typ payload] nil)

(defmethod gen-key :user [typ payload] (:username payload))
(defmethod gen-key :user-session [typ payload] (:id payload))
(defmethod gen-key :dataset [typ payload] nil)

(defmulti get-primary-key-field (fn [typ] typ))
(defmethod get-primary-key-field :programme [typ] :id)
(defmethod get-primary-key-field :project [typ] :id)
(defmethod get-primary-key-field :entity [typ] :id)
(defmethod get-primary-key-field :device [typ] :id)
(defmethod get-primary-key-field :user [typ] :id) ; TODO should be username...
(defmethod get-primary-key-field :user-session [typ] :id)
(defmethod get-primary-key-field :dataset [typ] :id)

(defmulti get-table identity)
(defmethod get-table :programme [_] "programmes")
(defmethod get-table :project [_] "projects")
(defmethod get-table :property [_] "entities")
(defmethod get-table :entity [_] "entities")
(defmethod get-table :device [_] "devices")
(defmethod get-table :sensor [_] "sensors")
(defmethod get-table :sensor-metadata [_] "sensor_metadata")
(defmethod get-table :measurement [_] "measurements")
(defmethod get-table :difference-series [_] "difference_series")
(defmethod get-table :hourly-rollups [_] "hourly_rollups")
(defmethod get-table :daily-rollups [_] "daily_rollups")
(defmethod get-table :user [_] "users")
(defmethod get-table :user-session [_] "user_sessions")
(defmethod get-table :dataset [_] "data_sets")


(defn cassandraify
  "Cassandra has various conventions, such as forbidding hyphens in
  keywords (error) and our current design decision to use varchars for
  fields (for the sake of simplicity)"
  [payload]
  (reduce-kv (fn [s k v] (conj s [(->snake_case_keyword k)
                                  v])) {} payload))

(defn cassandraify-v
  [where]
  (let [where (into [] (for [[op k v] where] [op (->snake_case_keyword k) v]))]
    where))

(defn de-cassandraify
  "Convert to kebab form after reading from Cassandra."
  [payload]
  (when payload
    (reduce-kv (fn [s k v]
                 (try
                   (conj s [(->kebab-case-keyword k)
                            (str v)])
                   (catch Exception e (do (println "exception on keyword: " k v) s))
                   )) {} payload)))


(deftype CassandraDirectCommander [session]
  proto/Commander
  (upsert! [_ typ payload]
    (assert session "No session!")
    (assert typ "No type!")
    (let [id (if-let [i (:id payload)] i (gen-key typ payload))]
      (alia/execute session
       (hayt/insert (get-table typ) (hayt/values
                                     (let [id-payload (if id (assoc payload :id id) payload)]
                                       (-> id-payload cassandraify)))))
      id))

  (update! [_ typ payload where]
    (assert where "No where clause")
    (alia/execute session
     (hayt/update (get-table typ) 
                  (hayt/set-columns (cassandraify payload))
                  (hayt/where (cassandraify-v where)))))

  (delete! [_ typ where]
    (assert where "No where clause!")
    (alia/execute session
     (hayt/delete (get-table typ)
                  (hayt/where (cassandraify-v where)))))

  (delete! [_ typ columns where]
    (assert where "No where clause!")
    (assert columns "No column(s) specified!")
    (alia/execute session
     (hayt/delete (get-table typ) (hayt/columns columns) (hayt/where (cassandraify-v where))))))

;; TODO de-cassandraify on exit?
(deftype CassandraQuerier [session]
  proto/Querier
  (item [_ typ id]
    (de-cassandraify
     (first
      (alia/execute session
       (hayt/select (get-table typ)
                    (hayt/where {(get-primary-key-field typ) id}))))))
  (items [_ typ]
    (map de-cassandraify
         (alia/execute session
          (hayt/select (get-table typ)))))
  (items [_ typ where]
    (map de-cassandraify
         (alia/execute session
          (hayt/select (get-table typ)
                       ;; Where clause should always be a vector of vectors, e.g.
                       ;; [[= :device-id "1"] [= :type "temp"] [> :timestamp "20140510"]
                       (hayt/where (cassandraify-v where)))))) )

(defrecord CassandraDirectStore []
  component/Lifecycle
  (start [this]
    (log/info "CassandraDirectStore starting")
    (if-let [session (get-in this [:session :session])]
      (assoc this
        :commander (->CassandraDirectCommander session)
        :querier (->CassandraQuerier session))
      (throw (ex-info "Store needs a session" {}))
      ))
  (stop [this]
    (log/info "CassandraDirectStore stopping")
    this))

(defn new-direct-store []
  (->CassandraDirectStore))

(defrecord Cluster [opts]
  component/Lifecycle
  (start [this]
    (assoc this :cluster (alia/cluster opts)))
  (stop [this]
    (when-let [cluster (:cluster this)]
      (alia/shutdown cluster)
      this)))

(def ClusterDefaults
  {:contact-points ["127.0.0.1"]
   :port 9042})

(defn new-cluster [opts]
  (->Cluster (merge ClusterDefaults opts)))

(defrecord Session [opts]
  component/Lifecycle
  (start [this]
    (assoc this :session (alia/connect (get-in this [:cluster :cluster]) (:keyspace opts))))
  (stop [this]
    (when-let [session (:session this)]
      (alia/shutdown session)
      this)))

(def SessionDefaults {})

(defn new-session [opts]
  (->Session (merge SessionDefaults opts)))
