(ns kixi.hecuba.data.validate-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.validate :as v]
            [generators :as g]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]
            [kixi.hecuba.data.batch-checks :as bc]))

;; Helpers

(defn transform-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m]
         (assoc-in m [:value] (read-string (:value m))))
       measurements))

;;; 200 x median ;;;

(deftest large-median
  (let [cumulative-sensors (g/generate-sensor-sample "CUMULATIVE" 3)
        instant-sensors    (g/generate-sensor-sample "INSTANT" 3)]

    (println "Testing large median.")

    (testing "Should find readings that are 200 x median"
      (doseq [sensor cumulative-sensors]
        (let [measurements (g/generate-measurements-above-median sensor)
              median       (bc/median (transform-measurements measurements))]
          (is (some true? (map #(v/larger-than-median median %) measurements))))))

   (testing "Should find no readings that are 200 x median"
      (doseq [sensor cumulative-sensors]
        (let [measurements (g/measurements sensor)
              median       (bc/median (transform-measurements measurements))]
          (is (every? false? (map #(v/larger-than-median median %) measurements))))))))
