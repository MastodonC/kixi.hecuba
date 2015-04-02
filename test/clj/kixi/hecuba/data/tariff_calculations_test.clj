(ns kixi.hecuba.data.tariff-calculations-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.tariff-calculations :refer :all]
            [generators :as g]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [kixi.hecuba.time :as time]
            [kixi.hecuba.data.measurements :as measurements]))

(deftest calculate-reading-from-seq-test
  (testing "It returns the minimum value for a sequence of sensor data."
    (is (= 0
           (calculate-reading-from-seq min-value
            (take 10 (g/generate-measurements (g/generate-sensor-sample "CUMULATIVE"))))))
    (is (= 1
           (calculate-reading-from-seq min-value
            [{:error nil, :reading_metadata {"is-number" "false"}, :value "Invalid reading", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:00:00.000-00:00"}
             {:reading_metadata {"is-number" "true"}, :error nil, :value "2", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:15:00.000-00:00"}
             {:reading_metadata {"is-number" "true"}, :error nil, :value "4", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:30:00.000-00:00"}
             {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:45:00.000-00:00"}])))
    (is (nil? (calculate-reading-from-seq max-value
               [])))
    (is (nil? (calculate-reading-from-seq avg-value
               nil)))))

(deftest calculation-test
  (testing "Testing calculation function."
    (let [data [{:error nil, :reading_metadata {"is-number" "false"}, :value "Invalid reading", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:00:00.000-00:00"}
             {:reading_metadata {"is-number" "true"}, :error nil, :value "2", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:15:00.000-00:00"}
             {:reading_metadata {"is-number" "true"}, :error nil, :value "3", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:30:00.000-00:00"}
             {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
              :timestamp #inst "2014-01-01T00:45:00.000-00:00"}]]
      (is (= 1 (calculation :min-for-day data)))
      (is (= 3 (calculation :max-for-day data)))
      (is (= 2.0 (calculation :avg-for-day data)))
      (is (= 1 (calculation :min-rolling-4-weeks data)))
      (is (= 3 (calculation :max-rolling-4-weeks data)))
      (is (= 2.0 (calculation :avg-rolling-4-weeks data)))
      (is (nil? (calculation :min-for-day [])))
      (is (nil? (calculation :max-for-day nil))))))
