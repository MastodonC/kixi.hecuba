(ns kixi.hecuba.tabs.programmes
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require
     [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
     [goog.userAgent :as agent]
     [ajax.core :refer (GET POST)]
     [clojure.string :as str]
     [kixi.hecuba.navigation :as nav]
     [kixi.hecuba.widgets.datetimepicker :as dtpicker]
     [kixi.hecuba.widgets.chart :as chart]
     [kixi.hecuba.bootstrap :as bs]
     [kixi.hecuba.common :refer (index-of map-replace find-first interval)]
     [kixi.hecuba.history :as history]
     [kixi.hecuba.model :refer (app-model)]
     [sablono.core :as html :refer-macros [html]]))

;; (enable-console-print!)

;; our banner is 50px so we need to tweak the scrolling
(defn fixed-scroll-to-element [element]
  (let [rect (-> (.getElementById js/document element)
                 .getBoundingClientRect)
        top (.-top rect)]
    (.scrollBy js/window 0 (- top 50))))

(defn scroll-to-element [element]
  (-> (.getElementById js/document element)
      .scrollIntoView))

(defn back-to-programmes [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (history/update-token-ids! history :projects nil)
    (history/update-token-ids! history :programmes nil)
    (fixed-scroll-to-element "programmes-div")))

(defn back-to-projects [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (history/update-token-ids! history :projects nil)
    (fixed-scroll-to-element "projects-div")))

(defn back-to-properties [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (fixed-scroll-to-element "properties-div")))

(defn back-to-devices [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (fixed-scroll-to-element "devices-div")))

(defn update-when [x pred f & args]
  (if pred (apply f x args) x))

(defn uri-for-selection-change
  "Returns the uri to load because of change of selection. Returns nil
   if no change to selection"
  [current-selected selection-key template nav-event]
  (let [ids          (-> nav-event :args :ids)
        new-selected (get ids selection-key)]
    (when (or (nil? current-selected)
              (nil? new-selected)
              (not= current-selected
                    new-selected))
      (vector new-selected
              (map-replace template ids)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Slugs
(defn slugify-programme [programme]
  (assoc programme :slug (:name programme)))

(defn slugify-project "Create a slug for projects in the UI"
  [project]
  (assoc project :slug (:name project)))

(defn slugify-property
  "Create a slug for a property in the UI"
  [property]
  (let [property_data (:property_data property)]
    (assoc property
      :slug
      (apply str (str/join
                  ", "
                  (->> (vector (:property_code property)
                               (:address_street_two property_data)
                               (:address_city property_data)
                               (:address_code property_data)
                               (:address_country property_data))
                       (keep identity)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Fetchers
(defn fetch-programmes [data]
  (om/update! data [:programmes :fetching] :fetching)
  (GET (str "/4/programmes/")
       {:handler  (fn [x]
                    (println "Fetching programmes.")
                    (om/update! data [:programmes :data] (mapv slugify-programme x))
                    (om/update! data [:programmes :fetching] (if (empty? x) :no-data :has-data))
                    (om/update! data [:programmes :selected] nil))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data [:programmes :fetching] :error)
                         (om/update! data [:programmes :error-status] status)
                         (om/update! data [:programmes :error-text] status-text))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn fetch-projects [programme-id data]
  (om/update! data [:projects :programme_id] programme-id)
  (om/update! data [:projects :fetching] :fetching)
  (GET (str "/4/programmes/" programme-id "/projects/")
       {:handler  (fn [x]
                    (println "Fetching projects for programme: " programme-id)
                    (om/update! data [:projects :data] (mapv slugify-project x))
                    (om/update! data [:projects :fetching] (if (empty? x) :no-data :has-data))
                    (om/update! data [:projects :selected] nil))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data [:projects :fetching] :error)
                         (om/update! data [:projects :error-status] status)
                         (om/update! data [:projects :error-text] status-text))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn fetch-properties [project-id data]
  (om/update! data [:properties :project_id] project-id)
  (om/update! data [:properties :fetching] :fetching)
  (GET (str "/4/projects/" project-id "/properties/")
       {:handler  (fn [x]
                    (println "Fetching properties for project: " project-id)
                    (om/update! data [:properties :data] (mapv slugify-property x))
                    (om/update! data [:properties :fetching] (if (empty? x) :no-data :has-data))
                    (om/update! data [:properties :selected] nil))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data [:properties :fetching] :error)
                         (om/update! data [:properties :error-status] status)
                         (om/update! data [:properties :error-text] status-text))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn flatten-device [device]
  (println "Flattening: " device)
  (let [device-keys (->> device keys (remove #(= % :readings)))
            parent-device (select-keys device device-keys)
            readings (:readings device)] 
        (map #(assoc % :parent-device parent-device) readings)))

(defn extract-sensors [devices]
  (println "Devices to extract sensors from: " devices)
  (let [sensors (vec (mapcat flatten-device devices))]
    (println "Flattened sensors: " sensors)
    sensors))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; History loop - this drives the fetches and clear downs
(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event (<! history-channel)
          {:keys [programmes projects properties]} (-> nav-event :args :ids)
          old-nav (:active-components @data)
          old-programmes (:programmes old-nav)
          old-projects (:projects old-nav)
          old-properties (:properties old-nav)]
      (println "Old Programmes: " old-programmes " Old Projects: " old-projects " Old Properties: " old-properties)
      (println "New Programmes: " programmes " New Projects: " projects " New Properties: " properties)
      (println "New Active Components: " (-> nav-event :args :ids))

      ;; Clear down
      (when (and old-programmes
                 (nil? programmes))
        (println "Clearing projects.")
        (om/update! data [:projects :data] [])
        (om/update! data [:projects :programme_id] nil))

      (when (and old-projects
                 (nil? projects))
        (println "Clearing properties.")
        (om/update! data [:properties :data] [])
        (om/update! data [:properties :project_id] nil))

      (when (and old-properties
                 (nil? properties))
        (println "Clearing devices, sensors and measurements.")
        (om/update! data [:property-details :data] {})
        (om/update! data [:property-details :property_id] nil)
        (om/update! data [:devices :data] [])
        (om/update! data [:sensors :data] [])
        (om/update! data [:measurements :data] []))

      ;; Fetchers
      (when-not programmes
        (om/update! data [:programmes :selected] nil)
        (fetch-programmes data))
      
      (when (and programmes
                 (not= programmes old-programmes))
        (println "Setting selected programme to: " programmes)
        (om/update! data [:programmes :selected] programmes)
        (fetch-projects programmes data))

      (when (and projects
                 (not= projects old-projects))
        (println "Setting selected project to: " projects)
        (om/update! data [:projects :selected] projects)
        (fetch-properties projects data))

      ;; property handling is special as it gets a tree of data
      (when (and properties
                 (not= properties old-properties))
        (om/update! data [:properties :selected] properties)
        (om/update! data [:property-details :property_id] properties)
        (let [property-details (->> @data
                                    :properties
                                    :data
                                    (filter #(= (:id %) properties))
                                    first)
              devices (get property-details :devices [])]
          (println "property-details keys: " (keys property-details))
          (om/update! data [:property-details :data] property-details)
          (om/update! data [:sensors :data] (extract-sensors devices))))
      
      ;; Update the new active components
      (om/update! data :active-components (-> nav-event :args :ids)))
    (recur)))

(defn selected-range-change
  [selected selection-key {{ids :ids search :search} :args}]
  (let [new-selected (get ids selection-key)]
    (when (or (nil? selected)
              (not= selected new-selected))
      (vector new-selected ids search))))

(defn chart-feedback-box [cursor owner]
  (om/component
   (dom/div nil cursor)))

(defn chart-ajax [in data {:keys [selection-key content-type]}]
  (go-loop []
    (let [nav-event (<! in)]
      (when-let [[new-range ids search] (selected-range-change (:range @data)
                                                               selection-key
                                                               nav-event)]
        (let [[start-date end-date] search
              entity_id        (get ids :properties)
              sensor_id        (get ids :sensors)
              [type device_id] (str/split sensor_id #"-")]

          (om/update! data :range {:start-date start-date :end-date end-date})
          (om/update! data :sensors sensor_id)
          (om/update! data :measurements [])

          ;; TODO ajax call should not be made on each change, only on this particular cursor update.
          (when (and (not (empty? start-date))
                     (not (empty? end-date))
                     (not (nil? device_id))
                     (not (nil? entity_id))
                     (not (nil? type)))

            ;; FIXME Should be a multimethod
            (let [url (case (interval start-date end-date)
                        :raw (str "/4/entities/" entity_id "/devices/" device_id "/measurements/"
                                  type "?startDate=" start-date "&endDate=" end-date)
                        :hourly_rollups (str "/4/entities/" entity_id "/devices/" device_id "/hourly_rollups/"
                                             type "?startDate=" start-date "&endDate=" end-date)
                        :daily_rollups (str "/4/entities/" entity_id "/devices/" device_id "/daily_rollups/"
                                            type "?startDate=" start-date "&endDate=" end-date))]
              (GET url
                   {:handler #(om/update! data :measurements %)
                    :headers {"Accept" "application/json"}
                    :response-format :json
                    :keywords? true}))))))
    (recur)))

(defn device-detail [{:keys [selected data] :as cursor} owner]
  (om/component
   (let [row      (first (filter #(= (:id %) selected) data))]
     (let [{:keys [description name
                   latitude longitude]} (:location row)]
       (dom/div nil
                (dom/h3 nil (apply str  "Device Detail "  (interpose \/ (remove nil? [description name])))) ;; TODO add a '-'
                (dom/p nil (str "Latitude: " latitude))
                (dom/p nil (str "Longitude: " longitude)))))))

(defn row-for [{:keys [selected data]}]
  (find-first #(= (:id %) selected) data))

(defn title-for [cursor & {:keys [title-key] :or {title-key :slug}}]
  (let [row (row-for cursor)]
    (get-in row (if (vector? title-key) title-key (vector title-key)))))

(defn title-for-sensor [{:keys [selected]}]
  (let [[type _] (str/split selected #"-")]
    type))

(defn error-row [data]
  [:div.row
   [:div.col-md-12.text-center
    [:p.lead {:style {:padding-top 30}}
     "There has been an error. Please contact " [:a {:href "mailto:support@mastodonc.com"} "support@mastodonc.com"]]
    [:p "Error Code: " (:error-status data) " Message: " (:error-text data)]]])

(defn no-data-row [data]
  [:div.row [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "No data available for this selection."]]])

(defn fetching-row [data]
  [:div.row [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data for selection." ]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; programmes
(defn programmes-table [data owner]
  (reify
    om/IRender
    (render [_]
      (let [programmes (-> data :programmes)
            table-id   "programme-table"
            history    (om/get-shared owner :history)]
        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr
            [:th "Name"]
            [:th "Organisations"]
            [:th "ID"]
            [:th "Created At"]]]
          [:tbody
           (for [row (sort-by :name (:data programmes))]
             (let [{:keys [id lead_organisations name description created_at]} row]
               [:tr {:onClick (fn [_ _]
                                (om/update! programmes :selected id)
                                (history/update-token-ids! history :programmes id)
                                (fixed-scroll-to-element "projects-div"))
                     :className (if (= id (:selected programmes)) "success")
                     :id (str table-id "-selected")}
                [:td name]
                [:td lead_organisations]
                [:td id]
                [:td created_at]]))]])))))

(defn programmes-div [data owner]
  (reify
    om/IRender
    (render [_]
      (html
       [:div.row#programmes-div
        [:div {:class "col-md-12"}
         [:h1 "Programmes"]
         (om/build programmes-table data)]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; projects
(defmulti projects-table-html (fn [projects owner] (:fetching projects)))
(defmethod projects-table-html :fetching [projects owner]
  (fetching-row projects))

(defmethod projects-table-html :no-data [projects owner]
  (no-data-row projects))

(defmethod projects-table-html :error [projects owner]
  (error-row projects))

(defmethod projects-table-html :has-data [projects owner]
  (let [table-id   "projects-table"
        history    (om/get-shared owner :history)]
    [:div.row
     [:div.col-md-12
      [:table {:className "table table-hover"}
       [:thead
        [:tr [:th "Name"] [:th "Type"] [:th "Description"] [:th "Created At"] [:th "Organisation"] [:th "Project Code"]]]
       [:tbody
        (for [row (sort-by :id (:data projects))]
          (let [{:keys [id name type_of description created_at organisation project_code]} row]
            [:tr {:onClick (fn [_ _]
                             (om/update! projects :selected id)
                             (history/update-token-ids! history :projects id)
                             (fixed-scroll-to-element "properties-div"))
                  :className (if (= id (:selected projects)) "success")
                  :id (str table-id "-selected")}
             [:td name]
             [:td type_of]
             [:td description]
             [:td created_at]
             [:td organisation]
             [:td project_code]]))]]]]))

(defmethod projects-table-html :default [projects owner]
  [:div.row [:div.col-md-12]])

(defn projects-table [projects owner]
  (reify
    om/IRender
    (render [_]
      (html (projects-table-html projects owner)))))

(defn projects-div [data owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div.row#projects-div
          [:div {:class (str "col-md-12 " (if (:programme_id projects) "" "hidden"))}
           [:h2 "Projects"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (title-for programmes)]]]
           (om/build projects-table projects {:opts {:histkey :projects}})]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; properties
(defmulti properties-table-html (fn [properties owner] (:fetching properties)))
(defmethod properties-table-html :fetching [properties owner]
  (fetching-row properties))

(defmethod properties-table-html :no-data [properties owner]
  (no-data-row properties))

(defmethod properties-table-html :error [properties owner]
  (error-row properties))

(defn postal-address
  ([property_data separator]
     (str/join
      separator
      (keep identity [(:address_street_two property_data)
                      (:address_city property_data)
                      (:address_code property_data)
                      (:address_country property_data)])))
  ([property_data]
     (postal-address property_data ", ")))

(defmethod properties-table-html :has-data [properties owner]
  (let [table-id "properties-table"
        history  (om/get-shared owner :history)]
    [:div.col-md-12
     [:table {:className "table table-hover"}
      [:thead
       [:tr [:th "ID"] [:th "Type"] [:th "Address"] [:th "Region"] [:th "Ownership"] [:th "Technologies"] [:th "Monitoring Hierarchy"]]]
      (for [property-details (sort-by #(-> % :property_code) (:data properties))]
        (let [property_data (:property_data property-details)
              id            (:id property-details)]
          [:tr
           {:onClick (fn [_ _]
                       (om/update! properties :selected id)
                       (history/update-token-ids! history :properties id))
            :className (if (= id (:selected properties)) "success")
            :id (str table-id "-selected")}
           [:td (:property_code property-details)]
           [:td (:property_type property_data)]
           [:td (postal-address property_data)]
           [:td (:address_region property_data)]
           [:td (:ownership property_data)]
           [:td (for [ti (:technology_icons property_data)]
                  [:img.tmg-responsive {:src ti :width 40 :height 40}])]
           [:td (:monitoring_hierarchy property_data)]]))]]))

(defmethod properties-table-html :default [properties owner]
  [:div.row [:div.col-md-12]])

(defn properties-table [properties owner]
  (reify
    om/IRender
    (render [_]
      (html (properties-table-html properties owner)))))

(defn properties-div [data owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div.row#properties-div
          [:div {:class (str "col-md-12 " (if (:project_id properties) "" "hidden"))}
           [:h2 "Properties"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (title-for projects)]]]
           (om/build properties-table properties {:opts {:histkey :properties}})]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sensors
(defn status-label [status]
  (if (= status "OK")
    [:span {:class "label label-success"} status]
    [:span {:class "label label-danger"} status]))

(defn sensors-table [data owner {:keys [histkey path]}]
  (reify
    om/IRender
    (render [_]
      (let [sensors (:sensors data)
            chart   (:chart data)
            history (om/get-shared owner :history)
            table-id "sensors-table"]

        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr [:th "Type"] [:th "Unit"] [:th "Period"] [:th "Device"] [:th "Status"]]]
          [:tbody
           (for [row (sort-by :type (-> sensors :data))]
             (let [{:keys [device_id type unit period status]} row
                   id (str type "-" device_id)]
               [:tr {:onClick (fn [_ _]
                                (om/update! sensors :selected id)
                                (om/update! chart :sensor id)
                                (om/update! chart :unit unit)
                                (history/update-token-ids! history :sensors id))
                     :className (if (= id (:selected sensors)) "success")
                     :id (str table-id "-selected")}
                [:td type]
                [:td unit]
                [:td period]
                [:td device_id]
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
        [:div {:id "chart-div"}
         [:div {:id "date-picker"}
          (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
         (om/build chart-feedback-box (get-in data [:chart :message]))
         (om/build chart-summary (:chart data))
         [:div {:className "well" :id "chart" :style {:width "100%" :height 600}}
          (om/build chart/chart-figure (:chart data))]]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; property-details
(defn postal-address-html
  [property_data]
  (interpose
   [:br ]
   (keep identity [(:address_street_two property_data)
                   (:address_city property_data)
                   (:address_code property_data)
                   (:address_country property_data)])))

(defn property-details-div [data owner]
  (reify
    om/IRender
    (render [_]
      (let [property-details (-> data :property-details :data)
            property_data (:property_data property-details)]
        (html [:div {:class (str "col-md-12" (if (:property_id (:property-details data)) "" " hidden"))}
               [:h2 "Property Details"]
               (bs/panel
                (:slug property-details)
                [:div.col-md-12
                 [:div.col-md-4
                  [:dl.dl-horizontal
                   [:dt "Address"] [:dd (postal-address-html property_data)]
                   [:dt "Property Type"] [:dd (:property_type property_data)]
                   [:dt "Built Form"] [:dd (:built_form property_data)]
                   [:dt "Age"] [:dd (:age property_data)]
                   [:dt "Ownership"] [:dd (:ownership property_data)]
                   [:dt "Practical Completion Date"] [:dd (:practical_completion_date property_data)]
                   [:dt "Construction Date"] [:dd (:construction_date property_data)]
                   [:dt "Conservation Area"] [:dd (:conservation_area property_data)]
                   [:dt "Listed Building"] [:dd (:listed property_data)]
                   [:dt "Terrain"] [:dd (:terrain property_data)]
                   [:dt "Degree Day Region"] [:dd (:degree_day_region property_data)]
                   ]]
                 [:div.col-md-2
                  (when-let [pic (:path (first (:photos property-details)))]
                    [:img.img-thumbnail.tmg-responsive
                     {:src (str "https://s3-us-west-2.amazonaws.com/get-embed-data/" pic)}])]
                 [:div.col-md-6
                  [:dl.dl-horizontal
                   [:dt "Project Phase"] [:dd (:project_phase property_data)]
                   [:dt "Other Notes"] [:dd (:other_notes property_data)]
                   [:dt "Monitoring Hierarchy"] [:dd (:monitoring_hierarchy property_data)]
                   [:dt "Monitoring Policy"] [:dd (:monitoring_policy property_data)]
                   ]]
                 [:div.col-md-12
                  [:h3 "Description"]
                  [:p (:description property_data)]
                  
                  [:h3 "Project Summary"]
                  [:p (:project_summary property_data)]

                  [:h3 "Project Team"]
                  [:p (:project_team property_data)]
                  
                  [:h3 "Design Strategy"]
                  [:p (:design_strategy property_data)]
                  
                  [:h3 "Energy Strategy"]
                  [:p (:energy_strategy property_data)]]]
                [:div.col-md-12
                 (om/build sensors-div data)]
                )
               ])
        ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main View
(defn programmes-tab [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [history     (om/get-shared owner :history)
            property-chan (om/get-shared owner :property-chan)
            m           (mult (history/set-chan! history (chan)))
            tap-history #(tap m (chan))]

        ;; handle navigation changes
        (history-loop (tap-history) data)

        (chart-ajax (tap-history)
                    (:chart data)
                    {:template "/4/entities/:properties/devices/:devices/measurements?startDate=:start-date&endDate=:end-date"
                     :content-type  "application/json"
                     :selection-key :range})))
    om/IRender
    (render [_]

      (html [:div
             (om/build programmes-div data)
             (om/build projects-div data)
             (om/build properties-div data)
             (om/build property-details-div data)
             ;; (om/build devices-div data)
             ;; 
             ]))))
