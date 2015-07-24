(ns etl
  (:require [cheshire.core :refer (encode)]
            [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.java.io :as io]
            [clojure.pprint :refer (pprint)]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.reader-types :refer (indexing-push-back-reader)]
            [clojure.walk :as walk]
            [generators :as generators]
            [org.httpkit.client :refer (request) :rename {request http-request}]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [kixi.hecuba.security :as sec]
            [kixipipe.application]))

(def credentials
  {:username "support@mastodonc.com" :password "password"})

(defn- config []
  (let [f (io/file (System/getProperty "user.home") ".hecuba.edn")]
    (when (.exists f)
      (clojure.tools.reader/read
       (indexing-push-back-reader
        (java.io.PushbackReader. (io/reader f)))))))

(def custom-formatter (tf/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))

(defn update-reading-metadata [metadata]
  (when metadata
    (let [new-metadata (walk/stringify-keys (read-string metadata))]
      new-metadata)))

(defn post-resource [post-uri content-type data]
  (let [response
        @(http-request
          {:method :post
           :url post-uri
           :headers {"Content-Type" content-type}
           :basic-auth ((juxt :username :password) credentials)
           :body (case content-type
                   "application/edn" (pr-str data)
                   "application/json" (encode data))}
          identity)]
    (assert (:status response) (format "Failed to connect to %s!" post-uri))
    (when (>= (:status response) 399)
      (println "Error status returned on HTTP request")
      (throw (ex-info (format "Failed to post resource, status is %d." (:status response)) {})))
    (:body response)))

(defn db-timestamp
  "Converts tiemstamps in CSV to db format" ; 2014-01-01 00:00:10+0000
  [t] (tf/unparse (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ") (tf/parse (tf/formatter  "yyyy-MM-dd HH:mm:ssZ") t)))

(defn read-response [response]
  (json/read-str response :key-fn keyword))

(defn post-generated-data
  "Generates contextual data and measurements (500) and posts them to Hecuba using the API."
  []
  (let [hostname              "http://127.0.0.1:8010"
        ;; PROGRAMME
        programme             {:name "TEST001"}
        programme_url         (:location (read-response (post-resource (str hostname "/4/programmes/")
                                                                       "application/json" programme)))
        [_ _ _ programme_id]  (string/split programme_url #"/")
        ;; PROJECT
        project               {:name "TESTPROJECT" :programme_id programme_id}
        project_url           (:location (read-response (post-resource (str hostname programme_url "/projects/")
                                                                       "application/json" project)))
        [_ _ _ project_id]    (string/split project_url #"/")
        ;; ENTITY
        entity                {:project_id project_id
                               :property_code "1a2b3c4d"}
        entity_url            (:location (:body (read-response (post-resource (str hostname "/4/entities/")
                                                                       "application/json" entity))))
        [_ _ _ entity_id]     (string/split entity_url #"/")
        ;; DEVICE
        device                {:description "External air temperature sensor"
                               :entity_id entity_id
                               :readings [{:type "electricityConsumption"
                                           :unit "kWh"
                                           :resolution "60"
                                           :period "CUMULATIVE"}
                                          {:type "temperature"
                                           :unit "C"
                                           :resolution "60"
                                           :period "PULSE"}]}
        device-response      (read-response (post-resource (str hostname entity_url "/devices/")
                                                           "application/json" device))
        device-url           (:location device-response)
        ;; MEASUREMENTS
        measurements1        (generators/measurements  {:type "electricityConsumption"
                                                        :unit "kWh"
                                                        :resolution 60
                                                        :period "CUMULATIVE"})
        measurements2        (generators/measurements  {:type "temperature"
                                                        :unit "C"
                                                        :resolution 60
                                                        :period "PULSE"})]
    (post-resource (str hostname device-url "/measurements/")
                   "application/json" {:measurements
                                       (map (fn [x]
                                              (-> x
                                                  (dissoc :reading_metadata :error)
                                                  (update-in [:timestamp]
                                                             #(tf/unparse custom-formatter (tc/from-date %)))))
                                            measurements1)})
    (post-resource (str hostname device-url "/measurements/")
                   "application/json" {:measurements
                                       (map (fn [x]
                                              (-> x
                                                  (dissoc :reading_metadata :error)
                                                  (update-in [:timestamp]
                                                             #(tf/unparse custom-formatter (tc/from-date %)))))
                                            measurements2)})))

(defn upsert-test-user
  []
  (sec/add-user!
   (:store kixipipe.application/system)
   "Mastodon"
   (:username credentials)
   (:password credentials)
   :kixi.hecuba.security/super-admin #{} #{} ))

(defn load-test-data []
  (upsert-test-user)
  (post-generated-data))

;; To load data (load-test-data)
