(ns kixi.hecuba.data.sensors
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data :as data]
            [kixi.hecuba.api :refer (stringify-values)]
            [kixi.hecuba.data.measurements :as measurements]
            [clojure.walk :as walk]
            [schema.core :as s]
            [clj-time.coerce :as tc]
            [clj-time.core :as t]))

(def Sensor {(s/required-key :device_id)                   s/Str
             (s/required-key :sensor_id)                   s/Str
             (s/optional-key :type)                        s/Str
             (s/optional-key :alias)                       (s/maybe s/Str)
             (s/optional-key :accuracy)                    (s/maybe s/Str)
             (s/optional-key :actual_annual)               (s/maybe s/Bool)
             (s/optional-key :corrected_unit)              (s/maybe s/Str)
             (s/optional-key :correction)                  (s/maybe s/Str)
             (s/optional-key :correction_factor)           (s/maybe s/Str)
             (s/optional-key :correction_factor_breakdown) (s/maybe s/Str)
             (s/optional-key :frequency)                   (s/maybe s/Str)
             (s/optional-key :max)                         (s/maybe s/Str)
             (s/optional-key :median)                      (s/maybe double)
             (s/optional-key :min)                         (s/maybe s/Str)
             (s/optional-key :period)                      (s/maybe (s/enum "INSTANT" "PULSE" "CUMULATIVE" ""))
             (s/optional-key :resolution)                  (s/maybe s/Str)
             (s/optional-key :status)                      (s/maybe s/Str)
             (s/optional-key :synthetic)                   (s/maybe s/Bool)
             (s/optional-key :unit)                        (s/maybe s/Str)
             (s/optional-key :user_id)                     (s/maybe s/Str)
             (s/optional-key :alias-sensor)                (s/maybe {s/Str s/Str})
             s/Any                                         s/Any})

(defn validate-and-log [sensor]
  (try
    (s/validate Sensor sensor)
    (catch Throwable t
      (log/errorf t "Sensor: %s" sensor)
      (throw t))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Dirty dates and lower_ts and upper_ts

(defn update-bounds [min-date max-date {:keys [lower_ts upper_ts]}]
  (cond
   (and (nil? lower_ts) (nil? upper_ts)) {:upper_ts max-date :lower_ts min-date}
   (.before min-date lower_ts) {:lower_ts min-date}
   (.after max-date upper_ts) {:upper_ts max-date}))

(defn update-date-range [sensor column min-date max-date]
  (let [existing-range (get sensor column)
        start          (get existing-range "start")
        end            (get existing-range "end")]
    (assert (and (not (nil? min-date)) (not (nil? max-date))) (format "Min and max dates are null. Sensor: %s,  column: %s" sensor column))
    (cond
     (or (nil? start) (nil? end)) {"start" min-date "end" max-date}
     (.before min-date start) {"start" min-date}
     (.after max-date end) {"end" max-date})))

(defn columns-to-update?
  "It returns columns to update, if there are any. Otherwise it returns nil."
  [sensor start end new-bounds]
  (merge
   (when-let [rollups (update-date-range sensor :rollups start end)]
     {:rollups [+ rollups]})
   (when-let [mislabelled (update-date-range sensor :mislabelled_sensors_check start end)]
     {:mislabelled_sensors_check [+ mislabelled]})
   (when-let [difference (update-date-range sensor :difference_series start end)]
     {:difference_series [+ difference]})
   (when-let [median (update-date-range sensor :median_calc_check start end)]
     {:median_calc_check [+ median]})
   (when-let [spikes (update-date-range sensor :spike_check start end)]
     {:spike_check [+ spikes]})
   (when-let [co2 (update-date-range sensor :co2 start end)]
     {:co2 [+ co2]})
   (when-let [kwh (update-date-range sensor :kwh start end)]
     {:kwh [+ kwh]})
   (when-let [datasets (update-date-range sensor :calculated_datasets start end)]
     {:calculated_datasets [+ datasets]})
   (when-let [actual-annual-calculation (update-date-range sensor :actual_annual_calculation start end)]
     {:actual_annual_calculation [+ actual-annual-calculation]})
   (when-let [lower (:lower_ts new-bounds)] {:lower_ts lower})
   (when-let [upper (:upper_ts new-bounds)] {:upper_ts upper})))

(defn decode [sensor]
  (-> sensor
      (cond-> (seq (get-in sensor [:alias_sensor "sensor_id"])) 
              (assoc :alias_sensor {:sensor_id (get-in sensor [:alias_sensor "sensor_id"])
                                    :device_id (get-in sensor [:alias_sensor "device_id"])}))))

(defn get-by-id
  ([sensor session]
     (-> (db/execute session
                     (hayt/select :sensors
                                  (hayt/where [[= :sensor_id (:sensor_id sensor)]
                                               [= :device_id (:device_id sensor)]])))
         first
         decode)))

(defn get-by-type
  ([sensor session]
     (-> (db/execute session
                     (hayt/select :sensors
                                  (hayt/where [[= :type (:type sensor)]
                                               [= :device_id (:device_id sensor)]])))
         first
         decode)))

(defn update-sensor-metadata
  "Updates start and end dates when new measurement is received."
  [session sensor min-date max-date]
  (let [start      (tc/to-date min-date)
        end        (tc/to-date max-date)
        where      (data/where-from sensor)
        s          (first (db/execute session
                                      (hayt/select :sensor_metadata
                                                   (hayt/where where))))
        new-bounds (update-bounds start end s)]
    (when-let [columns (columns-to-update? s start end new-bounds)]
      (db/execute session
                  (hayt/update :sensor_metadata
                               (hayt/set-columns columns)
                               (hayt/where where))))))

(defn reset-date-range
  "Given store, sensor, column and start/end dates, update these dates in sensor metadata."
  [store sensor col start-date end-date]
  (db/with-session [session (:hecuba-session store)]
    (let [sensor_id           (:sensor_id (get-by-id sensor session))
          device_id           (:device_id sensor)
          where               [[= :device_id device_id] [= :sensor_id sensor_id]]
          current-metadata    (first (db/execute session (hayt/select :sensor_metadata (hayt/where where))))
          current-range       (get current-metadata col)
          start               (get current-range "start")
          end                 (get current-range "end")]
      (when (and start end)
        (let [current-start (tc/from-date start)
              current-end   (tc/from-date end)]
          (when-not (and (t/before? current-start start-date)
                         (t/after? current-end end-date))
            (db/execute session (hayt/update :sensor_metadata (hayt/set-columns {col nil}) (hayt/where where)))))))))

(defn range-for-all-sensors
  "Takes a sequence of sensors and returns min and max dates from their lower_ts and upper_ts"
  [sensors]
  (assert (not (empty? sensors)) "Sensors passed to range-for-all-sensors are empty.")
  (when-let [s (seq (filter #(not (nil? (:lower_ts %))) sensors))]
    (let [all-starts (map #(tc/from-date (:lower_ts %)) s)
          all-ends   (map #(tc/from-date (:upper_ts %)) s)
          min-date   (tc/to-date-time (apply min (map tc/to-long all-starts)))
          max-date   (tc/to-date-time (apply max (map tc/to-long all-ends)))]
      [min-date max-date])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INSERT, UPDATE, DELETE

(defn user-metadata [sensor synthetic]
  (-> sensor
      (update-in [:user_metadata] (fn [user_metadata]
                                    (when-not synthetic
                                      (-> user_metadata
                                          walk/stringify-keys
                                          stringify-values))))))

(defn encode
  ([sensor]
   (encode sensor false))
  ([sensor remove-pk?]
   (-> sensor
       (cond-> (:user_metadata sensor) (user-metadata (:synthetic sensor)))
       (cond-> remove-pk? (dissoc :device_id :sensor_id))
       (cond-> (:alias_sensor sensor)
               (assoc :alias_sensor {"sensor_id" (get-in sensor [:alias_sensor :sensor_id])
                                     "device_id" (get-in sensor [:alias_sensor :device_id])})))))


(defn sensor-time-range [sensor session]
  (validate-and-log sensor)
  (if-let [alias-sensor (not-empty (:alias_sensor sensor))]
    (let [{:strs [device_id sensor_id]} alias-sensor]
      (first
       (db/execute session
                   (hayt/select :sensor_metadata
                                (hayt/columns :lower_ts :upper_ts)
                                (hayt/where [[= :device_id device_id]
                                             [= :sensor_id sensor_id]])))))
    (let [{:keys [device_id sensor_id]} sensor]
      (first
       (db/execute session
                   (hayt/select :sensor_metadata
                                (hayt/columns :lower_ts :upper_ts)
                                (hayt/where [[= :device_id device_id]
                                             [= :sensor_id sensor_id]])))))))

(defn add-metadata [sensor session]
  (validate-and-log sensor)
  (let [{:keys [lower_ts upper_ts]} (sensor-time-range sensor session)]
    (-> sensor
        (assoc :lower_ts lower_ts)
        (assoc :upper_ts upper_ts))))

(defn enrich-sensor [sensor session]
  (-> sensor
      (dissoc :user_id)
      (add-metadata session)))

(defn get-sensors [device_id session]
  (->> (db/execute session
                   (hayt/select :sensors
                                (hayt/where [[= :device_id device_id]])))
       (mapv #(enrich-sensor % session))))

(defn get-sensors-by-device_ids [device_ids session]
  (db/execute session
              (hayt/select :sensors
                           (hayt/where [[:in :device_id device_ids]]))))

(defn insert
  ([session sensor metadata]
     (validate-and-log sensor)
     (let [encoded-sensor (encode sensor)]
       (log/debugf "Inserting sensor: %s" encoded-sensor)
       (db/execute session (hayt/insert :sensors
                                        (hayt/values encoded-sensor)))
       (db/execute session (hayt/insert :sensor_metadata
                                        (hayt/values metadata)))))
  ([session sensor]
     (validate-and-log sensor)
     (let [encoded-sensor (encode sensor)]
       (log/debugf "Inserting sensor: %s" encoded-sensor)
       (db/execute session (hayt/insert :sensors
                                        (hayt/values encoded-sensor)))
       (db/execute session (hayt/insert :sensor_metadata
                                        (hayt/values {:device_id (:device_id sensor) :sensor_id (:sensor_id sensor)}))))))

(defn update-user-metadata [sensor]
  ;; sensor has primary keys removed by now
  (if-not (empty? (:user_metadata sensor))
    (update-in sensor [:user_metadata] (fn [metadata] [+ metadata]))
    sensor))

(defn update
  ([session sensor]
     (validate-and-log sensor)
     (update session (:device_id sensor) sensor))
  ([session device_id sensor]
     (validate-and-log sensor)
     (db/execute session (hayt/update :sensors
                                      (hayt/set-columns (-> sensor
                                                            (encode :remove-pk)
                                                            update-user-metadata))
                                      (hayt/where [[= :device_id device_id]
                                                   [= :sensor_id (:sensor_id sensor)]]))))
  ([session device_id sensor metadata]
     (validate-and-log sensor)
     (db/execute session (hayt/update :sensors
                                      (hayt/set-columns (-> sensor
                                                            (encode :remove-pk)
                                                            update-user-metadata))
                                      (hayt/where [[= :device_id device_id]
                                                   [= :sensor_id (:sensor_id sensor)]])))
     (db/execute session (hayt/update :sensor_metadata
                                      (hayt/set-columns (-> metadata
                                                            (encode :remove-pk)))
                                      (hayt/where [[= :device_id device_id]
                                                   [= :sensor_id (:sensor_id sensor)]])))))

(defn ->clojure
  "Sensors are called readings in the API."
  [device_id session]
  (get-sensors device_id session))

(defn delete-measurements [sensor session]
  (validate-and-log sensor)
  (let [{:keys [lower_ts upper_ts]} (sensor-time-range sensor session)]
    (when (and lower_ts upper_ts)
      (let [measurements-result (measurements/delete sensor lower_ts upper_ts session)
            sensor_metadata-result (update-sensor-metadata session sensor lower_ts upper_ts)]
        {assoc measurements-result
         :sensor_metadata sensor_metadata-result}))))

(defn delete
  ([sensor session]
     (validate-and-log sensor)
     (let [{:keys [device_id sensor_id]} sensor
           sensor-response
           (db/execute session (hayt/delete :sensors
                                            (hayt/where [[= :device_id device_id]
                                                         [= :sensor_id sensor_id]])))
           sensor_metadata-response
           (db/execute session (hayt/delete :sensor_metadata
                                            (hayt/where [[= :device_id device_id]
                                                         [= :sensor_id sensor_id]])))]
       {:sensors sensor-response
        :sensor_metadata sensor_metadata-response}))
  ([sensor measurements? session]
     (validate-and-log sensor)
     (if measurements?
       (merge (delete sensor session)
              (delete-measurements sensor session))
       (delete sensor session))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sensor information

(defn all-sensors
  "Eetrieves all sensors data joined with their metadata."
  [store]
  (db/with-session [session (:hecuba-session store)]
    (let [all-sensors-metadata (db/execute session (hayt/select :sensor_metadata))]
      (map #(merge (first (db/execute session
                                      (hayt/select :sensors
                                                   (hayt/where [[= :device_id (:device_id %)] [= :sensor_id (:sensor_id %)]]))))
                   %) all-sensors-metadata))))

(defn all-sensor-metadata-for-device
  "Given device_id, retrieves all sensors metadata."
  [store device_id]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session (hayt/select :sensor_metadata (hayt/where [[= :device_id device_id]])))))

(defn merge-sensor-metadata [store sensor]
  (when (seq sensor)
    (db/with-session [session (:hecuba-session store)]
      (merge (first (db/execute session (hayt/select :sensor_metadata
                                                     (hayt/where [[= :device_id (:device_id sensor)]
                                                                  [= :sensor_id (:sensor_id sensor)]])))) sensor))))

(defn all-sensor-information
  "Given store,  device_id and type, combines data from sensor and sensor_metadata tables
  for that sensor."
  [store device_id sensor_id]
  (db/with-session [session (:hecuba-session store)]
    (let [sensor    (get-by-id {:device_id device_id :sensor_id sensor_id} session)]
      (when sensor
        (merge-sensor-metadata store sensor)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Lookup

(defn find-broken-sensors
  "Finds sensors with bad metadata and label as broken.
  Returns sequence of maps."
  [store]
  (let [where [= :status "Broken"]]
    (db/with-session [session (:hecuba-session store)]
      (db/execute session
                  (hayt/select :sensors (hayt/where where))))))

(defn find-mislabelled-sensors
  "Finds sensots that are marked as mislabelled.
  Returns sequence of maps."
  [store]
  (let [where [= :mislabelled "true"]]
    (db/with-session [session (:hecuba-session store)]
      (db/execute session
                  (hayt/select :sensor_metadata (hayt/where where))))))

(defn output-unit-for [t]
  (log/error t)
  (case (.toUpperCase t)
    "VOL2KWH" "kWh"
    "KWH2CO2" "co2"))

(defn output-type-for [t operation]
  (let [op (if (string? operation)
             (.toUpperCase operation)
             operation)]
    (case op
      "VOL2KWH" (str t "_kwh")
      "KWH2CO2" (str t "_co2"))))
