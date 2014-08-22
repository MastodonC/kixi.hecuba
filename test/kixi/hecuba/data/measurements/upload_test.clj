(ns kixi.hecuba.data.measurements.upload-test
  (:require [kixi.hecuba.data.measurements.upload :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :as t]))

(deftest vertical-csv->sanity-test
  (is (= [[[:ts1 :d1_v1]
           [:ts2 :d1_v2]]
          [[:ts1 :d2_v1]
           [:ts2 :d2_v2]]]
         (vertical-csv->sanity [[:ts1 :d1_v1 :d2_v1]
                                [:ts2 :d1_v2 :d2_v2]]))))

;; These dates supplied by Geoff Stevens in e-mail Thu, 21 Aug 2014 16:43:07 +0000
(deftest geoff-date-formats-test
  (is (= (auto-date-parser "30/12/2013 00:00")
         (t/date-time 2013 12 30)))
  (is (= (auto-date-parser "30/12/2013 00:00:00")
         (t/date-time 2013 12 30)))
  (is (= (auto-date-parser "2011-10-01T00:10:00+00:00")
         (t/date-time 2011 10 1 0 10))))
