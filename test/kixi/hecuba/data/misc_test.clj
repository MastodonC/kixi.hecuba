(ns kixi.hecuba.data.misc-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.batch-checks :as bc]
            [generators :as g]
            [kixi.hecuba.data.misc :as misc]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

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
