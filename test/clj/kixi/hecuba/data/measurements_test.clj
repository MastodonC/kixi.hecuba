(ns kixi.hecuba.data.measurements-test
  (:require [kixi.hecuba.data.measurements :refer :all]
            [clojure.test :refer :all]))

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
