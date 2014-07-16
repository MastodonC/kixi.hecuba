(ns kixi.hecuba.data.measurements.download-test
  (:require [kixi.hecuba.data.measurements.download :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :as t]
            [clojure.test.check.clojure-test :refer (defspec)]
            [clojure.test.check :as tc]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]))


(defn- reading [mins val]
  {:timestamp (t/date-time 2014 7 1 0 mins 0) :value val})

(deftest one-measurement-three-timestamps-all-in-different-sensors
  (is (= [[(reading 3 3)
           (reading 5 nil)
           (reading 7 nil)
           (reading 9 9)]
          [(reading 3 nil)
           (reading 5 5)
           (reading 7 nil)
           (reading 9 nil)]
          [(reading 3 nil)
           (reading 5 nil)
           (reading 7 7)
           (reading 9 nil)]]

         (filled-measurements [[(reading 3 3)(reading 9 9)]
                               [(reading 5 5)]
                               [(reading 7 7)]]))))

(deftest one-measurement-three-timestamps-with-same-time-in-two-sensors
  (is (= [[(reading 3 3)
           (reading 5 nil)
           (reading 7 nil)
           (reading 9 9)]
          [(reading 3 4)
           (reading 5 5)
           (reading 7 nil)
           (reading 9 nil)]
          [(reading 3 nil)
           (reading 5 nil)
           (reading 7 7)
           (reading 9 nil)]]

         (filled-measurements [[(reading 3 3)(reading 9 9)]
                               [(reading 3 4)(reading 5 5)]
                               [(reading 7 7)]]))))

(deftest one-measurement-three-timestamps-with-empty-measurements-in-one-sensor
  (is (= [[(reading 3 3)
           (reading 4 nil)
           (reading 9 9)]
          [(reading 3 33)
           (reading 4 4)
           (reading 9 nil)]
          [(reading 3 nil)
           (reading 4 nil)
           (reading 9 nil)]]

         (filled-measurements [[(reading 3 3)(reading 9 9)]
                               [{:timestamp (t/date-time 2014 7 1 0 3 0) :value 33}(reading 4 4)]
                               []]))))

(def gen-date-time
  (gen/fmap (partial apply t/date-time)
            (gen/tuple (gen/choose 2012 2014)
                       (gen/choose 1 12)
                       (gen/choose 1 28) ; TODO 29,30,31 days
                       (gen/choose 0 23)
                       (gen/choose 0 59)
                       (gen/choose 0 59)
                       (gen/choose 0 999))))

(def gen-reading
  (gen/fmap (partial zipmap [:timestamp :value])
            (gen/tuple gen-date-time
                       gen/pos-int)))

(deftest generated-data-doesnt-fail
  (testing "no inputs cause exceptions"
    (is (:result
         (let [p (prop/for-all [data (gen/vector (gen/vector gen-reading 10) 50)]
                               (filled-measurements data))]
           (tc/quick-check 100 p))))))
