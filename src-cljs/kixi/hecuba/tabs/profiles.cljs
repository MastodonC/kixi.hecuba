(ns kixi.hecuba.tabs.profiles
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [kixi.hecuba.bootstrap :as bs]))

(defn get-profiles [selected-property-id data]
  (->>  data
        :properties
        :data
        (filter #(= (:id %) selected-property-id))
        first
        :profiles))

(defn handle-change [owner key e]
  (let [value (.-value (.-target e))]
    (om/set-state! owner [:profile_data key] value)))

(defn text-control [data state owner key label]
  [:div.form-group
   [:label.control-label {:for (name key)} label]
   (if (:editing state)
     [:div
      [:input {:defaultValue (get data key "")
               :class "form-control"
               :on-change #(handle-change owner key %1)
               :type "text"
               :id (name key)}]]
     [:p {:class "form-control-static col-md-10"} (get data key "")])])

(defn text-area-control [data state owner key title]
  (let [text (get data key)]
    (if (:editing state)
      [:div
       [:div.form-group
        [:label.control-label  title]
        [:form {:role "form"}
         [:textarea.form-control {:on-change #(handle-change owner key %1) :rows 2} text]]]]
      (if (and text (re-find #"\w" text))
        [:div [:h3 title]
         [:p text]]
        [:div {:class "hidden"} [:p {:class "form-control-static col-md-10"} text]]))))

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
       [:div
        (for [profile profiles]
          (let [category (get-in profile [:profile_data :category] "Intervention")
                timestamp (format-time-inst  (:timestamp profile) "yyyy-MM-dd")]
            [:div {:class (profile-column-width)}
             [:h3.text-center category [:br ] [:small timestamp]]]))]))))

(defn occupancy-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Occupancy"
              [:div
               (text-control profile_data state owner :occupancy_under_18 "Occupancy Under 18")
               (text-control profile_data state owner :occupancy_18_to_60 "Occupancy 18 To 60")
               (text-control profile_data state owner :occupancy_over_60 "Occupancy Over 60")
               (text-control profile_data state owner :occupant_change "Occupant Change")])]))]))))

(defn measurements-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Measurements"
              [:div
               (text-control profile_data state owner :footprint "Footprint (internal groundfloor area (m2)")
               (text-control profile_data state owner :external_perimeter "External Perimeter (m)")
               (text-control profile_data state owner :gross_internal_area "Gross Internal Area (m2)")
               (text-control profile_data state owner :number_of_storeys "Number Of Storeys")
               (text-control profile_data state owner :total_volume "Total Volume (m3)")
               (text-control profile_data state owner :total_rooms "Total Rooms")
               (text-control profile_data state owner :bedroom_count "Total Bedrooms")
               (text-control profile_data state owner :habitable_rooms "Habitable Rooms")
               (text-control profile_data state owner :inadequate_heating "Inadequate Heating?")
               (text-control profile_data state owner :heated_habitable_rooms "Heated Habitable Rooms")])]))]))))

(defn energy-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Energy"
              [:div
               (text-control profile_data state owner :der "DER (kgCO2/m2/yr)")
               (text-control profile_data state owner :ter "TER (kgCO2/m2/yr)")
               (text-control profile_data state owner :primary_energy_requirement "Total Primary Energy Requirement")
               (text-control profile_data state owner :space_heating_requirement "Space Heating Requirement")
               (text-control profile_data state owner :annual_space_heating_requirement "Annual Space Heating Requirement")
               (text-control profile_data state owner :renewable_contribution_heat "Renewable Contribution Heat")
               (text-control profile_data state owner :renewable_contribution_elec "Renewable Contribution Elec")
               (text-control profile_data state owner :electricity_meter_type "Electricity Meter Type")
               (text-control profile_data state owner :mains_gas "Linked to Mains Gas Supply")
               (text-control profile_data state owner :electricity_storage_present "Electricity Storage Present")
               (text-control profile_data state owner :heat_storage_present "Heat Storage Present")])]))]))))

(defn efficiency-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Efficiency"
              [:div
               (text-control profile_data state owner :pipe_lagging "Pipe Lagging")
               (text-control profile_data state owner :draught_proofing  "Draught Proofing")
               (text-control profile_data state owner :draught_proofing_location  "Draught Proofing Location")])]))]))))

(defn flats-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Flats"
              [:div
               (text-control profile_data state owner :flat_floors_in_block "Floors In Block")
               (text-control profile_data state owner :flat_floor_position "Floor Position")
               (text-control profile_data state owner :flat_heat_loss_corridor "Heat Loss Corridor")
               (text-control profile_data state owner :flat_heat_loss_corridor_other "Heat Loss Corridor Other")
               (text-control profile_data state owner :flat_length_sheltered_wall "Length of Sheltered Wall (m)")
               (text-control profile_data state owner :flat_floor_heat_loss_type "Floor Heat Loss Type")])]))]))))


(defn profile-rows [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        [:form {:role "form"}
         (om/build header-row profiles)
         (om/build occupancy-row profiles)
         (om/build measurements-row profiles)
         (om/build energy-row profiles)
         (om/build efficiency-row profiles)
         (om/build flats-row profiles)
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
         ]])))
  
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

  (text-control profile_data state owner :number_of_storeys "" "Number Of Storeys")
  (text-control profile_data state owner :air_tightness_equipment "" "Air Tightness Equipment")
  (text-control profile_data state owner :gross_internal_area "" "Gross Internal Area")
  (text-control profile_data state owner :category "" "Category")
  (text-control profile_data state owner :sap_performed_on "" "SAP Performed On")
  (text-control profile_data state owner :used_passivehaus_principles "" "Used Passivehaus Principles")
  (text-control profile_data state owner :sap_version_issue "" "SAP Version Issue")
  (text-control profile_data state owner :air_tightness_rate "" "Air Tightness Rate")
  (text-control profile_data state owner :multiple_glazing_type "" "Multiple Glazing Type")
  (text-control profile_data state owner :total_volume "" "Total Volume")
  (text-control profile_data state owner :appliances_strategy "" "Appliances Strategy")
  (text-control profile_data state owner :profile_noise "" "Profile Noise")
  (text-control profile_data state owner :space_heating_requirement "" "Space Heating Requirement")
  (text-control profile_data state owner :flat_heat_loss_corridor "" "Flat Heat Loss Corridor")
  (text-control profile_data state owner :primary_energy_requirement "" "Primary Energy Requirement")
  (text-control profile_data state owner :passive_solar_strategy "" "Passive Solar Strategy")
  (text-control profile_data state owner :profile_comfort "" "Profile Comfort")
  (text-control profile_data state owner :annual_space_heating_requirement "" "Annual Space Heating Requirement")
  (text-control profile_data state owner :onsite_days "" "Onsite Days")
  (text-control profile_data state owner :completeness "" "Completeness")
  (text-control profile_data state owner :renewable_contribution_elec "" "Renewable Contribution Elec")
  (text-control profile_data state owner :best_u_value_for_floors "" "Best U Value For Floors")
  (text-control profile_data state owner :profile_needs "" "Profile Needs")
  (text-control profile_data state owner :open_fireplaces "" "Open Fireplaces")
  (text-control profile_data state owner :sap_assessor "" "SAP Assessor")
  (text-control profile_data state owner :occupant_change "" "Occupant Change")
  (text-control profile_data state owner :controls_strategy "" "Controls Strategy")
  (text-control profile_data state owner :property_id "" "Property Id")
  (text-control profile_data state owner :heat_storage_present "" "Heat Storage Present")
  (text-control profile_data state owner :sap_regulations_date "" "SAP Regulations Date")
  (text-control profile_data state owner :best_u_value_for_windows "" "Best U Value For Windows")
  (text-control profile_data state owner :best_u_value_for_roof "" "Best U Value For Roof")
  (text-control profile_data state owner :co_heating_assessor "" "Co Heating Assessor")
  (text-control profile_data state owner :modelling_software_methods_used "" "Modelling Software Methods Used")
  (text-control profile_data state owner :best_u_value_for_walls "" "Best U Value For Walls")
  (text-control profile_data state owner :profile_air_in_summer "" "Profile Air In Summer")
  (text-control profile_data state owner :sap_software "" "SAP Software")
  (text-control profile_data state owner :intervention_start_date "" "Intervention Start Date")
  (text-control profile_data state owner :co_heating_loss "" "Co Heating Loss")
  (text-control profile_data state owner :intention_ofpassvhaus "" "Intention Ofpassvhaus")
  (text-control profile_data state owner :dwelling_u_value_other "" "Dwelling U Value Other")
  (text-control profile_data state owner :occupancy_18_to_60 "" "Occupancy 18 To 60")
  (text-control profile_data state owner :profile_bus_report_url "" "Profile Bus Report Url")
  (text-control profile_data state owner :orientation "" "Orientation")
  (text-control profile_data state owner :co_heating_equipment "" "Co Heating Equipment")
  (text-control profile_data state owner :ventilation_approach "" "Ventilation Approach")
  (text-control profile_data state owner :sealed_fireplaces "" "Sealed Fireplaces")
  (text-control profile_data state owner :intervention_description "" "Intervention Description")
  (text-control profile_data state owner :footprint "" "Footprint")
  (text-control profile_data state owner :estimated_cost_new_build "" "Estimated Cost New Build")
  (text-control profile_data state owner :mains_gas "" "Mains Gas")
  (text-control profile_data state owner :sap_version_year "" "SAP Version Year")
  (text-control profile_data state owner :annual_heating_load "" "Annual Heating Load")
  (text-control profile_data state owner :glazing_area_percentage "" "Glazing Area Percentage")
  (text-control profile_data state owner :profile_image_to_visitors "" "Profile Image To Visitors")
  (text-control profile_data state owner :profile_temperature_in_summer "" "Profile Temperature In Summer")
  (text-control profile_data state owner :renewable_contribution_heat "" "Renewable Contribution Heat")
  (text-control profile_data state owner :profile_design "" "Profile Design")
  (text-control profile_data state owner :ter "" "TER")
  (text-control profile_data state owner :onsite_days_new_build "" "Onsite Days New Build")
  (text-control profile_data state owner :total_area "" "Total Area")
  (text-control profile_data state owner :frame_type "" "Frame Type")
  (text-control profile_data state owner :multiple_glazing_area_percentage "" "Multiple Glazing Area Percentage")
  (text-control profile_data state owner :flat_floor_heat_loss_type "" "Flat Floor Heat Loss Type")
  (text-control profile_data state owner :airtightness_and_ventilation_strategy "" "Airtightness And Ventilation Strategy")
  (text-control profile_data state owner :overheating_cooling_strategy "" "Overheating Cooling Strategy")
  (text-control profile_data state owner :moisture_condensation_mould_strategy "" "Moisture Condensation Mould Strategy")
  (text-control profile_data state owner :intervention_completion_date "" "Intervention Completion Date")
  (text-control profile_data state owner :thermal_bridging_strategy "" "Thermal Bridging Strategy")
  (text-control profile_data state owner :draught_proofing_location "" "Draught Proofing Location")
  (text-control profile_data state owner :roof_rooms_present "" "Roof Rooms Present")
  (text-control profile_data state owner :innovation_approaches "" "Innovation Approaches")
  (text-control profile_data state owner :flat_floors_in_block "" "Flat Floors In Block")
  (text-control profile_data state owner :lighting_strategy "" "Lighting Strategy")
  (text-control profile_data state owner :total_budget "" "Total Budget")
  (text-control profile_data state owner :final_cost_new_build "" "Final Cost New Build")
  (text-control profile_data state owner :sap_rating "" "SAP Rating")
  (text-control profile_data state owner :pipe_lagging "" "Pipe Lagging")
  (text-control profile_data state owner :occupancy_over_60 "" "Occupancy Over 60")
  (text-control profile_data state owner :ber "" "BER")
  (text-control profile_data state owner :id "" "ID")
  (text-control profile_data state owner :profile_temperature_in_winter "" "Profile Temperature In Winter")
  (text-control profile_data state owner :total_envelope_area "" "Total Envelope Area")
  (text-control profile_data state owner :bedroom_count "" "Bedroom Count")
  (text-control profile_data state owner :profile_productivity "" "Profile Productivity")
  (text-control profile_data state owner :flat_length_sheltered_wall "" "Flat Length Sheltered Wall")
  (text-control profile_data state owner :multiple_glazing_u_value "" "Multiple Glazing U Value")
  (text-control profile_data state owner :glazing_area_glass_only "" "Glazing Area Glass Only")
  (text-control profile_data state owner :multiple_glazing_type_other "" "Multiple Glazing Type Other")
  (text-control profile_data state owner :air_tightness_performed_on "" "Air Tightness Performed On")
  (text-control profile_data state owner :air_tightness_assessor "" "Air Tightness Assessor")
  (text-control profile_data state owner :best_u_value_party_walls "" "Best U Value Party Walls")
  (text-control profile_data state owner :fabric_energy_efficiency "" "Fabric Energy Efficiency")
  (text-control profile_data state owner :construction_time_new_build "" "Construction Time New Build")
  (text-control profile_data state owner :profile_air_in_winter "" "Profile Air In Winter")
  (text-control profile_data state owner :external_perimeter "" "External Perimeter")
  (text-control profile_data state owner :cellar_basement_issues "" "Cellar Basement Issues")
  (text-control profile_data state owner :best_u_value_for_doors "" "Best U Value For Doors")
  (text-control profile_data state owner :heat_loss_parameter_hlp "" "Heat Loss Parameter Hlp")
  (text-control profile_data state owner :co_heating_performed_on "" "Co Heating Performed On")
  (text-control profile_data state owner :frame_type_other "" "Frame Type Other")
  (text-control profile_data state owner :design_guidance "" "Design Guidance")
  (text-control profile_data state owner :electricity_meter_type "" "Electricity Meter Type")
  (text-control profile_data state owner :ventilation_approach_other "" "Ventilation Approach Other")
  (text-control profile_data state owner :heated_habitable_rooms "" "Heated Habitable Rooms")
  (text-control profile_data state owner :profile_bus_summary_index "" "Profile Bus Summary Index")
  (text-control profile_data state owner :total_budget_new_build "" "Total Budget New Build")
  (text-control profile_data state owner :electricity_storage_present "" "Electricity Storage Present")
  (text-control profile_data state owner :flat_heat_loss_corridor_other "" "Flat Heat Loss Corridor Other")
  (text-control profile_data state owner :water_saving_strategy "" "Water Saving Strategy")
  (text-control profile_data state owner :conservation_issues "" "Conservation Issues")
  (text-control profile_data state owner :habitable_rooms "" "Habitable Rooms")
  (text-control profile_data state owner :best_u_value_for_other "" "Best U Value For Other")
  (text-control profile_data state owner :profile_lightning "" "Profile Lightning")
  (text-control profile_data state owner :total_rooms "" "Total Rooms")
  (text-control profile_data state owner :inadequate_heating "" "Inadequate Heating")
  (text-control profile_data state owner :flat_floor_position "" "Flat Floor Position")
  (text-control profile_data state owner :planning_considerations "" "Planning Considerations")
  (text-control profile_data state owner :draught_proofing "" "Draught Proofing")
  (text-control profile_data state owner :occupancy_under_18 "" "Occupancy Under 18")
  (text-control profile_data state owner :profile_health "Profile Health")
  )
