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
    
    (testing "Generate a day worth of timestamps with interval of 60 seconds. Inclusive."
      (is (= 1441 (count (calc/timestamp-seq-inclusive start end)))))))
