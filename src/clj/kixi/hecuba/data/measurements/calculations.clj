(ns kixi.hecuba.data.measurements.calculations
  "Collection of generic calculations on measurements."
  (:require [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.profiles     :as profiles]
            [kixi.hecuba.time              :as time]
            [kixi.hecuba.data.calculate    :as c]
            [clj-time.coerce               :as tc]
            [clj-time.core                 :as t]
            [clj-time.periodic             :as tp]
            [kixi.hecuba.data.sensors      :as sensors]
            [kixi.hecuba.data.devices      :as devices]
            [kixi.hecuba.storage.db        :as db]
            [clojure.tools.logging         :as log]))

(defn min-value
  "Finds the least of a sequence of numbers.
  Returns a numerical value."
  [xs]
  (when (seq xs)
    (apply min xs)))

(defn max-value
  "Finds the largest of a sequence of numbers.
  Returns a numerical value."
  [xs]
  (when (seq xs)
    (apply max xs)))

(defn avg-value
  "Finds the average of a sequence of numbers.
  Returns a numerical value."
  [xs]
  (when (seq xs)
    (c/average-reading xs)))

(defn calculate-reading-from-seq
  "Gets a sequence of data for a period of time.
  Filters out numerical values and calculates
  them using provided function."
  [calculation-fn xs]
  (->> xs
       (measurements/parse-measurements)
       (keep :value)
       (calculation-fn)))

(defn morning?
  "Returns true if measurement falls between 5:00 and 10:00.
  Returns false otherwise."
  [x]
  (-> x :timestamp tc/to-date-time time/morning?))

(defn day?
  "Returns true if measurement falls between 10:30 and 17:00.
  Returns false otherwise."
  [x]
  (-> x :timestamp tc/to-date-time time/day?))

(defn evening?
  "Returns true if measurement falls between 17:30 and 23:30.
  Returns false otherwise."
  [x]
  (-> x :timestamp tc/to-date-time time/evening?))

(defn night?
  "Returns true if measurement falls between 00:00 and 04:30.
  Returns false otherwise."
  [x]
  (-> x :timestamp tc/to-date-time time/night?))

(defmulti calculation (fn [calculation data] calculation))

(defmethod calculation :min-for-day [_ data]
  (calculate-reading-from-seq min-value data))

(defmethod calculation :max-for-day [_ data]
  (calculate-reading-from-seq max-value data))

(defmethod calculation :avg-for-day [_ data]
  (calculate-reading-from-seq avg-value data))

(defmethod calculation :min-for-day-morning [_ data]
  (->> data
       (filter morning?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :min-for-day-day [_ data]
  (->> data
       (filter day?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :min-for-day-evening [_ data]
  (->> data
       (filter evening?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :max-for-day-morning [_ data]
  (->> data
       (filter morning?)
       (calculate-reading-from-seq max-value)))

(defmethod calculation :max-for-day-day [_ data]
  (->> data
       (filter day?)
       (calculate-reading-from-seq max-value)))

(defmethod calculation :max-for-day-evening [_ data]
  (->> data
       (filter evening?)
       (calculate-reading-from-seq max-value)))

(defmethod calculation :max-for-day-night [_ data]
  (->> data
       (filter night?)
       (calculate-reading-from-seq max-value)))

(defmethod calculation :min-for-day-night [_ data]
  (->> data
       (filter night?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :avg-for-day-morning [_ data]
  (->> data
       (filter morning?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :avg-for-day-day [_ data]
  (->> data
       (filter day?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :avg-for-day-evening [_ data]
  (->> data
       (filter evening?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :avg-for-day-night [_ data]
  (->> data
       (filter night?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :min-rolling-4-weeks [_ data]
  (calculate-reading-from-seq min-value data))

(defmethod calculation :max-rolling-4-weeks [_ data]
  (calculate-reading-from-seq max-value data))

(defmethod calculation :avg-rolling-4-weeks [_ data]
  (calculate-reading-from-seq avg-value data))

(defmethod calculation :avg-rolling-4-weeks-morning [_ data]
  (->> data
       (filter morning?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :avg-rolling-4-weeks-day [_ data]
  (->> data
       (filter day?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :avg-rolling-4-weeks-evening [_ data]
  (->> data
       (filter evening?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :avg-rolling-4-weeks-night [_ data]
  (->> data
       (filter night?)
       (calculate-reading-from-seq avg-value)))

(defmethod calculation :min-rolling-4-weeks-morning [_ data]
  (->> data
       (filter morning?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :min-rolling-4-weeks-day [_ data]
  (->> data
       (filter day?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :min-rolling-4-weeks-evening [_ data]
  (->> data
       (filter evening?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :min-rolling-4-weeks-night [_ data]
  (->> data
       (filter night?)
       (calculate-reading-from-seq min-value)))

(defmethod calculation :max-rolling-4-weeks-morning [_ data]
  (->> data
       (filter morning?)
       (calculate-reading-from-seq max-value)))

(defmethod calculation :max-rolling-4-weeks-day [_ data]
  (->> data
       (filter day?)
       (calculate-reading-from-seq max-value)))

(defmethod calculation :max-rolling-4-weeks-evening [_ data]
  (->> data
       (filter night?)
       (calculate-reading-from-seq max-value)))

(defmethod calculation :max-rolling-4-weeks-night [_ data]
  (->> data
       (filter morning?)
       (calculate-reading-from-seq max-value)))

(defn calculate-batch
  [store {:keys [device_id type sensor_id period]} start-date end-date calculation-type & [condition]]
  (db/with-session [session (:hecuba-session store)]
    (let [month            (time/get-month-partition-key start-date)
          where            [[= :device_id device_id] [= :sensor_id sensor_id] [= :month month]
                            [>= :timestamp start-date] [< :timestamp end-date]]
          measurements     (measurements/fetch-measurements store where)
          new-type         (sensors/output-type-for type calculation-type)
          output-sensor_id (:sensor_id (sensors/get-by-type {:device_id device_id :type new-type} session))]
      (when (seq measurements)
        (log/infof "Calculating %s for device_id %s and sensor_id %s" calculation-type device_id sensor_id)
        (if output-sensor_id
          (let [calculated-sensor {:device_id device_id :sensor_id output-sensor_id}
                calculated-data   [{:value (str (c/round (calculation calculation-type measurements)))
                                    :timestamp (tc/to-date end-date)
                                    :month (time/get-month-partition-key end-date)
                                    :device_id device_id
                                    :sensor_id output-sensor_id}]]
            (measurements/insert-measurements store calculated-sensor 10 calculated-data)
            (sensors/update-sensor-metadata session calculated-sensor start-date end-date))
          (log/errorf "Could not find the output sensor_id for device_id %s and new type %s" device_id new-type))))))

(defn reading-for-a-day
  "Performs calculation for a single day.
  Works in batches of one day. Result of each batch is
  inserted onto C*."
  [store {:keys [sensor range calculation-type]}]
  (let [{:keys [start-date end-date]} range]
    (doseq [timestamp (time/seq-dates start-date end-date (t/days 1))]
      (calculate-batch store sensor timestamp (t/plus timestamp (t/days 1)) calculation-type))))

(defn reading-rolling-for-4-weeks
  "Performs calculation for a rolling 4 week window.
  Works in batches of a day, and inserts found value into C*."
  [store {:keys [sensor range calculation-type]}]
  (let [{:keys [start-date end-date]} range]
    (doseq [timestamp (time/seq-dates start-date end-date (t/days 1))]
      (calculate-batch store sensor (t/minus timestamp (t/weeks 4)) (t/plus timestamp (t/days 1)) calculation-type))))

(defn read-tariffs
  "Reads profiles from the database and extracts tariffs."
  [store entity_id]
  (db/with-session [session (:hecuba-session store)]
    (let [profiles (profiles/get-profiles entity_id session)]
      (map #(select-keys % [:timestamp :profile_data]) profiles))))

(defn tariff-ranges
  "Filters timestamps from tariffs, and creates ranges of datetimes
  that span each tariff."
  [tariffs]
  (let [tarrifs-count (count tariffs)]
    (map-indexed (fn [idx item]
                   (let [next (inc idx)]
                     (hash-map :start (tc/to-date-time (:timestamp item))
                               :end (if (< next tarrifs-count)
                                      (tc/to-date-time (:timestamp (nth tariffs next)))
                                      (time/update-timestamp (t/now) {:hour 0 :minutes 0 :seconds 0
                                                                      :milliseconds 0}))
                               :tariff (-> item :profile_data :tariff)))) tariffs)))

(defn match-tariff
  "Finds tariff that matches period of a measurement."
  [m tariffs]
  (let [t (-> m :timestamp tc/to-date-time)]
    (some #(when (and (:tariff %)
                      (time/in-range? t (:start %) (:end %)))
             (:tariff %)) (tariff-ranges tariffs))))

(defn discounted-standing-charge
  "Gets daily standing charge and total annual lump-sum discount
  and calculates discounted standing charge.
  Returns a numerical value."
  [daily-standing-charge annual-lump-sum-discount]
  (float (- daily-standing-charge (/ annual-lump-sum-discount 365))))

(defmulti apply-tariff
  "Processes one day's worth of data (differenceSeries),
  works out unit tariff from the tariff map provided and
  multiplies it by the consumption for each period.
  Returns a sequence of expenditure measurements."
  (fn [tariff _]
    (condp #(%1 %2) tariff
      :off-peak-periods :on-off-peak
      :cost-per-kwh     :simple
      :none)))

(defn day->on-off-periods
  "Takes a sequence of off peak start and end dates, e.g.
  [{:start \"00:00\" :end \"05:00\"}
  {:start \"22:00\" :end \"23:59\"}]
  and splits measurements into two groups: on and off peak.
  Returns a map with :on and :off keywords and two sequences
  of measurements."
  [off-peak-periods measurements]
  (let [xs (group-by #(if (some (fn [period]
                                  (time/in-interval? (tc/to-date-time (:timestamp %)) period))
                                off-peak-periods)
                        :off :on)
                     measurements)]
    xs))

(defn apply-on-off-peak
  "Accepts on and off peak cost values and a sequence of
  measurements grouped by the peak, first one being on,
  second off peak.
  Multiplies approppriate costs by usages and returns a single
  numerical value."
  [on-peak-cost off-peak-cost [on off]]
  (cond
    (and on off) (+ (* on-peak-cost on) (* off-peak-cost off))
    on           (* on-peak-cost on)
    off          (* off-peak-cost off)))

(defmethod apply-tariff :on-off-peak [tariff measurements]
  (let [{:keys [cost-per-kwh off-peak-periods
                cost-per-on-peak-kwh cost-per-off-peak-kwh]} tariff]
    (->> measurements
         measurements/parse-measurements
         (day->on-off-periods off-peak-periods)
         (map (fn [[period xs]]
                (apply + (map :value (filter #(number? (:value %)) xs)))))
         (apply-on-off-peak cost-per-on-peak-kwh cost-per-off-peak-kwh))))

(defmethod apply-tariff :simple [tariff measurements]
  (let [{:keys [cost-per-kwh]} tariff
        total-consumption (apply + (map :value (filter #(number? (:value %)) (measurements/parse-measurements measurements))))]
    (-> total-consumption
        (* cost-per-kwh))))

(defmethod apply-tariff :none [tariff measurements]
  nil)

(defn daily-batch
  "Gets store and sensor information, start
  and end dates and a list of tariffs.
  Retrieves a day's worth of data, matches appropriate tariff
  and calculates expenditure.

  Daily standing charges can be spread evenly across the time periods
  of the day, however this can result in the incorrect impression that
  a low level of energy is being continuously consumed, therefore this
  cost is being separate out so it can be displayed as a separate item.

  Inserts result of that calculation into the database."
  [store sensor start-date end-date tariffs]
  (db/with-session [session (:hecuba-session store)]
    (let [device_id        (:device_id sensor)
          sensor_id        (:sensor_id sensor)
          month            (time/get-month-partition-key start-date)
          where            {:device_id device_id :sensor_id sensor_id :month month
                            :start start-date :end end-date}
          measurements     (measurements/fetch-measurements store where)
          new-type         (sensors/output-type-for (:type sensor) :tariff)
          output-sensor_id (:sensor_id (sensors/get-by-type {:device_id device_id :type new-type} session))]
      (when (seq measurements)
        (log/infof "Calculating daily expenditure for device_id %s and sensor_id %s" device_id sensor_id)
        (if output-sensor_id
          (let [tariff            (match-tariff (first measurements) tariffs)
                calculated-sensor {:device_id device_id :sensor_id output-sensor_id}
                calculated-data   [{:value (str (c/round (apply-tariff tariff measurements)))
                                    :timestamp (tc/to-date end-date)
                                    :month (time/get-month-partition-key end-date)
                                    :device_id device_id
                                    :sensor_id output-sensor_id}]]
            (measurements/insert-measurements store calculated-sensor 10 calculated-data)
            (sensors/update-sensor-metadata session calculated-sensor start-date end-date))
          (log/errorf "Could not find the output sensor_id for device_id %s and new type %s" device_id new-type))))))

(defn calculate-expenditure-daily
  "Gets tariff map and a map containing
  sensor and range information. Retrieves measurements,
  processes them in batches of one day and aplies this tariff to them.
  Returns a sequence."
  [store {:keys [sensor range]}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [start-date end-date]} range
          entity_id                     (:entity_id (devices/get-by-id session (:device_id sensor)))
          tariffs                       (read-tariffs store entity_id)
          sensor                        (sensors/get-by-id sensor session)]
      (doseq [timestamp (time/seq-dates start-date end-date (t/days 1))]
        (daily-batch store sensor timestamp (t/plus timestamp (t/days 1)) tariffs)))))

(defn daily->hourly
  "Assume we process one day’s worth of data at a time and that day’s consumption
  is split up into a number of consumption readings (e.g. 48 half-hour records).
  We then need to work out the unit rate tariff that applies to each time period."
  [store device_id output-sensor_id start-date end-date tariffs measurements]
  (db/with-session [session (:hecuba-session store)]
    (let [tariff            (match-tariff (first measurements) tariffs)
          calculated-sensor {:device_id device_id :sensor_id output-sensor_id}]
      (when tariff
        (doseq [m measurements]
          (let [calculated-data  [{:value (str (c/round (apply-tariff tariff [m])))
                                   :timestamp (tc/to-date end-date)
                                   :month (time/get-month-partition-key end-date)
                                   :device_id device_id
                                   :sensor_id output-sensor_id}]]
            (measurements/insert-measurements store calculated-sensor 10 calculated-data)
            (sensors/update-sensor-metadata session calculated-sensor start-date end-date)))))))

(defn expenditure-hourly
  [store sensor start-date end-date tariffs]
  (db/with-session [session (:hecuba-session store)]
    (let [device_id        (:device_id sensor)
          sensor_id        (:sensor_id sensor)
          month            (time/get-month-partition-key start-date)
          where            {:device_id device_id :sensor_id sensor_id :month month
                            :start start-date :end end-date}
          measurements     (measurements/fetch-measurements store where)
          new-type         (sensors/output-type-for type :tariff)
          output-sensor_id (:sensor_id (sensors/get-by-type {:device_id device_id :type new-type} session))]
      (when (seq measurements)
        (log/infof "Calculating hourly expenditure for device_id %s and sensor_id %s" device_id sensor_id)
        (if output-sensor_id
          (daily->hourly store device_id output-sensor_id start-date end-date tariffs measurements)
          (log/errorf "Could not find the output sensor_id for device_id %s and new type %s" device_id new-type))))))

(defn calculate-expenditure-hourly
  "Accepts store, sensor information, start and end dates and
  a list of tariffs. Retrieves a day's worth of data and works on it in
  half an hour batches. Matches appropriate tariff and calculates
  expenditure for each period.

  Inserts results of that calculation into the database."
  [store sensor start-date end-date tariffs]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [start-date end-date]} range
          entity_id                     (:entity_id (devices/get-by-id session (:device_id sensor)))
          tariffs                       (read-tariffs store entity_id)]
      (doseq [timestamp (time/seq-dates start-date end-date (t/days 1))]
        (expenditure-hourly store sensor timestamp (t/plus timestamp (t/days 1)) tariffs)))))

(defn sum-up-expenditure
  "Calculate total and insert it into the database. Update output sensor's dirty dates."
  [store calculated-sensor start end measurements]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [sensor_id device_id]} calculated-sensor
          calculated-data  [{:value (str (apply + (map :value (filter #(number? (:value %)) measurements))))
                             :timestamp (tc/to-date end)
                             :month (time/get-month-partition-key end)
                             :device_id device_id
                             :sensor_id sensor_id}]]
      (measurements/insert-measurements store calculated-sensor 10 calculated-data)
      (sensors/update-sensor-metadata session calculated-sensor start end))))

(defn sum-up-expenditure-batch
  "Fetch data for given range and underlying sensor type (calculated expenditure),
  calculate total and insert it int othe database."
  [store sensor start end calculation-type]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [device_id sensor_id type]} sensor
          new-type                           (sensors/output-type-for type calculation-type)
          month                              (time/get-month-partition-key start)
          where                              {:device_id device_id :sensor_id sensor_id :month month
                                              :start start :end end}
          measurements                       (measurements/parse-measurements (measurements/fetch-measurements store where))
          output-sensor_id                   (:sensor_id (sensors/get-by-type {:device_id device_id :type new-type} session))]
      (when (seq measurements)
        (if output-sensor_id
          ;; calculate total
          (sum-up-expenditure store {:sensor_id output-sensor_id :device_id device_id} start end measurements)
          (log/errorf "Could not find the output sensor_id for device_id %s and new type %s" device_id type))))))

(defmulti total-usage (fn [_ item] (:usage-period item)))

(defmethod total-usage :weekly
  [store {:keys [sensor range]}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [start-date end-date]} range
          sensor                        (sensors/get-by-id sensor session)]
      (log/infof "Calculating total weekly usage for device_id %s and sensor_id %s" (:device_id sensor) (:sensor_id sensor))
      (doseq [timestamp (time/seq-dates start-date end-date (t/days 7))]
        (sum-up-expenditure-batch store sensor timestamp (t/plus timestamp (t/days 7)) :total-usage-weekly)))))

(defmethod total-usage :monthly
  [store {:keys [sensor range]}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [start-date end-date]} range
          sensor                        (sensors/get-by-id sensor session)]
      (log/infof "Calculating total monthly usage for device_id %s and sensor_id %s" (:device_id sensor) (:sensor_id sensor))
      (doseq [timestamp (time/seq-dates start-date end-date (t/days 30))]
        (sum-up-expenditure-batch store sensor timestamp (t/plus timestamp (t/days 30)) :total-usage-monthly)))))
