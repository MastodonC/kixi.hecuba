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
            [kixi.hecuba.storage.db :as db]
            [org.httpkit.client :refer (request) :rename {request http-request}]
            [qbits.hayt :as hayt]
            [etl.url :refer (urls resources)]))

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
  (doseq [batch (partition-all 500 measurements)]
    (post-measurements batch url)))

(defn post-contextual-data 
  "Creates test programme, project and devices which are used to post measurements.
  Uses the API."
  [store]
  (post-resource (:programme urls) "application/json" {:name "TEST102"})
  (post-resource (:project urls) "application/json" {:name "TESTPROJECT" :programme_id "6216349fb60ada047e5218dbe7efd68f6f937862"})
  (post-resource (:entity urls) "application/json" {:project_id "ba776928f94b3aaa1e444569276ee5b66d6b21f7"
                                                    :property_code "3e49de0-12af-012e-4f3a-12313b0348f8"})
  (post-resource (:device-1 urls) "application/json" (:device-1 resources))

  (db/with-session [session (:hecuba-session store)]
    (db/execute session 
                (hayt/insert :entities (hayt/values {:id "14366c761c74592b9926e851bae8a64ece7239ff"
                                                     :project_id "ba776928f94b3aaa1e444569276ee5b66d6b21f7"})))
    (db/execute session 
                (hayt/insert :entities (hayt/values {:id "9ac7f5635832d843dda594f58525239263ffdd37"
                                                     :project_id "ba776928f94b3aaa1e444569276ee5b66d6b21f7"}))))
  
  (post-resource (:device-2 urls) "application/json" (:device-2 resources))
  (post-resource (:device-3 urls) "application/json" (:device-3 resources))
  (post-resource (:device-3 urls) "application/json" (:device-4 resources))
  (post-resource (:device-3 urls) "application/json" (:device-5 resources))
  (post-resource (:device-3 urls) "application/json" (:device-6 resources)))

(defn post-readings
  "Reads in CSV files containing measurements and posts them in batches through the API."
  []
  (with-open [in-file (io/reader (io/resource "csv/gasConsumption-fe5ab5bf19a7265276ffe90e4c0050037de923e2.csv"))]
    (let [measurements2 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :value] %)
                             (rest (csv/read-csv in-file)))]
      (batch-csv measurements2 (:measurement-2 urls))))
  
  (with-open [in-file2 (io/reader (io/resource "csv/b4f0c7e2b15ba9636f3fb08379cc4b3798a226bb-interpolatedHeatConsumption.csv"))]
    (let [measurements3 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :metadata :value] %)
                             (rest (csv/read-csv in-file2)))
          m3 (map #(update-in % [:timestamp] (fn [t] (let [d (tf/parse (tf/formatter  "yyyy-MM-dd HH:mm:ssZ") t)]
                                                     (tf/unparse 
                                                      (tf/formatter "yyyy-MM-dd HH:mm:ssZ")
                                                      (t/minus (t/plus d (t/years 2)) (t/days 9))))))
                  measurements3)]
      (batch-csv m3 (:measurement-3 urls))))

  (with-open [in-file3 (io/reader (io/resource "csv/268e93a5249c24482ac1519b77f6a45f36a6231d-interpolatedElectricityConsumption.csv"))]
    (let [measurements4 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :metadata :value] %)
                             (rest (csv/read-csv in-file3)))
           m4 (map #(update-in % [:timestamp] (fn [t] (let [d (tf/parse (tf/formatter  "yyyy-MM-dd HH:mm:ssZ") t)]
                                                      (tf/unparse 
                                                       (tf/formatter "yyyy-MM-dd HH:mm:ssZ")
                                                       (t/minus (t/plus d (t/years 2)) (t/days 9))))))
                   measurements4)]
      (batch-csv m4 (:measurement-4 urls))))

  (with-open [in-file4 (io/reader (io/resource "csv/3aae0fe7ddaa5e400a2cf9580a5e548d9255c70a-interpolatedElectricityConsumption.csv"))]
    (let [measurements5 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :value] %)
                             (rest (csv/read-csv in-file4)))]
      (batch-csv measurements5 (:measurement-5 urls))))
  
  (with-open [in-file5 (io/reader (io/resource "csv/122b4bf8dfa66cbf8f321f2d03c4d73f924bb719-interpolatedElectricityConsumption.csv"))]
    (let [measurements6 (map #(zipmap [:device_id :type :month :timestamp :error :reading_metadata :value] %)
                             (rest (csv/read-csv in-file5)))]
      (batch-csv measurements6 (:measurement-6 urls)))))


(defn post-generated-measurements 
  "Generates measurements (500) and posts them to Hecuba"
  []
  (let [generated-measurements (generators/measurements  {:type "electricityConsumption"
                                                          :unit "kWh"
                                                          :resolution 60
                                                          :period "PULSE"})]
    (post-resource (:measurement-1 urls) "application/json" {:measurements
                                                             (map (fn [x] (update-in x [:timestamp]
                                                                                     #(tf/unparse custom-formatter (tc/from-date %))))
                                                                  generated-measurements)})))

(defn load-test-data [system]
  (let [store (:store system)]
    
    (post-contextual-data store)
    (post-readings)
    (post-generated-measurements)))

;; To load data (load-test-data system)

