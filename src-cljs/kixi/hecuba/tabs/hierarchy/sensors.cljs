(ns kixi.hecuba.tabs.hierarchy.sensors
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [goog.userAgent :as agent]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (static-text text-input-control checkbox) :as bs]
            [kixi.hecuba.history :as history]
            [kixi.hecuba.widgets.chart :as chart]
            [kixi.hecuba.widgets.datetimepicker :as dtpicker]
            [ajax.core :refer [GET POST PUT]]
            [kixi.hecuba.tabs.hierarchy.data :refer (fetch-properties)]
            [clojure.string :as string]
            [kixi.hecuba.common :refer (log) :as common]))

(defn chart-feedback-box [cursor owner]
  (om/component
   (let [message (:message cursor)]
     (html
      [:div
       [:div {:id "chart-feedback" :class "alert alert-danger" :style {:display (if (empty? message) "none" "block")}}
        [:button.close {:type "button" :onClick (fn [e]
                                                  (om/update! cursor :message "")
                                                  (set! (.-display (.-style (.getElementById js/document "chart-feedback")))
                                                        "none"))}
         [:span {:class "fa fa-times"}]]
        [:div message]]]))))

(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (map #(assoc % :parent-device parent-device) readings)))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

;; FIXME: This is a dupe from property-details
(defn get-property-details [selected-property-id data]
  (->>  data
        :properties
        :data
        (filter #(= (:id %) selected-property-id))
        first))

(defn get-sensors [selected-property-id data]
  (if selected-property-id
    (if-let [property-details (get-property-details selected-property-id data)]
      (let [editable (:editable property-details)
            sensors  (extract-sensors (:devices property-details))]
        (map #(assoc % :editable editable) sensors))
      [])
    []))

(defn split-device-and-sensor [m]
   [(select-keys m [:device_id :description :parent_id :entity_id :name
                    :location :metadata :privacy :metering_point_id])
    (select-keys m [:device_id :type :alias :accuracy :actual_annual :corrected_unit
                    :correction :correction_factor :correction_factor_breakdown
                    :errors :events :frequency :max :median :min :period
                    :resolution :status :synthetic :unit :user_id])])

(defn refresh-contents [data project_id property_id device_id]
  (GET (str "/4/entities/property_id" property_id "/devices/" device_id)
       {:handler #(fetch-properties project_id data)}))

(defn post-resource [data project_id property_id device_id sensor-data]
  (PUT  (str "/4/entities/" property_id "/devices/" device_id)
         {:content-type "application/json"
          :handler #(refresh-contents data project_id property_id device_id)
          :params sensor-data}))

(defn save-form [data owner project_id property_id device_id type]
  (let [sensor-data (om/get-state owner [:sensor])
        [device readings] (split-device-and-sensor sensor-data)]
    (post-resource data project_id property_id device_id (assoc device :readings [(assoc readings :type type)]))
    (om/update! data [:sensor-edit :editing] false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sensors

(defn status-label [status privacy calculated-field]
  [:div
   [:div
    (if (= status "OK")
      [:span {:class "label label-success"} status]
      [:span {:class "label label-danger"} status])]
   (when (= "true" privacy) [:div {:class "fa fa-key"}])
   (when (= true calculated-field) [:div {:class "fa fa-magic"}])])

(defn sorting-th [owner label header-key]
  (let [{:keys [sort-spec th-chan]} (om/get-state owner)
        {:keys [sort-key sort-asc]} sort-spec]
    [:th {:onClick (fn [_ _] (put! th-chan header-key))}
     (str label " ")
     (if (= sort-key header-key)
       (if sort-asc
         [:i.fa.fa-sort-asc]
         [:i.fa.fa-sort-desc]))]))

(defn sensor-edit-form [data]
  (fn [cursor owner]
    (om/component
     (let [property-id (-> data :active-components :properties)
           project-id  (-> data :active-components :projects)
           row         (-> cursor :row)
           {:keys [device_id type]} row]
       (html
        [:div
         [:h3 "Editing sensor"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button.btn.btn-default {:type "button"
                                       :class (str "btn btn-success")
                                       :onClick (fn [_ _] (save-form data owner project-id property-id device_id type))} "Save"]
             [:button.btn.btn-default {:type "button"
                                       :class (str "btn btn-danger")
                                       :onClick (fn [_ _] (om/update! data [:sensor-edit :editing] false))} "Cancel"]]]
           (static-text row :device_id "Device ID")
           (static-text row :type "Type")
           (text-input-control (:parent-device row) owner :sensor :name "Parent Device Name")
           (text-input-control (:alias row) owner :sensor  :name "Alias")
           (text-input-control row owner :sensor :unit "Unit")
           (text-input-control row owner :sensor :period "Period")
           (text-input-control row owner :sensor :resolution "Resolution")
           (text-input-control (:parent-device row) owner :sensor :location "Location")
           (text-input-control (:parent-device row) owner :sensor :privacy "Privacy")
           (checkbox row owner :sensor :actual_annual "Calculated Field")]]])))))

(defn update-sensor-selection [selected-sensors unit chart sensors history]
  (om/update! sensors :selected selected-sensors)
  (om/update! chart :sensor selected-sensors)
  (om/update! chart :unit (if (seq selected-sensors) unit ""))
  (om/update! chart :message "")
  (history/update-token-ids! history :sensors (if (seq selected-sensors)
                                                (string/join ";" selected-sensors)
                                                nil)))

(defn sensor-click [selected? sensors chart history id unit]

  (let [selected-sensors ((if selected? disj conj) (:selected @sensors) id)]
    (if-not selected?
      (let [current-unit (:unit @chart)]
        (if (or (empty? current-unit)
                (= current-unit unit))
          (update-sensor-selection selected-sensors unit chart sensors history)
          (om/update! chart :message "Sensors must be of the same unit.")))
      (update-sensor-selection selected-sensors unit chart sensors history))))

(defn form-row [data chart history table-id editing-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [device_id type unit period resolution status
                       parent-device lower_ts upper_ts actual_annual editable]} cursor
                       {:keys [description privacy location]} parent-device
                       id (str type "-" device_id)
                       sensors (:sensors data)
                       selected? (contains? (:selected sensors) id)]
           [:tr {:onClick (fn [e] (let [div-id (.-id (.-target e))]
                                    (when-not (= "edit" div-id)
                                      (sensor-click selected? sensors chart history id unit)
                                      (when (and lower_ts upper_ts)
                                        (om/update! chart :range {:start-date (common/unparse-date lower_ts "yyyy-MM-dd HH:MM:SS")
                                                                  :end-date (common/unparse-date upper_ts "yyyy-MM-dd HH:MM:SS")})))))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            (when editable
              [:td [:div {:class "fa fa-pencil-square-o" :id "edit"
                          :onClick #(when selected? (put! editing-chan cursor))}]])
            [:td description]
            [:td type]
            [:td unit]
            [:td period]
            [:td resolution]
            [:td device_id]
            [:td location]
            [:td (if-let [t (common/unparse-date lower_ts "yyyy-MM-dd")] t "")]
            [:td (if-let [t (common/unparse-date upper_ts "yyyy-MM-dd")] t "")]
            [:td (status-label status privacy actual_annual)]]))))))

(defn sensors-table [editing-chan data]
  (fn [cursor owner {:keys [histkey path]}]
    (reify
      om/IInitState
      (init-state [_]
        {:th-chan (chan)
         :sort-spec {:sort-key :type
                     :sort-asc true}})
      om/IWillMount
      (will-mount [_]
        (go-loop []
          (let [{:keys [th-chan sort-spec]} (om/get-state owner)
                {:keys [sort-key sort-asc]} sort-spec
                th-click                    (<! th-chan)]
            (if (= th-click sort-key)
              (om/update-state! owner #(assoc %
                                         :sort-spec {:sort-key th-click
                                                     :sort-asc (not sort-asc)}))
              (om/update-state! owner #(assoc %
                                         :sort-spec {:sort-key th-click
                                                     :sort-asc true}))))
          (recur)))
      om/IRenderState
      (render-state [_ state]
        (let [{:keys [sort-key sort-asc]} (:sort-spec state)
              selected-property-id        (-> data :active-components :properties)
              selected-project-id         (-> data :active-components :projects)
              flattened-sensors           (get-sensors selected-property-id data)
              chart                       (:chart data)
              history                     (om/get-shared owner :history)
              table-id                    "sensors-table"]
          (html
           [:div.col-md-12 {:style {:overflow "auto"}}
            [:table {:class "table table-hover table-condensed"}
             [:thead
              [:tr
               [:th ""]
               (sorting-th owner "Description" :description)
               (sorting-th owner "Type" :type)
               (sorting-th owner "Unit" :unit)
               (sorting-th owner "Period" :period)
               (sorting-th owner "Resolution" :resolution)
               (sorting-th owner "Device ID" :device_id)
               (sorting-th owner "Location" :location)
               (sorting-th owner "Earliest Event" :lower_ts)
               (sorting-th owner "Last Event" :upper_ts)
               (sorting-th owner "Status" :status)]]
             [:tbody
              (for [row (if sort-asc
                          (sort-by sort-key flattened-sensors)
                          (reverse (sort-by sort-key flattened-sensors)))]
                (om/build (form-row data chart history table-id editing-chan) row))]]]))))))

(defn chart-summary
  "Show min, max, delta and average of chart data."
  [chart owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [unit measurements]} chart
            ;; FIXME why are measurements nested? (in prep for multi-series?)
            values (map :value measurements)
            measurements-min (apply min values)
            measurements-max (apply max values)
            measurements-sum (reduce + values)
            measurements-count (count measurements)
            measurements-mean (if (not= 0 measurements-count) (/ measurements-sum measurements-count) "NA")]
        (html
         (if (seq measurements)
           [:div.col-md-12#summary-stats
            [:div {:class "col-md-3"}
             (bs/panel "Minimum" (str (.toFixed (js/Number. measurements-min) 3) " " unit))]
            [:div {:class "col-md-3"}
             (bs/panel "Maximum" (str (.toFixed (js/Number. measurements-max) 3) " " unit))]
            [:div {:class "col-md-3"}
             (bs/panel "Average (mean)" (str (.toFixed (js/Number. measurements-mean) 3) " " unit))]
            [:div {:class "col-md-3"}
             (bs/panel "Range" (str (.toFixed (js/Number. (- measurements-max measurements-min)) 3) " " unit))]]
           [:div.row#summary-stats [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "No data."]]]))))))

(defn sensors-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! data [:sensor-edit :editing] true)
          (om/update! data [:sensor-edit :row] edited-row)
          (common/fixed-scroll-to-element "sensor-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (let [editing (:editing (:sensor-edit data))]
        (html
         [:div.col-md-12
          [:h3 "Sensors"]
          [:div {:id "sensors-table"}
           (om/build (sensors-table editing-chan data)
                     (:sensors data)
                     {:opts {:histkey :sensors
                             :path    :readings}})]
          [:div {:id "sensor-edit-div" :class (if editing "" "hidden")}
           (om/build (sensor-edit-form data) (:sensor-edit data))]
          ;; FIXME: We should have better handling for IE8 here.
          (if (or (not agent/IE)
                  (agent/isVersionOrHigher 9))
            [:div {:id "chart-div" :class (if editing "hidden" "")}
             [:div {:id "date-picker"}
              (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
             (om/build chart-feedback-box (get-in data [:chart]))
             (om/build chart-summary (:chart data))
             [:div.col-md-12.well
              [:div#chart {:style {:width "100%" :height 600}}
               (om/build chart/chart-figure (:chart data))]]]
            [:div.col-md-12.text-center
             [:p.lead {:style {:padding-top 30}}
              "Charting in Internet Explorer version " agent/VERSION " coming soon."]])])))))
