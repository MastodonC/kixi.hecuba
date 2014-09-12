(ns kixi.hecuba.tabs.hierarchy.sensors
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [om.core :as om :include-macros true]
            [goog.userAgent :as agent]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (static-text text-input-control checkbox) :as bs]
            [kixi.hecuba.history :as history]
            [kixi.hecuba.widgets.chart :as chart]
            [kixi.hecuba.widgets.datetimepicker :as dtpicker]
            [ajax.core :refer [GET POST PUT]]
            [kixi.hecuba.tabs.hierarchy.data :refer (fetch-property fetch-sensors)]
            [clojure.string :as string]
            [kixi.hecuba.common :refer (log) :as common]))

(defn error-handler [owner]
  (fn [{:keys [status status-text]}]
    (om/set-state! owner :error true)
    (om/set-state! owner :http-error-response {:status status
                                               :status-text status-text})))

(defn split-device-and-sensor [m]
   [(select-keys m [:device_id :description :parent_id :entity_id :name
                    :location :metadata :privacy :metering_point_id])
    (select-keys m [:device_id :type :alias :accuracy :actual_annual :corrected_unit
                    :correction :correction_factor :correction_factor_breakdown
                    :errors :events :frequency :max :median :min :period
                    :resolution :status :synthetic :unit :user_id])])

(defn post-resource [refresh-chan property_id device_id sensor-data]
  (PUT  (str "/4/entities/" property_id "/devices/" device_id)
         {:content-type "application/json"
          :handler #(put! refresh-chan {:event :property})
          :params sensor-data}))

(defn save-form [properties refresh-chan owner property_id device_id type]
  (let [sensor-data (om/get-state owner [:sensor])
        [device readings] (split-device-and-sensor sensor-data)]
    (post-resource refresh-chan property_id device_id (assoc device :readings [(assoc readings :type type)]))
    (om/update! properties [:sensors :editing] false)))

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

(defn sensor-edit-form [properties]
  (fn [cursor owner]
    (om/component
     (let [property-id (-> properties :selected)
           project-id  (-> properties :project_id)
           row         (-> cursor :row)
           {:keys [device_id type]} row
           refresh-chan (om/get-shared owner :refresh)]
       (html
        [:div
         [:h3 "Editing sensor"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button.btn.btn-default {:type "button"
                                       :class (str "btn btn-success")
                                       :onClick (fn [_ _] (save-form properties refresh-chan owner property-id device_id type))}
              "Save"]
             [:button.btn.btn-default {:type "button"
                                       :class (str "btn btn-danger")
                                       :onClick (fn [_ _] (om/update! properties [:sensors :editing] false))} "Cancel"]]]
           (static-text row :device_id "Device ID")
           (static-text row :type "Type")
           (text-input-control (:parent-device row) owner :sensor :name "Parent Device Name")
           (text-input-control row owner :sensor :alias "Header Rows")
           (text-input-control row owner :sensor :unit "Unit")
           (text-input-control row owner :sensor :period "Period")
           (text-input-control row owner :sensor :resolution "Resolution")
           (checkbox row owner :sensor :actual_annual "Calculated Field")]]])))))


(defn update-sensor-selection [selected-sensors unit properties sensors history]
  (om/update! sensors :selected selected-sensors)
  (om/update! properties [:chart :sensor] selected-sensors)
  (om/update! properties [:chart :unit] (if (seq selected-sensors) unit ""))
  (history/update-token-ids! history :sensors (if (seq selected-sensors)
                                                (string/join ";" selected-sensors)
                                                nil)))

(defn sensor-click [selected? sensors properties history id unit]

  (let [selected-sensors ((if selected? disj conj) (:selected @sensors) id)]
    (if-not selected?
      (let [current-unit (-> @properties :chart :unit)]
        (if (or (empty? current-unit)
                (= current-unit unit))
          (update-sensor-selection selected-sensors unit properties sensors history)
          (om/update! properties [:sensors :alert] {:status true
                                                    :class "alert alert-danger"
                                                    :text "Sensors must be of the same unit."})))
      (update-sensor-selection selected-sensors unit properties sensors history))))

(defn form-row [properties history table-id editing-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [device_id type unit period resolution status synthetic
                       parent-device lower_ts upper_ts actual_annual editable]} cursor
                       {:keys [description privacy location]} parent-device
                       id (str type "-" device_id)
                       sensors (:sensors properties)
                       selected? (contains? (:selected sensors) id)]
           [:tr {:onClick (fn [e] (let [div-id (.-id (.-target e))]
                                    (when-not (= "edit" div-id)
                                      (sensor-click selected? sensors properties history id unit)
                                      (when (and lower_ts upper_ts)
                                        (om/update! properties [:chart :range] {:start-date (common/unparse-date lower_ts "yyyy-MM-dd")
                                                                                :end-date (common/unparse-date upper_ts "yyyy-MM-dd")})))))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td (when (and editable (not synthetic))
                   [:div {:class "fa fa-pencil-square-o" :id "edit"
                          :onClick #(when selected? (put! editing-chan cursor))}])]
            [:td description]
            [:td type]
            [:td unit]
            [:td period]
            [:td resolution]
            [:td device_id]
            [:td (common/location-col location)]
            [:td (if-let [t (common/unparse-date lower_ts "yyyy-MM-dd")] t "")]
            [:td (if-let [t (common/unparse-date upper_ts "yyyy-MM-dd")] t "")]
            [:td (status-label status privacy actual_annual)]]))))))

(defn sensors-table [editing-chan properties]
  (fn [cursor owner]
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
              sensors-data                (fetch-sensors (-> properties :selected) properties)
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
                          (sort-by sort-key sensors-data)
                          (reverse (sort-by sort-key sensors-data)))]
                (om/build (form-row properties history table-id editing-chan) row))]]]))))))

(defn chart-summary
  "Show min, max, delta and average of chart data."
  [chart owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [unit measurements]} chart
            all-series (group-by #(get % "sensor") measurements)]
        (html
         (if (->> all-series first seq)
           [:div.col-md-12
            (for [[key series] all-series]
              (let [values             (keep #(let [v (:value %)]
                                                (cond (nil? v) nil
                                                      (number? v) v
                                                      (re-matches #"[-+]?\d+(\.\d+)?" v) (js/parseFloat v))) series)
                    measurements-min   (apply min values)
                    measurements-max   (apply max values)
                    measurements-sum   (reduce + values)
                    measurements-count (count values)
                    measurements-mean  (if (not= 0 measurements-count) (/ measurements-sum measurements-count) "NA")]
                [:div.col-md-3
                 (bs/panel
                  key
                  [:div.col-md-12
                   [:table.table.table-hover.table-condensed
                    [:tr [:td "Minimum"] [:td.number (str (.toFixed (js/Number. measurements-min) 3))] [:td unit]]
                    [:tr [:td "Maximum"] [:td.number (str (.toFixed (js/Number. measurements-max) 3))] [:td unit]]
                    [:tr [:td "Average (Mean)"] [:td.number (str (.toFixed (js/Number. measurements-mean) 3))] [:td unit]]
                    [:tr [:td "Range"] [:td.number (str (.toFixed (js/Number. (- measurements-max measurements-min)) 3))] [:td unit]]]])]))]
           [:div.row#summary-stats [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "No data."]]]))))))

(defn sensors-div [properties owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! properties [:sensors :editing] true)
          (om/update! properties [:sensors :row] edited-row)
          (common/fixed-scroll-to-element "sensor-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (let [{:keys [sensors]} properties
            editing        (-> properties :sensors :editing)
            project_id     (-> properties :project_id)
            property_id    (-> properties :selected)
            property       (-> (filter #(= (:entity_id %) property_id) properties) first)]
        (html
         [:div.col-md-12
          [:h3 "Sensors"]
          [:div {:id "sensors-unit-alert"}
           (om/build bs/alert (-> properties :sensors :alert))]
          [:div {:id "sensors-table" :class (if editing "hidden" "")}
           (om/build (sensors-table editing-chan properties) sensors)]
          [:div {:id "sensor-edit-div" :class (if editing "" "hidden")}
           (om/build (sensor-edit-form properties) sensors)]

          ;; FIXME: We should have better handling for IE8 here.
          (if (or (not agent/IE)
                  (agent/isVersionOrHigher 9))
            [:div {:id "chart-div" :class (if editing "hidden" "")}
             [:div {:class "col-md-4"}
              [:div {:id "picker-alert"}
               (om/build bs/alert (-> properties :chart :range :alert))]
              (om/build dtpicker/datetime-picker (-> properties :chart :range) {:opts {:div-id "chart-date-picker"}})]
             (om/build chart-summary (select-keys (:chart properties) [:measurements :unit]))
             [:div.col-md-12.well
              [:div#chart {:style {:width "100%" :height 600}}
               (om/build chart/chart-figure (select-keys (:chart properties) [:measurements :unit]))]]]
            [:div.col-md-12.text-center
             [:p.lead {:style {:padding-top 30}}
              "Charting in Internet Explorer version " agent/VERSION " coming soon."]])])))))
