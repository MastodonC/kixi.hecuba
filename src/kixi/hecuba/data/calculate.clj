(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.periodic :as p]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.datasets :as datasets]
            [kixi.hecuba.data :as data]
            [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.time :as time]
            [kixi.hecuba.data.profiles :as profiles]
            [kixi.hecuba.storage.db :as db]
            [qbits.hayt :as hayt]
            [kixi.hecuba.time :as time]))

;; Helpers

(defn round [x]
  (with-precision 3 x))

(defn merge-sensor-metadata [store sensor]
  (db/with-session [session (:hecuba-session store)]
    (merge (first (db/execute session (hayt/select :sensor_metadata
                                                   (hayt/where [[= :device_id (:device_id sensor)]
                                                                [= :sensor_id (:sensor_id sensor)]])))) sensor)))
(defn sensors-for-dataset
  "Returns all the sensors for the given dataset."
  [{:keys [operands]} store]
  (db/with-session [session (:hecuba-session store)]
    (let [parsed-sensors (map (fn [s] (into [] (next (re-matches #"(\w+)-(\w+)" s)))) operands)
          sensor (fn [[type device_id]]
                   (sensors/get-by-type {:device_id device_id :type type} session))]
      (->> (mapcat sensor parsed-sensors)
           (map #(merge-sensor-metadata store %))))))

(def conversion-factors {"vol2kwh" {"gasConsumption" {"m^3" 10.97222
                                                      "ft^3" (* 2.83 10.9722)}
                                    "oilConsumption" {"m^3" 10308.34
                                                      "ft^3" (* 2.83 10308.34)}}
                         "kwh2co2" {"electricityConsumption" {"kWh" 0.517}
                                    "gasConsumption" {"kWh" 0.185}
                                    "oilConsumption" {"kWh" 0.246}}})

(defn conversion-fn [{:keys [type unit]} output-sensor_id operation]
  (let [typ              (first (str/split type #"_"))
        factor           (get-in conversion-factors [operation typ unit])]
    (when factor
      (fn [m]
        (cond-> m (measurements/metadata-is-number? m)
                (assoc :value (str (round (* factor (edn/read-string (:value m)))))
                       :sensor_id output-sensor_id))))))

(defn timestamp-seq-inclusive
  ([start end] (timestamp-seq-inclusive start end 60))
  ([start end resolution] (map (fn [t] (tc/to-date t)) (take-while #(not (t/after? % end)) (p/periodic-seq start (t/seconds resolution))))))

(defn all-timestamps-for-range
  "Takes a start date, end date and resolution (in seconds) and creates a sequence
  of timestamps (inclusive). Seconds are truncated. "
  [start end resolution]
  (let [start-date (time/truncate-minutes start)
        end-date   (time/truncate-minutes end)]
    (map #(hash-map :timestamp %) (timestamp-seq-inclusive start-date end-date resolution))))

(defn pad-measurements
  "Takes a sequence of measurements, expected timestamps and pads it with template readings."
  [measurements expected-timestamps]
  (let []
    (let [template-reading (fn [t] (hash-map :value "N/A" :month (time/get-month-partition-key (:timestamp t))))
          grouped-readings (into {} (map #(vector (:timestamp %) %) measurements))]
      (map #(merge (template-reading %) (get grouped-readings (:timestamp %) %)) expected-timestamps))))

;;;;;;; Difference series ;;;;;;;;;

(defn- ext-type [type] (str type "_differenceSeries"))

(defn get-difference [output-sensor_id]
  (fn [m n]
    (let [value (if (every? measurements/metadata-is-number? [m n])
                  (str (round (- (edn/read-string (:value n)) (edn/read-string (:value m)))))
                  "N/A")]
      {:timestamp (:timestamp n)
       :value value
       :reading_metadata {"is-number" (if (number? (edn/read-string value)) "true" "false") "median-spike" "n-a"}
       :device_id (:device_id m)
       :sensor_id output-sensor_id
       :month (:month m)})))

(defn diff-seq [output-sensor_id coll]
  (assert (not (empty? coll)) "No measurements passed to diff-seq")
  (map #((get-difference output-sensor_id) %1 %2) coll (rest coll)))

(defn timestamp-seq-inclusive
  ([start end] (timestamp-seq-inclusive start end 60))
  ([start end resolution] (map (fn [t] (tc/to-date t)) (take-while #(not (t/after? % end)) (p/periodic-seq start (t/seconds resolution))))))

(defn difference-series
  "Takes store, sensor and a range of dates and calculates difference series using resolution
  stored in the sensor data. If resolution is not specified, default 60 seconds is used.
  Measurements have gaps filled in."
  [store {:keys [sensor range]}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [start-date end-date]} range
          measurements                  (measurements/measurements-for-range store sensor range (t/hours 1))]
      (when (> (count (take 2 measurements)) 1)
        (let [{:keys [device_id sensor_id type]} sensor
              new-type                      (ext-type type)
              calculated-sensor             (sensors/get-by-type {:device_id device_id :type new-type} session)
              output-sensor_id              (:sensor_id calculated-sensor)
              calculated                    (diff-seq output-sensor_id measurements)
              page-size                     10]
          (when calculated
            (measurements/insert-measurements store calculated-sensor page-size calculated)
            (sensors/update-sensor-metadata session calculated-sensor (:start-date range) (:end-date range))))))))

(defn kWh->co2
  "Converts measurements from kWh to co2."
  [store {:keys [sensor range]}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [device_id type sensor_id]} sensor
          new-type                 (sensors/output-type-for type "kwh2co2")
          output-sensor_id         (:sensor_id (sensors/get-by-type {:device_id device_id :type new-type} session))
          get-fn-and-measurements  (fn [s] [(conversion-fn s "kwh2co2" output-sensor_id)
                                            (measurements/measurements-for-range store s range (t/hours 1))])
          convert                  (fn [[f xs]] (when-not (nil? f) (map f xs)))
          calculated               (->> sensor
                                        get-fn-and-measurements
                                        convert)
          page-size                10]
      (when calculated
        (let [calculated-sensor {:device_id device_id :sensor_id output-sensor_id}]
          (measurements/insert-measurements store calculated-sensor page-size calculated)
          (sensors/update-sensor-metadata session calculated-sensor (:start-date range) (:end-date range)))))))

(defn gas-volume->kWh
  "Converts measurements from m^3 and ft^3 to kWh."
  [store {:keys [sensor range]}]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [device_id type]} sensor
          new-type                 (sensors/output-type-for type "vol2kwh")
          output-sensor_id         (:sensor_id (sensors/get-by-type {:device_id device_id :type new-type} session))
          get-fn-and-measurements  (fn [s] [(conversion-fn s "vol2kwh" output-sensor_id)
                                            (measurements/measurements-for-range store s range (t/hours 1))])
          convert                  (fn [[f xs]] (when-not (nil? f) (map f xs)))

          calculated               (->> sensor
                                        get-fn-and-measurements
                                        convert)
          page-size                10]
      (when calculated
        (let [calculated-sensor {:device_id device_id :sensor_id output-sensor_id}]
          (measurements/insert-measurements store calculated-sensor page-size calculated)
          (sensors/update-sensor-metadata session calculated-sensor (:start-date range) (:end-date range)))))))

;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn average-reading [measurements]
  (let [[sum count] (reduce (fn [[sum count] m]
                              [(+ sum m) (inc count)])
                            [0 0] measurements)]
    (if-not (zero? count)
      (float (/ sum count))
      "N/A")))

(defn parse-hourly-rollups
  "Takes measurements from hourly_rollups and parses their values if present.
  Because data in hourly_rollups has already been checked, it contains either nils
  or stringified numbers."
  [measurements]
  (map (fn [m] (assoc-in m [:value] (when-let [value (:value m)]
                                      (edn/read-string value)))) measurements))

(defn daily-batch
  [store {:keys [device_id type period sensor_id]} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/days 1))
          year         (time/get-year-partition-key start-date)
          where        [[= :device_id device_id] [= :sensor_id sensor_id]
                        [= :year year] [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (parse-hourly-rollups (db/execute session (hayt/select :hourly_rollups (hayt/where where))))
          filtered     (filter #(number? %) (map :value measurements))
          funct        (fn [measurements] (case period
                                            "CUMULATIVE" (last measurements)
                                            "INSTANT"    (average-reading measurements)
                                            "PULSE"      (reduce + measurements)))]
      (when-not (empty? filtered)
        (db/execute session
                    (hayt/insert :daily_rollups (hayt/values {:value (str (round (funct filtered)))
                                                              :timestamp (tc/to-date start-date)
                                                              :device_id device_id
                                                              :sensor_id sensor_id}))))
      end-date)))

(defn daily-rollups
  "Calculates daily rollups for given sensor and date range."
  [store {:keys [sensor range]}]
  (let [{:keys [start-date end-date]} range]
    (loop [start start-date]
      (when-not (t/before? end-date start)
        (recur (daily-batch store sensor start))))))

(defn data-to-calculate? [measurements]
  (let [filtered (filter #(number? %) (map :value measurements))]
    (when-not (empty? filtered)
      (let [invalid (/ (count filtered) (count measurements))]
        (when-not (and (not= invalid 1) (> invalid 0.10))
          filtered)))))

(defn hour-batch
  [store {:keys [device_id type sensor_id period]} start-date]
  (db/with-session [session (:hecuba-session store)]
    (let [end-date     (t/plus start-date (t/hours 1))
          month        (time/get-month-partition-key start-date)
          where        [[= :device_id device_id] [= :sensor_id sensor_id] [= :month month]
                        [>= :timestamp start-date] [< :timestamp end-date]]
          measurements (measurements/parse-measurements (db/execute session
                                                                    (hayt/select :partitioned_measurements (hayt/where where))))
          funct        (fn [measurements] (case period
                                            "CUMULATIVE" (last measurements)
                                            "INSTANT"    (average-reading measurements)
                                            "PULSE"      (reduce + measurements)))]
      (when-let [filtered (data-to-calculate? measurements)]
        (db/execute session
                    (hayt/insert :hourly_rollups (hayt/values
                                                  {:value (str (round (funct filtered)))
                                                   :timestamp (tc/to-date end-date)
                                                   :year (time/get-year-partition-key start-date)
                                                   :device_id device_id
                                                   :sensor_id sensor_id}))))
      end-date)))

(defn hourly-rollups
  "Calculates hourly rollups for given sensor and date range.
  Example of item: {:sensor {:device_id \"f11a21b8e5e6b97eacba2632db4a2037a43f4791\" :type \"temperatureGround\"
                   :period \"CUMULATIVE\"} :range {:start-date \"Sat Mar 01 00:00:00 UTC 2014\"
                   :end-date \"Sun Mar 02 23:00:00 UTC 2014\"}}"
  [store {:keys [sensor range]}]
  (let [{:keys [start-date end-date]} range]
    (loop [start  (time/truncate-seconds start-date)]
      (when-not (t/before? end-date start)
        (recur (hour-batch store sensor start))))))

;;;;;;;;; Resolution calculation ;;;;;;;;;;

(defn- diff [coll]
  (map - coll (rest coll)))

(defn- mode [coll]
  (first (last (sort-by second (frequencies coll)))))

(defn find-resolution [measurements]
  (let [differences (diff (map #(/ (.getTime (time/truncate-seconds (:timestamp %))) 1000) measurements))
        v           (mode differences)]
    (if v (java.lang.Math/abs v) "")))

(defn resolution
  "Updates resolution if it's missing. Infers it from last 100 measurements.
  Depending on the order of data in the database, it might not always return last 100 of measurements."
  [store item]
  (db/with-session [session (:hecuba-session store)]
    (let [sensors-to-update (filter #(empty? (:resolution %)) (sensors/all-sensors store))]
      (doseq [sensor sensors-to-update]
        (when (and (:lower_ts sensor) (:upper_ts sensor))
          (let [months       (time/range->months (:lower_ts sensor) (:upper_ts sensor))
                measurements (db/execute session (hayt/select :partitioned_measurements
                                                              (hayt/where [[= :device_id (:device_id sensor)]
                                                                           [= :sensor_id (:sensor_id sensor)]
                                                                           [:in :month months]])
                                                              (hayt/limit 100)))]
            (when (seq measurements)
              (let [calculated-resolution  (str (find-resolution measurements))]
                (log/debugf "Checking resolution for id: %s type: %s. Calculated resolution is: %s"
                          (:device_id sensor) (:type sensor) calculated-resolution)
                (db/execute session (hayt/update :sensors
                                                 (hayt/set-columns [[:resolution calculated-resolution]])
                                                 (hayt/where [[= :device_id (:device_id sensor)]
                                                              [= :sensor_id (:sensor_id sensor)]])))))))))))
(defn update-resolution [store sensor resolution]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session (hayt/update :sensors (hayt/set-columns {:resolution (str resolution)})
                                     (hayt/where (data/where-from sensor))))))

(defn get-resolution [store sensor measurements]
  (let [existing-resolution (:resolution sensor)
        calculated-resolution (find-resolution measurements)]
    (log/debugf "Checking resolution for id: %s type: %s. Existing resolution is: %s. Calculated resolution is: %s"
                (:device_id sensor) (:type sensor) existing-resolution calculated-resolution)
    (if (or (nil? existing-resolution)
            (zero? (edn/read-string existing-resolution))
            (not= existing-resolution calculated-resolution))
      (do (update-resolution store sensor calculated-resolution)
          calculated-resolution)
      existing-resolution)))

;;;;;; Calculated datasets ;;;;;;;;;;;

(defn- sum
  "Adds all numeric values in a sequence of measurements. Some measurements might contain
  error messages instead of readings."
  [m]
  (apply + (map :value (filter #(number? (:value %)) m))))

(defmulti calculation (fn [operation & datasets] operation))

(defmethod calculation :sum [_ & datasets]
  (sum datasets))

(defmethod calculation :subtract [_ & datasets]
  (let [m (map :value datasets)]
    (if (every? number? m)
      (round (apply - m))
      "N/A")))

(defmethod calculation :divide [_ & datasets]
  (let [m (map :value datasets)]
    (if (and (every? number? m)
             (not (zero? (second m))))
      (round (apply / m))
      "N/A")))

(defmethod calculation :multiply [_ & datasets]
  (let [m (map :value datasets)]
    (if (every? number? m)
      (if (some #{0} m)
        0
        (round (apply * m)))
      "N/A")))

(defmethod calculation :multiply-series-by-field [_ & datasets]
  (let [m (:value (first datasets))
        f (last datasets)]
    (if (number? m)
      (if (= 0 m)
        0
        (round (* m f)))
      "N/A")))

(defmethod calculation :divide-series-by-field [_ & datasets]
  (let [m (:value (first datasets))
        f (last datasets)]
    (if (number? m)
      (round (/ m f))
      "N/A")))

(defn compute-datasets-using-field [operation device_id sensor_id dataset field]
  (map (fn [m]
         (let [value (calculation operation m field)]
           (hash-map :value (str value)
                     :device_id device_id
                     :sensor_id sensor_id
                     :reading_metadata {"is-number" (str (number? value)) "median-spike" "n/a"}
                     :timestamp (:timestamp m)
                     :month (:month m))))
       dataset))

(defn compute-datasets [operation device_id sensor_id & datasets]
  (apply map (fn [& args]
               (let [value (apply calculation operation args)]
                 (hash-map :value (str value)
                           :device_id device_id
                           :sensor_id sensor_id
                           :reading_metadata {"is-number" (str (number? value)) "median-spike" "n/a"}
                           :timestamp (:timestamp (first args))
                           :month (:month (first args)))))
         datasets))

;; Padding ;;;

(defn even-all-collections
  "Takes a vector containg lists of measurements, a sequence of required timestamps
  and pads the measurements."
  [all-colls timestamps]
  (map #(pad-measurements % timestamps) all-colls))

(defn padded-measurements
  "Takes a sequence of a variable number of sequences of measurements
   and makes them of even length by padding."
  [sensors measurements-seq resolution]
  (let [[start end]  (sensors/range-for-all-sensors sensors)
        expected-ts  (all-timestamps-for-range start end resolution)]
     (even-all-collections measurements-seq expected-ts)))

;;; Decisions around sensors ;;;

(defmulti should-calculate? (fn [ds sensors] (keyword (:operation ds))))

(defmethod should-calculate? :sum [ds sensors]
  (let [{:keys [period unit]} (first sensors)]
    (every? #(and (= period (:period %))
                  (= unit   (:unit %))) sensors)))

(defmethod should-calculate? :divide [ds sensors]
  (let [{:keys [period unit]} (first sensors)]
    (every? #(and (= period (:period %))
                  (= unit (:unit %))) sensors)))

(defmethod should-calculate? :subtract [ds sensors]
  (let [{:keys [period]} (first sensors)]
    (every? #(= period (:period %)) sensors)))

(defn profile-comparator [x y]
  (let [order (zipmap ["pre-retrofit" "planned retrofit" "post retrofit" ;; R4F
                       "as designed" "as built" ;; BPE
                       "intervention"] (range))
        x-ind (order (-> x (get-in [:profile_data :event_type] "") str/lower-case))
        y-ind (order (-> y (get-in [:profile_data :event_type] "") str/lower-case))]
    (if (and x-ind y-ind)
      (if (zero? (compare x-ind y-ind))
        (compare (:timestamp x) (:timestamp y))
        (compare x-ind y-ind))
      (compare (:timestamp x) (:timestamp y)))))

(defn get-field-value
  "Retrieves all profiles for given enity-id, sorts them and returns the latest value for
   requested field name."
  [entity-id field-name store]
  (db/with-session [session (:hecuba-session store)]
    (when-let [profiles (seq (profiles/get-profiles entity-id session))]
      (let [sorted       (sort profile-comparator profiles)
            merged       (apply data/deep-merge sorted)
            latest-value (-> merged :profile_data field-name)]
        latest-value))))

(defn calculate-dataset
  ([store ds sensors range field]
     (let [{:keys [operation operands device_id entity_id]} ds
           operation   (keyword operation)
           field-value (edn/read-string (get-field-value entity_id (keyword field) store))]
       (log/info "Calculating datasets for operands: " operands "and operation: " operation "and range: " range)
       (let [measurements (mapcat #(measurements/parse-measurements
                                    (measurements/measurements-for-range store % range (t/hours 1))) sensors)]
         (db/with-session [session (:hecuba-session store)]
           (if (and (> (count (take 2 measurements)) 0) (not (nil? field-value)))
             (let [new-type                      (:name ds)
                   output-sensor                 (sensors/merge-sensor-metadata
                                                  store
                                                  (sensors/get-by-type {:device_id device_id :type new-type} session))
                   sensor_id                     (:sensor_id output-sensor)
                   calculated                    (compute-datasets-using-field operation device_id sensor_id measurements field-value)
                   {:keys [start-date end-date]} range
                   page-size                     10]
               (when calculated
                 (measurements/insert-measurements store output-sensor page-size calculated)
                 (sensors/update-sensor-metadata session output-sensor (:start-date range) (:end-date range)))
               (log/info "Finished calculation for operands: " operands "and operation: " operation))
             (log/info "There are not enough data to calculate."))))))
  ([store ds sensors range]
     (let [{:keys [operation operands device_id]} ds
           operation (keyword operation)]
       (log/info "Calculating datasets for operands: " operands "and operation: " operation "and range: " range)
       (if (and (> (count sensors) 1)
                (should-calculate? ds sensors))
         (let [measurements (into [] (map #(measurements/parse-measurements
                                            (measurements/hourly-rollups-for-range store % range (t/days 1)))
                                          sensors))]
           (if (every? #(> (count (take 2 %)) 1) measurements)
             (db/with-session [session (:hecuba-session store)]
               (let [padded                        (padded-measurements sensors measurements 3600)
                     new-type                      (:name ds)
                     output-sensor                 (sensors/merge-sensor-metadata
                                                    store
                                                    (sensors/get-by-type {:device_id device_id :type new-type} session))
                     sensor_id                     (:sensor_id output-sensor)
                     calculated                    (apply compute-datasets operation device_id sensor_id padded)
                     {:keys [start-date end-date]} range
                     page-size                     10]
                 (when calculated
                   (measurements/insert-measurements store output-sensor page-size calculated)
                   (sensors/update-sensor-metadata session output-sensor (:start-date range) (:end-date range)))
                 (log/info "Finished calculation for operands: " operands "and operation: " operation)))
             (log/info "Sensors do not have enough measurements to calculate.")))
         (log/info "Sensors do not meet requirements for calculation.")))))
