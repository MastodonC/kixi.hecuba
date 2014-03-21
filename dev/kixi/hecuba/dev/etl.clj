(ns kixi.hecuba.dev.etl
  (:require
   [com.stuartsierra.component :as component]
   [clojure.pprint :refer (pprint)]
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   clj-time.coerce
   [clj-time.format :as tf]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [bidi.bidi :refer (path-for match-route)]
   [org.httpkit.client :refer (request) :rename {request http-request}]
   [cheshire.core :refer (encode)]
   [kixi.hecuba.dev.generators :as generators]
   [kixi.hecuba.data.calculate :as calc]
   [kixi.hecuba.data.misc :as m]
   [kixi.hecuba.protocols :refer (items)]
   [camel-snake-kebab :refer (->camelCaseString)]))

(def custom-formatter (tf/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))

(defn difference-series-batch
  "Retrieves all sensors that need to have difference series calculated and performs calculations."
  [commander querier item]
  (let [sensors (m/all-sensors querier)]
    (doseq [s sensors]
      (let [device-id (:device-id s)
            type      (:type s)
            period    (:period s)
            where     {:device-id device-id :type type}
            range     (m/start-end-dates querier :measurement :difference-series s where)
            new-item  (assoc item :sensor s :range range)]
        (when range
          (calc/difference-series commander querier new-item)
          (m/reset-date-range querier commander s :difference-series (:start-date range) (:end-date range)))))))

(defn rollups
  "Retrieves all sensors that need to have hourly measurements rolled up and performs calculations."
  [commander querier item]
  (let [sensors (m/all-sensors querier)]
    (doseq [s sensors]
      (let [device-id  (:device-id s)
            type       (:type s)
            period     (:period s)
            table      (case period
                         "CUMULATIVE" :difference-series
                         "INSTANT"    :measurement
                         "PULSE"      :measurement)
            where      {:device-id device-id :type type}
            range      (m/start-end-dates querier table :rollups s where)
            new-item   (assoc item :sensor s :range range)]
        (when range
          (calc/hourly-rollups commander querier new-item)
          (calc/daily-rollups commander querier new-item)
          (m/reset-date-range querier commander s :rollups (:start-date range) (:end-date range)))))))

(defn post-resource [post-uri content-type data]
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
      (throw (ex-info (format "Failed to post resource, status is %d" (:status response)) {})))
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
                  programme-id (get-in (match-route routes location) [:params :programme-id])]

              [id programme-id]
              )))))

(defn load-project-data
  [data {:keys [host port routes handlers programme-id-map]}]
  ;;(assert handler "Warning! no handler found")
  (into {}
        (for [project data]
          (let [id (:id project)
                ;; TODO Should we turn CSV keys into kebab form?
                programme-id (get programme-id-map (:programme_id project))

                path (if programme-id
                       (path-for routes (:projects handlers) :programme-id programme-id)
                       (path-for routes (:allprojects handlers)))

                response (post-resource
                           (format "http://%s:%d%s" host port path)
                           "application/edn"
                           ;; We have to update the simple numeric id
                           ;; with the generated sha1 id
                           (assoc project :programme_id programme-id))

                location (get-in response [:headers :location])
                project-id (get-in (match-route routes location) [:params :project-id])]

            [id project-id]
            ))))

(defn load-entity-data
  [data {:keys [host port routes handlers project-id-map]}]
  (into {}
        (for [entity data]
          (let [id (:id entity)
                ;; TODO Should we turn CSV keys into kebab form?
                project-id (get project-id-map (:project_id entity))

                path (if project-id
                       (path-for routes (:entities handlers) :project-id project-id)
                       (path-for routes (:allentities handlers)))

                response (post-resource
                           (format "http://%s:%d%s" host port path)
                           "application/json"
                           ;; We have to update the simple numeric id
                           ;; with the generated sha1 id
                           (assoc entity :project_id project-id))

                location (get-in response [:headers :location])
                entity-id (get-in (match-route routes location) [:params :entity-id])]

            [id entity-id]
            ))))

(defn jsonify [x]
  (reduce-kv
   (fn [s k v]
     (conj s
           [(->camelCaseString k)
            v]))
   {} x))

(defrecord CsvLoader [config]
  component/Lifecycle
  (start [this]
    (try
      (let [host "localhost"
            port (or (get-in config [:webserver :port]) 8000) ; TODO coupling!
            routes (-> this :bidi-ring-handler :routes)

            programme-map
            (-> (extract-data (io/file (-> config :etl :data-directory)) "programmes.csv")
                (load-programme-data {:host host :port port
                                      :routes routes
                                      :handler (-> this :amon-api :handlers :programmes)}))]

        (let [project-map
              (as-> (extract-data (io/file (-> config :etl :data-directory)) "projects.csv") %
                    (map #(dissoc % :leader_id) %)
                    (load-project-data % {:host host
                                          :port port
                                          :routes routes
                                          :handlers (-> this :amon-api :handlers)
                                          :programme-id-map programme-map}))]

          (let [entity-map
                (as-> (extract-data (io/file (-> config :etl :data-directory)) "properties.csv") %
                      (map #(assoc % :property_data (pr-str (select-keys % [:age]))) %)
                      (map #(apply dissoc % (map keyword (clojure.string/split "locked,uuid,description,monitoring_policy,address_street,address_city,address_county,address_code,created_at,updated_at,terrain,degree_day_region,ownership,fuel_poverty,property_value,property_value_basis,retrofit_start_date,retrofit_completion_date,project_summary,energy_strategy,project_team,design_strategy,other_notes,property_type,property_type_other,built_form,built_form_other,age,construction_date,conservation_area,listed,property_code,monitoring_hierarchy,project_phase,construction_start_date,practical_completion_date,photo_file_name,photo_content_type,photo_file_size,photo_updated_at,completeness,entity_completeness_6m,latitude,longitude,technology_icons,address_code_masked" #","))
                                   ) %)

                      (load-entity-data % {:host host
                                           :port port
                                           :routes routes
                                           :handlers (-> this :amon-api :handlers)
                                           :project-id-map project-map}))]

            ;; We'll create the device here.
            ;; Let's find a nice property


            (let [entity-id (get entity-map "64")]
              (doseq [device (generators/generate-device-sample entity-id 3)]
                (let [sensors (generators/generate-sensor-sample "CUMULATIVE" 3)

                      response
                      (post-resource
                       (format "http://%s:%d%s" host port
                               (path-for (-> this :bidi-ring-handler :routes)
                                         (-> this :amon-api :handlers :devices) :entity-id entity-id))
                       "application/json"


                       (jsonify (-> device
                                    (assoc :readings (map jsonify sensors)) ; TODO: make jsonify deep, then won't need to call it here
                                    (dissoc :device-id) ; Don't need device-id, it gets generated by liberator
                                    )))
                      ;; Take location, parse out entity-id and device-id
                      location (get-in response [:headers :location])
                      ;; Use entity-id and device-id to get measurements URL
                      measurements-uri (apply path-for routes (-> this :amon-api :handlers :measurements)
                                              (apply concat (:params (match-route routes location))))
                      ]

                  (assert (= (:status response) 201) (format "Failed to create device, status was %d" (:status response)))

                  ;; POST to URL a JSON block
                  (let [response
                        (post-resource (format "http://%s:%d%s" host port measurements-uri)
                                       "application/json"
                                       (jsonify {:measurements
                                                 (map
                                                  (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter  (clj-time.coerce/from-date %))))
                                                  (mapcat generators/measurements sensors))}))]
                    )

                  )))

          ;;;;; Insert measurements with readings above median ;;;;;;

            (let [entity-id (get entity-map "33")] ;; Woodbine Cottage
              (doseq [device (generators/generate-device-sample entity-id 3)]
                (let [sensors (generators/generate-sensor-sample "INSTANT" 3)

                      response
                      (post-resource
                       (format "http://%s:%d%s" host port
                               (path-for (-> this :bidi-ring-handler :routes)
                                         (-> this :amon-api :handlers :devices) :entity-id entity-id))
                       "application/json"

                       (jsonify (-> device
                                    (assoc :readings (map jsonify sensors)) ; TODO: make jsonify deep, then won't need to call it here
                                    (dissoc :device-id) ; Don't need device-id, it gets generated by liberator
                                    )))
                      ;; Take location, parse out entity-id and device-id
                      location (get-in response [:headers :location])
                      ;; Use entity-id and device-id to get measurements URL
                      measurements-uri (apply path-for routes (-> this :amon-api :handlers :measurements)
                                              (apply concat (:params (match-route routes location))))
                      ]

                                        ; (println "response to creating device is" response)

                  ;; POST to URL a JSON block
                  (let [response
                        (post-resource (format "http://%s:%d%s" host port measurements-uri)
                                       "application/json"
                                       (jsonify {:measurements
                                                 (map
                                                  (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                                  (mapcat generators/generate-measurements-above-median sensors))}))]))))


          ;;;;; Insert  measurements ;;;;;;

            (let [entity-id (get entity-map "38")] ;; Willow Cottage
              (doseq [device (generators/generate-device-sample entity-id 1)]
                (let [sensors1 (generators/generate-sensor-sample "CUMULATIVE" 2)
                      sensors2 (generators/generate-sensor-sample "PULSE" 1)
                      sensors3 (generators/generate-sensor-sample "INSTANT" 3)

                      ;; Mislabelled measurements
                      response1
                      (post-resource (format "http://%s:%d%s" host port
                                             (path-for (-> this :bidi-ring-handler :routes)
                                                       (-> this :amon-api :handlers :devices) :entity-id entity-id))
                                     "application/json"
                                     (jsonify (-> device
                                                  (assoc :readings (map jsonify sensors1))
                                                  (dissoc :device-id))))
                      location1 (get-in response1 [:headers :location])
                      measurements-uri1 (apply path-for routes (-> this :amon-api :handlers :measurements)
                                               (apply concat (:params (match-route routes location1))))

                      ;; Errored measurements
                      response2 (post-resource (format "http://%s:%d%s" host port
                                                       (path-for (-> this :bidi-ring-handler :routes)
                                                                 (-> this :amon-api :handlers :devices) :entity-id entity-id))
                                               "application/json"
                                               (jsonify (-> device
                                                            (assoc :readings (map jsonify sensors2))
                                                            (dissoc :device-id))))
                      location2 (get-in response2 [:headers :location])
                      measurements-uri2 (apply path-for routes (-> this :amon-api :handlers :measurements)
                                               (apply concat (:params (match-route routes location2))))

                      ;; Instant measurements
                      response3 (post-resource (format "http://%s:%d%s" host port
                                                       (path-for (-> this :bidi-ring-handler :routes)
                                                                 (-> this :amon-api :handlers :devices) :entity-id entity-id))
                                               "application/json"
                                               (jsonify (-> device
                                                            (assoc :readings (map jsonify sensors3))
                                                            (dissoc :device-id))))
                      location3 (get-in response3 [:headers :location])
                      measurements-uri3 (apply path-for routes (-> this :amon-api :handlers :measurements)
                                               (apply concat (:params (match-route routes location3))))

                      ]

                  ;; POST to URL a JSON block
                  (let [response1
                        (post-resource (format "http://%s:%d%s" host port measurements-uri1)
                                       "application/json"
                                       (jsonify {:measurements
                                                 (map
                                                  (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                                  (mapcat generators/mislabelled-measurements sensors1))}))
                        response2
                        (post-resource (format "http://%s:%d%s" host port measurements-uri2)
                                       "application/json"
                                       (jsonify {:measurements
                                                 (map
                                                  (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                                  (mapcat generators/generate-invalid-measurements sensors2))}))

                        response3
                        (post-resource (format "http://%s:%d%s" host port measurements-uri3)
                                       "application/json"
                                       (jsonify {:measurements
                                                 (map
                                                  (fn [x] (update-in x [:timestamp] #(tf/unparse custom-formatter (clj-time.coerce/from-date %))))
                                                  (mapcat generators/measurements sensors3))}))

                        ]))))))
        (let [commander (-> this :store :commander)
              querier   (-> this :store :querier)]
          
          (difference-series-batch commander querier {})
          (rollups commander querier {})
          ))
      this
      (catch Exception e
        (println "ETL failed:" e)
        (assoc this :error e))
      )

    )

  (stop [this] this))

(defn new-csv-loader [config]
  (->CsvLoader config))


;; Makes use of kixi.hecuba.users/ApiService to create some users
(defrecord UserDataLoader [config]
  component/Lifecycle
  (start [this]
    (let [host "localhost"
          port (or (get-in config [:webserver :port]) 8000) ; TODO coupling!
          routes (-> this :bidi-ring-handler :routes)]

      (doseq [{:keys [username password]} (:users config)]
        (let [body {:username username :password password}
              response
              (post-resource
               (format "http://%s:%d%s" host port "/users/")
               "application/edn"
               body
               )]
          (when (not= (:status response) 201)
            (println "Failed to add user:" body)
            (pprint response)))))
    this)
  (stop [this] this))

(defn new-user-data-loader [config]
  (->UserDataLoader config))
