(ns kixi.hecuba.tabs.programmes
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [cljs.reader :as reader]
   [goog.userAgent :as agent]
   [ajax.core :refer (GET POST)]
   [clojure.string :as str]
   [cljs-time.core :as t]
   [cljs-time.format :as tf]
   [kixi.hecuba.navigation :as nav]
   [kixi.hecuba.bootstrap :as bs]
   [kixi.hecuba.common :refer (index-of map-replace find-first interval)]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.tabs.property-details :as property-details]
   [sablono.core :as html :refer-macros [html]]))

(when (or (not agent/IE)
          (agent/isVersionOrHigher 9))
  (enable-console-print!))

(defn log [& msgs]
  (when (or (not agent/IE)
            (agent/isVersionOrHigher 9))
    (apply println msgs)))

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
;; Data Fetchers
(defn fetch-programmes [data]
  (om/update! data [:programmes :fetching] :fetching)
  (GET (str "/4/programmes/")
       {:handler  (fn [x]
                    (log "Fetching programmes.")
                    (om/update! data [:programmes :data] (mapv slugs/slugify-programme x))
                    (om/update! data [:programmes :fetching] (if (empty? x) :no-data :has-data)))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data [:programmes :fetching] :error)
                         (om/update! data [:programmes :error-status] status)
                         (om/update! data [:programmes :error-text] status-text))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn fetch-projects [programme-id data]
  (om/update! data [:projects :fetching] :fetching)
  (GET (str "/4/programmes/" programme-id "/projects/")
       {:handler  (fn [x]
                    (log "Fetching projects for programme: " programme-id)
                    (om/update! data [:projects :data] (mapv slugs/slugify-project x))
                    (om/update! data [:projects :fetching] (if (empty? x) :no-data :has-data)))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data [:projects :fetching] :error)
                         (om/update! data [:projects :error-status] status)
                         (om/update! data [:projects :error-text] status-text))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

(defn fetch-properties [project-id data]
  (om/update! data [:properties :fetching] :fetching)
  (GET (str "/4/projects/" project-id "/properties/")
       {:handler  (fn [x]
                    (log "Fetching properties for project: " project-id)
                    (om/update! data [:properties :data] (mapv slugs/slugify-property x))
                    (om/update! data [:properties :fetching] (if (empty? x) :no-data :has-data)))
        :error-handler (fn [{:keys [status status-text]}]
                         (om/update! data [:properties :fetching] :error)
                         (om/update! data [:properties :error-status] status)
                         (om/update! data [:properties :error-text] status-text))
        :headers {"Accept" "application/edn"}
        :response-format :text}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; History loop - this drives the fetches and clear downs
(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event                                        (<! history-channel)
          history-status                                   (-> nav-event :args :ids)
          {:keys [programmes projects properties sensors]} history-status
          old-nav                                          (:active-components @data)
          old-programmes                                   (:programmes old-nav)
          old-projects                                     (:projects old-nav)
          old-properties                                   (:properties old-nav)]
      (log "Old Programmes: " old-programmes " Old Projects: " old-projects " Old Properties: " old-properties)
      (log "New Programmes: " programmes " New Projects: " projects " New Properties: " properties)

      ;; Clear down
      (when (or (nil? programmes)
                (empty? (-> @data :programmes :data)))
        (log "Clearing projects.")
        (om/update! data [:programmes :selected] nil)
        (om/update! data [:projects :data] [])
        (om/update! data [:projects :programme_id] nil)
        (fetch-programmes data))

      (when-not projects
        (log "Clearing properties.")
        (om/update! data [:projects :selected] nil)
        (om/update! data [:properties :data] [])
        (om/update! data [:properties :project_id] nil))

      (when-not properties
        (log "Clearing devices, sensors and measurements.")
        (om/update! data [:properties :selected] nil)
        (om/update! data [:property-details :data] {})
        (om/update! data [:property-details :property_id] nil)
        (om/update! data [:devices :data] [])
        (om/update! data [:sensors :data] [])
        (om/update! data [:measurements :data] []))

      (when-not sensors
        (om/update! data [:sensors :selected] nil))

      (when (and (not= programmes old-programmes)
                 programmes)
        (log "Setting selected programme to: " programmes)
        (om/update! data [:programmes :selected] programmes)
        (om/update! data [:projects :programme_id] programmes)
        (fetch-projects programmes data))

      (when (and (not= projects old-projects)
                 projects)
        (log "Setting selected project to: " projects)
        (om/update! data [:projects :selected] projects)
        (om/update! data [:properties :project_id] projects)
        (fetch-properties projects data))

      (when (and (not= properties old-properties)
                 properties)
        (log "Setting property details to: " properties)
        (om/update! data [:properties :selected] properties))

      (when sensors
        (om/update! data [:sensors :selected] sensors))
      
      ;; Update the new active components
      (om/update! data :active-components history-status))
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

          (when (and (not (empty? start-date))
                     (not (empty? end-date))
                     (not (nil? device_id))
                     (not (nil? entity_id))
                     (not (nil? type)))

            (let [url (url-str start-date end-date entity_id device_id type (interval start-date end-date))]
              (GET url
                   {:handler #(om/update! data :measurements %)
                    :headers {"Accept" "application/json"}
                    :response-format :json
                    :keywords? true}))))))
    (recur)))

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
           [:td (slugs/postal-address property_data)]
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
             (om/build property-details/property-details-div data)]))))
