(ns kixi.hecuba.scheduler
  "Scheduling functions"
  (:require [clojurewerkz.quartzite.conversion    :as qc]
            [clojurewerkz.quartzite.jobs          :as j]
            [clojurewerkz.quartzite.scheduler     :as qs]
            [clojurewerkz.quartzite.schedule.cron :as cron]
            [clojurewerkz.quartzite.triggers      :as trig]
            [clojure.walk                         :as walk]
            [kixi.hecuba.pipeline                 :refer [submit-item]]
            jig)
  
  (:import (jig Lifecycle)))

(j/defjob SubmitToPipeJob
  [ctx]
  (let [item (walk/keywordize-keys (qc/from-job-data ctx))
        pipeline (:pipeline item)
        item (dissoc item :pipeline)]
    (println "Submitting " item)
    (submit-item pipeline item)))

(defn build-job [pipeline item id]
    (j/build
     (j/of-type SubmitToPipeJob)
     (j/using-job-data (walk/stringify-keys (assoc item :pipeline (:head pipeline))))
     (j/with-identity (j/key (format "jobs.process-jobs.%02d" id)))))

(defn build-trigger [cron-str id]
   (trig/build
    (trig/with-identity (trig/key (format "triggers.%02d" id)))
    (trig/start-now)
    (trig/with-schedule (cron/schedule
                      (cron/cron-schedule cron-str)))))

(defn configure-scheduler [config pipeline]
  (let [schedule             (:schedule config)
        process-job-schedule (:process-job-schedule schedule)]
    (qs/initialize)
    (dorun (map-indexed (fn [id [cron-str item]]
                          (qs/schedule (build-job pipeline item id)
                                       (build-trigger cron-str id)))
                        process-job-schedule))))

(deftype Scheduler [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [pipeline (get-in system [:hecuba/pipeline :kixi.hecuba.pipeline/pipeline])]
      (configure-scheduler config pipeline)
      (qs/start))
    (assoc-in system [(:jig/id config) ::scheduler] (constantly @qs/*scheduler*)))
  (stop [_ system] 
    (qs/shutdown)
    system))
