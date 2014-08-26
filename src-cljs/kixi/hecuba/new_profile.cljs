(ns kixi.hecuba.new-profile
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [clojure.string :as string]
            [kixi.hecuba.common :as common :refer (log)]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.profiles.app-model :as model]
            [kixi.hecuba.profiles.panels :as panels]))

(defn panel-heading [cursor title key]
  [:div.btn-toolbar
   title
   [:button {:type "button" :class (str "btn btn-primary btn-xs pull-right ")
             :on-click (fn [_]
                         (om/transact! cursor key #(conj % (-> model/profile-schema key first))))}
    [:div {:class "fa fa-plus"}]]])

(defn non-empty? [n] (if (coll? n) (seq n) n))

(defn parse
  "Remove all empty elements from the nested data structure and flatten :_value elements.
  Since postwalk will check elements like [:some-key], there is a check against tuples:
 (and (coll? m) (not (keyword (first m))))."
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

(defn post-new-profile [cursor profile]
  (let [[_ _ entity_id] (string/split js/window.location.pathname #"/")
        url             (str "/4/entities/" entity_id "/profiles/")]
    (common/post-resource url (-> profile
                                  (assoc :entity_id entity_id)
                                  (dissoc :alert))
                          (fn [response]
                            (om/update! cursor :alert {:status true
                                                       :class "alert alert-success"
                                                       :text "Profile added successfully."})
                            (.back js/history))
                          (fn [{:keys [status status-text]}]
                            (om/update! cursor :alert {:status true
                                                       :class "alert alert-danger"
                                                       :text status-text})))))

(defn post-if-valid [cursor profile]
  (if (-> profile :profile_data :event_type seq)
    (post-new-profile cursor profile)
    (om/update! cursor :alert {:status true
                               :class "alert alert-danger"
                               :text "Please enter event type."})))

(defn new-profile-form [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:active-item :description})
    om/IRenderState
    (render-state [_ state]
      (html
       (let [active-item (om/get-state owner :active-item)]
         [:div
          [:div.col-md-12
           [:h3 "Adding new profile"]
           [:div.btn-toolbar {:style {:padding-bottom "15px"}}
            [:button {:type "button"
                      :class "btn btn-success"
                      :onClick (fn [_] (let [profile (parse @cursor)]
                                         (log profile)
                                         (post-if-valid cursor profile)))}
             "Save"]
            [:button {:type "button"
                      :class "btn btn-danger"
                      :onClick (fn [_] (set! (.-location js/window) "/app"))}
             "Cancel"]]]

          [:div.col-md-12 {:id "alert-div" :style {:padding-top "10px"}}
           (om/build bs/alert (:alert cursor))]

          [:div.col-md-12
           [:form.form-horizontal {:role "form"}
            [:div.form-group
             [:label.control-label.col-md-1 {:for "event_type" :style {:text-align "left"}} "Event Type"]
             [:div {:class "required col-md-3"}
              [:input {:id "event-type"
                       :class "form-control"
                       :default-value (-> cursor :profile_data :event_type :_value)
                       :on-change #(om/update! cursor [:profile_data :event_type :_value] (.-value (.-target %)))}]]]
            [:div.form-group
             [:label.control-label.col-md-1 {:for "event_type" :style {:text-align "left"}} "Timestamp"]
             [:div {:class "col-md-3"}
              [:input {:id "event-type"
                       :class "form-control"
                       :default-value (-> cursor :timestamp :_value)
                       :on-change #(om/update! cursor [:timestamp :_value] (.-value (.-target %)))}]]]]]
          [:div
           [:div.col-md-4
            [:div.list-group {:style {:height "400px" :overflow-y "auto"}}
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :description))
                  :class (str "list-group-item " (when (= active-item :description) "active"))}
              "Description"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :occupancy))
                  :class (str "list-group-item " (when (= active-item :occupancy) "active"))}
              "Occupancy"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :measurements))
                  :class (str "list-group-item " (when (= active-item :measurements) "active"))}
              "Measurements"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :passivhaus))
                  :class (str "list-group-item " (when (= active-item :passivhaus) "active"))}
              "PassivHaus"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :energy))
                  :class (str "list-group-item " (when (= active-item :energy) "active"))}
              "Energy"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :efficiency))
                  :class (str "list-group-item " (when (= active-item :efficiency) "active"))}
              "Efficiency"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :flats))
                  :class (str "list-group-item " (when (= active-item :flats) "active"))}
              "Flats"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :fireplaces))
                  :class (str "list-group-item " (when (= active-item :fireplaces) "active"))}
              "Fireplaces"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :glazing))
                  :class (str "list-group-item " (when (= active-item :glazing) "active"))}
              "Glazing"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :issues))
                  :class (str "list-group-item " (when (= active-item :issues) "active"))}
              "Issues"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :lesson-learnt))
                  :class (str "list-group-item " (when (= active-item :lesson-learnt) "active"))}
              "Lesson Learnt"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :project-details))
                  :class (str "list-group-item " (when (= active-item :project-details) "active"))}
              "Project Details"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :coheating-test))
                  :class (str "list-group-item " (when (= active-item :coheating-test) "active"))}
              "Coheating Test"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :dwelling))
                  :class (str "list-group-item " (when (= active-item :dwelling) "active"))}
              "Dwelling U-values Summary"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :air-tightness))
                  :class (str "list-group-item " (when (= active-item :air-tightness) "active"))}
              "Air Tightness Test"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :bus))
                  :class (str "list-group-item " (when (= active-item :bus) "active"))}
              "BUS Survey Information"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :sap))
                  :class (str "list-group-item " (when (= active-item :sap) "active"))}
              "SAP Results"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :conservatory))
                  :class (str "list-group-item " (when (= active-item :conservatory) "active"))}
              "Conservatories"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :extension))
                  :class (str "list-group-item " (when (= active-item :extension) "active"))}
              "Extensions"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :heating-system))
                  :class (str "list-group-item " (when (= active-item :heating-system) "active"))}
              "Heating Systems"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :hot-water))
                  :class (str "list-group-item " (when (= active-item :hot-water) "active"))}
              "Hot Water Systems"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :storey))
                  :class (str "list-group-item " (when (= active-item :storey) "active"))}
              "Storeys"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :wall))
                  :class (str "list-group-item " (when (= active-item :wall) "active"))}
              "Walls"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :roof))
                  :class (str "list-group-item " (when (= active-item :roof) "active"))}
              "Roofs"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :window-set))
                  :class (str "list-group-item " (when (= active-item :window-set) "active"))}
              "Window Sets"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :door-set))
                  :class (str "list-group-item " (when (= active-item :door-set) "active"))}
              "Door Sets"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :floor))
                  :class (str "list-group-item " (when (= active-item :floor) "active"))}
              "Floors"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :roof-room))
                  :class (str "list-group-item " (when (= active-item :roof-room) "active"))}
              "Roof Rooms"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :light))
                  :class (str "list-group-item " (when (= active-item :light) "active"))}
              "Low Energy Lights"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :ventilation))
                  :class (str "list-group-item " (when (= active-item :ventilation) "active"))}
              "Ventilation Systems"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :air-flow))
                  :class (str "list-group-item " (when (= active-item :air-flow) "active"))}
              "Air Flow Measurements"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :photovoltaics))
                  :class (str "list-group-item " (when (= active-item :photovoltaics) "active"))}
              "Photovoltaic Panels"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :solar))
                  :class (str "list-group-item " (when (= active-item :solar) "active"))}
              "Solar Thermal Panels"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :wind))
                  :class (str "list-group-item " (when (= active-item :wind) "active"))}
              "Wind Turbines"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :small-hydro-plant))
                  :class (str "list-group-item " (when (= active-item :small-hydro-plant) "active"))}
              "Small Hydro Plants"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :heat-pump))
                  :class (str "list-group-item " (when (= active-item :heat-pump) "active"))}
              "Heat Pumps"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :biomass))
                  :class (str "list-group-item " (when (= active-item :biomass) "active"))}
              "Biomass Boilers"]
             [:a {:on-click (fn [_] (om/set-state! owner :active-item :chp))
                  :class (str "list-group-item " (when (= active-item :chp) "active"))}
              "mCHP Systems"]]]
           [:div.col-md-8
            (when (= active-item :description)
              (bs/panel "Description" (panels/description (:profile_data cursor))))

            (when (= active-item :occupancy)
              (bs/panel "Occupancy" (panels/occupancy (:profile_data cursor))))

            (when (= active-item :measurements)
              (bs/panel "Measurements" (panels/measurements (:profile_data cursor))))

            (when (= active-item :energy)
              (bs/panel "Energy" (panels/energy (:profile_data cursor))))

            (when (= active-item :passivhaus)
              (bs/panel "PassivHaus" (panels/passivehaus (:profile_data cursor))))

            (when (= active-item :efficiency)
              (bs/panel "Efficiency" (panels/efficiency (:profile_data cursor))))

            (when (= active-item :flats)
              (bs/panel "Flats" (panels/flats (:profile_data cursor))))

            (when (= active-item :fireplaces)
              (bs/panel "Fireplaces" (panels/fireplaces (:profile_data cursor))))

            (when (= active-item :glazing)
              (bs/panel "Glazing" (panels/glazing (:profile_data cursor))))

            (when (= active-item :issues)
              (bs/panel "Issues" (panels/issues (:profile_data cursor))))

            (when (= active-item :lesson-learnt)
              (bs/panel "Lesson Learnt" (panels/lessons-learnt (:profile_data cursor))))

            (when (= active-item :project-details)
              (bs/panel "Project Details" (panels/project-details (:profile_data cursor))))

            (when (= active-item :coheating-test)
              (bs/panel "Coheating Test" (panels/coheating-test (:profile_data cursor))))

            (when (= active-item :dwelling)
              (bs/panel "Dwelling U-values Summary" (panels/dwelling-u-values-summary (:profile_data cursor))))

            (when (= active-item :air-tightness)
              (bs/panel "Air Tightness Test" (panels/air-tightness-test (:profile_data cursor))))

            (when (= active-item :bus)
              (bs/panel "BUS Survey Information" (panels/bus-survey-information (:profile_data cursor))))

            (when (= active-item :sap)
              (bs/panel "SAP Results" (panels/sap-results (:profile_data cursor))))

            (when (= active-item :conservatory)
              (bs/panel (panel-heading cursor "Conservatories" :conservatories)
                        (panels/conservatory (:conservatories cursor))))

            (when (= active-item :extension)
              (bs/panel (panel-heading cursor "Extensions" :extensions)
                        (panels/extension (:extensions cursor))))

            (when (= active-item :heating-system)
              (bs/panel (panel-heading cursor "Heating Systems" :heating_systems)
                        (panels/heating-system (:heating_systems cursor))))

            (when (= active-item :hot-water)
              (bs/panel (panel-heading cursor "Hot Water Systems" :hot_water_systems)
                        (panels/hot-water-system (:hot_water_systems cursor))))

            (when (= active-item :storey)
              (bs/panel (panel-heading cursor "Storeys" :storeys)
                        (panels/storey (:storeys cursor))))

            (when (= active-item :wall)
              (bs/panel (panel-heading cursor "Walls" :walls)
                        (panels/wall (:walls cursor))))

            (when (= active-item :roof)
              (bs/panel (panel-heading cursor "Roofs" :roofs)
                        (panels/roof (:roofs cursor))))

            (when (= active-item :window-set)
              (bs/panel (panel-heading cursor "Window Sets" :window_sets)
                        (panels/window (:window_sets cursor))))

            (when (= active-item :door-set)
              (bs/panel (panel-heading cursor "Door Sets" :door_sets)
                        (panels/door (:door_sets cursor))))

            (when (= active-item :floor)
              (bs/panel (panel-heading cursor "Floors" :floors)
                        (panels/floor (:floors cursor))))

            (when (= active-item :roof-room)
              (bs/panel (panel-heading cursor "Roof Rooms" :roof_rooms)
                        (panels/roof-room (:roof_rooms cursor))))

            (when (= active-item :light)
              (bs/panel (panel-heading cursor "Low Energy Lights" :low_energy_lights)
                        (panels/low-energy-lights (:low_energy_lights cursor))))

            (when (= active-item :ventilation)
              (bs/panel (panel-heading cursor "Ventilation Systems" :ventilation_systems)
                        (panels/ventilation-system (:ventilation_systems cursor))))

            (when (= active-item :air-flow)
              (bs/panel (panel-heading cursor "Air Flow Measurements" :airflow_measurements)
                        (panels/airflow-measurement (:airflow_measurements cursor))))

            (when (= active-item :photovoltaics)
              (bs/panel (panel-heading cursor "Photovoltaic Panels" :photovoltaics)
                        (panels/photovoltaic-panel (:photovoltaics cursor))))

            (when (= active-item :solar)
              (bs/panel (panel-heading cursor "Solar Thermal Panels" :solar_thermals)
                        (panels/solar-thermal-panel (:solar_thermals cursor))))

            (when (= active-item :wind)
              (bs/panel (panel-heading cursor "Wind Turbines" :wind_turbines)
                        (panels/wind-turbine (:wind_turbines cursor))))

            (when (= active-item :small-hydro-plant)
              (bs/panel (panel-heading cursor "Small Hydro Plants" :small_hydros)
                        (panels/small-hydros-plant (:small_hydros cursor))))

            (when (= active-item :heat-pump)
              (bs/panel (panel-heading cursor "Heat Pumps" :heat-pump)
                        (panels/heat-pump (:heat-pump cursor))))

            (when (= active-item :biomass)
              (bs/panel (panel-heading cursor "Biomass Boilers" :biomasses)
                        (panels/biomass-boiler (:biomasses cursor))))

            (when (= active-item :chp)
              (bs/panel (panel-heading cursor "mCHP Systems" :chps)
                        (panels/mCHP-system (:chps cursor))))]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view of new profile page

(defn new-profile-view [data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div {:style {:padding-top "10px"}}
        (om/build new-profile-form (:new-profile data))]))))

(when-let [new-profile (.getElementById js/document "new-profile")]
  (om/root new-profile-view
           model/app-model
           {:target new-profile}))
