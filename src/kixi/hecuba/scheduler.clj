(ns kixi.hecuba.scheduler
  "Scheduling functions"
  (:require [clojurewerkz.quartzite.conversion    :as qc]
            [clojurewerkz.quartzite.jobs          :as j]
            [clojurewerkz.quartzite.scheduler     :as qs]
            [clojurewerkz.quartzite.schedule.cron :as cron]
            [clojurewerkz.quartzite.triggers      :as trig]
            [clojure.walk                         :as walk]
            [kixi.hecuba.pipeline                 :refer [submit-item]]
            [com.stuartsierra.component           :as component]
            ))

(j/defjob SubmitToPipeJob
  [ctx]
  (prn "ctx: " ctx)
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
  (let [process-job-schedule (:process-job-schedule config)]
    (qs/initialize)
    (dorun (map-indexed (fn [id [cron-str item]]
                          (qs/schedule (build-job pipeline item id)
                                       (build-trigger cron-str id)))
                        process-job-schedule))))

(defrecord Scheduler [config]
  component/Lifecycle
  (start [this]
    (let [pipeline (get-in this [:pipeline])]
      (configure-scheduler config pipeline)
      (qs/start))
    (assoc this :scheduler (constantly @qs/*scheduler*)))
  (stop [this]
    (qs/shutdown)
    this))

(defn new-scheduler [config]
  (->Scheduler config))
