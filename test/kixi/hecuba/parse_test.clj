(ns kixi.hecuba.parse-test
  (:use clojure.test)
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check :as tc]
            [schema.core :as s]
            [schema_gen.core :as sg]
            [clojure.test.check.properties :as prop]))

(defn non-empty? [n] (if (coll? n) (seq n) n))

;; These are functions copied from forms.cljs. Test should be moved to cljs tests once they are set up.
(defn parse
  "Remove all empty elements from the nested data structure and flatten :_value elements."
  [cursor]
  (clojure.walk/postwalk (fn [m]
                           (cond
                            (:_value m)
                            (:_value m)

                            (map? m)
                            (reduce-kv (fn [agg k v] (if (non-empty? v) (assoc agg k v) agg)) {} m)

                            (and (coll? m) (not (keyword? (first m))))
                            (into (empty m) (filter non-empty? m))

                            :else
                            m))
                         cursor))

(defn update-map [m]
  (into {} (map (fn [[k v]] {k {:_value v}}) m)))

(defn unparse
  [profile]
  (clojure.walk/postwalk (fn [m]
                           (cond
                            (and (map? m)
                                 (not (empty? m))
                                 (every? (fn [v] (and (not (coll? v))
                                                      (not (nil? v))
                                                      (not (keyword? v)))) (vals m)))
                            (update-map m)
                            :else m))
                         profile))


(deftest unparse-test
  (testing "Unparse function"
    (println "Testing unparsing")

    (let [m1 {:timestamp 1
              :profile_data {:event_type "Intervention"
                             :footprint 1}
              :floors [{:floor_type 10}
                       {:construction 5}]}
          m2 {:bar 1
              :moo {:event 2
                    :ter 1}
              :floors [{:area 10}]}]

      (is (= {:timestamp 1
              :profile_data {:event_type {:_value "Intervention"}
                             :footprint {:_value 1}}
              :floors [{:floor_type {:_value 10}}
                       {:construction {:_value 5}}]} (unparse m1)))
      (is (= {:bar 1
              :moo {:event {:_value 2} :ter {:_value 1}}
              :floors [{:area {:_value 10}}]} (unparse m2))))))

(deftest parse-test
  (testing "Parse function"
    (println "Testing parsing")

    (let [m1 {:timestamp 1
              :profile_data {:event_type {:_value "Intervention"}
                             :footprint {:_value 1}
                             :ber {}}
              :floors [{:floor_type {:_value 10}
                        :construction {}}
                       {:uvalue {}, :construction {:_value 5}}],
              :roofs [{:roof_type {}}]}
          m2 {:bar 1
              :moo {:event {:_value 2}
                    :ter {:_value 1} :ber {} :project {}}
              :floors [{:area {:_value 10} :height {}}]
              :roofs [{:area {}} {:area {}}]}]
      (is (= {:timestamp 1
              :profile_data {:event_type "Intervention"
                             :footprint 1}
              :floors [{:floor_type 10}
                       {:construction 5}]} (parse m1)))
      (is (= {:bar 1
              :moo {:event 2
                    :ter 1}
              :floors [{:area 10}]} (parse m2))))))

(def value {:_value (s/one s/Str "s")})

(def profile
  {:timestamp value
   :profile_data {:ber value :ter {}}
   :conservatories [{:area value}]})

(defn gen-profile [] (sg/generate-examples profile))

(deftest parse-generated-profiles-test
  (testing
      (let [profiles (gen-profile)]
        (doseq [profile profiles]
          (let [parsed (parse profile)]
            (is (and (nil? (get-in parsed [:timestamp :_value]))
                     (nil? (get-in parsed [:conservatories :area :_value]))
                     (nil? (get-in parsed [:profile_data :ber :_value]))
                     (nil? (get-in parsed [:profile_data :ter :_value])))))
          (is (false? (and (nil? (get-in profile [:timestamp :_value]))
                           (nil? (get-in profile [:conservatories :area :_value]))
                           (nil? (get-in profile [:profile_data :ber :_value]))
                           (nil? (get-in profile [:profile_data :ter :_value])))))))))

(defn should-show-list? [profiles keys]
  (some (fn [profile] (-> profile (get-in keys) seq)) profiles))

(defn should-show-row-in-list? [field i profiles k]
  (some (fn [profile]
          (let [l (get-in profile field)
                size (count l)]
            (when (<= i size)
              (-> l (get i) (get k))))) profiles))

(defn longest-list [profiles field]
  (apply max (map #(count (get-in % field)) profiles)))

(defn profile-section-list [label-all label profiles field keys]
  (when (should-show-list? profiles field)
    (cons [:tr.active [:td {:col-span (inc (count profiles))} [:h4 label-all]]] ;; main label
          (let [items-in-list (longest-list profiles field)]
            (apply concat (keep (fn [i]
                                  (cons
                                   [:tr [:td {:col-span (inc (count profiles))} [:h5 label]]] ;; list label
                                   (keep (fn [{:keys [k v]}]
                                           (when (should-show-row-in-list? field i profiles k)
                                             (into [] (cons :tr (cons [:td v]
                                                                      (for [p profiles]
                                                                        (if (<= i (count (get-in p field)))
                                                                          [:td (get-in p (conj field i k) "")]
                                                                          [:td ""]))))))) keys)))
                                (range items-in-list)))))))

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
                         [:td {:col-span 3} [:h5 "Ventilation System"]]]
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
        (is (= expected (profile-section-list label-all label profiles field keys))))))
