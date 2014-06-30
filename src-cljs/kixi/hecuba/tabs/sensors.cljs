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
            [kixi.hecuba.widgets.datetimepicker :as dtpicker]))

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
      (extract-sensors (:devices property-details))
      [])
    []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sensors
(defn status-label [status]
  (if (= status "OK")
    [:span {:class "label label-success"} status]
    [:span {:class "label label-danger"} status]))

(defn sorting-th [owner label header-key]
  (let [{:keys [sort-spec th-chan]} (om/get-state owner)
        {:keys [sort-key sort-asc]} sort-spec]
    [:th {:onClick (fn [_ _] (put! th-chan header-key))}
     (str label " ")
     (if (= sort-key header-key)
       (if sort-asc
         [:i.fa.fa-sort-asc]
         [:i.fa.fa-sort-desc]))]))

(defn sensors-table [data owner {:keys [histkey path]}]
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
            flattened-sensors           (get-sensors selected-property-id data)
            chart                       (:chart data)
            history                     (om/get-shared owner :history)
            table-id                    "sensors-table"]
        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr
            (sorting-th owner "Name" :name)
            (sorting-th owner "Type" :type)
            (sorting-th owner "Unit" :unit)
            (sorting-th owner "Period" :period)
            (sorting-th owner "Resolution" :resolution)
            (sorting-th owner "Device ID" :device_id)
            (sorting-th owner "Location" :location)
            (sorting-th owner "Privacy" :privacy)
            (sorting-th owner "Earliest Event" :lower_ts)
            (sorting-th owner "Last Event" :upper_ts)
            (sorting-th owner "Status" :status)]]
          [:tbody
           (for [row (if sort-asc
                       (sort-by sort-key flattened-sensors)
                       (reverse (sort-by sort-key flattened-sensors)))]
             (let [{:keys [device_id type unit period resolution status
                           parent-device lower_ts upper_ts]} row
                   {:keys [name privacy location]} parent-device
                   id (str type "-" device_id)]
               [:tr {:onClick (fn [_ _]
                                (om/update! sensors :selected id)
                                (om/update! chart :sensor id)
                                (om/update! chart :unit unit)
                                (history/update-token-ids! history :sensors id))
                     :className (if (= id (:selected sensors)) "success")
                     :id (str table-id "-selected")}
                [:td name]
                [:td type]
                [:td unit]
                [:td period]
                [:td resolution]
                [:td device_id]
                [:td location]
                [:td privacy]
                [:td (str lower_ts)]
                [:td (str upper_ts)]
                [:td (status-label status)]]))]])))))

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
    om/IRender
    (render [_]
      (html
       [:div.col-md-12
        [:h3 {:id "sensors"} "Sensors"]
        (om/build sensors-table data {:opts {:histkey :sensors
                                             :path    :readings}})
        ;; FIXME: We should have better handling for IE8 here.
        (if (or (not agent/IE)
                (agent/isVersionOrHigher 9))
          [:div {:id "chart-div"}
           [:div {:id "date-picker"}
            (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
           (om/build chart-feedback-box (get-in data [:chart :message]))
           (om/build chart-summary (:chart data))
           [:div.col-md-12.well
            [:div#chart {:style {:width "100%" :height 600}}
             (om/build chart/chart-figure (:chart data))]]]
          [:div.col-md-12.text-center
           [:p.lead {:style {:padding-top 30}}
            "Charting in Internet Explorer version " agent/VERSION " coming soon."]])]))))

