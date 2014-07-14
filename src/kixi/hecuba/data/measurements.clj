(ns kixi.hecuba.data.measurements
  (:require [clj-time.core          :as t]
            [clj-time.coerce        :as tc]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.misc  :as misc]
            [qbits.hayt             :as hayt]
            [kixi.hecuba.webutil    :as util]))

(defn sensor_metadata-for [store sensor_id]
  (let [{:keys [type device_id]} sensor_id]
    (db/with-session [session (:hecuba-session store)]
      (first (db/execute session
                         (hayt/select
                          :sensor_metadata
                          (hayt/where [[= :device_id device_id]
                                       [= :type type]])))))))

(defn resolve-start-end [store type device_id start end]
  (mapv tc/to-date-time
        (if (not (and start end))
          (let [sm (sensor_metadata-for store {:type type :device_id device_id})
                [lower upper] ((juxt :lower_ts :upper_ts) sm)]
            [(or start lower)
             (or end upper)])
          [start end])))

(defn all-measurements
  "Returns a sequence of all the measurements for a sensor
   matching (type,device_id). The sequence pages to the database in the
   background. The page size is a clj-time Period representing a range
   in the timestamp column. page size defaults to (clj-time/hours 1)"
  ([store sensor_id & [opts]]
     (let [{:keys [type device_id]} sensor_id
           {:keys [page start end] :or {page (t/hours 1)}} opts
           [start end] (resolve-start-end store type device_id start end)]
       (when (and start end)
         (let  [next-start (t/plus start page)]
           (db/with-session [session (:hecuba-session store)]
             (lazy-cat (db/execute session
                                   (hayt/select :partitioned_measurements
                                                (hayt/where [[= :device_id device_id]
                                                             [= :type type]
                                                             [= :month (misc/get-month-partition-key start)]
                                                             [>= :timestamp start]
                                                             [< :timestamp next-start]]))
                                   nil)
                       (when (t/before? next-start end)
                         (all-measurements store sensor_id (merge opts {:start next-start :end end}))))))))))


(defn measurements-for-range
  "Returns a lazy sequence of measurements for a sensor matching type and device_id for a specified
  datetime range."
  [store sensor {:keys [start-date end-date]} page]
  (let [device_id  (:device_id sensor)
        type       (:type sensor)
        next-start (t/plus start-date page)]
    (db/with-session [session (:hecuba-session store)]
      (lazy-cat (db/execute session
                               (hayt/select :partitioned_measurements
                                            (hayt/where [[= :device_id device_id]
                                                         [= :type type]
                                                         [= :month (misc/get-month-partition-key start-date)]
                                                         [>= :timestamp start-date]
                                                         [< :timestamp next-start]]))
                               nil)
                (when (t/before? next-start end-date)
                  (measurements-for-range store sensor {:start-date next-start :end-date end-date} page))))))

(defn retrieve-measurements
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [session start-date end-date device-id reading-type]
  (let [range  (util/time-range start-date end-date (t/months 1))
        months (map #(util/get-month-partition-key (tc/to-date %)) range)
        where  [[= :device_id device-id]
                [= :type reading-type]
                [>= :timestamp (tc/to-date start-date)]
                [<= :timestamp (tc/to-date end-date)]]]
    (mapcat (fn [month] (db/execute session
                                    (hayt/select :partitioned_measurements
                                                 (hayt/where (conj where [= :month month]))))) months)))
