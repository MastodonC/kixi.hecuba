(ns kixi.hecuba.tabs.profiles
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [kixi.hecuba.bootstrap :as bs]))

(enable-console-print!)

(defn get-profiles [selected-property-id data]
  (->>  data
        :properties
        :data
        (filter #(= (:id %) selected-property-id))
        first
        :profiles))

(defn format-time-inst [t format]
  (if (nil? t)
    "Undated"
    (let [date (tc/from-date t)]
      (tf/unparse (tf/formatter format) date))))

(defn displayable? [d]
  (and d
       (not (re-find #"\w" d))))

(defn display [m k]
  (if-let [d (displayable? (get m k))]
    d
    "N/A"))

(defn profile-column-width []
  "col-md-3")

(defn header-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [category (get-in profile [:profile_data :category] "Intervention")
                timestamp (format-time-inst  (:timestamp profile) "yyyy-MM-dd")]
            [:div {:class (profile-column-width)}
             [:h3.text-center category [:br ] [:small timestamp]]]))]))))

(defn occupancy-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (println "Number of profiles: " (count profiles))
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Occupancy"
              [:dl
               [:dt "Occupancy Under 18"] [:dd (display profile_data :occupancy_under_18)]
               [:dt "Occupancy 18 To 60"] [:dd (display profile_data :occupancy_18_to_60)]
               [:dt "Occupancy Over 60"] [:dd (display profile_data :occupancy_over_60)]
               [:dt "Occupant Change"] [:dd (display profile_data :occupant_change)]])]))]))))

(defn profile-rows [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (om/build header-row profiles)
        (om/build occupancy-row profiles)
        ;; (om/build measurements profiles)
        ;; (om/build energy profiles)
        ;; (om/build efficiency profiles)
        ;; (om/build flats profiles)
        ;; (om/build firepalces profiles)
        ;; (om/build glazing profiles)
        ;; (om/build issues profiles)
        ;; (om/build sap-results profiles)
        ;; (om/build documents profiles)
        ;; (om/build co-heating profiles)
        ;; (om/build air-tightness profiles)
        ;; (om/build conservatories profiles)
        ;; (om/build extensions profiles)
        ;; (om/build heating-systems profiles)
        ;; (om/build hot-water-systems profiles)
        ;; (om/build storeys profiles)
        ;; (om/build walls profiles)
        ;; (om/build roofs profiles)
        ;; (om/build window-types profiles)
        ;; (om/build door-types profiles)
        ;; (om/build floors profiles)
        ;; (om/build roof-rooms profiles)
        ;; (om/build low-energy-lights profiles)
        ;; (om/build ventilation-systems profiles)
        ;; (om/build photovoltaic-panels profiles)
        ;; (om/build solar-thermal-panels profiles)
        ;; (om/build wind-turbines profiles)
        ;; (om/build small-hydro-plants profiles)
        ;; (om/build heat-pumps profiles)
        ;; (om/build biomass-boilers profiles)
        ;; (om/build mCHP-systems profiles)
        ])))
  
  )

(defn profiles-div [data owner]
  (reify
    om/IRender
    (render [_]
      (let [selected-property-id (-> data :active-components :properties)
            profiles             (sort-by :timestamp (get-profiles selected-property-id data))]
        (html
         [:div
          [:h3 "Profiles"]
          (if (seq profiles)
            (om/build profile-rows profiles)
            [:div.col-md-12.text-center
             [:p.lead {:style {:padding-top 30}}
              "No profile data to display"]])])))))


(comment
  [:div.col-md-12
     [:dl
      [:dt "Number Of Storeys"] [:dd (get profile_data :number_of_storeys "")]
      [:dt "Air Tightness Equipment"] [:dd (get profile_data :air_tightness_equipment "")]
      [:dt "Gross Internal Area"] [:dd (get profile_data :gross_internal_area "")]
      [:dt "Category"] [:dd (get profile_data :category "")]
      [:dt "SAP Performed On"] [:dd (get profile_data :sap_performed_on "")]
      [:dt "Used Passivehaus Principles"] [:dd (get profile_data :used_passivehaus_principles "")]
      [:dt "SAP Version Issue"] [:dd (get profile_data :sap_version_issue "")]
      [:dt "Air Tightness Rate"] [:dd (get profile_data :air_tightness_rate "")]
      [:dt "Multiple Glazing Type"] [:dd (get profile_data :multiple_glazing_type "")]
      [:dt "Total Volume"] [:dd (get profile_data :total_volume "")]
      [:dt "Appliances Strategy"] [:dd (get profile_data :appliances_strategy "")]
      [:dt "Profile Noise"] [:dd (get profile_data :profile_noise "")]
      [:dt "Space Heating Requirement"] [:dd (get profile_data :space_heating_requirement "")]
      [:dt "Flat Heat Loss Corridor"] [:dd (get profile_data :flat_heat_loss_corridor "")]
      [:dt "Primary Energy Requirement"] [:dd (get profile_data :primary_energy_requirement "")]
      [:dt "Passive Solar Strategy"] [:dd (get profile_data :passive_solar_strategy "")]
      [:dt "Profile Comfort"] [:dd (get profile_data :profile_comfort "")]
      [:dt "Annual Space Heating Requirement"] [:dd (get profile_data :annual_space_heating_requirement "")]
      [:dt "Onsite Days"] [:dd (get profile_data :onsite_days "")]
      [:dt "Completeness"] [:dd (get profile_data :completeness "")]
      [:dt "Renewable Contribution Elec"] [:dd (get profile_data :renewable_contribution_elec "")]
      [:dt "Best U Value For Floors"] [:dd (get profile_data :best_u_value_for_floors "")]
      [:dt "Profile Needs"] [:dd (get profile_data :profile_needs "")]
      [:dt "Open Fireplaces"] [:dd (get profile_data :open_fireplaces "")]
      [:dt "SAP Assessor"] [:dd (get profile_data :sap_assessor "")]
      [:dt "Occupant Change"] [:dd (get profile_data :occupant_change "")]
      [:dt "Controls Strategy"] [:dd (get profile_data :controls_strategy "")]
      [:dt "Property Id"] [:dd (get profile_data :property_id "")]
      [:dt "Heat Storage Present"] [:dd (get profile_data :heat_storage_present "")]
      [:dt "SAP Regulations Date"] [:dd (get profile_data :sap_regulations_date "")]
      [:dt "Best U Value For Windows"] [:dd (get profile_data :best_u_value_for_windows "")]
      [:dt "Best U Value For Roof"] [:dd (get profile_data :best_u_value_for_roof "")]
      [:dt "Co Heating Assessor"] [:dd (get profile_data :co_heating_assessor "")]
      [:dt "Modelling Software Methods Used"] [:dd (get profile_data :modelling_software_methods_used "")]
      [:dt "Best U Value For Walls"] [:dd (get profile_data :best_u_value_for_walls "")]
      [:dt "Profile Air In Summer"] [:dd (get profile_data :profile_air_in_summer "")]
      [:dt "SAP Software"] [:dd (get profile_data :sap_software "")]
      [:dt "Intervention Start Date"] [:dd (get profile_data :intervention_start_date "")]
      [:dt "Co Heating Loss"] [:dd (get profile_data :co_heating_loss "")]
      [:dt "Intention Ofpassvhaus"] [:dd (get profile_data :intention_ofpassvhaus "")]
      [:dt "Dwelling U Value Other"] [:dd (get profile_data :dwelling_u_value_other "")]
      [:dt "Occupancy 18 To 60"] [:dd (get profile_data :occupancy_18_to_60 "")]
      [:dt "Profile Bus Report Url"] [:dd (get profile_data :profile_bus_report_url "")]
      [:dt "Orientation"] [:dd (get profile_data :orientation "")]
      [:dt "Co Heating Equipment"] [:dd (get profile_data :co_heating_equipment "")]
      [:dt "Ventilation Approach"] [:dd (get profile_data :ventilation_approach "")]
      [:dt "Sealed Fireplaces"] [:dd (get profile_data :sealed_fireplaces "")]
      [:dt "Intervention Description"] [:dd (get profile_data :intervention_description "")]
      [:dt "Footprint"] [:dd (get profile_data :footprint "")]
      [:dt "Estimated Cost New Build"] [:dd (get profile_data :estimated_cost_new_build "")]
      [:dt "Mains Gas"] [:dd (get profile_data :mains_gas "")]
      [:dt "SAP Version Year"] [:dd (get profile_data :sap_version_year "")]
      [:dt "Annual Heating Load"] [:dd (get profile_data :annual_heating_load "")]
      [:dt "Glazing Area Percentage"] [:dd (get profile_data :glazing_area_percentage "")]
      [:dt "Profile Image To Visitors"] [:dd (get profile_data :profile_image_to_visitors "")]
      [:dt "Profile Temperature In Summer"] [:dd (get profile_data :profile_temperature_in_summer "")]
      [:dt "Renewable Contribution Heat"] [:dd (get profile_data :renewable_contribution_heat "")]
      [:dt "Profile Design"] [:dd (get profile_data :profile_design "")]
      [:dt "TER"] [:dd (get profile_data :ter "")]
      [:dt "Onsite Days New Build"] [:dd (get profile_data :onsite_days_new_build "")]
      [:dt "Total Area"] [:dd (get profile_data :total_area "")]
      [:dt "Frame Type"] [:dd (get profile_data :frame_type "")]
      [:dt "Multiple Glazing Area Percentage"] [:dd (get profile_data :multiple_glazing_area_percentage "")]
      [:dt "Flat Floor Heat Loss Type"] [:dd (get profile_data :flat_floor_heat_loss_type "")]
      [:dt "Airtightness And Ventilation Strategy"] [:dd (get profile_data :airtightness_and_ventilation_strategy "")]
      [:dt "Overheating Cooling Strategy"] [:dd (get profile_data :overheating_cooling_strategy "")]
      [:dt "Moisture Condensation Mould Strategy"] [:dd (get profile_data :moisture_condensation_mould_strategy "")]
      [:dt "Intervention Completion Date"] [:dd (get profile_data :intervention_completion_date "")]
      [:dt "Thermal Bridging Strategy"] [:dd (get profile_data :thermal_bridging_strategy "")]
      [:dt "Draught Proofing Location"] [:dd (get profile_data :draught_proofing_location "")]
      [:dt "Roof Rooms Present"] [:dd (get profile_data :roof_rooms_present "")]
      [:dt "Innovation Approaches"] [:dd (get profile_data :innovation_approaches "")]
      [:dt "Flat Floors In Block"] [:dd (get profile_data :flat_floors_in_block "")]
      [:dt "Lighting Strategy"] [:dd (get profile_data :lighting_strategy "")]
      [:dt "Total Budget"] [:dd (get profile_data :total_budget "")]
      [:dt "Final Cost New Build"] [:dd (get profile_data :final_cost_new_build "")]
      [:dt "SAP Rating"] [:dd (get profile_data :sap_rating "")]
      [:dt "Pipe Lagging"] [:dd (get profile_data :pipe_lagging "")]
      [:dt "Occupancy Over 60"] [:dd (get profile_data :occupancy_over_60 "")]
      [:dt "BER"] [:dd (get profile_data :ber "")]
      [:dt "ID"] [:dd (get profile_data :id "")]
      [:dt "Profile Temperature In Winter"] [:dd (get profile_data :profile_temperature_in_winter "")]
      [:dt "Total Envelope Area"] [:dd (get profile_data :total_envelope_area "")]
      [:dt "Bedroom Count"] [:dd (get profile_data :bedroom_count "")]
      [:dt "Profile Productivity"] [:dd (get profile_data :profile_productivity "")]
      [:dt "Flat Length Sheltered Wall"] [:dd (get profile_data :flat_length_sheltered_wall "")]
      [:dt "Multiple Glazing U Value"] [:dd (get profile_data :multiple_glazing_u_value "")]
      [:dt "Glazing Area Glass Only"] [:dd (get profile_data :glazing_area_glass_only "")]
      [:dt "Multiple Glazing Type Other"] [:dd (get profile_data :multiple_glazing_type_other "")]
      [:dt "Air Tightness Performed On"] [:dd (get profile_data :air_tightness_performed_on "")]
      [:dt "Air Tightness Assessor"] [:dd (get profile_data :air_tightness_assessor "")]
      [:dt "Best U Value Party Walls"] [:dd (get profile_data :best_u_value_party_walls "")]
      [:dt "Fabric Energy Efficiency"] [:dd (get profile_data :fabric_energy_efficiency "")]
      [:dt "Construction Time New Build"] [:dd (get profile_data :construction_time_new_build "")]
      [:dt "Profile Air In Winter"] [:dd (get profile_data :profile_air_in_winter "")]
      [:dt "External Perimeter"] [:dd (get profile_data :external_perimeter "")]
      [:dt "Cellar Basement Issues"] [:dd (get profile_data :cellar_basement_issues "")]
      [:dt "Best U Value For Doors"] [:dd (get profile_data :best_u_value_for_doors "")]
      [:dt "Heat Loss Parameter Hlp"] [:dd (get profile_data :heat_loss_parameter_hlp "")]
      [:dt "Co Heating Performed On"] [:dd (get profile_data :co_heating_performed_on "")]
      [:dt "Frame Type Other"] [:dd (get profile_data :frame_type_other "")]
      [:dt "Design Guidance"] [:dd (get profile_data :design_guidance "")]
      [:dt "Electricity Meter Type"] [:dd (get profile_data :electricity_meter_type "")]
      [:dt "Ventilation Approach Other"] [:dd (get profile_data :ventilation_approach_other "")]
      [:dt "Heated Habitable Rooms"] [:dd (get profile_data :heated_habitable_rooms "")]
      [:dt "Profile Bus Summary Index"] [:dd (get profile_data :profile_bus_summary_index "")]
      [:dt "Total Budget New Build"] [:dd (get profile_data :total_budget_new_build "")]
      [:dt "Electricity Storage Present"] [:dd (get profile_data :electricity_storage_present "")]
      [:dt "Flat Heat Loss Corridor Other"] [:dd (get profile_data :flat_heat_loss_corridor_other "")]
      [:dt "Water Saving Strategy"] [:dd (get profile_data :water_saving_strategy "")]
      [:dt "Conservation Issues"] [:dd (get profile_data :conservation_issues "")]
      [:dt "Habitable Rooms"] [:dd (get profile_data :habitable_rooms "")]
      [:dt "Best U Value For Other"] [:dd (get profile_data :best_u_value_for_other "")]
      [:dt "Profile Lightning"] [:dd (get profile_data :profile_lightning "")]
      [:dt "Total Rooms"] [:dd (get profile_data :total_rooms "")]
      [:dt "Inadequate Heating"] [:dd (get profile_data :inadequate_heating "")]
      [:dt "Flat Floor Position"] [:dd (get profile_data :flat_floor_position "")]
      [:dt "Planning Considerations"] [:dd (get profile_data :planning_considerations "")]
      [:dt "Draught Proofing"] [:dd (get profile_data :draught_proofing "")]
      [:dt "Occupancy Under 18"] [:dd (get profile_data :occupancy_under_18 "")]
      [:dt "Profile Health"] [:dd (get profile_data :profile_health "")]]])
