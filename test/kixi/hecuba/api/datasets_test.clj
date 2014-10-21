(ns kixi.hecuba.api.datasets-test
  (:use clojure.test)
  (:require [kixi.hecuba.api.datasets :refer :all]
            [clojure.test :refer :all]))


(deftest get-period-test
  (testing "Testing period for the synthethic sensor"
    (is (= "PULSE" (get-period :sum [{:period "PULSE"} {:period "PULSE"}])))
    (is (= "CUMULATIVE" (get-period :sum [{:period "CUMULATIVE"} {:period "CUMULATIVE"}])))
    (is (= "PULSE" (get-period :subtract [{:period "PULSE"} {:period "PULSE"}])))
    (is (= "CUMULATIVE" (get-period :subtract [{:period "CUMULATIVE"} {:period "PULSE"}])))
    (is (= "INSTANT" (get-period :divide [{:period "PULSE"} {:period "PULSE"}])))
    (is (= "INSTANT" (get-period :multiply-series-by-field [{:period "INSTANT"}])))
    (is (= "CUMULATIVE" (get-period :multiply-series-by-field [{:period "CUMULATIVE"}])))))
