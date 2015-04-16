(ns kixi.hecuba.data.measurements.calculations-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.measurements.calculations :refer :all]
            [kixi.hecuba.data.calculate :as calculate]
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
      (is (nil? (calculation :max-for-day nil)))
      (is (= 2.0 (calculation :avg-rolling-4-weeks-night data)))
      (is (= 0 (calculation :min-rolling-4-weeks-night (g/generate-measurements (g/generate-sensor-sample "CUMULATIVE")))))
      (is (= 499 (calculation :max-rolling-4-weeks-night (g/generate-measurements (g/generate-sensor-sample "CUMULATIVE")))))
      (is (= 134.5 (calculation :avg-rolling-4-weeks-night (g/generate-measurements (g/generate-sensor-sample "CUMULATIVE"))))))))

(deftest filter-test
  (testing "Filtering data according to a given period."
    (let [data [{:error nil, :reading_metadata {"is-number" "false"}, :value "Invalid reading", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T00:00:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "2", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T00:15:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "3", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T00:30:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T00:45:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T10:45:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T10:50:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T11:00:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T17:45:00.000-00:00"}
                {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
                 :timestamp #inst "2014-01-01T00:03:00.000-00:00"}]]
      (is (= []
             (filter morning? data)))
      (is (= [{:error nil, :reading_metadata {"is-number" "false"}, :value "Invalid reading", :type "temperatureGround",
               :timestamp #inst "2014-01-01T00:00:00.000-00:00"}
              {:reading_metadata {"is-number" "true"}, :error nil, :value "2", :type "temperatureGround",
               :timestamp #inst "2014-01-01T00:15:00.000-00:00"}
              {:reading_metadata {"is-number" "true"}, :error nil, :value "3", :type "temperatureGround",
               :timestamp #inst "2014-01-01T00:30:00.000-00:00"}
              {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
               :timestamp #inst "2014-01-01T00:45:00.000-00:00"}
              {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
               :timestamp #inst "2014-01-01T00:03:00.000-00:00"}]
             (filter night? data)))
      (is (= [{:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
               :timestamp #inst "2014-01-01T10:45:00.000-00:00"}
              {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
               :timestamp #inst "2014-01-01T10:50:00.000-00:00"}
              {:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
               :timestamp #inst "2014-01-01T11:00:00.000-00:00"}]
             (filter day? data)))
      (is (= [{:reading_metadata {"is-number" "true"}, :error nil, :value "1", :type "temperatureGround",
               :timestamp #inst "2014-01-01T17:45:00.000-00:00"}]
             (filter evening? data))))))

(deftest tariff-ranges-test
  (testing "Testing finding date ranges for tariffs."
    (let [dt (time/update-timestamp (t/now) {:hour 0 :minutes 0 :seconds 0 :milliseconds 0})]
      (is (= [{:start (t/date-time 2012 9 25) :end (t/date-time 2012 10 25)
               :tariff {:a 1}}
              {:start (t/date-time 2012 10 25) :end (t/date-time 2012 12 25)
               :tariff {:a 2}}
              {:start (t/date-time 2012 12 25) :end (t/date-time 2013 1 25)
               :tariff {:a 3}}
              {:start (t/date-time 2013 1 25) :end dt
               :tariff {:a 4}}]
             (tariff-ranges [{:profile_data {:tariff {:a 1}}
                              :timestamp #inst "2012-09-25T00:00:00.000-00:00"}
                             {:profile_data {:tariff {:a 2}}
                              :timestamp #inst "2012-10-25T00:00:00.000-00:00"}
                             {:profile_data {:tariff {:a 3}}
                              :timestamp #inst "2012-12-25T00:00:00.000-00:00"}
                             {:profile_data {:tariff {:a 4}}
                              :timestamp #inst "2013-01-25T00:00:00.000-00:00"}]))))))

(deftest match-tariff-test
  (testing "Testing finding a matching tariff."
    (is (= {:a 2}
           (match-tariff {:timestamp "2012-12-24T00:00:00.000-00:00"
                          :value "20"}
                         [{:profile_data {:tariff {:a 1}}
                           :timestamp #inst "2012-09-25T00:00:00.000-00:00"}
                          {:profile_data {:tariff {:a 2}}
                           :timestamp #inst "2012-10-25T00:00:00.000-00:00"}
                          {:profile_data {:tariff {:a 3}}
                           :timestamp #inst "2012-12-25T00:00:00.000-00:00"}
                          {:profile_data {:tariff {:a 4}}
                           :timestamp #inst "2013-01-25T00:00:00.000-00:00"}])))))

(deftest discounted-standing-charge-test
  (testing "Testing discounted standng charge calculation."
    (is (= 0.1918 (calculate/round (discounted-standing-charge 0.2192 10) 4)))))

(deftest apply-tariff-test
  (testing "Testing calculation of standard tariff."
    (is (= 0.08
           (calculate/round (apply-tariff {:cost-per-kwh 0.13 :type :electricity :daily-standing-charge 0.2192
                                           :annual-lump-sum-discount 5.0}
                                          [{:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T20:10:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.15"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T20:15:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.13"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T20:20:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.25"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T20:25:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.13"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T20:30:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.02"}])
                            2)))
    (is (= 0.06
           (calculate/round (apply-tariff {:type "electricity_time_of_use"
                                           :daily-standing-charge 0.2192
                                           :cost-per-on-peak-kwh 0.15
                                           :cost-per-off-peak-kwh 0.06
                                           :annual-lump-sum-discount 10.0
                                           :off-peak-periods [{:start "00:00" :end "05:00"}
                                                              {:start "22:00" :end "23:59"}]}
                                          [{:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T20:10:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.15"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T20:15:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.13"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T04:20:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.25"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T04:25:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.13"}
                                           {:device_id "aa8392871e0f0a5dc23fb1f89f58b765d85674aa",
                                            :sensor_id "Electricity consumption_differenceSeries",
                                            :month 201309,
                                            :timestamp #inst "2013-09-28T05:30:00.000-00:00",
                                            :error nil,
                                            :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                                            :value "0.02"}])
                            2)))))

(deftest day->on-off-periods-test
  (testing "Testing function that splits sequence of measurements into on and off peak sequences."
    (is (= {:on [{:timestamp #inst "2013-09-28T20:10:00.000-00:00"}
                 {:timestamp #inst "2013-09-28T20:15:00.000-00:00"}
                 {:timestamp #inst "2013-09-28T05:30:00.000-00:00"}]
            :off [{:timestamp #inst "2013-09-28T04:20:00.000-00:00"}
                  {:timestamp #inst "2013-09-28T04:25:00.000-00:00"}]}
           (day->on-off-periods [{:start "00:00" :end "05:00"}
                                 {:start "22:00" :end "23:59"}]
                                [{:timestamp #inst "2013-09-28T20:10:00.000-00:00"}
                                 {:timestamp #inst "2013-09-28T20:15:00.000-00:00"}
                                 {:timestamp #inst "2013-09-28T04:20:00.000-00:00"}
                                 {:timestamp #inst "2013-09-28T04:25:00.000-00:00"}
                                 {:timestamp #inst "2013-09-28T05:30:00.000-00:00"}])))
    (is (= {:on [{:timestamp #inst "2013-09-28T05:30:00.000-00:00"}]
            :off [{:timestamp #inst "2013-09-28T20:10:00.000-00:00"}
                  {:timestamp #inst "2013-09-28T20:15:00.000-00:00"}
                  {:timestamp #inst "2013-09-28T04:20:00.000-00:00"}
                  {:timestamp #inst "2013-09-28T04:25:00.000-00:00"}]}
           (day->on-off-periods [{:start "22:00" :end "05:00"}
                                 {:start "13:00" :end "16:00"}
                                 {:start "20:00" :end "22:00"}]
                                [{:timestamp #inst "2013-09-28T20:10:00.000-00:00",}
                                 {:timestamp #inst "2013-09-28T20:15:00.000-00:00",}
                                 {:timestamp #inst "2013-09-28T04:20:00.000-00:00",}
                                 {:timestamp #inst "2013-09-28T04:25:00.000-00:00",}
                                 {:timestamp #inst "2013-09-28T05:30:00.000-00:00",}])))))
