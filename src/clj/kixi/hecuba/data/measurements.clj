(ns kixi.hecuba.data.measurements
  (:require [clj-time.core          :as t]
            [clj-time.coerce        :as tc]
            [schema.core            :as s]
            [kixi.hecuba.time       :as time]
            [kixi.hecuba.storage.db :as db]
            [qbits.hayt             :as hayt]
            [clojure.tools.logging  :as log]
            [clojure.edn            :as edn]))

(def truthy? #{"true"})

(def SensorId {(s/required-key :device_id) s/Str
               (s/required-key :sensor_id) s/Str
               s/Any s/Any})

(defn sensor_metadata-for [store sensor]
  (s/validate SensorId sensor)
  (let [{:keys [sensor_id device_id]} sensor]
    (db/with-session [session (:hecuba-session store)]
      (first (db/execute session
                         (hayt/select
                          :sensor_metadata
                          (hayt/where [[= :device_id device_id]
                                       [= :sensor_id sensor_id]])))))))

(defn resolve-start-end [store sensor_id device_id start end]
  (mapv tc/to-date-time
        (if (not (and start end))
          (let [sm (sensor_metadata-for store {:sensor_id sensor_id :device_id device_id})
                [lower upper] ((juxt :lower_ts :upper_ts) sm)]
            [(or start lower)
             (or end upper)])
          [start end])))

(defn delete
  "WARNING: This will delete data in 1 month chunks based on the dates
  passed in."
  [sensor start-date end-date session]
  (log/infof "Deleting Measurements for %s from %s to %s" sensor start-date end-date)
  (s/validate SensorId sensor)
  (let [months (time/range->months start-date end-date)
        years  (time/range->years start-date end-date)
        {:keys [device_id sensor_id]} sensor]
    {:measurements
     (db/execute session
                 (hayt/delete :partitioned_measurements
                              (hayt/where [[= :device_id device_id]
                                           [= :sensor_id sensor_id]
                                           [:in :month months]])))
     :hourly_rollups
     (db/execute session
                 (hayt/delete :hourly_rollups
                              (hayt/where [[= :device_id device_id]
                                           [= :sensor_id sensor_id]
                                           ;; FIXME: This should be partitioned by years, but isn't
                                           ;; [:in :year years]
                                           ])))
     :daily_rollups
     (db/execute session
                 (hayt/delete :daily_rollups
                              (hayt/where [[= :device_id device_id]
                                           [= :sensor_id sensor_id]])))}))

(defn all-measurements
  "Returns a sequence of all the measurements for a sensor
   matching (type,device_id). The sequence pages to the database in the
   background. The page size is a clj-time Period representing a range
   in the timestamp column. page size defaults to (clj-time/hours 1)"
  ([store sensor & [opts]]
     (s/validate SensorId sensor)
     (let [{:keys [sensor_id device_id]} sensor
           {:keys [page start end] :or {page (t/hours 1)}} opts
           [start end] (resolve-start-end store sensor_id device_id start end)]
       (when (and start end)
         (let  [next-start (t/plus start page)]
           (db/with-session [session (:hecuba-session store)]
             (lazy-cat (db/execute session
                                   (hayt/select :partitioned_measurements
                                                (hayt/where [[= :device_id device_id]
                                                             [= :sensor_id sensor_id]
                                                             [= :month (time/get-month-partition-key start)]
                                                             [>= :timestamp start]
                                                             [< :timestamp next-start]]))
                                   nil)
                       (when (t/before? next-start end)
                         (all-measurements store sensor_id (merge opts {:start next-start :end end}))))))))))


(defn measurements-for-range
  "Returns a lazy sequence of measurements for a sensor matching type and device_id for a specified
  datetime range."
  [store sensor {:keys [start-date end-date]} page]
  (s/validate SensorId sensor)
  (let [device_id  (:device_id sensor)
        sensor_id  (:sensor_id sensor)
        next-start (t/plus start-date page)]
    (db/with-session [session (:hecuba-session store)]
      (lazy-cat (db/execute session
                               (hayt/select :partitioned_measurements
                                            (hayt/where [[= :device_id device_id]
                                                         [= :sensor_id sensor_id]
                                                         [= :month (time/get-month-partition-key start-date)]
                                                         [>= :timestamp start-date]
                                                         [< :timestamp next-start]]))
                               nil)
                (when (t/before? next-start end-date)
                  (measurements-for-range store sensor {:start-date next-start :end-date end-date} page))))))

(defn hourly-rollups-for-range
  "Returns a lazy sequence of hourly rollups for a sensor matching type and device_id for a specified
  datetime range."
  [store sensor {:keys [start-date end-date]} page]
  (s/validate SensorId sensor)
  (let [device_id  (:device_id sensor)
        sensor_id  (:sensor_id sensor)
        next-start (t/plus start-date page)]
    (db/with-session [session (:hecuba-session store)]
      (lazy-cat (db/execute session
                               (hayt/select :hourly_rollups
                                            (hayt/where [[= :device_id device_id]
                                                         [= :sensor_id sensor_id]
                                                         [= :year (time/get-year-partition-key start-date)]
                                                         [>= :timestamp start-date]
                                                         [< :timestamp next-start]]))
                               nil)
                (when (t/before? next-start end-date)
                  (hourly-rollups-for-range store sensor {:start-date next-start :end-date end-date} page))))))

(defn- retrieve-measurements-for-months [session [month & more]  where]
  (log/info "Got month " month)
  (when month
    (lazy-cat (db/execute session
                          (hayt/select :partitioned_measurements
                                       (hayt/where (conj where [= :month month]))))
              (when (seq more)
                (retrieve-measurements-for-months session more where)))))

(defn retrieve-measurements
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [session start-date end-date device-id sensor_id]
  (let [range  (time/time-range start-date end-date (t/months 1))
        months (map #(time/get-month-partition-key (tc/to-date %)) range)
        where  [[= :device_id device-id]
                [= :sensor_id sensor_id]
                [>= :timestamp (tc/to-date start-date)]
                [<= :timestamp (tc/to-date end-date)]]]
    (retrieve-measurements-for-months session months where)))

(defn prepare-batch
  "Creates a CQL batch statement. For performance reasons
   it should be passed a page of measurements."
  [measurements]
  (hayt/batch
   (apply hayt/queries (map #(hayt/insert :partitioned_measurements (hayt/values %)) measurements))
   (hayt/logged false)))

(defn insert-batch [session batch]
  (let [batch-statement (prepare-batch batch)]
    (db/execute session batch-statement)))

(defn insert-measurements
  "Takes store, lazy sequence of measurements and
   size of the batches and inserts them into the database."
  [store sensor page measurements]
  (db/with-session [session (:hecuba-session store)]
    (reduce (fn [{:keys [min-date max-date]} batch]
              (let [dates (time/min-max-dates batch)
                    new-min (:min-date dates)
                    new-max (:max-date dates)]
                (insert-batch session batch)
                {:min-date (if (t/before? new-min min-date) new-min min-date)
                 :max-date (if (t/after? new-max max-date) new-max max-date)}))
            {:min-date (tc/from-date (:timestamp (first measurements)))
             :max-date (tc/from-date (:timestamp (first measurements)))} (partition-all page measurements))))

(defn parse-double [txt]
  (Double/parseDouble txt))

(defn metadata-is-number? [{:keys [reading_metadata] :as m}]
  (truthy? (get reading_metadata "is-number")))

(defn metadata-is-spike? [{:keys [reading_metadata] :as m}]
  (truthy? (get reading_metadata "median-spike")))


(defn parse-value
  "AMON API specifies that when value is not present, error must be returned and vice versa."
  [measurement]
  (let [value (:value measurement)
        convert-fn (fn [v] (if (metadata-is-number? measurement) (read-string v) v))]
    (if-not (empty? value)
      (-> measurement
          (update-in [:value] convert-fn)
          (dissoc :error))
      (dissoc measurement :value))))

(defn sort-measurments
  [m]
  (sort-by :timestamp m) m)

(defn parse-measurements
  "Takes measurements in the format returned from the database.
   Returns a list of maps, with all values parsed approprietly."
  [measurements]
  (map (fn [m] (assoc-in m [:value]
                         (let [n (if (number? (:value m)) 
                                   (:value m)
                                   (try 
                                     (Double/valueOf (:value m))
                                     (catch NumberFormatException e nil
                                            (log/errorf e "> NumberFormatException in parse-measurements - %s %s [%s] " (:sensor_id m) (:timestamp m) (:value m)))
                                     (catch NullPointerException e nil
                                            (log/errorf e "> NumberFormatException in parse-measurements - %s %s [%s] " (:sensor_id m) (:timestamp m) (:value m)))))] 
                           (if (number? n)
                             n
                             nil))))
       measurements))

(defn where
  "Takes a map of clauses for database query and returns
  a vector of vectors, where :start and :end keys are
  renamed to :timestamp. Follows Hayt rules."
  [m]
  (mapv (fn [[k v :as item]] (case k
                     :start [>= :timestamp v]
                     :end [< :timestamp v]
                     (into [=] item))) (mapv vec m)))

(defn fetch-measurements
  "Fetches measurements using given where clause and fetch size.
  If fetch size is not provided, uses the default 100.
  Returns a lazy sequence of measurements."
  [store args & [opts]]
  {:pre [(map? args)]}
  (let [{:keys [fetch-size] :or {fetch-size 100}} opts]
    (db/with-session [session (:hecuba-session store)]
      (db/execute session
                  (hayt/select :partitioned_measurements
                               (hayt/where (where args)))
                  {:fetch-size fetch-size}))))
