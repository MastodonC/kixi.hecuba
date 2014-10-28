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
   {:search {:data []
             :fetching false
             :stats {}}
    :properties {:data []
                 :selected #{}
                 :fetching false}
    :sensors {:data []
              :selected #{}
              :fetching false
              :alert {}
              :units {}}
    :chart {:range {}
            :measurements []
            :all-groups []
            :fetching false}}))

(defn fetching-row []
  [:div [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data." ]]])

(defn no-data-row []
  [:div [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "No data." ]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Filtering sensors according to selected properties

(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (->> readings
         (map #(assoc % :parent-device parent-device
                      :id (str (:entity_id parent-device) "-" (:device_id %) "-"(:type %))))
         (filter #(every? seq [(:upper_ts %) (:lower_ts %)])))))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

(defn sensors-for-property [data selected id]
  (let [properties (-> data :properties :data)
        property   (first (filter #(= (:entity_id %) id) properties))
        sensors    (extract-sensors (:devices property))]
    (mapv (fn [s]
            (assoc s
              :property-data (dissoc property :devices)
              :selected (if (some #{(:id s)} selected) true false))) sensors)))

(defn update-sensors [data]
  (let [properties (-> data :properties :data)
        selected   (-> data :sensors :selected)]
    (vec (mapcat #(sensors-for-property data selected (:entity_id %)) properties))))

(defn fetch-sensors-for-property [property]
  (extract-sensors (:devices property)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data fetchers

(defn search-entities
  "Searches for entity using elasticsearch. Doesn't return the entities that are already selected."
  [data query selected-entities]
  (om/update! data [:search :fetching] true)
  (GET (str "/4/entities/?q=" query)
       {:handler (fn [response]
                   (om/update! data [:search :data] (take 20 (remove #(some #{(:entity_id %)} selected-entities) (:entities response))))
                   (om/update! data [:search :stats] {:total_hits (:total_hits response)
                                                      :page (:page response)})
                   (om/update! data [:search :fetching] false))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn get-unit [property-id sensors sensors-for-property]
  (let [selected-sensors     (filter (fn [sensor] (some #(= (:id sensor) %) sensors))
                                     sensors-for-property)]
    (into {} (map #(hash-map (:id %) (:unit %)) selected-sensors))))

(defn fetch-entity
  "Fetches selected property (used on page reload)."
  [data id]
  (when-not (seq (filter #(= (:entity_id %) id) (-> @data :properties :data))) ;; Don't fetch if it's already in data
    (om/update! data [:properties :fetching] true)
    (let [sensors    (-> @data :sensors :selected)]
      (GET (str "/4/entities/" id)
           {:handler (fn [response]
                       (om/transact! data [:properties :data] #(conj % (assoc response :selected true)))
                       (let [sensor-data (fetch-sensors-for-property response)
                             enriched-sensor-data (mapv (fn [s]
                                                          (assoc s
                                                            :property-data (dissoc response :devices)
                                                            :selected (if (some #{(:id s)} sensors) true false))) sensor-data)]
                         (om/update! data [:sensors :data] (vec (concat (-> @data :sensors :data) enriched-sensor-data)))
                         (when (seq sensors)
                           (let [units (get-unit id sensors enriched-sensor-data)]
                             (om/transact! data [:sensors :units] #(conj % units)))))
                       (om/update! data [:properties :fetching] false))
            :headers {"Accept" "application/edn"}
            :response-format :text}))))

;;;;;;;;;;;;;;;;; Measurements ;;;;;;;;;;;;;;;;;

(def amon-date (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ"))

(defn get-description [sensors measurement]
  (-> (filter #(= (:type measurement) (:type %)) sensors) first :parent-device :description))

(defn parse
  "Enriches measurements with unit and description of device and parses timestamp into a JavaScript Date object"
  [measurements units sensors]
  (map (fn [measurements-seq] (map #(assoc % :unit (get units (-> % :sensor))
                                           :description (get-description sensors %)
                                           :timestamp (tf/parse amon-date (:timestamp %))) measurements-seq)) measurements))

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
  (om/update! data [:chart :fetching] true)
  (om/update! data [:chart :measurements] [])
  (doseq [sensor sensors]
    (log "Fetching measurements for sensor: " sensor)
    (let [[entity_id device_id type] (str/split sensor #"-")
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
                           (when (= (-> sensors last) sensor)
                             (let [units         (-> @data :sensors :units)
                                   sensors       (-> @data :sensors :data)
                                   measurements  (-> @data :chart :measurements)
                                   all-series    (parse (filter seq measurements) units sensors)
                                   unit-groups   (group-by #(-> % first :unit) all-series)
                                   all-groups    (vals unit-groups)]
                               (om/update! data [:chart :all-groups] all-groups)
                               (om/update! data [:chart :fetching] false))))
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
        (om/update! data [:chart :measurements] [])
        (om/update! data [:chart :all-groups] [])
        (om/update! data [:sensors :data] [])
        (om/update! data [:sensors :selected] #{}))

      (when sensors
        (let [selected-sensors (into #{} (str/split sensors #";"))]
          (om/update! data [:sensors :selected] selected-sensors)))

      ;; Select/fetch properties
      (when (and (not= entities old-entities)
                 entities)
        (let [entities-seq  (str/split entities #";")]
          (om/update! data [:properties :selected] (into #{} entities-seq))

          ;; on refresh
          (when-not (seq (-> @data :search :data))
            (doseq [entity entities-seq]
              (fetch-entity data entity)))

          ;; Fetch sensors for selected property
          (om/update! data [:sensors :data] (update-sensors @data))))

      (when (and (or (not= start-date old-start-date)
                     (not= end-date old-end-date)))
        (om/update! data [:chart :range] {:start-date start-date :end-date end-date}))

      ;; Update the new active components
      (om/update! data :active-components (common/deep-merge old-nav history-status)))
    (recur)))


(defn chart-feedback-box [cursor owner]
  (om/component
   (html
    [:div cursor])))

(defn change [e owner]
  (om/set-state! owner :text (.. e -target -value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sensors table

(defn sensor-row [sensor owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [selected-chan]}]
      (let [{:keys [type device_id parent-device unit lower_ts upper_ts selected
                    property-data]} sensor
            history   (om/get-shared owner :history)
            entity_id (:entity_id parent-device)
            id        (str entity_id "-" device_id "-" type)]
        (html
         [:tr {:class (when selected "success")
               :onClick (fn [_]
                          (put! selected-chan {:id id :selected (not selected) :unit unit
                                               :sensor-row sensor
                                               :lower_ts lower_ts :upper_ts upper_ts}))}
          [:td (when-let [uri (:uri (first (:photos property-data)))]
                 [:img.img-thumbnail.table-image
                  {:src uri}])]
          [:td (:property_code property-data)]
          [:td type]
          [:td device_id]
          [:td unit]
          [:td (if-let [t (common/unparse-date-str lower_ts "yyyy-MM-dd")] t "")]
          [:td (if-let [t (common/unparse-date-str upper_ts "yyyy-MM-dd")] t "")]])))))

(defn update-sensor [history sensors sensor-row selected id unit lower_ts upper_ts chart-range-chan]
  (let [selected-sensors ((if selected conj disj) (-> @sensors :selected) id)
        [entity_id type device_id] (str/split id #"-")]
    ;; update history
    (history/update-token-ids! history
                               :sensors (if (seq selected-sensors)
                                          (str/join ";" selected-sensors)
                                          nil))
    ;; Update highlighting of the sensor
    (om/update! sensor-row :selected selected)
    ;; update chart default range
    (when (and lower_ts upper_ts selected)
      (put! chart-range-chan {:start-date (common/unparse-date lower_ts "yyyy-MM-dd")
                              :end-date (common/unparse-date upper_ts "yyyy-MM-dd")}))
    ;; Update entities cursor with new selection
    (om/update! sensors :selected selected-sensors)
    ;; update units in chart
    (om/transact! sensors :units (fn [units]
                                   (if selected
                                     (assoc units id unit)
                                     (dissoc units id))))))

(defn allowed-unit? [existing-units new-unit]
  (or (not (seq existing-units))
      (< (count existing-units) 2)
      (some #(= new-unit %) existing-units)))

(defn process-sensor-row-click [history sensors sensor-row selected id unit lower_ts upper_ts chart-range-chan]
  (let [existing-units   (into #{} (vals (-> @sensors :units)))]

    (if selected
      (if (allowed-unit? existing-units unit)
        (update-sensor history sensors sensor-row selected id unit lower_ts upper_ts chart-range-chan)
        (om/update! sensors [:alert] {:status true
                                      :class "alert alert-danger"
                                      :text "Please limit the number of different units to 2."}))
      (update-sensor history sensors sensor-row selected id unit lower_ts upper_ts chart-range-chan))))

(defn sensors-table [sensors owner {:keys [chart-range-chan]}]
  (reify
    om/IInitState
    (init-state [_]
      {:selected-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [selected-chan (om/get-state owner :selected-chan)
            history       (om/get-shared owner :history)]
        (go-loop []
          (let [{:keys [selected id unit lower_ts upper_ts sensor-row]} (<! selected-chan)]
            (process-sensor-row-click history sensors sensor-row selected id unit lower_ts upper_ts chart-range-chan))
          (recur))))
    om/IRenderState
    (render-state [_ {:keys [selected-chan]}]
      (html
       [:div.col-md-12
        (if (:entities (:fetching sensors))
          (fetching-row)
          [:div
           [:div {:id "sensors-unit-alert"}
            (om/build bootstrap/alert (-> sensors :alert))]
           [:table.table.table-hover.table-condensed {:style {:font-size "85%"}}
            [:thead [:tr
                     [:th "Photo"]
                     [:th "Property Code"]
                     [:th "Type"]
                     [:th "Device Id"]
                     [:th "Unit"]
                     [:th "Earliest Event"]
                     [:th "Last Event"]]]
            [:tbody
             (if (-> (:fetching sensors))
               (fetching-row)
               (om/build-all sensor-row (:data sensors) {:key :id :init-state {:selected-chan selected-chan}}))]]])]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Selected properties table

(defn property-row [property owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [selected-chan]}]
      (let [{:keys [property_code project_name project_id property_data entity_id selected]} property
            history  (om/get-shared owner :history)]
        (html
         [:tr {:onClick (fn [_]
                          (om/update! property :selected (not selected))
                          (put! selected-chan {:id entity_id :selected (not selected)}))
               :style {:display (if (:hidden property) "none" "")}
               :class (when selected "success")}
          [:td (when-let [uri (:uri (first (:photos property)))]
                 [:img.img-thumbnail.table-image
                  {:src uri}])]
          [:td property_code]
          [:td [:p {:class "form-control-static col-md-10"} (slugs/postal-address-html property_data)]]
          [:td entity_id]
          [:td project_name]
          [:td project_id]])))))

(defn properties-table [properties owner {:keys [selected-property-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.col-md-12
        (if (:fetching properties)
          (fetching-row)
          [:div.col-md-12 {:style {:overflow "auto"}}
           [:table {:class "table table-hover table-condensed" :style {:font-size "85%"}}
            [:thead
             [:tr
              [:th "Photo"]
              [:th "Property Code"]
              [:th "Property Address"]
              [:th "Property Id"]
              [:th "Project Name"]
              [:th "Project"]]]
            [:tbody
             (om/build-all property-row (:data properties)
                           {:key :entity_id :init-state {:selected-chan selected-property-chan}})]]])]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Property search

(defn search-stats [stats owner]
  (om/component
   (let [hits (:total_hits stats)]
     (html
      [:div {:class (if hits "" "hidden")}
       (str "About " hits " result(s).")]))))

(defn input-box [_ owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [search-chan]}]
      (html
       [:div
        [:div.input-group.input-group-md
         [:input {:type "text"
                  :class "form-control input-md"
                  :on-change (fn [e] (om/set-state! owner :value (.-value (.-target e))))
                  :on-key-press (fn [e] (when (= (.-keyCode e) 13)
                                          (let [value (om/get-state owner :value)]
                                            (when value
                                              (put! search-chan value)))))}]
         [:span.input-group-btn
          [:button {:type "button" :class "btn btn-primary"
                    :on-click (fn [e]
                                (let [value (om/get-state owner :value)]
                                  (when value
                                    (put! search-chan value))))} "Go"]]]]))))

(defn search-row [property owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [selected-search-chan]}]
      (let [{:keys [property_code property_data entity_id project_id project_name selected]} property
            history  (om/get-shared owner :history)]
        (html
         [:tr {:onClick (fn [_]
                          (put! selected-search-chan entity_id))
               :style {:display (if (:hidden property) "none" "")}
               :class (when selected "success")}
          [:td (when-let [uri (:uri (first (:photos property)))]
                 [:img.img-thumbnail.table-image
                  {:src uri}])]
          [:td property_code]
          [:td [:p {:class "form-control-static col-md-10"} (slugs/postal-address-html property_data)]]
          [:td entity_id]
          [:td project_name]
          [:td project_id]])))))

(defn properties-search-table [search owner opts]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       [:div.col-md-12
        (if (:fetching search)
          (fetching-row)
          [:div
           [:h4 "Search for a property:"]
           (om/build input-box nil {:init-state {:search-chan (:search-chan opts)}})
           (om/build search-stats (:stats search))
           [:div.col-md-12 {:style {:overflow "auto"}}
            [:table {:class "table table-hover table-condensed" :style {:font-size "85%"}}
             [:thead
              [:tr
               [:th "Photo"]
               [:th "Property Code"]
               [:th "Property Address"]
               [:th "Property Id"]
               [:th "Project Name"]
               [:th "Project ID"]]]
             [:tbody
              (om/build-all search-row (:data search)
                            {:key :entity_id :init-state {:selected-search-chan (:selected-search-chan opts)}})]]]])]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Chart summaries

(defn chart-summary
  "Show min, max, delta and average of chart data."
  [cursor owner {:keys [border]}]
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
      (let [{:keys [measurements]}     cursor
            mouseover                  (:mouseover state)
            {:keys [value timestamp]}  (:value state)
            [entity_id device_id type] (-> measurements first (aget "sensor") (str/split #"-"))
            description                (-> measurements first (aget "description"))]
        (html
         [:div {:style {:font-size "80%"}}
          (bootstrap/panel border "panel-info"
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

(defn multiple-properties-chart [data owner]
  (reify
    om/IInitState
    (init-state [_]
      (let [hovering-chan (chan (sliding-buffer 100))
            m             (mult hovering-chan)]
        {:hovering-chan hovering-chan
         :mult-chan m
         :search-chan (chan)
         :selected-search-chan (chan)
         :selected-property-chan (chan)
         :chart-range-chan (chan)
         :datetimepicker-chan (chan)}))
    om/IWillMount
    (will-mount [_]
      (let [history     (om/get-shared owner :history)
            m           (mult (history/set-chan! history (chan)))
            tap-history #(tap m (chan))
            {:keys [selected-search-chan chart-range-chan get-data-chan search-chan
                    selected-property-chan datetimepicker-chan]} (om/get-state owner)]
        (history-loop (tap-history) data)
        ;; Search loop
        (go-loop []
          (let [v (<! search-chan)]
            (search-entities data v (-> @data :properties :selected)))
          (recur))
        ;; Selected property in search loop
        (go-loop []
          (let [id                   (<! selected-search-chan)
                search-data          (-> @data :search :data)
                property-data        (-> (filter #(= id (:entity_id %)) search-data) first)
                selected-properties  (-> @data :properties :selected)]
            ;; Move from search to properties
            (history/update-token-ids! history
                                       :entities (str/join ";" (conj selected-properties id))
                                       true)
            (om/transact! data [:properties :data] (fn [d] (conj d (-> property-data
                                                                       (assoc :selected true)))))
            (om/transact! data [:search :data] (fn [d] (into [] (remove #(= (:entity_id %) id) d)))))
          (recur))
        ;; Deselected property in properties loop
        (go-loop []
          (let [{:keys [id selected]} (<! selected-property-chan)
                selected-properties ((if selected conj disj) (-> @data :properties :selected) id)
                selected-sensors (filter #(let [[entity_id _ _] (str/split % "-")]
                                            (some #{entity_id} selected-properties)) (-> @data :sensors :selected))]
            (om/transact! data [:properties :data] (fn [d] (into [] (remove #(= (:entity_id %) id) d))))
            (history/update-token-ids! history
                                       :entities (if (seq selected-properties)
                                                   (str/join ";" selected-properties)
                                                   nil)
                                       :keep-all)
            ;; update sensors cursor and history accordingly
            (om/transact! data [:sensors :units] #(select-keys % selected-sensors))
            (history/update-token-ids! history :sensors (str/join ";" selected-sensors) :keep-all))
          (recur))
        ;; Chart range loop (getting upper_ts and lower_ts)
        (go-loop []
          (let [range (<! chart-range-chan)]
            (om/update! data [:chart :range] range))
          (recur))
        ;; Chart data button
        (go-loop []
          (let [{:keys [start-date end-date] :as range} (<! datetimepicker-chan)
                sensors   (-> @data :sensors :selected)]
            (log "Fetching for sensors: " sensors "start: " start-date "end: " end-date)
            (when (and sensors end-date start-date)
              (om/update! data [:chart :range] range)
              (fetch-measurements data sensors start-date end-date)))
          (recur))))
    om/IRenderState
    (render-state [_ {:keys [hovering-chan mult-chan selected-search-chan chart-range-chan search-chan
                             selected-property-chan datetimepicker-chan]}]
      (html
       [:div.col-md-12 {:style {:padding-top "10px"}}
        [:div.panel-group {:id "accordion"}
         (bootstrap/accordion-panel  "#collapseOne" "collapseOne" "Search"
                                     (om/build properties-search-table (:search data)
                                               {:opts {:selected-search-chan selected-search-chan
                                                       :search-chan search-chan}}))
         (bootstrap/accordion-panel  "#collapseTwo" "collapseTwo" "Selected Properties"
                                     (om/build properties-table (:properties data)
                                               {:opts {:selected-property-chan selected-property-chan}}))
         (bootstrap/accordion-panel  "#collapseThree" "collapseThree" "Sensors"
                                     (om/build sensors-table (:sensors data) {:opts {:chart-range-chan chart-range-chan}}))]
        [:br]
        [:div.panel-group
         [:div.panel.panel-default
          [:div.panel-heading
           [:h3.panel-title "Chart"]]
          [:div.panel-body
           [:div {:id "date-picker" :class "col-md-6 col-md-offset-3"}
            (om/build dtpicker/datetime-picker (-> data :chart :range) {:init-state {:date-range-chan datetimepicker-chan}})]
           (if (-> data :chart :fetching)
             (fetching-row)
             ;; Chart and infoboxes
             (let [{:keys [all-groups]} (:chart data)
                   {:keys [units]} (:sensors data)
                   left-border-colours   ["#6baed6" "#4292c6" "#2171b5" "#08519c" "#08306b"]
                   right-border-colours  ["#fd8d3c" "#f16913" "#d94801" "#a63603" "#7f2704"]]
               (if (and (some seq all-groups) (seq units))
                 (let [left-group        (first all-groups)
                       left-with-colours (zipmap left-group (cycle left-border-colours))]
                   [:div.col-md-12
                    [:div.col-md-2
                     (for [[series colours] left-with-colours]
                       (let [c (chan (sliding-buffer 100))]
                         (om/build chart-summary {:measurements (clj->js series)} {:init-state {:chan (tap mult-chan c)}
                                                                                   :opts {:border colours}})))]
                    [:div.col-md-8
                     [:div#chart {:style {:width "100%" :height 600}}
                      (om/build chart/chart-figure {:measurements (mapv #(into [] (flatten %)) all-groups)}
                                {:opts {:chan hovering-chan}})]]
                    [:div.col-md-2
                     (when (> (count all-groups) 1)
                       (let [right-group        (last all-groups)
                             right-with-colours (zipmap right-group (cycle right-border-colours))]
                         ;; Always max 2 groups as max 2 units
                         (for [[series colours] right-with-colours]
                           (let [c (chan (sliding-buffer 100))]
                             (om/build chart-summary {:measurements (clj->js series)} {:init-state {:chan (tap mult-chan c)}
                                                                                        :opts {:border colours}})))))]])
                 (no-data-row))))]]]]))))

(when-let [charting (.getElementById js/document "charting")]
  (om/root multiple-properties-chart
           data-model
           {:target charting
            :shared {:history (history/new-history [:entities :sensors :range])}}))
