(ns kixi.hecuba.data-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.dev :refer (create-ref-store ->CassandraDirectCommander ->CassandraQuerier sha1-keyfn)]
            [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
            [kixi.hecuba.db :as db]
            [clojurewerkz.cassaforte.client :as client]
            [clojurewerkz.cassaforte.query :as query]
            [clojurewerkz.cassaforte.cql :as cql]
            [simple-check.generators :as gen]                                   
            [roul.random :as rr]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [clj-time.periodic :as periodic]
            [clojure.data.json :as json]))

;; Helpers

(defn uuid [] (java.util.UUID/randomUUID))

(defn transform-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m]
         (assoc-in m [:value] (read-string (:value m))))
       measurements))

;;;;;;;;;;;;;; Generate sensors ;;;;;;;;;;;;;;;

(defn type-gen []
  (gen/elements ["relativeHumidity" "temperature" "electricityConsumption" "gasAsHeatingFuel"]))

(defn generate-sensor-sample [device-id period n]
  (take n (repeatedly n #(hash-map :sensor-id (str (uuid))
                                   :device-id device-id
                                   :type (first (gen/sample (type-gen) 1)) 
                                   :unit ""
                                   :resolution ""
                                   :accuracy ""
                                   :period (first (gen/sample (gen/elements [period])))
                                   :min ""
                                   :max ""
                                   :correction ""
                                   :correctedUnit ""
                                   :correctionFactor ""
                                   :correctionFactorBreakdown ""
                                   :events 0 
                                   :errors 0))))

;;;;;;;;;;;;;;; Generate devices ;;;;;;;;;;;;;;

(def location-gen
  (gen/hash-map :name gen/string-alpha-numeric
                :latitude gen/string-alpha-numeric
                :longitude gen/string-alpha-numeric))

(defn generate-device-sample
  [n]
  (take n (repeatedly n
                      #(hash-map :device-id (str (uuid))
                                 :description (first (gen/sample gen/string 1)) 
                                 :parent-id (uuid)
                                 :entity-id (uuid)
                                 :location (json/write-str (first (gen/sample location-gen 1)))
                                 :metadata ""
                                 :privacy (first (gen/sample (gen/not-empty (gen/elements ["public" "private"])) 1)) 
                                 :meteringPointId (uuid)))))

;;;;;;;;;;;;;;;;; Generate measurements ;;;;;;;;;;;;;;;;

(defn timestamps [frequency]
  (into [] (take 50 (periodic/periodic-seq (t/now) frequency))))

(defn get-month [t]
  (format "%4d-%02d" (t/year t) (t/month t)))

;; Standard measurements

(defmulti generate-measurements
  "Dispatches a call to a specific device period and generates
  appropriate measurements."
  (fn [sensor]
    (:period sensor)))

(defmethod generate-measurements "INSTANT"
  [sensor]
    (let [timestamps (timestamps (t/minutes 5))
          type       (:type sensor)]
    (map #(hash-map :device-id (:device-id sensor)
                    :type type
                    :month (get-month %)
                    :timestamp (tc/to-date %)
                    :value (str (rand 150))
                    :error "false") timestamps)))

(defmethod generate-measurements "PULSE"
  [sensor]
  (let [timestamps (timestamps (t/hours 2))
        type       (:type sensor)]
    (map #(hash-map :device-id (:device-id sensor) 
                    :type type
                    :month (get-month %)
                    :timestamp (tc/to-date %)
                    :value (str (rand-int 100))
                    :error "false") timestamps)))

(defmethod generate-measurements "CUMULATIVE"
  [sensor]
   (let [timestamps (timestamps (t/minutes 15))
         type       (:type sensor)]
     (map-indexed (fn [i t] (hash-map :device-id (:device-id sensor) 
                                      :type type
                                      :month (get-month t)
                                      :timestamp (tc/to-date t)
                                      :value (str i)
                                      :error "false")) timestamps)))

(defn measurements
  "Iterates through all sensors and generates appropriate
  measurements. Returns a list of maps."
  [sensor]
  (generate-measurements sensor))

;; Invalid measurements
(defn generate-measurements-above-median
  "Generates measurements that contain readings 200 x median."
  [sensor]
  (let [timestamps (timestamps (t/minutes 5))
        device-id  (:device_id sensor)
        type       (:type sensor)]
    (map-indexed (fn [i t] (hash-map :device_id device-id
                                     :type type
                                     :month (get-month t)
                                     :timestamp t
                                     :value (str (if (= 0 (mod i 50)) (* 300 (rr/rand-gaussian-int)) (rr/rand-gaussian-int)))
                                     :error "false")) timestamps)))

(defn generate-invalid-measurements
  "Generates measurements that contain invalid readings."
  [sensor]
  (let [timestamps (timestamps (t/minutes 5))
        device-id  (:device_id sensor)
        type       (:type sensor)]
    (map-indexed (fn [i t] (merge {:device_id device-id
                                   :type type
                                   :month (get-month t)
                                   :timestamp (tc/to-date t)}
                                  (if (= 0 (mod i 5))
                                    {:value "Invalid reading"
                                     :error "true"}
                                    {:value (str (rand-int 10))
                                     :error "false"}))) timestamps)))

(defn mislabelled-measurements
  "Generates mislabelled measurements
   for a given sensor."
  [sensor period]
  (let [sensor (assoc-in sensor [:period] period)]
    (generate-measurements sensor)))



;;;;;;;;;;; Tests with Cassandra ;;;;;;;;;;;

(defn converge-db
  "Creates database schema."
  [session]
  (binding [client/*default-session* session]
    (cql/create-table "devices"
                      (query/column-definitions {:device_id :varchar
                                                 :description :varchar
                                                 :parent_id :uuid
                                                 :entity_id :uuid
                                                 :location :varchar
                                                 :metadata :varchar
                                                 :sensors :varchar
                                                 :privacy :varchar
                                                 :meteringPointId :uuid
                                                 :primary-key [:device_id]}))
    (cql/create-index "devices" :entity_id)

    (cql/create-table "sensors"
                      (query/column-definitions {:sensor_id :varchar
                                                 :device_id :varchar
                                                 :type :varchar 
                                                 :unit :varchar
                                                 :resolution :varchar
                                                 :accuracy :varchar
                                                 :period :varchar
                                                 :min :varchar
                                                 :max :varchar
                                                 :correction :varchar
                                                 :correctedUnit :varchar
                                                 :correctionFactor :varchar
                                                 :correctionFactorBreakdown :varchar
                                                 :events :int
                                                 :errors :int
                                                 :status :varchar
                                                 :primary-key [:device_id :type]}))

    (cql/create-table "measurements"
                      (query/column-definitions {:device_id :varchar
                                                 :type :varchar
                                                 :month :varchar
                                                 :timestamp :timestamp
                                                 :value :varchar
                                                 :error :varchar
                                                 :primary-key [[:device_id :type :month] :timestamp]}))))

(defn delete-schema
  "Drops all tables"
  [session]
  (binding [client/*default-session* session]
    (cql/drop-table "devices")
    (cql/drop-table "sensors")
    (cql/drop-table "measurements")))

(defn validate-and-insert
  "Updates appropriate counters and inserts data."
  [querier commander measurement]
  (let [t (tf/parse (:date-time-no-ms tf/formatters (get measurement "timestamp")))
        m {:device-id (get measurement "device-id")
           :type (get measurement "type")
           :timestamp (tc/to-date t)
           :value (get measurement "value")
           :error (get measurement "error")
           :month (get-month t)}])
  (v/validate-measurement querier commander  measurement)
  (upsert! commander :measurement measurement))

(defn insert-measurements
  [querier commander measurements]
  (doseq [m measurements] (validate-and-insert querier commander m)))

(defn insert-sensors
  [session sensors]
  (binding [client/*default-session* session]
    (doseq [s sensors] (cql/insert "sensors" s))))

(defn insert-devices
  [session devices]
  (binding [client/*default-session* session]
    (doseq [d devices] (cql/insert "devices" d))))

(defn get-devices
  "Retrieves a map of n devices from the database."
  [n]
  (let [session (-> user/system :cassandra :session)]
    (binding [client/*default-session* session]
      (cql/select "devices" (query/limit n)))))

(defn get-measurements
  [device_id sensor-type month]
  (let [session (-> user/system :cassandra :session)]
    (binding [client/*default-session* session]
      (cql/select "measurements" (query/where :device_id device_id
                                              :type sensor-type
                                              :month month)))))

(deftest db-tests
  ;; TODO Should create and use separate keyspace for tests.
  (let [session   (get-in user/system [:cassandra :session])
        commander (->CassandraDirectCommander (get-in user/system [:cassandra :session]))
        querier   (->CassandraQuerier (get-in user/system [:cassandra :session]))]

    (delete-schema session) ;; C* 1.2 does not support "IF (NOT) EXISTS"
    (converge-db session)

   (testing "Cumulative devices should be labelled correctly."
      (let [devices (generate-device-sample 5)
            sensors (reduce concat (map #(generate-sensor-sample (:device_id %) "CUMULATIVE" 1) devices))]
        (println "Testing CUMULATIVE sensors - correct labels.")
        (insert-devices session devices)
        (insert-sensors session sensors)
        (doseq [s sensors]
          (println "Inserting measurements for sensor: " (:sensor_id s))
          (insert-measurements querier commander (measurements s))
          (is (v/labelled-correctly? s (sort-by :timestamp (filter #(= (:type s) (:type %))
                                                                   (transform-measurements
                                                                    (get-measurements (:device_id s) (:type s) "2014-2"))))))))) 
    (testing "Cumulative devices should be mislabelled."
      (let [devices (generate-device-sample 5)
            sensors (reduce concat (map #(generate-sensor-sample (:device_id %) "CUMULATIVE" 1) devices))]
        (println "Testing CUMULATIVE devices - mislabelled.")
        (insert-devices session devices)
        (insert-sensors session sensors)
        (doseq [s sensors]
          (println "Inserting measurements for sensor: " (:sensor_id s))
          (insert-measurements querier commander (mislabelled-measurements s "INSTANT"))
          (is (= false (v/labelled-correctly? s 
                                              (sort-by :timestamp
                                                       (filter #(= (:type s) (:type %))
                                                               (transform-measurements
                                                                (get-measurements (:device_id s) (:type s) "2014-2"))))))))))

    (testing "Should increment error counter."
      (let [devices (generate-device-sample 5)
            sensors (reduce concat (map #(generate-sensor-sample (:device_id %) "INSTANT" 1) devices))]
        (println "Testing errored measurements: INSTANT")
        (insert-devices session devices)
        (insert-sensors session sensors)
        (doseq [s sensors]
          (println "Inserting measurements for sensor: " (:sensor_id s))
          (insert-measurements querier commander (generate-invalid-measurements s))
          (is (= 10 (:errors (db/get-counter session (:device_id s) (:type s) "errors"))))
          (is (= "broken" (:status (db/get-sensor-status session (:type s) (:device_id s))))))))))


;;;;;;;;;;;; Simple check tests (no C*) ;;;;;;;;;;;;

;;; Mislabelled devices ;;;

(deftest mislabelled-devices
  (let [devices            (generate-device-sample 5)
        cumulative-sensors (reduce concat (map #(generate-sensor-sample (:device_id %) "CUMULATIVE" 1) devices))
        pulse-sensors      (reduce concat (map #(generate-sensor-sample (:device_id %) "PULSE" 1) devices))
        instant-sensors    (reduce concat (map #(generate-sensor-sample (:device_id %) "INSTANT" 2) devices))]

    (testing "Mislabelled devices"
      (println "Testing mislabelled devices.")
         
      (doseq [sensor cumulative-sensors]
        (is (v/labelled-correctly? sensor
                                   (sort-by :timestamp  (filter #(= (:type sensor) (:type %))
                                                                (transform-measurements (measurements sensor)))))))
     
      (doseq [sensor cumulative-sensors]
        (is (= false (v/labelled-correctly? sensor
                                            (sort-by :timestamp (filter #(= (:type sensor) (:type %))
                                                                        (transform-measurements 
                                                                         (mislabelled-measurements sensor "INSTANT"))))))))

      (doseq [sensor instant-sensors]
        (is (v/labelled-correctly? sensor 
                                   (filter #(= (:type sensor) (:type %))
                                           (transform-measurements (measurements sensor))))))

      (doseq [sensor pulse-sensors]
        (is (v/labelled-correctly? sensor
                                   (filter #(= (:type sensor) (:type %))
                                           (transform-measurements (measurements sensor))))))

      (doseq [sensor pulse-sensors]
        (is (= false (v/labelled-correctly? sensor
                                            (filter #(= (:type sensor) (:type %))
                                                    (transform-measurements (mislabelled-measurements sensor "INSTANT"))))))))))

;;; 200 x median ;;;

(defn less-than-200x-median-stream?
  "Returns true if there are no measurements that are greater than their median.
  Works in a stream."
  [measurements]
  (empty? (v/larger-than-median (v/median-stream measurements) measurements)))

(defn less-than-200x-median?
  "Returns true if there are no measurements that are greater than their median."
  [measurements]
  (empty? (v/larger-than-median (v/median measurements) measurements)))

(deftest large-median
  (let [devices            (generate-device-sample 1)
        cumulative-sensors (reduce concat (map #(generate-sensor-sample (:device_id %) "CUMULATIVE" 1) devices))
        pulse-sensors      (reduce concat (map #(generate-sensor-sample (:device_id %) "PULSE" 1) devices))
        instant-sensors    (reduce concat (map #(generate-sensor-sample (:device_id %) "INSTANT" 1) devices))]

    (println "Testing large median.")

    (testing "Should find readings that are 200 x median in a stream"
      (doseq [sensor instant-sensors]
        (is (= false (less-than-200x-median-stream? (transform-measurements (generate-measurements-above-median
                                                                               sensor)))))))

    (testing "Should find no readings that are 200 x median in a stream"
      (doseq [sensor cumulative-sensors]
        (is (less-than-200x-median-stream? (sort-by :timestamp (transform-measurements (measurements sensor)))))))

    (testing "Should find readings that are 200 x median"
      (doseq [sensor instant-sensors]
        (is (= false (less-than-200x-median? (transform-measurements (generate-measurements-above-median 
                                                                        sensor)))))))

   (testing "Should find no readings that are 200 x median"
      (doseq [sensor cumulative-sensors]
        (is (less-than-200x-median? (transform-measurements (measurements sensor))))))))



