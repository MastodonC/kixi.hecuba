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
        difference-series-q   (new-queue {:name "difference-series-q" :queue-size 50})
        rollups-q             (new-queue {:name "rollups-q" :queue-size 50})
        spike-check-q         (new-queue {:name "spike-check-q" :queue-size 50})
        synthetic-readings-q  (new-queue {:name "synthetic-readings-q" :queue-size 50})]

    (defnconsumer fanout-q [{:keys [dest type] :as item}]
      (let [item (dissoc item :dest)]
        (condp = dest
          :data-quality (condp = type
                          :median-calculation  (produce-item item median-calculation-q)
                          :mislabelled-sensors (produce-item item mislabelled-sensors-q)
                          :spike-check         (produce-item item spike-check-q))
          :calculated-datasets (condp = type
                                 :difference-series (produce-item item difference-series-q)
                                 :rollups           (produce-item item rollups-q)
                                 :synthetic-readings (produce-item item synthetic-readings-q)
                                 ))))

    (defnconsumer median-calculation-q [item]
      (log/info "Starting median calculation.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device-id (:device-id s)
                type      (:type s)
                period    (:period s)
                where     {:device-id device-id :type type}
                table     (case period
                            "CUMULATIVE" :difference-series
                            "INSTANT"    :measurement
                            "PULSE"      :measurement)
                range     (misc/start-end-dates :median-calc-check s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (not= period "PULSE"))
              (checks/median-calculation commander querier table new-item)
              (misc/reset-date-range querier commander s :median-calc-check (:start-date range) (:end-date range))))))
      (log/info "Finished median calculation."))

    (defnconsumer mislabelled-sensors-q [item]
      (log/info "Starting mislabelled sensors check.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device-id (:device-id s)
                type      (:type s)
                period    (:period s)
                where     {:device-id device-id :type type}
                range     (misc/start-end-dates :mislabelled-sensors-check s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (checks/mislabelled-sensors commander querier new-item)
              (misc/reset-date-range querier commander s :mislabelled-sensors-check (:start-date range) (:end-date range))))))
      (log/info "Finished mislabelled sensors check."))

    (defnconsumer difference-series-q [item]
      (log/info "Starting calculation of difference series.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device-id (:device-id s)
                type      (:type s)
                period    (:period s)
                where     {:device-id device-id :type type}
                range     (misc/start-end-dates :difference-series s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (calculate/difference-series commander querier new-item)
              (misc/reset-date-range querier commander s :difference-series (:start-date range) (:end-date range))))))
      (log/info "Finished calculation of difference series."))

    (defnconsumer rollups-q [item]
      (log/info "Starting rollups.")
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device-id  (:device-id s)
                type       (:type s)
                period     (:period s)
                table      (case period
                             "CUMULATIVE" :difference-series
                             "INSTANT"    :measurement
                             "PULSE"      :measurement)
                where      {:device-id device-id :type type}
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
          (let [device-id (:device-id s)
                type      (:type s)
                period    (:period s)
                where     {:device-id device-id :type type}
                range     (misc/start-end-dates :spike-check s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (not= period "PULSE"))
              (checks/median-spike-check commander querier new-item)
              (misc/reset-date-range querier commander s :spike-check (:start-date range) (:end-date range))))))
      (log/info "Finished median spike check."))

    (defnconsumer synthetic-readings-q [item]
      (calculate/generate-synthetic-readings store item))

    (producer-of fanout-q median-calculation-q mislabelled-sensors-q spike-check-q difference-series-q rollups-q synthetic-readings-q)

    (list fanout-q #{median-calculation-q mislabelled-sensors-q spike-check-q difference-series-q rollups-q synthetic-readings-q})))

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
