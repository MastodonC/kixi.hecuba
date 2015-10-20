(ns kixi.hecuba.data.sensors-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.sensors :as sensors]
            [generators :as g]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(deftest encode-test
  (let [metadata {:sensor_id "gasConsumption,"
                  :device_id "ce02feec54cc7b394c7f9099d8032b1b8bb0ed6c,"
                  :difference_series {"start" #inst "2009-08-03T00:00:00.000-00:00",
                                      "end" #inst "2014-08-17T23:55:00.000-00:00"},
                  :co2 {"start" #inst "2009-08-03T00:00:00.000-00:00",
                        "end" #inst "2014-08-17T23:55:00.000-00:00"},
                  :kwh {"start" #inst "2009-08-03T00:00:00.000-00:00",
                        "end" #inst "2014-08-17T23:55:00.000-00:00"},
                  :upper_ts #inst "2014-08-17T23:55:00.000-00:00",
                  :lower_ts #inst "2009-08-03T00:00:00.000-00:00"}
        sensor1 {:min nil, :unit "m^3", :user_metadata nil, :accuracy nil,
                :sensor_id "ef23c1b7-fe79-4916-8462-0baa0543da4b", :frequency nil,
                :corrected_unit nil, :type "gasConsumption_differenceSeries",
                :correction_factor nil, :alias_sensor nil, :correction nil, :resolution nil,
                :alias "Gas|BPE007_BI62|148362|m3|", :max nil, :user_id "username@mastodonc.com",
                :correction_factor_breakdown nil, :period "PULSE", :synthetic true,
                 :device_id "ce02feec54cc7b394c7f9099d8032b1b8bb0ed6c", :actual_annual false}
        sensor2 {:min nil, :unit "m^3", :user_metadata nil, :accuracy nil,
                 :sensor_id "ef23c1b7-fe79-4916-8462-0baa0543da4b", :frequency nil,
                 :corrected_unit nil, :type "gasConsumption_differenceSeries",
                 :correction_factor nil, :alias_sensor {:device_id "abc" :sensor_id "xyz"},
                 :correction nil, :resolution nil,
                 :alias "Gas|BPE007_BI62|148362|m3|", :max nil, :user_id "username@mastodonc.com",
                 :correction_factor_breakdown nil, :period "PULSE", :synthetic true,
                 :device_id "ce02feec54cc7b394c7f9099d8032b1b8bb0ed6c", :actual_annual false}]
    (testing "Metadata encoding during update." ;; Make sure ":alias_sensor {}" is not added
             (is (= {:difference_series {"start" #inst "2009-08-03T00:00:00.000-00:00",
                                         "end" #inst "2014-08-17T23:55:00.000-00:00"},
                     :co2 {"start" #inst "2009-08-03T00:00:00.000-00:00",
                           "end" #inst "2014-08-17T23:55:00.000-00:00"},
                     :kwh {"start" #inst "2009-08-03T00:00:00.000-00:00",
                           "end" #inst "2014-08-17T23:55:00.000-00:00"},
                     :upper_ts #inst "2014-08-17T23:55:00.000-00:00",
                     :lower_ts #inst "2009-08-03T00:00:00.000-00:00"}
                    (sensors/encode metadata :remove-pk))))
    (testing "Sensor data encoding during update." ;; ":alias_sensor nil" -> ":alias_sensor {}"
      (is (= {:min nil, :unit "m^3", :user_metadata nil, :accuracy nil, :frequency nil,
              :corrected_unit nil, :type "gasConsumption_differenceSeries",
              :correction_factor nil, :alias_sensor {}, :correction nil, :resolution nil,
              :alias "Gas|BPE007_BI62|148362|m3|", :max nil, :user_id "username@mastodonc.com",
              :correction_factor_breakdown nil, :period "PULSE", :synthetic true,
              :actual_annual false}
             (sensors/encode sensor1 :remove-pk))))
    (testing "Alias sensor data is added if present"
      (is (= {:min nil, :unit "m^3", :user_metadata nil, :accuracy nil, :frequency nil,
              :corrected_unit nil, :type "gasConsumption_differenceSeries",
              :correction_factor nil, :alias_sensor {"sensor_id" "xyz", "device_id" "abc"},
              :correction nil, :resolution nil, :alias "Gas|BPE007_BI62|148362|m3|", :max nil,
              :user_id "username@mastodonc.com", :correction_factor_breakdown nil,
              :period "PULSE", :synthetic true, :actual_annual false}
             (sensors/encode sensor2 :remove-pk))))))

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
             (sensors/update-date-range empty-sensor :rollups min-date max-date))))
    (testing "Sensor with full metadata."
      (is (= {"end" max-date}
             (sensors/update-date-range full-sensor :rollups min-date max-date))))
    (testing "Sensor with empty metadata and empty max and min dates."
      (is (thrown? AssertionError (sensors/update-date-range empty-sensor :rollups nil nil))))))


(deftest range-for-all-sensors-test
  (let [sensors [{:type "interpolatedHeatConsumption_differenceSeries"
                   :upper_ts #inst "2012-06-28T22:39:00.000-00:00"
                   :lower_ts #inst "2012-06-27T21:01:00.000-00:00"
                  :device_id "b4f0c7e2b15ba9636f3fb08379cc4b3798a226bb"}
                 {:type "interpolatedElectricityConsumption_differenceSeries"
                  :upper_ts #inst "2012-06-27T22:39:00.000-00:00"
                  :lower_ts #inst "2012-06-21T21:01:00.000-00:00"
                  :device_id "268e93a5249c24482ac1519b77f6a45f36a6231d"}]]

    (testing "Testing range-for-padding"
      (let [[start end] (sensors/range-for-all-sensors sensors)]
        (is (= (t/date-time 2012 06 21 21 01 00) start))
        (is (= (t/date-time 2012 06 28 22 39 00) end))))))
