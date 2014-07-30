(ns kixi.hecuba.tabs.hierarchy.profiles
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
     [:p.form-control-static (get data key "")])])

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
        [:div {:class "hidden"} [:p.form-control-static text]]))))

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
          (let [category (get-in profile [:profile_data :event_type] "Intervention")
                timestamp (format-time-inst  (:timestamp profile) "yyyy-MM-dd")]
            [:div {:class (profile-column-width)}
             [:h3.text-center category [:br ] [:small timestamp]]]))]))))

(defn description-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Description"
              [:div
               ;; FIXME format dates better
               (text-control profile_data state owner :intervention_start_date "Intervention Start Date")
               (text-control profile_data state owner :intervention_completion_date "Intervention Completion Date")
               (text-control profile_data state owner :intervention_description "Intervention Description")])]))]))))


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
               (text-control profile_data state owner :footprint "Footprint (internal groundfloor area m2)")
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
               (text-control profile_data state owner :ber "BER (kgCO2/m2/yr)")
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

(defn passivhaus-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "PassivHaus"
              [:div
               (text-control profile_data state owner :passive_solar_strategy "Passive Solar Strategy")
               (text-control profile_data state owner :used_passivehaus_principles "Used Passivehaus Principles")])]))]))))

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

(defn fireplaces-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Fireplaces"
              [:div
               (text-control profile_data state owner :open_fireplaces "Number of Open Fireplaces")
               (text-control profile_data state owner :sealed_fireplaces "Number of Sealed Fireplaces")])]))]))))

(defn glazing-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Glazing"
              [:div
               (text-control profile_data state owner :glazing_area_glass_only "Glazing Area Glass Only")
               (text-control profile_data state owner :glazing_area_percentage "Glazing Area (% if known)")
               (text-control profile_data state owner :multiple_glazing_type "Multiple Glazing Type")
               (text-control profile_data state owner :multiple_glazing_area_percentage "Multiple Glazing Area (% if known)")
               (text-control profile_data state owner :multiple_glazing_u_value "Multiple Glazing U Value")
               (text-control profile_data state owner :multiple_glazing_type_other "Multiple Glazing Type Other")
               (text-control profile_data state owner :frame_type "Frame Type")
               (text-control profile_data state owner :frame_type_other "Frame Type Other")])]))]))))

(defn issues-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Issues"
              [:div
               (text-control profile_data state owner :moisture_condensation_mould_strategy "Moisture Condensation Mould Strategy")
               (text-control profile_data state owner :appliances_strategy "Appliances Strategy")
               (text-control profile_data state owner :cellar_basement_issues "Cellar Basement Issues")])]))]))))

(defn lessons-learnt-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Lesson Learnt"
              [:div
               (text-control profile_data state owner :thermal_bridging_strategy "Thermal Bridging Strategy")
               (text-control profile_data state owner :airtightness_and_ventilation_strategy "Airtightness And Ventilation Strategy")
               ;; moisture/mould/condensation?
               ;; passive solar?
               (text-control profile_data state owner :overheating_cooling_strategy "Overheating Cooling Strategy")
               (text-control profile_data state owner :controls_strategy "Controls Strategy")
               ;; appliances
               (text-control profile_data state owner :lighting_strategy "Lighting Strategy")
               (text-control profile_data state owner :water_saving_strategy "Water Saving Strategy")
               (text-control profile_data state owner :innovation_approaches "Innovation Approaches")])]))]))))

(defn project-details-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Project Details"
              [:div
               (text-control profile_data state owner :total_budget_new_build "Total Budget New Build")
               (text-control profile_data state owner :estimated_cost_new_build "Estimated Cost New Build")
               (text-control profile_data state owner :final_cost_new_build "Final Cost New Build")
               (text-control profile_data state owner :construction_time_new_build "Construction Time New Build (Days)")
               (text-control profile_data state owner :design_guidance "Design Guidance")
               (text-control profile_data state owner :planning_considerations "Planning Considerations")
               (text-control profile_data state owner :total_budget "Total Budget")])]))]))))

(defn coheating-test-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Coheating Test"
              [:div
               (text-control profile_data state owner :co_heating_loss "Fabric Heat Loss (W/m2)")
               (text-control profile_data state owner :co_heating_performed_on "Dates Performed")
               (text-control profile_data state owner :co_heating_assessor "Assessor Used")
               (text-control profile_data state owner :co_heating_equipment "Equipment")])]))]))))

(defn dwelling-u-values-summary-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Dwelling U-values Summary"
              [:div
               (text-control profile_data state owner :best_u_value_for_doors "Best U Value For Doors")
               (text-control profile_data state owner :best_u_value_for_floors "Best U Value For Floors")
               (text-control profile_data state owner :best_u_value_for_other "Best U Value For Other")
               (text-control profile_data state owner :best_u_value_for_roof "Best U Value For Roof")
               (text-control profile_data state owner :best_u_value_for_walls "Best U Value For Walls")
               (text-control profile_data state owner :best_u_value_for_windows "Best U Value For Windows")
               (text-control profile_data state owner :best_u_value_party_walls "Best U Value Party Walls")
               (text-control profile_data state owner :dwelling_u_value_other "Dwelling U Value Other")])]))]))))

(defn air-tightness-test-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "Air Tightness Test"
              [:div
               (text-control profile_data state owner :air_tightness_assessor "Air Tightness Assessor")
               (text-control profile_data state owner :air_tightness_equipment "Air Tightness Equipment")
               (text-control profile_data state owner :air_tightness_performed_on "Air Tightness Performed On")
               (text-control profile_data state owner :air_tightness_rate "Air Tightness Rate")])]))]))))

(defn bus-survey-information-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "BUS Survey Information"
              [:div
               (text-control profile_data state owner :profile_temperature_in_summer "Temperature In Summer")
               (text-control profile_data state owner :profile_temperature_in_winter "Temperature In Winter")
               (text-control profile_data state owner :profile_air_in_summer "Air In Summer")
               (text-control profile_data state owner :profile_air_in_winter "Air In Winter")
               (text-control profile_data state owner :profile_lightning "Lightning")
               (text-control profile_data state owner :profile_noise "Noise")
               (text-control profile_data state owner :profile_comfort "Comfort")
               (text-control profile_data state owner :profile_design "Design")
               (text-control profile_data state owner :profile_needs "Needs")
               (text-control profile_data state owner :profile_health "Health (perceived)")
               (text-control profile_data state owner :profile_image_to_visitors "Image To Visitors")
               (text-control profile_data state owner :profile_productivity "Productivity")
               (text-control profile_data state owner :profile_bus_summary_index "Bus Summary Index")
               (text-control profile_data state owner :profile_bus_report_url "BUS Report Url")])]))]))))

(defn sap-results-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          (let [profile_data (:profile_data profile)]
            [:div {:class (profile-column-width)}
             (bs/panel
              "SAP Results"
              [:div
               (text-control profile_data state owner :sap_rating "SAP Rating")
               (text-control profile_data state owner :sap_performed_on "SAP Performed On")
               (text-control profile_data state owner :sap_assessor "SAP Assessor")
               (text-control profile_data state owner :sap_version_issue "SAP Version Issue")
               (text-control profile_data state owner :sap_version_year "SAP Version Year")
               (text-control profile_data state owner :sap_regulations_date "SAP Regulations Date")
               (text-control profile_data state owner :sap_software "Name of SAP Software")])]))]))))

(defn conservatories-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Conservatories"
            (if-let [conservatories (seq (:conservatories profile))]
              [:div
               (for [c conservatories]
                 (bs/panel
                  "Conservtory"
                  [:div
                   (text-control c state owner :conservatory_type "Conservatory Type")
                   (text-control c state owner :area "Area")
                   (text-control c state owner :double_glazed "Double Glazed")
                   (text-control c state owner :glazed_perimeter "Glazed Perimeter")
                   (text-control c state owner :height "Height")]))]
              [:p "No conservatories."]))])]))))

(defn extensions-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Extensions"
            (if-let [extensions (seq (:extensions profile))]
              [:div
               (for [item extensions]
                 (bs/panel
                  "Extension"
                  [:div
                   (text-control item state owner :age "Age")
                   (text-control item state owner :construction_date "Construction Date")]))]
              [:p "No extensions."]))])]))))

(defn heating-systems-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Heating Systems"
            (if-let [heating-systems (seq (:heating_systems profile))]
              [:div
               (for [item heating-systems]
                 (bs/panel
                  "Heating System"
                  [:div
                   (text-control item state owner :heating_type "Heating Type")
                   (text-control item state owner :heat_source "Heat Source")
                   (text-control item state owner :heat_transport "Heat Transport")
                   (text-control item state owner :heat_delivery "Heat Delivery")
                   (text-control item state owner :heat_delivery_source "Heat Delivery Source")
                   (text-control item state owner :efficiency_derivation "Efficiency Derivation")
                   (text-control item state owner :boiler_type "Boiler Type")
                   (text-control item state owner :boiler_type_other "Boiler Type Other")
                   (text-control item state owner :fan_flue "Fan Flue")
                   (text-control item state owner :open_flue "Open Flue")
                   (text-control item state owner :fuel "Fuel")
                   (text-control item state owner :heating_system "Heating System")
                   (text-control item state owner :heating_system_other "Heating System Other")
                   (text-control item state owner :heating_system_type "Heating System Type")
                   (text-control item state owner :heating_system_type_other "Heating System Type Other")
                   (text-control item state owner :heating_system_solid_fuel "Heating System Solid Fuel")
                   (text-control item state owner :heating_system_solid_fuel_other "Heating System Solid Fuel Other")
                   (text-control item state owner :bed_index "Bed Index")
                   (text-control item state owner :make_and_model "Make and Model")
                   (text-control item state owner :controls "Controls")
                   (text-control item state owner :controls_other "Controls Other")
                   (text-control item state owner :controls_make_and_model "Controls Make and Model")
                   (text-control item state owner :emitter "Emitter")
                   (text-control item state owner :trvs_on_emitters "TRVs on Emitters")
                   (text-control item state owner :use_hours_per_week "Use Hours per Week")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_engineers_name "Installer Engineers Name")
                   (text-control item state owner :installer_registration_number "Installer Registration Number")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :inspector "Inspector")
                   (text-control item state owner :inspector_engineers_name "Inspector Engineers Name")
                   (text-control item state owner :inspector_registration_number "Inspector Registration Number ")
                   (text-control item state owner :inspection_date "Inspection Date")
                   (text-control item state owner :efficiency "Efficiency")]))]
              [:p "No heating systems."]))])]))))

(defn hot-water-systems-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Hot Water Systems"
            (if-let [hot-water-systems (seq (:hot_water_systems profile))]
              [:div
               (for [item hot-water-systems]
                 (bs/panel
                  "Hot Water System"
                  [:div
                   (text-control item state owner :dhw_type "DHW Type")
                   (text-control item state owner :fuel "Fuel")
                   (text-control item state owner :fuel_other "Fuel Other")
                   (text-control item state owner :immersion "Immersion")
                   (text-control item state owner :cylinder_capacity "Cylinder Capacity")
                   (text-control item state owner :cylinder_capacity_other "Cylinder Capacity Other")
                   (text-control item state owner :cylinder_insulation_type "Cylinder Insulation Type")
                   (text-control item state owner :cylinder_insulation_type_other "Cylinder Insulation Type Other")
                   (text-control item state owner :cylinder_insulation_thickness "Cylinder Insulation Thickness")
                   (text-control item state owner :cylinder_insulation_thickness_other "Cylinder Insulation Thickness Other")
                   (text-control item state owner :cylinder_thermostat "Cylinder Thermostat")
                   (text-control item state owner :controls_same_for_all_zones "Controls Same For All Zones")]))]
              [:p "No hot water systems."]))])]))))

(defn storeys-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Storeys"
            (if-let [storeys (seq (:storeys profile))]
              [:div
               (for [item storeys]
                 (bs/panel
                  "Storey"
                  [:div
                   (text-control item state owner :storey_type "Storey Type")
                   (text-control item state owner :storey "Storey")
                   (text-control item state owner :heat_loss_w_per_k "Heat Loss W Per K")
                   (text-control item state owner :heat_requirement_kwth_per_year "Heat Requirement kWh Per Year")]))]
              [:p "No storeys recorded."]))])]))))

(defn walls-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Walls"
            (if-let [walls (seq (:walls profile))]
              [:div
               (for [item walls]
                 (bs/panel
                  "Wall"
                  [:div
                   (text-control item state owner :wall_type "Wall Type")
                   (text-control item state owner :construction "Construction")
                   (text-control item state owner :construction_other "Construction Other")
                   (text-control item state owner :insulation "Insulation")
                   (text-control item state owner :insulation_date "Insulation Date")
                   (text-control item state owner :insulation_type "Insulation Type")
                   (text-control item state owner :insulation_thickness "Insulation Thickness")
                   (text-control item state owner :insulation_product "Insulation Product")
                   (text-control item state owner :uvalue "U Value")
                   (text-control item state owner :location "Location")
                   (text-control item state owner :area "Area")]))]
              [:p "No walls recorded."]))])]))))

(defn roofs-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Roofs"
            (if-let [roofs (seq (:roofs profile))]
              [:div
               (for [item roofs]
                 (bs/panel
                  "Roof"
                  [:div
                   (text-control item state owner :roof_type "Roof Type")
                   (text-control item state owner :construction "Construction")
                   (text-control item state owner :construction_other "Construction Other")
                   (text-control item state owner :insulation_location_one "Insulation Location One")
                   (text-control item state owner :insulation_location_one_other "Insulation Location One Other")
                   (text-control item state owner :insulation_location_two "Insulation Location Two")
                   (text-control item state owner :insulation_location_two_other "Insulation Location Two Other")
                   (text-control item state owner :insulation_thickness_one "Insulation Thickness One")
                   (text-control item state owner :insulation_thickness_one_other "Insulation Thickness One Other")
                   (text-control item state owner :insulation_thickness_two "Insulation Thickness Two")
                   (text-control item state owner :insulation_thickness_two_other "Insulation Thickness Two Other")
                   (text-control item state owner :insulation_date "Insulation Date")
                   (text-control item state owner :insulation_type "Insulation Type")
                   (text-control item state owner :insulation_product "Insulation Product")
                   (text-control item state owner :uvalue "U Value")
                   (text-control item state owner :uvalue_derived "U Value Derived")]))]
              [:p "No roofs recorded."]))])]))))

(defn window-sets-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Window Sets"
            (if-let [window-sets (seq (:window_sets profile))]
              [:div
               (for [item window-sets]
                 (bs/panel
                  "Window Set"
                  [:div
                   (text-control item state owner :window_type "Window Type")
                   (text-control item state owner :frame_type "Frame Type")
                   (text-control item state owner :frame_type_other "Frame Type Other")
                   (text-control item state owner :percentage_glazing "Percentage Glazing")
                   (text-control item state owner :area "Area")
                   (text-control item state owner :location "Location")
                   (text-control item state owner :uvalue "Uvalue")]))]
              [:p "No window sets recorded."]))])]))))

(defn door-sets-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Door Sets"
            (if-let [door-sets (seq (:door_sets profile))]
              [:div
               (for [item door-sets]
                 (bs/panel
                  "Door Set"
                  [:div
                   (text-control item state owner :door_type "Door Type")
                   (text-control item state owner :door_type_other "Door Type Other")
                   (text-control item state owner :frame_type "Frame Type")
                   (text-control item state owner :frame_type_other "Frame Type Other")
                   (text-control item state owner :percentage_glazing "Percentage Glazing")
                   (text-control item state owner :area "Area")
                   (text-control item state owner :location "Location")
                   (text-control item state owner :uvalue "U Value")]))]
              [:p "No door sets recorded."]))])]))))


(defn floors-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Floors"
            (if-let [floors (seq (:floors profile))]
              [:div
               (for [item floors]
                 (bs/panel
                  "Floor"
                  [:div
                   (text-control item state owner :floor_type "Floor Type")
                   (text-control item state owner :construction "Construction")
                   (text-control item state owner :construction_other "Construction Other")
                   (text-control item state owner :insulation_thickness_one "Insulation Thickness One")
                   (text-control item state owner :insulation_thickness_two "Insulation Thickness Two")
                   (text-control item state owner :insulation_type "Insulation Type")
                   (text-control item state owner :insulation_product "Insulation Product")
                   (text-control item state owner :uvalue "U Value")
                   (text-control item state owner :uvalue_derived "Uvalue Derived")]))]
              [:p "No floors recorded."]))])]))))

(defn roof-rooms-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Door Sets"
            (if-let [roof-rooms (seq (:roof_rooms profile))]
              [:div
               (for [item roof-rooms]
                 (bs/panel
                  "Door Set"
                  [:div
                   (text-control item state owner :location "Location")
                   (text-control item state owner :age "Age")
                   (text-control item state owner :insulation_placement "Insulation Placement")
                   (text-control item state owner :insulation_thickness_one "Insulation Thickness One")
                   (text-control item state owner :insulation_thickness_one_other "Insulation Thickness One Other")
                   (text-control item state owner :insulation_thickness_two "Insulation Thickness Two")
                   (text-control item state owner :insulation_thickness_two_other "Insulation Thickness Two Other")
                   (text-control item state owner :insulation_date "Insulation Date")
                   (text-control item state owner :insulation_type "Insulation Type")
                   (text-control item state owner :insulation_product "Insulation Product")
                   (text-control item state owner :uvalue "U Value")
                   (text-control item state owner :uvalue_derived "Uvalue Derived")]))]
              [:p "No roof rooms recorded."]))])]))))

(defn low-energy-lights-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Low Energy Lights"
            (if-let [low-energy-lights (seq (:low_energy_lights profile))]
              [:div
               (for [item low-energy-lights]
                 (bs/panel
                  "Low Energy Light"
                  [:div
                   (text-control item state owner :light_type "Light Type")
                   (text-control item state owner :light_type_other "Light Type Other")
                   (text-control item state owner :bed_index "Bed Index")
                   (text-control item state owner :proportion "Proportion")]))]
              [:p "No low energy lights recorded."]))])]))))

(defn ventilation-systems-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Ventilation Systems"
            (if-let [ventilation-systems (seq (:ventilation_systems profile))]
              [:div
               (for [item ventilation-systems]
                 (bs/panel
                  "Ventilation System"
                  [:div
                   (text-control item state owner :approach "Approach")
                   (text-control item state owner :approach_other "Approach Other")
                   (text-control item state owner :ventilation_type "Ventilation Type")
                   (text-control item state owner :ventilation_type_other "Ventilation Type Other")
                   (text-control item state owner :mechanical_with_heat_recovery "Mechanical With Heat Recovery")
                   (text-control item state owner :manufacturer "Manufacturer")
                   (text-control item state owner :ductwork_type "Ductwork Type")
                   (text-control item state owner :ductwork_type_other "Ductwork Type Other")
                   (text-control item state owner :controls "Controls")
                   (text-control item state owner :controls_other "Controls Other")
                   (text-control item state owner :manual_control_location "Manual Control Location")
                   (text-control item state owner :operational_settings "Operational Settings")
                   (text-control item state owner :operational_settings_other "Operational Settings Other")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_engineers_name "Installer Engineers Name")
                   (text-control item state owner :installer_registration_number "Installer Registration Number")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :total_installed_area "Total Installed Area")]))]
              [:p "No ventilation systems lights recorded."]))])]))))

(defn airflow-measurements-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Air Flow Measurements"
            (if-let [airflow-measurements (seq (:airflow_measurements profile))]
              [:div
               (for [item airflow-measurements]
                 (bs/panel
                  "Air Flow Measurements"
                  [:div
                   (text-control item state owner :reference "Reference")
                   (text-control item state owner :system "System")
                   (text-control item state owner :inspector "Inspector")
                   (text-control item state owner :inspector_engineers_name "Inspector Engineers Name")
                   (text-control item state owner :inspector_registration_number "Inspector Registration Number")
                   (text-control item state owner :inspection_date "Inspection Date")
                   (text-control item state owner :measured_low "Measured Low")
                   (text-control item state owner :design_low "Design Low")
                   (text-control item state owner :measured_high "Measured High")
                   (text-control item state owner :design_high "Design High")]))]
              [:p "No air flow measurements recorded."]))])]))))


(defn photovoltaic-panels-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Photovoltaic-Panels"
            (if-let [photovoltaic-panels (seq (:photovoltaics profile))]
              [:div
               (for [item photovoltaic-panels]
                 (bs/panel
                  "Photovoltaic Panel"
                  [:div
                   (text-control item state owner :percentage_roof_covered "Percentage Roof Covered")
                   (text-control item state owner :photovoltaic_type "Photovoltaic Type")
                   (text-control item state owner :photovoltaic_type_other "Photovoltaic Type Other")
                   (text-control item state owner :make_model "Make Model")
                   (text-control item state owner :mcs_no "MCS No")
                   (text-control item state owner :efficiency "Efficiency")
                   (text-control item state owner :inverter_type "Inverter Type")
                   (text-control item state owner :inverter_make_model "Inverter Make Model")
                   (text-control item state owner :inverter_mcs_no "Inverter MCS No")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_mcs_no "Installer MCS No")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :capacity "Capacity")
                   (text-control item state owner :area "Area")
                   (text-control item state owner :orientation "Orientation")
                   (text-control item state owner :pitch "Pitch")
                   (text-control item state owner :est_annual_generation "Est Annual Generation")
                   (text-control item state owner :est_percentage_requirement_met "Est Percentage Requirement Met")
                   (text-control item state owner :est_percentage_exported "Est Percentage Exported")
                   (text-control item state owner :performance "Performance")]))]
              [:p "No Photovoltaic-Panels recorded."]))])]))))

(defn solar-thermal-panels-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Solar Thermal Panels"
            (if-let [solar-thermal-panels (seq (:solar_thermals profile))]
              [:div
               (for [item solar-thermal-panels]
                 (bs/panel
                  "Solar Thermal Panel"
                  [:div
                   (text-control item state owner :solar_type "Solar Type")
                   (text-control item state owner :solar_type_other "Solar Type Other")
                   (text-control item state owner :make_model "Make Model")
                   (text-control item state owner :mcs_no "MCS No")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_mcs_no "Installer MCS No")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :capacity "Capacity")
                   (text-control item state owner :area "Area")
                   (text-control item state owner :orientation "Orientation")
                   (text-control item state owner :pitch "Pitch")
                   (text-control item state owner :est_annual_generation "Est Annual Generation")
                   (text-control item state owner :est_percentage_requirement_met "Est Percentage Requirement Met")]))]
              [:p "No solar thermal panels recorded."]))])]))))

(defn wind-turbines-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Wind Turbines"
            (if-let [wind-turbines (seq (:wind_turbines profile))]
              [:div
               (for [item wind-turbines]
                 (bs/panel
                  "Wind Turbine"
                  [:div
                   (text-control item state owner :turbine_type "Turbine Type")
                   (text-control item state owner :turbine_type_other "Turbine Type Other")
                   (text-control item state owner :make_model "Make Model")
                   (text-control item state owner :mcs_no "MCS No")
                   (text-control item state owner :inverter_type "Inverter Type")
                   (text-control item state owner :inverter_make_model "Inverter Make Model")
                   (text-control item state owner :inverter_mcs_no "Inverter MCS No")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_mcs_no "Installer MCS No")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :capacity "Capacity")
                   (text-control item state owner :hub_height "Hub Height")
                   (text-control item state owner :height_above_canpoy "Height Above Canpoy")
                   (text-control item state owner :wind_speed "Wind Speed")
                   (text-control item state owner :wind_speed_info_source "Wind Speed Info Source")
                   (text-control item state owner :wind_speed_info_source_other "Wind Speed Info Source Other")
                   (text-control item state owner :est_annual_generation "Est Annual Generation")
                   (text-control item state owner :est_percentage_requirement_met "Est Percentage Requirement Met")
                   (text-control item state owner :est_percentage_exported "Est Percentage Exported")]))]
              [:p "No wind turbines recorded."]))])]))))

(defn small-hydro-plants-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Small Hydro Plants"
            (if-let [small-hydro-plants (seq (:small_hydro profile))]
              [:div
               (for [item small-hydro-plants]
                 (bs/panel
                  "Small Hydro Plant"
                  [:div
                   (text-control item state owner :hydro_type "Hydro Type")
                   (text-control item state owner :make_model "Make Model")
                   (text-control item state owner :mcs_no "MCS No")
                   (text-control item state owner :inverter_type "Inverter Type")
                   (text-control item state owner :inverter_make_model "Inverter Make Model")
                   (text-control item state owner :inverter_mcs_no "Inverter MCS No")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_mcs_no "Installer MCS No")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :capacity "Capacity")
                   (text-control item state owner :head_drop "Head Drop")
                   (text-control item state owner :design_flow "Design Flow")
                   (text-control item state owner :est_annual_generation "Est Annual Generation")
                   (text-control item state owner :est_percentage_requirement_met "Est Percentage Requirement Met")
                   (text-control item state owner :est_percentage_exported "Est Percentage Exported")]))]
              [:p "No small hydro plants recorded."]))])]))))

(defn heat-pumps-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Heat Pumps"
            (if-let [heat-pumps (seq (:heat_pumps profile))]
              [:div
               (for [item heat-pumps]
                 (bs/panel
                  "Heat Pump"
                  [:div
                   (text-control item state owner :heat_pump_type "Heat Pump Type")
                   (text-control item state owner :make_model "Make Model")
                   (text-control item state owner :cop "CoP")
                   (text-control item state owner :spf "SPF")
                   (text-control item state owner :mcs_no "MCS No")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_mcs_no "Installer MCS No")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :heat_source_type "Heat Source Type")
                   (text-control item state owner :heat_source_type_other "Heat Source Type Other")
                   (text-control item state owner :depth "Depth")
                   (text-control item state owner :geology "Geology")
                   (text-control item state owner :capacity "Capacity")
                   (text-control item state owner :est_annual_generation "Est Annual Generation")
                   (text-control item state owner :est_percentage_requirement_met "Est Percentage Requirement Met")
                   (text-control item state owner :dhw "DHW")
                   (text-control item state owner :est_percentage_dhw_requirement_met "Est Percentage Dhw Requirement Met")]))]
              [:p "No heat pumps recorded."]))])]))))

(defn biomass-boilers-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "Biomass Boilers"
            (if-let [biomass-boilers (seq (:biomass_boilers profile))]
              [:div
               (for [item biomass-boilers]
                 (bs/panel
                  "Biomass Boiler"
                  [:div
                   (text-control item state owner :biomass_type "Biomass Type")
                   (text-control item state owner :model "Model")
                   (text-control item state owner :mcs_no "MCS No")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_mcs_no "Installer MCS No")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :capacity "Capacity")
                   (text-control item state owner :percentage_efficiency_from_spec "Percentage Efficiency From Spec")
                   (text-control item state owner :est_annual_generation "Est Annual Generation")
                   (text-control item state owner :est_percentage_requirement_met "Est Percentage Requirement Met")]))]
              [:p "No biomass boilers recorded."]))])]))))

(defn mCHP-systems-row [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (for [profile profiles]
          [:div {:class (profile-column-width)}
           (bs/panel
            "mCHP Systems"
            (if-let [mCHP-systems (seq (:chps profile))]
              [:div
               (for [item mCHP-systems]
                 (bs/panel
                  "mCHP System"
                  [:div
                   (text-control item state owner :chp_type "CHP Type")
                   (text-control item state owner :model "Model")
                   (text-control item state owner :mcs_no "MCS No")
                   (text-control item state owner :installer "Installer")
                   (text-control item state owner :installer_mcs_no "Installer MCS No")
                   (text-control item state owner :commissioning_date "Commissioning Date")
                   (text-control item state owner :capacity_elec "Capacity Elec")
                   (text-control item state owner :capacity_thermal "Capacity Thermal")
                   (text-control item state owner :est_annual_generation "Est Annual Generation")
                   (text-control item state owner :est_percentage_thermal_requirement_met "Est Percentage Thermal Requirement Met")
                   (text-control item state owner :est_percentage_exported "Est Percentage Exported")]))]
              [:p "No mCHP systems recorded."]))])]))))

(defn profile-rows [profiles owner]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        [:form {:role "form"}
         ;; profile data
         (om/build header-row profiles)
         (om/build description-row profiles)
         (om/build occupancy-row profiles)
         (om/build measurements-row profiles)
         (om/build energy-row profiles)
         (om/build efficiency-row profiles)
         (om/build passivhaus-row profiles)
         (om/build flats-row profiles)
         (om/build fireplaces-row profiles)
         (om/build glazing-row profiles)
         (om/build issues-row profiles)
         (om/build sap-results-row profiles)
         (om/build lessons-learnt-row profiles)
         (om/build dwelling-u-values-summary-row profiles)
         (om/build air-tightness-test-row profiles)
         (om/build bus-survey-information-row profiles)
         (om/build project-details-row profiles)
         ;; (om/build documents profiles)

         ;; dwelling details
         (om/build conservatories-row profiles)
         (om/build extensions-row profiles)
         (om/build heating-systems-row profiles)
         (om/build hot-water-systems-row profiles)
         (om/build storeys-row profiles)
         (om/build walls-row profiles)
         (om/build roofs-row profiles)
         (om/build window-sets-row profiles)
         (om/build door-sets-row profiles)
         (om/build floors-row profiles)
         (om/build roof-rooms-row profiles)
         (om/build low-energy-lights-row profiles)
         (om/build ventilation-systems-row profiles)
         (om/build airflow-measurements-row profiles)

         ;; renewable energy systems
         (om/build photovoltaic-panels-row profiles)
         (om/build solar-thermal-panels-row profiles)
         (om/build wind-turbines-row profiles)
         (om/build small-hydro-plants-row profiles)
         (om/build heat-pumps-row profiles)
         (om/build biomass-boilers-row profiles)
         (om/build mCHP-systems-row profiles)]]))))

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

  (text-control profile_data state owner :annual_heating_load "Annual Heating Load")
  (text-control profile_data state owner :completeness "Completeness")
  (text-control profile_data state owner :conservation_issues "Conservation Issues")
  (text-control profile_data state owner :fabric_energy_efficiency "Fabric Energy Efficiency")
  (text-control profile_data state owner :heat_loss_parameter_hlp "Heat Loss Parameter Hlp")
  (text-control profile_data state owner :id "ID")
  (text-control profile_data state owner :intention_ofpassvhaus "Intention Ofpassvhaus")
  (text-control profile_data state owner :intervention_completion_date "Intervention Completion Date")
  (text-control profile_data state owner :intervention_description "Intervention Description")
  (text-control profile_data state owner :intervention_start_date "Intervention Start Date")
  (text-control profile_data state owner :modelling_software_methods_used "Modelling Software Methods Used")
  (text-control profile_data state owner :onsite_days "Onsite Days")
  (text-control profile_data state owner :onsite_days_new_build "Onsite Days New Build")
  (text-control profile_data state owner :orientation "Orientation")
  (text-control profile_data state owner :property_id "Property Id")
  (text-control profile_data state owner :roof_rooms_present "Roof Rooms Present")
  (text-control profile_data state owner :space_heating_requirement "Space Heating Requirement")
  (text-control profile_data state owner :total_area "Total Area")
  (text-control profile_data state owner :total_envelope_area "Total Envelope Area")
  (text-control profile_data state owner :ventilation_approach "Ventilation Approach")
  (text-control profile_data state owner :ventilation_approach_other "Ventilation Approach Other")
  )
