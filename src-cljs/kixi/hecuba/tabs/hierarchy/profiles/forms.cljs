(ns kixi.hecuba.tabs.hierarchy.profiles.forms
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [kixi.hecuba.common :as common :refer (log)]))

(defn handle-change [owner keys e]
  (let [value (.-value (.-target e))]
    (om/set-state! owner keys value)))

(defn text-control [data owner keys label]
  (let [{:keys [editing adding]} (om/get-state owner)]
    [:div.form-group
     [:label.control-label {:for (name (last keys))} label]
     (if (or editing adding)
       [:div
        [:input {:defaultValue (when editing (get-in data keys ""))
                 :class "form-control"
                 :on-change #(handle-change owner keys %1)
                 :type "text"
                 :id (name (last keys))}]]
       [:p.form-control-static (get-in data keys "")])]))

(defn text-area-control [data owner keys title]
  (let [text    (get-in data keys)
        {:keys [editing adding]} (om/get-state owner)]
    (if (or editing adding)
      [:div
       [:div.form-group
        [:label.control-label title]
        [:form {:role "form"}
         [:textarea.form-control {:on-change #(handle-change owner keys %1) :rows 2} (when editing text)]]]]
      (if (and text (re-find #"\w" text))
        [:div [:h3 title]
         [:p text]]
        [:div {:class "hidden"} [:p.form-control-static text]]))))

(defn format-time-inst [t format]
  (if (nil? t)
    "Undated"
    (let [date (tc/from-date t)]
      (tf/unparse (tf/formatter format) date))))

(defn description [owner profile keys]
  [:div
   ;; FIXME format dates better
   (text-control profile owner (conj keys :intervention_start_date) "Intervention Start Date")
   (text-control profile owner (conj keys :intervention_completion_date) "Intervention Completion Date")
   (text-control profile owner (conj keys :intervention_description) "Intervention Description")])

(defn occupancy [owner profile keys]
  [:div
   (text-control profile owner (conj keys :occupancy_total) "Total Occupancy")
   (text-control profile owner (conj keys :occupancy_under_18) "Occupancy Under 18")
   (text-control profile owner (conj keys :occupancy_18_to_60) "Occupancy 18 To 60")
   (text-control profile owner (conj keys :occupancy_over_60) "Occupancy Over 60")
   (text-control profile owner (conj keys :occupant_change) "Occupant Change")])

(defn measurements [owner profile keys]
  [:div
   (text-control profile owner (conj keys :footprint) "Footprint (internal groundfloor area m2)")
   (text-control profile owner (conj keys :external_perimeter) "External Perimeter (m)")
   (text-control profile owner (conj keys :gross_internal_area) "Gross Internal Area (m2)")
   (text-control profile owner (conj keys :number_of_storeys) "Number Of Storeys")
   (text-control profile owner (conj keys :total_volume) "Total Volume (m3)")
   (text-control profile owner (conj keys :total_rooms) "Total Rooms")
   (text-control profile owner (conj keys :bedroom_count) "Total Bedrooms")
   (text-control profile owner (conj keys :habitable_rooms) "Habitable Rooms")
   (text-control profile owner (conj keys :inadequate_heating) "Inadequate Heating?")
   (text-control profile owner (conj keys :heated_habitable_rooms) "Heated Habitable Rooms")])

(defn energy [owner profile keys]
  [:div
   (text-control profile owner (conj keys :ber) "BER (kgCO2/m2/yr)")
   (text-control profile owner (conj keys :ter) "TER (kgCO2/m2/yr)")
   (text-control profile owner (conj keys :primary_energy_requirement) "Total Primary Energy Requirement")
   (text-control profile owner (conj keys :space_heating_requirement) "Space Heating Requirement")
   (text-control profile owner (conj keys :annual_space_heating_requirement) "Annual Space Heating Requirement")
   (text-control profile owner (conj keys :renewable_contribution_heat) "Renewable Contribution Heat")
   (text-control profile owner (conj keys :renewable_contribution_elec) "Renewable Contribution Elec")
   (text-control profile owner (conj keys :electricity_meter_type) "Electricity Meter Type")
   (text-control profile owner (conj keys :mains_gas) "Linked to Mains Gas Supply")
   (text-control profile owner (conj keys :electricity_storage_present) "Electricity Storage Present")
   (text-control profile owner (conj keys :heat_storage_present) "Heat Storage Present")])

(defn efficiency [owner profile keys]
    [:div
     (text-control profile owner (conj keys :pipe_lagging) "Pipe Lagging")
     (text-control profile owner (conj keys :draught_proofing)  "Draught Proofing")
     (text-control profile owner (conj keys :draught_proofing_location)  "Draught Proofing Location")])

(defn passivhaus [owner profile keys]
  [:div
   (text-control profile owner (conj keys :passive_solar_strategy) "Passive Solar Strategy")
   (text-control profile owner (conj keys :used_passivehaus_principles) "Used Passivehaus Principles")])

(defn flats [owner profile keys]
  [:div
   (text-control profile owner (conj keys :flat_floors_in_block) "Floors In Block")
   (text-control profile owner (conj keys :flat_floor_position) "Floor Position")
   (text-control profile owner (conj keys :flat_heat_loss_corridor) "Heat Loss Corridor")
   (text-control profile owner (conj keys :flat_heat_loss_corridor_other) "Heat Loss Corridor Other")
   (text-control profile owner (conj keys :flat_length_sheltered_wall) "Length of Sheltered Wall (m)")
   (text-control profile owner (conj keys :flat_floor_heat_loss_type) "Floor Heat Loss Type")])

(defn fireplaces [owner profile keys]
   [:div
    (text-control profile owner (conj keys :open_fireplaces) "Number of Open Fireplaces")
    (text-control profile owner (conj keys :sealed_fireplaces) "Number of Sealed Fireplaces")])

(defn glazing [owner profile keys]
   [:div
    (text-control profile owner (conj keys :glazing_area_glass_only) "Glazing Area Glass Only")
    (text-control profile owner (conj keys :glazing_area_percentage) "Glazing Area (% if known)")
    (text-control profile owner (conj keys :multiple_glazing_type) "Multiple Glazing Type")
    (text-control profile owner (conj keys :multiple_glazing_area_percentage) "Multiple Glazing Area (% if known)")
    (text-control profile owner (conj keys :multiple_glazing_u_value) "Multiple Glazing U Value")
    (text-control profile owner (conj keys :multiple_glazing_type_other) "Multiple Glazing Type Other")
    (text-control profile owner (conj keys :frame_type) "Frame Type")
    (text-control profile owner (conj keys :frame_type_other) "Frame Type Other")])

(defn issues [owner profile keys]
  [:div
   (text-control profile owner (conj keys :moisture_condensation_mould_strategy) "Moisture Condensation Mould Strategy")
   (text-control profile owner (conj keys :appliances_strategy) "Appliances Strategy")
   (text-control profile owner (conj keys :cellar_basement_issues) "Cellar Basement Issues")])

(defn sap-results [owner profile keys]
  [:div
   (text-control profile owner (conj keys :sap_rating) "SAP Rating")
   (text-control profile owner (conj keys :sap_performed_on) "SAP Performed On")
   (text-control profile owner (conj keys :sap_assessor) "SAP Assessor")
   (text-control profile owner (conj keys :sap_version_issue) "SAP Version Issue")
   (text-control profile owner (conj keys :sap_version_year) "SAP Version Year")
   (text-control profile owner (conj keys :sap_regulations_date) "SAP Regulations Date")
   (text-control profile owner (conj keys :sap_software) "Name of SAP Software")])

(defn lessons-learnt [owner profile keys]
    [:div
     (text-control profile owner (conj keys :thermal_bridging_strategy) "Thermal Bridging Strategy")
     (text-control profile owner (conj keys :airtightness_and_ventilation_strategy) "Airtightness And Ventilation Strategy")
     ;; moisture/mould/condensation?
     ;; passive solar?
     (text-control profile owner (conj keys :overheating_cooling_strategy) "Overheating Cooling Strategy")
     (text-control profile owner (conj keys :controls_strategy) "Controls Strategy")
     ;; appliances
     (text-control profile owner (conj keys :lighting_strategy) "Lighting Strategy")
     (text-control profile owner (conj keys :water_saving_strategy) "Water Saving Strategy")
     (text-control profile owner (conj keys :innovation_approaches) "Innovation Approaches")])

(defn dwelling-u-values-summary [owner profile keys]
   [:div
    (text-control profile owner (conj keys :best_u_value_for_doors) "Best U Value For Doors")
    (text-control profile owner (conj keys :best_u_value_for_floors) "Best U Value For Floors")
    (text-control profile owner (conj keys :best_u_value_for_other) "Best U Value For Other")
    (text-control profile owner (conj keys :best_u_value_for_roof) "Best U Value For Roof")
    (text-control profile owner (conj keys :best_u_value_for_walls) "Best U Value For Walls")
    (text-control profile owner (conj keys :best_u_value_for_windows) "Best U Value For Windows")
    (text-control profile owner (conj keys :best_u_value_party_walls) "Best U Value Party Walls")
    (text-control profile owner (conj keys :dwelling_u_value_other) "Dwelling U Value Other")])

(defn air-tightness-test [owner profile keys]
  [:div
   (text-control profile owner (conj keys :air_tightness_assessor) "Air Tightness Assessor")
   (text-control profile owner (conj keys :air_tightness_equipment) "Air Tightness Equipment")
   (text-control profile owner (conj keys :air_tightness_performed_on) "Air Tightness Performed On")
   (text-control profile owner (conj keys :air_tightness_rate) "Air Tightness Rate")])


(defn bus-survey-information [owner profile keys]
  [:div
   (text-control profile owner (conj keys :profile_temperature_in_summer) "Temperature In Summer")
   (text-control profile owner (conj keys :profile_temperature_in_winter) "Temperature In Winter")
   (text-control profile owner (conj keys :profile_air_in_summer) "Air In Summer")
   (text-control profile owner (conj keys :profile_air_in_winter) "Air In Winter")
   (text-control profile owner (conj keys :profile_lightning) "Lightning")
   (text-control profile owner (conj keys :profile_noise) "Noise")
   (text-control profile owner (conj keys :profile_comfort) "Comfort")
   (text-control profile owner (conj keys :profile_design) "Design")
   (text-control profile owner (conj keys :profile_needs) "Needs")
   (text-control profile owner (conj keys :profile_health) "Health (perceived)")
   (text-control profile owner (conj keys :profile_image_to_visitors) "Image To Visitors")
   (text-control profile owner (conj keys :profile_productivity) "Productivity")
   (text-control profile owner (conj keys :profile_bus_summary_index) "Bus Summary Index")
   (text-control profile owner (conj keys :profile_bus_report_url) "BUS Report Url")])

(defn project-details [owner profile keys]
  [:div
   (text-control profile owner (conj keys :total_budget_new_build) "Total Budget New Build")
   (text-control profile owner (conj keys :estimated_cost_new_build) "Estimated Cost New Build")
   (text-control profile owner (conj keys :final_cost_new_build) "Final Cost New Build")
   (text-control profile owner (conj keys :construction_time_new_build) "Construction Time New Build (Days)")
   (text-control profile owner (conj keys :design_guidance) "Design Guidance")
   (text-control profile owner (conj keys :planning_considerations) "Planning Considerations")
   (text-control profile owner (conj keys :total_budget) "Total Budget")])

(defn coheating-test [owner profile keys]
  [:div
   (text-control profile owner (conj keys :co_heating_loss) "Fabric Heat Loss (W/m2)")
   (text-control profile owner (conj keys :co_heating_performed_on) "Dates Performed")
   (text-control profile owner (conj keys :co_heating_assessor) "Assessor Used")
   (text-control profile owner (conj keys :co_heating_equipment) "Equipment")])

(defn energy-costs [owner profile keys]
  [:div
   (text-control profile owner (conj keys :gas_cost) "Gas Cost")
   (text-control profile owner (conj keys :electricity_cost) "Electricity Cost")])

(defn conservatory [owner profile keys]
  [:div
   (text-control profile owner (conj keys :conservatory_type) "Conservatory Type")
   (text-control profile owner (conj keys :area) "Area")
   (text-control profile owner (conj keys :double_glazed) "Double Glazed")
   (text-control profile owner (conj keys :glazed_perimeter) "Glazed Perimeter")
   (text-control profile owner (conj keys :height) "Height")])

(defn extension [owner profile keys]
  [:div
   (text-control profile owner (conj keys :age) "Age")
   (text-control profile owner (conj keys :construction_date) "Construction Date")])

(defn heating-system [owner profile keys]
  [:div
   (text-control profile owner (conj keys :heating_type) "Heating Type")
   (text-control profile owner (conj keys :heat_source) "Heat Source")
   (text-control profile owner (conj keys :heat_transport) "Heat Transport")
   (text-control profile owner (conj keys :heat_delivery) "Heat Delivery")
   (text-control profile owner (conj keys :heat_delivery_source) "Heat Delivery Source")
   (text-control profile owner (conj keys :efficiency_derivation) "Efficiency Derivation")
   (text-control profile owner (conj keys :boiler_type) "Boiler Type")
   (text-control profile owner (conj keys :boiler_type_other) "Boiler Type Other")
   (text-control profile owner (conj keys :fan_flue) "Fan Flue")
   (text-control profile owner (conj keys :open_flue) "Open Flue")
   (text-control profile owner (conj keys :fuel) "Fuel")
   (text-control profile owner (conj keys :heating_system) "Heating System")
   (text-control profile owner (conj keys :heating_system_other) "Heating System Other")
   (text-control profile owner (conj keys :heating_system_type) "Heating System Type")
   (text-control profile owner (conj keys :heating_system_type_other) "Heating System Type Other")
   (text-control profile owner (conj keys :heating_system_solid_fuel) "Heating System Solid Fuel")
   (text-control profile owner (conj keys :heating_system_solid_fuel_other) "Heating System Solid Fuel Other")
   (text-control profile owner (conj keys :bed_index) "Bed Index")
   (text-control profile owner (conj keys :make_and_model) "Make and Model")
   (text-control profile owner (conj keys :controls) "Controls")
   (text-control profile owner (conj keys :controls_other) "Controls Other")
   (text-control profile owner (conj keys :controls_make_and_model) "Controls Make and Model")
   (text-control profile owner (conj keys :emitter) "Emitter")
   (text-control profile owner (conj keys :trvs_on_emitters) "TRVs on Emitters")
   (text-control profile owner (conj keys :use_hours_per_week) "Use Hours per Week")
   (text-control profile owner (conj keys :installer) "Installer")
   (text-control profile owner (conj keys :installer_engineers_name) "Installer Engineers Name")
   (text-control profile owner (conj keys :installer_registration_number) "Installer Registration Number")
   (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
   (text-control profile owner (conj keys :inspector) "Inspector")
   (text-control profile owner (conj keys :inspector_engineers_name) "Inspector Engineers Name")
   (text-control profile owner (conj keys :inspector_registration_number) "Inspector Registration Number ")
   (text-control profile owner (conj keys :inspection_date) "Inspection Date")
   (text-control profile owner (conj keys :efficiency) "Efficiency")])

(defn hot-water-system [owner profile keys]
  [:div
   (text-control profile owner (conj keys :dhw_type) "DHW Type")
   (text-control profile owner (conj keys :fuel) "Fuel")
   (text-control profile owner (conj keys :fuel_other) "Fuel Other")
   (text-control profile owner (conj keys :immersion) "Immersion")
   (text-control profile owner (conj keys :cylinder_capacity) "Cylinder Capacity")
   (text-control profile owner (conj keys :cylinder_capacity_other) "Cylinder Capacity Other")
   (text-control profile owner (conj keys :cylinder_insulation_type) "Cylinder Insulation Type")
   (text-control profile owner (conj keys :cylinder_insulation_type_other) "Cylinder Insulation Type Other")
   (text-control profile owner (conj keys :cylinder_insulation_thickness) "Cylinder Insulation Thickness")
   (text-control profile owner (conj keys :cylinder_insulation_thickness_other) "Cylinder Insulation Thickness Other")
   (text-control profile owner (conj keys :cylinder_thermostat) "Cylinder Thermostat")
   (text-control profile owner (conj keys :controls_same_for_all_zones) "Controls Same For All Zones")])

(defn storey [owner profile keys]
  [:div
   (text-control profile owner (conj keys :storey_type) "Storey Type")
   (text-control profile owner (conj keys :storey) "Storey")
   (text-control profile owner (conj keys :heat_loss_w_per_k) "Heat Loss W Per K")
   (text-control profile owner (conj keys :heat_requirement_kwth_per_year) "Heat Requirement kWh Per Year")])

(defn wall [owner profile keys]
  [:div
   (text-control profile owner (conj keys :wall_type) "Wall Type")
   (text-control profile owner (conj keys :construction) "Construction")
   (text-control profile owner (conj keys :construction_other) "Construction Other")
   (text-control profile owner (conj keys :insulation) "Insulation")
   (text-control profile owner (conj keys :insulation_date) "Insulation Date")
   (text-control profile owner (conj keys :insulation_type) "Insulation Type")
   (text-control profile owner (conj keys :insulation_thickness) "Insulation Thickness")
   (text-control profile owner (conj keys :insulation_product) "Insulation Product")
   (text-control profile owner (conj keys :uvalue) "U Value")
   (text-control profile owner (conj keys :location) "Location")
   (text-control profile owner (conj keys :area) "Area")])

(defn roof [owner profile keys]
  [:div
   (text-control profile owner (conj keys :roof_type) "Roof Type")
   (text-control profile owner (conj keys :construction) "Construction")
   (text-control profile owner (conj keys :construction_other) "Construction Other")
   (text-control profile owner (conj keys :insulation_location_one) "Insulation Location One")
   (text-control profile owner (conj keys :insulation_location_one_other) "Insulation Location One Other")
   (text-control profile owner (conj keys :insulation_location_two) "Insulation Location Two")
   (text-control profile owner (conj keys :insulation_location_two_other) "Insulation Location Two Other")
   (text-control profile owner (conj keys :insulation_thickness_one) "Insulation Thickness One")
   (text-control profile owner (conj keys :insulation_thickness_one_other) "Insulation Thickness One Other")
   (text-control profile owner (conj keys :insulation_thickness_two) "Insulation Thickness Two")
   (text-control profile owner (conj keys :insulation_thickness_two_other) "Insulation Thickness Two Other")
   (text-control profile owner (conj keys :insulation_date) "Insulation Date")
   (text-control profile owner (conj keys :insulation_type) "Insulation Type")
   (text-control profile owner (conj keys :insulation_product) "Insulation Product")
   (text-control profile owner (conj keys :uvalue) "U Value")
   (text-control profile owner (conj keys :uvalue_derived) "U Value Derived")])

(defn window [owner profile keys]
  [:div
   (text-control profile owner (conj keys :window_type) "Window Type")
   (text-control profile owner (conj keys :frame_type) "Frame Type")
   (text-control profile owner (conj keys :frame_type_other) "Frame Type Other")
   (text-control profile owner (conj keys :percentage_glazing) "Percentage Glazing")
   (text-control profile owner (conj keys :area) "Area")
   (text-control profile owner (conj keys :location) "Location")
   (text-control profile owner (conj keys :uvalue) "Uvalue")])

(defn door [owner profile keys]
  [:div
   (text-control profile owner (conj keys :door_type) "Door Type")
   (text-control profile owner (conj keys :door_type_other) "Door Type Other")
   (text-control profile owner (conj keys :frame_type) "Frame Type")
   (text-control profile owner (conj keys :frame_type_other) "Frame Type Other")
   (text-control profile owner (conj keys :percentage_glazing) "Percentage Glazing")
   (text-control profile owner (conj keys :area) "Area")
   (text-control profile owner (conj keys :location) "Location")
   (text-control profile owner (conj keys :uvalue) "U Value")])

(defn floor [owner profile keys]
  [:div
   (text-control profile owner (conj keys :floor_type) "Floor Type")
   (text-control profile owner (conj keys :construction) "Construction")
   (text-control profile owner (conj keys :construction_other) "Construction Other")
   (text-control profile owner (conj keys :insulation_thickness_one) "Insulation Thickness One")
   (text-control profile owner (conj keys :insulation_thickness_two) "Insulation Thickness Two")
   (text-control profile owner (conj keys :insulation_type) "Insulation Type")
   (text-control profile owner (conj keys :insulation_product) "Insulation Product")
   (text-control profile owner (conj keys :uvalue) "U Value")
   (text-control profile owner (conj keys :uvalue_derived) "Uvalue Derived")])

(defn roof-room [owner profile keys]
  [:div
   (text-control profile owner (conj keys :location) "Location")
   (text-control profile owner (conj keys :age) "Age")
   (text-control profile owner (conj keys :insulation_placement) "Insulation Placement")
   (text-control profile owner (conj keys :insulation_thickness_one) "Insulation Thickness One")
   (text-control profile owner (conj keys :insulation_thickness_one_other) "Insulation Thickness One Other")
   (text-control profile owner (conj keys :insulation_thickness_two) "Insulation Thickness Two")
   (text-control profile owner (conj keys :insulation_thickness_two_other) "Insulation Thickness Two Other")
   (text-control profile owner (conj keys :insulation_date) "Insulation Date")
   (text-control profile owner (conj keys :insulation_type) "Insulation Type")
   (text-control profile owner (conj keys :insulation_product) "Insulation Product")
   (text-control profile owner (conj keys :uvalue) "U Value")
   (text-control profile owner (conj keys :uvalue_derived) "Uvalue Derived")])

(defn low-energy-lights [owner profile keys]
 [:div
  (text-control profile owner (conj keys :light_type) "Light Type")
  (text-control profile owner (conj keys :light_type_other) "Light Type Other")
  (text-control profile owner (conj keys :bed_index) "Bed Index")
  (text-control profile owner (conj keys :proportion) "Proportion")])

(defn ventilation-system [owner profile keys]
  [:div
   (text-control profile owner (conj keys :approach) "Approach")
   (text-control profile owner (conj keys :approach_other) "Approach Other")
   (text-control profile owner (conj keys :ventilation_type) "Ventilation Type")
   (text-control profile owner (conj keys :ventilation_type_other) "Ventilation Type Other")
   (text-control profile owner (conj keys :mechanical_with_heat_recovery) "Mechanical With Heat Recovery")
   (text-control profile owner (conj keys :manufacturer) "Manufacturer")
   (text-control profile owner (conj keys :ductwork_type) "Ductwork Type")
   (text-control profile owner (conj keys :ductwork_type_other) "Ductwork Type Other")
   (text-control profile owner (conj keys :controls) "Controls")
   (text-control profile owner (conj keys :controls_other) "Controls Other")
   (text-control profile owner (conj keys :manual_control_location) "Manual Control Location")
   (text-control profile owner (conj keys :operational_settings) "Operational Settings")
   (text-control profile owner (conj keys :operational_settings_other) "Operational Settings Other")
   (text-control profile owner (conj keys :installer) "Installer")
   (text-control profile owner (conj keys :installer_engineers_name) "Installer Engineers Name")
   (text-control profile owner (conj keys :installer_registration_number) "Installer Registration Number")
   (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
   (text-control profile owner (conj keys :total_installed_area) "Total Installed Area")])


(defn airflow-measurement [owner profile keys]
 [:div
  (text-control profile owner (conj keys :reference) "Reference")
  (text-control profile owner (conj keys :system) "System")
  (text-control profile owner (conj keys :inspector) "Inspector")
  (text-control profile owner (conj keys :inspector_engineers_name) "Inspector Engineers Name")
  (text-control profile owner (conj keys :inspector_registration_number) "Inspector Registration Number")
  (text-control profile owner (conj keys :inspection_date) "Inspection Date")
  (text-control profile owner (conj keys :measured_low) "Measured Low")
  (text-control profile owner (conj keys :design_low) "Design Low")
  (text-control profile owner (conj keys :measured_high) "Measured High")
  (text-control profile owner (conj keys :design_high) "Design High")])

(defn photovoltaic-panel [owner profile keys]
  [:div
   (text-control profile owner (conj keys :percentage_roof_covered) "Percentage Roof Covered")
   (text-control profile owner (conj keys :photovoltaic_type) "Photovoltaic Type")
   (text-control profile owner (conj keys :photovoltaic_type_other) "Photovoltaic Type Other")
   (text-control profile owner (conj keys :make_model) "Make Model")
   (text-control profile owner (conj keys :mcs_no) "MCS No")
   (text-control profile owner (conj keys :efficiency) "Efficiency")
   (text-control profile owner (conj keys :inverter_type) "Inverter Type")
   (text-control profile owner (conj keys :inverter_make_model) "Inverter Make Model")
   (text-control profile owner (conj keys :inverter_mcs_no) "Inverter MCS No")
   (text-control profile owner (conj keys :installer) "Installer")
   (text-control profile owner (conj keys :installer_mcs_no) "Installer MCS No")
   (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
   (text-control profile owner (conj keys :capacity) "Capacity")
   (text-control profile owner (conj keys :area) "Area")
   (text-control profile owner (conj keys :orientation) "Orientation")
   (text-control profile owner (conj keys :pitch) "Pitch")
   (text-control profile owner (conj keys :est_annual_generation) "Est Annual Generation")
   (text-control profile owner (conj keys :est_percentage_requirement_met) "Est Percentage Requirement Met")
   (text-control profile owner (conj keys :est_percentage_exported) "Est Percentage Exported")
   (text-control profile owner (conj keys :performance) "Performance")])

(defn solar-thermal-panel [owner profile keys]
 [:div
  (text-control profile owner (conj keys :solar_type) "Solar Type")
  (text-control profile owner (conj keys :solar_type_other) "Solar Type Other")
  (text-control profile owner (conj keys :make_model) "Make Model")
  (text-control profile owner (conj keys :mcs_no) "MCS No")
  (text-control profile owner (conj keys :installer) "Installer")
  (text-control profile owner (conj keys :installer_mcs_no) "Installer MCS No")
  (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
  (text-control profile owner (conj keys :capacity) "Capacity")
  (text-control profile owner (conj keys :area) "Area")
  (text-control profile owner (conj keys :orientation) "Orientation")
  (text-control profile owner (conj keys :pitch) "Pitch")
  (text-control profile owner (conj keys :est_annual_generation) "Est Annual Generation")
  (text-control profile owner (conj keys :est_percentage_requirement_met) "Est Percentage Requirement Met")])


(defn wind-turbine [owner profile keys]
  [:div
   (text-control profile owner (conj keys :turbine_type) "Turbine Type")
   (text-control profile owner (conj keys :turbine_type_other) "Turbine Type Other")
   (text-control profile owner (conj keys :make_model) "Make Model")
   (text-control profile owner (conj keys :mcs_no) "MCS No")
   (text-control profile owner (conj keys :inverter_type) "Inverter Type")
   (text-control profile owner (conj keys :inverter_make_model) "Inverter Make Model")
   (text-control profile owner (conj keys :inverter_mcs_no) "Inverter MCS No")
   (text-control profile owner (conj keys :installer) "Installer")
   (text-control profile owner (conj keys :installer_mcs_no) "Installer MCS No")
   (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
   (text-control profile owner (conj keys :capacity) "Capacity")
   (text-control profile owner (conj keys :hub_height) "Hub Height")
   (text-control profile owner (conj keys :height_above_canpoy) "Height Above Canpoy")
   (text-control profile owner (conj keys :wind_speed) "Wind Speed")
   (text-control profile owner (conj keys :wind_speed_info_source) "Wind Speed Info Source")
   (text-control profile owner (conj keys :wind_speed_info_source_other) "Wind Speed Info Source Other")
   (text-control profile owner (conj keys :est_annual_generation) "Est Annual Generation")
   (text-control profile owner (conj keys :est_percentage_requirement_met) "Est Percentage Requirement Met")
   (text-control profile owner (conj keys :est_percentage_exported) "Est Percentage Exported")])

(defn small-hydros-plant [owner profile keys]
  [:div
   (text-control profile owner (conj keys :hydro_type) "Hydro Type")
   (text-control profile owner (conj keys :make_model) "Make Model")
   (text-control profile owner (conj keys :mcs_no) "MCS No")
   (text-control profile owner (conj keys :inverter_type) "Inverter Type")
   (text-control profile owner (conj keys :inverter_make_model) "Inverter Make Model")
   (text-control profile owner (conj keys :inverter_mcs_no) "Inverter MCS No")
   (text-control profile owner (conj keys :installer) "Installer")
   (text-control profile owner (conj keys :installer_mcs_no) "Installer MCS No")
   (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
   (text-control profile owner (conj keys :capacity) "Capacity")
   (text-control profile owner (conj keys :head_drop) "Head Drop")
   (text-control profile owner (conj keys :design_flow) "Design Flow")
   (text-control profile owner (conj keys :est_annual_generation) "Est Annual Generation")
   (text-control profile owner (conj keys :est_percentage_requirement_met) "Est Percentage Requirement Met")
   (text-control profile owner (conj keys :est_percentage_exported) "Est Percentage Exported")])


(defn heat-pump [owner profile keys]
  [:div
   (text-control profile owner (conj keys :heat_pump_type) "Heat Pump Type")
   (text-control profile owner (conj keys :make_model) "Make Model")
   (text-control profile owner (conj keys :cop) "CoP")
   (text-control profile owner (conj keys :spf) "SPF")
   (text-control profile owner (conj keys :mcs_no) "MCS No")
   (text-control profile owner (conj keys :installer) "Installer")
   (text-control profile owner (conj keys :installer_mcs_no) "Installer MCS No")
   (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
   (text-control profile owner (conj keys :heat_source_type) "Heat Source Type")
   (text-control profile owner (conj keys :heat_source_type_other) "Heat Source Type Other")
   (text-control profile owner (conj keys :depth) "Depth")
   (text-control profile owner (conj keys :geology) "Geology")
   (text-control profile owner (conj keys :capacity) "Capacity")
   (text-control profile owner (conj keys :est_annual_generation) "Est Annual Generation")
   (text-control profile owner (conj keys :est_percentage_requirement_met) "Est Percentage Requirement Met")
   (text-control profile owner (conj keys :dhw) "DHW")
   (text-control profile owner (conj keys :est_percentage_dhw_requirement_met) "Est Percentage Dhw Requirement Met")])

(defn biomass-boiler [owner profile keys]
 [:div
  (text-control profile owner (conj keys :biomass_type) "Biomass Type")
  (text-control profile owner (conj keys :model) "Model")
  (text-control profile owner (conj keys :mcs_no) "MCS No")
  (text-control profile owner (conj keys :installer) "Installer")
  (text-control profile owner (conj keys :installer_mcs_no) "Installer MCS No")
  (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
  (text-control profile owner (conj keys :capacity) "Capacity")
  (text-control profile owner (conj keys :percentage_efficiency_from_spec) "Percentage Efficiency From Spec")
  (text-control profile owner (conj keys :est_annual_generation) "Est Annual Generation")
  (text-control profile owner (conj keys :est_percentage_requirement_met) "Est Percentage Requirement Met")])

(defn mCHP-system [owner profile keys]
  [:div
   (text-control profile owner (conj keys :chp_type) "CHP Type")
   (text-control profile owner (conj keys :model) "Model")
   (text-control profile owner (conj keys :mcs_no) "MCS No")
   (text-control profile owner (conj keys :installer) "Installer")
   (text-control profile owner (conj keys :installer_mcs_no) "Installer MCS No")
   (text-control profile owner (conj keys :commissioning_date) "Commissioning Date")
   (text-control profile owner (conj keys :capacity_elec) "Capacity Elec")
   (text-control profile owner (conj keys :capacity_thermal) "Capacity Thermal")
   (text-control profile owner (conj keys :est_annual_generation) "Est Annual Generation")
   (text-control profile owner (conj keys :est_percentage_thermal_requirement_met) "Est Percentage Thermal Requirement Met")
   (text-control profile owner (conj keys :est_percentage_exported) "Est Percentage Exported")])
