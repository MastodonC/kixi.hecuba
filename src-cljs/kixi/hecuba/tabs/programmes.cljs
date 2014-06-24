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

(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event (<! history-channel)]
      (println "Old Active Components: " (:active-components @data))
      (println "New Active Components: " (-> nav-event :args :ids))
      (om/update! data :active-components (-> nav-event :args :ids)))
    (recur)))

(defn property-loop [property-chan data]
  (go-loop []
    (let [property-details (<! property-chan)]
      (om/update! data [:property-details :data] property-details))))

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
  [:div.row [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching properties for selected project." ]]])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; programmes
(defn slugify-programme [programme]
  (assoc programme :slug (:name programme)))

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
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [programmes active-components]} data
            programme_id (:programmes active-components)]
        (when-not programme_id
          (om/update! programmes :selected nil))

        (if (not (seq (:data programmes)))
          (GET (str "/4/programmes/")
               {:handler  (fn [x]
                            ;; (println "Fetching programmes.")
                            (om/update! programmes :data (mapv slugify-programme x))
                            (om/update! programmes :selected nil))
                ;; TODO: Add Error Handler
                :headers {"Accept" "application/edn"}
                :response-format :text}))

        ;; handle selection on programmes table (this should be nil if called)
        (om/update! programmes :selected (:programmes active-components))))
    om/IRender
    (render [_]
      (html
       [:div.row#programmes-div
        [:div {:class "col-md-12"}
         [:h1 "Programmes"]
         (om/build programmes-table data)]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; projects
(defn slugify-project "Create a slug for projects in the UI"
  [project]
  (assoc project :slug (:name project)))

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
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [programmes projects active-components]} data
            new-programme_id (:programmes active-components)]

        ;; handle selection in programme table
        (when-not new-programme_id
          (om/update! projects :data [])
          (om/update! projects :selected nil))

        ;; get the data if we have a new id
        (if (and new-programme_id
                 (not (= (:programme_id projects) new-programme_id)))
          (do
            (om/update! projects :fetching :fetching)
            (GET (str "/4/programmes/" new-programme_id "/projects/")
                 {:handler  (fn [x]
                              ;; (println "Fetching projects for programme: " new-programme_id)
                              (om/update! projects :data (mapv slugify-project x))
                              (om/update! projects :fetching (if (empty? x) :no-data :has-data))
                              (om/update! projects :selected nil))
                  :error-handler (fn [{:keys [status status-text]}]
                                   (om/update! projects :fetching :error)
                                   (om/update! projects :error-status status)
                                   (om/update! projects :error-text status-text))
                  :headers {"Accept" "application/edn"}
                  :response-format :text})))

        ;; update our current id with the new one
        (om/update! projects :programme_id new-programme_id)

        ;; handle selection in projects table
        (om/update! projects :selected (:projects active-components))))
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
(defn slugify-property
  "Create a slug for a property in the UI"
  [property]
  (assoc property :slug (apply str (interpose ", " (->> (vector (:property_code property) (:address_street_two property))
                                                        (keep identity)
                                                        (remove empty?))))))

(defmulti properties-table-html (fn [properties owner] (:fetching properties)))
(defmethod properties-table-html :fetching [properties owner]
  (fetching-row properties))

(defmethod properties-table-html :no-data [properties owner]
  (no-data-row properties))

(defmethod properties-table-html :error [properties owner]
  (error-row properties))

(defn postal-address
  ([property_data separator]
     (println "Separator: " separator " Property Data: " property_data)
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
        history  (om/get-shared owner :history)
        property-chan (om/get-shared owner :property-chan)]
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
                       (history/update-token-ids! history :properties id)
                       (put! property-chan property-details))
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
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [projects properties active-components]} data
            new-project_id (:projects active-components)]

        ;; handle selection in projects table
        (when-not new-project_id
          (om/update! properties :data [])
          (om/update! properties :selected nil))

        (if (and new-project_id
                 (not (= (:project_id properties) new-project_id)))
          (do
            (om/update! properties :fetching :fetching)
            (GET (str "/4/projects/" new-project_id "/properties/")
                 {:handler  (fn [x]
                              ;; (println "Fetching properties for project: " new-project_id)
                              (om/update! properties :data (mapv slugify-property x))
                              (om/update! properties :fetching (if (empty? x) :no-data :has-data))
                              (om/update! properties :selected nil))
                  :error-handler (fn [{:keys [status status-text]}]
                                   (om/update! properties :fetching :error)
                                   (om/update! properties :error-status status)
                                   (om/update! properties :error-text status-text))
                  :headers {"Accept" "application/edn"}
                  :response-format :text})))
        (om/update! properties :project_id new-project_id)

        ;; handle selection on properties table
        (om/update! properties :selected (:properties active-components))))
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
        (html [:div.col-md-12
               [:h2 "Property Details"]
               (bs/panel
                (:slug property-details)
                [:div.col-md-12
                 [:div.col-md-4
                  [:dl.dl-horizontal
                   [:dt "Address"] [:dd (postal-address-html property_data)]
                   ]]
                 [:div.col-md-2
                  (when-let [pic (:path (first (:photos property-details)))]
                    [:img.img-thumbnail.tmg-responsive
                     {:src (str "https://s3-us-west-2.amazonaws.com/get-embed-data/" pic)}])]
                 [:div.col-md-6
                  [:dl.dl-horizontal
                   [:dt "Address"] [:dd (postal-address-html property_data)]
                   ]]
                 [:div.col-md-12
                  [:h3 "Design Strategy"]
                  [:p (:design_strategy property_data)]
                  
                  [:h3 "Energy Strategy"]
                  [:p (:energy_strategy property_data)]
                  ]
                 ]
                )]
              
              )))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; devices
(defn slugify-device
  "Create a user friendly slug for a device"
  [device]
  (assoc device :slug (apply str (interpose ", " (->> (vector (:name device) (:description device))
                                                      (keep identity)
                                                      (remove empty?))))))

(defmulti devices-table-html (fn [devices owner] (:fetching devices)))
(defmethod devices-table-html :fetching [devices owner]
  (fetching-row devices))

(defmethod devices-table-html :no-data [devices owner]
  (no-data-row devices))

(defmethod devices-table-html :error [devices owner]
  (error-row devices))

(defmethod devices-table-html :has-data [devices owner]
  (let [table-id   "devices-table"
        history    (om/get-shared owner :history)]
    [:div.row
     [:div.col-md-12
      [:table {:className "table table-hover"}
       [:thead
        [:tr [:th "Name"] [:th "Description"] [:th "Privacy"]]]
       [:tbody
        (for [row (sort-by :name (:data devices))]
          (let [{:keys [id location description privacy]} row
                name (:name location)]
            [:tr {:onClick (fn [_ _]
                             (om/update! devices :selected id)
                             (history/update-token-ids! history :devices id)
                             (fixed-scroll-to-element "sensors-div"))
                  :className (if (= id (:selected devices)) "success")
                  :id (str table-id "-selected")}
             [:td name]
             [:td description]
             [:td privacy]]))]]]]))

(defmethod devices-table-html :default [devices owner]
  [:div.row [:div.col-md-12]])

(defn devices-table [devices owner]
  (reify
    om/IRender
    (render [_]
      (html (devices-table-html devices owner)))))

(defn devices-div [data owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [properties devices active-components]} data
            new-property-id (:properties active-components)]

        ;; handle selection properties table
        (when-not new-property-id
          (om/update! devices :data [])
          (om/update! devices :selected nil))

        (if (and new-property-id
                 (not (= (:property_id devices) new-property-id)))
          (do
            (om/update! devices :fetching :fetching)
            (GET (str "/4/entities/" new-property-id "/devices/")
                 {:handler  (fn [x]
                              ;; (println "Fetching devices for property: " new-property-id)
                              (om/update! devices :fetching false)
                              (om/update! devices :data (mapv slugify-device x))
                              (om/update! devices :fetching (if (empty? x) :no-data :has-data))
                              (om/update! devices :selected nil))
                  :error-handler (fn [{:keys [status status-text]}]
                                   (om/update! devices :fetching :error)
                                   (om/update! devices :error-status status)
                                   (om/update! devices :error-text status-text))
                  :headers {"Accept" "application/edn"}
                  :response-format :text})))
        (om/update! devices :property_id new-property-id)

        ;; handle selection in devices table
        (om/update! devices :selected (:devices active-components))))
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties devices active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div.row#devices-div
          [:div {:class (str "col-md-12 " (if (:property_id devices) "" "hidden"))}
           [:h2  "Devices"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (title-for projects)]]
            [:li [:a
                  {:onClick (back-to-properties history)}
                  (title-for properties)]]]
           (om/build devices-table devices {:opts {:histkey :devices}})]])))))

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
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [sensors (:sensors data)
            chart   (:chart data)
            cols    (get-in sensors [:header :cols])
            history (om/get-shared owner :history)
            table-id "sensors-table"]

        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr [:th "Type"] [:th "Unit"] [:th "Period"] [:th "Device"] [:th "Status"]]]
          [:tbody
           (for [row (sort-by :type (-> sensors :data :readings))]
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
           [:div.row#summary-stats
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
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [sensors active-components]} data
            new-device_id                       (:devices active-components)
            property_id                         (:properties active-components)]

        ;; handle selection properties table
        (when-not new-device_id
          (om/update! sensors :data [])
          (om/update! sensors :selected nil))

        (if (and new-device_id
                 (not= (:device_id sensors) new-device_id))
          (do
            (om/update! sensors :fetching true)
            ;; "/4/entities/:properties/devices/:devices"
            (GET (str "/4/entities/" property_id "/devices/" new-device_id)
                 {:handler  (fn [x]
                              ;; (println "Fetching sensors for device: " new-device_id)
                              (om/update! sensors :fetching false)
                              (om/update! sensors :data x)
                              (om/update! sensors :selected nil))
                  :error-handler (fn [{:keys [status status-text]}]
                                   (om/update! sensors :fetching false)
                                   (om/update! sensors :error-status status)
                                   (om/update! sensors :error-text status-text))
                  :headers {"Accept" "application/edn"}})))
        (om/update! sensors :device_id new-device_id)

        ;; handle selection in sensors table
        (om/update! sensors :selected (:sensors active-components))))
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties devices sensors active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div.row#sensors-div
          [:div {:class (str "col-md-12 "  (if (:device_id sensors) "" "hidden"))}
           [:h2 {:id "sensors"} "Sensors"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (title-for projects)]]
            [:li [:a
                  {:onClick (back-to-properties history)}
                  (title-for properties)]]
            [:li [:a
                  {:onClick (back-to-devices history)}
                  (title-for devices)]]]
           (om/build sensors-table data {:opts {:histkey :sensors
                                                :path    :readings}})
           (if (or (not agent/IE)
                   (agent/isVersionOrHigher 9))
             [:div {:id "chart-div"}
              [:div {:id "date-picker"}
               (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
              (om/build chart-feedback-box (get-in data [:chart :message]))
              (om/build chart-summary (:chart data))
              [:div {:className "well" :id "chart" :style {:width "100%" :height 600}}
               (om/build chart/chart-figure (:chart data))]]
             [:div.col-md-12.text-center
              [:p.lead {:style {:padding-top 30}}
               "Charting in Internet Explorer version " agent/VERSION " coming soon."]])]])))))

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

        (property-loop property-chan data)

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
             ;; (om/build device-detail devices)
             ;; (om/build sensors-div data)
             ;; (om/build sensor/define-data-set-button data)

             ]))))
