;; Some utilities that help speed up development
(ns kixi.hecuba.dev
  (:require
   clojure.edn
   clojure.set
   kixi.hecuba.protocols
   [bidi.bidi :refer (path-for match-route)]
   [clojure.pprint :refer (pprint)]
   [kixi.hecuba.hash :refer (sha1)]
   [kixi.hecuba.data :as data]
   [clojure.tools.logging :refer :all]
   [clojure.java.io :as io]
   [clojure.walk :refer (postwalk)]
   [clojurewerkz.cassaforte.client :as cassaclient]
   [clojurewerkz.cassaforte.query :as cassaquery]
   [clojurewerkz.cassaforte.cql :as cql]
   [org.httpkit.client :refer (request) :rename {request http-request}]
   [camel-snake-kebab :refer (->snake_case_keyword ->kebab-case-keyword ->camelCaseString)])
  (:import
   (kixi.hecuba.protocols Commander Querier)))

(defn post-resource [post-uri data]
  (http-request
   {:method :post
    :url post-uri
    :basic-auth ["bob" "secret"]
    :body [data]}
   identity))

;; This is going over HTTP, kixi.hecuba.amon-test goes over ring-mock, so they're a bit different.

(def dummy-data
  {:programmes
   [{:name "America"
     :leaders "Bush"
     :projects
     [{:name "Green Manhattan"
       :properties [{:name "The Empire State Building"
                     :address "New York"
                     :rooms 100
                     :date-of-construction 1930}]}


      {:name "The Historical Buildings Project"
       :properties [{:name "Falling Water"
                     :address "1491 Mill Run Rd, Mill Run, PA"
                     :rooms 4
                     :date-of-construction 1937}]}

      {:name "Area 51 Conservation Project"}]}

    {:name "London"
     :leaders "Blair"
     :projects
     [{:name "Monarchy Energy Savings"
       :properties [{:name "Buckingham Palace"
                     :address "London SW1A 1AA, United Kingdom"
                     :rooms 775}

                    {:name "Windsor Castle"
                     :rooms 175
                     :devices [{:name "Corgi Boiler"
                                :room "Throne Room"}
                               {:name "Crown Jewels Security Camera"
                                :room "The Vault"}
                               {:name "Lord Lucan Escape Monitor"
                                :room "Dungeon"}]
                     }]}

      {:name "Carbon Neutral Tech City"
       :properties [{:name "The ODI"
                     :address "3rd Floor, 65 Clifton Street, London EC2A 4JE"
                     :rooms 13
                     }]}]}]})

(defn sha1-keyfn
  "From a given payload, compute an id that is a SHA1 dependent on the given types."
  [& types]
  (fn [payload]
    (assert (every? payload (set types))
            (format "Cannot form a SHA1 because required types (%s) are missing from the payload: %s"
                    (apply str (interpose ", " (map ->camelCaseString (clojure.set/difference (set types) (set (keys payload))))))
                    payload))
    (-> payload ((apply juxt types)) pr-str sha1)))

;; The keyfn here is a fn that takes a single argument (the payload)
;; and computes the id. The keyfn can be created with sha1-keyfn, or
;; simply :id or a combination (for instance, using
;; clojure.core/some)

;; We can delete this RefCommander when everything work

(deftype RefCommander [r keyfn]
  Commander
  (upsert! [_ typ payload]
    (let [id (keyfn payload)]
      (dosync (alter r assoc-in [typ id] (-> payload (assoc :id id) (dissoc :type))))
      id))
  (delete! [_ typ id]
    (dosync (alter r update-in [typ] dissoc id))))

(defrecord RefQuerier [r]
  Querier
  (item [_ typ id] (get-in @r [typ id]))
  (items [_ typ]
    (vals (get @r typ)))
  (items [this typ where] (filter #(= where (select-keys % (keys where))) (.items this typ))))

(defn create-ref-store [r keyfn]
  {:commander (->RefCommander r keyfn)
   :querier (->RefQuerier r)})

(defmacro ignoring-error [& body]
  `(try
    ~@body
    (catch Exception e# nil)))


(defn spider
  "A spider takes some data, and constructs a new map. The mapping given
  as the second argumen is a mapping between keys and functions that,
  when given the data as an argument, will provide a entry's value."
  [data mapping]
  (letfn [(as-coll [c] (if (coll? c) c (list c)))
          (tr [f]
            (cond
             (vector? f) (fn [x] (filter (apply comp (reverse (map tr f))) x))
             ;; Collections treated like node-sets are in xpath
             (list? f) (partial map (first f))
             :otherwise f)
            )]
    (reduce-kv
     (fn [s k path]
       (if-let
           [val (reduce
                 (fn [e f] (when e ((tr f) e)))
                 data (as-coll path))]
         (assoc s k val)
         s))
     {} mapping)))

(defmulti gen-key (fn [typ payload] typ))
(defmethod gen-key :programme [typ payload] ((sha1-keyfn :name) payload))
(defmethod gen-key :project [typ payload] ((sha1-keyfn :name) payload))

;; From the CSV files, we actually have an id we can SHA1 from
;; For new properties, we'll have to decide which fields make up the property's 'value'
(defmethod gen-key :entity [typ payload] ((sha1-keyfn :property-code :project-id) payload))
;; Again, not sure what should go into the SHA1 here, but it's unlikely for two devices to share the same exact location (which includes name), so let's use that for now
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
  Commander
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
  Querier
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
