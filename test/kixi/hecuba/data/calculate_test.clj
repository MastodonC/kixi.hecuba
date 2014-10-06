(ns kixi.hecuba.data.calculate-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.calculate :as calc]
            [generators :as g]
            [kixi.hecuba.data.misc :as misc]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

;; Helpers

(defn transform-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m]
         (assoc-in m [:value] (read-string (:value m))))
       measurements))

(deftest average-reading-test
  (testing "Testing average-reading"
    (let [sensor (first (g/generate-sensor-sample "CUMULATIVE" 1))
          measurements (g/measurements sensor)]
      (is (= 249.5 (calc/average-reading (map :value (misc/parse-measurements measurements))))))))

(deftest data-to-calculate-test
  (testing "Testing data-to-calculate?"
    (let [sensor               (first (g/generate-sensor-sample "INSTANT" 1))
          invalid-measurements (g/generate-invalid-measurements sensor)
          valid-measurements   (g/measurements sensor)]
      (is (nil? (calc/data-to-calculate? (misc/parse-measurements invalid-measurements))))
      (is (seq (calc/data-to-calculate? (misc/parse-measurements valid-measurements)))))))

(deftest should-calculate-test
  (testing "Testing should-calculate?"
    (let [ds {:operation :sum}]
      (is (= true (calc/should-calculate? ds [{:period "PULSE" :unit "kWh"}
                                              {:period "PULSE" :unit "kWh"}])))
      (is (= false (calc/should-calculate? ds [{:period "PULSE" :unit "C"}
                                               {:period "CUMULATIVE" :unit "C"}])))
      (is (= true (calc/should-calculate? ds [{:period "INSTANT" :unit "kWh"}])))
      (is (= true (calc/should-calculate? ds [{:period "PULSE"}]))))))

(deftest quantize-timestamp-test
  (let [sensor (first (g/generate-sensor-sample "PULSE"))]

    (println "Testing quantize-timestamp")

    (testing "Testing quantize-timestamp"
      (let [measurements-250 (g/generate-measurements-with-interval sensor 250)
            measurements-40  (g/generate-measurements-with-interval sensor 40)
            measurements-1700  (g/generate-measurements-with-interval sensor 1800)
            measurements   [{:timestamp #inst "2011-10-18T21:24:18.000-00:00"} {:timestamp #inst "2011-10-18T21:29:18.000-00:00"}
                            {:timestamp #inst "2011-10-18T21:34:18.000-00:00"} {:timestamp #inst "2011-10-18T21:39:21.000-00:00"}
                            {:timestamp #inst "2011-10-18T21:44:21.000-00:00"}]]
        (is (= (t/date-time 2014 1 1 0 5 0 0) (tc/from-date (:timestamp (second (map #(calc/quantize-timestamp % 300) measurements-250))))))
        (is (= (t/date-time 2014 1 1 0 0 0 0) (tc/from-date (:timestamp (second (map #(calc/quantize-timestamp % 60) measurements-40))))))
        (is (= (t/date-time 2014 1 1 0 30 0 0) (tc/from-date (:timestamp (second (map #(calc/quantize-timestamp % 1800) measurements-1700))))))
        (is (= (t/date-time 2011 10 18 21 25 0 0) (tc/from-date (:timestamp (first (map #(calc/quantize-timestamp % 300) measurements))))))))))

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

(deftest find-resolution-test
  (let [sensor-60     (first (g/generate-sensor-sample "CUMULATIVE" 1))
        sensor-300    (first (g/generate-sensor-sample "PULSE" 1))]
     (println "Testing find-resolution.")

    (testing "Testing find-resolution"
      (is (= 60 (calc/find-resolution (g/measurements sensor-60))))
      (is (= 300 (calc/find-resolution (g/measurements sensor-300)))))))

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

(deftest timestamp-seq-inclusive-test
  (let [start (t/date-time 2014 01 01)
        end   (t/date-time 2014 01 02)]
    (println "Testing timestmap-seq-inclusive.")

    (testing "Generate a day worth of timestamps with interval of 60 seconds. Inclusive."
      (is (= 1441 (count (calc/timestamp-seq-inclusive start end)))))))
