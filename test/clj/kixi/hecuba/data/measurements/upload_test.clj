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
  (is (= ((date-parser-fn nil) "30/12/2013 00:00")
         (t/date-time 2013 12 30)))
  (is (= ((date-parser-fn "") "30/12/2013 00:00:00")
         (t/date-time 2013 12 30)))
  (is (= ((date-parser-fn "") "2011-10-01T00:10:00+00:00")
         (t/date-time 2011 10 1 0 10)))
  (is (nil? ((date-parser-fn "") ""))))

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

(def sample-csv
  [["Device UUID" "b0601d9c9829f40503ff452f6059fdca870868cd" "a13676c830412d44e6fbc90f40376eb7fdd05064"]
   ["Reading Type" "Temperature Air" "Temperature"]
   ["Customer Ref" "32394tt" "32394lt"]
   ["Description" "32394 - HallAirTemperature" "32394 - living room temperature"]
   ["Location" "hall" "living room"]
   ["Accuracy (percent)" "" ""]
   ["Sample Interval (seconds)" "3600" "3600"]
   ["Frequency" "" ""]
   ["Period" "INSTANT" "INSTANT"]
   ["Parent UUID" "" ""]
   ["Sensor Range Max" "60" "60"]
   ["Sensor Range Min" "-30" "-30"]])

(def expected-devices
  [{:description "32394 - HallAirTemperature",
    :min "-30",
    :customer_ref "32394tt",
    :accuracy "",
    :frequency "",
    :type "Temperature Air",
    :resolution "3600",
    :max "60",
    :entity_id "",
    :period "INSTANT",
    :device_id "b0601d9c9829f40503ff452f6059fdca870868cd",
    :location "hall"}
   {:description "32394 - living room temperature",
    :min "-30",
    :customer_ref "32394lt",
    :accuracy "",
    :frequency "",
    :type "Temperature",
    :resolution "3600",
    :max "60",
    :entity_id "",
    :period "INSTANT",
    :device_id "a13676c830412d44e6fbc90f40376eb7fdd05064",
    :location "living room"}])

(def sample-csv-with-unit
  [["Device UUID" "b0601d9c9829f40503ff452f6059fdca870868cd" "a13676c830412d44e6fbc90f40376eb7fdd05064"]
   ["Reading Type" "Temperature Air" "Temperature"]
   ["Customer Ref" "32394tt" "32394lt"]
   ["Unit" "degC" "degC"]
   ["Description" "32394 - HallAirTemperature" "32394 - living room temperature"]
   ["Location" "hall" "living room"]
   ["Accuracy (percent)" "" ""]
   ["Sample Interval (seconds)" "3600" "3600"]
   ["Frequency" "" ""]
   ["Period" "INSTANT" "INSTANT"]
   ["Parent UUID" "" ""]
   ["Sensor Range Max" "60" "60"]
   ["Sensor Range Min" "-30" "-30"]])

(def expected-devices-with-unit
  [{:description "32394 - HallAirTemperature",
    :unit "degC"
    :min "-30",
    :customer_ref "32394tt",
    :accuracy "",
    :frequency "",
    :type "Temperature Air",
    :resolution "3600",
    :max "60",
    :entity_id "",
    :period "INSTANT",
    :device_id "b0601d9c9829f40503ff452f6059fdca870868cd",
    :location "hall"}
   {:description "32394 - living room temperature",
    :unit "degC"
    :min "-30",
    :customer_ref "32394lt",
    :accuracy "",
    :frequency "",
    :type "Temperature",
    :resolution "3600",
    :max "60",
    :entity_id "",
    :period "INSTANT",
    :device_id "a13676c830412d44e6fbc90f40376eb7fdd05064",
    :location "living room"}])

(deftest parse-vertical-header
  (is (= expected-devices (vec (parse-full-header sample-csv))))
  (is (= expected-devices-with-unit (vec (parse-full-header sample-csv-with-unit)))))
