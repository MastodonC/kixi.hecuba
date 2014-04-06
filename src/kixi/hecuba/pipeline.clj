(ns kixi.hecuba.pipeline
  "Pipeline for scheduled jobs."
  (:require [pipejine.core                 :as pipe]
            [clojure.tools.macro           :as mac]
            [clojure.tools.logging         :as log]
            [clojure.stacktrace            :as st]
            [kixi.hecuba.data.batch-checks :as checks]
            [kixi.hecuba.data.misc         :as misc]
            [kixi.hecuba.data.calculate    :as calculate]
            [com.stuartsierra.component    :as component]))

(defn submit-item [pipe job]
  (pipe/produce pipe job))

(defn shutdown-pipe [pipe]
  (pipe/shutdown pipe))

(defn pipe-produce-done [pipe]
  (pipe/produce-done pipe))

(defn produce-item [item & qs]
  (doseq [q qs]
    (pipe/produce q item)))

(defn produce-items [items & qs]
  (doseq [q qs]

    (doseq [item items]
      (pipe/produce q item))))

(defmacro defnconsumer
  "Defines a fn that will consume the given queue"
  [q & fn-tail]
  (let [[fn-name [args & body]] (mac/name-with-attributes q fn-tail)
        fn# (symbol (str (name q) "-consumer"))]
    `(pipe/spawn-consumers
      ~q
      ;; This is somewhat convoluted and maybe simplifiable.
      ;; Goals for any rewrite.
      ;;   + Terse definition of pipeline with common exception handling 'built-in'
      ;;   + Logging of items when the enter each pipeline node/consumer.
      ;;   + Logging of the failing item close to the exception logging, so when error
      ;;     is emailed to support address, can work out what needs to be done ASAP.
      ;;   + Good stacktraces with few anonymous functions.
      (fn [item#]
        (log/info "Got " item#)
        (let [log-exception#
              (fn [t#]
                (let [stacktrace# (java.io.StringWriter.)]
                  (binding [*out* stacktrace#]
                    (println "Error processing " (pr-str item#))
                    (st/print-stack-trace t#)
                    (log/error (str stacktrace#)))))
              f# (fn ~fn# ~args
                  (let []
                    (try
                      ~@body
                      (catch java.sql.SQLException sqle#
                        (let [e# (or (.getNextException sqle#) sqle#)]
                          (log-exception# e#))
                        nil)
                      (catch Throwable t#
                        (log-exception# t#)
                        nil))))]
          (f# item#))))))

(defn build-pipeline [commander querier]
  (let [fanout-q              (pipe/new-queue {:name "fanout-q" :queue-size 50})
        data-quality-q        (pipe/new-queue {:name "data-quality-q" :queue-size 50})
        median-calculation-q  (pipe/new-queue {:name "median-calculation-q" :queue-size 50})
        mislabelled-sensors-q (pipe/new-queue {:name "mislabelled-sensors-q" :queue-size 50})
        difference-series-q   (pipe/new-queue {:name "difference-series-q" :queue-size 50})
        rollups-q             (pipe/new-queue {:name "rollups-q" :queue-size 50})
        spike-check-q         (pipe/new-queue {:name "spike-check-q" :queue-size 50})
        synthetic-readings-q  (pipe/new-queue {:name "synthetic-readings-q" :queue-size 50})]

    (defnconsumer fanout-q [{:keys [dest type] :as item}]
      (let [item (dissoc item :dest)]
        (condp = dest
          :data-quality (condp = type
                          :median-calculation  (produce-item item median-calculation-q)
                          :mislabelled-sensors (produce-item item mislabelled-sensors-q)
                          :spike-check         (produce-item item spike-check-q))
          :calculated-datasets (condp = type
                                 :difference-series  (produce-item item difference-series-q)
                                 :rollups            (produce-item item rollups-q)
                                 :synthetic-readings (produce-item item synthetic-readings-q)))))

    (defnconsumer median-calculation-q [item]
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
                range     (misc/start-end-dates querier table :median-calc-check s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (not= period "PULSE"))
              (checks/median-calculation commander querier table new-item)
              (misc/reset-date-range querier commander s :median-calc-check (:start-date range) (:end-date range)))))))

    (defnconsumer mislabelled-sensors-q [item]
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device-id (:device-id s)
                type      (:type s)
                period    (:period s)
                where     {:device-id device-id :type type}
                range     (misc/start-end-dates querier :measurement :mislabelled-sensors-check s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (checks/mislabelled-sensors commander querier new-item)
              (misc/reset-date-range querier commander s :mislabelled-sensors-check (:start-date range) (:end-date range)))))))

    (defnconsumer difference-series-q [item]
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device-id (:device-id s)
                type      (:type s)
                period    (:period s)
                where     {:device-id device-id :type type}
                range     (misc/start-end-dates querier :measurement :difference-series s where)
                new-item  (assoc item :sensor s :range range)]
            (when range
              (calculate/difference-series commander querier new-item)
              (misc/reset-date-range querier commander s :difference-series (:start-date range) (:end-date range)))))))

    (defnconsumer rollups-q [item]
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
                range      (misc/start-end-dates querier table :rollups s where)
                new-item   (assoc item :sensor s :range range)]
            (when range
              (calculate/hourly-rollups commander querier new-item)
              (calculate/daily-rollups commander querier new-item)
              (misc/reset-date-range querier commander s :rollups (:start-date range) (:end-date range)))))))

    (defnconsumer spike-check-q [item]
      (let [sensors (misc/all-sensors querier)]
        (doseq [s sensors]
          (let [device-id (:device-id s)
                type      (:type s)
                period    (:period s)
                where     {:device-id device-id :type type}
                range     (misc/start-end-dates querier :measurement :spike-check s where)
                new-item  (assoc item :sensor s :range range)]
            (when (and range (not= period "PULSE"))
              (checks/median-spike-check commander querier new-item)
              (misc/reset-date-range querier commander s :spike-check (:start-date range) (:end-date range)))))))

    (defnconsumer synthetic-readings-q [item]
      (produce-item
       (calculate/generate-synthetic-readings commander querier item)))

    (pipe/producer-of fanout-q median-calculation-q mislabelled-sensors-q spike-check-q difference-series-q rollups-q synthetic-readings-q)

    (list fanout-q #{median-calculation-q mislabelled-sensors-q spike-check-q difference-series-q rollups-q synthetic-readings-q})))

(defrecord Pipeline []
  component/Lifecycle
  (start [this]
    (let [commander (-> this :store :commander)
          querier   (-> this :store :querier)
          [head others] (build-pipeline commander querier)]
      (-> this
          (assoc :head head)
          (assoc :others others))))
  (stop [this] this))

(defn new-pipeline []
  (->Pipeline))
