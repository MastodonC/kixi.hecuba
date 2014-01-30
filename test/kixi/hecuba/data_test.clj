(ns kixi.hecuba.data-test
  (:use clojure.test)
  (:require [simple-check.core :as sc]                                          
            [simple-check.generators :as gen]                                   
            [simple-check.properties :as prop]
            [simple-check.clojure-test :as ct :refer (defspec)]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.periodic :as periodic]))

(def custom-formatter (tf/formatter "yyyy-MM-dd HH:mm:ss"))

;;;;;;;;;;;;;;;; Generate devices ;;;;;;;;;;;;;;

(defn reading-gen [period]
  (gen/hash-map :type (gen/not-empty gen/string-alpha-numeric) 
                :unit gen/string-alpha-numeric
                :resolution gen/nat
                :accuracy gen/nat
                :period (gen/not-empty (gen/elements [period]))
                :min gen/nat
                :max gen/nat
                :correction gen/boolean
                :correctedUnit gen/string-alpha-numeric
                :correctionFactor gen/nat
                :correctionFactorBreakdown gen/string-alpha-numeric))

(def location-gen
  (gen/hash-map :name gen/string-alpha-numeric
               :latitude gen/string-alpha-numeric
               :longitude gen/string-alpha-numeric))

(def measurement-value-gen
  (gen/one-of [gen/int gen/boolean gen/string gen/ratio]))

(def timestamp-gen
  (gen/elements [(tf/unparse custom-formatter (t/now))]))

(def measurement-gen
  (gen/hash-map :type (gen/not-empty gen/string)
                :timestamp (gen/not-empty timestamp-gen)
                :value measurement-value-gen
                :error gen/string))

(defn device-gen [period]
  (gen/hash-map :deviceId (gen/not-empty gen/string-alpha-numeric)
                :entityId (gen/not-empty gen/string-alpha-numeric)
                :parentId gen/string-alpha-numeric
                :description gen/string 
                :meteringPointId gen/string-alpha-numeric
                :privacy (gen/not-empty gen/string-alpha-numeric) 
                :location location-gen
                :metadata (gen/elements [{}])
                :readings (reading-gen period)
                :measurements measurement-gen))

(defn generate-device-sample
  [period n]
  (gen/sample (device-gen period) n))

;;;;;;;;;;;;;;;;; Generate measurements ;;;;;;;;;;;;;;;;

(def counter (atom 0))

(defn timestamps [frequency]
  (into [] (take 50 (periodic/periodic-seq (t/now) frequency))))

(defn generate-cumulative-measurements
  "Cumulative (incremental) measurements is a counter that 
  should always go up. It may sometimes re-set back to zero
  on device restart or counter overflow"
  []
  (let [timestamps (timestamps (t/minutes 5))]
    (map #(hash-map :type (first (gen/sample (gen/not-empty gen/string) 1))
                    :timestamp (tf/unparse custom-formatter %)
                    :value (swap! counter + (rand-int 10))
                    :error (first (gen/sample gen/string 1))) timestamps)))

(defn generate-pulse-measurements
  "Pulse measurements is an indication of the number of times
  an event has been recorded since the last reading"
  []
  (let [timestamps (timestamps (t/hours 2))]
    (map #(hash-map :type (first (gen/sample (gen/not-empty gen/string) 1))
                    :timestamp (tf/unparse custom-formatter %)
                    :value (rand-int 100)
                    :error (first (gen/sample gen/string 1))) timestamps)))

(defn generate-instant-measurements
  "Instant measurement is simply the value recorded at that
  instant in time"
  []
  (let [timestamps (timestamps (t/minutes 5))]
    (map #(hash-map :type (first (gen/sample (gen/not-empty gen/string) 1))
                    :timestamp (tf/unparse custom-formatter %)
                    :value (rand 150)
                    :error (first (gen/sample gen/string 1))) timestamps)))

;;; TESTS

(defn going-up?
  "Check if all measurements are going up"
  [measurements]
  (= (sort-by :value measurements) (sort-by :timestamp measurements)))

(defn neg-or-not-int? [measurement]
  (or (not (integer? (:value measurement))) (neg? (:value measurement))))

(defn labelled-correctly?
  "Check if a given device is not mislabelled, e.g. 'INSTANT' providing
  cumulative measurements."
  [device measurements]
  (let [label (:period (:readings device))]
    (prn label)
    (case label
      "INSTANT" true ; Anything goes
      "CUMULATIVE" (going-up? measurements)
      "PULSE" (empty? (filter neg-or-not-int? measurements)))))

(deftest mislabelled-devices
  (testing "Cumulative device"
    (is (labelled-correctly? (first (generate-device-sample "CUMULATIVE" 1)) (generate-cumulative-measurements)))
    (is (= false (labelled-correctly? (first (generate-device-sample "CUMULATIVE" 1)) (generate-instant-measurements)))))
  (testing "Instant device"
    (is (labelled-correctly? (first (generate-device-sample "INSTANT" 1)) (generate-instant-measurements))))
  (testing "Pulse device"
    (is (labelled-correctly? (first (generate-device-sample "PULSE" 1)) (generate-pulse-measurements)))
    (is (= false (labelled-correctly? (first (generate-device-sample "PULSE" 1)) (generate-instant-measurements))))))


