(ns kixi.hecuba.system
  (:require
   [com.stuartsierra.component :as component]

   ;; Hecuba custom components

   ;; Modular reusable components
   [modular.core :as mod]
   [modular.http-kit :refer (new-webserver)]
   #_[modular.ring :refer (resolve-handler-provider)]
   [modular.bidi :refer (new-bidi-ring-handler-provider #_resolve-routes-contributors)]
   [modular.cassandra :refer (new-session new-cluster)]

   [kixi.hecuba.dev :refer (->CassandraDirectCommander ->CassandraQuerier)]


   [kixi.hecuba.web :refer (new-main-routes)]
   [kixi.hecuba.amon :refer (new-amon-api-routes)]
   [kixi.hecuba.user :refer (new-user-api-routes)]
   [kixi.hecuba.cljs :refer (new-cljs-routes)]
   [kixi.hecuba.dev.etl :refer (new-user-data-loader)]

   [clojurewerkz.cassaforte.client :as cassaclient]
   [clojurewerkz.cassaforte.query :as cassaquery]
   [clojurewerkz.cassaforte.cql :as cql]

   [shadow.cljs.build :as cljs]

   ;; Misc
   clojure.tools.reader
   [clojure.pprint :refer (pprint)]
   [clojure.tools.reader.reader-types :refer (indexing-push-back-reader source-logging-push-back-reader)]
   [clojure.java.io :as io]))

(defn config []
  (let [f (io/file (System/getProperty "user.home") ".hecuba.edn")]
    (when (.exists f)
      (clojure.tools.reader/read
       (indexing-push-back-reader
        (java.io.PushbackReader. (io/reader f)))))))


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

(defmacro ignoring-error [& body]
  `(try
    ~@body
    (catch Exception e# nil)))

(defrecord CassandraSchema []
  component/Lifecycle
  (start [this]
    (let [session (get-in this [:session :session])]
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
                             :primary-key :id}))))

      this))
  (stop [this] this))

(defn new-schema []
  (->CassandraSchema))

(defn define-modules [state]
  (-> state
      (cljs/step-configure-module
       :cljs ;; module name
       ['cljs.core] ;; module mains, a main usually contains exported functions or code that just runs
       #{}) ;; module dependencies
      (cljs/step-configure-module :hecuba ['kixi.hecuba.main] #{:cljs})
      ))

(defn message [state message]
  (println message)
  state
)

(defn compile-cljs
  "build the project, wait for file changes, repeat"
  [& args]
  (let [state (-> (cljs/init-state)
                  (cljs/enable-source-maps)
                  (assoc :optimizations :none
                         :pretty-print true
                         :work-dir (io/file "target/cljs-work") ;; temporary output path, not really needed
                         :public-dir (io/file "target/cljs") ;; where should the output go
                         :public-path "/cljs") ;; whats the path the html has to use to get the js?
                  (cljs/step-find-resources-in-jars) ;; finds cljs,js in jars from the classpath
                  (cljs/step-find-resources "lib/js-closure" {:reloadable false})
                  (cljs/step-find-resources "src-cljs") ;; find cljs in this path
                  (cljs/step-finalize-config) ;; shouldn't be needed but is at the moment
                  (cljs/step-compile-core) ;; compile cljs.core
                  (define-modules)
                  )]

    (-> state
        (cljs/step-compile-modules)
        (cljs/flush-unoptimized)))

  :done)

(defn spy [x]
  (println "System map is now")
  (pprint x)
  x)

(defrecord ClojureScriptBuilder []
  component/Lifecycle
  (start [this]
    (try
      (compile-cljs)
      this
      (catch Exception e
        (println "ClojureScript build failed:" e)
        (assoc this :error e))))
  (stop [this] this))


(defn new-cljs-builder []
  (->ClojureScriptBuilder))

(defn new-system []
  (let [cfg (config)]
    (-> (component/system-map
         :cluster (new-cluster (:cassandra-cluster cfg))
         :session (new-session (:cassandra-session cfg))
         :schema (new-schema)
         :store (new-direct-store)
         :cljs-builder (new-cljs-builder)
         :web-server (new-webserver (:web-server cfg))
         :bidi-ring-handler (new-bidi-ring-handler-provider)
         :main-routes (new-main-routes)
         :amon-api-routes (new-amon-api-routes "/3")
         :user-api-routes (new-user-api-routes)
         :cljs-routes (new-cljs-routes (:cljs-builder cfg))

         :user-data-loader (new-user-data-loader cfg)

         )

        (mod/system-using {:main-routes [:store]
                           :amon-api-routes [:store]
                           :user-api-routes [:store]
                           :store [:session :schema]
                           :schema [:session]
                           :user-data-loader [:bidi-ring-handler]
                           ;;:cljs-routes [:cljs-builder]
                           :session [:cluster]}))))
