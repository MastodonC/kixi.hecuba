(ns kixi.hecuba.data.sensors
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.webutil :refer (stringify-values)]
            [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.misc :as misc] ;; FIXME
            [clojure.walk :as walk]
            [schema.core :as s]))

(def Sensor {(s/required-key :device_id)                   s/Str
             (s/required-key :type)                        s/Str
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
             s/Any                                         s/Any})



(defn user-metadata [sensor synthetic]
  (-> sensor
      (merge (stringify-values (dissoc sensor :user_metadata :actual_annual :synthetic))) ;; actual_annual is a boolean
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
         (cond-> (seq (:user-metadata sensor)) (user-metadata (:synthetic sensor)))
         (cond-> remove-pk? (dissoc :device_id :type)))))

(defn sensor-time-range [sensor session]
  (s/validate Sensor sensor)
  (let [{:keys [device_id type]} sensor]
    (first
     (db/execute session
                 (hayt/select :sensor_metadata
                              (hayt/columns :lower_ts :upper_ts)
                              (hayt/where [[= :device_id device_id]
                                           [= :type type]]))))))

(defn add-metadata [sensor session]
  (s/validate Sensor sensor)
  (let [{:keys [device_id type]}    sensor
        {:keys [lower_ts upper_ts]} (sensor-time-range sensor session)]
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

(defn get-by-id
  ([{:keys [device_id type]} session]
     (db/execute session
                 (hayt/select :sensors
                              (hayt/where [[= :type type]
                                           [= :device_id device_id]])))))

(defn insert
  ([session sensor metadata]
     (s/validate Sensor sensor)
     (let [encoded-sensor (encode sensor)]
       (log/debugf "Inserting sensor: %s" encoded-sensor)
       (db/execute session (hayt/insert :sensors
                                        (hayt/values encoded-sensor)))
       (db/execute session (hayt/insert :sensor_metadata
                                        (hayt/values metadata)))))
  ([session sensor]
     (s/validate Sensor sensor)
     (let [encoded-sensor (encode sensor)]
       (log/debugf "Inserting sensor: %s" encoded-sensor)
       (db/execute session (hayt/insert :sensors
                                        (hayt/values encoded-sensor)))
       (db/execute session (hayt/insert :sensor_metadata
                                        (hayt/values {:device_id (:device_id sensor) :type (:type sensor)}))))))

(defn update-user-metadata [sensor]
  ;; sensor has primary keys removed by now
  (if-not (empty? (:user_metadata sensor))
    (update-in sensor [:user_metadata] (fn [metadata] [+ metadata]))
    sensor))

(defn update
  ([session sensor]
     (s/validate Sensor sensor)
     (update session (:device_id sensor) sensor))
  ([session device_id sensor]
     (s/validate Sensor sensor)
     (db/execute session (hayt/update :sensors
                                      (hayt/set-columns (-> sensor
                                                            (encode :remove-pk)
                                                            update-user-metadata))
                                      (hayt/where [[= :device_id device_id]
                                                   [= :type (:type sensor)]]))))
  ([session device_id sensor metadata]
     (s/validate Sensor sensor)
     (db/execute session (hayt/update :sensors
                                      (hayt/set-columns (-> sensor
                                                            (encode :remove-pk)
                                                            update-user-metadata))
                                      (hayt/where [[= :device_id device_id]
                                                   [= :type (:type sensor)]])))
     (db/execute session (hayt/update :sensor_metadata
                                      (hayt/set-columns (-> metadata
                                                            (encode :remove-pk)))
                                      (hayt/where [[= :device_id device_id]
                                                   [= :type (:type sensor)]])))))

(defn ->clojure
  "Sensors are called readings in the API."
  [device_id session]
  (get-sensors device_id session))

(defn delete-measurements [sensor session]
  (s/validate Sensor sensor)
  (let [{:keys [lower_ts upper_ts]} (sensor-time-range sensor session)]
    (when (and lower_ts upper_ts)
      (let [measurements-result (measurements/delete sensor lower_ts upper_ts session)
            sensor_metadata-result (misc/update-sensor-metadata session sensor lower_ts upper_ts)]
        {assoc measurements-result
         :sensor_metadata sensor_metadata-result}))))

(defn delete
  ([sensor session]
     (s/validate Sensor sensor)
     (let [{:keys [device_id type]} sensor
           sensor-response
           (db/execute session (hayt/delete :sensors
                                            (hayt/where [[= :device_id device_id]
                                                         [= :type type]])))
           sensor_metadata-response
           (db/execute session (hayt/delete :sensor_metadata
                                            (hayt/where [[= :device_id device_id]
                                                         [= :type type]])))]
       {:sensors sensor-response
        :sensor_metadata sensor_metadata-response}))
  ([sensor measurements? session]
     (s/validate Sensor sensor)
     (if measurements?
       (merge (delete sensor session)
              (delete-measurements sensor session))
       (delete sensor session))))
