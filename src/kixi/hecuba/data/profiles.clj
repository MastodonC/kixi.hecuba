(ns kixi.hecuba.data.profiles
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [schema.core :as s]
            [schema-contrib.core :as sc]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data :refer [parse-item parse-list] :as data]
            [kixi.hecuba.schema-utils :as su]
            [cheshire.core :as json]))

(defn decode [profile]
  (-> profile
      (assoc :profile_id (:id profile))
      (dissoc :id)
      ;; id text,
      ;; airflow_measurements list<text>,
      (parse-list :airflow_measurements)
      ;; biomasses list<text>
      (parse-list :biomasses)
      ;; chps list<text>,
      (parse-list :chps)
      ;; conservatories list<text>,
      (parse-list :conservatories)
      ;; door_sets list<text>,
      (parse-list :door_sets)
      ;; entity_id text,
      ;; extensions list<text>,
      (parse-list :extensions)
      ;; floors list<text>,
      (parse-list :floors)
      ;; heat_pumps list<text>,
      (parse-list :heat_pumps)
      ;; heating_systems list<text>,
      (parse-list :heating_systems)
      ;; hot_water_systems list<text>,
      (parse-list :hot_water_systems)
      ;; low_energy_lights list<text>,
      (parse-list :low_energy_lights)
      ;; photovoltaics list<text>,
      (parse-list :photovoltaics)
      ;; profile_data text,
      (parse-item :profile_data)
      ;; roof_rooms list<text>,
      (parse-list :roof_rooms)
      ;; roofs list<text>,
      (parse-list :roofs)
      ;; small_hydros list<text>,
      (parse-list :small_hydros)
      ;; solar_thermals list<text>,
      (parse-list :solar_thermals)
      ;; storeys list<text>,
      (parse-list :storeys)
      ;; thermal_images list<text>,
      (parse-list :thermal_images)
      ;; timestamp timestamp,
      ;; user_id text,
      ;; ventilation_systems list<text>,
      (parse-list :ventilation_systems)
      ;; walls list<text>,
      (parse-list :walls)
      ;; wind_turbines list<text>,
      (parse-list :wind_turbines)
      ;; window_sets list<text>,
      (parse-list :window_sets)))

(defn fix-timestamp [profile]
  (if-let [ts (:timestamp profile)]
    (if-let [d (tf/parse (tf/formatters :date-time) ts)]
      (assoc profile :timestamp (tc/to-date d))
      profile)
    profile))

(defn encode [profile]
  (-> profile
      (assoc :id (:profile_id profile))
      (dissoc :profile_id)
      fix-timestamp
      (data/assoc-encode-item-if :profile_data (:profile_data profile))
      (data/assoc-encode-list-if :airflow_measurements (:airflow_measurements profile))
      (data/assoc-encode-list-if :biomasses (:biomasses profile))
      (data/assoc-encode-list-if :chps (:chps profile))
      (data/assoc-encode-list-if :conservatories (:conservatories profile))
      (data/assoc-encode-list-if :door_sets (:door_sets profile))
      (data/assoc-encode-list-if :extensions (:extensions profile))
      (data/assoc-encode-list-if :floors (:floors profile))
      (data/assoc-encode-list-if :heat_pumps (:heat_pumps profile))
      (data/assoc-encode-list-if :heating_systems (:heating_systems profile))
      (data/assoc-encode-list-if :hot_water_systems (:hot_water_systems profile))
      (data/assoc-encode-list-if :low_energy_lights (:low_energy_lights profile))
      (data/assoc-encode-list-if :photovoltaics (:photovoltaics profile))
      (data/assoc-encode-list-if :roof_rooms (:roof_rooms profile))
      (data/assoc-encode-list-if :roofs (:roofs profile))
      (data/assoc-encode-list-if :small_hydros (:small_hydros profile))
      (data/assoc-encode-list-if :solar_thermals (:solar_thermals profile))
      (data/assoc-encode-list-if :storeys (:storeys profile))
      (data/assoc-encode-list-if :thermal_images (:thermal_images profile))
      (data/assoc-encode-list-if :ventilation_systems (:ventilation_systems profile))
      (data/assoc-encode-list-if :walls (:walls profile))
      (data/assoc-encode-list-if :wind_turbines (:wind_turbines profile))
      (data/assoc-encode-list-if :window_sets (:window_sets profile))))

(defn get-profiles [entity_id session]
  (let [profiles (db/execute session (hayt/select :profiles (hayt/where [[= :entity_id entity_id]])))]
    (log/infof "Got %s profiles to parse" (count profiles))
    (mapv decode profiles)))

(defn ->clojure [entity_id session]
  (get-profiles entity_id session))

(defn get-by-id
  ([session id]
     (-> (db/execute session (hayt/select :profiles
                                          (hayt/where [[= :id id]])))
         first
         decode)))

(defn delete [session id]
  (db/execute session (hayt/delete :profiles (hayt/where [[= :id id]]))))

(def InsertableProfile
  {:profile_id s/Str
   (s/optional-key :biomasses) [{s/Keyword s/Any}]
   (s/optional-key :airflow_measurements) [{s/Keyword s/Any}]
   (s/optional-key :chps) [{s/Keyword s/Any}]
   (s/optional-key :conservatories) [{s/Keyword s/Any}]
   (s/optional-key :door_sets) [{s/Keyword s/Any}]
   :entity_id s/Str
   (s/optional-key :extensions) [{s/Keyword s/Any}]
   (s/optional-key :floors) [{s/Keyword s/Any}]
   (s/optional-key :heat_pumps) [{s/Keyword s/Any}]
   (s/optional-key :heating_systems) [{s/Keyword s/Any}]
   (s/optional-key :hot_water_systems) [{s/Keyword s/Any}]
   (s/optional-key :low_energy_lights) [{s/Keyword s/Any}]
   (s/optional-key :photovoltaics) [{s/Keyword s/Any}]
   (s/optional-key :profile_data) {s/Keyword s/Str}
   (s/optional-key :roof_rooms) [{s/Keyword s/Any}]
   (s/optional-key :roofs) [{s/Keyword s/Any}]
   (s/optional-key :small_hydros) [{s/Keyword s/Any}]
   (s/optional-key :solar_thermals) [{s/Keyword s/Any}]
   (s/optional-key :storeys) [{s/Keyword s/Any}]
   (s/optional-key :thermal_images) [{s/Keyword s/Any}]
   (s/optional-key :timestamp) (s/maybe sc/ISO-Date-Time)
   (s/optional-key :user_id)  s/Str
   (s/optional-key :ventilation_systems) [{s/Keyword s/Any}]
   (s/optional-key :walls) [{s/Keyword s/Any}]
   (s/optional-key :wind_turbines) [{s/Keyword s/Any}]
   (s/optional-key :window_sets) [{s/Keyword s/Any}]})

(defn insert [session profile]
  (try
    (let [insertable-profile (su/select-keys-by-schema profile InsertableProfile)]
      (s/validate InsertableProfile insertable-profile)
      (db/execute session (hayt/insert :profiles (hayt/values (encode insertable-profile)))))
    (catch Throwable t
      (log/errorf t "Could not insert: %s" (pr-str profile))
      (throw t))))

(defn update [session id profile]
  (try
    (let [insertable-profile (su/select-keys-by-schema profile InsertableProfile)]
      (db/execute session (hayt/update :profiles (hayt/set-columns (dissoc (encode insertable-profile) :id))
                                       (hayt/where [[= :id id]]))))
    (catch Throwable t
      (log/errorf t "Could not update: %s" (pr-str profile))
      (throw t))))

(defn has-technology? [profile technology]
  (-> profile (get technology) seq nil? not))

(defn has-walls-technology? [profile technology]
  (when-let [walls (seq (-> profile :walls))]
    (not (nil? (some #(= (:insulation %) technology) walls)))))
