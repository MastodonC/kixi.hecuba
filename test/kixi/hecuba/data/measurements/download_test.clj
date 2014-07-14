(ns kixi.hecuba.data.measurements.download-test
  (:require [kixi.hecuba.data.measurements.download :refer :all]
            [clojure.test :refer :all]
            [clj-time.core :as t]))

(defn third [coll]
  (nth coll 2))

(deftest one-measurement-three-timestamps-all-in-different-sensors
  (is (= [[{:timestamp (t/date-time 2014 7 1 0 3 0) :value 3}
           {:timestamp (t/date-time 2014 7 1 0 5 0) :value nil}
           {:timestamp (t/date-time 2014 7 1 0 7 0) :value nil}
           {:timestamp (t/date-time 2014 7 1 0 9 0) :value 9}]
          [{:timestamp (t/date-time 2014 7 1 0 3 0) :value nil}
           {:timestamp (t/date-time 2014 7 1 0 5 0) :value 5}
           {:timestamp (t/date-time 2014 7 1 0 7 0) :value nil}
           {:timestamp (t/date-time 2014 7 1 0 9 0) :value nil}]
          [{:timestamp (t/date-time 2014 7 1 0 3 0) :value nil}
           {:timestamp (t/date-time 2014 7 1 0 5 0) :value nil}
           {:timestamp (t/date-time 2014 7 1 0 7 0) :value 7}
           {:timestamp (t/date-time 2014 7 1 0 9 0) :value nil}]]

         (filled-measurements [[{:timestamp (t/date-time 2014 7 1 0 3 0) :value 3}{:timestamp (t/date-time 2014 7 1 0 9 0) :value 9}]
                               [{:timestamp (t/date-time 2014 7 1 0 5 0) :value 5}]
                               [{:timestamp (t/date-time 2014 7 1 0 7 0) :value 7}]])
         )))
