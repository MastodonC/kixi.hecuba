(ns kixi.hecuba.transport.db
  "Implementations of commander and querier."
  (:require  [camel-snake-kebab :refer (->snake_case_keyword ->kebab-case-keyword ->camelCaseString)]
             [clojurewerkz.cassaforte.client :as cassaclient]
             [clojurewerkz.cassaforte.query :as cassaquery]
             [clojurewerkz.cassaforte.cql :as cql]
             [kixi.hecuba.hash :refer (sha1)]
             [kixi.hecuba.protocols :as proto]
             [com.stuartsierra.component :as component]))

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
(defmethod gen-key :project [typ payload] ((sha1-keyfn :name) payload))
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
  [payload]
  (reduce (fn [s [k v]] (conj s [(->snake_case_keyword k)
                                  v])) [] (partition 2 payload)))

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
    (binding [cassaclient/*default-session* session]
      (let [id (gen-key typ payload)]
        (cql/insert (get-table typ)
             (let [id-payload (if id (assoc payload :id id) payload)]
               (-> id-payload cassandraify)))
        id)))

  (update! [_ typ payload where]
    (binding [cassaclient/*default-session* session]
      (cql/update (get-table typ) (cassandraify payload) (apply cassaquery/where (apply concat (cassandraify where))))))

  (delete! [_ typ id]
    (assert id "No id!")
    (binding [cassaclient/*default-session* session]
      (cql/delete
       (get-table typ)
       (cassaquery/where :hecuba/id id)))))


(deftype CassandraQuerier [session]
  proto/Querier
  (item [_ typ id]
    (de-cassandraify
     (binding [cassaclient/*default-session* session]
       (first (cql/select
               (get-table typ)
               (cassaquery/where (get-primary-key-field typ) id))))))
  (items [_ typ]
    (map de-cassandraify
         (binding [cassaclient/*default-session* session]
           (cql/select (get-table typ)))))
  (items [_ typ where]
    (map de-cassandraify
         (binding [cassaclient/*default-session* session]
           (cql/select (get-table typ)
                       ;; TODO Vectors should be used across but refactoring will take lots of time. Can't do it now.
                       (if (instance? clojure.lang.PersistentVector where)
                         (do (let [w (vec (apply concat (cassandraify-v where)))]
                               (apply cassaquery/where w)))
                         (apply cassaquery/where (apply concat (cassandraify where))))))))
  (items [_ typ where n]
    (map de-cassandraify
         (binding [cassaclient/*default-session* session]
           (cql/select (get-table typ)
                       (apply cassaquery/where (apply concat (cassandraify where)))
                       (cassaquery/limit n)))))
  (items [_ typ where paginate-key per-page]
    (map de-cassandraify
         (binding [cassaclient/*default-session* session]
           (cql/select (get-table typ)
                       (cassaquery/paginate :key paginate-key :per-page per-page :where (cassandraify where))))))
  (items [_ typ where paginate-key per-page last-key]
    (map de-cassandraify
         (binding [cassaclient/*default-session* session]
           (cql/select (get-table typ)
                       (cassaquery/paginate :key paginate-key :per-page per-page :last-key last-key :where (cassandraify where)))))))

(defrecord CassandraDirectStore []
  component/Lifecycle
  (start [this]
    (if-let [session (get-in this [:session :session])]
      (assoc this
        :commander (->CassandraDirectCommander session)
        :querier (->CassandraQuerier session))
      (throw (ex-info "Store needs a session" {}))
      ))
  (stop [this] this))

(defn new-direct-store []
  (->CassandraDirectStore))
