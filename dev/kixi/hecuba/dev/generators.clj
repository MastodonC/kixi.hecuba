(ns kixi.hecuba.dev.generators
  (:refer-clojure :exclude [rand-int])
  (:require [roul.random :as rr]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [clj-time.periodic :as periodic]
            [simple-check.generators :as gen]
            [clojure.data.json :as json]

            [clojurewerkz.cassaforte.client :as client]
            [clojurewerkz.cassaforte.query :as query]
            [clojurewerkz.cassaforte.cql :as cql]))

;; Malcolm says: Sorry Anna, but I really need some predicatability to
;; work on the chart integration - so I'm seeding the randomness to it
;; always produces the same results.
(def rnd (java.util.Random. 1234))
(defn rand-int [n] (.nextInt rnd n))

(defn uuid [] (java.util.UUID/randomUUID))

;;;;;;;;;;;;;; Generate sensors ;;;;;;;;;;;;;;;

(defn type-gen []
  (gen/elements ["relativeHumidity" "temperature" "electricityConsumption" "gasAsHeatingFuel"
                 "solarRadiation" "waterConsumption" "temperatureGround" "co2"]))

(defn unit-gen [type]
  (cond
   (= "relativeHumidity" type) "%RH"
   (= "temperature" type) "C"
   (= "electricityConsumption" type) "kWh"
   (= "gasHeatingFuel" type) "m^3"
   (= "solarRadiation" type) "W/m^2"
   (= "waterConsumption" type) "L"
   (= "temperatureGround" type) "C"
   (= "co2" type) "ppm"
   :else ""))

(defn generate-sensor-sample [period n]
  (take n (repeatedly n #(let [type (first (gen/sample (type-gen) 1))
                               unit (unit-gen type)]
                           {:type type
                            :unit unit
                            :resolution (str (rand-int 60))
                            :accuracy (str (rand-int 100))
                            :period (first (gen/sample (gen/elements [period])))
                            :min "0"
                            :max (str (rand-int 100))
                            :correction nil
                            :correctedUnit nil
                            :correctionFactor nil
                            :correctionFactorBreakdown nil
                            :events 0
                            :errors 0
                            :status "Not enough data"
                            :median 0}))))


;;;;;;;;;;;;;;; Generate devices ;;;;;;;;;;;;;;

(defn location-gen []
  {:name (first (gen/sample
                 (gen/not-empty (gen/elements ["Kitchen" "Living Room Floor" "Living Room Ceiling" "Bathroom"])) 1))
   :latitude (str (rand 55))
   :longitude (str (rand 1))})

(defn generate-device-sample
  [entity-id n]
  (take n (repeatedly n
                      #(hash-map :device-id (str (uuid))
                                 :description (first (gen/sample (gen/not-empty gen/string-alpha-numeric) 1))
                                 :parent-id (uuid)
                                 :entity-id entity-id
                                 :location (location-gen)
                                 :metadata nil
                                 :privacy (first (gen/sample (gen/not-empty (gen/elements ["public" "private"])) 1))
                                 :meteringPointId (uuid)))))

;;;;;;;;;;;;;;;;; Generate measurements ;;;;;;;;;;;;;;;;

(defn timestamps [frequency]
  (into [] (take 200 (periodic/periodic-seq (t/date-time (t/year (t/now)) (t/month (t/now))) frequency))))

(defn get-month [timestamp]
   (str (t/year timestamp) "-" (t/month timestamp)))

;; Standard measurements

(defmulti generate-measurements
  "Dispatches a call to a specific device period and generates
  appropriate measurements."
  (fn [sensor]
    (:period sensor)))

(defmethod generate-measurements "INSTANT"
  [sensor]
    (let [timestamps (timestamps (t/hours 2))
          type       (:type sensor)]
    (map #(hash-map :type type
                    :timestamp (tc/to-date %)
                    :value (str (rand 50))
                    :error nil) timestamps)))

(defmethod generate-measurements "PULSE"
  [sensor]
  (let [timestamps (timestamps (t/hours 2))
        type       (:type sensor)]
    (map #(hash-map :type type
                    :timestamp (tc/to-date %)
                    :value (str (rand-int 100))
                    :error nil) timestamps)))

(defmethod generate-measurements "CUMULATIVE"
  [sensor]
   (let [timestamps (timestamps (t/hours 2))
         type       (:type sensor)]
     (map-indexed (fn [i t] (hash-map :type type
                                      :timestamp (tc/to-date t)
                                      :value (str i)
                                      :error nil)) timestamps)))

(defn measurements
  "Iterates through all sensors and generates appropriate
  measurements. Returns a list of maps."
  [sensor]
  (generate-measurements sensor))

;; Invalid measurements
(defn generate-measurements-above-median
  "Generates measurements that contain readings 200 x median."
  [sensor]
  (let [timestamps (timestamps (t/hours 2))
        type       (:type sensor)]
    (map-indexed (fn [i t] (hash-map :type type
                                     :timestamp (tc/to-date t)
                                     :value (str (if (= 0 (mod i 50)) (* 300 (rand 50)) (rand 50)))
                                     :error nil)) timestamps)))

(defn generate-invalid-measurements
  "Generates measurements that contain invalid readings."
  [sensor]
  (let [timestamps (timestamps (t/hours 2))
        type       (:type sensor)]
    (map-indexed (fn [i t] (merge {:type type
                                   :timestamp (tc/to-date t)}
                                  (if (= 0 (mod i 5))
                                    {:value "Invalid reading"
                                     :error nil}
                                    {:value (str (rand-int 10))
                                     :error nil}))) timestamps)))

(defn mislabelled-measurements
  "Generates mislabelled measurements
   for a given sensor."
  [sensor period]
  (let [sensor (assoc-in sensor [:period] period)]
    (generate-measurements sensor)))

(defn validate-and-insert
  "Updates appropriate counters and inserts data."
  [session measurement]
  (binding [client/*default-session* session]
    (cql/insert "measurements" measurement)))

(defn insert-measurements
  [session measurements]
  (doseq [m measurements] (validate-and-insert session m)))

(defn insert-sensors
  [session sensors]
  (binding [client/*default-session* session]
    (doseq [s sensors] (cql/insert "sensors" s))))

(defn insert-devices
  [session devices]
  (binding [client/*default-session* session]
    (doseq [d devices] (cql/insert "devices" d))))

;; STEPS:
;; 1. Create devices
;; 2. Create sensor for each device
;; 3. Create measurements for each sensor
;; e.g.
;; (let [devices (generate-device-sample 5)
;;       sensors (reduce concat (map #(generate-sensor-sample "CUMULATIVE" 1) devices))]
;;   (doseq [s sensors] (insert-measurements session (measurements s))))


#_(deftype DataGenerator [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system] system)
  (stop [_ system] system))
