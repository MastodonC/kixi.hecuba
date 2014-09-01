(ns kixi.hecuba.tabs.hierarchy.profiles
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.common :as common :refer (log)]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.tabs.hierarchy.profiles.forms :as pf]
            [cljs.core.async :refer [<! >! chan put!]]))

(defn merge-element [profile edited-profile keys]
  (if (and (= 0 (last keys)) (= (count (get-in profile (drop-last keys))) 0))
    (assoc-in profile (into [] (drop-last keys)) [(get-in edited-profile keys)])
    (assoc-in profile keys (merge (get-in profile keys) (get-in edited-profile keys)))))

(defn put-profile [properties refresh-chan profile edited-profile keys]
  (let [entity_id  (-> @properties :selected)
        profile_id (:profile_id profile)
        resource   (merge-element profile edited-profile keys)]
    (common/put-resource (str "/4/entities/" entity_id "/profiles/" profile_id)
                         (-> resource
                             (assoc :entity_id entity_id)
                             (dissoc :editing :timestamp :adding))
                         (fn [_] (put! refresh-chan {:event :property}))
                         (fn [{:keys [status status-text]}]
                           (om/update! properties [:profiles :alert]
                                       {:status true
                                        :class "alert alert-danger"
                                        :text status-text})))))

(defn get-profiles [selected-property-id properties]
  (->>  properties
        :data
        (filter #(= (:entity_id %) selected-property-id))
        first
        :profiles))

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
                timestamp (common/unparse-date  (:timestamp profile) "yyyy-MM-dd")]
            [:div {:class (profile-column-width)}
             [:h3.text-center category [:br ] [:small timestamp]]]))]))))

(defn panel-heading [properties owner profile title {:keys [add-btn edit-btn]} keys]
  (let [{:keys [editing adding]} (om/get-state owner)
        editable (:editable profile)
        refresh-chan (om/get-shared owner :refresh)]
    [:div.btn-toolbar
     title
     (when (and edit-btn editable)
       [:button {:type "button" :class (str "btn btn-primary btn-xs pull-right "
                                            (when (or adding editing) "hidden"))
                 :on-click (fn [_] (om/set-state! owner :editing true))}
        [:div {:class "fa fa-pencil-square-o"}]])
     (when (and add-btn editable)
       [:button {:type "button" :class (str "btn btn-primary btn-xs pull-right "
                                            (when (or adding editing) "hidden"))
                 :on-click (fn [_] (om/set-state! owner :adding true))}
        [:div {:class "fa fa-plus"}]])
     [:button {:type "button" :class (str "btn btn-danger btn-xs pull-right "
                                          (if (or (and adding add-btn)
                                                  (and editing edit-btn)) "" "hidden"))
               :on-click (fn [_]
                           (om/set-state! owner :editing false)
                           (om/set-state! owner :adding false))} "Cancel"]
     [:button {:type "button" :class (str "btn btn-success btn-xs pull-right "
                                          (if (or (and editing edit-btn)
                                                  (and adding add-btn)) "" "hidden"))
               :on-click (fn [_]
                           (let [edited-properties (om/get-state owner)
                                 entity_id (-> @properties :selected)]
                             (put-profile properties refresh-chan @profile edited-properties keys)
                             (om/set-state! owner :editing false)
                             (om/set-state! owner :adding false)))} "Save"]]))

(defn description-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [keys [:profile_data]]
               (bs/panel
                (panel-heading properties owner profile "Description" {:add-btn false :edit-btn true} keys)
                (pf/description owner profile keys)))])])))))

(defn occupancy-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Occupancy" {:add-btn false :edit-btn true} keys)
                (pf/occupancy owner profile keys))]))])))))

(defn measurements-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Measurements" {:add-btn false :edit-btn true} keys)
                (pf/measurements owner profile keys))]))])))))

(defn energy-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Energy" {:add-btn false :edit-btn true} keys)
                (pf/energy owner profile keys))]))])))))

(defn passivhaus-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "PassivHaus" {:add-btn false :edit-btn true} keys)
               (pf/passivhaus owner profile keys))]))])))))

(defn efficiency-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Efficiency" {:add-btn false :edit-btn true} keys)
                (pf/efficiency owner profile keys))]))])))))

(defn flats-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Flats" {:add-btn false :edit-btn true} keys)
                (pf/flats owner profile keys))]))])))))

(defn fireplaces-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Fireplaces" {:add-btn false :edit-btn true} keys)
               (pf/fireplaces owner profile keys))]))])))))

(defn glazing-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Glazing" {:add-btn false :edit-btn true} keys)
                (pf/glazing owner profile keys))]))])))))

(defn issues-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Issues" {:add-btn false :edit-btn true} keys)
                (pf/issues owner profile keys))]))])))))

(defn lessons-learnt-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Lesson Learnt" {:add-btn false :edit-btn true} keys)
                (pf/lessons-learnt owner profile keys))]))])))))

(defn project-details-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Project Details" {:add-btn false :edit-btn true} keys)
                (pf/project-details owner profile keys))]))])))))

(defn coheating-test-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Coheating Test" {:add-btn false :edit-btn true} keys)
                (pf/coheating-test owner profile keys))]))])))))

(defn dwelling-u-values-summary-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Dwelling U-values Summary" {:add-btn false :edit-btn true} keys)
                (pf/dwelling-u-values-summary owner profile keys))]))])))))

(defn air-tightness-test-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "Air Tightness Test" {:add-btn false :edit-btn true} keys)
                (pf/air-tightness-test owner profile keys))]))])))))

(defn bus-survey-information-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "BUS Survey Information" {:add-btn false :edit-btn true} keys)
                (pf/bus-survey-information owner profile keys))]))])))))

(defn sap-results-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            (let [keys [:profile_data]]
              [:div {:class (profile-column-width)}
               (bs/panel
                (panel-heading properties owner profile "SAP Results" {:add-btn false :edit-btn true} keys)
                (pf/sap-results owner profile keys))]))])))))

;; Fields containing lists

(defn conservatories-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding    (om/get-state owner :adding)
                   position  (count (:conservatories profile))
                   key       [:conservatories position]]
               (bs/panel
                (panel-heading properties owner profile "Conservatories" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/conservatory owner profile key)
                  (if-let [conservatories (seq (:conservatories profile))]
                    (let [profile (assoc profile :conservatories (vec conservatories))]
                      :div
                      (for [c conservatories]
                        (let [keys [:conservatories (.indexOf (to-array conservatories) c)]]
                          (bs/panel
                           (panel-heading properties owner profile "Conservatory" {:add-btn false :edit-btn true} keys)
                           (pf/conservatory owner profile keys)))))
                    [:p "No conservatories."]))))])])))))

(defn extensions-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding    (om/get-state owner :adding)
                   position  (count (:extensions profile))
                   key       [:extensions position]]
               (bs/panel
                (panel-heading properties owner profile "Extensions" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/extension owner profile key)
                  (if-let [extensions (seq (:extensions profile))]
                    (let [profile (assoc profile :extensions (vec extensions))]
                      [:div
                       (for [item extensions]
                         (let [keys [:extensions (.indexOf (to-array extensions) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Extension" {:add-btn false :edit-btn true} keys)
                            (pf/extension owner profile keys))))])
                    [:p "No extensions."]))))])])))))

(defn heating-systems-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:heating_systems profile))
                   key      [:heating_systems position]]
               (bs/panel
                (panel-heading properties owner profile "Heating Systems" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/heating-system owner profile key)
                  (if-let [heating-systems (seq (:heating_systems profile))]
                    (let [profile (assoc profile :heating_systems (vec heating-systems))]
                      [:div
                       (for [item heating-systems]
                         (let [keys [:heating_systems (.indexOf (to-array heating-systems) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Heating System" {:add-btn false :edit-btn true} keys)
                            (pf/heating-system owner profile keys))))])
                    [:p "No heating systems."]))))])])))))

(defn hot-water-systems-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:hot_water_systems profile))
                   key      [:hot_water_systems position]]
               (bs/panel
                (panel-heading properties owner profile "Hot Water Systems" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/hot-water-system owner profile key)
                  (if-let [hot-water-systems (seq (:hot_water_systems profile))]
                    (let [profile (assoc profile :hot_water_systems (vec hot-water-systems))]
                      [:div
                       (for [item hot-water-systems]
                         (let [keys [:hot_water_systems (.indexOf (to-array hot-water-systems) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Hot Water System" {:add-btn false :edit-btn true} keys)
                            (pf/hot-water-system owner profile keys))))])
                    [:p "No hot water systems."]))))])])))))

(defn storeys-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:storeys profile))
                   key      [:storeys position]]
               (bs/panel
                (panel-heading properties owner profile "Storeys" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/storey owner profile key)
                  (if-let [storeys (seq (:storeys profile))]
                    (let [profile (assoc profile :storeys (vec storeys))]
                      [:div
                       (for [item storeys]
                         (let [keys [:storeys (.indexOf (to-array storeys) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Storey" {:add-btn false :edit-btn true} keys)
                            (pf/storey owner profile keys))))])
                      [:p "No storeys recorded."]))))])])))))

(defn walls-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:walls profile))
                   key      [:walls position]]
               (bs/panel
                (panel-heading properties owner profile "Walls" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/wall owner profile key)
                  (if-let [walls   (seq (:walls profile))]
                    (let [profile (assoc profile :walls (vec walls))]
                      [:div
                       (for [item walls]
                         (let [keys [:walls (.indexOf (to-array walls) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Wall" {:add-btn false :edit-btn true} keys)
                            (pf/wall owner profile keys))))])
                    [:p "No walls recorded."]))))])])))))

(defn roofs-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:roofs profile))
                   key      [:roofs position]]
               (bs/panel
                (panel-heading properties owner profile "Roofs" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/roof owner profile key)
                  (if-let [roofs (seq (:roofs profile))]
                    (let [profile (assoc profile :roofs (vec roofs))]
                      [:div
                       (for [item roofs]
                         (let [keys [:roofs (.indexOf (to-array roofs) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Roof" {:add-btn false :edit-btn true} keys)
                            (pf/roof owner profile keys))))])
                    [:p "No roofs recorded."]))))])])))))

(defn window-sets-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:window_sets profile))
                   key      [:window_sets position]]
               (bs/panel
                (panel-heading properties owner profile "Window Sets" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/window owner profile key)
                  (if-let [window-sets (seq (:window_sets profile))]
                    (let [profile (assoc profile :window_sets (vec window-sets))]
                      [:div
                       (for [item window-sets]
                         (let [keys [:window_sets (.indexOf (to-array window-sets) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Window Set" {:add-btn false :edit-btn true} keys)
                            (pf/window owner profile keys))))])
                    [:p "No window sets recorded."]))))])])))))

(defn door-sets-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding (om/get-state owner :adding)
                   position (count (:door_sets profile))
                   key [:door_sets position]]
               (bs/panel
                (panel-heading properties owner profile "Door Sets" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/door owner profile key)
                  (if-let [door-sets (seq (:door_sets profile))]
                    (let [profile (assoc profile :door_sets (vec door-sets))]
                      [:div
                       (for [item door-sets]
                         (let [keys [:door_sets (.indexOf (to-array door-sets) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Door Set" {:add-btn false :edit-btn true} keys)
                            (pf/door owner profile keys))))])
                    [:p "No door sets recorded."]))))])])))))

(defn floors-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:floors profile))
                   key      [:floors position]]
               (bs/panel
                (panel-heading properties owner profile "Floors" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/floor owner profile key)
                  (if-let [floors (seq (:floors profile))]
                    (let [profile (assoc profile :floors (vec floors))]
                      [:div
                       (for [item floors]
                         (let [keys [:floors (.indexOf (to-array floors) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Floor" {:add-btn false :edit-btn true} keys)
                            (pf/floor owner profile keys))))])
                    [:p "No floors recorded."]))))])])))))

(defn roof-rooms-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:roof_rooms profile))
                   key      [:roof_rooms position]]
               (bs/panel
                (panel-heading properties owner profile "Roof Rooms" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/roof-room owner profile key)
                  (if-let [roof-rooms (seq (:roof_rooms profile))]
                    (let [profile (assoc profile :roof_rooms (vec roof-rooms))]
                      [:div
                       (for [item roof-rooms]
                         (let [keys [:roof_rooms (.indexOf (to-array roof-rooms) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Roof Room" {:add-btn false :edit-btn true} keys)
                            (pf/roof-room owner profile keys))))])
                    [:p "No roof rooms recorded."]))))])])))))

(defn low-energy-lights-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:low_energy_lights profile))
                   key      [:low_energy_lights position]]
               (bs/panel
                (panel-heading properties owner profile "Low Energy Lights" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/low-energy-lights owner profile key)
                  (if-let [low-energy-lights (seq (:low_energy_lights profile))]
                    (let [profile (assoc profile :low_energy_lights (vec low-energy-lights))]
                      [:div
                       (for [item low-energy-lights]
                         (let [keys [:low_energy_lights (.indexOf (to-array low-energy-lights) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Low Energy Light" {:add-btn false :edit-btn true} keys)
                            (pf/low-energy-lights owner profile keys))))])
                    [:p "No low energy lights recorded."]))))])])))))

(defn ventilation-systems-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:ventilation_systems profile))
                   key      [:ventilation_systems position]]
               (bs/panel
                (panel-heading properties owner profile "Ventilation Systems" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/ventilation-system owner profile key)
                  (if-let [ventilation-systems (seq (:ventilation_systems profile))]
                    (let [profile (assoc profile :ventilation_systems (vec ventilation-systems))]
                      [:div
                       (for [item ventilation-systems]
                         (let [keys [:ventilation_systems (.indexOf (to-array ventilation-systems) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Ventilation System" {:add-btn false :edit-btn true} keys)
                            (pf/ventilation-system owner profile keys))))])
                    [:p "No ventilation systems lights recorded."]))))])])))))

(defn airflow-measurements-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:airflow_measurements profile))
                   key      [:airflow_measurements position]]
               (bs/panel
                (panel-heading properties owner profile "Air Flow Measurements" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/airflow-measurement owner profile key)
                  (if-let [airflow-measurements (seq (:airflow_measurements profile))]
                    (let [profile (assoc profile :airflow_measurements (vec airflow-measurements) )]
                      [:div
                       (for [item airflow-measurements]
                         (let [keys [:airflow_measurements (.indexOf (to-array airflow-measurements) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Air Flow Measurements" {:add-btn false :edit-btn true} keys)
                            (pf/airflow-measurement owner profile keys))))])
                    [:p "No air flow measurements recorded."]))))])])))))

(defn photovoltaic-panels-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:photovoltaics profile))
                   key      [:photovoltaics position]]
               (bs/panel
                (panel-heading properties owner profile "Photovoltaic Panels" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/photovoltaic-panel owner profile key)
                  (if-let [photovoltaic-panels (seq (:photovoltaics profile))]
                    (let [profile (assoc profile :photovoltaics (vec photovoltaic-panels))]
                      [:div
                       (for [item photovoltaic-panels]
                         (let [keys [:photovoltaics (.indexOf (to-array photovoltaic-panels) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Photovoltaic Panel" {:add-btn false :edit-btn true} keys)
                            (pf/photovoltaic-panel owner profile keys))))])
                    [:p "No Photovoltaic-Panels recorded."]))))])])))))

(defn solar-thermal-panels-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:solar_thermals profile))
                   key      [:solar_thermals position]]
               (bs/panel
                (panel-heading properties owner profile "Solar Thermal Panels" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/solar-thermal-panel owner profile key)
                  (if-let [solar-thermal-panels (seq (:solar_thermals profile))]
                    (let [profile (assoc profile :solar_thermals (vec solar-thermal-panels))]
                      [:div
                       (for [item solar-thermal-panels]
                         (let [keys [:solar_thermals (.indexOf (to-array solar-thermal-panels) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Solar Thermal Panel" {:add-btn false :edit-btn true} keys)
                            (pf/solar-thermal-panel owner profile keys))))])
                    [:p "No solar thermal panels recorded."]))))])])))))

(defn wind-turbines-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:wind_turbines profile))
                   key      [:wind_turbines position]]
               (bs/panel
                (panel-heading properties owner profile "Wind Turbines" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/wind-turbine owner profile key)
                  (if-let [wind-turbines (seq (:wind_turbines profile))]
                    (let [profile (assoc profile :wind_turbines (vec wind-turbines))]
                      [:div
                       (for [item wind-turbines]
                         (let [keys [:wind_turbines (.indexOf (to-array wind-turbines) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Wind Turbine" {:add-btn false :edit-btn true} keys)
                            (pf/wind-turbine owner profile keys))))])
                    [:p "No wind turbines recorded."]))))])])))))

(defn small-hydro-plants-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:small_hydros profile))
                   key      [:small_hydros position]]
               (bs/panel
                (panel-heading properties owner profile "Small Hydro Plants" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/small-hydros-plant owner profile key)
                  (if-let [small-hydro-plants (seq (:small_hydros profile))]
                    (let [profile (assoc profile :small_hydros (vec small-hydro-plants))]
                      [:div
                       (for [item small-hydro-plants]
                         (let [keys [:small_hydros (.indexOf (to-array small-hydro-plants) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Small Hydro Plant" {:add-btn false :edit-btn true} keys)
                            (pf/small-hydros-plant owner profile keys))))])
                    [:p "No small hydro plants recorded."]))))])])))))

(defn heat-pumps-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding (om/get-state owner :adding)
                   position (count (:heat_pumps profile))
                   key [:heat_pumps position]]
               (bs/panel
                (panel-heading properties owner profile "Heat Pumps" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/heat-pump owner profile key)
                  (if-let [heat-pumps (seq (:heat_pumps profile))]
                    (let [profile (assoc profile :heat_pumps (vec heat-pumps))]
                      [:div
                       (for [item heat-pumps]
                         (let [keys [:heat_pumps (.indexOf (to-array heat-pumps) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Heat Pump" {:add-btn false :edit-btn true} keys)
                            (pf/heat-pump owner profile keys))))])
                    [:p "No heat pumps recorded."]))))])])))))

(defn biomass-boilers-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding   (om/get-state owner :adding)
                   position (count (:biomasses profile))
                   key      [:biomasses position]]
               (bs/panel
                (panel-heading properties owner profile "Biomass Boilers" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/biomass-boiler owner profile key)
                  (if-let [biomass-boilers (seq (:biomasses profile))]
                    (let [profile (assoc profile :biomasses (vec biomass-boilers))]
                      [:div
                       (for [item biomass-boilers]
                         (let [keys [:biomasses (.indexOf (to-array biomass-boilers) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "Biomass Boiler" {:add-btn false :edit-btn true} keys)
                            (pf/biomass-boiler owner profile keys))))])
                    [:p "No biomass boilers recorded."]))))])])))))

(defn mCHP-systems-row [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          (for [profile profiles]
            [:div {:class (profile-column-width)}
             (let [adding (om/get-state owner :adding)
                   position (count (:chps profile))
                   key [:chps position]]
               (bs/panel
                (panel-heading properties owner profile "mCHP Systems" {:add-btn true :edit-btn false} key)
                (if adding
                  (pf/mCHP-system owner profile key)
                  (if-let [mCHP-systems (seq (:chps profile))]
                    (let [profile (assoc profile :chps (vec mCHP-systems))]
                      [:div
                       (for [item mCHP-systems]
                         (let [keys [:chps (.indexOf (to-array mCHP-systems) item)]]
                           (bs/panel
                            (panel-heading properties owner profile "mCHP System" {:add-btn false :edit-btn true} keys)
                            (pf/mCHP-system owner profile keys))))])
                    [:p "No mCHP systems recorded."]))))])])))))

(defn profile-rows [properties]
  (fn [profiles owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         [:div.col-md-12
          [:form {:role "form"}
           ;; profile properties
           (om/build header-row profiles)
           (om/build (description-row properties) profiles)
           (om/build (occupancy-row properties) profiles)
           (om/build (measurements-row properties) profiles)
           (om/build (energy-row properties) profiles)
           (om/build (efficiency-row properties) profiles)
           (om/build (passivhaus-row properties) profiles)
           (om/build (flats-row properties) profiles)
           (om/build (fireplaces-row properties) profiles)
           (om/build (glazing-row properties) profiles)
           (om/build (issues-row properties) profiles)
           (om/build (sap-results-row properties) profiles)
           (om/build (lessons-learnt-row properties) profiles)
           (om/build (dwelling-u-values-summary-row properties) profiles)
           (om/build (air-tightness-test-row properties) profiles)
           (om/build (bus-survey-information-row properties) profiles)
           (om/build (project-details-row properties) profiles)
           ;; (om/build documents profiles)

           ;; dwelling details
           (om/build (conservatories-row properties) profiles)
           (om/build (extensions-row properties) profiles)
           (om/build (heating-systems-row properties) profiles)
           (om/build (hot-water-systems-row properties) profiles)
           (om/build (storeys-row properties) profiles)
           (om/build (walls-row properties) profiles)
           (om/build (roofs-row properties) profiles)
           (om/build (window-sets-row properties) profiles)
           (om/build (door-sets-row properties) profiles)
           (om/build (floors-row properties) profiles)
           (om/build (roof-rooms-row properties) profiles)
           (om/build (low-energy-lights-row properties) profiles)
           (om/build (ventilation-systems-row properties) profiles)
           (om/build (airflow-measurements-row properties) profiles)

           ;; renewable energy systems
           (om/build (photovoltaic-panels-row properties) profiles)
           (om/build (solar-thermal-panels-row properties) profiles)
           (om/build (wind-turbines-row properties) profiles)
           (om/build (small-hydro-plants-row properties) profiles)
           (om/build (heat-pumps-row properties) profiles)
           (om/build (biomass-boilers-row properties) profiles)
           (om/build (mCHP-systems-row properties) profiles)]])))))

(defn profiles-div [properties owner]
  (reify
    om/IRender
    (render [_]
      (let [selected-property-id (-> properties :selected)
            property             (-> (filter #(= (:entity_id %) selected-property-id) (-> properties :data)) first)
            editable             (:editable property)
            profiles             (map #(assoc % :editable editable)
                                      (sort-by :timestamp (get-profiles selected-property-id properties)))]
        (html
         [:div
          [:h3 "Profiles"]
          [:div [:button {:type "button"
                          :class (str "btn btn-primary " (if editable "" "hidden"))
                          :onClick (fn [_]  (set! (.-location js/window) (str "/profile/" selected-property-id)))}
                 "Add new profile"]]
          [:div {:id "alert"} (om/build bs/alert (-> properties :profiles :alert))]
          [:div
           (if (seq profiles)
             (om/build (profile-rows properties) profiles)
             [:div.col-md-12.text-center
              [:p.lead {:style {:padding-top 30}}
               "No profile properties to display"]])]])))))

(comment

  (text-control profile_properties owner :annual_heating_load "Annual Heating Load")
  (text-control profile_properties owner :completeness "Completeness")
  (text-control profile_properties owner :conservation_issues "Conservation Issues")
  (text-control profile_properties owner :fabric_energy_efficiency "Fabric Energy Efficiency")
  (text-control profile_properties owner :heat_loss_parameter_hlp "Heat Loss Parameter Hlp")
  (text-control profile_properties owner :id "ID")
  (text-control profile_properties owner :intention_ofpassvhaus "Intention Ofpassvhaus")
  (text-control profile_properties owner :intervention_completion_date "Intervention Completion Date")
  (text-control profile_properties owner :intervention_description "Intervention Description")
  (text-control profile_properties owner :intervention_start_date "Intervention Start Date")
  (text-control profile_properties owner :modelling_software_methods_used "Modelling Software Methods Used")
  (text-control profile_properties owner :onsite_days "Onsite Days")
  (text-control profile_properties owner :onsite_days_new_build "Onsite Days New Build")
  (text-control profile_properties owner :orientation "Orientation")
  (text-control profile_properties owner :property_id "Property Id")
  (text-control profile_properties owner :roof_rooms_present "Roof Rooms Present")
  (text-control profile_properties owner :space_heating_requirement "Space Heating Requirement")
  (text-control profile_properties owner :total_area "Total Area")
  (text-control profile_properties owner :total_envelope_area "Total Envelope Area")
  (text-control profile_properties owner :ventilation_approach "Ventilation Approach")
  (text-control profile_properties owner :ventilation_approach_other "Ventilation Approach Other")
  )
