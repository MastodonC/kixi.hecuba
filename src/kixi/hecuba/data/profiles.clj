(ns kixi.hecuba.data.profiles
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data :refer [parse-item parse-list]]))

(defn parse-profile [profile]
  (-> profile
      ;; id text,
      ;; airflow_measurements list<text>,
      (parse-list :airflow_measurements)
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

(defn get-profiles [entity_id session]
  (db/execute session (hayt/select :profiles (hayt/where [[= :entity_id entity_id]]))))

(defn ->clojure [entity_id session]
  (let [profiles (get-profiles entity_id session)]
    (log/infof "Got %s profiles to parse" (count profiles))
    (mapv parse-profile profiles)))
