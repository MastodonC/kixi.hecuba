(ns kixi.hecuba.controller.pipeline
  "Pipeline for scheduled jobs."
  (:require [kixipipe.pipeline                      :refer [defnconsumer produce-item produce-items submit-item] :as p]
            [pipejine.core                          :as pipejine :refer [new-queue producer-of]]
            [clojure.tools.logging                  :as log]
            [kixipipe.storage.s3                    :as s3]
            [kixi.hecuba.data.batch-checks          :as checks]
            [kixi.hecuba.data.misc                  :as misc]
            [kixi.hecuba.data.calculate             :as calculate]
            [kixi.hecuba.data.calculated-fields     :as fields]
            [kixi.hecuba.data.measurements.download :as measurements-download]
            [kixi.hecuba.data.measurements.upload   :as measurements-upload]
            [kixi.hecuba.data.entities.upload       :as entities-upload]
            [clj-time.core                          :as t]
            [com.stuartsierra.component             :as component]
            [kixi.hecuba.api.datasets               :as datasets]))

(defn build-pipeline [store]
  (let [fanout-q                (new-queue {:name "fanout-q" :queue-size 50})
        data-quality-q          (new-queue {:name "data-quality-q" :queue-size 50})
        median-calculation-q    (new-queue {:name "median-calculation-q" :queue-size 50})
        mislabelled-sensors-q   (new-queue {:name "mislabelled-sensors-q" :queue-size 50})
        rollups-q               (new-queue {:name "rollups-q" :queue-size 50})
        spike-check-q           (new-queue {:name "spike-check-q" :queue-size 50})
        synthetic-readings-q    (new-queue {:name "synthetic-readings-q" :queue-size 50})
        resolution-q            (new-queue {:name "resolution-q" :queue-size 50})
        difference-series-q     (new-queue {:name "difference-series-q" :queue-size 50})
        convert-to-co2-q        (new-queue {:name "convert-to-co2-q" :queue-size 50})
        convert-to-kwh-q        (new-queue {:name "convert-to-kwh-q" :queue-size 50})
        sensor-status-q         (new-queue {:name "sensor-status" :queue-size 50})
        actual-annual-q         (new-queue {:name "actual-annual-q" :queue-size 50})
        store-upload-s3-q       (new-queue {:name "store-upload-s3-q" :queue-size 50})
        upload-measurements-q   (new-queue {:name "upload-measurements-q" :queue-size 10})
        upload-images-q         (new-queue {:name "image-upload-q" :queue-size 10})
        upload-documents-q      (new-queue {:name "document-upload-q" :queue-size 10})
        download-measurements-q (new-queue {:name "download-measurements-q" :queue-size 50 :number-of-consumer-threads 4})]

    (defnconsumer fanout-q [{:keys [dest type] :as item}]
      (let [item (dissoc item :dest)]
        (condp = dest
          :data-quality (condp = type
                          :median-calculation  (produce-item item median-calculation-q)
                          :mislabelled-sensors (produce-item item mislabelled-sensors-q)
                          :spike-check         (produce-item item spike-check-q)
                          :resolution          (produce-item item resolution-q)
                          :sensor-status       (produce-item item sensor-status-q))
          :calculated-datasets (condp = type
                                 :rollups            (produce-item item rollups-q)
                                 :synthetic-readings (produce-item item synthetic-readings-q)
                                 :difference-series  (produce-item item difference-series-q)
                                 :convert-to-co2     (produce-item item convert-to-co2-q)
                                 :convert-to-kwh     (produce-item item convert-to-kwh-q))
          :calculated-fields (condp = type
                               :actual-annual (produce-item item actual-annual-q))
          :upload (condp = type
                    :measurements (produce-item item upload-measurements-q)
                    :images (produce-item item upload-images-q)
                    :documents (produce-item item upload-documents-q))
          :download (condp = type
                      :measurements (produce-item item download-measurements-q)))))

    (defnconsumer median-calculation-q [item]
      (log/info "Starting median calculation.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :median_calc_check s where)
                new-item  (assoc item :sensor s :range range)]
            (when period
              (when (and range (not= period "PULSE"))
                (log/info  "Calculating median for: " device_id type)
                (checks/median-calculation store new-item)
                (misc/reset-date-range store s :median_calc_check (:start-date range) (:end-date range)))))))
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
            (when (and range (some #{period} ["INSTANT" "PULSE" "CUMULATIVE"]))
              (log/info  "Checking if mislabelled: " device_id type)
              (checks/mislabelled-sensors store new-item)
              (misc/reset-date-range store s :mislabelled_sensors_check (:start-date range) (:end-date range))))))
      (log/info "Finished mislabelled sensors check."))

    (defnconsumer difference-series-q [item]
      (log/info "Starting calculation of difference series.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [device_id (:device_id s)
                type      (:type s)
                period    (:period s)
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :difference_series s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (= "CUMULATIVE" period))
              (log/debugf "Started calculating Difference Series for Sensor: %s:%s Start: %s End: %s" device_id type (:start-date range) (:end-date range))
              (try
                (calculate/difference-series store new-item)
                (misc/reset-date-range store s :difference_series (:start-date range) (:end-date range))
                (catch Throwable t
                  (log/errorf t "FAILED to calculate Difference Series for Sensor: %s:%s Start: %s End: %s" device_id type (:start-date range) (:end-date range))
                  (throw t)))
              (log/debugf "COMPLETED calculating Difference Series for Sensor: %s:%s Start: %s End: %s" device_id type (:start-date range) (:end-date range))))))
      (log/info "COMPLETED calculation of difference series."))

    (defnconsumer convert-to-co2-q [item]
      (log/info "Starting conversion from kWh to co2.")
      (let [sensors              (misc/all-sensors store)
            substring?           (fn [sub st] (not= (.indexOf st sub) -1))
            regex-seq            ["oil" "gas" "electricity" "kwh"]
            should-convert-type? (fn [type] (some #(substring? % type) regex-seq))]
        (doseq [s sensors]
          (let [{:keys [device_id type unit period]} s
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :co2 s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range
                       unit
                       (= "KWH" (.toUpperCase unit))
                       (= "PULSE" period)
                       (should-convert-type? type))
              (log/info  "Converting to co2: " device_id type)
              (calculate/kWh->co2 store new-item)
              (misc/reset-date-range store s :co2 (:start-date range) (:end-date range))))))
      (log/info "Finished conversion from kWh to co2."))

    (defnconsumer convert-to-kwh-q [item]
      (log/info "Starting conversion from vol to kwh.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [{:keys [device_id type unit period]} s
                where     {:device_id device_id :type type}
                range     (misc/start-end-dates :kwh s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range
                       unit
                       (some #(= (.toUpperCase unit) (.toUpperCase %)) ["m^3" "ft^3"])
                       (some #(= type %) ["gasConsumption" "oilConsumption"]))
              (log/info  "Converting to kWh: " device_id type)
              (calculate/gas-volume->kWh store new-item)
              (misc/reset-date-range store s :kwh (:start-date range) (:end-date range))))))
      (log/info "Finished conversion from vol to kwh."))

    (defnconsumer rollups-q [item]
      (log/info "Starting rollups.")
      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (let [device_id  (:device_id s)
                type       (:type s)
                period     (:period s)]
            (when period
              (let [where      {:device_id device_id :type type}
                    range      (misc/start-end-dates :rollups s where)
                    new-item   (assoc item :sensor s :range range)]
                (when range
                  (log/debugf "Started Rolling Up for Sensor: %s:%s Start: %s End: %s" device_id type (:start-date range) (:end-date range))
                  (try
                    (calculate/hourly-rollups store new-item)
                    (calculate/daily-rollups store new-item)
                    (misc/reset-date-range store s :rollups (:start-date range) (:end-date range))
                    (catch Throwable t
                      (log/errorf t "FAILED to Roll Up for Sensor: %s:%s Start: %s End: %s" device_id type (:start-date range) (:end-date range))
                      (throw t)))
                  (log/debugf "COMPLETED Rolling Up for Sensor: %s:%s Start: %s End: %s" device_id type (:start-date range) (:end-date range))))))))
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
      (log/info "Starting synthetic readings job.")
      (let [datasets (datasets/all-datasets store)]
        (doseq [ds datasets]
          (let [sensors (datasets/sensors-for-dataset ds store)
                {:keys [device_id name]} ds
                where  {:device_id device_id :type name}
                sensor (misc/all-sensor-information store device_id name)]
            (when-let [range (misc/start-end-dates :calculated_datasets sensor where)]
              (calculate/calculate-dataset store ds sensors range)))))
      (log/info "Finished synthetic readings job."))

    (defnconsumer actual-annual-q [item]
      (log/info "Starting calculation of actual annual use.")

      (let [sensors (misc/all-sensors store)]
        (doseq [s sensors]
          (when (and (= (:actual_annual s) true)
                     (not= (:period s) "CUMULATIVE"))
            (let [{:keys [device_id type period]} s
                  where {:device_id device_id :type type}
                  range (misc/start-end-dates :actual_annual_calculation s where)]

              (when-let [new-range (misc/dates-overlap? range (t/months 12))]
                (fields/calculate store s :actual-annual
                                  "actual_annual_12months"
                                  new-range))

              (when-let [new-range (misc/dates-overlap? range (t/months 1))]
                (prn "dates overlap")
                (fields/calculate store s :actual-annual
                                  "actual_annual_1month"
                                  new-range))

              (misc/reset-date-range store s :actual_annual_calculation (:start-date range) (:end-date range))))))

      (log/info "Finished calculation of actual annual use."))

    (defnconsumer resolution-q [item]
      (log/info "Starting resolution check.")
      (calculate/resolution store item)
      (log/info "Finished resolution check."))

    (defnconsumer sensor-status-q [item]
      (log/info "Starting sensor status check.")
      (let [sensors (misc/all-sensors store)
            end     (t/now)
            start   (t/minus end (t/days 1))]
        (doseq [s sensors]
          (checks/sensor-status store {:sensor s :range {:start-date start :end-date end}})))
      (log/info "Finished sensor status check"))

    (defnconsumer store-upload-s3-q [item]
      (s3/store-file (:s3 store) item)
      (produce-item item upload-measurements-q))

    (defnconsumer upload-measurements-q [item]
      (measurements-upload/upload-item store item))

    (defnconsumer download-measurements-q [item]
      (measurements-download/download-item store item))

    (defnconsumer upload-images-q [item]
      (entities-upload/image-upload store item))

    (defnconsumer upload-documents-q [item]
      (entities-upload/document-upload store item))

    (producer-of fanout-q median-calculation-q mislabelled-sensors-q spike-check-q rollups-q synthetic-readings-q
                 resolution-q difference-series-q convert-to-co2-q convert-to-kwh-q sensor-status-q actual-annual-q
                 upload-measurements-q)

    (list fanout-q #{median-calculation-q mislabelled-sensors-q spike-check-q rollups-q synthetic-readings-q
                     resolution-q difference-series-q convert-to-co2-q convert-to-kwh-q sensor-status-q actual-annual-q
                     upload-measurements-q})))

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
