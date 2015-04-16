(ns kixi.hecuba.data.measurements-test
  (:require [kixi.hecuba.data.measurements :refer :all]
            [clojure.test :refer :all]))

(deftest where-test
  (testing "Testing conversion of a where clause map to hayt format."
    (is (= (set [[= :device_id "12345"] [= :sensor_id "6789"]
                 [= :month 201101] [>= :timestamp 20140101] [< :timestamp 20140201]])
           (set (where {:device_id "12345" :sensor_id "6789"
                        :month 201101 :start 20140101 :end 20140201}))))))
