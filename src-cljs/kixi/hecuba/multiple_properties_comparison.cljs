(ns kixi.hecuba.multiple-properties-comparison
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [ajax.core :refer (GET POST)]
   [clojure.string :as str]
   [cljs.reader :as reader]
   [kixi.hecuba.bootstrap :as bootstrap]
   [kixi.hecuba.widgets.chart :as chart]
   [kixi.hecuba.widgets.datetimepicker :as dtpicker]
   [kixi.hecuba.common :refer (interval log) :as common]
   [kixi.hecuba.history :as history]
   [sablono.core :as html :refer-macros [html]]
   [kixi.hecuba.tabs.slugs :as slugs]
   [cljs-time.format :as tf]
   [cljs-time.coerce :as tc]
   [cljs-time.core :as t]))

(def data-model
  (atom
   {:entities {:searched-data []
               :selected-data []
               :selected-entities #{}
               :selected-sensors #{}}
    :fetching {:entities false
               :measurements false}
    :chart {:range {}
            :sensors #{}
            :measurements []
            :message ""
            :units {}
            :mouseover false}
    :stats {}}))

(defn fetching-row []
  [:div [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data." ]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Filtering sensors according to selected properties

(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (map #(assoc % :parent-device parent-device
                 :id (str (:type %) "-" (:device_id %))) readings)))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

(defn sensors-for-property [data id]
  (let [loaded-properties (if-let [searched (seq (-> data :entities :searched-data))]
                            searched
                            (-> data :entities :selected-data))
        property (first (filter #(= (:entity_id %) id) loaded-properties))
        sensors  (extract-sensors (:devices property))]
    sensors))

(defn get-sensors [data]
  (let [properties (-> data :entities :selected-entities)]
    (log "Getting sensors for properties: " properties)
    (vec (mapcat #(sensors-for-property data %) properties))))

(defn fetch-sensors-for-property [property]
  (extract-sensors (:devices property)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data fetchers

(defn fetch-entities [data query]
  (GET (str "/4/entities/?q=" query)
       {:handler (fn [response]
                   (om/update! data [:entities :searched-data] (take 20 (:entities response)))
                   (om/update! data [:stats] {:total_hits (:total_hits response)
                                              :page (:page response)})
                   (om/update! data [:fetching :entities] false))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn get-unit [property-id sensors property]
  (let [sensors-for-property (fetch-sensors-for-property property)
        selected-sensors     (filter (fn [sensor] (some #(= (:id sensor) %) sensors))
                                     sensors-for-property)]
    (into {} (map #(hash-map (:id %) (:unit %)) selected-sensors))))

(defn fetch-entity [data id]
  (log "Fetching entity: " id)
  (let [sensors (-> @data :entities :selected-sensors)]
    (GET (str "/4/entities/" id)
         {:handler (fn [response]
                     (om/transact! data [:entities :selected-data] #(conj % response))
                     (when (seq sensors)
                       (om/transact! data [:entities :chart :units] #(conj % (get-unit id sensors response))))
                     (om/update! data [:fetching :entities] false))
          :headers {"Accept" "application/edn"}
          :response-format :text})))

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

(defn date->amon-timestamp [date]
  (->> date
       (tf/parse (tf/formatter "yyyy-MM-dd"))
       (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss"))))

(defn pad-end-date [date]
  (let [timestamp (tf/parse (tf/formatter "yyyy-MM-dd") date)
        padded    (t/plus timestamp (t/days 1))]
    (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss") padded)))

(defn fetch-measurements [data sensors start-date end-date]
  (log "Fetching measurements for sensors: " sensors)
  (om/update! data [:fetching :measurements] true)
  (om/update! data [:chart :measurements] [])
  (doseq [sensor (str/split sensors #";")]
    (let [[entity_id device_id type] (str/split sensor #"-" )
          end (if (= start-date end-date) (pad-end-date end-date) (date->amon-timestamp end-date))
          start-date (date->amon-timestamp start-date)
          measurements-type (interval start-date end)
          url (url-str start-date end entity_id device_id type measurements-type)]
      (GET url {:handler (fn [response]
                           (om/transact! data [:chart :measurements]
                                         (fn [measurements]
                                           (conj measurements
                                                 (into []
                                                       (map (fn [m]
                                                              (assoc m :sensor sensor))
                                                            (:measurements response))))))
                           (when (= (-> (str/split sensors #";") last) sensor)
                             (om/update! data [:fetching :measurements] false)))
                :headers {"Accept" "application/json"}
                :response-format :json
                :keywords? true}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; History loop - this drives the fetches and clear downs

(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event                     (<! history-channel)
          history-status                (-> nav-event :args)
          [start-date end-date]         (:search history-status)
          {:keys [entities sensors]}    (:ids history-status)
          old-nav                       (:active-components @data)
          old-entities                  (:entities (:ids old-nav))
          old-sensors                   (:sensors (:ids old-nav))
          [old-start-date old-end-date] (:range (:ids old-nav))]

      ;; Clear down
      (when-not entities
        (om/update! data [:chart :measurements] []))

      (when-not sensors
        (om/update! data [:entities :selected-sensors] #{})
        (om/update! data [:chart :sensors] #{})
        (om/update! data [:chart :measurements] []))

      ;; Fetch data
      (when (and (not= entities old-entities)
                 entities)
        (log "Setting selected entities to: " entities)
        (om/update! data [:chart :sensors] #{})
        (let [entities-seq  (str/split entities #";")]
          (om/update! data [:entities :selected-entities] (into #{} entities-seq))
          (when-not (seq (-> @data :entities :searched-data))
            (doseq [entity entities-seq]
              (fetch-entity data entity)))
          (om/update! data [:sensors :data] (get-sensors @data))))

      (when sensors
        (log "Setting selected sensors to: " sensors)
        (let [selected-sensors (into #{} (str/split sensors #";"))]
          (om/update! data [:entities :selected-sensors] selected-sensors)
          (om/update! data [:chart :sensors] selected-sensors))

        (when (and sensors old-start-date old-end-date)
          (fetch-measurements data sensors old-start-date old-end-date)))

      (when (and (or (not= start-date old-start-date)
                     (not= end-date old-end-date)))
        (log "Setting date range to: " start-date end-date)
        (om/update! data [:chart :range] {:start-date start-date :end-date end-date})
        (when (and (not (every? empty? [start-date end-date]))
                   sensors)
          (fetch-measurements data sensors start-date end-date)))

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

(defn allowed-unit? [existing-units new-unit]
  (or (not (seq existing-units))
      (< (count existing-units) 2)
      (some #(= new-unit %) existing-units)))

(defn update-sensor [id unit lower_ts upper_ts data history selected?]
  (let [new-selected-sensors ((if selected? disj conj) (-> @data :entities :selected-sensors) id)
        [entity_id type device_id] (str/split id #"-")
        device-entity-mapping (if selected?
                                (dissoc (-> @data :entities :device-entity) id)
                                (assoc (-> @data :entities :device-entity) id entity_id))]
    ;; update history
    (history/update-token-ids! history
                               :sensors (if (seq new-selected-sensors)
                                          (str/join ";" new-selected-sensors)
                                          nil))
    ;; update chart default range
    (when (and lower_ts upper_ts (not selected?))
      (om/update! data [:chart :range] {:start-date (common/unparse-date lower_ts "yyyy-MM-dd")
                                        :end-date (common/unparse-date upper_ts "yyyy-MM-dd")}))
    ;; Update entities cursor with new selection
    (om/update! data [:entities :device-entity] device-entity-mapping)
    (om/update! data [:entities :selected-sensors] new-selected-sensors)
    ;; update units in chart
    (om/transact! data [:chart :units] (fn [units]
                                         (if (seq new-selected-sensors)
                                           (assoc units id unit)
                                           {})))))

(defn process-sensor-row-click [data history id unit lower_ts upper_ts selected? entity_id]
  (let [selected-sensors ((if selected? disj conj) (-> @data :entities :selected-sensors) id)
        device-entity-mapping (if selected?
                                (dissoc (-> @data :entities :device-entity) id)
                                (assoc (-> @data :entities :device-entity) id entity_id))
        existing-units    (into #{} (vals (-> @data :chart :units)))]

    (if-not selected?
      (if (allowed-unit? existing-units unit)
        (update-sensor id unit lower_ts upper_ts data history false)
        (om/update! data [:alert] {:status true
                                   :class "alert alert-danger"
                                   :text "Please limit the number of different units to 2."}))
      (update-sensor id unit lower_ts upper_ts  data history true))))

(defn sensor-row [data]
  (fn [the-item owner]
    (om/component
     (let [{:keys [type device_id parent-device unit lower_ts upper_ts]} the-item
           history   (om/get-shared owner :history)
           entity_id (:entity_id parent-device)
           id        (str entity_id "-" device_id "-" type)
           selected? (contains? (-> data :entities :selected-sensors) id)]
       (html
        [:tr {:class (when selected? "success")
              :onClick (fn [_] (process-sensor-row-click data history
                                                         id unit lower_ts upper_ts selected?
                                                         entity_id))}
         [:td type]
         [:td device_id]
         [:td entity_id]
         [:td unit]
         [:td (if-let [t (common/unparse-date-str lower_ts "yyyy-MM-dd")] t "")]
         [:td (if-let [t (common/unparse-date-str upper_ts "yyyy-MM-dd")] t "")]])))))

(defn sensors-select-table [cursor owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.col-md-12
        (if (:entities (:fetching cursor))
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
            (if (-> cursor :fetching :entities)
              (fetching-row)
              (when (and (or (-> cursor :entities :selected-data seq)
                             (-> cursor :entities :searched-data seq))
                         (-> cursor :entities :selected-entities seq))
                (om/build-all (sensor-row cursor) (get-sensors cursor) {:key :id})))]])]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Properties table

(defn process-property-row-click [data history id selected?]
  (let [selected-properties ((if selected? disj conj) (-> @data :entities :selected-entities) id)]
    (om/update! data [:entities :selected-entities] selected-properties)
    (history/update-token-ids! history
                               :entities (if (seq selected-properties)
                                           (str/join ";" selected-properties)
                                           nil))))

(defn property-row [data]
  (fn [the-item owner]
    (om/component
     (let [code                (get the-item :property_code)
           property_data       (get the-item :property_data)
           id                  (get the-item :entity_id)
           selected-entities   (-> data :entities :selected-entities)
           selected?           (contains? selected-entities id)
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

(defn search-stats [cursor owner]
  (om/component
   (let [hits (:total_hits cursor)]
     (html
      [:div {:class (if hits "" "hidden")}
       (str "About " hits " result(s).")]))))

(defn input-box [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [event-chan]}]
      (html
       [:div
        [:div.input-group.input-group-md
         [:input {:type "text"
                  :class "form-control input-md"
                  :on-change (fn [e] (om/set-state! owner :value (.-value (.-target e))))
                  :on-key-press (fn [e] (when (= (.-keyCode e) 13)
                                          (let [value (om/get-state owner :value)]
                                            (when value
                                              (put! event-chan value)))))}]
         [:span.input-group-btn
          [:button {:type "button" :class "btn btn-primary"
                    :on-click (fn [e]
                                (let [value (om/get-state owner :value)]
                                  (when value
                                    (put! event-chan value))))} "Go"]]]]))))

(defn properties-select-table [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:event-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [event-chan (om/get-state owner :event-chan)]
        (go (while true
              (let [v (<! event-chan)]
                (fetch-entities cursor v))))))
    om/IRenderState
    (render-state [_ state]
      (let [searched-data (into [] (-> cursor :entities :searched-data))
            selected-data (-> cursor :entities :selected-data)
            merged-data   (concat searched-data selected-data)
            history    (om/get-shared owner :history)]
        (html
         [:div.col-md-12
          (if (:properties (:fetching cursor))
            (fetching-row)
            [:div
             [:h4 "Search for a property:"]
             (om/build input-box nil {:init-state state})
             (om/build search-stats (:stats cursor))
             [:div.col-md-12 {:style {:overflow "auto"}}
              [:table {:class "table table-hover table-condensed" :style {:font-size "85%"}}
               [:thead
                [:tr
                 [:th "Property Address"]
                 [:th "Property Code"]
                 [:th "Property Id"]
                 [:th "Project"]]]
               [:tbody
                (om/build-all (property-row cursor) (distinct merged-data)
                              {:key :entity_id})]]]])])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart summaries

(defn chart-summary
  "Show min, max, delta and average of chart data."
  [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:value {}
       :mouseover false})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [measurements       (-> cursor :measurements)
              event-chan         (om/get-state owner :chan)
              {:keys [event v]}  (<! event-chan)
              bisect             (-> js/d3 (.bisector (fn [d] (aget d "timestamp"))) .-right)]
          (cond
           (= event :timestamp) (let [index  (bisect measurements v 1)
                                      value  (js->clj (aget measurements index))]
                                  (om/set-state! owner :value {:value (get value "value" "N/A")
                                                               :timestamp (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm")
                                                                                      (tc/from-date v))}))
           (= event :mouseover) (om/set-state! owner :mouseover v)))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [measurements]} cursor
            mouseover (:mouseover state)
            {:keys [value timestamp]} (:value state)
            [type device_id] (-> measurements first (aget "sensor") (str/split #"-"))
            description (-> measurements first (aget "description"))]
        (html
         [:div {:style {:font-size "80%"}}
          (bootstrap/panel
           [:div [:p {:style {:word-wrap "break-word" :font-size "80%"}} type]
            [:p {:style {:word-wrap "break-word" :font-size "80%"}} description]]
           [:div
            (if mouseover
              [:dl
               [:dt "Timestamp:"] [:dd timestamp]
               [:dt "Value:"] [:dd value]]
              (let [unit               (-> measurements first (aget "unit"))
                    series             (js->clj measurements)
                    values             (keep #(let [v (get % "value")]
                                                (cond (nil? v) nil
                                                      (number? v) v
                                                      (re-matches #"[-+]?\d+(\.\d+)?" v) (js/parseFloat v))) series)
                    measurements-min   (apply min values)
                    measurements-max   (apply max values)
                    measurements-sum   (reduce + values)
                    measurements-count (count values)
                    measurements-mean  (if (not= 0 measurements-count) (/ measurements-sum measurements-count) "NA")]
                [:table.table.table-hover.table-condensed
                 [:tr [:td "Minimum"] [:td.number (str (.toFixed (js/Number. measurements-min) 3))] [:td unit]]
                 [:tr [:td "Maximum"] [:td.number (str (.toFixed (js/Number. measurements-max) 3))] [:td unit]]
                 [:tr [:td "Average (Mean)"] [:td.number (str (.toFixed (js/Number. measurements-mean) 3))] [:td unit]]
                 [:tr [:td "Range"] [:td.number (str (.toFixed (js/Number. (- measurements-max measurements-min)) 3))] [:td unit]]]))])])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire view

(def amon-date (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ"))

(defn fetch-sensors [property selected-sensors]
  (let [editable (:editable property)
        sensors  (extract-sensors (:devices property))]
    (map #(assoc % :editable editable :selected (if (contains? selected-sensors (:id %)) true false)) sensors)))

(defn get-description [sensors measurement]
  (-> (filter #(= (:type measurement) (:type %)) sensors) first :parent-device :description))

(defn parse
  "Enriches measurements with unit and description of device and parses timestamp into a JavaScript Date object"
  [measurements units sensors]
  (map (fn [measurements-seq] (map #(assoc % :unit (get units (-> % :sensor))
                                           :description (get-description sensors %)
                                           :timestamp (tf/parse amon-date (:timestamp %))) measurements-seq)) measurements))

(defn multiple-properties-chart [data owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [hovering-chan (chan 100)
            m             (mult hovering-chan)]
        {:hovering-chan hovering-chan
         :mult-chan m}))
    om/IWillMount
    (will-mount [this]
      (let [clicked     (om/get-state owner [:chans :selection])
            history     (om/get-shared owner :history)
            m           (mult (history/set-chan! history (chan)))
            tap-history #(tap m (chan))]
        (history-loop (tap-history) data)))
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12 {:style {:padding-top "10px"}}
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
           [:div {:id "date-picker" :class "col-md-6 col-md-offset-3"}
            (om/build dtpicker/datetime-picker (-> data :chart :range))]
           (om/build chart-feedback-box (get-in data [:chart]))
           (if (-> data :fetching :measurements)
             (fetching-row)
             ;; Chart and infoboxes
             (let [{:keys [measurements units mouseover]} (:chart data)
                   {:keys [hovering-chan mult-chan]} (om/get-state owner)]
               (when (and (seq measurements) (seq units))
                 (let [all-series    (parse measurements units (get-sensors data))
                       unit-groups   (group-by #(-> % first :unit) all-series)
                       all-groups    (vals unit-groups)
                       left-group    (first all-groups)]
                   [:div.col-md-12
                    [:div.col-md-2
                     (for [series left-group]
                       (let [c (chan)]
                         (om/build chart-summary {:measurements (clj->js series)} {:init-state {:chan (tap mult-chan c)}})))]
                    [:div.col-md-8
                     [:div#chart {:style {:width "100%" :height 600}}
                      (om/build chart/chart-figure {:measurements (mapv #(into [] (flatten %)) all-groups)}
                                {:opts {:chan hovering-chan}})]]
                    [:div.col-md-2
                     (when (> (count all-groups) 1)
                       ;; Always max 2 groups as max 2 units
                       (for [series (last all-groups)]
                         (let [c (chan)]
                           (om/build chart-summary {:measurements (clj->js series)} {:init-state {:chan (tap mult-chan c)}}))))]]))))]]]]))))

(when-let [charting (.getElementById js/document "charting")]
  (om/root multiple-properties-chart
           data-model
           {:target charting
            :shared {:history (history/new-history [:entities :sensors :range])}}))
