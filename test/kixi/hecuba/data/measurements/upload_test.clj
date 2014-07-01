(ns kixi.hecuba.data.measurements.upload-test
  (:require [kixi.hecuba.data.measurements.upload :refer :all]
            [clojure.test :refer :all]))

(deftest vertical-csv->sanity-test
  (is (= [[[:ts1 :d1_v1]
           [:ts2 :d1_v2]]
          [[:ts1 :d2_v1]
           [:ts2 :d2_v2]]]
         (vertical-csv->sanity [[:ts1 :d1_v1 :d2_v1]
                                [:ts2 :d1_v2 :d2_v2]]))))
