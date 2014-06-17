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
            [generators :as generators]
            [etl.fixture :as fixture]
            [kixi.hecuba.data.calculate :as calc]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.webutil :as util]
            [org.httpkit.client :refer (request) :rename {request http-request}]
            [qbits.hayt :as hayt]
            [kixi.hecuba.web-paths :as p]))

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

(defn- get-credentials []
  (let [cred (first (filter #(= "mastodon" (:name %)) (:users (config))))]
    [(:username cred) (:password cred)]))

(defn post-resource [post-uri content-type data]
  (pr-str "DATA:" data)
  (let [response
        @(http-request
          {:method :post
           :url post-uri
           :headers {"Content-Type" content-type}
           :basic-auth (get-credentials)
           :body (case content-type
                   "application/edn" (pr-str data)
                   "application/json" (encode data))}
          identity)]
    (assert (:status response) (format "Failed to connect to %s!" post-uri))
    (when (>= (:status response) 399)
      (println "Error status returned on HTTP request")
      (throw (ex-info (format "Failed to post resource, status is %d." (:status response)) {})))
    nil))

(defn db-timestamp
  "Converts tiemstamps in CSV to db format" ; 2014-01-01 00:00:10+0000
  [t] (tf/unparse (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ") (tf/parse (tf/formatter  "yyyy-MM-dd HH:mm:ssZ") t)))

(defn post-measurements [measurements url]
  (post-resource url "application/json"
                 {:measurements
                  (into [] (map #(-> %
                                     (dissoc :device_id :month :metadata :reading_metadata (when (= "null" (:error %)) :error))
                                     (update-in [:timestamp] db-timestamp)) measurements))}))

(defn batch-csv [measurements url]
  (loop [m measurements]
    (post-measurements (take 500 m) url)
    (when-let [batch (seq (drop 500 m))]
      (recur batch))))

(defn load-test-data [system]
  
  (let [programme-url    "http://127.0.0.1:8010/4/programmes/"
        project-url      "http://127.0.0.1:8010/4/programmes/6216349fb60ada047e5218dbe7efd68f6f937862/projects/"
        entity-url       "http://127.0.0.1:8010/4/entities/"
        device-url       "http://127.0.0.1:8010/4/entities/821e6367f385d82cc71b2afd9dc2df3b2ec5b81c/devices/"
        device-url2      "http://127.0.0.1:8010/4/entities/14366c761c74592b9926e851bae8a64ece7239ff/devices/"
        device-url3      "http://127.0.0.1:8010/4/entities/9ac7f5635832d843dda594f58525239263ffdd37/devices/"
        measurement-url  "http://127.0.0.1:8010/4/entities/821e6367f385d82cc71b2afd9dc2df3b2ec5b81c/devices/8c077c2c3eac472d153886244e7b8aa6cad6a7e7/measurements/"
        measurement-url2 "http://127.0.0.1:8010/4/entities/14366c761c74592b9926e851bae8a64ece7239ff/devices/fe5ab5bf19a7265276ffe90e4c0050037de923e2/measurements/"
        measurement-url3 "http://127.0.0.1:8010/4/entities/9ac7f5635832d843dda594f58525239263ffdd37/devices/b4f0c7e2b15ba9636f3fb08379cc4b3798a226bb/measurements/"
        measurement-url4 "http://127.0.0.1:8010/4/entities/9ac7f5635832d843dda594f58525239263ffdd37/devices/268e93a5249c24482ac1519b77f6a45f36a6231d/measurements/"
        measurements (generators/measurements  {:type "electricityConsumption"
                                                :unit "kWh"
                                                :resolution 60
                                                :period "PULSE"})]
    (post-resource programme-url "application/json" {:name "TEST102"})
    (post-resource project-url "application/json" {:name "TESTPROJECT" :programme_id "6216349fb60ada047e5218dbe7efd68f6f937862"})
    (post-resource project-url "application/json" {:name "TESTPROJECT" :programme_id "6216349fb60ada047e5218dbe7efd68f6f937862"})
    (post-resource entity-url "application/json" {:project_id "ba776928f94b3aaa1e444569276ee5b66d6b21f7"
                                                  :property_code "3e49de0-12af-012e-4f3a-12313b0348f8"})
    (post-resource device-url "application/json" {:entity_id "821e6367f385d82cc71b2afd9dc2df3b2ec5b81c"
                                                  :description "External air temperature sensor"
                                                  :readings [{:type "electricityConsumption"
                                                              :unit "kWh"
                                                              :resolution "60"
                                                              :period "PULSE"}]})
    (post-resource measurement-url "application/json" {:measurements
                                             (map (fn [x] (update-in x [:timestamp]
                                                                     #(tf/unparse custom-formatter (tc/from-date %))))
                                                  measurements)})
    (db/with-session [session (:hecuba-session (:store system))]
      (db/execute session 
                  (hayt/insert :entities (hayt/values {:id "14366c761c74592b9926e851bae8a64ece7239ff"
                                                       :project_id "ba776928f94b3aaa1e444569276ee5b66d6b21f7"})))
      (db/execute session 
                  (hayt/insert :entities (hayt/values {:id "9ac7f5635832d843dda594f58525239263ffdd37"
                                                       :project_id "ba776928f94b3aaa1e444569276ee5b66d6b21f7"}))))

    (post-resource device-url2 "application/json" {:entity_id "14366c761c74592b9926e851bae8a64ece7239ff"
                                                   :description "GasMeterPulse"
                                                   :readings [{:type "gasConsumption"
                                                               :unit "m^3"
                                                               :resolution "60"
                                                               :period "PULSE"}]})
    (post-resource device-url3 "application/json" {:entity_id "9ac7f5635832d843dda594f58525239263ffdd37"
                                                   :description "Heat meter (overall)"
                                                   :readings [{:type "interpolatedHeatConsumption"
                                                               :unit ""
                                                               :resolution "60"
                                                               :period "CUMULATIVE"}]})
    (post-resource device-url3 "application/json" {:entity_id "9ac7f5635832d843dda594f58525239263ffdd37"
                                                   :description "Heat pump electricity meter"
                                                   :readings [{:type "interpolatedElectricityConsumption"
                                                               :unit ""
                                                               :resolution "60"
                                                               :period "CUMULATIVE"}]})
    
    (with-open [in-file (io/reader (io/resource "gasConsumption-fe5ab5bf19a7265276ffe90e4c0050037de923e2.csv"))]
      (let [measurements2 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :value] %)
                               (rest (csv/read-csv in-file)))]
        (batch-csv measurements2 measurement-url2)))

    (with-open [in-file2 (io/reader (io/resource "b4f0c7e2b15ba9636f3fb08379cc4b3798a226bb-interpolatedHeatConsumption.csv"))]
      (let [measurements3 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :metadata :value] %)
                               (rest (csv/read-csv in-file2)))]
        (batch-csv measurements3 measurement-url3)))

     (with-open [in-file3 (io/reader (io/resource "268e93a5249c24482ac1519b77f6a45f36a6231d-interpolatedElectricityConsumption.csv"))]
      (let [measurements4 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :metadata :value] %)
                               (rest (csv/read-csv in-file3)))]
        (batch-csv measurements4 measurement-url4)))))

;; To load data (load-test-data system)
