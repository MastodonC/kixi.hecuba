(ns kixi.hecuba.tabs.hierarchy.profiles-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [kixi.hecuba.tabs.hierarchy.profiles :as profiles]))

(deftest profile-section-list-test
  (testing
      (let [label-all "All Ventilation Systems"
            label "Ventilation System"
            profiles [{:thermal_images []
                       :profile_data {:number_of_storeys "3"
                                      :gross_internal_area "119.95"
                                      :used_passivehaus_principles "No"
                                      :air_tightness_rate "5"
                                      :total_volume "306.86"
                                      :space_heating_requirement "2807.94"
                                      :primary_energy_requirement "Not specified"
                                      :renewable_contribution_elec "2167.36"
                                      :controls_strategy "Gas boiler for heating & hot water, 2 heating zones, timer & thermostats, TRV's"
                                      :heat_storage_present "No"
                                      :modelling_software_methods_used "SAP"
                                      :best_u_value_for_walls "0.2"
                                      :co_heating_loss "No"
                                      :intention_ofpassvhaus "No"
                                      :orientation "North/South"
                                      :footprint "16.65"
                                      :sap_version_year "EES SAP 2005.018.03 Oct 2009"
                                      :annual_heating_load "3640"
                                      :renewable_contribution_heat "1206.71"
                                      :ter "21.48"
                                      :roof_rooms_present "No"
                                      :innovation_approaches "Unusual suspended steel bays to rear of property boosted glazing area but introduced design and construction challenges in terms of junction detailing"
                                      :sap_rating "85"
                                      :ber "16.07"
                                      :bedroom_count "4"
                                      :occupancy_total "7"
                                      :fabric_energy_efficiency "Not applicable"
                                      :external_perimeter "26.35"
                                      :heat_loss_parameter_hlp "1.1412"
                                      :event_type "As designed"
                                      :electricity_storage_present "No"
                                      :water_saving_strategy "Low flow taps, ECO bath"
                                      :habitable_rooms "5"
                                      :total_rooms "10"}
                       :editable true,
                       :small_hydros [],
                       :wind_turbines [],
                       :ventilation_systems [{:ductwork_type "Mixed"
                                              :operational_settings "Factory settings"
                                              :approach "Mechanical"
                                              :ventilation_type "MVHR"
                                              :controls "No time or thermostatic control of room temperature"
                                              :ventilation_type_other "ITHO HRU ECO4"}]
                       :walls [{:construction "Cavity"}]
                       :photovoltaics []
                       :roofs [{:construction "Pitched access to loft"
                                :uvalue "0.11"}]
                       :conservatories [],
                       :chps [],
                       :solar_thermals [],
                       :storeys [],
                       :heat_pumps [],
                       :airflow_measurements [],
                       :extensions [],
                       :low_energy_lights [{:light_type "CFL"}],
                       :heating_systems [{:fuel "Mains gas�"
                                          :boiler_type "Gas boilers"
                                          :heating_type "Boiler systems"
                                          :controls_make_and_model "Main 15 HEA" }],
                       :profile_id "c48e97801a9cf97a22515c86c0ccb75536e1f3c6"
                       :floors [{:construction "Solid"
                                 :uvalue 0.15}],
                       :entity_id "1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3"
                       :roof_rooms [],
                       :door_sets [{:uvalue "1.2"
                                    :door_type "Solid (< 30% glazing)" }]
                       :timestamp "2014-09-16T00:00:00.000Z"
                       :biomasses [],
                       :hot_water_systems [{:immersion "Single"
                                            :fuel "Mains gas"
                                            :dhw_type "From main heating system"
                                            :cylinder_capacity "210l" }],
                       :window_sets [{:percentage_glazing "12%"
                                      :frame_type "uPVC"
                                      :window_type "Double glazed"
                                      :area "29.98"
                                      :uvalue "1.4" }]}

                      {:thermal_images [],
                       :profile_data {:number_of_storeys "3"
                                      :gross_internal_area "119.95"
                                      :used_passivehaus_principles "No"
                                      :air_tightness_rate "7.78"
                                      :total_volume "306.86"
                                      :profile_noise "5"
                                      :profile_comfort "6.8"
                                      :profile_needs "5.8"
                                      :controls_strategy "The tenants found this system relatively easy to understand and this was reflected in theenergy use being only marginally higher than SAP predictions"
                                      :heat_storage_present "No"
                                      :modelling_software_methods_used "SAP"
                                      :best_u_value_for_walls "0.19"
                                      :profile_air_in_summer "6.6"
                                      :intention_ofpassvhaus "No"
                                      :profile_bus_report_url "http://portal.busmethodology.org.uk/Upload/Analysis/ffyv5cmc.s4j/index.html"
                                      :orientation "North/South"
                                      :footprint "16.65"
                                      :profile_temperature_in_summer "6"
                                      :profile_design "6.4"
                                      :roof_rooms_present "No"
                                      :innovation_approaches "Construction period extended by circa 8 weeks as a result of steel bays , differential movement with main structure was designed for and observed. and craning into place was required"
                                      :profile_temperature_in_winter "5.75"
                                      :bedroom_count "4"
                                      :occupancy_total "up to 6"
                                      :profile_air_in_winter "5.6"
                                      :external_perimeter "26.35"
                                      :event_type "As built"
                                      :profile_bus_summary_index "100"
                                      :electricity_storage_present "No"
                                      :water_saving_strategy "Not studied in detail"
                                      :habitable_rooms "5"
                                      :profile_lightning "4.4"
                                      :total_rooms "10"
                                      :profile_health "5.8" }
                       :editable true,
                       :small_hydros [],
                       :wind_turbines [],
                       :ventilation_systems [{:ductwork_type "Mixed"
                                              :operational_settings "Factory settings"
                                              :approach "Mechanical"
                                              :ventilation_type "MVHR"
                                              :controls "No time or thermostatic control of room temperature"
                                              :ventilation_type_other "ITHO HRU ECO4"}]
                       :walls [{:construction "Cavity"}],
                       :photovoltaics [],
                       :roofs [{:construction "Pitched access to loft"}],
                       :conservatories [],
                       :chps [],
                       :solar_thermals [],
                       :storeys [],
                       :heat_pumps [],
                       :airflow_measurements [],
                       :extensions [],
                       :low_energy_lights [{:light_type "CFL"}],
                       :heating_systems [{:fuel "Mains gas�"
                                          :boiler_type "Gas boilers"
                                          :heating_type "Boiler systems"
                                          :controls_make_and_model "Baxi megaflow HEA 15" }],
                       :profile_id "83325bd9189fbaa9b4b88cd2b205044da2ac52f2"
                       :floors [{:construction "Solid"}]
                       :entity_id "1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3"
                       :roof_rooms [],
                       :door_sets [{:door_type "Solid (< 30% glazing)" }]
                       :timestamp "2014-09-16T00:00:00.000Z"
                       :biomasses []
                       :hot_water_systems [{:immersion "Single"
                                            :fuel "Mains gas"
                                            :dhw_type "From main heating system"
                                            :cylinder_capacity "210l"}],
                       :window_sets [{:percentage_glazing "12%"
                                      :frame_type "uPVC"
                                      :window_type "Double glazed"
                                      :area "29.98"
                                      :uvalue "1.37" }]}]
            field  [:ventilation_systems]
            keys  [{:k :approach, :v "Approach"}
                   {:k :approach_other, :v "Approach Other"}
                   {:k :ventilation_type, :v "Ventilation Type"}
                   {:k :ventilation_type_other, :v "Ventilation Type Other"}
                   {:k :mechanical_with_heat_recovery, :v "Mechanical With Heat Recovery"}
                   {:k :manufacturer, :v "Manufacturer"}
                   {:k :ductwork_type, :v "Ductwork Type"}
                   {:k :ductwork_type_other, :v "Ductwork Type Other"}
                   {:k :controls, :v "Controls"}
                   {:k :controls_other, :v "Controls Other"}
                   {:k :manual_control_location, :v "Manual Control Location"}
                   {:k :operational_settings, :v "Operational Settings"}
                   {:k :operational_settings_other, :v "Operational Settings Other"}
                   {:k :installer, :v "Installer"}
                   {:k :installer_engineers_name, :v "Installer Engineers Name"}
                   {:k :installer_registration_number, :v "Installer Registration Number"}
                   {:k :commissioning_date, :v "Commissioning Date"}
                   {:k :total_installed_area, :v "Total Installed Area"}]

            expected '( [:tr.active
                         [:td {:col-span 3} [:h4 "All Ventilation Systems"]]]
                        [:tr
                         [:td {:col-span 3} [:h5.subheader "Ventilation System"]]]
                         [:tr
                          [:td "Approach"]
                          [:td "Mechanical"]
                          [:td "Mechanical"]]
                         [:tr
                          [:td "Ventilation Type"]
                          [:td "MVHR"]
                          [:td "MVHR"]]
                         [:tr
                          [:td "Ventilation Type Other"]
                          [:td "ITHO HRU ECO4"]
                          [:td "ITHO HRU ECO4"]]
                         [:tr
                          [:td "Ductwork Type"]
                          [:td "Mixed"]
                          [:td "Mixed"]]
                         [:tr
                          [:td "Controls"]
                          [:td "No time or thermostatic control of room temperature"]
                          [:td "No time or thermostatic control of room temperature"]]
                         [:tr
                          [:td "Operational Settings"]
                          [:td "Factory settings"]
                          [:td "Factory settings"]])]
        (is (= expected (profiles/profile-section-list label-all label profiles field keys))))))
