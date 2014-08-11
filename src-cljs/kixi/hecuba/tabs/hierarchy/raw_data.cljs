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
            [kixi.hecuba.common :refer (log) :as common]))

(defn url-str [start end entity_id device_id type]
  (str "/4/entities/" entity_id "/devices/" device_id "/measurements/"
       type "?startDate=" start "&endDate=" end))

(defn date->amon-timestamp [date]
  (->> date
       (tf/parse (tf/formatter "yyyy-MM-dd"))
       (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss"))))

(defn get-measurements [cursor entity_id sensor start-date end-date]
  (let [[type device_id] (str/split sensor #"-")
        start-ts (date->amon-timestamp start-date)
        end-ts (date->amon-timestamp end-date)
        url (url-str start-ts end-ts entity_id device_id type)]
    (GET url
         {:handler #(om/update! cursor :data (:measurements %))
          :error-handler (fn [{:keys [status status-text]}]
                           (log "There was an error. Status " status " Text " status-text))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))

(defn handle-change [cursor owner key e]
  (let [target (.. e -target)
        value (.. e -target -value)
        node (om/get-node owner)]
    (log "Value: " value)
    ;; (om/update! cursor key value)
    (om/set-state! owner :target target)
    (om/set-state! owner key value)
    (log "State: " (om/get-state owner))))

(defn datetime-input-control [data owner table key label & required]
  (let [id-for (str (name table) "-" (name key))]
    [:div
     [:label.control-label {:for id-for} label]
     [:input {:ref (name :key)
              :default-value (let [default (get-in data [table key] "")]
                               (om/set-state! owner [table key] default)
                               default)
              :placeholder "YYYY-MM-DD"
              :on-change #(handle-change data owner [table key] %1)
              :class "form-control"
              :id id-for
              :type "date"}]]))

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

(defn datetime-picker [entity_id]
  (fn [cursor owner]
    (reify
      om/IInitState
      (init-state [_]
        {:target nil})
      om/IRenderState
      (render-state [_ state]
        (log "State: " state)
        (html
         [:div
          (datetime-input-control cursor owner :range :start-date "Start Date")
          (datetime-input-control cursor owner :range :end-date "End Date")
          [:div.form-group
           [:button.btn.btn-primary
            {:type "button"
             :id "raw-data-get-dates"
             :on-click (fn [_ _] (let [date-range (:range state)
                                       {:keys [start-date end-date]} date-range]
                                   (om/update! cursor :range date-range)
                                   (get-measurements cursor
                                                     entity_id
                                                     (:selected @cursor)
                                                     start-date
                                                     end-date)))}
            "Get Data"]]])))))

(defn data-row [raw-data table-id]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [device_id type unit period resolution status
                       parent-device lower_ts upper_ts actual_annual editable]} cursor
                       {:keys [description privacy location]} parent-device
                       id (str type "-" device_id)]
           [:tr {:on-click (fn
                            data-row-click
                            [e] (let [div-id (.-id (.-target e))]
                                  ;; this is the multi-select bit we need to change
                                  (om/update! raw-data :selected id)
                                  (when (and lower_ts upper_ts)
                                    (om/update! raw-data :range {:start-date (common/unparse-date lower_ts "yyyy-MM-dd")
                                                                 :end-date (common/unparse-date upper_ts "yyyy-MM-dd")}))))
                 :class (when (= id (:selected raw-data)) "success")
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
            [:td (status-label status privacy actual_annual)]]))))))


(defn sensors-table [data]
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
              raw-data                    (:raw-data data)
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
                          (sort-by sort-key flattened-sensors)
                          (reverse (sort-by sort-key flattened-sensors)))]
                (om/build (data-row raw-data table-id) row))]]]))))))

(defn raw-data-table []
  (fn [raw-data-cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         [:table.table.table-striped.table-hover
          [:thead
           [:tr [:th "Timestamp"] [:th "Value"]]]
          [:tbody
           (for [row (sort-by :timestamp (:data raw-data-cursor))]
             (let [{:keys [timestamp value]} row]
               [:tr [:td timestamp] [:td value]]))]])))))

(defn raw-data-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {})
    om/IRenderState
    (render-state [_ state]
      (let [raw-data (:raw-data data)
            entity_id (-> data :active-components :properties)]
        (html
         [:div [:h3 "Raw Sensor Data"]
          [:button.btn.btn-primary
           {:on-click (fn [_ _] (log "All Data: " (pr-str @data)))}
           "Dump"]
          (om/build (sensors-table data) raw-data)
          [:div
           [:div.col-md-2.col-md-offset-3
            (om/build (datetime-picker entity_id) raw-data)]
           [:div.col-md-4 (om/build (raw-data-table) raw-data)]]])))))
