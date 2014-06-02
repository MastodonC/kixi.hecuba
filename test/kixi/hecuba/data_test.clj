(ns kixi.hecuba.data-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.data.batch-checks :as bc]
            [generators :as g]))


;; Helpers

(defn transform-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m]
         (assoc-in m [:value] (read-string (:value m))))
       measurements))

;;;;;;;;;;;; Simple check tests (no C*) ;;;;;;;;;;;;

;;; Mislabelled devices ;;;

(deftest mislabelled-devices
  (let [cumulative-sensors (g/generate-sensor-sample "CUMULATIVE" 3)
        pulse-sensors      (g/generate-sensor-sample "PULSE" 3)
        instant-sensors    (g/generate-sensor-sample "INSTANT" 3)]

    (testing "Mislabelled devices"
      (println "Testing mislabelled devices.")
         
      (doseq [sensor cumulative-sensors]
        (is (bc/labelled-correctly? sensor (transform-measurements (g/measurements sensor)))))
     
      (doseq [sensor cumulative-sensors]
        (is (= false (bc/labelled-correctly? sensor (transform-measurements (g/mislabelled-measurements sensor))))))

      (doseq [sensor instant-sensors]
        (is (bc/labelled-correctly? sensor (transform-measurements (g/measurements sensor)))))

      (doseq [sensor pulse-sensors]
        (is (bc/labelled-correctly? sensor (transform-measurements (g/measurements sensor)))))

      (doseq [sensor pulse-sensors]
        (is (= false (bc/labelled-correctly? sensor (transform-measurements (g/mislabelled-measurements sensor)))))))))

;;; 200 x median ;;;

(deftest large-median
  (let [cumulative-sensors (g/generate-sensor-sample "CUMULATIVE" 3)
        instant-sensors    (g/generate-sensor-sample "INSTANT" 3)]

    (println "Testing large median.")

    (testing "Should find readings that are 200 x median"
      (doseq [sensor cumulative-sensors]
        (let [measurements (g/generate-measurements-above-median sensor)
              median       (bc/median (transform-measurements measurements))]
          (not (empty? (map #(v/larger-than-median median %) measurements))))))

   (testing "Should find no readings that are 200 x median"
      (doseq [sensor cumulative-sensors]
        (let [measurements (g/measurements sensor)
              median       (bc/median (transform-measurements measurements))]
          (empty? (map #(v/larger-than-median median %) measurements)))))))
