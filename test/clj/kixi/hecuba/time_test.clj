(ns kixi.hecuba.time-test
  (:require [kixi.hecuba.time :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :as t]
            [generators :as g]))

;; January in java.util.Calendar is 0
(defn- date
  ([year month day hour minute second]
     (.getTime (doto (java.util.Calendar/getInstance)
                 (.set  year month day hour minute second)
                 (.set java.util.Calendar/MILLISECOND 0)))))

(deftest truncate-seconds-date-non-zero-seconds
  (is (= (date 2014 8 4 12 14 0)
         (truncate-seconds (date 2014 8 4 12 14 35)))))

(deftest truncate-seconds-date-zero-seconds-no-change
  (is (= (date 2014 8 4 12 14 0)
         (truncate-seconds (date 2014 8 4 12 14 0)))))

(deftest truncate-seconds-datetime-non-zero-seconds
  (is (= (t/date-time 2014 8 4 12 14 0)
         (truncate-seconds (t/date-time 2014 8 4 12 14 35)))))

(deftest truncate-seconds-datetime-zero-seconds-no-change
  (is (= (t/date-time 2014 8 4 12 14 0)
         (truncate-seconds (t/date-time 2014 8 4 12 14 0)))))

(deftest hourly-timestamp-test
  (is (= (date 2014 8 4 12 0 0)
         ;; year is a delta to 1900
         (hourly-timestamp (date 2014 8 4 12 54 12)))))

(deftest daily-timestamp-test
  (is (= (date 2014 8 4 0 0 0)
         (daily-timestamp (date 2014 8 4 12 54 12)))))

(deftest get-month-partition-key-date
  (is (= 201409
         ;; year is a delta to 1900
         (get-month-partition-key (date 2014 8 4 12 15 35)))))

(deftest get-month-partition-key-datetime
  (is (= 201408
         (get-month-partition-key (t/date-time 2014 8 4 12 15 35)))))

(deftest start-end-dates-test
  (let [sensor {:mislabelled "false",
                :median_calc_check {},
                :type "electricityConsumption",
                :actual_annual_calculation {"end" #inst "2012-03-31T23:25:00.000-00:00"
                                            "start" #inst "2011-05-23T00:00:00.000-00:00"}
                :upper_ts #inst "2012-03-31T23:25:00.000-00:00",
                :kwh {"end" #inst "2012-03-31T23:25:00.000-00:00",
                      "start" #inst "2011-05-23T00:00:00.000-00:00"},
                :lower_ts #inst "2011-05-23T00:00:00.000-00:00",
                :co2 {"end" #inst "2012-03-31T23:25:00.000-00:00",
                      "start" #inst "2011-05-23T00:00:00.000-00:00"},
                :mislabelled_sensors_check {},
                :spike_check {"end" #inst "2012-03-31T23:25:00.000-00:00",
                              "start" #inst "2011-05-23T00:00:00.000-00:00"},
                :device_id "6ac214f3fb7c5eefc9ce691a159b3e9fc600205f",
                :calculated_datasets {"end" #inst "2012-03-31T23:25:00.000-00:00",
                                      "start" #inst "2011-05-23T00:00:00.000-00:00"},
                :rollups {},
                :difference_series {"end" #inst "2012-03-31T23:25:00.000-00:00",
                                    "start" #inst "2012-03-31T23:25:00.000-00:00"}}]
    (is (= {:start-date (t/date-time 2011 5 23 0 0)
            :end-date (t/date-time 2012 3 31 23 25 0)}
           (start-end-dates :spike_check sensor)))
    (is (= {:start-date (t/date-time 2011 5 23 0 0)
            :end-date (t/date-time 2012 3 31 23 25 0)}
           (start-end-dates :kwh sensor)))
    (is (nil? (start-end-dates :difference_series sensor)))
    (is (nil? (start-end-dates :rollups sensor)))))

(deftest min-max-dates-test
  (let [sensors (g/generate-sensor-sample "INSTANT" 3)]
    (testing "A sequence of 500 measurements."
      (doseq [s sensors]
        (let [measurements (g/measurements s)
              min-date     (t/date-time 2014 01 01)
              max-date     (t/plus min-date (t/minutes 499))]
          (is (= {:min-date min-date :max-date max-date} (min-max-dates measurements))))))

    (testing "Sequence of 1 measurement."
      (doseq [s sensors]
        (let [measurements (g/measurements s)
              min-date     (t/date-time 2014 01 01)]
          (is (= {:min-date min-date :max-date min-date} (min-max-dates (take 1 measurements)))))))

    (testing "No measurements passed."
      (is (thrown? AssertionError (min-max-dates nil))))))

(deftest seq-dates-test
  (testing "Checks a sequence of dates is created."
    (is (= [(t/date-time 2012 5 8) (t/date-time 2012 5 9)]
         (seq-dates (t/date-time 2012 5 8)
                    (t/date-time 2012 5 10)
                    (t/days 1))))
    (is (nil?
         (seq-dates nil
                    (t/date-time 2012 5 10)
                    (t/days 1))))
    (is (nil?
         (seq-dates "2012 5 8"
                    (t/date-time 2012 5 10)
                    (t/days 1))))))

(deftest update-timestamp-test
  (testing "Testing function that updates the timestamp."
    (let [timestamp (t/date-time 1990 1 1 10 0 0)]
      (is (= (t/date-time 1989 1 1 10 0 59)
             (update-timestamp timestamp {:year 1989 :seconds 59})))
      (is (= (t/date-time 1991 10 1 11 0 0)
             (update-timestamp timestamp {:year 1991 :month 10 :hour 11})))
      (is (= (t/date-time 1992 8 22 7 7 7)
             (update-timestamp timestamp {:year 1992 :month 8 :day 22 :hour 7 :minutes 7 :seconds 7})))
      (is (= (t/date-time 1990 1 1 10 0 0)
             (update-timestamp timestamp {})))
      (is (nil? (update-timestamp nil {}))))))

(deftest in-interval?-test
  (testing "Testing function that checks if timestamp falls in a specified tiem interval."
    (is (in-interval? (t/date-time 1980 1 1 12 50 0) {:start "10:30" :end "17:20"}))
    (is (not (in-interval? (t/date-time 1980 1 1 12 50 0) {:start "10:30" :end "10:45"})))
    (is (in-interval? (t/date-time 1980 1 2 3 50 0) {:start "22:00" :end "5:00"}))
    (is (in-interval? (t/date-time 1980 1 2 22 01 0) {:start "22:00" :end "5:00"}))
    (is (not (in-interval? (t/date-time 1980 1 2 21 01 0) {:start "22:00" :end "5:00"})))
    (is (in-interval? (t/date-time 1980 1 2 10 01 0) {:start "10:00" :end "11:00"}))
    (is (not (in-interval? (t/date-time 1980 1 2 11 01 0) {:start "10:00" :end "11:00"})))))
