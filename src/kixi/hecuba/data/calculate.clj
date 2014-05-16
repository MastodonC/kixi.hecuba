(ns kixi.hecuba.data.calculate
  "Calculated datasets."
  (:require [clj-time.coerce :as tc]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clojure.string :as str]
            [clojure.tools.logging :as log]
            [kixi.hecuba.api.datasets :as datasets]
            [kixi.hecuba.api.measurements :as measurements]
            [kixi.hecuba.data.misc :as m]
            [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.storage.dbnew :as dbnew]
            [qbits.hayt :as hayt]
            [kixi.hecuba.queue :as q]))

(defn get-difference
  "Returns difference if both values are numbers, otherwise returns N/A."
  [rest-coll first-coll]
  (let [m (first rest-coll)
        n first-coll]
    (if (and (= "true" (get-in m [:metadata :is-number]))
             (= "true" (get-in n [:metadata :is-number])))
      (str (- (:value m) (:value n)))
      "N/A")))

(defn diff-and-insert
  "Calculates difference and inserts it to differece_series table.
  Column's header is the end of the calculated interval."
  [commander first-coll rest-coll]
  (when (and (not (nil? first-coll)) ;; Sequence might not have an even number of measurements
             (not (nil? (first rest-coll))))
    (let [d         (get-difference rest-coll first-coll)
          device-id (:device-id first-coll)
          type      (:type first-coll)
          timestamp (m/to-timestamp (:timestamp (first rest-coll)))
          month     (Integer/parseInt (:month first-coll))]
      (upsert! commander :difference-series {:timestamp timestamp
                                             :value d
                                             :device-id device-id
                                             :type type
                                             :month month})))
  rest-coll)

(defn calculate-difference-series
  "Takes a commander, querier, sensor and a start date. Retrieves an hour worth of data and calculates the difference
  series. Difference series is inserted into another table."
  [commander querier {:keys [device-id type period]} start-date]
  (let [end-date     (m/add-hour start-date)
        month        (m/get-month-partition-key start-date)
        where        [[= :device-id device-id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
        measurements (m/decassandraify-measurements (items querier :measurement where))]
    (when-not (empty? measurements)
      (loop [m measurements]
        (when-not (empty? m)
          (recur (diff-and-insert commander (first m) (rest m))))))
    end-date))

(defn difference-series
  "Takes commander, querier, sensor and date range and calculates difference series for that range."
  [commander querier {:keys [sensor range]}]
  (let [start  (m/int-format-to-timestamp (:start-date range))
        end    (m/int-format-to-timestamp (:end-date range))]
    (loop [start-date start]
      (when-not (.before end start-date)
        (recur (calculate-difference-series commander querier sensor start-date))))))


;;;;;;;;;;; Rollups of measurements ;;;;;;;;;

(defn daily-batch
  [commander querier {:keys [device-id type period]} start-date]
  (let [end-date     (m/add-day start-date)
        year         (m/get-year-partition-key start-date)
        where        [[= :device-id device-id] [= :type type] [= :year year] [>= :timestamp start-date] [< :timestamp end-date]]
        measurements (m/decassandraify-measurements (items querier :hourly-rollups where))
        funct        (case period
                       "CUMULATIVE" (fn [measurements] (reduce + (map :value measurements)))
                       "INSTANT"    (fn [measurements] (/ (reduce + (map :value measurements)) (count measurements)))
                       "PULSE"      (fn [measurements] (reduce + (map :value measurements))))]
    (when-not (empty? measurements)
      (upsert! commander :daily-rollups {:value (str (funct measurements))
                                         :timestamp start-date
                                         :device-id device-id
                                         :type type}))
    end-date))


(defn daily-rollups
  "Calculates daily rollups for given sensor and date range."
  [commander querier {:keys [sensor range]}]
  (let [start         (m/int-format-to-timestamp (:start-date range))
        end           (m/int-format-to-timestamp (:end-date range))]
    (loop [start-date  start]
      (when-not (.before end start-date)
        (recur (daily-batch commander querier sensor start-date))))))

(defn hour-batch
  [commander querier {:keys [device-id type period]} start-date table]
  (let [end-date     (m/add-hour start-date)
        month        (m/get-month-partition-key start-date)
        where        [[= :device-id device-id] [= :type type] [= :month month] [>= :timestamp start-date] [< :timestamp end-date]]
        measurements (m/decassandraify-measurements (items querier table where))
        filtered     (filter #(number? %) (map :value measurements))
        funct        (case period
                       "CUMULATIVE" (fn [measurements] (reduce + measurements))
                       "INSTANT"    (fn [measurements] (/ (reduce + measurements) (count measurements)))
                       "PULSE"      (fn [measurements] (reduce + measurements)))]
    (when-not (empty? filtered)
      (let [invalid (/ (count filtered) (count measurements))]
        (when-not (and (not= invalid 1) (> invalid 0.10))
          (upsert! commander :hourly-rollups {:value (str (funct filtered))
                                              :timestamp end-date
                                              :year (m/get-year-partition-key start-date)
                                              :device-id device-id
                                              :type type}))))
    end-date))

(defn hourly-rollups
  "Calculates hourly rollups for given sensor and date range.
  Example of item: {:sensor {:device-id \"f11a21b8e5e6b97eacba2632db4a2037a43f4791\" :type \"temperatureGround\"
                   :period \"CUMULATIVE\"} :range {:start-date \"Sat Mar 01 00:00:00 UTC 2014\"
                   :end-date \"Sun Mar 02 23:00:00 UTC 2014\"}}"
  [commander querier {:keys [sensor range]}]
  (let [start         (m/int-format-to-timestamp (:start-date range))
        end           (m/int-format-to-timestamp (:end-date range))
        period        (:period sensor)
        table         (case period
                        "CUMULATIVE" :difference-series
                        "INSTANT"    :measurement
                        "PULSE"      :measurement)]
    (loop [start-date  start]
      (when-not (.before end start-date)
        (recur (hour-batch commander querier sensor start-date table))))))

;;;;;;;;; Resolution calculation ;;;;;;;;;;

(defn diff [coll]
  (map - coll (rest coll)))

(defn mode [coll]
  (first (last (sort-by second (frequencies coll)))))

(defn resolution
  "Updates resolution if it's missing. Infers it from last 100 measurements.
  Depending on the order of data in the database, it might not always return last 100 of measurements."
  [store item]
  (dbnew/with-session [session (:hecuba-session store)]
    (let [sensors-to-update (filter #(empty? (:resolution %)) (dbnew/execute session (hayt/select :sensors)))]
      (doseq [sensor sensors-to-update]
        (let [measurements (dbnew/execute session (hayt/select :measurements
                                                               (hayt/where [[= :device_id (:device_id sensor)]
                                                                            [= :type (:type sensor)]])
                                                               (hayt/order-by [:type :desc])
                                                               (hayt/limit 100)))
              differences  (diff (map #(/ (.getTime (m/truncate-seconds (:timestamp %))) 1000) measurements))
              resolution  (str (mode differences))]
          (dbnew/execute session (hayt/update :sensors
                                              (hayt/set-columns [[:resolution resolution]])
                                              (hayt/where [[= :device_id (:device_id sensor)]
                                                           [= :type (:type sensor)]]))))))))

(defn sensors-for-dataset
  "Returns all the sensors for the given dataset."
  [{:keys [members]} store]
  (dbnew/with-session [session (:hecuba-session store)]
    (let [parse-sensor (comp next (partial re-matches #"(\w+)-(\w+)"))
          sensor (fn [[type device-id]]
                   (dbnew/->clj (dbnew/execute session
                                               (hayt/select :sensors
                                                            (hayt/where [[= :type type]
                                                                         [= :device_id device-id]])))))]
      (mapcat sensor (->> members
                       (keep parse-sensor)
                       (into (hash-set)))))))


(defn insert-measurement [store m ]
  (dbnew/with-session [session (:hecuba-session store)]
    (dbnew/execute session
                   (hayt/insert :measurements
                                (hayt/values m)))))

(defn output-unit-for [t]
  (log/error t)
  (case t
    "vol2Kwh" "kWh"))

(defn output-type-for [t]
  (str "converted_" t))

(def truthy? #{"true"})

(def conversions {"gasConsumption" {"m^3" 10.97222
                                    "ft^3" (* 2.83 10.9722)}
                  "oilConsumption" {"m^3" 10308.34
                                    "ft^3" (* 2.83 10308.34)}})

(defn metadata-is-number? [{:keys [metadata]}]
  (truthy? (:is-number (read-string metadata))))

(defn conversion-fn [{:keys [type unit]}]
  (let [factor (get-in conversions [type unit])]
    (fn [m]
      (cond-> m (metadata-is-number? m)
              (assoc :value (str (* factor (read-string (:value m))))
                     :type (output-type-for type))))))

(defmulti calculate-data-set (comp keyword :operation))

(defmethod calculate-data-set :vol2kwh [ds store]

  (let [get-fn-and-measurements  (fn [s] [(conversion-fn s) (measurements/all-measurements store s)])
        convert                  (fn [[f xs]] (map f xs))
        topic (get-in (:queue store) [:queue "measurements"])
        {:keys [operation device_id]} ds]
    (doseq [m  (->> (sensors-for-dataset ds store)
                    (map get-fn-and-measurements)
                    (mapcat convert)
                    (map #(assoc % :device_id device_id)))]
      (q/put-on-queue topic m)
      (insert-measurement store m))))

(defmethod calculate-data-set :total-vol2kwh [ds store]
 
  )

(defn generate-synthetic-readings [store item]
  (let [data-sets (datasets/all-datasets store)]
    (doseq [ds data-sets]
      (calculate-data-set ds store))))
