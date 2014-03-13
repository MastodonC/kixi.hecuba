(ns kixi.hecuba.pipeline
  "Pipeline for scheduled jobs."
  (:require [pipejine.core         :as pipe]
            [clojure.tools.macro   :as mac]
            [clojure.tools.logging :as log]
            [clojure.stacktrace    :as st]
            [kixi.hecuba.data.batch-checks :as checks]
            [kixi.hecuba.data.calculate :as calculate]))

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
        spike-check-q         (pipe/new-queue {:name "spike-check-q" :queue-size 50})]

    (defnconsumer fanout-q [{:keys [dest type] :as item}]
      (let [item (dissoc item :dest)]
        (condp = dest
          :data-quality (condp = type
                          :median-calculation  (produce-item item median-calculation-q)
                          :mislabelled-sensors (produce-item item mislabelled-sensors-q)
                          :spike-check         (produce-item item spike-check-q))
          :calculated-datasets (condp = type
                                 :difference-series (produce-item item difference-series-q)))))

    (defnconsumer median-calculation-q [item]
      (produce-item
       (checks/median-calculation commander querier item)))

    (defnconsumer mislabelled-sensors-q [item]
      (produce-item
       (checks/mislabelled-sensors commander querier item)))

    (defnconsumer difference-series-q [item]
      (produce-item
       (calculate/difference-series commander querier item)))

    (defnconsumer spike-check-q [item]
      (produce-item
       (checks/median-spike-batch-check commander querier item)))

    (pipe/producer-of fanout-q median-calculation-q mislabelled-sensors-q spike-check-q difference-series-q)

    (list fanout-q #{median-calculation-q mislabelled-sensors-q spike-check-q difference-series-q})))

#_(deftype Pipeline [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [commander (-> system :commander)
          querier   (-> system :querier)
          [head others] (build-pipeline commander querier)]
      (-> system
          (assoc-in [(:jig/id config) ::pipeline] {:head head})
          (assoc-in [(:jig/id config) :pipeline] {:others others}))))
  (stop [_ system] system))
