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
   [camel-snake-kebab :refer (->snake_case_keyword ->kebab-case-keyword)])
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

#_(deftype ExampleDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [handlers (:amon/handlers system)
          routes (get-routes system)]
      (doseq [programme (:programmes dummy-data)]
        (println (path-for routes (:programmes handlers)))))
    system)
  (stop [_ system] system))

(defn sha1-keyfn
  "From a given payload, compute an id that is a SHA1 dependent on the given types."
  [& types]
  (fn [payload]
    (assert (every? payload (set types))
            (format "Cannot form a SHA1 because required types (%s) are missing from the payload: %s"
                    (apply str (interpose "," (clojure.set/difference (set types) (set (keys payload)))))
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

#_(deftype RefStore [config]
  Lifecycle
  (init [_ system]
    ;; Note, this may overwrite an existing store
    (merge system (create-ref-store (ref {}) (sha1-keyfn :name :type))))
  (start [_ system] system)
  (stop [_ system] system))

#_(deftype CassandraCluster [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (assoc-in system
              [:cassandra :cluster]
              (cassaclient/build-cluster (select-keys config [:contact-points :port]))))
  (stop [_ system]
    (when-let [cluster (get-in system [:cassandra :cluster])]
      (.shutdown cluster))
    system))

#_(deftype CassandraSession [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [cluster (get-in system [:cassandra :cluster])]
      (assert cluster "No cluster found in system")
      (assoc-in system [:cassandra :session] (cassaclient/connect cluster (:keyspace config)))))
  (stop [_ system]
    (when-let [session (get-in system [:cassandra :session])]
      (.shutdown session))
    system))

(defmacro ignoring-error [& body]
  `(try
    ~@body
    (catch Exception e# nil)))

;; Perhaps this schema can make it's way into the config
#_(deftype CassandraSchema [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [session (get-in system [:cassandra :session])]
      (assert session "No session found in system")
      (binding [cassaclient/*default-session* session]

        (ignoring-error (cql/drop-table "programmes"))
        (ignoring-error (cql/drop-table "projects"))
        (ignoring-error (cql/drop-table "entities"))
        (ignoring-error (cql/drop-table "devices"))
        (ignoring-error (cql/drop-table "sensors"))
        (ignoring-error (cql/drop-table "sensor_metadata"))
        (ignoring-error (cql/drop-table "measurements"))
        (ignoring-error (cql/drop-table "difference_series"))
        (ignoring-error (cql/drop-table "hourly_rollups"))
        (ignoring-error (cql/drop-table "daily_rollups"))
        (ignoring-error (cql/drop-table "users"))

        ;; While developing, it's a pain to have to keep logging in
        ;;(ignoring-error (cql/drop-table "user_sessions"))

        (ignoring-error
         (cql/create-table "programmes"
              (cassaquery/column-definitions
                          {:id :varchar
                           :name :varchar
                           :leaders :varchar
                           :home_page_text :varchar
                           :lead_page_text :varchar
                           :lead_organisations :varchar
                           :public_access :varchar
                           :description :varchar
                           :created_at :varchar
                           :updated_at :varchar
                           :primary-key [:id]})))

        (ignoring-error
         (cql/create-table "projects"
              (cassaquery/column-definitions
                          {:id :varchar
                           :name :varchar
                           :updated_at :varchar
                           :organisation :varchar
                           :created_at :varchar
                           :project_code :varchar
                           :programme_id :varchar
                           :project_type :varchar
                           :type_of :varchar
                           :description :varchar
                           :primary-key [:id]})))

        (ignoring-error
         (cql/create-index "projects" :programme_id))

        (ignoring-error
         (cql/create-table "entities"
              (cassaquery/column-definitions
                          {:id :varchar
                           :project_id :varchar

                           :name :varchar
                           :user_id :varchar
                           :documents :list<text>
                           :notes :list<text>
                           :csv_uploads :list<text>
                           :photos :list<text>

                           :address_street_two :varchar
                           :address_county :varchar
                           :address_region :varchar
                           :address_country :varchar

                           ;; :rooms :varchar
                           ;; :date_of_construction :varchar
                           :retrofit_completion_date :varchar
                           ;; :address :varchar
                           :property_data :text
                           :primary-key [:id]})))

        (ignoring-error
         (cql/create-index "entities" :project_id))

        (ignoring-error
         (cql/create-table "devices"
              (cassaquery/column-definitions
                          {:id :varchar
                           :name :varchar
                           :entity_id :varchar
                           :description :varchar
                           :metering_point_id :varchar
                           :parent_id :varchar
                           :location :varchar
                           :privacy :varchar
                           :metadata :varchar
                           :primary-key [:id]})))

        (ignoring-error
         (cql/create-index "devices" :entity_id))

        (ignoring-error
         (cql/create-table "sensors"
              (cassaquery/column-definitions
                          {:device_id :varchar
                           :type :varchar
                           :unit :varchar
                           :resolution :varchar
                           :accuracy :varchar
                           :period :varchar
                           :min :varchar
                           :max :varchar
                           :correction :varchar
                           :corrected_unit :varchar
                           :correction_factor :varchar
                           :correction_factor_breakdown :varchar
                           :events :int
                           :errors :int
                           :median :double
                           :status :varchar
                           :primary-key [:device_id :type]})))

        (ignoring-error
         (cql/create-index "sensors" :status))

        (ignoring-error
         (cql/create-table "sensor_metadata"
                           (cassaquery/column-definitions
                            {:device_id :varchar
                             :type :varchar
                             :mislabelled :varchar
                             :median_calc_check :bigint
                             :bootstrapped :varchar
                             :mislabelled_sensors_check :bigint
                             :difference_series :bigint
                             :hourly_rollups :bigint
                             :daily_rollups :bigint
                             :primary-key [:device_id :type]})))

        (ignoring-error
         (cql/create-index "sensor_metadata" :mislabelled))

        (ignoring-error
         (cql/create-table "measurements"
                           (cassaquery/column-definitions
                            {:device_id :varchar
                             :type :varchar
                             :month :int
                             :value :varchar
                             :error :varchar
                             :timestamp :timestamp
                             :metadata :varchar
                             :primary-key [:device_id :type :month :timestamp]})))

        (ignoring-error
         (cql/create-table "difference_series"
                           (cassaquery/column-definitions
                            {:device_id :varchar
                             :type :varchar
                             :month :int
                             :timestamp :timestamp
                             :value :varchar
                             :primary-key [:device_id :type :month :timestamp]})))


         (ignoring-error
         (cql/create-table "hourly_rollups"
                           (cassaquery/column-definitions
                            {:device_id :varchar
                             :type :varchar
                             :month :int
                             :timestamp :timestamp
                             :value :varchar
                             :primary-key [:device_id :type :month :timestamp]})))

         (ignoring-error
         (cql/create-table "daily_rollups"
                           (cassaquery/column-definitions
                            {:device_id :varchar
                             :type :varchar
                             :month :int
                             :timestamp :timestamp
                             :value :varchar
                             :primary-key [:device_id :type :month :timestamp]})))

        ;; This could possibly go into another component, but for now we'll hijack this one.
        (ignoring-error
         (cql/create-table "users"
              (cassaquery/column-definitions
                          {:id :varchar
                           ;; TODO This is weird, will clean this up after getting current task done
                           :username :varchar ; the username and id are equal
                           :salt :varchar
                           :hash :varchar
                           :primary-key [:id]})))

        (ignoring-error
         (cql/create-table "user_sessions"
                           (cassaquery/column-definitions
                          {:id :varchar
                           :user :varchar
                           :timestamp :timestamp
                           :primary-key [:id :timestamp]})))

        (ignoring-error
         (cql/create-table "data_sets"
                           (cassaquery/column-definitions
                            {:id :varchar
                             :data_set_name :varchar
                             :sensor_group :varchar
                             :primary-key :id})))))
    system)

  (stop [_ system]
    (let [session (get-in system [:cassandra :session])]
      (assert session "No session found in system")
      ;; Let's not drop tables, it's maybe desirable to stop Jig and
      ;; take a look at the C* tables.
      #_(binding [cassaclient/*default-session* session]
          (cql/drop-table "programmes")
          (cql/drop-table "projects")
          (cql/drop-table "entities")
          (cql/drop-table "devices")
          (cql/drop-table "sensors")
          (cql/drop-table "sensor_metadata")
          (cql/drop-table "measurements")))
    system))

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
(defmethod gen-key :entity [typ payload] ((sha1-keyfn :id) payload))
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
(defmethod gen-key :dataset [typ payload] (sha1-keyfn :name :entity-id))

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
    (debugf "type is %s, payload %s" typ payload)
    (binding [cassaclient/*default-session* session]
      (let [id (gen-key typ payload)]
        (cql/insert (get-table typ)
             (let [id-payload (if id (assoc payload :id id) payload)]
               (-> id-payload cassandraify)))
        id)))

  (update! [_ typ col payload where]
    (assert col "No column!")
    (assert where "No where clause!")
    (binding [cassaclient/*default-session* session]
      (cql/update (get-table typ) {col payload}
                  (apply cassaquery/where (apply concat (cassandraify where))))))

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
  (items [this typ where]
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

#_(deftype CassandraDirectStore [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [session (get-in system [:cassandra :session])]
      (assert session "No session found in system")
      (-> system
          (assoc :commander (->CassandraDirectCommander session))
          (assoc :querier (->CassandraQuerier session)))))
  (stop [_ system]
    (dissoc system :commander :querier)))

#_(deftype HttpClientChecks [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [uri (format "http://localhost:%d%s"
                      (get-port system)
                      (path-for (get-routes system) (:programmes (get-handlers system))))]
      (let [projects-response
            @(http-request
              {:method :get
               :url uri
               :basic-auth ["bob" "secret"]
               :headers {"Accept" "application/edn"}
               }
              identity)
            projects
            (clojure.edn/read (java.io.PushbackReader. (io/reader (:body projects-response))))]
        (when-not
            (and
             (= (get-in projects-response [:headers :content-type]) "application/edn;charset=UTF-8")
             (<= 2 (count projects)))
          (println "Warning: HTTP client checks failed" (count projects)))))
    system)
  (stop [_ system] system))
