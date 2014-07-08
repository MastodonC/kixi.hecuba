(ns kixi.hecuba.tabs.sensors
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.userAgent :as agent]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.history :as history]
            [kixi.hecuba.widgets.chart :as chart]
            [kixi.hecuba.widgets.datetimepicker :as dtpicker]
            [ajax.core :refer [GET POST PUT]]
            [kixi.hecuba.tabs.programmes :as programmes]
            [cljs-time.coerce :as tc]
            [cljs-time.format :as tf]
            [clojure.string :as string]))

(defn chart-feedback-box [cursor owner]
  (om/component
   (dom/div nil cursor)))

(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (map #(assoc % :parent-device parent-device) readings)))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

(defn parse-date [t]
  (when-not (nil? t)
    (let [date (tc/from-date t)]
      (tf/unparse (tf/formatter "yyyy-MM-dd") date))))

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
    (select-keys m [:device_id :type :accuracy :actual_annual :corrected_unit
                    :correction :correction_factor :correction_factor_breakdown
                    :errors :events :frequency :max :median :min :period
                    :resolution :status :synthetic :unit :user_id])])

(defn refresh-contents [data project_id property_id device_id]
  (GET (str "/4/entities/property_id" property_id "/devices/" device_id)
       {:handler #(programmes/fetch-properties project_id data)}))

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

(defn handle-change [owner key e]
  (let [value (.-value (.-target e))]
    (om/set-state! owner [:sensor key] value)))

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

(defn text-input-control [owner data key label]
  [:div.form-group
   [:label.control-label.col-md-2 {:for (name key)} label]
   [:div.col-md-10
    [:input {:defaultValue (get data key "")
             :on-change #(handle-change owner key %1)
             :class "form-control"
             :type "text"}]]])

(defn static-text [data key label]
  [:div.form-group
   [:label.control-label.col-md-2 {:for (name key)} label]
   [:p {:class "form-control-static col-md-10"} (get data key "")]])

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
           (text-input-control owner (:parent-device row) :name "Name")
           (text-input-control owner row :unit "Unit")
           (text-input-control owner row :period "Period")
           (text-input-control owner row :resolution "Resolution")
           (text-input-control owner (:parent-device row) :location "Location")
           (text-input-control owner (:parent-device row) :privacy "Privacy")
           [:div.form-group
            [:label.control-label.col-md-2 {:for "calculated_field"} "Calculated Field"]
            [:input {:type "checkbox"
                     :defaultChecked (get row :actual_annual "")
                     :on-change #(om/set-state! owner [:sensor :actual_annual] (.-checked (.-target %)))}]]]]])))))

(defn form-row [data chart history table-id editing-chan] 
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [device_id type unit period resolution status
                       parent-device lower_ts upper_ts actual_annual editable]} cursor
                       {:keys [name privacy location]} parent-device
                       id (str type "-" device_id)
                       sensors (:sensors data)
                       selected? (= id (:selected sensors))]
           [:tr {:onClick (fn [e] (let [div-id (.-id (.-target e))]
                                    (when-not (= "edit" div-id) 
                                        (om/update! sensors :selected id)
                                        (om/update! chart :sensor id)
                                        (om/update! chart :unit unit)
                                        (history/update-token-ids! history :sensors id))))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            (when editable
              [:td [:div {:class "fa fa-pencil-square-o" :id "edit"
                          :onClick #(when selected? (put! editing-chan cursor))}]])
            [:td name]
            [:td type]
            [:td unit]
            [:td period]
            [:td resolution]
            [:td device_id]
            [:td location]
            [:td (if-let [t (parse-date lower_ts)] t "")]
            [:td (if-let [t (parse-date upper_ts)] t "")]
            [:td (status-label status privacy actual_annual)]]))))))

(defn sensors-table [editing-chan]
  (fn [data owner {:keys [histkey path]}]
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
              sensors                     (:sensors data)
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
               (sorting-th owner "Name" :name)
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
            series-1 (:measurements measurements)
            vals-1 (map :value series-1)
            series-1-min (apply min vals-1)
            series-1-max (apply max vals-1)
            series-1-sum (reduce + vals-1)
            series-1-count (count series-1)
            series-1-mean (if (not= 0 series-1-count) (/ series-1-sum series-1-count) "NA")]
        (html
         (if (seq series-1)
           [:div.col-md-12#summary-stats
            [:div {:class "col-md-3"}
             (bs/panel "Minimum" (str (.toFixed (js/Number. series-1-min) 3) " " unit))]
            [:div {:class "col-md-3"}
             (bs/panel "Maximum" (str (.toFixed (js/Number. series-1-max) 3) " " unit))]
            [:div {:class "col-md-3"}
             (bs/panel "Average (mean)" (str (.toFixed (js/Number. series-1-mean) 3) " " unit))]
            [:div {:class "col-md-3"}
             (bs/panel "Range" (str (.toFixed (js/Number. (- series-1-max series-1-min)) 3) " " unit))]]
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
          (programmes/fixed-scroll-to-element "sensor-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
      (let [editing (:editing (:sensor-edit data))]
        (html
         [:div.col-md-12
          [:h3 "Sensors"]
          [:div {:id "sensors-table"}
           (om/build (sensors-table editing-chan) data {:opts {:histkey :sensors
                                                               :path    :readings}})]
          
          [:div {:id "sensor-edit-div" :class (if editing "" "hidden")}
           (om/build (sensor-edit-form data) (:sensor-edit data))]
          ;; FIXME: We should have better handling for IE8 here.
          (if (or (not agent/IE)
                  (agent/isVersionOrHigher 9))
            [:div {:id "chart-div" :class (if editing "hidden" "")}
             [:div {:id "date-picker"}
              (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
             (om/build chart-feedback-box (get-in data [:chart :message]))
             (om/build chart-summary (:chart data))
             [:div.col-md-12.well
              [:div#chart {:style {:width "100%" :height 600}}
               (om/build chart/chart-figure (:chart data))]]]
            [:div.col-md-12.text-center
             [:p.lead {:style {:padding-top 30}}
              "Charting in Internet Explorer version " agent/VERSION " coming soon."]])
         ])))))

