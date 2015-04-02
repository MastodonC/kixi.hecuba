(ns kixi.hecuba.data.calculate-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.calculate :as calc]
            [generators :as g]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [kixi.hecuba.time :as time]
            [kixi.hecuba.data.measurements :as measurements]))

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
      (is (= 249.5 (calc/average-reading (map :value (measurements/parse-measurements measurements))))))))

(deftest data-to-calculate-test
  (testing "Testing data-to-calculate?"
    (let [sensor               (first (g/generate-sensor-sample "INSTANT" 1))
          invalid-measurements (g/generate-invalid-measurements sensor)
          valid-measurements   (g/measurements sensor)]
      (is (nil? (calc/data-to-calculate? (measurements/parse-measurements invalid-measurements))))
      (is (seq (calc/data-to-calculate? (measurements/parse-measurements valid-measurements)))))))

(deftest should-calculate-test
  (testing "Testing should-calculate?"
    (let [ds {:operation :sum}]
      (is (= true (calc/should-calculate? ds [{:period "PULSE" :unit "kWh"}
                                              {:period "PULSE" :unit "kWh"}])))
      (is (= true (calc/should-calculate? ds [{:period "PULSE" :unit "C"}
                                              {:period "CUMULATIVE" :unit "C"}])))
      (is (= false (calc/should-calculate? ds [{:period "INSTANT" :unit "C"}
                                               {:period "CUMULATIVE" :unit "C"}])))
      (is (= true (calc/should-calculate? ds [{:period "INSTANT" :unit "kWh"}])))
      (is (= true (calc/should-calculate? ds [{:period "PULSE"}]))))))

(deftest pad-measurements-test
  (let [sensors (g/generate-sensor-sample "INSTANT" 3)]
    (println "Testing pad-measurements.")

    (testing "Each sensor should have gaps in measurements filled with template measurements."
      (doseq [s sensors]
        (let [all-measurements       (g/generate-measurements-with-interval s 3600) ;; 500
              measurements-with-gaps (remove #(= 12 (t/hour (tc/from-date (:timestamp %)))) all-measurements) ;; 21
              start-date             (time/truncate-minutes (t/date-time 2014 01 01))
              end-date               (time/truncate-minutes (t/plus start-date (t/hours 499)))
              expected-timestamps    (calc/all-timestamps-for-range start-date end-date 3600)
              padded                 (calc/pad-measurements measurements-with-gaps expected-timestamps)]
          (is (= 500 (count padded)))
          (is (= {:min-date start-date :max-date end-date} (time/min-max-dates padded))))))))

(deftest find-resolution-test
  (let [sensor-60     (first (g/generate-sensor-sample "CUMULATIVE" 1))
        sensor-300    (first (g/generate-sensor-sample "PULSE" 1))]
     (println "Testing find-resolution.")

    (testing "Testing find-resolution"
      (is (= 60 (calc/find-resolution (g/measurements sensor-60))))
      (is (= 300 (calc/find-resolution (g/measurements sensor-300)))))))

(deftest compute-datasets-test
  (let [sensors              (g/generate-sensor-sample "CUMULATIVE" 2)
        measurements         (into [] (map #(measurements/parse-measurements (g/generate-measurements %)) sensors))
        invalid-measurements (into [] (map #(measurements/parse-measurements (g/generate-invalid-measurements %)) sensors))]

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
            calculated   (calc/diff-seq "12345" measurements)]
        (is (= "1" (first (keys (frequencies (map :value calculated))))))))

    (testing "Filled measurements should result in (2 * n) of N/As than originally."
      (let [measurements           (g/generate-measurements-with-interval sensor 3600) ;; 500
            with-gaps              (remove #(= 12 (t/hour (tc/from-date (:timestamp %)))) measurements) ;; 21
            start-date             (time/truncate-minutes (t/date-time 2014 01 01))
            end-date               (time/truncate-minutes (t/plus start-date (t/hours 499)))
            expected-timestamps    (calc/all-timestamps-for-range start-date end-date 3600)
            padded                 (calc/pad-measurements with-gaps expected-timestamps)
            calculated             (calc/diff-seq "12345" padded)
            freqs                  (frequencies (map :value calculated))]
        (is (= 457 (get-in freqs ["1"]))) ;; last measurement is odd so it's not calculated
        (is (= 42  (get-in freqs ["N/A"])))))))

(deftest timestamp-seq-inclusive-test
  (let [start (t/date-time 2014 01 01)
        end   (t/date-time 2014 01 02)]
    (println "Testing timestmap-seq-inclusive.")

    (testing "Generate a day worth of timestamps with interval of 60 seconds. Inclusive."
      (is (= 1441 (count (calc/timestamp-seq-inclusive start end)))))))

(deftest difference-value-test
  (testing "Should return difference or \"N/A\" if values aren't numbers."
    (is (= "1" (calc/difference-value {:value "1" :reading_metadata {"is-number" "true"}}
                                      {:value "2" :reading_metadata {"is-number" "true"}})))
    (is (= "5" (calc/difference-value {:value "1" :reading_metadata {"is-number" "true"}}
                                      {:value "6" :reading_metadata {"is-number" "true"}})))
    (is (= "N/A" (calc/difference-value {:value "N/A" :reading_metadata {"is-number" "false"}}
                                        {:value "2" :reading_metadata {"is-number" "true"}})))
    (is (= "N/A" (calc/difference-value {:value "1" :reading_metadata {"is-number" "false"}}
                                        {:value "2" :reading_metadata {"is-number" "false"}})))
     (is (= "1.5" (calc/difference-value {:value "1" :reading_metadata {"is-number" "true"}}
                                         {:value "2.5" :reading_metadata {"is-number" "true"}})))))
