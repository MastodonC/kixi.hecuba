(ns kixi.hecuba.dev.etl
  (:require
   [clojure.pprint :refer (pprint)]
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   clj-time.coerce
   [clj-time.format :as tf]
   [clj-time.core :as t]
   [bidi.bidi :refer (path-for match-route)]
   [org.httpkit.client :refer (request) :rename {request http-request}]
   [cheshire.core :refer (encode)]
   [kixi.hecuba.dev.generators :as generators]
   [camel-snake-kebab :refer (->camelCaseString)]
   jig)
  (:import (jig Lifecycle)))


(defn get-port [system]
  (-> system :jig/config :jig/components :hecuba/webserver :port))

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
    (when (>= (:status response) 400)
      (println "Error status returned on HTTP request")
      (pprint response)
      (throw (ex-info (format "Failed to post resource, status is %d" (:status response)) {})))
    response))

(defn extract-data
  [dir filename]
  (assert (.exists dir))
  (assert (.isDirectory dir))
  (let [programmes-file (io/file dir filename)]
    (assert (.exists programmes-file))
    (assert (.isFile programmes-file))
    (let [data (csv/read-csv (io/reader programmes-file))]
      (map zipmap (repeat (map keyword (first data))) (rest data)))))

(defn load-programme-data
  [data {:keys [host port routes handler]}]
  (assert handler "Warning! no handler found")
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

(deftype CsvLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [host "localhost"
          port (get-port user/system)
          routes (-> system :hecuba/routing :jig.bidi/routes)
          programme-map
          (-> (extract-data (io/file (:data-directory config)) "programmes.csv")
              (load-programme-data {:host host :port port
                                    :routes routes
                                    :handler (-> system :amon/handlers :programmes)}))]

      (let [project-map
            (as-> (extract-data (io/file (:data-directory config)) "projects.csv") %
                  (map #(dissoc % :leader_id) %)
                  (load-project-data % {:host "localhost" :port (get-port user/system)
                                        :routes routes
                                        :handlers (-> system :amon/handlers)
                                        :programme-id-map programme-map}))]

        (let [entity-map
              (as-> (extract-data (io/file (:data-directory config)) "properties.csv") %
                    (map #(assoc % :property_data (pr-str (select-keys % [:age]))) %)
                    (map #(apply dissoc % (map keyword (clojure.string/split "locked,uuid,description,monitoring_policy,address_street,address_city,address_county,address_code,created_at,updated_at,terrain,degree_day_region,ownership,fuel_poverty,property_value,property_value_basis,retrofit_start_date,retrofit_completion_date,project_summary,energy_strategy,project_team,design_strategy,other_notes,property_type,property_type_other,built_form,built_form_other,age,construction_date,conservation_area,listed,property_code,monitoring_hierarchy,project_phase,construction_start_date,practical_completion_date,photo_file_name,photo_content_type,photo_file_size,photo_updated_at,completeness,entity_completeness_6m,latitude,longitude,technology_icons,address_code_masked" #","))
                                 ) %)

                    (load-entity-data % { ;; TODO: Refactor me :(
                                         :host "localhost" :port (get-port user/system)
                                         :routes routes
                                         :handlers (-> system :amon/handlers)
                                         :project-id-map project-map}))]

          ;; We'll create the device here.
          ;; Let's find a nice property


          (let [entity-id (get entity-map "64")]
            (doseq [device (generators/generate-device-sample entity-id 3)]
              (let [sensors (generators/generate-sensor-sample "INSTANT" 3)

                    response
                    (post-resource
                      (format "http://%s:%d%s" host port
                              (path-for (-> system :hecuba/routing :jig.bidi/routes)
                                        (-> system :amon/handlers :devices) :entity-id entity-id))
                      "application/json"


                      (jsonify (-> device
                                   (assoc :readings (map jsonify sensors)) ; TODO: make jsonify deep, then won't need to call it here
                                   (dissoc :device-id) ; Don't need device-id, it gets generated by liberator
                                   )))
                    ;; Take location, parse out entity-id and device-id
                    location (get-in response [:headers :location])
                    ;; Use entity-id and device-id to get measurements URL
                    measurements-uri (apply path-for routes (-> system :amon/handlers :measurements)
                                            (apply concat (:params (match-route routes location))))
                    ]

                (assert (= (:status response) 201) (format "Failed to create device, status was %d" (:status response)))

                ;; POST to URL a JSON block
                #_(let [response
                      (post-resource (format "http://%s:%d%s" host port measurements-uri)
                                      "application/json"
                                      (jsonify {:measurements
                                                (map
                                                 (fn [x] (update-in x [:timestamp] #(tf/unparse (:date-time-no-ms tf/formatters)  (clj-time.coerce/from-date %))))
                                                 (mapcat generators/measurements sensors))}))]
                ;  (println "response to adding measurements to device is" response)
                  )

                )))

          ;;;;; Insert invalid measurements ;;;;;;

          (let [entity-id (get entity-map "33")] ;; Woodbine Cottage
            (doseq [device (generators/generate-device-sample entity-id 3)]
              (let [sensors (generators/generate-sensor-sample "INSTANT" 3)

                    response
                    @(post-resource
                      (format "http://%s:%d%s" host port
                              (path-for (-> system :hecuba/routing :jig.bidi/routes)
                                        (-> system :amon/handlers :devices) :entity-id entity-id))
                      "application/json"


                      (jsonify (-> device
                                   (assoc :readings (map jsonify sensors)) ; TODO: make jsonify deep, then won't need to call it here
                                   (dissoc :device-id) ; Don't need device-id, it gets generated by liberator
                                   )))
                    ;; Take location, parse out entity-id and device-id
                    location (get-in response [:headers :location])
                    ;; Use entity-id and device-id to get measurements URL
                    measurements-uri (apply path-for routes (-> system :amon/handlers :measurements)
                                            (apply concat (:params (match-route routes location))))
                    ]

              ;  (println "response to creating device is" response)

                ;; POST to URL a JSON block
                (let [response
                      @(post-resource (format "http://%s:%d%s" host port measurements-uri)
                                      "application/json"
                                      (jsonify {:measurements
                                                (map
                                                 (fn [x] (update-in x [:timestamp] #(tf/unparse (:date-time-no-ms tf/formatters)  (clj-time.coerce/from-date %))))
                                                 (mapcat generators/generate-measurements-above-median sensors))}))]
                 ; (println "response to adding measurements to device is" response)
                  )

                )))


          ;;;;; Insert mislabelled measurements ;;;;;;

          (let [entity-id (get entity-map "38")] ;; Willow Cottage
            (doseq [device (generators/generate-device-sample entity-id 3)]
              (let [sensors (generators/generate-sensor-sample "CUMULATIVE" 3)

                    response
                    @(post-resource
                      (format "http://%s:%d%s" host port
                              (path-for (-> system :hecuba/routing :jig.bidi/routes)
                                        (-> system :amon/handlers :devices) :entity-id entity-id))
                      "application/json"


                      (jsonify (-> device
                                   (assoc :readings (map jsonify sensors)) ; TODO: make jsonify deep, then won't need to call it here
                                   (dissoc :device-id) ; Don't need device-id, it gets generated by liberator
                                   )))
                    ;; Take location, parse out entity-id and device-id
                    location (get-in response [:headers :location])
                    ;; Use entity-id and device-id to get measurements URL
                    measurements-uri (apply path-for routes (-> system :amon/handlers :measurements)
                                            (apply concat (:params (match-route routes location))))
                    ]

              ;  (println "response to creating device is" response)

                ;; POST to URL a JSON block
                (let [response
                      @(post-resource (format "http://%s:%d%s" host port measurements-uri)
                                      "application/json"
                                      (jsonify {:measurements
                                                (map
                                                 (fn [x] (update-in x [:timestamp] #(tf/unparse (:date-time-no-ms tf/formatters)  (clj-time.coerce/from-date %))))
                                                 (mapcat generators/generate-measurements-above-median sensors))}))]
                 ; (println "response to adding measurements to device is" response)
                  )

                )))


                ))))

  (stop [_ system] system))

;; Makes use of kixi.hecuba.users/ApiService to create some users
(deftype UserDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [host "localhost"
          port (get-port user/system)
          routes (-> system :hecuba/routing :jig.bidi/routes)
          ]

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
    system)
  (stop [_ system] system))
