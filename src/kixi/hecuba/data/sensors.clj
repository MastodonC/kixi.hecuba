(ns kixi.hecuba.data.sensors
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.webutil :refer (stringify-values)]
            [clojure.walk :as walk]
            [schema.core :as s]))

(def user-editable-keys [:device_id :type :accuracy
                        :corrected_unit :correction
                        :correction_factor :correction_factor_breakdown
                        :frequency :max :min :period
                        :resolution :unit :user_metadata])

(defn user-metadata [sensor synthetic]
  (-> sensor
      (merge (stringify-values (dissoc sensor :user_metadata)))
      (update-in [:user_metadata] (fn [user_metadata]
                                    (when-not synthetic
                                      (-> user_metadata
                                          walk/stringify-keys
                                          stringify-values))))))

(defn encode
  ([sensor]
     (encode sensor false))
  ([sensor remove-pk?]
     (-> (select-keys sensor user-editable-keys)
         (user-metadata (:synthetic sensor))
         (cond-> remove-pk? (dissoc :device_id :type)))))

(def Sensor {(s/required-key :device_id)                   s/Str
             (s/required-key :type)                        s/Str
             (s/optional-key :accuracy)                    (s/maybe s/Str)
             (s/optional-key :actual_annual)               (s/maybe s/Bool)
             (s/optional-key :corrected_unit)              (s/maybe s/Str)
             (s/optional-key :correction)                  (s/maybe s/Str)
             (s/optional-key :correction_factor)           (s/maybe s/Str)
             (s/optional-key :correction_factor_breakdown) (s/maybe s/Str)
             (s/optional-key :frequency)                   (s/maybe s/Str)
             (s/optional-key :max)                         (s/maybe s/Str)
             (s/optional-key :median)                      double
             (s/optional-key :min)                         (s/maybe s/Str)
             (s/optional-key :period)                      (s/enum "INSTANT" "PULSE" "CUMULATIVE")
             (s/optional-key :resolution)                  (s/maybe s/Str)
             (s/optional-key :status)                      (s/maybe s/Str)
             (s/optional-key :synthetic)                   s/Bool
             (s/optional-key :unit)                        (s/maybe s/Str)
             ;; (s/enum "%RH" "Amps" "C" "Hz" "L" "Litres/5min" "Ls^-1"
             ;;         "V" "VA" "VAr" "W/m^2" "W/m^2.K" "degrees" "g/Kg"
             ;;         "kVArh" "kW" "kWh" "kWhth" "mA" "mV" "m^3" "ft^3"
             ;;         "m^3" "ft^3" "kWh" "m^3/h" "mbar" "millisecs"
             ;;         "ms^-1" "ppm" "wm-2" "" ;; allow blank for now.
             ;;         )
             (s/required-key :user_id)                     s/Str})

(defn invalid? [sensor]
  (s/check Sensor sensor))

(defn sensor-time-range [device_id type session]
  (first
   (db/execute session
               (hayt/select :sensor_metadata
                            (hayt/columns :lower_ts :upper_ts)
                            (hayt/where [[= :device_id device_id]
                                         [= :type type]])))))

(defn add-metadata [sensor session]
  (let [{:keys [device_id type]}    sensor
        {:keys [lower_ts upper_ts]} (sensor-time-range device_id type session)]
    (-> sensor
        (assoc :lower_ts lower_ts)
        (assoc :upper_ts upper_ts))))

(defn enrich-sensor [sensor session]
  (-> sensor
      (dissoc :user_id)
      (add-metadata session)))

(defn get-sensors [device_id session]
  (db/execute session
              (hayt/select :sensors
                           (hayt/where [[= :device_id device_id]]))))

(defn insert [session sensor]
  (db/execute session (hayt/insert :sensors
                                   (hayt/values (encode sensor))))
  (db/execute session (hayt/insert :sensor_metadata
                                   (hayt/values {:device_id (:device_id sensor) :type (:type sensor)}))))

(defn update-user-metadata [sensor]
  (if-not (empty? (:user_metadata sensor))
    (update-in sensor [:user_metadata] (fn [metadata] [+ metadata]))
    sensor))

(defn update
  ([session sensor]
     (update session (:device_id sensor) sensor))
  ([session device_id sensor]
     (db/execute session (hayt/update :sensors
                                      (hayt/set-columns (-> sensor
                                                            (encode :remove-pk)
                                                            update-user-metadata))
                                      (hayt/where [[= :device_id device_id]
                                                   [= :type (:type sensor)]])))))

(defn ->clojure
  "Sensors are called readings in the API."
  [device_id session]
  (let [sensors (get-sensors device_id session)]
    (mapv #(enrich-sensor % session) sensors)))
