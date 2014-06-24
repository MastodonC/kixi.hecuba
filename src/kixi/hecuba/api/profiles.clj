(ns kixi.hecuba.api.profiles
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values update-stringified-lists sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]))

(def ^:private entity-profiles-resource (p/resource-path-string :entity-profiles-resource))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          method       (:request-method request)
          route-params (:route-params request)
          entity_id    (:entity_id route-params)
          entity       (-> (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]]))) first)]
      (case method
        :post (not (nil? entity))
        :get (let [items (db/execute session (hayt/select :profiles (hayt/where [[= :entity_id entity_id]])))]
               {::items items})))))

(defn add-profile-keys [& pairs]
  (->> pairs
       (map-indexed
         (fn [index pair]
           (let [k (first pair)
                 v (last  pair)]
           {(str "profile_" index "_key")   k
            (str "profile_" index "_value") v})))
       (into {})))

(defn attribute-type [attr]
  (if (keyword? attr)
    :attribute
    (:type attr)))

(def profile-data-schema
  [ :id
    :occupancy_under_18
    :onsite_days_new_build
    :flat_floor_heat_loss_type
    :best_u_value_for_walls
    :estimated_cost_new_build
    :ter
    :sap_version_year
    :total_envelope_area
    :sap_regulations_date
    :multiple_glazing_area_percentage
    :flat_floor_position
    :modelling_software_methods_used
    :co_heating_loss
    :sap_assessor
    :inadequate_heating
    :profile_noise
    :multiple_glazing_type
    :sap_software
    :profile_bus_summary_index
    :thermal_bridging_strategy
    :sealed_fireplaces
    :flat_floors_in_block
    :property_id
    :air_tightness_assessor
    :glazing_area_glass_only
    :final_cost_new_build
    :lighting_strategy
    :fabric_energy_efficiency
    :habitable_rooms
    :profile_needs
    :co_heating_assessor
    :best_u_value_for_other
    :renewable_contribution_heat
    :total_area
    :profile_temperature_in_summer
    :draught_proofing_location
    :heat_storage_present
    :profile_productivity
    :number_of_storeys
    :passive_solar_strategy
    :external_perimeter
    :intervention_completion_date
    :heat_loss_parameter_hlp
    :electricity_storage_present
    :roof_rooms_present
    :primary_energy_requirement
    :dwelling_u_value_other
    :ventilation_approach
    :construction_time_new_build
    :draught_proofing
    :frame_type
    :appliances_strategy
    :bedroom_count
    :co_heating_equipment
    :flat_heat_loss_corridor_other
    :ber
    :profile_image_to_visitors
    :air_tightness_equipment
    :innovation_approaches
    :orientation
    :total_budget_new_build
    :best_u_value_for_floors
    :completeness
    :onsite_days
    :water_saving_strategy
    :airtightness_and_ventilation_strategy
    :glazing_area_percentage
    :occupant_change
    :intention_ofpassvhaus
    :profile_health
    :occupancy_over_60
    :annual_heating_load
    :intervention_start_date
    :profile_design
    :gross_internal_area
    :profile_air_in_winter
    :intervention_description
    :mains_gas
    :profile_lightning
    :multiple_glazing_type_other
    :total_volume
    :sap_version_issue
    :profile_comfort
    :heated_habitable_rooms
    :open_fireplaces
    :occupancy_18_to_60
    :flat_length_sheltered_wall
    :planning_considerations
    :profile_bus_report_url
    :design_guidance
    :sap_rating
    :overheating_cooling_strategy
    :best_u_value_for_windows
    :used_passivehaus_principles
    :moisture_condensation_mould_strategy
    :ventilation_approach_other
    :sap_performed_on
    :best_u_value_for_doors
    :pipe_lagging
    :renewable_contribution_elec
    :controls_strategy
    :conservation_issues
    :annual_space_heating_requirement
    :air_tightness_performed_on
    :flat_heat_loss_corridor
    :total_rooms
    :space_heating_requirement
    :multiple_glazing_u_value
    :best_u_value_party_walls
    :best_u_value_for_roof
    :frame_type_other
    :electricity_meter_type
    :category
    :cellar_basement_issues
    :profile_air_in_summer
    :co_heating_performed_on
    :profile_temperature_in_winter
    :air_tightness_rate
    :footprint ])

(def window-set-schema
  [ :window_type
    :frame_type
    :frame_type_other
    :percentage_glazing
    :area
    :location
    :uvalue
    :created_at
    :updated_at ])

(def thermal-images-schema
  [])

(def storey-schema
  [:storey_type
   :storey
   :heat_loss_w_per_k
   :heat_requirement_kwth_per_year
   :created_at
   :updated_at ])

(def wall-schema
  [:wall_type
   :construction
   :construction_other
   :insulation
   :insulation_date
   :insulation_type
   :insulation_thickness
   :insulation_product
   :uvalue
   :location
   :area
   :created_at
   :updated_at ])

(def roof-schema
  [:roof_type
   :construction
   :construction_other
   :insulation_location_one
   :insulation_location_one_other
   :insulation_location_two
   :insulation_location_two_other
   :insulation_thickness_one
   :insulation_thickness_one_other
   :insulation_thickness_two
   :insulation_thickness_two_other
   :insulation_date
   :insulation_type
   :insulation_product
   :uvalue
   :uvalue_derived
   :created_at
   :updated_at])

(def floor-schema
  [:floor_type
   :construction
   :construction_other
   :insulation_thickness_one
   :insulation_thickness_two
   :insulation_type
   :insulation_product
   :uvalue
   :uvalue_derived
   :created_at
   :updated_at])

(def roof-room-schema
  [:location
   :age
   :insulation_placement
   :insulation_thickness_one
   :insulation_thickness_one_other
   :insulation_thickness_two
   :insulation_thickness_two_other
   :insulation_date
   :insulation_type
   :insulation_product
   :uvalue
   :uvalue_derived
   :created_at
   :updated_at])

(def door-set-schema
  [:door_type
   :door_type_other
   :frame_type
   :frame_type_other
   :percentage_glazing
   :area
   :location
   :uvalue
   :created_at
   :updated_at])

(def extension-schema
  [:age
   :construction_date
   :created_at
   :updated_at])

(def conservatory-schema
  [:conservatory_type
   :area
   :double_glazed
   :glazed_perimeter
   :height
   :created_at
   :updated_at])

(def wind-turbine-schema
  [:turbine_type
   :turbine_type_other
   :make_model
   :mcs_no
   :inverter_type
   :inverter_make_model
   :inverter_mcs_no
   :installer
   :installer_mcs_no
   :commissioning_date
   :capacity
   :hub_height
   :height_above_canpoy
   :wind_speed
   :wind_speed_info_source
   :wind_speed_info_source_other
   :est_annual_generation
   :est_percentage_requirement_met
   :est_percentage_exported
   :created_at
   :updated_at])

(def small-hydro-schema
  [:hydro_type
   :make_model
   :mcs_no
   :inverter_type
   :inverter_make_model
   :inverter_mcs_no
   :installer
   :installer_mcs_no
   :commissioning_date
   :capacity
   :head_drop
   :design_flow
   :est_annual_generation
   :est_percentage_requirement_met
   :est_percentage_exported
   :created_at
   :updated_at])

(def photovoltaic-schema
  [:percentage_roof_covered
   :photovoltaic_type
   :photovoltaic_type_other
   :make_model
   :mcs_no
   :efficiency
   :inverter_type
   :inverter_make_model
   :inverter_mcs_no
   :installer
   :installer_mcs_no
   :commissioning_date
   :capacity
   :area
   :orientation
   :pitch
   :est_annual_generation
   :est_percentage_requirement_met
   :est_percentage_exported
   :created_at
   :updated_at
   :performance])

(def solar-thermal-schema
  [:solar_type
   :solar_type_other
   :make_model
   :mcs_no
   :installer
   :installer_mcs_no
   :commissioning_date
   :capacity
   :area
   :orientation
   :pitch
   :est_annual_generation
   :est_percentage_requirement_met
   :created_at
   :updated_at])

(def heat-pump-schema
  [:heat_pump_type
   :make_model
   :cop
   :spf
   :mcs_no
   :installer
   :installer_mcs_no
   :commissioning_date
   :heat_source_type
   :heat_source_type_other
   :depth
   :geology
   :capacity
   :est_annual_generation
   :est_percentage_requirement_met
   :dhw
   :est_percentage_dhw_requirement_met
   :created_at
   :updated_at])

(def biomass-schema
  [:biomass_type
   :model
   :mcs_no
   :installer
   :installer_mcs_no
   :commissioning_date
   :capacity
   :percentage_efficiency_from_spec
   :est_annual_generation
   :est_percentage_requirement_met
   :created_at
   :updated_at])

(def chp-schema
  [:chp_type
   :model
   :mcs_no
   :installer
   :installer_mcs_no
   :commissioning_date
   :capacity_elec
   :capacity_thermal
   :est_annual_generation
   :est_percentage_thermal_requirement_met
   :est_percentage_exported
   :created_at
   :updated_at])

(def heating-system-schema
  [:heating_type
   :heat_source
   :heat_transport
   :heat_delivery
   :heat_delivery_source
   :efficiency_derivation
   :boiler_type
   :boiler_type_other
   :fan_flue
   :open_flue
   :fuel
   :heating_system
   :heating_system_other
   :heating_system_type
   :heating_system_type_other
   :heating_system_solid_fuel
   :heating_system_solid_fuel_other
   :bed_index
   :make_and_model
   :controls
   :controls_other
   :controls_make_and_model
   :emitter
   :trvs_on_emitters
   :use_hours_per_week
   :installer
   :installer_engineers_name
   :installer_registration_number
   :commissioning_date
   :inspector
   :inspector_engineers_name
   :inspector_registration_number
   :inspection_date
   :created_at
   :updated_at
   :efficiency])

(def hot-water-system-schema
  [:dhw_type
   :fuel
   :fuel_other
   :immersion
   :cylinder_capacity
   :cylinder_capacity_other
   :cylinder_insulation_type
   :cylinder_insulation_type_other
   :cylinder_insulation_thickness
   :cylinder_insulation_thickness_other
   :cylinder_thermostat
   :controls_same_for_all_zones
   :created_at
   :updated_at])

(def low-energy-light-schema
  [:light_type
   :light_type_other
   :bed_index
   :created_at
   :updated_at
   :proportion])

(def ventilation-system-schema
  [:approach
   :approach_other
   :ventilation_type
   :ventilation_type_other
   :mechanical_with_heat_recovery
   :manufacturer
   :ductwork_type
   :ductwork_type_other
   :controls
   :controls_other
   :manual_control_location
   :operational_settings
   :operational_settings_other
   :installer
   :installer_engineers_name
   :installer_registration_number
   :commissioning_date
   :total_installed_area
   :created_at
   :updated_at])

(def airflow-measurement-schema
  [:reference
   :system
   :inspector
   :inspector_engineers_name
   :inspector_registration_number
   :inspection_date
   :created_at
   :updated_at
   :measured_low
   :design_low
   :measured_high
   :design_high])

(def profile-schema
  [ :id
    :entity_id
    { :name    :profile_data
      :type    :nested-item
      :schema  profile-data-schema }
    { :name    :window_sets
      :type    :associated-items
      :schema   window-set-schema}
    { :name    :thermal-images
      :type    :associated-items
      :schema  thermal-images-schema }
    { :name    :storeys
      :type    :associated-items
      :schema  storey-schema }
    { :name    :walls
      :type    :associated-items
      :schema  wall-schema }
    { :name    :roofs
      :type    :associated-items
      :schema  roof-schema }
    { :name    :floors
      :type    :associated-items
      :schema  floor-schema }
    { :name    :roof_rooms
      :type    :associated-items
      :schema  roof-room-schema }
    { :name    :door_sets
      :type    :associated-items
      :schema  door-set-schema }
    { :name    :extensions
      :type    :associated-items
      :schema  extension-schema }
    { :name    :conservatories
      :type    :associated-items
      :schema  conservatory-schema }
    { :name    :wind_turbines
      :type    :associated-items
      :schema  wind-turbine-schema }
    { :name    :small_hydros
      :type    :associated-items
      :schema  small-hydro-schema }
    { :name    :photovoltaics
      :type    :associated-items
      :schema  photovoltaic-schema }
    { :name    :solar_thermals
      :type    :associated-items
      :schema  solar-thermal-schema }
    { :name    :heat_pumps
      :type    :associated-items
      :schema  heat-pump-schema }
    { :name    :biomasses
      :type    :associated-items
      :schema  biomass-schema }
    { :name    :chps
      :type    :associated-items
      :schema  chp-schema }
    { :name    :heating_systems
      :type    :associated-items
      :schema  heating-system-schema }
    { :name    :hot_water_systems
      :type    :associated-items
      :schema  hot-water-system-schema }
    { :name    :low_energy_lights
      :type    :associated-items
      :schema  low-energy-light-schema }
    { :name    :ventilation_systems
      :type    :associated-items
      :schema  ventilation-system-schema }
    { :name    :airflow_measurements
      :type    :associated-items
      :schema  airflow-measurement-schema }])

(defn explode-nested-item [association item-string]
  (let [item (json/decode item-string)
        association-name   (:name   association)
        association-schema (:schema association)]
    (->> association-schema
         (map
           (fn [attr]
             [ (str (name association-name) "_" (name attr)) (item (name attr)) ])))))

(defn explode-associated-items [association items]
  (let [association-name   (name (:name    association))
        association-schema (:schema  association)]
    (apply concat
    (map-indexed
      (fn [index item-string]
         (let [item-name         (str association-name "_" index)
               named-association (assoc association :name item-name)]
           (if (empty? association-schema)
             [ item-name item-string ]
             (explode-nested-item named-association item-string))))
      items))))

(defn explode-and-sort-by-schema [item schema]
  (let [exploded-item
        (->> schema
             (mapcat
               (fn [attr]
                 (let [t (attribute-type attr)]
                   (case t
                     :attribute          (do
                                           (list [(name attr) (item attr)]))
                     :nested-item        (do
                                           (explode-nested-item attr (item (:name attr))))
                     :associated-items   (do
                                           (explode-associated-items attr (item (:name attr)))))))))]
    exploded-item))

(defn extract-attribute [attr input]
  (println "Attribute: " attr)
  (println "Input: " input)
  {attr (input attr)})

(defn extract-nested-item [attr input]
  (let [association-name   (:name   attr)
        association-schema (:schema attr)
        nested-item (->> association-schema
                         (reduce
                           (fn [nested-item attr]
                             (let [attr-name (name attr)
                                   exploded-attr-name (str association-name "_" attr-name)]
                               { (keyword attr-name) (input exploded-attr-name) }))
                           {}))]
    {association-name nested-item}))

(defn extract-associated-items [attr input]
  {})

(defn parse-by-schema [input schema]
  (->> schema
       (reduce
         (fn [item attr]
           (let [t (attribute-type attr)]
             (case t
               :attribute               (extract-attribute attr input)
               :nested-item             (extract-nested-item attr input)
               :associated-item         (extract-associated-items attr input))))
         {})))

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request
        entity_id (:entity_id route-params)]
    (case request-method
      :post (let [decoded-body  (decode-body request)
                  _ (log/info "BODY SIZE: " (count decoded-body))
                  _ (log/info "Oh damn, why is it just one vector-in-a-list? Let's see what's in there at least")
                  raw-body (first decoded-body)
                  _ (log/info "RAW BODY:\n" raw-body)
                  _ (log/info "BODY SIZE:\t" (count raw-body))
                  _ (log/info "doesn't make much sense does it... it's like we got back a vector of strings, joined on \\n")
                  body     (if (= "text/csv" (:content-type request))
                             (let [body-map (reduce conj {} raw-body)
                                   _ (println "BODY:\n" body-map)]
                               (parse-by-schema body-map profile-schema))
                             raw-body)]
              ;; We need to assert a few things
              (if (not= (:entity_id body) entity_id)
                true                  ; it's malformed, game over
                [false {:body body}]))  ; it's not malformed, return the body now we've read it
      false)))

(defn index-handle-ok [ctx]
  (let [{items ::items
         {mime :media-type} :representation} ctx
         userless-items (->> items
                             (map #(update-in % [:timestamp] str))
                             (map #(dissoc % :user_id)))
         formatted-items (if (= "text/csv" mime)
                           ; serving tall csv style profiles
                           (let [exploded-items (->> userless-items
                                                     (map #(explode-and-sort-by-schema % profile-schema)))]
                             (apply util/map-longest
                                    add-profile-keys ["" ""] exploded-items))
                           ; serving json profiles
                           userless-items)]
    (util/render-items ctx formatted-items)))

(defn index-handle-created [ctx]
  (let [entity_id  (-> ctx :request :route-params :entity_id)
        profile_id (:profile_id ctx)]
    (if-not (empty? profile_id)
      (let [location (format entity-profiles-resource entity_id profile_id)]
        (when-not location
          (throw (ex-info "No path resolved for Location header"
                          {:entity_id entity_id
                           :profile_id profile_id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422
                      :body "Provide valid entity_id and timestamp."}))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request    (-> ctx :request)
          _          (log/infof "resource-exists? request: %s" request)
          entity_id  (-> request :params :entity_id)
          profile_id (-> request :params :profile_id)
          item       (first (db/execute session (hayt/select
                                                 :profiles
                                                 (hayt/where [[= :id profile_id]]))))]
      (if-not (empty? item)
        {::item (-> item
                    (assoc :profile_id profile_id)
                    (dissoc :id))}
        false))))

(defn resource-delete-enacted? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item} ctx
          profile_id (:profile_id item)
          response (db/execute session (hayt/delete :profiles (hayt/where [[= :id profile_id]])))]
      (empty? response))))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{request :request} ctx]
      (if-let [item (::item ctx)]
        (let [body       (decode-body request)
              entity_id  (-> item :entity_id)
              username   (sec/session-username (-> ctx :request :session))
              user_id    (:id (users/get-by-username session username))
              profile_id (-> item :profile-id)]
          (db/execute session (hayt/insert :profiles (hayt/values (-> body
                                                                      (assoc :id profile_id)
                                                                      (assoc :user_id user_id)
                                                                      ;; TODO: add storeys, walls, etc.
                                                                      stringify-values)))))
        (ring-response {:status 404 :body "Please provide valid entity_id and timestamp"})))))

(defn resource-handle-ok [store ctx]
  (let [{item ::item
         {mime :media-type} :representation} ctx
        userless-item (-> item
            (update-in [:timestamp] str)
            (dissoc :user_id))
        formatted-item (if (= "text/csv" mime)
                         (let [exploded-item (explode-and-sort-by-schema userless-item profile-schema)]
                           exploded-item)
                         userless-item)]
    (util/render-item (:request ctx) formatted-item)))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request    (-> ctx :request)
          profile    (-> ctx :body)
          entity_id  (-> profile :entity_id)
          timestamp  (-> profile :timestamp)
          username   (sec/session-username (-> ctx :request :session))
          ;; FIXME: Why user_id?
          user_id    (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
          profile_id (sha1/gen-key :profile profile)]
      (when (and entity_id timestamp)
        (when-not (empty? (-> (db/execute session (hayt/select :entities (hayt/where [[= :id entity_id]]))) first))
          (db/execute session (hayt/insert :profiles (-> profile
                                                         (assoc :user_id user_id)
                                                         (update-stringified-lists
                                                          [:airflow_measurements :chps
                                                           :conservatories :door_sets
                                                           :extensions :floors :heat_pumps
                                                           :heating_systems :hot_water_systems
                                                           :low_energy_lights :photovoltaics
                                                           :roof_rooms :roofs :small_hydros
                                                           :solar_thermals :storeys :thermal_images
                                                           :ventilation_systems :walls
                                                           :wind_turbines :window_sets])
                                                         (update-in [:profile_data] json/encode)
                                                         )))
          {:profile_id profile_id})))))


(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types #{"text/csv" "application/json" "application/edn"}
  :known-content-type? #{"text/csv" "application/json" "application/edn"}
  :authorized? (authorized? store)
  :exists? (partial index-exists? store)
  :malformed? index-malformed?
  :post! (partial index-post! store)
  :handle-ok index-handle-ok
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"text/csv" "application/json"}
  :known-content-type? #{"text/csv" "application/json"}
  :authorized? (authorized? store)
  :exists? (partial resource-exists? store)
  :delete-enacted? (partial resource-delete-enacted? store)
  :respond-with-entity? resource-respond-with-entity
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok store))
