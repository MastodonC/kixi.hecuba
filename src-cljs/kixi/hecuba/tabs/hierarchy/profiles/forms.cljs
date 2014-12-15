(ns kixi.hecuba.tabs.hierarchy.profiles.forms
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [kixi.hecuba.common :as common :refer (log)]))

(def fields {:description [{:k :intervention_start_date :v "Intervention Start Date"}
                           {:k :intervention_completion_date :v "Intervention Completion Date"}
                           {:k :intervention_description :v "Intervention Description"}]

             :occupancy [{:k :occupancy_total :v "Total Occupancy"}
                         {:k :occupancy_under_18 :v "Occupancy Under 18"}
                         {:k :occupancy_18_to_60 :v "Occupancy 18 To 60"}
                         {:k :occupancy_over_60 :v "Occupancy Over 60"}
                         {:k :occupant_change :v "Occupant Change"}]

             :measurements [{:k :footprint :v "Footprint (internal groundfloor area m2)"}
                            {:k :external_perimeter :v "External Perimeter (m)"}
                            {:k :gross_internal_area :v "Gross Internal Area (m2)"}
                            {:k :number_of_storeys :v "Number Of Storeys"}
                            {:k :total_volume :v "Total Volume (m3)"}
                            {:k :total_rooms :v "Total Rooms"}
                            {:k :bedroom_count :v "Total Bedrooms"}
                            {:k :habitable_rooms :v "Habitable Rooms"}
                            {:k :inadequate_heating :v "Inadequate Heating?"}
                            {:k :heated_habitable_rooms :v "Heated Habitable Rooms"}]
             :energy [{:k :ber :v "BER (kgCO2/m2/yr)"}
                      {:k :ter :v "TER (kgCO2/m2/yr)"}
                      {:k :primary_energy_requirement :v "Total Primary Energy Requirement"}
                      {:k :space_heating_requirement :v "Space Heating Requirement"}
                      {:k :annual_space_heating_requirement :v "Annual Space Heating Requirement"}
                      {:k :renewable_contribution_heat :v "Renewable Contribution Heat"}
                      {:k :renewable_contribution_elec :v "Renewable Contribution Elec"}
                      {:k :electricity_meter_type :v "Electricity Meter Type"}
                      {:k :mains_gas :v "Linked to Mains Gas Supply"}
                      {:k :electricity_storage_present :v "Electricity Storage Present"}
                      {:k :heat_storage_present :v "Heat Storage Present"}]
             :passivhaus [{:k :passive_solar_strategy :v "Passive Solar Strategy"}
                          {:k :used_passivehaus_principles :v "Used Passivehaus Principles"}]
             :efficiency [{:k :pipe_lagging :v "Pipe Lagging"}
                          {:k :draught_proofing :v "Draught Proofing"}
                          {:k :draught_proofing_location :v "Draught Proofing Location"}]
             :flats [{:k :flat_floors_in_block :v "Floors In Block"}
                     {:k :flat_floor_position :v "Floor Position"}
                     {:k :flat_heat_loss_corridor :v "Heat Loss Corridor"}
                     {:k :flat_heat_loss_corridor_other :v "Heat Loss Corridor Other"}
                     {:k :flat_length_sheltered_wall :v "Length of Sheltered Wall (m)"}
                     {:k :flat_floor_heat_loss_type :v "Floor Heat Loss Type"}]
             :fireplaces [{:k :open_fireplaces :v "Number of Open Fireplaces"}
                          {:k :sealed_fireplaces :v "Number of Sealed Fireplaces"}]
             :glazing [{:k :glazing_area_glass_only :v "Glazing Area Glass Only"}
                       {:k :glazing_area_percentage :v "Glazing Area (% if known)"}
                       {:k :multiple_glazing_type :v "Multiple Glazing Type"}
                       {:k :multiple_glazing_area_percentage :v "Multiple Glazing Area (% if known)"}
                       {:k :multiple_glazing_u_value :v "Multiple Glazing U Value"}
                       {:k :multiple_glazing_type_other :v "Multiple Glazing Type Other"}
                       {:k :frame_type :v "Frame Type"}
                       {:k :frame_type_other :v "Frame Type Other"}]
             :issues [{:k :moisture_condensation_mould_strategy :v "Moisture Condensation Mould Strategy"}
                      {:k :appliances_strategy :v "Appliances Strategy"}
                      {:k :cellar_basement_issues :v "Cellar Basement Issues"}]
             :lessons-learnt [{:k :thermal_bridging_strategy :v "Thermal Bridging Strategy"}
                              {:k :airtightness_and_ventilation_strategy :v "Airtightness And Ventilation Strategy"}
                              {:k :overheating_cooling_strategy :v "Overheating Cooling Strategy"}
                              {:k :controls_strategy :v "Controls Strategy"}
                              {:k :lighting_strategy :v "Lighting Strategy"}
                              {:k :water_saving_strategy :v "Water Saving Strategy"}
                              {:k :innovation_approaches :v "Innovation Approaches"}]
             :project-details [{:k :total_budget_new_build :v "Total Budget New Build"}
                               {:k :estimated_cost_new_build :v "Estimated Cost New Build"}
                               {:k :final_cost_new_build :v "Final Cost New Build"}
                               {:k :construction_time_new_build :v "Construction Time New Build (Days)"}
                               {:k :design_guidance :v "Design Guidance"}
                               {:k :planning_considerations :v "Planning Considerations"}
                               {:k :total_budget :v "Total Budget"}]
             :coheating-test [{:k :co_heating_loss :v "Fabric Heat Loss (W/m2)"}
                              {:k :co_heating_performed_on :v "Dates Performed"}
                              {:k :co_heating_assessor :v "Assessor Used"}
                              {:k :co_heating_equipment :v "Equipment"}]
             :dwelling-summary [{:k :predominant_u_value_for_doors :v "Predominant U Value For Doors"}
                                {:k :predominant_u_value_for_floors :v "Predominant U Value For Floors"}
                                {:k :predominant_u_value_for_other :v "Predominant U Value For Other"}
                                {:k :predominant_u_value_for_roof :v "Predominant U Value For Roof"}
                                {:k :predominant_u_value_for_walls :v "Predominant U Value For Walls"}
                                {:k :predominant_u_value_for_windows :v "Predominant U Value For Windows"}
                                {:k :predominant_u_value_party_walls :v "Predominant U Value Party Walls"}
                                {:k :best_u_value_for_doors :v "Best U Value For Doors"}
                                {:k :best_u_value_for_floors :v "Best U Value For Floors"}
                                {:k :best_u_value_for_other :v "Best U Value For Other"}
                                {:k :best_u_value_for_roof :v "Best U Value For Roof"}
                                {:k :best_u_value_for_walls :v "Best U Value For Walls"}
                                {:k :best_u_value_for_windows :v "Best U Value For Windows"}
                                {:k :best_u_value_party_walls :v "Best U Value Party Walls"}
                                {:k :dwelling_u_value_other :v "Dwelling U Value Other"}]
             :air-tightness-test [{:k :air_tightness_assessor :v "Air Tightness Assessor"}
                                  {:k :air_tightness_equipment :v "Air Tightness Equipment"}
                                  {:k :air_tightness_performed_on :v "Air Tightness Performed On"}
                                  {:k :air_tightness_rate :v "Air Tightness Rate"}]
             :bus-survey [{:k :profile_temperature_in_summer :v "Temperature In Summer"}
                          {:k :profile_temperature_in_winter :v "Temperature In Winter"}
                          {:k :profile_air_in_summer :v "Air In Summer"}
                          {:k :profile_air_in_winter :v "Air In Winter"}
                          {:k :profile_lightning :v "Lightning"}
                          {:k :profile_noise :v "Noise"}
                          {:k :profile_comfort :v "Comfort"}
                          {:k :profile_design :v "Design"}
                          {:k :profile_needs :v "Needs"}
                          {:k :profile_health :v "Health (perceived)"}
                          {:k :profile_image_to_visitors :v "Image To Visitors"}
                          {:k :profile_productivity :v "Productivity"}
                          {:k :profile_bus_summary_index :v "Bus Summary Index"}
                          {:k :profile_bus_report_url :v "BUS Report Url"}]
             :sap-results [{:k :sap_rating :v "SAP Rating"}
                           {:k :sap_performed_on :v "SAP Performed On"}
                           {:k :sap_assessor :v "SAP Assessor"}
                           {:k :sap_version_issue :v "SAP Version Issue"}
                           {:k :sap_version_year :v "SAP Version Year"}
                           {:k :sap_regulations_date :v "SAP Regulations Date"}
                           {:k :sap_software :v "Name of SAP Software"}]
             :energy-costs [{:k :gas_cost :v "Gas Cost"}
                            {:k :electricity_cost :v "Electricity Cost"}]
             :conservatories [{:k :conservatory_type :v "Conservatory Type"}
                              {:k :area :v "Area"}
                              {:k :double_glazed :v  "Double Glazed"}
                              {:k :glazed_perimeter :v "Glazed Perimeter"}
                              {:k :height :v "Height"}]
             :extensions [{:k :age :v "Age"}
                          {:k :construction_date :v "Construction Date"}]
             :heating_systems [{:k :heating_type :v "Heating Type"}
                               {:k :heat_source :v "Heat Source"}
                               {:k :heat_transport :v "Heat Transport"}
                               {:k :heat_delivery :v "Heat Delivery"}
                               {:k :heat_delivery_source :v "Heat Delivery Source"}
                               {:k :efficiency_derivation :v "Efficiency Derivation"}
                               {:k :boiler_type :v "Boiler Type"}
                               {:k :boiler_type_other :v "Boiler Type Other"}
                               {:k :fan_flue :v "Fan Flue"}
                               {:k :open_flue :v "Open Flue"}
                               {:k :fuel :v "Fuel"}
                               {:k :heating_system :v "Heating System"}
                               {:k :heating_system_other :v "Heating System Other"}
                               {:k :heating_system_type :v "Heating System Type"}
                               {:k :heating_system_type_other :v "Heating System Type Other"}
                               {:k :heating_system_solid_fuel :v "Heating System Solid Fuel"}
                               {:k :heating_system_solid_fuel_other :v "Heating System Solid Fuel Other"}
                               {:k :bed_index :v "Bed Index"}
                               {:k :make_and_model :v "Make and Model"}
                               {:k :controls :v "Controls"}
                               {:k :controls_other :v "Controls Other"}
                               {:k :controls_make_and_model :v "Controls Make and Model"}
                               {:k :emitter :v "Emitter"}
                               {:k :trvs_on_emitters :v "TRVs on Emitters"}
                               {:k :use_hours_per_week :v "Use Hours per Week"}
                               {:k :installer :v "Installer"}
                               {:k :installer_engineers_name :v "Installer Engineers Name"}
                               {:k :installer_registration_number :v "Installer Registration Number"}
                               {:k :commissioning_date :v "Commissioning Date"}
                               {:k :inspector :v "Inspector"}
                               {:k :inspector_engineers_name :v "Inspector Engineers Name"}
                               {:k :inspector_registration_number :v "Inspector Registration Number "}
                               {:k :inspection_date :v "Inspection Date"}
                               {:k :efficiency :v "Efficiency"}]
             :hot_water_systems [{:k :dhw_type :v "DHW Type"}
                                 {:k :fuel :v "Fuel"}
                                 {:k :fuel_other :v "Fuel Other"}
                                 {:k :immersion :v "Immersion"}
                                 {:k :cylinder_capacity :v "Cylinder Capacity"}
                                 {:k :cylinder_capacity_other :v "Cylinder Capacity Other"}
                                 {:k :cylinder_insulation_type :v "Cylinder Insulation Type"}
                                 {:k :cylinder_insulation_type_other :v "Cylinder Insulation Type Other"}
                                 {:k :cylinder_insulation_thickness :v "Cylinder Insulation Thickness"}
                                 {:k :cylinder_insulation_thickness_other :v "Cylinder Insulation Thickness Other"}
                                 {:k :cylinder_thermostat :v "Cylinder Thermostat"}
                                 {:k :controls_same_for_all_zones :v "Controls Same For All Zones"}]
             :storeys [{:k :storey_type :v "Storey Type"}
                       {:k :storey :v "Storey"}
                       {:k :heat_loss_w_per_k :v "Heat Loss W Per K"}
                       {:k :heat_requirement_kwth_per_year :v "Heat Requirement kWh Per Year"}]
             :walls [{:k :wall_type :v "Wall Type"}
                     {:k :construction :v "Construction"}
                     {:k :construction_other :v "Construction Other"}
                     {:k :insulation :v "Insulation"}
                     {:k :insulation_date :v "Insulation Date"}
                     {:k :insulation_type :v "Insulation Type"}
                     {:k :insulation_thickness :v "Insulation Thickness"}
                     {:k :insulation_product :v "Insulation Product"}
                     {:k :uvalue :v "U Value"}
                     {:k :location :v "Location"}
                     {:k :area :v "Area"}]
             :roofs [{:k :roof_type :v "Roof Type"}
                     {:k :construction :v "Construction"}
                     {:k :construction_other :v "Construction Other"}
                     {:k :insulation_location_one :v "Insulation Location One"}
                     {:k :insulation_location_one_other :v "Insulation Location One Other"}
                     {:k :insulation_location_two :v "Insulation Location Two"}
                     {:k :insulation_location_two_other :v "Insulation Location Two Other"}
                     {:k :insulation_thickness_one :v "Insulation Thickness One"}
                     {:k :insulation_thickness_one_other :v "Insulation Thickness One Other"}
                     {:k :insulation_thickness_two :v "Insulation Thickness Two"}
                     {:k :insulation_thickness_two_other :v "Insulation Thickness Two Other"}
                     {:k :insulation_date :v "Insulation Date"}
                     {:k :insulation_type :v "Insulation Type"}
                     {:k :insulation_product :v "Insulation Product"}
                     {:k :uvalue :v "U Value"}
                     {:k :uvalue_derived :v "U Value Derived"}]
             :window_sets [{:k :window_type :v "Window Type"}
                           {:k :frame_type :v "Frame Type"}
                           {:k :frame_type_other :v "Frame Type Other"}
                           {:k :percentage_glazing :v "Percentage Glazing"}
                           {:k :area :v "Area"}
                           {:k :location :v "Location"}
                           {:k :uvalue :v "Uvalue"}]
             :door_sets [{:k :door_type :v "Door Type"}
                         {:k :door_type_other :v "Door Type Other"}
                         {:k :frame_type :v "Frame Type"}
                         {:k :frame_type_other :v "Frame Type Other"}
                         {:k :percentage_glazing :v "Percentage Glazing"}
                         {:k :area :v "Area"}
                         {:k :location :v "Location"}
                         {:k :uvalue :v "U Value"}]
             :floors [{:k :floor_type :v "Floor Type"}
                      {:k :construction :v "Construction"}
                      {:k :construction_other :v "Construction Other"}
                      {:k :insulation_thickness_one :v "Insulation Thickness One"}
                      {:k :insulation_thickness_two :v "Insulation Thickness Two"}
                      {:k :insulation_type :v "Insulation Type"}
                      {:k :insulation_product :v "Insulation Product"}
                      {:k :uvalue :v "U Value"}
                      {:k :uvalue_derived :v "Uvalue Derived"}]
             :roof_rooms [{:k :location :v "Location"}
                          {:k :age :v "Age"}
                          {:k :insulation_placement :v "Insulation Placement"}
                          {:k :insulation_thickness_one :v "Insulation Thickness One"}
                          {:k :insulation_thickness_one_other :v "Insulation Thickness One Other"}
                          {:k :insulation_thickness_two :v "Insulation Thickness Two"}
                          {:k :insulation_thickness_two_other :v "Insulation Thickness Two Other"}
                          {:k :insulation_date :v "Insulation Date"}
                          {:k :insulation_type :v "Insulation Type"}
                          {:k :insulation_product :v "Insulation Product"}
                          {:k :uvalue :v "U Value"}
                          {:k :uvalue_derived :v "Uvalue Derived"}]
             :low_energy_lights [{:k :light_type :v "Light Type"}
                                 {:k :light_type_other :v "Light Type Other"}
                                 {:k :bed_index :v "Bed Index"}
                                 {:k :proportion :v "Proportion"}]
             :ventilation_systems [{:k :approach :v "Approach"}
                                   {:k :approach_other :v "Approach Other"}
                                   {:k :ventilation_type :v "Ventilation Type"}
                                   {:k :ventilation_type_other :v "Ventilation Type Other"}
                                   {:k :mechanical_with_heat_recovery :v "Mechanical With Heat Recovery"}
                                   {:k :manufacturer :v "Manufacturer"}
                                   {:k :ductwork_type :v "Ductwork Type"}
                                   {:k :ductwork_type_other :v "Ductwork Type Other"}
                                   {:k :controls :v "Controls"}
                                   {:k :controls_other :v "Controls Other"}
                                   {:k :manual_control_location :v "Manual Control Location"}
                                   {:k :operational_settings :v "Operational Settings"}
                                   {:k :operational_settings_other :v "Operational Settings Other"}
                                   {:k :installer :v "Installer"}
                                   {:k :installer_engineers_name :v "Installer Engineers Name"}
                                   {:k :installer_registration_number :v "Installer Registration Number"}
                                   {:k :commissioning_date :v "Commissioning Date"}
                                   {:k :total_installed_area :v "Total Installed Area"}]
             :airflow_measurements [{:k :reference :v "Reference"}
                                    {:k :system :v "System"}
                                    {:k :inspector :v "Inspector"}
                                    {:k :inspector_engineers_name :v "Inspector Engineers Name"}
                                    {:k :inspector_registration_number :v "Inspector Registration Number"}
                                    {:k :inspection_date :v "Inspection Date"}
                                    {:k :measured_low :v "Measured Low"}
                                    {:k :design_low :v "Design Low"}
                                    {:k :measured_high :v "Measured High"}
                                    {:k :design_high :v "Design High"}]
             :photovoltaics [{:k :percentage_roof_covered :v "Percentage Roof Covered"}
                             {:k :photovoltaic_type :v "Photovoltaic Type"}
                             {:k :photovoltaic_type_other :v "Photovoltaic Type Other"}
                             {:k :make_model :v "Make Model"}
                             {:k :mcs_no :v "MCS No"}
                             {:k :efficiency :v "Efficiency"}
                             {:k :inverter_type :v "Inverter Type"}
                             {:k :inverter_make_model :v "Inverter Make Model"}
                             {:k :inverter_mcs_no :v "Inverter MCS No"}
                             {:k :installer :v "Installer"}
                             {:k :installer_mcs_no :v "Installer MCS No"}
                             {:k :commissioning_date :v "Commissioning Date"}
                             {:k :capacity :v "Capacity"}
                             {:k :area :v "Area"}
                             {:k :orientation :v "Orientation"}
                             {:k :pitch :v "Pitch"}
                             {:k :est_annual_generation :v "Est Annual Generation"}
                             {:k :est_percentage_requirement_met :v "Est Percentage Requirement Met"}
                             {:k :est_percentage_exported :v "Est Percentage Exported"}
                             {:k :performance :v "Performance"}]
             :solar_thermals [{:k :solar_type :v "Solar Type"}
                              {:k :solar_type_other :v "Solar Type Other"}
                              {:k :make_model :v "Make Model"}
                              {:k :mcs_no :v "MCS No"}
                              {:k :installer :v "Installer"}
                              {:k :installer_mcs_no :v "Installer MCS No"}
                              {:k :commissioning_date :v "Commissioning Date"}
                              {:k :capacity :v "Capacity"}
                              {:k :area :v "Area"}
                              {:k :orientation :v "Orientation"}
                              {:k :pitch :v "Pitch"}
                              {:k :est_annual_generation :v "Est Annual Generation"}
                              {:k :est_percentage_requirement_met :v "Est Percentage Requirement Met"}]
             :wind_turbines [{:k :turbine_type :v "Turbine Type"}
                             {:k :turbine_type_other :v "Turbine Type Other"}
                             {:k :make_model :v "Make Model"}
                             {:k :mcs_no :v "MCS No"}
                             {:k :inverter_type :v "Inverter Type"}
                             {:k :inverter_make_model :v "Inverter Make Model"}
                             {:k :inverter_mcs_no :v "Inverter MCS No"}
                             {:k :installer :v "Installer"}
                             {:k :installer_mcs_no :v "Installer MCS No"}
                             {:k :commissioning_date :v "Commissioning Date"}
                             {:k :capacity :v "Capacity"}
                             {:k :hub_height :v "Hub Height"}
                             {:k :height_above_canpoy :v "Height Above Canpoy"}
                             {:k :wind_speed :v "Wind Speed"}
                             {:k :wind_speed_info_source :v "Wind Speed Info Source"}
                             {:k :wind_speed_info_source_other :v "Wind Speed Info Source Other"}
                             {:k :est_annual_generation :v "Est Annual Generation"}
                             {:k :est_percentage_requirement_met :v "Est Percentage Requirement Met"}
                             {:k :est_percentage_exported :v "Est Percentage Exported"}]
             :small_hydros [{:k :hydro_type :v "Hydro Type"}
                            {:k :make_model :v "Make Model"}
                            {:k :mcs_no :v "MCS No"}
                            {:k :inverter_type :v "Inverter Type"}
                            {:k :inverter_make_model :v "Inverter Make Model"}
                            {:k :inverter_mcs_no :v "Inverter MCS No"}
                            {:k :installer :v "Installer"}
                            {:k :installer_mcs_no :v "Installer MCS No"}
                            {:k :commissioning_date :v "Commissioning Date"}
                            {:k :capacity :v "Capacity"}
                            {:k :head_drop :v "Head Drop"}
                            {:k :design_flow :v "Design Flow"}
                            {:k :est_annual_generation :v "Est Annual Generation"}
                            {:k :est_percentage_requirement_met :v "Est Percentage Requirement Met"}
                            {:k :est_percentage_exported :v "Est Percentage Exported"}]
             :heat_pumps [{:k :heat_pump_type :v "Heat Pump Type"}
                          {:k :make_model :v "Make Model"}
                          {:k :cop :v "CoP"}
                          {:k :spf :v "SPF"}
                          {:k :mcs_no :v "MCS No"}
                          {:k :installer :v "Installer"}
                          {:k :installer_mcs_no :v "Installer MCS No"}
                          {:k :commissioning_date :v "Commissioning Date"}
                          {:k :heat_source_type :v "Heat Source Type"}
                          {:k :heat_source_type_other :v "Heat Source Type Other"}
                          {:k :depth :v "Depth"}
                          {:k :geology :v "Geology"}
                          {:k :capacity :v "Capacity"}
                          {:k :est_annual_generation :v "Est Annual Generation"}
                          {:k :est_percentage_requirement_met :v "Est Percentage Requirement Met"}
                          {:k :dhw :v "DHW"}
                          {:k :est_percentage_dhw_requirement_met :v "Est Percentage Dhw Requirement Met"}]
             :biomasses [{:k :biomass_type :v "Biomass Type"}
                         {:k :model :v "Model"}
                         {:k :mcs_no :v "MCS No"}
                         {:k :installer :v "Installer"}
                         {:k :installer_mcs_no :v "Installer MCS No"}
                         {:k :commissioning_date :v "Commissioning Date"}
                         {:k :capacity :v "Capacity"}
                         {:k :percentage_efficiency_from_spec :v "Percentage Efficiency From Spec"}
                         {:k :est_annual_generation :v "Est Annual Generation"}
                         {:k :est_percentage_requirement_met :v "Est Percentage Requirement Met"}]
             :chps [{:k :chp_type :v "CHP Type"}
                    {:k :model :v "Model"}
                    {:k :mcs_no :v "MCS No"}
                    {:k :installer :v "Installer"}
                    {:k :installer_mcs_no :v "Installer MCS No"}
                    {:k :commissioning_date :v "Commissioning Date"}
                    {:k :capacity_elec :v "Capacity Elec"}
                    {:k :capacity_thermal :v "Capacity Thermal"}
                    {:k :est_annual_generation :v "Est Annual Generation"}
                    {:k :est_percentage_thermal_requirement_met :v "Est Percentage Thermal Requirement Met"}
                    {:k :est_percentage_exported :v "Est Percentage Exported"}]})

(defn format-time-inst [t format]
  (if (nil? t)
    "Undated"
    (let [date (tc/from-date t)]
      (tf/unparse (tf/formatter format) date))))
