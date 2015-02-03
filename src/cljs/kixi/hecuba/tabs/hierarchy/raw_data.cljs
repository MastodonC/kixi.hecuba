(ns kixi.hecuba.tabs.hierarchy.raw-data
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.string :as str]
            [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [ajax.core :refer [GET]]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.model :refer (app-model)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

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
    [:th {:on-click (fn [_ _] (put! th-chan header-key))}
     (str label " ")
     (if (= sort-key header-key)
       (if sort-asc
         [:i.fa.fa-sort-asc]
         [:i.fa.fa-sort-desc]))]))

(defn raw-data []
  (om/ref-cursor (-> (om/root-cursor app-model) :properties :raw-data)))

(defn url-str [start end entity_id device_id type]
  (str "/4/entities/" entity_id "/devices/" device_id "/measurements/"
       type "?startDate=" start "&endDate=" end))

(defn date->amon-timestamp [date]
  (->> date
       (tf/parse (tf/formatter "yyyy-MM-dd"))
       (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss"))))

(defn calculate-end-date [start-date]
  (let [d (tf/parse (tf/formatter "yyyy-MM-dd") start-date)]
    (->> (t/plus d (t/days 1))
         (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss")))))

(defn get-measurements [raw-data entity_id sensor start-date]
  (let [[type device_id] (str/split sensor #"~")
        start-ts (date->amon-timestamp start-date)
        end-ts   (calculate-end-date start-date)
        url (url-str start-ts end-ts entity_id device_id type)]
    (log "Fetching measurements for date: " start-ts end-ts)
    (GET url
         {:handler #(om/update! raw-data :data (:measurements %))
          :error-handler (fn [{:keys [status status-text]}]
                           (log "There was an error. Status " status " Text " status-text))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Date mover

(defn date-mover [cursor owner {:keys [event-chan]}]
  (om/component
   (html
    (let [inactive? (nil? (-> cursor :date))]
      [:div#date-mover.col-centered
       [:div
        {:class (str "col-md-1 fa fa-fast-backward " (if inactive? "disabled" "datemover"))
         :title "- 1 Week"
         :on-click (fn [_] (when-not inactive? (put! event-chan {:event :date-mover :value :week-minus})))}]
       [:div
        {:class (str "col-md-1 fa fa-step-backward " (if inactive? "disabled" "datemover"))
         :title "- 1 Day"
         :on-click (fn [_] (when-not inactive? (put! event-chan {:event :date-mover :value :day-minus})))}]
       [:div
        {:class (str "col-md-1 fa fa-step-forward " (if inactive? "disabled" "datemover"))
         :title "+ 1 Day"
         :on-click (fn [_] (when-not inactive? (put! event-chan {:event :date-mover :value :day-plus})))}]
       [:div
        {:class (str "col-md-1 fa fa-fast-forward " (if inactive? "disabled" "datemover"))
         :title "+ 1 Week"
         :on-click (fn [_] (when-not inactive? (put! event-chan {:event :date-mover :value :week-plus})))}]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Datetimepicker

(defn handle-change [owner key e]
  (let [value (.. e -target -value)]
    (om/set-state! owner key value)))

(defn datetime-input-control [owner key label]
  (let [id-for (str (name key))]
    [:div
     [:label.control-label {:for id-for} label]
     [:input {:ref (name :key)
              :value (om/get-state owner key)
              :placeholder "YYYY-MM-DD"
              :on-change #(handle-change owner key %1)
              :class "form-control"
              :id id-for
              :type "date"}]]))

(defn datetime-picker [raw-data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:date nil})
    om/IWillReceiveProps
    (will-receive-props [_ {:keys [date]}]
      (om/set-state! owner :date date))
    om/IRenderState
    (render-state [_ {:keys [date event-chan]}]
      (html
       [:div
        [:div.row
         (datetime-input-control owner :date "Date")]
        [:div.row.row-centered {:style {:padding-top "10px" :padding-bottom "10px"}}
         (om/build date-mover raw-data {:opts {:event-chan event-chan}})]
        [:div.row {:style {:padding-top "10px"}}
         [:div.form-group
          [:button.btn.btn-primary
           {:type "button"
            :id "raw-data-get-dates"
            :on-click (fn [_ _]
                        (put! event-chan {:event :fetch-raw-data :value date}))}
           "Get Data"]]]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sensors

(defn sensor-row [sensor owner {:keys [table-id event-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [device_id type unit period resolution status
                     parent-device lower_ts upper_ts actual_annual editable]} sensor
             {:keys [description privacy location]} parent-device
             id (str type "~" device_id)
             selected (:selected (om/observe owner (raw-data)))]
         [:tr {:on-click (fn [e]
                           (put! event-chan {:event :sensor-click :value id})
                           (when (and lower_ts upper_ts)
                             (put! event-chan {:event :default-date :value (common/unparse-date lower_ts "yyyy-MM-dd")})))
               :class (when (= id selected) "success")
               :id (str table-id "-selected")}
          [:td description]
          [:td type]
          [:td unit]
          [:td period]
          [:td resolution]
          [:td device_id]
          [:td (common/location-col location)]
          [:td (if-let [t (common/unparse-date lower_ts "yyyy-MM-dd")] t "")]
          [:td (if-let [t (common/unparse-date upper_ts "yyyy-MM-dd")] t "")]
          [:td (status-label status privacy actual_annual)]])))))

(defn sensors-table [sensors owner {:keys [event-chan]}]
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
    (render-state [_ {:keys [sort-spec]}]
      (let [{:keys [sort-key sort-asc]} sort-spec
            history                     (om/get-shared owner :history)
            table-id                    "sensors-table"]
        (html
         [:div.col-md-12 {:style {:overflow "auto"}}
          [:table {:class "table table-hover table-condensed"}
           [:thead
            [:tr
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
                        (sort-by sort-key sensors)
                        (reverse (sort-by sort-key sensors)))]
              (om/build sensor-row row {:opts {:event-chan event-chan}}))]]])))))

(defn raw-data-table [raw-data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:table.table.table-striped.table-hover
        [:thead
         [:tr [:th "Timestamp"] [:th "Value"]]]
        [:tbody
         (for [row (sort-by :timestamp raw-data)]
           (let [{:keys [timestamp value]} row]
             [:tr [:td timestamp] [:td value]]))]]))))

(defn process-get-data-click [raw-data value entity_id]
  (om/update! raw-data :date value)
  (get-measurements raw-data
                    entity_id
                    (:selected @raw-data)
                    value))

(defmulti calculate-new-date (fn [event _] event))
(defmethod calculate-new-date :day-minus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/minus d (t/days 1)))))
(defmethod calculate-new-date :day-plus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/plus d (t/days 1)))))
(defmethod calculate-new-date :week-minus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/minus d (t/weeks 1)))))
(defmethod calculate-new-date :week-plus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/plus d (t/weeks 1)))))

(defn raw-data-div [{:keys [entity_id raw-data]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:event-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [event-chan]}  (om/get-state owner)
              {:keys [event value]} (<! event-chan)]
          (case event
            :sensor-click (om/update! raw-data :selected value)
            :default-date (om/update! raw-data :date value)
            :fetch-raw-data (process-get-data-click raw-data value entity_id)
            :date-mover (om/update! raw-data :date (calculate-new-date value (-> @raw-data :date)))))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [event-chan]}]
      (html
       [:div [:h3 "Raw Sensor Data"]
        (om/build sensors-table (:sensors raw-data) {:opts {:event-chan event-chan}})
        [:div
         [:div.col-md-2.col-md-offset-3
          (om/build datetime-picker raw-data {:init-state {:event-chan event-chan}})]
         [:div.col-md-4 (om/build raw-data-table (:data raw-data))]]]))))
