(ns kixi.hecuba.data.measurements-test
  (:require [kixi.hecuba.data.measurements :refer :all]
            [clojure.test                  :refer :all]
            [kixi.hecuba.data.calculate    :as calc]
            [generators                    :as g]))

;; Make sure the query for rollups to insert data into partitioned_measurement
;; is formatted correctly and doesn't lead to an error
(deftest prepare-batch-test
  (testing "Testing the queries are formatted correctly"
    (let [sensor (first (g/generate-sensor-sample "CUMULATIVE" 1))
          measurements        (g/measurements sensor)
          calc-measurements   (calc/diff-seq "12345" measurements)
          batch-measurements  (first (partition-all 10 calc-measurements))]
      (is (= {:logged false,
              :batch `({:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:01:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:02:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:03:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:04:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:05:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:06:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:07:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:08:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:09:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}}
                       {:insert :partitioned_measurements, :values
                        {:timestamp #inst "2014-01-01T00:10:00.000-00:00", :value "1",
                         :reading_metadata {"is-number" "true", "median-spike" "n-a"},
                         :device_id nil, :sensor_id "12345", :month nil}})}
             (prepare-batch batch-measurements)))
      ;; Make sure there is a sensor_id
      (is (not-any? empty?
                    (map #(get-in % [:values :sensor_id]) 
                         (:batch (prepare-batch batch-measurements))))))))

(deftest where-test
  (testing "Testing conversion of a where clause map to hayt format."
    (is (= (set [[= :device_id "12345"] [= :sensor_id "6789"]
                 [= :month 201101] [>= :timestamp 20140101] [< :timestamp 20140201]])
           (set (where {:device_id "12345" :sensor_id "6789"
                        :month 201101 :start 20140101 :end 20140201}))))))

(deftest parse-measurements-test
  (testing "Testing conversion of measurement map values to number values"
    ;; Testing string and number values
    (is (= '({:type "thing", :timestamp "2015-01-01 00:00:00", :value 0.192}
             {:type "thing", :timestamp "2015-01-01 01:00:00", :value 0.2992}
             {:type "thing", :timestamp "2015-01-01 02:00:00", :value 1.989})
           (parse-measurements [{:type "thing", :timestamp "2015-01-01 00:00:00", :value "0.192"}
                                {:type "thing", :timestamp "2015-01-01 01:00:00", :value "0.2992"}
                                {:type "thing", :timestamp "2015-01-01 02:00:00", :value 1.989}])))
    ;; Testing string or number values with a leading zero
    (is  (= '({:type "thing", :timestamp "2015-01-01 00:00:00", :value 0.192}
              {:type "thing", :timestamp "2015-01-01 01:00:00", :value 9.2992}
              {:type "thing", :timestamp "2015-01-01 02:00:00", :value 1.989})
            (parse-measurements [{:type "thing" :timestamp "2015-01-01 00:00:00" :value "0.192"}
                                 {:type "thing" :timestamp "2015-01-01 01:00:00" :value "09.2992"}
                                 {:type "thing" :timestamp "2015-01-01 02:00:00" :value 01.989}])))
    ;; Testing nil value and random string value
    (is (= '({:type "thing", :timestamp "2015-01-01 00:00:00", :value 0.192}
             {:type "thing", :timestamp "2015-01-01 01:00:00", :value nil}
             {:type "thing", :timestamp "2015-01-01 02:00:00", :value nil})
           (parse-measurements [{:type "thing" :timestamp "2015-01-01 00:00:00" :value "0.192"}
                                {:type "thing" :timestamp "2015-01-01 01:00:00" :value nil}
                                {:type "thing" :timestamp "2015-01-01 02:00:00" :value "wibble"}])))))
