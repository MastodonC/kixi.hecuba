(ns kixi.hecuba.multiple-properties-comparison
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [ajax.core :refer (GET POST)]
   [goog.userAgent :as agent]
   [clojure.string :as str]
   [cljs.reader :as reader]
   [kixi.hecuba.bootstrap :as bootstrap]
   [kixi.hecuba.widgets.chart :as chart]
   [kixi.hecuba.widgets.datetimepicker :as dtpicker]
   [kixi.hecuba.common :refer (interval) :as common]
   [kixi.hecuba.history :as history]
   [sablono.core :as html :refer-macros [html]]
   [kixi.hecuba.tabs.slugs :as slugs]))

(when (not agent/IE)
  (enable-console-print!))

(defn log [& msgs]
  (when (or (and agent/GECKO
                 (agent/isVersionOrHigher 30))
            (and agent/WEBKIT
                 (agent/isVersionOrHigher 537)))
    (apply println msgs)))

(def data-model
  (atom
   {:properties {:data []
                 :selected-properties #{}
                 :selected-sensors #{}}
    :fetching {:properties false
               :measurements false}
    :chart {:unit ""
            :range {}
            :sensors #{}
            :measurements []
            :message ""}}))

(defn fetching-row []
  [:div [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data." ]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data fetchers

(defn fetch-properties [data]
  (GET "/4/properties/"
       {:handler (fn [response]
                   (om/update! data [:properties :data] response)
                   (om/update! data [:fetching :properties] false))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defmulti url-str (fn [start end entity_id device_id type measurements-type] measurements-type))
(defmethod url-str :raw [start end entity_id device_id type _]
  (str "/4/entities/" entity_id "/devices/" device_id "/measurements/"
       type "?startDate=" start "&endDate=" end))
(defmethod url-str :hourly_rollups [start end entity_id device_id type _]
  (str "/4/entities/" entity_id "/devices/" device_id "/hourly_rollups/"
       type "?startDate=" start "&endDate=" end))
(defmethod url-str :daily_rollups [start end entity_id device_id type _]
  (str "/4/entities/" entity_id "/devices/" device_id "/daily_rollups/"
       type "?startDate=" start "&endDate=" end))

(defn fetch-measurements [data sensors start-date end-date]
  (log "Fetching measurements for sensors: " sensors)
  (doseq [sensor (str/split sensors #";")]
    (let [[entity_id device_id type] (str/split sensor #"-" )
          measurements-type (interval start-date end-date)
          url (url-str start-date end-date entity_id device_id type measurements-type)]
      (om/update! data [:chart :measurements] []) ;; TODO should remove from exising when sensor is deselected
      (om/update! data [:fetching :measurements] true)
      (GET url {:handler (fn [response]
                           (om/update! data [:chart :measurements]
                                       (concat (:measurements (:chart @data))
                                               (into []
                                                     (map (fn [m]
                                                            (assoc m "sensor" sensor))
                                                          (:measurements response)))))
                           (om/update! data [:fetching :measurements] false))
                :headers {"Accept" "application/json"}
                :response-format :json
                :keywords? true}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Filtering sensors according to selected properties

(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (map #(assoc % :parent-device parent-device) readings)))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

(defn sensors-for-property [data id]
  (let [property (first (filter #(= (:id %) id) (get-in data [:properties :data])))
        sensors  (extract-sensors (:devices property))]
    sensors))

(defn get-sensors [data]
  (let [properties (-> data :properties :selected-properties)]
    (log "Getting sensors for properties: " properties)
    (vec (mapcat #(sensors-for-property data %) properties))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; History loop - this drives the fetches and clear downs

(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event                     (<! history-channel)
          history-status                (-> nav-event :args)
          [start-date end-date]         (:search history-status)
          {:keys [properties sensors]}  (:ids history-status)
          old-nav                       (:active-components @data)
          old-properties                (:properties (:ids old-nav))
          old-sensors                   (:sensors (:ids old-nav))
          [old-start-date old-end-date] (:range (:ids old-nav))]

      ;; Clear down
      (when-not properties
        (om/update! data [:chart :measurements] []))

      (when-not sensors
        (om/update! data [:properties :selected-sensors] #{})
        (om/update! data [:chart :sensors] #{})
        (om/update! data [:chart :measurements] []))
      
      ;; Fetch data
      
      (when (empty? (-> @data :properties :data))
        (log "Fetching all properties")
        (om/update! data [:fetching :properties] true)
        (fetch-properties data))

      (when (and (not= properties old-properties)
                 properties)
        (log "Setting selected properties to: " properties)
        (let [properties-seq (str/split properties #";")]
          (om/update! data [:properties :selected-properties] (into #{} properties-seq))))
      
      (when (and (not= sensors old-sensors)
                 sensors)
        (log "Setting selected sensors to: " sensors)
        (om/update! data [:properties :selected-sensors] (into #{} (str/split sensors #";")))
        (om/update! data [:chart :sensors] (into #{} (str/split sensors #";")))
        
        (when (and sensors start-date end-date) (fetch-measurements data sensors
                                                                    start-date
                                                                    end-date)))
      
      (when (and (or (not= start-date old-start-date)
                     (not= end-date old-end-date))
                 (not (every? empty? [start-date end-date])))
        (log "Setting date range to: " start-date end-date)
        (om/update! data [:chart :range] {:start-date start-date :end-date end-date})
        (fetch-measurements data sensors start-date end-date))
      
      ;; Update the new active components
      (om/update! data :active-components history-status))
    (recur)))


(defn chart-feedback-box [cursor owner]
  (om/component
   (html
    [:div cursor])))

(defn change [e owner]
  (om/set-state! owner :text (.. e -target -value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sensors table

(defn process-sensor-row-click [data history id selected? entity_id]
  (let [selected-sensors ((if selected? disj conj) (-> @data :properties :selected-sensors) id)
        device-entity-mapping (if selected?
                                (dissoc (-> @data :properties :device-entity) id)
                                (assoc (-> @data :properties :device-entity) id entity_id))]
    (om/update! data [:properties :device-entity] device-entity-mapping)
    (om/update! data [:properties :selected-sensors] selected-sensors)
    (history/update-token-ids! history
                               :sensors (if (seq selected-sensors)
                                          (str/join ";" selected-sensors)
                                          nil))))

(defn sensor-row [data]
  (fn [the-item owner]
    (om/component
     (let [{:keys [type device_id parent-device unit lower_ts upper_ts]} the-item
           history   (om/get-shared owner :history)
           entity_id (:entity_id parent-device)
           id        (str entity_id "-" device_id "-" type)
           selected? (contains? (-> data :properties :selected-sensors) id)]
       (html
        [:tr {:class (when selected? "success")
              :onClick (fn [_] (process-sensor-row-click data history
                                                         id selected?
                                                         entity_id))} 
         [:td type]
         [:td device_id]
         [:td entity_id]
         [:td unit]
         [:td (if-let [t (common/unparse-date lower_ts "yyyy-MM-dd")] t "")]
         [:td (if-let [t (common/unparse-date upper_ts "yyyy-MM-dd")] t "")]])))))

(defn sensors-select-table [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.col-md-12
        (if (:properties (:fetching cursor))
          (fetching-row)
          [:table.table.table-hover.table-condensed {:style {:font-size "85%"}}
           [:thead [:tr 
                    [:th "Type"]
                    [:th "Device Id"]
                    [:th "Entity Id"]
                    [:th "Unit"]
                    [:th "Earliest Event"]
                    [:th "Last Event"]]]
           [:tbody
            (if (-> cursor :fetching :properties)
              (fetching-row)
              (om/build-all (sensor-row cursor) (get-sensors cursor)))]])]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Properties table

(defn process-property-row-click [data history id selected?]
  (let [selected-properties ((if selected? disj conj) (-> @data :properties :selected-properties) id)]
    (om/update! data [:properties :selected-properties] selected-properties)
    (history/update-token-ids! history
                               :properties (if (seq selected-properties)
                                             (str/join ";" selected-properties)
                                             nil))))

(defn property-row [data]
  (fn [the-item owner]
    (om/component
     (let [code                (get the-item :property_code)
           property_data       (get the-item :property_data)
           id                  (get the-item :id)
           selected-properties (-> data :properties :selected-properties)
           selected?           (contains? selected-properties id)
           history             (om/get-shared owner :history)]
       (html
        [:tr {:onClick (fn [_] (process-property-row-click data history
                                                           id selected?))
              :style {:display (if (:hidden the-item) "none" "")}
              :class (when selected? "success")} 
         [:td [:p {:class "form-control-static col-md-10"} (slugs/postal-address-html property_data)]]
         [:td code]
         [:td id]
         [:td (:project_id the-item)]])))))

(defn properties-select-table [data owner]
  (reify
    om/IInitState
    (init-state [_] {:text ""})
    om/IRenderState
    (render-state [_ {:keys [text selection]}]
      (let [properties (:data (:properties data))
            history    (om/get-shared owner :history)]
        (html
         [:div.col-md-12
          (if (:properties (:fetching data))
            (fetching-row)
            [:div
             [:h4 "Search for a property:"]
             [:form {:role "form"}
              [:div.form-group                     
               [:input.form-control {:type "text"
                                     :ref "text-field"
                                     :value text
                                     :onChange #(change % owner)}]]]
             [:p [:i "Property code"]]
             [:div.col-md-12 {:style {:overflow "auto"}}
              [:table {:class "table table-hover table-condensed" :style {:font-size "85%"}}
               [:thead
                [:tr 
                 [:th "Property Address"]
                 [:th "Property Code"]
                 [:th "Property Id"]
                 [:th "Project"]]]
               [:tbody
                (for [property properties]
                  (om/build (property-row data) property
                            {:fn (fn [x]
                                   (if-not (< (count text) 3)
                                     (cond-> x
                                             (or (nil? (:property_code x))
                                                 (not (zero? (.indexOf (:property_code x) text))))
                                             (assoc :hidden true))
                                     x))}))]]]])])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view

(defn multiple-properties-chart [data owner]
  (reify
    om/IWillMount
    (will-mount [this]
      (let [clicked     (om/get-state owner [:chans :selection])
            history     (om/get-shared owner :history)
            m           (mult (history/set-chan! history (chan)))
            tap-history #(tap m (chan))]
        (history-loop (tap-history) data)))
    om/IRender
    (render [_]
      (html 
       [:div
        [:div.panel-group {:id "accordion"}
         (bootstrap/accordion-panel  "#collapseOne" "collapseOne" "Properties"
                                     (om/build properties-select-table data))
         
         (bootstrap/accordion-panel  "#collapseThree" "collapseThree" "Sensors"
                                     (om/build sensors-select-table data))]
        [:br]
        [:div.panel-group
         [:div.panel.panel-default
          [:div.panel-heading
           [:h3.panel-title "Chart"]]
          [:div.panel-body
           [:div {:id "date-picker"}
            (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
           (om/build chart-feedback-box (get-in data [:chart]))
           (when (-> data :fetching :measurements) (fetching-row))
           [:div.col-md-12.well
            [:div#chart {:style {:width "100%" :height 600}}
             (om/build chart/chart-figure (:chart data))]]]]]]))))


(om/root multiple-properties-chart data-model {:target (.getElementById js/document "charting")
                                               :shared {:history (history/new-history [:properties :sensors :range])}})
