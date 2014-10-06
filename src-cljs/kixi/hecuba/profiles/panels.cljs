(ns kixi.hecuba.profiles.panels
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.common :refer (log)]))

(defn text-control [cursor owner opts]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [label]} opts]
        (html
         [:div.form-group
          [:label.control-label {:for label} label]
          [:div
           [:input {:default-value (:_value cursor)
                    :class "form-control"
                    :type "text"
                    :on-change (fn [e] (let [value (.-value (.-target e))]
                                         (om/update! cursor :_value value)))
                    :id label}]]])))))

(defn description [cursor]
  [:div
   (om/build text-control (:intervention_start_date cursor) {:opts {:label "Intervention Start Date"}})
   (om/build text-control (:intervention_completion_date cursor) {:opts {:label "Intervention Completion Date"}})
   (om/build text-control (:intervention_description cursor) {:opts {:label "Intervention Description"}})])

(defn occupancy [cursor]
  [:div
   (om/build text-control (:occupancy_total cursor) {:opts {:label "Total Occupancy"}})
   (om/build text-control (:occupancy_under_18 cursor) {:opts {:label "Occupancy Under 18"}})
   (om/build text-control (:occupancy_18_to_60 cursor) {:opts {:label "Occupancy 18 To 60"}})
   (om/build text-control (:occupancy_over_60 cursor) {:opts {:label "Occupancy Over 60"}})
   (om/build text-control (:occupant_change cursor) {:opts {:label "Occupant Change"}})])

(defn measurements [cursor]
  [:div
   (om/build text-control (:footprint cursor) {:opts {:label "Footprint (internal groundfloor area m2)"}})
   (om/build text-control (:external_perimeter cursor) {:opts {:label "External Perimeter (m)"}})
   (om/build text-control (:gross_internal_area cursor) {:opts {:label "Gross Internal Area (m2)"}})
   (om/build text-control (:number_of_storeys cursor) {:opts {:label "Number Of Storeys"}})
   (om/build text-control (:total_volume cursor) {:opts {:label "Total Volume (m3)"}})
   (om/build text-control (:total_rooms cursor) {:opts {:label "Total Rooms"}})
   (om/build text-control (:bedroom_count cursor) {:opts {:label "Total Bedrooms"}})
   (om/build text-control (:habitable_rooms cursor) {:opts {:label "Habitable Rooms"}})
   (om/build text-control (:inadequate_heating cursor) {:opts {:label "Inadequate Heating?"}})
   (om/build text-control (:heated_habitable_rooms cursor) {:opts {:label "Heated Habitable Rooms"}})])

(defn energy [cursor]
  [:div
   (om/build text-control (:ber cursor) {:opts {:label "BER (kgCO2/m2/yr)"}})
   (om/build text-control (:ter cursor) {:opts {:label "TER (kgCO2/m2/yr)"}})
   (om/build text-control (:primary_energy_requirement cursor) {:opts {:label "Total Primary Energy Requirement"}})
   (om/build text-control (:space_heating_requirement cursor) {:opts {:label "Space Heating Requirement"}})
   (om/build text-control (:annual_space_heating_requirement cursor) {:opts {:label "Annual Space Heating Requirement"}})
   (om/build text-control (:renewable_contribution_heat cursor) {:opts {:label "Renewable Contribution Heat"}})
   (om/build text-control (:renewable_contribution_elec cursor) {:opts {:label "Renewable Contribution Elec"}})
   (om/build text-control (:electricity_meter_type cursor) {:opts {:label "Electricity Meter Type"}})
   (om/build text-control (:mains_gas cursor) {:opts {:label "Linked to Mains Gas Supply"}})
   (om/build text-control (:electricity_storage_present cursor) {:opts {:label "Electricity Storage Present"}})
   (om/build text-control (:heat_storage_present cursor) {:opts {:label "Heat Storage Present"}})])

(defn efficiency [cursor]
  [:div
   (om/build text-control (:pipe_lagging cursor) {:opts {:label "Pipe Lagging"}})
   (om/build text-control (:draught_proofing cursor) {:opts {:label "Draught Proofing"}})
   (om/build text-control (:draught_proofing_location cursor) {:opts {:label "Draught Proofing Location"}})])

(defn passivehaus [cursor]
  [:div
   (om/build text-control (:passive_solar_strategy cursor) {:opts {:label "Passive Solar Strategy"}})
   (om/build text-control (:used_passivehaus_principles cursor) {:opts {:label "Used Passivehaus Principles"}})])

(defn flats [cursor]
  [:div
   (om/build text-control (:flat_floors_in_block cursor) {:opts {:label "Floors In Block"}})
   (om/build text-control (:flat_floor_position cursor) {:opts {:label "Floor Position"}})
   (om/build text-control (:flat_heat_loss_corridor cursor) {:opts {:label "Heat Loss Corridor"}})
   (om/build text-control (:flat_heat_loss_corridor_other cursor) {:opts {:label "Heat Loss Corridor Other"}})
   (om/build text-control (:flat_length_sheltered_wall cursor) {:opts {:label "Length of Sheltered Wall (m)"}})
   (om/build text-control (:flat_floor_heat_loss_type cursor) {:opts {:label "Floor Heat Loss Type"}})])

(defn fireplaces [cursor]
  [:div
   (om/build text-control (:open_fireplaces cursor) {:opts {:label "Number of Open Fireplaces"}})
   (om/build text-control (:sealed_fireplaces cursor) {:opts {:label "Number of Sealed Fireplaces"}})])

(defn glazing [cursor]
  [:div
   (om/build text-control (:glazing_area_glass_only cursor) {:opts {:label "Glazing Area Glass Only"}})
   (om/build text-control (:glazing_area_percentage cursor) {:opts {:label "Glazing Area (% if known)"}})
   (om/build text-control (:multiple_glazing_type cursor) {:opts {:label "Multiple Glazing Type"}})
   (om/build text-control (:multiple_glazing_area_percentage cursor) {:opts {:label "Multiple Glazing Area (% if known)"}})
   (om/build text-control (:multiple_glazing_u_value cursor) {:opts {:label "Multiple Glazing U Value"}})
   (om/build text-control (:multiple_glazing_type_other cursor) {:opts {:label "Multiple Glazing Type Other"}})
   (om/build text-control (:frame_type cursor) {:opts {:label "Frame Type"}})
   (om/build text-control (:frame_type_other cursor) {:opts {:label "Frame Type Other"}})])

(defn issues [cursor]
  [:div
   (om/build text-control (:moisture_condensation_mould_strategy cursor) {:opts {:label "Moisture Condensation Mould Strategy"}})
   (om/build text-control (:appliances_strategy cursor) {:opts {:label "Appliances Strategy"}})
   (om/build text-control (:cellar_basement_issues cursor) {:opts {:label "Cellar Basement Issues"}})])

(defn sap-results [cursor]
  [:div
   (om/build text-control (:sap_rating cursor) {:opts {:label "SAP Rating"}})
   (om/build text-control (:sap_performed_on cursor) {:opts {:label "SAP Performed On"}})
   (om/build text-control (:sap_assessor cursor) {:opts {:label "SAP Assessor"}})
   (om/build text-control (:sap_version_issue cursor) {:opts {:label "SAP Version Issue"}})
   (om/build text-control (:sap_version_year cursor) {:opts {:label "SAP Version Year"}})
   (om/build text-control (:sap_regulations_date cursor) {:opts {:label "SAP Regulations Date"}})
   (om/build text-control (:sap_software cursor) {:opts {:label "Name of SAP Software"}})])

(defn lessons-learnt [cursor]
  [:div
   (om/build text-control (:thermal_bridging_strategy cursor) {:opts {:label "Thermal Bridging Strategy"}})
   (om/build text-control (:airtightness_and_ventilation_strategy cursor) {:opts {:label "Airtightness And Ventilation Strategy"}})
   (om/build text-control (:overheating_cooling_strategy cursor) {:opts {:label "Overheating Cooling Strategy"}})
   (om/build text-control (:controls_strategy cursor) {:opts {:label "Controls Strategy"}})
   (om/build text-control (:lighting_strategy cursor) {:opts {:label "Lighting Strategy"}})
   (om/build text-control (:water_saving_strategy cursor) {:opts {:label "Water Saving Strategy"}})
   (om/build text-control (:innovation_approaches cursor) {:opts {:label "Innovation Approaches"}})])

(defn dwelling-u-values-summary [cursor]
  [:div
   (om/build text-control (:best_u_value_for_doors cursor) {:opts {:label "Best U Value For Doors"}})
   (om/build text-control (:best_u_value_for_floors cursor) {:opts {:label "Best U Value For Floors"}})
   (om/build text-control (:best_u_value_for_other cursor) {:opts {:label "Best U Value For Other"}})
   (om/build text-control (:best_u_value_for_roof cursor) {:opts {:label "Best U Value For Roof"}})
   (om/build text-control (:best_u_value_for_walls cursor) {:opts {:label "Best U Value For Walls"}})
   (om/build text-control (:best_u_value_for_windows cursor) {:opts {:label "Best U Value For Windows"}})
   (om/build text-control (:best_u_value_party_walls cursor) {:opts {:label "Best U Value Party Walls"}})
   (om/build text-control (:dwelling_u_value_other cursor) {:opts {:label "Dwelling U Value Other"}})])

(defn air-tightness-test [cursor]
  [:div
   (om/build text-control (:air_tightness_assessor cursor) {:opts {:label "Air Tightness Assessor"}})
   (om/build text-control (:air_tightness_equipment cursor) {:opts {:label "Air Tightness Equipment"}})
   (om/build text-control (:air_tightness_performed_on cursor) {:opts {:label "Air Tightness Performed On"}})
   (om/build text-control (:air_tightness_rate cursor) {:opts {:label "Air Tightness Rate"}})])

(defn bus-survey-information [cursor]
  [:div
   (om/build text-control (:profile_temperature_in_summer cursor) {:opts {:label "Temperature In Summer"}})
   (om/build text-control (:profile_temperature_in_winter cursor) {:opts {:label "Temperature In Winter"}})
   (om/build text-control (:profile_air_in_summer cursor) {:opts {:label "Air In Summer"}})
   (om/build text-control (:profile_air_in_winter cursor) {:opts {:label "Air In Winter"}})
   (om/build text-control (:profile_lightning cursor) {:opts {:label "Lightning"}})
   (om/build text-control (:profile_noise cursor) {:opts {:label "Noise"}})
   (om/build text-control (:profile_comfort cursor) {:opts {:label "Comfort"}})
   (om/build text-control (:profile_design cursor) {:opts {:label "Design"}})
   (om/build text-control (:profile_needs cursor) {:opts {:label "Needs"}})
   (om/build text-control (:profile_health cursor) {:opts {:label "Health (perceived)"}})
   (om/build text-control (:profile_image_to_visitors cursor) {:opts {:label "Image To Visitors"}})
   (om/build text-control (:profile_productivity cursor) {:opts {:label "Productivity"}})
   (om/build text-control (:profile_bus_summary_index cursor) {:opts {:label "Bus Summary Index"}})
   (om/build text-control (:profile_bus_report_url cursor) {:opts {:label "BUS Report Url"}})])

(defn project-details [cursor]
  [:div
   (om/build text-control (:total_budget_new_build cursor) {:opts {:label "Total Budget New Build"}})
   (om/build text-control (:estimated_cost_new_build cursor) {:opts {:label "Estimated Cost New Build"}})
   (om/build text-control (:final_cost_new_build cursor) {:opts {:label "Final Cost New Build"}})
   (om/build text-control (:construction_time_new_build cursor) {:opts {:label "Construction Time New Build (Days)"}})
   (om/build text-control (:design_guidance cursor) {:opts {:label "Design Guidance"}})
   (om/build text-control (:planning_considerations cursor) {:opts {:label "Planning Considerations"}})
   (om/build text-control (:total_budget cursor) {:opts {:label "Total Budget"}})])

(defn coheating-test [cursor]
  [:div
   (om/build text-control (:co_heating_loss cursor) {:opts {:label "Fabric Heat Loss (W/m2)"}})
   (om/build text-control (:co_heating_performed_on cursor) {:opts {:label "Dates Performed"}})
   (om/build text-control (:co_heating_assessor cursor) {:opts {:label "Assessor Used"}})
   (om/build text-control (:co_heating_equipment cursor) {:opts {:label "Equipment"}})])

(defn energy-costs [cursor]
  [:div
   (om/build text-control (:gas_cost cursor) {:opts {:label "Gas Cost"}})
   (om/build text-control (:electricity_cost cursor) {:opts {:label "Electricity Cost"}})])

;;;;;;;;;;;;;;;;;;;;;;;;; Lists ;;;;;;;;;;;;;;;;;;;;

(defn conservatory [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:conservatory_type cursor) {:opts {:label "Conservatory Type"}})
     (om/build text-control (:area cursor) {:opts {:label "Area"}})
     (om/build text-control (:double_glazed cursor) {:opts {:label "Double Glazed"}})
     (om/build text-control (:glazed_perimeter cursor) {:opts {:label "Glazed Perimeter"}})
     (om/build text-control (:height cursor) {:opts {:label "Height"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn extension [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:age cursor) {:opts {:label "Age"}})
     (om/build text-control (:construction_date cursor){:opts {:label "Construction Date"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn heating-system [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:heating_type cursor) {:opts {:label "Heating Type"}})
     (om/build text-control (:heat_source cursor) {:opts {:label "Heat Source"}})
     (om/build text-control (:heat_transport cursor) {:opts {:label "Heat Transport"}})
     (om/build text-control (:heat_delivery cursor) {:opts {:label "Heat Delivery"}})
     (om/build text-control (:heat_delivery_source cursor) {:opts {:label "Heat Delivery Source"}})
     (om/build text-control (:efficiency_derivation cursor) {:opts {:label "Efficiency Derivation"}})
     (om/build text-control (:boiler_type cursor) {:opts {:label "Boiler Type"}})
     (om/build text-control (:boiler_type_other cursor) {:opts {:label "Boiler Type Other"}})
     (om/build text-control (:fan_flue cursor) {:opts {:label "Fan Flue"}})
     (om/build text-control (:open_flue cursor) {:opts {:label "Open Flue"}})
     (om/build text-control (:fuel cursor) {:opts {:label "Fuel"}})
     (om/build text-control (:heating_system cursor) {:opts {:label "Heating System"}})
     (om/build text-control (:heating_system_other cursor) {:opts {:label "Heating System Other"}})
     (om/build text-control (:heating_system_type cursor) {:opts {:label "Heating System Type"}})
     (om/build text-control (:heating_system_type_other cursor) {:opts {:label "Heating System Type Other"}})
     (om/build text-control (:heating_system_solid_fuel cursor) {:opts {:label "Heating System Solid Fuel"}})
     (om/build text-control (:heating_system_solid_fuel_other cursor) {:opts {:label "Heating System Solid Fuel Other"}})
     (om/build text-control (:bed_index cursor) {:opts {:label "Bed Index"}})
     (om/build text-control (:make_and_model cursor) {:opts {:label "Make and Model"}})
     (om/build text-control (:controls cursor) {:opts {:label "Controls"}})
     (om/build text-control (:controls_other cursor) {:opts {:label "Controls Other"}})
     (om/build text-control (:controls_make_and_model cursor) {:opts {:label "Controls Make and Model"}})
     (om/build text-control (:emitter cursor) {:opts {:label "Emitter"}})
     (om/build text-control (:trvs_on_emitters cursor) {:opts {:label "TRVs on Emitters"}})
     (om/build text-control (:use_hours_per_week cursor) {:opts {:label "Use Hours per Week"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_engineers_name cursor) {:opts {:label "Installer Engineers Name"}})
     (om/build text-control (:installer_registration_number cursor) {:opts {:label "Installer Registration Number"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:inspector cursor) {:opts {:label "Inspector"}})
     (om/build text-control (:inspector_engineers_name cursor) {:opts {:label "Inspector Engineers Name"}})
     (om/build text-control (:inspector_registration_number cursor) {:opts {:label "Inspector Registration Number "}})
     (om/build text-control (:inspection_date cursor) {:opts {:label "Inspection Date"}})
     (om/build text-control (:efficiency cursor) {:opts {:label "Efficiency"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn hot-water-system [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:dhw_type cursor) {:opts {:label "DHW Type"}})
     (om/build text-control (:fuel cursor) {:opts {:label "Fuel"}})
     (om/build text-control (:fuel_other cursor) {:opts {:label "Fuel Other"}})
     (om/build text-control (:immersion cursor) {:opts {:label "Immersion"}})
     (om/build text-control (:cylinder_capacity cursor) {:opts {:label "Cylinder Capacity"}})
     (om/build text-control (:cylinder_capacity_other cursor) {:opts {:label "Cylinder Capacity Other"}})
     (om/build text-control (:cylinder_insulation_type cursor) {:opts {:label "Cylinder Insulation Type"}})
     (om/build text-control (:cylinder_insulation_type_other cursor) {:opts {:label "Cylinder Insulation Type Other"}})
     (om/build text-control (:cylinder_insulation_thickness cursor) {:opts {:label "Cylinder Insulation Thickness"}})
     (om/build text-control (:cylinder_insulation_thickness_other cursor) {:opts {:label "Cylinder Insulation Thickness Other"}})
     (om/build text-control (:cylinder_thermostat cursor) {:opts {:label "Cylinder Thermostat"}})
     (om/build text-control (:controls_same_for_all_zones cursor) {:opts {:label "Controls Same For All Zones"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn storey [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:storey_type cursor) {:opts {:label "Storey Type"}})
     (om/build text-control (:storey cursor) {:opts {:label "Storey"}})
     (om/build text-control (:heat_loss_w_per_k cursor) {:opts {:label "Heat Loss W Per K"}})
     (om/build text-control (:heat_requirement_kwth_per_year cursor) {:opts {:label "Heat Requirement kWh Per Year"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn wall [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:wall_type cursor) {:opts {:label "Wall Type"}})
     (om/build text-control (:construction cursor) {:opts {:label "Construction"}})
     (om/build text-control (:construction_other cursor) {:opts {:label "Construction Other"}})
     (om/build text-control (:insulation cursor) {:opts {:label "Insulation"}})
     (om/build text-control (:insulation_date cursor) {:opts {:label "Insulation Date"}})
     (om/build text-control (:insulation_type cursor) {:opts {:label "Insulation Type"}})
     (om/build text-control (:insulation_thickness cursor) {:opts {:label "Insulation Thickness"}})
     (om/build text-control (:insulation_product cursor) {:opts {:label "Insulation Product"}})
     (om/build text-control (:uvalue cursor) {:opts {:label "U Value"}})
     (om/build text-control (:location cursor) {:opts {:label "Location"}})
     (om/build text-control (:area cursor) {:opts {:label "Area"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn roof [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:roof_type cursor) {:opts {:label "Roof Type"}})
     (om/build text-control (:construction cursor) {:opts {:label "Construction"}})
     (om/build text-control (:construction_other cursor) {:opts {:label "Construction Other"}})
     (om/build text-control (:insulation_location_one cursor) {:opts {:label "Insulation Location One"}})
     (om/build text-control (:insulation_location_one_other cursor) {:opts {:label "Insulation Location One Other"}})
     (om/build text-control (:insulation_location_two cursor) {:opts {:label "Insulation Location Two"}})
     (om/build text-control (:insulation_location_two_other cursor) {:opts {:label "Insulation Location Two Other"}})
     (om/build text-control (:insulation_thickness_one cursor) {:opts {:label "Insulation Thickness One"}})
     (om/build text-control (:insulation_thickness_one_other cursor) {:opts {:label "Insulation Thickness One Other"}})
     (om/build text-control (:insulation_thickness_two cursor) {:opts {:label "Insulation Thickness Two"}})
     (om/build text-control (:insulation_thickness_two_other cursor) {:opts {:label "Insulation Thickness Two Other"}})
     (om/build text-control (:insulation_date cursor) {:opts {:label "Insulation Date"}})
     (om/build text-control (:insulation_type cursor) {:opts {:label "Insulation Type"}})
     (om/build text-control (:insulation_product cursor) {:opts {:label "Insulation Product"}})
     (om/build text-control (:uvalue cursor) {:opts {:label "U Value"}})
     (om/build text-control (:uvalue_derived cursor) {:opts {:label "U Value Derived"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn window [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:window_type cursor) {:opts {:label "Window Type"}})
     (om/build text-control (:frame_type cursor) {:opts {:label "Frame Type"}})
     (om/build text-control (:frame_type_other cursor) {:opts {:label "Frame Type Other"}})
     (om/build text-control (:percentage_glazing cursor) {:opts {:label "Percentage Glazing"}})
     (om/build text-control (:area cursor) {:opts {:label "Area"}})
     (om/build text-control (:location cursor) {:opts {:label "Location"}})
     (om/build text-control (:uvalue cursor) {:opts {:label "Uvalue"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn door [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:door_type cursor) {:opts {:label "Door Type"}})
     (om/build text-control (:door_type_other cursor) {:opts {:label "Door Type Other"}})
     (om/build text-control (:frame_type cursor) {:opts {:label "Frame Type"}})
     (om/build text-control (:frame_type_other cursor) {:opts {:label "Frame Type Other"}})
     (om/build text-control (:percentage_glazing cursor) {:opts {:label "Percentage Glazing"}})
     (om/build text-control (:area cursor) {:opts {:label "Area"}})
     (om/build text-control (:location cursor) {:opts {:label "Location"}})
     (om/build text-control (:uvalue cursor) {:opts {:label "U Value"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn floor [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:floor_type cursor) {:opts {:label "Floor Type"}})
     (om/build text-control (:construction cursor) {:opts {:label "Construction"}})
     (om/build text-control (:construction_other cursor) {:opts {:label "Construction Other"}})
     (om/build text-control (:insulation_thickness_one cursor) {:opts {:label "Insulation Thickness One"}})
     (om/build text-control (:insulation_thickness_two cursor) {:opts {:label "Insulation Thickness Two"}})
     (om/build text-control (:insulation_type cursor) {:opts {:label "Insulation Type"}})
     (om/build text-control (:insulation_product cursor) {:opts {:label "Insulation Product"}})
     (om/build text-control (:uvalue cursor) {:opts {:label "U Value"}})
     (om/build text-control (:uvalue_derived cursor) {:opts {:label "Uvalue Derived"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn roof-room [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:location cursor) {:opts {:label "Location"}})
     (om/build text-control (:age cursor) {:opts {:label "Age"}})
     (om/build text-control (:insulation_placement cursor) {:opts {:label "Insulation Placement"}})
     (om/build text-control (:insulation_thickness_one cursor) {:opts {:label "Insulation Thickness One"}})
     (om/build text-control (:insulation_thickness_one_other cursor) {:opts {:label "Insulation Thickness One Other"}})
     (om/build text-control (:insulation_thickness_two cursor) {:opts {:label "Insulation Thickness Two"}})
     (om/build text-control (:insulation_thickness_two_other cursor) {:opts {:label "Insulation Thickness Two Other"}})
     (om/build text-control (:insulation_date cursor) {:opts {:label "Insulation Date"}})
     (om/build text-control (:insulation_type cursor) {:opts {:label "Insulation Type"}})
     (om/build text-control (:insulation_product cursor) {:opts {:label "Insulation Product"}})
     (om/build text-control (:uvalue cursor) {:opts {:label "U Value"}})
     (om/build text-control (:uvalue_derived cursor) {:opts {:label "Uvalue Derived"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn low-energy-lights [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:light_type cursor) {:opts {:label "Light Type"}})
     (om/build text-control (:light_type_other cursor) {:opts {:label "Light Type Other"}})
     (om/build text-control (:bed_index cursor) {:opts {:label "Bed Index"}})
     (om/build text-control (:proportion cursor) {:opts {:label "Proportion"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn ventilation-system [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:approach cursor) {:opts {:label "Approach"}})
     (om/build text-control (:approach_other cursor) {:opts {:label "Approach Other"}})
     (om/build text-control (:ventilation_type cursor) {:opts {:label "Ventilation Type"}})
     (om/build text-control (:ventilation_type_other cursor) {:opts {:label "Ventilation Type Other"}})
     (om/build text-control (:mechanical_with_heat_recovery cursor) {:opts {:label "Mechanical With Heat Recovery"}})
     (om/build text-control (:manufacturer cursor) {:opts {:label "Manufacturer"}})
     (om/build text-control (:ductwork_type cursor) {:opts {:label "Ductwork Type"}})
     (om/build text-control (:ductwork_type_other cursor) {:opts {:label "Ductwork Type Other"}})
     (om/build text-control (:controls cursor) {:opts {:label "Controls"}})
     (om/build text-control (:controls_other cursor) {:opts {:label "Controls Other"}})
     (om/build text-control (:manual_control_location cursor) {:opts {:label "Manual Control Location"}})
     (om/build text-control (:operational_settings cursor) {:opts {:label "Operational Settings"}})
     (om/build text-control (:operational_settings_other cursor) {:opts {:label "Operational Settings Other"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_engineers_name cursor) {:opts {:label "Installer Engineers Name"}})
     (om/build text-control (:installer_registration_number cursor) {:opts {:label "Installer Registration Number"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:total_installed_area cursor) {:opts {:label "Total Installed Area"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn airflow-measurement [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:reference cursor) {:opts {:label "Reference"}})
     (om/build text-control (:system cursor) {:opts {:label "System"}})
     (om/build text-control (:inspector cursor) {:opts {:label "Inspector"}})
     (om/build text-control (:inspector_engineers_name cursor) {:opts {:label "Inspector Engineers Name"}})
     (om/build text-control (:inspector_registration_number cursor) {:opts {:label "Inspector Registration Number"}})
     (om/build text-control (:inspection_date cursor) {:opts {:label "Inspection Date"}})
     (om/build text-control (:measured_low cursor) {:opts {:label "Measured Low"}})
     (om/build text-control (:design_low cursor) {:opts {:label "Design Low"}})
     (om/build text-control (:measured_high cursor) {:opts {:label "Measured High"}})
     (om/build text-control (:design_high cursor) {:opts {:label "Design High"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn photovoltaic-panel [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:percentage_roof_covered cursor) {:opts {:label "Percentage Roof Covered"}})
     (om/build text-control (:photovoltaic_type cursor) {:opts {:label "Photovoltaic Type"}})
     (om/build text-control (:photovoltaic_type_other cursor) {:opts {:label "Photovoltaic Type Other"}})
     (om/build text-control (:make_model cursor) {:opts {:label "Make Model"}})
     (om/build text-control (:mcs_no cursor) {:opts {:label "MCS No"}})
     (om/build text-control (:efficiency cursor) {:opts {:label "Efficiency"}})
     (om/build text-control (:inverter_type cursor) {:opts {:label "Inverter Type"}})
     (om/build text-control (:inverter_make_model cursor) {:opts {:label "Inverter Make Model"}})
     (om/build text-control (:inverter_mcs_no cursor) {:opts {:label "Inverter MCS No"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_mcs_no cursor) {:opts {:label "Installer MCS No"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:capacity cursor) {:opts {:label "Capacity"}})
     (om/build text-control (:area cursor) {:opts {:label "Area"}})
     (om/build text-control (:orientation cursor) {:opts {:label "Orientation"}})
     (om/build text-control (:pitch cursor) {:opts {:label "Pitch"}})
     (om/build text-control (:est_annual_generation cursor) {:opts {:label "Est Annual Generation"}})
     (om/build text-control (:est_percentage_requirement_met cursor) {:opts {:label "Est Percentage Requirement Met"}})
     (om/build text-control (:est_percentage_exported cursor) {:opts {:label "Est Percentage Exported"}})
     (om/build text-control (:performance cursor) {:opts {:label "Performance"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn solar-thermal-panel [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:solar_type cursor) {:opts {:label "Solar Type"}})
     (om/build text-control (:solar_type_other cursor) {:opts {:label "Solar Type Other"}})
     (om/build text-control (:make_model cursor) {:opts {:label "Make Model"}})
     (om/build text-control (:mcs_no cursor) {:opts {:label "MCS No"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_mcs_no cursor) {:opts {:label "Installer MCS No"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:capacity cursor) {:opts {:label "Capacity"}})
     (om/build text-control (:area cursor) {:opts {:label "Area"}})
     (om/build text-control (:orientation cursor) {:opts {:label "Orientation"}})
     (om/build text-control (:pitch cursor) {:opts {:label "Pitch"}})
     (om/build text-control (:est_annual_generation cursor) {:opts {:label "Est Annual Generation"}})
     (om/build text-control (:est_percentage_requirement_met cursor) {:opts {:label "Est Percentage Requirement Met"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn wind-turbine [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:turbine_type cursor) {:opts {:label "Turbine Type"}})
     (om/build text-control (:turbine_type_other cursor) {:opts {:label "Turbine Type Other"}})
     (om/build text-control (:make_model cursor) {:opts {:label "Make Model"}})
     (om/build text-control (:mcs_no cursor) {:opts {:label "MCS No"}})
     (om/build text-control (:inverter_type cursor) {:opts {:label "Inverter Type"}})
     (om/build text-control (:inverter_make_model cursor) {:opts {:label "Inverter Make Model"}})
     (om/build text-control (:inverter_mcs_no cursor) {:opts {:label "Inverter MCS No"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_mcs_no cursor) {:opts {:label "Installer MCS No"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:capacity cursor) {:opts {:label "Capacity"}})
     (om/build text-control (:hub_height cursor) {:opts {:label "Hub Height"}})
     (om/build text-control (:height_above_canpoy cursor) {:opts {:label "Height Above Canpoy"}})
     (om/build text-control (:wind_speed cursor) {:opts {:label "Wind Speed"}})
     (om/build text-control (:wind_speed_info_source cursor) {:opts {:label "Wind Speed Info Source"}})
     (om/build text-control (:wind_speed_info_source_other cursor) {:opts {:label "Wind Speed Info Source Other"}})
     (om/build text-control (:est_annual_generation cursor) {:opts {:label "Est Annual Generation"}})
     (om/build text-control (:est_percentage_requirement_met cursor) {:opts {:label "Est Percentage Requirement Met"}})
     (om/build text-control (:est_percentage_exported cursor) {:opts {:label "Est Percentage Exported"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn small-hydros-plant [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:hydro_type cursor) {:opts {:label "Hydro Type"}})
     (om/build text-control (:make_model cursor) {:opts {:label "Make Model"}})
     (om/build text-control (:mcs_no cursor) {:opts {:label "MCS No"}})
     (om/build text-control (:inverter_type cursor) {:opts {:label "Inverter Type"}})
     (om/build text-control (:inverter_make_model cursor) {:opts {:label "Inverter Make Model"}})
     (om/build text-control (:inverter_mcs_no cursor) {:opts {:label "Inverter MCS No"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_mcs_no cursor) {:opts {:label "Installer MCS No"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:capacity cursor) {:opts {:label "Capacity"}})
     (om/build text-control (:head_drop cursor) {:opts {:label "Head Drop"}})
     (om/build text-control (:design_flow cursor) {:opts {:label "Design Flow"}})
     (om/build text-control (:est_annual_generation cursor) {:opts {:label "Est Annual Generation"}})
     (om/build text-control (:est_percentage_requirement_met cursor) {:opts {:label "Est Percentage Requirement Met"}})
     (om/build text-control (:est_percentage_exported cursor) {:opts {:label "Est Percentage Exported"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn heat-pump [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:heat_pump_type cursor) {:opts {:label "Heat Pump Type"}})
     (om/build text-control (:make_model cursor) {:opts {:label "Make Model"}})
     (om/build text-control (:cop cursor) {:opts {:label "CoP"}})
     (om/build text-control (:spf cursor) {:opts {:label "SPF"}})
     (om/build text-control (:mcs_no cursor) {:opts {:label "MCS No"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_mcs_no cursor) {:opts {:label "Installer MCS No"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:heat_source_type cursor) {:opts {:label "Heat Source Type"}})
     (om/build text-control (:heat_source_type_other cursor) {:opts {:label "Heat Source Type Other"}})
     (om/build text-control (:depth cursor) {:opts {:label "Depth"}})
     (om/build text-control (:geology cursor) {:opts {:label "Geology"}})
     (om/build text-control (:capacity cursor) {:opts {:label "Capacity"}})
     (om/build text-control (:est_annual_generation cursor) {:opts {:label "Est Annual Generation"}})
     (om/build text-control (:est_percentage_requirement_met cursor) {:opts {:label "Est Percentage Requirement Met"}})
     (om/build text-control (:dhw cursor) {:opts {:label "DHW"}})
     (om/build text-control (:est_percentage_dhw_requirement_met cursor) {:opts {:label "Est Percentage Dhw Requirement Met"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn biomass-boiler [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:biomass_type cursor) {:opts {:label "Biomass Type"}})
     (om/build text-control (:model cursor) {:opts {:label "Model"}})
     (om/build text-control (:mcs_no cursor) {:opts {:label "MCS No"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_mcs_no cursor) {:opts {:label "Installer MCS No"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:capacity cursor) {:opts {:label "Capacity"}})
     (om/build text-control (:percentage_efficiency_from_spec cursor) {:opts {:label "Percentage Efficiency From Spec"}})
     (om/build text-control (:est_annual_generation cursor) {:opts {:label "Est Annual Generation"}})
     (om/build text-control (:est_percentage_requirement_met cursor) {:opts {:label "Est Percentage Requirement Met"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))

(defn mCHP-system [cursors]
  (for [cursor cursors]
    [:div
     (om/build text-control (:chp_type cursor) {:opts {:label "CHP Type"}})
     (om/build text-control (:model cursor) {:opts {:label "Model"}})
     (om/build text-control (:mcs_no cursor) {:opts {:label "MCS No"}})
     (om/build text-control (:installer cursor) {:opts {:label "Installer"}})
     (om/build text-control (:installer_mcs_no cursor) {:opts {:label "Installer MCS No"}})
     (om/build text-control (:commissioning_date cursor) {:opts {:label "Commissioning Date"}})
     (om/build text-control (:capacity_elec cursor) {:opts {:label "Capacity Elec"}})
     (om/build text-control (:capacity_thermal cursor) {:opts {:label "Capacity Thermal"}})
     (om/build text-control (:est_annual_generation cursor) {:opts {:label "Est Annual Generation"}})
     (om/build text-control (:est_percentage_thermal_requirement_met cursor) {:opts {:label "Est Percentage Thermal Requirement Met"}})
     (om/build text-control (:est_percentage_exported cursor) {:opts {:label "Est Percentage Exported"}})
     (when (> (count cursors) 1) [:div.col-md-12 [:hr]])]))
