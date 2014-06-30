(ns kixi.hecuba.data-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.data.batch-checks :as bc]
            [generators :as g]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [kixi.hecuba.data.misc :as misc]
            [kixi.hecuba.data.calculate :as calc]))


;; Helpers

(defn transform-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m]
         (assoc-in m [:value] (read-string (:value m))))
       measurements))

;;;;;;;;;;;; Simple check tests (no C*) ;;;;;;;;;;;;

;;; Mislabelled devices ;;;

(deftest mislabelled-devices
  (let [cumulative-sensors (g/generate-sensor-sample "CUMULATIVE" 3)
        pulse-sensors      (g/generate-sensor-sample "PULSE" 3)
        instant-sensors    (g/generate-sensor-sample "INSTANT" 3)]

    (testing "Mislabelled devices"
      (println "Testing mislabelled devices.")
         
      (doseq [sensor cumulative-sensors]
        (is (bc/labelled-correctly? sensor (transform-measurements (g/measurements sensor)))))
     
      (doseq [sensor cumulative-sensors]
        (is (= false (bc/labelled-correctly? sensor (transform-measurements (g/mislabelled-measurements sensor))))))

      (doseq [sensor instant-sensors]
        (is (bc/labelled-correctly? sensor (transform-measurements (g/measurements sensor)))))

      (doseq [sensor pulse-sensors]
        (is (bc/labelled-correctly? sensor (transform-measurements (g/measurements sensor)))))

      (doseq [sensor pulse-sensors]
        (is (= false (bc/labelled-correctly? sensor (transform-measurements (g/mislabelled-measurements sensor)))))))))

;;; 200 x median ;;;

(deftest large-median
  (let [cumulative-sensors (g/generate-sensor-sample "CUMULATIVE" 3)
        instant-sensors    (g/generate-sensor-sample "INSTANT" 3)]

    (println "Testing large median.")

    (testing "Should find readings that are 200 x median"
      (doseq [sensor cumulative-sensors]
        (let [measurements (g/generate-measurements-above-median sensor)
              median       (bc/median (transform-measurements measurements))]
          (not (empty? (map #(v/larger-than-median median %) measurements))))))

   (testing "Should find no readings that are 200 x median"
      (doseq [sensor cumulative-sensors]
        (let [measurements (g/measurements sensor)
              median       (bc/median (transform-measurements measurements))]
          (empty? (map #(v/larger-than-median median %) measurements)))))))


;;; Dirty dates ;;;

(deftest update-date-range-test
  (let [min-date    (tc/to-date (t/minus (t/now) (t/weeks 1)))
        max-date    (tc/to-date (t/now))
        full-sensor {:type "gasConsumption" :device_id "fe5ab5bf19a7265276ffe90e4c0050037de923e2" 
                     :correction nil :resolution "60" :median 0.0 :status nil :max nil
                     :mislabelled nil :min nil :unit "m^3" :accuracy nil :frequency nil :corrected_unit nil :correction_factor nil
                     :user_id nil :correction_factor_breakdown nil :period "PULSE" :synthetic false
                     :median_calc_check {"end" #inst "2014-01-31T23:59:39.000-00:00" "start" #inst "2014-01-01T00:00:10.000-00:00"}
                     :upper_ts #inst "2014-01-31T23:59:39.000-00:00" :lower_ts #inst "2014-01-01T00:00:10.000-00:00"
                     :kwh {"end" #inst "2014-01-31T23:59:39.000-00:00" "start" #inst "2014-01-01T00:00:10.000-00:00"}
                     :co2 {"end" #inst "2014-01-31T23:59:39.000-00:00" "start" #inst "2014-01-01T00:00:10.000-00:00"}
                     :mislabelled_sensors_check {"end" #inst "2014-01-31T23:59:39.000-00:00" "start" #inst "2014-01-01T00:00:10.000-00:00"}
                     :spike_check {"end" #inst "2014-01-31T23:59:39.000-00:00" "start" #inst "2014-01-01T00:00:10.000-00:00"}
                     :rollups {"end" #inst "2014-01-20T07:53:54.000-00:00" "start" #inst "2014-01-01T00:00:10.000-00:00"}
                     :difference_series {"end" #inst "2014-01-31T23:59:39.000-00:00" "start" #inst "2014-01-01T00:00:10.000-00:00"}}
        empty-sensor {:device_id "12345" :type "temperature"}]
    
    (println "Testing date ranges.")
    
    (testing "Sensor with just device_id and type."
      (is (= {"start" min-date "end" max-date} 
             (misc/update-date-range empty-sensor :rollups min-date max-date))))
    (testing "Sensor with full metadata."
      (is (= {"end" max-date} 
             (misc/update-date-range full-sensor :rollups min-date max-date))))
    (testing "Sensor with empty metadata and empty max and min dates."
      (is (thrown? AssertionError (misc/update-date-range empty-sensor :rollups nil nil))))))

(deftest min-max-dates-test
  (let [sensors (g/generate-sensor-sample "INSTANT" 3)]
    (testing "A sequence of 500 measurements."
      (doseq [s sensors]
        (let [measurements (g/measurements s)
              min-date     (t/date-time 2014 01 01)
              max-date     (t/plus min-date (t/minutes 499))]
          (is (= {:min-date min-date :max-date max-date} (misc/min-max-dates measurements))))))

    (testing "Sequence of 1 measurement."
      (doseq [s sensors]
        (let [measurements (g/measurements s)
              min-date     (t/date-time 2014 01 01)]
          (is (= {:min-date min-date :max-date min-date} (misc/min-max-dates (take 1 measurements)))))))

    (testing "No measurements passed."
      (is (thrown? AssertionError (misc/min-max-dates nil))))))

(deftest timestamp-seq-inclusive-test
  (let [start (t/date-time 2014 01 01)
        end   (t/date-time 2014 01 02)]
    (println "Testing timestmap-seq-inclusive.")
    
    (testing "Generate a day worth of timestamps with interval of 60 seconds. Inclusive."
      (is (= 1441 (count (calc/timestamp-seq-inclusive start end)))))))

(deftest pad-measurements-test
  (let [sensors (g/generate-sensor-sample "INSTANT" 3)]
    (println "Testing pad-measurements.")
    
    (testing "Each sensor should have gaps in measurements filled with template measurements."
      (doseq [s sensors]
        (let [all-measurements       (g/measurements s)
              measurements-with-gaps (remove #(= 0 (t/minute (tc/from-date (:timestamp %)))) all-measurements)
              start-date             (t/date-time 2014 01 01)
              end-date               (t/plus start-date (t/minutes 499))
              expected-timestamps    (calc/all-timestamps-for-range start-date end-date 60)
              padded                 (calc/pad-measurements measurements-with-gaps expected-timestamps 60)]
          (is (= 500 (count padded)))
          (is (= {:min-date start-date :max-date end-date} (misc/min-max-dates padded))))))))

(deftest diff-seq-test
  (let [sensor (first (g/generate-sensor-sample "CUMULATIVE" 1))]
    (println "Testing diff-seq.")
    
    (testing "Difference between measurements should be 1."
      (let [measurements (g/measurements sensor)
            calculated   (calc/diff-seq measurements)]
        (is (= "1" (first (keys (frequencies (map :value calculated))))))))

    (testing "Filled measurements should result in (2 * n) - 1 of N/As than originally."
      (let [measurements           (g/measurements sensor)
            with-gaps              (remove #(= 0 (t/minute (tc/from-date (:timestamp %)))) measurements)
            start-date             (t/date-time 2014 01 01)
            end-date               (t/plus start-date (t/minutes 499))
            expected-timestamps    (calc/all-timestamps-for-range start-date end-date 60)
            padded                 (calc/pad-measurements with-gaps expected-timestamps 60)
            calculated             (calc/diff-seq padded)
            freqs                  (frequencies (map :value calculated))]
        (is (= 482 (get-in freqs ["1"])))
        (is (= 17  (get-in freqs ["N/A"])))))))

(deftest compute-datasets-test
  (let [sensors              (g/generate-sensor-sample "CUMULATIVE" 2)
        measurements         (into [] (map #(misc/parse-measurements (g/generate-measurements %)) sensors))
        invalid-measurements (into [] (map #(misc/parse-measurements (g/generate-invalid-measurements %)) sensors))]

    (println "Testing compute-datasets.")
    
    (testing "Testing addition"
      (is (= "0"   (:value (first (apply calc/compute-datasets :sum "12345" "temperature" measurements)))))
      (is (= "2"   (:value (second (apply calc/compute-datasets :sum "12345" "temperature" measurements)))))
      (is (= "998" (:value (last (apply calc/compute-datasets :sum "12345" "temperature" measurements)))))
      (is (= "499" (:value (first (calc/compute-datasets :sum "12345" "temperature"
                                                         (first measurements) 
                                                         (reverse (last measurements))))))))

    (testing "Testing subtraction"
      (is (= "0"    (:value (first (apply calc/compute-datasets :subtract "12345" "temperature" measurements)))))
      (is (= "0"    (:value (second (apply calc/compute-datasets :subtract "12345" "temperature" measurements)))))
      (is (= "0"    (:value (last (apply calc/compute-datasets :subtract "12345" "temperature" measurements)))))
      (is (= "-499" (:value (first (calc/compute-datasets :subtract "12345" "temperature"
                                                          (first measurements) 
                                                          (reverse (last measurements))))))))
    
    (testing "Testing division"
      (is (= "N/A"  (:value (first (apply calc/compute-datasets :divide "12345" "temperature" measurements)))))
      (is (= "1"    (:value (second (apply calc/compute-datasets :divide "12345" "temperature" measurements)))))
      (is (= "1"    (:value (last (apply calc/compute-datasets :divide "12345" "temperature" measurements)))))
      (is (= "0"    (:value (first (calc/compute-datasets :divide "12345" "temperature" 
                                                          (first measurements)
                                                          (reverse (last measurements)))))))
      (is (= "N/A"  (:value (first (calc/compute-datasets :divide "12345" "temperature" 
                                                          (reverse (last measurements))
                                                          (first measurements)))))))
    (testing "Testing multiplication"
      (is (= "0"      (:value (first (apply calc/compute-datasets :multiply "12345" "temperature" measurements)))))
      (is (= "1"      (:value (second (apply calc/compute-datasets :multiply "12345" "temperature" measurements)))))
      (is (= "249001" (:value (last (apply calc/compute-datasets :multiply "12345" "temperature" measurements)))))
      (is (= "N/A" (:value (nth (apply calc/compute-datasets :multiply "12345" "temperature" invalid-measurements) 5)))))))

(deftest find-resolution-test
  (let [sensor-60     (first (g/generate-sensor-sample "CUMULATIVE" 1))
        sensor-300    (first (g/generate-sensor-sample "PULSE" 1))]
     (println "Testing find-resolution.")
    
    (testing "Testing find-resolution"
      (is (= 60 (calc/find-resolution (g/measurements sensor-60))))
      (is (= 300 (calc/find-resolution (g/measurements sensor-300)))))))

(deftest range-for-padding-test
  (let [sensors [{:type "interpolatedHeatConsumption_differenceSeries" 
                   :upper_ts #inst "2012-06-28T22:39:00.000-00:00"
                   :lower_ts #inst "2012-06-27T21:01:00.000-00:00"
                  :device_id "b4f0c7e2b15ba9636f3fb08379cc4b3798a226bb"}
                 {:type "interpolatedElectricityConsumption_differenceSeries"
                  :upper_ts #inst "2012-06-27T22:39:00.000-00:00"
                  :lower_ts #inst "2012-06-21T21:01:00.000-00:00"
                  :device_id "268e93a5249c24482ac1519b77f6a45f36a6231d"}]]
    
    (testing "Testing range-for-padding"
      (let [[start end] (misc/range-for-all-sensors sensors)]
        (is (= (t/date-time 2012 06 21 21 01 00) start))
        (is (= (t/date-time 2012 06 28 22 39 00) end))))))

(deftest quantize-timestamp-test
  (let [sensor (first (g/generate-sensor-sample "PULSE"))]

    (println "Testing quantize-timestamp")
    
    (testing "Testing quantize-timestamp"
      (let [measurements-250 (g/generate-measurements-with-interval sensor 250)
            measurements-40  (g/generate-measurements-with-interval sensor 40)
            measurements-1700  (g/generate-measurements-with-interval sensor 1800)]
        (is (= (t/date-time 2014 1 1 0 5 0 0) (tc/from-date (:timestamp (second (map #(calc/quantize-timestamp % 300) measurements-250))))))
        (is (= (t/date-time 2014 1 1 0 0 0 0) (tc/from-date (:timestamp (second (map #(calc/quantize-timestamp % 60) measurements-40))))))
        (is (= (t/date-time 2014 1 1 0 30 0 0) (tc/from-date (:timestamp (second (map #(calc/quantize-timestamp % 1800) measurements-1700))))))))))

(deftest should-calculate-test
  (testing "Testing should-calculate?"
    (let [ds {:operation :sum}]
      (is (= true (calc/should-calculate? ds [{:period "PULSE" :unit "kWh"}
                                              {:period "PULSE" :unit "kWh"}])))
      (is (= false (calc/should-calculate? ds [{:period "PULSE" :unit "C"}
                                               {:period "CUMULATIVE" :unit "C"}])))
      (is (= true (calc/should-calculate? ds [{:period "INSTANT" :unit "kWh"}])))
      (is (= true (calc/should-calculate? ds [{:period "PULSE"}]))))))

