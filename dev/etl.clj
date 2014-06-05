(ns etl
  (:require [cheshire.core :refer (encode)]
            [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.pprint :refer (pprint)]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.reader-types :refer (indexing-push-back-reader)]
            [clojure.walk :as walk]
            [com.stuartsierra.component :as component]
            [generators :as generators]
            [etl.fixture :as fixture]
            [kixi.hecuba.data.calculate :as calc]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.webutil :as util]
            [org.httpkit.client :refer (request) :rename {request http-request}]
            [qbits.hayt :as hayt]
            [bidi.bidi :refer (path-for match-route)]))

(defn config []
  (let [f (io/file (System/getProperty "user.home") ".hecuba.edn")]
    (when (.exists f)
      (clojure.tools.reader/read
       (indexing-push-back-reader
        (java.io.PushbackReader. (io/reader f)))))))

(def custom-formatter (tf/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))

(defn difference-series-batch
  "Retrieves all sensors that need to have difference series calculated and performs calculations."
  [store item]
  (let [sensors (m/all-sensors store)]
    (doseq [s sensors]
      (let [device_id (:device_id s)
            type      (:type s)
            period    (:period s)
            where     {:device_id device_id :type type}
            range     (m/start-end-dates :difference_series s where)
            new-item  (assoc item :sensor s :range range)]
        (when (and range (= "CUMULATIVE" period))
          (calc/difference-series store new-item)
          (m/reset-date-range store s :difference_series (:start-date range) (:end-date range)))))))

(defn rollups
  "Retrieves all sensors that need to have hourly measurements rolled up and performs calculations."
  [store item]
  (let [sensors (m/all-sensors store)]
    (doseq [s sensors]
      (let [device_id  (:device_id s)
            type       (:type s)
            period     (:period s)
            table      (case period
                         "CUMULATIVE" :difference_series
                         "INSTANT"    :measurement
                         "PULSE"      :measurement)
            where      {:device_id device_id :type type}
            range      (m/start-end-dates :rollups s where)
            new-item   (assoc item :sensor s :range range)]
        (when range
          (calc/hourly-rollups store new-item)
          (calc/daily-rollups store new-item)
          (m/reset-date-range store s :rollups (:start-date range) (:end-date range)))))))

(defn post-resource [post-uri content-type data]
  (pr-str "DATA:" data)
  (let [response
        @(http-request
          {:method :post
           :url post-uri
           :headers {"Content-Type" content-type}
           :basic-auth ["bob" "123465"]
           :body (case content-type
                   "application/edn" (pr-str data)
                   "application/json" (encode data))}
          identity)]
    (assert (:status response) (format "Failed to connect to %s!" post-uri))
    (when (>= (:status response) 400)
      (println "Error status returned on HTTP request")
      (pprint response)
      (throw (ex-info (format "Failed to post resource, status is %d, body is %s" (:status response) (:body response)) {})))
    response))

(defn extract-data
  [dir filename]
  (assert (.exists dir) (format "Dir doesn't exist! %s" dir))
  (assert (.isDirectory dir))
  (let [programmes-file (io/file dir filename)]
    (assert (.exists programmes-file))
    (assert (.isFile programmes-file))
    (let [data (csv/read-csv (io/reader programmes-file))]
      (map zipmap (repeat (map keyword (first data))) (rest data)))))

(defn load-programme-data
  [data {:keys [host port routes handler]}]
  (assert handler "Warning! no handler found")
  (assert port)
  (let [path (path-for routes handler)]
    (into {}
          (for [programme data]
            (let [id (:id programme)
                  response (post-resource
                            (format "http://%s:%d%s" host port path)
                            "application/edn"
                             programme)
                  location (get-in response [:headers :location])
                  _ (assert location)
                  programme_id (get-in (match-route routes location) [:params :programme_id])]

              [id programme_id]
              )))))

(defn load-project-data
  [data {:keys [host port routes handlers programme_id-map]}]
  ;;(assert handler "Warning! no handler found")
  (into {}
        (for [project data]
          (let [id (:id project)
                ;; TODO Should we turn CSV keys into kebab form?
                programme_id (get programme_id-map (:programme_id project))

                path (if programme_id
                       (path-for routes (:projects handlers) :programme_id programme_id)
                       (path-for routes (:allprojects handlers)))

                response (post-resource
                           (format "http://%s:%d%s" host port path)
                           "application/edn"
                           ;; We have to update the simple numeric id
                           ;; with the generated sha1 id
                           (assoc project :programme_id programme_id))

                location (get-in response [:headers :location])
                project_id (get-in (match-route routes location) [:params :project_id])]

            [id project_id]
            ))))

(defn load-entity-data
  [data {:keys [host port routes handlers project_id-map]}]
  (into {}
        (for [entity data]
          (do
            (let [id (:id entity)
                  ;; TODO Should we turn CSV keys into kebab form?
                  project_id (get project_id-map (:project_id entity))
                  property_code (-> entity :uuid)

                  path (if project_id
                         (path-for routes (:entities handlers) :project_id project_id)
                         (path-for routes (:allentities handlers)))

                  response (post-resource
                            (format "http://%s:%d%s" host port path)
                            "application/json"
                            ;; We have to update the simple numeric id
                            ;; with the generated sha1 id
                            (-> entity
                                (dissoc :uuid)
                                (assoc :project_id project_id :property_code property_code)))

                  location (get-in response [:headers :location])
                  entity_id (get-in (match-route routes location) [:params :entity_id])]

              [id entity_id]
              )))))

;; Loads data from CSV exports from MySQL database. Usage: (load-csv system)
(defn load-csv [system]
  (try
    (let [config (config)
          host "localhost"
          port 8000
          routes (-> system :bidi-ring-handler :routes)

          programme-map
          (-> (extract-data (io/file (-> config :etl :data-directory)) "programmes.csv")
              (load-programme-data {:host host :port port
                                    :routes routes
                                    :handler (-> system :amon-api :handlers :programmes)}))]

      (let [project-map
            (as-> (extract-data (io/file (-> config :etl :data-directory)) "projects.csv") %
                  (map #(dissoc % :leader_id) %)
                  (load-project-data % {:host host
                                        :port port
                                        :routes routes
                                        :handlers (-> system :amon-api :handlers)
                                        :programme_id-map programme-map}))]

        (let [entity-map
              (as-> (extract-data (io/file (-> config :etl :data-directory)) "properties.csv") %
                    (map #(assoc % :property_data (pr-str (select-keys % [:age]))) %)
                    (map #(apply dissoc % (map keyword (clojure.string/split "locked,description,monitoring_policy,address_street,address_city,address_county,address_code,created_at,updated_at,terrain,degree_day_region,ownership,fuel_poverty,property_value,property_value_basis,retrofit_start_date,retrofit_completion_date,project_summary,energy_strategy,project_team,design_strategy,other_notes,property_type,property_type_other,built_form,built_form_other,age,construction_date,conservation_area,listed,property_code,monitoring_hierarchy,project_phase,construction_start_date,practical_completion_date,photo_file_name,photo_content_type,photo_file_size,photo_updated_at,completeness,entity_completeness_6m,latitude,longitude,technology_icons,address_code_masked" #","))
                                 ) %)

                    (load-entity-data % {:host host
                                         :port port
                                         :routes routes
                                         :handlers (-> system :amon-api :handlers)
                                         :project_id-map project-map}))]

          (let [entity_id (get entity-map "64")]
            (doseq [device (generators/generate-device-sample entity_id 3)]
              (let [sensors (generators/generate-sensor-sample "CUMULATIVE" 3)

                    response
                    (post-resource
                     (format "http://%s:%d%s" host port
                             (path-for (-> system :bidi-ring-handler :routes)
                                       (-> system :amon-api :handlers :devices) :entity_id entity_id))
                     "application/json"


                     (-> device
                         (assoc :readings sensors)
                         (dissoc :device_id) ; Don't need device_id, it gets generated by liberator
                         ))
                    ;; Take location, parse out entity_id and device_id
                    location (get-in response [:headers :location])
                    ;; Use entity_id and device_id to get measurements URL
                    measurements-uri (apply path-for routes (-> system :amon-api :handlers :measurements)
                                            (apply concat (:params (match-route routes location))))
                    ]

                (assert (= (:status response) 201) (format "Failed to create device, status was %d" (:status response)))

                ;; POST to URL a JSON block
                (let [response
                      (post-resource (format "http://%s:%d%s" host port measurements-uri)
                                     "application/json"
                                     {:measurements
                                      (map
                                       (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter  (clj-time.coerce/from-date %))))
                                       (mapcat generators/measurements sensors))})]
                  )

                )))

          ;;;;; Insert measurements with readings above median ;;;;;;

          (let [entity_id (get entity-map "33")] ;; Woodbine Cottage
            (doseq [device (generators/generate-device-sample entity_id 3)]
              (let [sensors (generators/generate-sensor-sample "INSTANT" 3)

                    response
                    (post-resource
                     (format "http://%s:%d%s" host port
                             (path-for (-> system :bidi-ring-handler :routes)
                                       (-> system :amon-api :handlers :devices) :entity_id entity_id))
                     "application/json"

                     (-> device
                         (assoc :readings sensors)
                         (dissoc :device_id)))
                    ;; Take location, parse out entity_id and device_id
                    location (get-in response [:headers :location])
                    ;; Use entity_id and device_id to get measurements URL
                    measurements-uri (apply path-for routes (-> system :amon-api :handlers :measurements)
                                            (apply concat (:params (match-route routes location))))
                    ]

                ;; POST to URL a JSON block
                (let [response
                      (post-resource (format "http://%s:%d%s" host port measurements-uri)
                                     "application/json"
                                     {:measurements
                                      (map
                                       (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                       (mapcat generators/generate-measurements-above-median sensors))})]))))


          ;;;;; Insert  measurements ;;;;;;

          (let [entity_id (get entity-map "38")] ;; Willow Cottage
            (doseq [device (generators/generate-device-sample entity_id 1)]
              (let [sensors1 (generators/generate-sensor-sample "CUMULATIVE" 2)
                    sensors2 (generators/generate-sensor-sample "PULSE" 1)
                    sensors3 (generators/generate-sensor-sample "INSTANT" 3)

                    ;; Mislabelled measurements
                    response1
                    (post-resource (format "http://%s:%d%s" host port
                                           (path-for (-> system :bidi-ring-handler :routes)
                                                     (-> system :amon-api :handlers :devices) :entity_id entity_id))
                                   "application/json"
                                   (-> device
                                       (assoc :readings sensors1)
                                       (dissoc :device_id)))
                    location1 (get-in response1 [:headers :location])
                    measurements-uri1 (apply path-for routes (-> system :amon-api :handlers :measurements)
                                             (apply concat (:params (match-route routes location1))))

                    ;; Errored measurements
                    response2 (post-resource (format "http://%s:%d%s" host port
                                                     (path-for (-> system :bidi-ring-handler :routes)
                                                               (-> system :amon-api :handlers :devices) :entity_id entity_id))
                                             "application/json"
                                             (-> device
                                                 (assoc :readings sensors2)
                                                 (dissoc :device_id)))
                    location2 (get-in response2 [:headers :location])
                    measurements-uri2 (apply path-for routes (-> system :amon-api :handlers :measurements)
                                             (apply concat (:params (match-route routes location2))))

                    ;; Instant measurements
                    response3 (post-resource (format "http://%s:%d%s" host port
                                                     (path-for (-> system :bidi-ring-handler :routes)
                                                               (-> system :amon-api :handlers :devices) :entity_id entity_id))
                                             "application/json"
                                             (-> device
                                                 (assoc :readings sensors3)
                                                 (dissoc :device_id)))
                    location3 (get-in response3 [:headers :location])
                    measurements-uri3 (apply path-for routes (-> system :amon-api :handlers :measurements)
                                             (apply concat (:params (match-route routes location3))))

                    ]

                ;; POST to URL a JSON block
                (let [response1
                      (post-resource (format "http://%s:%d%s" host port measurements-uri1)
                                     "application/json"
                                     {:measurements
                                      (map
                                       (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                       (mapcat generators/mislabelled-measurements sensors1))})
                      response2
                      (post-resource (format "http://%s:%d%s" host port measurements-uri2)
                                     "application/json"
                                     {:measurements
                                      (map
                                       (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                       (mapcat generators/generate-invalid-measurements sensors2))})

                      response3
                      (post-resource (format "http://%s:%d%s" host port measurements-uri3)
                                     "application/json"
                                     {:measurements
                                      (map
                                       (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                       (mapcat generators/measurements sensors3))})

                      ]))))))
      (let [store (-> system :store)]

        (difference-series-batch store {})
        (rollups store {})))
    (catch Exception e
      (log/error e "ETL failed:"))))

;; Makes use of kixi.hecuba.users/ApiService to create users listed in .hecuba.edn file
(defn  load-user-data []
  (let [config (config)
        host   "localhost"
        port   8000
        routes (-> config :bidi-ring-handler :routes)]

    (doseq [{:keys [username password]} (:users config)]
      (let [body {:username username :password password}
            response
            (post-resource
             (format "http://%s:%d%s" host port "/users/")
             "application/edn"
             body)]
        (when (not= (:status response) 201)
          (println "Failed to add user:" body)
          (pprint response))))))

(defn db-timestamp
  "Returns java.util.Date from String timestamp." ; 2014-01-01 00:00:10+0000
  [t] (.parse (java.text.SimpleDateFormat.  "yyyy-MM-dd HH:mm:ssZ") t))

(defn load-sensor-sample [system]
  (let [programme_id "2312312314"
        project_id "32523453"
        property_id "34653464"
        device_id_1 "fe5ab5bf19a7265276ffe90e4c0050037de923e2"
        device_id_2 "fe5ab5bf19a7265276ffe90e4c0050037de923e2"]
   (db/with-session [session (:hecuba-session system)]

     (db/execute session
                 (hayt/insert :programmes
                              (hayt/values {:id programme_id
                                            :name "AAA_Calculated_Test Programme"})))
     (db/execute session
                 (hayt/insert :projects
                              (hayt/values {:id project_id
                                            :name "AAA_Calculated_Test Project"
                                            :programme_id programme_id
                                            })))
     (db/execute session
                 (hayt/insert :entities
                              (hayt/values {:id property_id
                                            :name "AAA_Calculated_Test Properties"
                                            :address_street_two "A1 Flat, A1 road, A1 Town, A1 1AA"
                                            :project_id project_id})))
     (db/execute session
                 (hayt/insert :devices
                              (hayt/values {:id device_id_1
                                            :name "AAA_Calculated_Test Device 1"
                                            :entity_id property_id})))
     (db/execute session
                 (hayt/insert :devices
                              (hayt/values {:id device_id_2
                                            :name "AAA_Calculated_Test Device 2"
                                            :entity_id property_id})))

     (db/execute session
                 (hayt/insert :sensors
                              (hayt/values {:device_id device_id_1
                                            :period "PULSE"
                                            :unit "m^3"
                                            :type "gasConsumption"})))
     (db/execute session
                 (hayt/insert :sensors
                              (hayt/values {:device_id device_id_2
                                            :period "PULSE"
                                            :unit "kWh"
                                            :type "electricityConsumption"})))
     (db/execute session
                 (hayt/insert :sensors
                              (hayt/values {:device_id device_id_2
                                            :period "PULSE"
                                            :unit "co2"
                                            :type "electricityConsumption_converted_co2"})))
     (db/execute session
                 (hayt/insert :sensor_metadata
                              (hayt/values {:device_id device_id_1
                                            :lower_ts (.getMillis (t/date-time 2014 1))
                                            :upper_ts (.getMillis (t/date-time 2014 2))
                                            :rollups {"start" (db-timestamp "2014-01-01 00:00:00+0000") 
                                                      "end" (db-timestamp "2014-02-01 00:00:00+0000")}
                                            :type "gasConsumption"})))

     (db/execute session
                 (hayt/insert :sensor_metadata
                              (hayt/values {:device_id device_id_2
                                            :type "electricityConsumption"})))
     
     (db/execute session
                 (hayt/insert :sensor_metadata
                              (hayt/values {:device_id device_id_2
                                            :type "electricityConsumption_converted_co2"})))
     
     (with-open [in-file (io/reader (io/resource "gasConsumption-fe5ab5bf19a7265276ffe90e4c0050037de923e2.csv"))]
       (doseq [m (map #(zipmap [:device_id :type :month :timestamp :error :metadata :value] %) (rest (csv/read-csv in-file )))]
         (db/execute session
                     (hayt/insert :measurements
                                  (hayt/values
                                   (-> m
                                       (update-in [:metadata] #(walk/stringify-keys (read-string %)))
                                       (update-in [:timestamp] db-timestamp)
                                       (update-in [:month] #(Integer/parseInt %))))))))
     
     (let [url "http://127.0.0.1:8000/4/entities/34653464/devices/fe5ab5bf19a7265276ffe90e4c0050037de923e2/measurements/"
           measurements (generators/measurements  {:type "electricityConsumption"
                                                   :unit "kWh"
                                                   :period "PULSE"
                                                   :events 0
                                                   :errors 0})
           response (post-resource url "application/json" {:measurements
                                                           (map (fn [x] (update-in x [:timestamp]
                                                                                   #(tf/unparse custom-formatter (tc/from-date %))))
                                                                measurements)})]))))

(defn insert-all [session table xs]
  (doseq [x xs]
     (db/execute session (hayt/insert table (hayt/values x)))))

(defn readings [n]
  (map #(hash-map :value (str %) :month 201405 :timestamp (t/date-time 2014 5 n)) (range n)))

(defn load-total-kwh-sample [system]
  (db/with-session [session (:hecuba-session system)]
    (fixture/load-fixture session [:programmes#A1
                                   [:projects#A1
                                    [:entities#A1 {:address_street_two "A1 Flat, A1 road, A1 Town, A1 1AA"}
                                     [:devices#D1
                                      [:sensors#gasConsumption
                                       {:unit "m^3"
                                        :period "PULSE"
                                        :etl.fixture/start (t/date-time 2014 5 1)
                                        :etl.fixture/end  (t/date-time 2014 5 1)}
                                       [:measurements] (readings 2)]]
                                     [:devices#D2
                                      [:sensors#electricityConsumption
                                       {:unit "kwh"
                                        :period "CUMULATIVE"
                                        :etl.fixture/start (t/date-time 2014 5 1)
                                        :etl.fixture/end  (t/date-time 2014 5 1)}
                                       [:measurements (readings 2)]]]]]])))

;; To load users from .hecuba.edn: (load-user-data)
;; To load data from CSV files: (load-csv system)
