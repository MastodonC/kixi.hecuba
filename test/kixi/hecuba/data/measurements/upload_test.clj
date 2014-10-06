(ns kixi.hecuba.data.measurements.upload-test
  (:require [kixi.hecuba.data.measurements.upload :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :as t]
            [kixipipe.storage.s3 :refer (s3-key-from item-from-s3-key)]))

(deftest vertical-csv->sanity-test
  (is (= [[[:ts1 :d1_v1]
           [:ts2 :d1_v2]]
          [[:ts1 :d2_v1]
           [:ts2 :d2_v2]]]
         (vertical-csv->sanity [[:ts1 :d1_v1 :d2_v1]
                                [:ts2 :d1_v2 :d2_v2]]))))

;; These dates supplied by Geoff Stevens in e-mail Thu, 21 Aug 2014 16:43:07 +0000
(deftest geoff-date-formats-test
  (is (= ((date-parser nil) "30/12/2013 00:00")
         (t/date-time 2013 12 30)))
  (is (= ((date-parser "") "30/12/2013 00:00:00")
         (t/date-time 2013 12 30)))
  (is (= ((date-parser "") "2011-10-01T00:10:00+00:00")
         (t/date-time 2011 10 1 0 10))))

(deftest s3-key-from-test
  (is (= (s3-key-from {:src-name "uploads" :metadata {:username "foo@example.com"} :entity_id 1234})
         "uploads/foo@example.com/1234/data")
      (= (s3-key-from {:src-name "uploads" :entity_id 1234 :metadata {:username "foo@example.com"} :suffix "status"})
         "uploads/foo@example.com/1234/status")))

(deftest item-from-s3-key-test
  (is (= {:src-name "uploads" :entity_id "1234" :metadata {:username "foo@example.com"} :uuid "12312"}
         (item-from-s3-key "uploads/foo@example.com/1234/12312"))
      (= nil
         (item-from-s3-key "uploads/1234/123/"))))
