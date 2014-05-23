(ns kixi.hecuba.controller.pipeline
  "Pipeline for scheduled jobs."
  (:require [kixipipe.pipeline             :refer [defnconsumer produce-item produce-items submit-item] :as p]
            [pipejine.core                 :as pipejine :refer [new-queue producer-of]]
            [clojure.tools.logging         :as log]
            [kixi.hecuba.data.batch-checks :as checks]
            [kixi.hecuba.data.misc         :as misc]
            [kixi.hecuba.data.calculate    :as calculate]
            [com.stuartsierra.component    :as component]))

(defn build-pipeline [commander querier store]
  (let [fanout-q              (new-queue {:name "fanout-q" :queue-size 50})
        data-quality-q        (new-queue {:name "data-quality-q" :queue-size 50})
        median-calculation-q  (new-queue {:name "median-calculation-q" :queue-size 50})
        mislabelled-sensors-q (new-queue {:name "mislabelled-sensors-q" :queue-size 50})
        rollups-q             (new-queue {:name "rollups-q" :queue-size 50})
        spike-check-q         (new-queue {:name "spike-check-q" :queue-size 50})
        synthetic-readings-q  (new-queue {:name "synthetic-readings-q" :queue-size 50})
        resolution-q          (new-queue {:name "resolution-q" :queue-size 50})
        diff-series-res-q     (new-queue {:name "diff-series-res-q" :queue-size 50})
        ]

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
                                 :diff-series-res    (produce-item item diff-series-res-q)
                                 ))))

    (defnconsumer median-calculation-q [item]
      (log/info "Starting median calculation.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                table     (case period
                            "CUMULATIVE" :difference_series
                            "INSTANT"    :measurement
                            "PULSE"      :measurement)
                range     (misc/start-end-dates :median_calc_check s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (not= period "PULSE"))
              (checks/median-calculation commander querier table new-item)
              (misc/reset-date-range querier commander s :median_calc_check (:start-date range) (:end-date range))))))
      (log/info "Finished median calculation."))

    (defnconsumer mislabelled-sensors-q [item]
      (log/info "Starting mislabelled sensors check.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :mislabelled_sensors_check s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (checks/mislabelled-sensors commander querier new-item)
              (misc/reset-date-range querier commander s :mislabelled_sensors_check (:start-date range) (:end-date range))))))
      (log/info "Finished mislabelled sensors check."))

    (defnconsumer diff-series-res-q [item]
      (log/info "Starting calculation of difference series from resolution.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :difference_series s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (calculate/difference-series-from-resolution store new-item)
              (misc/reset-date-range querier commander s :difference_series (:start-date range) (:end-date range))))))
      (log/info "Finished calculation of difference series from resolution."))

    (defnconsumer rollups-q [item]
      (log/info "Starting rollups.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device_id  (:device_id s)
                type       (:type s)
                period     (:period s)
                table      (case period
                             "CUMULATIVE" :difference_series
                             "INSTANT"    :measurement
                             "PULSE"      :measurement)
                where      {:device_id device_id :type type}
                range      (misc/start-end-dates :rollups s where)
                new-item   (assoc item :sensor s :range range)]
            (when range
              (calculate/hourly-rollups commander querier new-item)
              (calculate/daily-rollups commander querier new-item)
              (misc/reset-date-range querier commander s :rollups (:start-date range) (:end-date range))))))
      (log/info "Finished rollups."))

    (defnconsumer spike-check-q [item]
      (log/info "Starting median spike check.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :spike_check s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (not= period "PULSE"))
              (checks/median-spike-check commander querier new-item)
              (misc/reset-date-range querier commander s :spike_check (:start-date range) (:end-date range))))))
      (log/info "Finished median spike check."))

    (defnconsumer synthetic-readings-q [item]
      (calculate/generate-synthetic-readings store item))

    (defnconsumer resolution-q [item]
      (log/info "Starting resolution check.")
      (calculate/resolution store item)
      (log/info "Finished resolution check."))

    (producer-of fanout-q median-calculation-q mislabelled-sensors-q spike-check-q rollups-q synthetic-readings-q
                 resolution-q diff-series-res-q)

    (list fanout-q #{median-calculation-q mislabelled-sensors-q spike-check-q rollups-q synthetic-readings-q
                     resolution-q diff-series-res-q})))

(defrecord Pipeline []
  component/Lifecycle
  (start [this]
    (log/info "Pipeline starting")
    (let [commander     (-> this :store :commander)
          querier       (-> this :store :querier)
          store         (-> this :store-new)
          [head others] (build-pipeline commander querier store)]
      (-> this
          (assoc :head head)
          (assoc :others others))))
  (stop [this] this))

(defn new-pipeline []
  (->Pipeline))
