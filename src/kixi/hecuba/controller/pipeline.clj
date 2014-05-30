(ns kixi.hecuba.controller.pipeline
  "Pipeline for scheduled jobs."
  (:require [kixipipe.pipeline             :refer [defnconsumer produce-item produce-items submit-item] :as p]
            [pipejine.core                 :as pipejine :refer [new-queue producer-of]]
            [clojure.tools.logging         :as log]
            [kixi.hecuba.data.batch-checks :as checks]
            [kixi.hecuba.data.misc         :as misc]
            [kixi.hecuba.data.calculate    :as calculate]
            [com.stuartsierra.component    :as component]))

(defn build-pipeline [store]
  (let [fanout-q              (new-queue {:name "fanout-q" :queue-size 50})
        data-quality-q        (new-queue {:name "data-quality-q" :queue-size 50})
        median-calculation-q  (new-queue {:name "median-calculation-q" :queue-size 50})
        mislabelled-sensors-q (new-queue {:name "mislabelled-sensors-q" :queue-size 50})
        rollups-q             (new-queue {:name "rollups-q" :queue-size 50})
        spike-check-q         (new-queue {:name "spike-check-q" :queue-size 50})
        synthetic-readings-q  (new-queue {:name "synthetic-readings-q" :queue-size 50})
        resolution-q          (new-queue {:name "resolution-q" :queue-size 50})
        difference-series-q   (new-queue {:name "difference-series" :queue-size 50})]

    (defnconsumer fanout-q [{:keys [dest type] :as item}]
      (let [item (dissoc item :dest)]
        (condp = dest
          :data-quality (condp = type
                          :median-calculation  (produce-item item median-calculation-q)
                          :mislabelled-sensors (produce-item item mislabelled-sensors-q)
                          :spike-check         (produce-item item spike-check-q)
                          :resolution          (produce-item item resolution-q))
          :calculated-datasets (condp = type
                                 :rollups            (produce-item item rollups-q)
                                 :synthetic-readings (produce-item item synthetic-readings-q)
                                 :difference-series  (produce-item item difference-series-q)
                                 ))))

    (defnconsumer median-calculation-q [item]
      (log/info "Starting median calculation.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                table     (case period
                            "CUMULATIVE" :difference_series
                            "INSTANT"    :measurements
                            "PULSE"      :measurements)
                range     (misc/start-end-dates :median_calc_check s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (not= period "PULSE"))
              (checks/median-calculation store table new-item)
              (misc/reset-date-range store s :median_calc_check (:start-date range) (:end-date range))))))
      (log/info "Finished median calculation."))

    (defnconsumer mislabelled-sensors-q [item]
      (log/info "Starting mislabelled sensors check.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :mislabelled_sensors_check s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (checks/mislabelled-sensors store new-item)
              (misc/reset-date-range store s :mislabelled_sensors_check (:start-date range) (:end-date range))))))
      (log/info "Finished mislabelled sensors check."))

    (defnconsumer difference-series-q [item]
      (log/info "Starting calculation of difference series from resolution.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :difference_series s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (calculate/difference-series-from-resolution store new-item)
              (misc/reset-date-range store s :difference_series (:start-date range) (:end-date range))))))
      (log/info "Finished calculation of difference series from resolution."))

    (defnconsumer rollups-q [item]
      (log/info "Starting rollups.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [device_id  (:device_id s)
                type       (:type s)
                period     (:period s)
                table      (case period
                             "CUMULATIVE" :difference_series
                             "INSTANT"    :measurements
                             "PULSE"      :measurements)
                where      {:device_id device_id :type type}
                range      (misc/start-end-dates :rollups s where)
                new-item   (assoc item :sensor s :range range)]
            (when range
              (calculate/hourly-rollups store new-item)
              (calculate/daily-rollups store new-item)
              (misc/reset-date-range store s :rollups (:start-date range) (:end-date range))))))
      (log/info "Finished rollups."))

    (defnconsumer spike-check-q [item]
      (log/info "Starting median spike check.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (when (and (not (nil? (:median s))) (not (zero? (:median s))))
            (let [device_id (:device_id s)
                  type      (:type s)
                  period    (:period s)
                  where     {:device_id device_id :type type}
                  range     (misc/start-end-dates :spike_check s where)
                  new-item  (assoc item :sensor s :range range)]
              (when (and range (not= period "PULSE"))
                (checks/median-spike-check store new-item)
                (misc/reset-date-range store s :spike_check (:start-date range) (:end-date range)))))))
      (log/info "Finished median spike check."))

    (defnconsumer synthetic-readings-q [item]
      (calculate/generate-synthetic-readings store item))

    (defnconsumer resolution-q [item]
      (log/info "Starting resolution check.")
      (calculate/resolution store item)
      (log/info "Finished resolution check."))

    (producer-of fanout-q median-calculation-q mislabelled-sensors-q spike-check-q rollups-q synthetic-readings-q
                 resolution-q difference-series-q)

    (list fanout-q #{median-calculation-q mislabelled-sensors-q spike-check-q rollups-q synthetic-readings-q
                     resolution-q difference-series-q})))

(defrecord Pipeline []
  component/Lifecycle
  (start [this]
    (log/info "Pipeline starting")
    (let [store         (-> this :store)
          [head others] (build-pipeline store)]
      (-> this
          (assoc :head head)
          (assoc :others others))))
  (stop [this] this))

(defn new-pipeline []
  (->Pipeline))
