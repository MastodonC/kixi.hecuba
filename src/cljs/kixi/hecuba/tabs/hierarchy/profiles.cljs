(ns kixi.hecuba.tabs.hierarchy.profiles
  (:require [clojure.string :as string]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.common :as common :refer (log)]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.tabs.hierarchy.profiles.forms :as pf]
            [cljs.core.async :refer [<! >! chan put!]]))

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
                timestamp (common/unparse-date  (:timestamp profile) "yyyy-MM-dd")
                download-href (str "/4/entities/" (:entity_id profile) "/profiles/" (:profile_id profile) "?type=csv")]
            [:div {:class (profile-column-width)}
             [:h3.text-center [:span category ]
              [:br ] [:small timestamp]]]))]))))

(defn profile-comparator [x y]
  (let [order (zipmap ["pre-retrofit" "planned retrofit" "post retrofit" ;; R4F
                       "as designed" "as built" ;; BPE
                       "intervention"] (range))
        x-ind (order (-> x (get-in [:profile_data :event_type] "") string/lower-case))
        y-ind (order (-> y (get-in [:profile_data :event_type] "") string/lower-case))]
    (if (and x-ind y-ind)
      (if (zero? (compare x-ind y-ind))
        (compare (:timestamp x) (:timestamp y))
        (compare x-ind y-ind))
      (compare (:timestamp x) (:timestamp y)))))

(defn should-show-row? [profiles keys]
  (some (fn [profile] (-> profile (get-in keys) str seq)) profiles))

(defn should-show? [profiles keys]
  (some (fn [profile] (some #(-> profile (get-in (conj [:profile_data] (:k %))) str seq) keys)) profiles))

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
                                   [:tr [:td {:col-span (inc (count profiles))} [:h5.subheader label]]] ;; list label
                                   (keep (fn [{:keys [k v]}]
                                           (when (should-show-row-in-list? field i profiles k)
                                             (into [] (cons :tr (cons [:td v]
                                                                      (for [p profiles]
                                                                        (if (<= i (count (get-in p field)))
                                                                          [:td (get-in p (conj field i k) "")]
                                                                          [:td ""]))))))) keys)))
                                (range items-in-list)))))))

(defn profile-section [label profiles keys]
  (when (should-show? profiles keys)
    (list [:tr.active [:td {:col-span (inc (count profiles))} [:h4 label]]]
          (for [{:keys [k v]} keys]
            (when (should-show-row? profiles [:profile_data k])
              [:tr
               [:td v]
               (for [p profiles]
                 ;; TOFIX Profiles contain URLs that are long and not broken into multiple lines inside of a bootstrap table.
                 ;; I've tried several solution and only the below or setting word-wrap to "break-all" worked. "Break-all" doesn't require setting min and max on the column
                 ;; and can be used with col-md-x to specify width. But it works on the entire table which means all words are being broken.
                 [:td {:style {:word-wrap "break-word" :min-width "200px" :max-width "200px"}} (get-in p [:profile_data k] "")])])))))

(defn profile-header [profile]
  [:th.col-md-3 {:style {:word-wrap "break-word"}}
   (when (:editable profile)
     [:button {:type "button"
               :class "btn btn-primary btn-xs"
               :on-click (fn [_] (set! (.-location js/window) (str "/profile/" (:entity_id profile) "/" (:profile_id profile))))}
      [:div {:class "fa fa-pencil-square-o"}]])
   (get-in profile [:profile_data :event_type] "")])

(defn profiles-div [property-details owner]
  (reify
    om/IRender
    (render [_]
      (let [selected-property-id (:entity_id property-details)
            editable             (:editable property-details)
            profiles             (map #(assoc % :editable editable)
                                      (sort profile-comparator (:profiles property-details)))]
        (html
         [:div
          [:h3 "Profiles"
           (when editable
             [:div.btn-toolbar.pull-right
              [:button {:type "button"
                        :title "Add new profile"
                        :class (str "btn btn-primary fa fa-plus")
                        :onClick (fn [_]  (set! (.-location js/window) (str "/profile/" selected-property-id)))}]
              (when (seq profiles)
                [:a {:role "button"
                     :class "btn btn-primary fa fa-download"
                     :title "Download Profiles"
                     :href (str "/4/entities/" selected-property-id "/profiles/?type=csv")}])])]
          [:div {:id "alert"} (om/build bs/alert (:alert profiles))]
          [:div.table-responsive
           (if (seq profiles)
             [:table.table.table-condensed
              [:thead
               [:tr
                [:th.col-md-4 ""]
                (for [profile profiles]
                  (profile-header profile))]]
              [:tbody
               (profile-section "Description" profiles (:description pf/fields))
               (profile-section "Occupancy" profiles (:occupancy pf/fields))
               (profile-section "Measurements" profiles (:measurements pf/fields))
               (profile-section "Energy" profiles (:energy pf/fields))
               (profile-section "PassivHaus" profiles (:passivhaus pf/fields))
               (profile-section "Efficiency" profiles (:efficiency pf/fields))
               (profile-section "Flats" profiles (:flats pf/fields))
               (profile-section "Fireplaces" profiles (:fireplaces pf/fields))
               (profile-section "Glazing" profiles (:glazing pf/fields))
               (profile-section "Issues" profiles (:issues pf/fields))
               (profile-section "Lessons Learnt" profiles (:lessons-learnt pf/fields))
               (profile-section "Project Details" profiles (:project-details pf/fields))
               (profile-section "Coheating Test" profiles (:coheating-test pf/fields))
               (profile-section "Dwelling U-values Summary" profiles (:dwelling-summary pf/fields))
               (profile-section "Air Tightness Test" profiles (:air-tightness-test pf/fields))
               (profile-section "BUS Survey Information" profiles (:bus-survey pf/fields))
               (profile-section "SAP Results" profiles (:sap-results pf/fields))
               (profile-section "Energy Costs" profiles (:energy-costs pf/fields))

               ;; Fields containing lists

               (profile-section-list "All Conservatories" "Conservatory" profiles
                                     [:conservatories] (:conservatories pf/fields))
               (profile-section-list "All Extensions" "Extenstion" profiles
                                     [:extensions] (:extensions pf/fields))
               (profile-section-list "All Heating Systems" "Heating System" profiles
                                     [:heating_systems] (:heating_systems pf/fields))
               (profile-section-list "All Hot Water Systems" "Hot Water System" profiles
                                     [:hot_water_systems] (:hot_water_systems pf/fields))
               (profile-section-list "All Storeys" "Storey" profiles
                                     [:storeys] (:storeys pf/fields))
               (profile-section-list "All Walls" "Wall" profiles
                                     [:walls] (:walls pf/fields))
               (profile-section-list "All Roofs" "Roof" profiles
                                     [:roofs] (:roofs pf/fields))
               (profile-section-list "All Window Sets" "Window Set" profiles
                                     [:window_sets] (:window_sets pf/fields))
               (profile-section-list "All Door Sets" "Door Set" profiles
                                     [:door_sets] (:door_sets pf/fields))
               (profile-section-list "All Floors" "Floor" profiles
                                     [:floors] (:floors pf/fields))
               (profile-section-list "All Roof Rooms" "Roof Rooms" profiles
                                     [:roof_rooms] (:roof_rooms pf/fields))
               (profile-section-list "All Low Energy Lights" "Low Energy Lights" profiles
                                     [:low_energy_lights] (:low_energy_lights pf/fields))
               (profile-section-list "All Ventilation Systems" "Ventilation System" profiles
                                     [:ventilation_systems] (:ventilation_systems pf/fields))
               (profile-section-list "All Air Flow Measurements" "Air Flow Measurements" profiles
                                     [:airflow_measurements] (:airflow_measurements pf/fields))
               (profile-section-list "All Photovoltaic Panels" "Photovoltaic Panels" profiles
                                     [:photovoltaics] (:photovoltaics pf/fields))
               (profile-section-list "All Solar Thermal Panels" "Solar Thermal Panels" profiles
                                     [:solar_thermals] (:solar_thermals pf/fields))
               (profile-section-list "All Wind Turbines" "Wind Turbines" profiles
                                     [:wind_turbines] (:wind_turbines pf/fields))
               (profile-section-list "All Small Hydro Plants" "Small Hydro Plants" profiles
                                     [:small_hydros] (:small_hydros pf/fields))
               (profile-section-list "All Heat Pumps" "Heat Pumps" profiles
                                     [:heat_pumps] (:heat_pumps pf/fields))
               (profile-section-list "All Biomass Boilers" "Biomass Boilers" profiles
                                     [:biomasses] (:biomasses pf/fields))
               (profile-section-list "All mCHP Systems" "mCHP Systems" profiles
                                     [:chps] (:chps pf/fields))]]

             [:div.col-md-12.text-center
              [:p.lead {:style {:padding-top 30}}
               "No profile properties to display"]])]])))))
