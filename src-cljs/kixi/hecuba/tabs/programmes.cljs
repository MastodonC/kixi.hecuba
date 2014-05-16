(ns kixi.hecuba.tabs.programmes
    (:require-macros [cljs.core.async.macros :refer [go go-loop]])
    (:require
     [om.core :as om :include-macros true]
     [om.dom :as dom :include-macros true]
     [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
     [ajax.core :refer (GET POST)]
     [clojure.string :as str]
     [kixi.hecuba.navigation :as nav]
     [kixi.hecuba.widgets.datetimepicker :as dtpicker]
     [kixi.hecuba.widgets.chart :as chart]
     [kixi.hecuba.common :refer (index-of map-replace find-first interval)]
     [kixi.hecuba.history :as history]
     [kixi.hecuba.model :refer (app-model)]
     [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

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

(defn ajax [in data path {:keys [template selection-key content-type]} & [chart]]
  (go-loop []
    (let [nav-event (<! in)
          [new-selected uri] (uri-for-selection-change (:selected @data)
                                                       selection-key
                                                       template
                                                       nav-event)]

      ;; (om/update! data :active-components (-> nav-event :args :ids))
      
      (when uri
        (println "Fetching: " uri)
        (GET uri
             (-> {:handler  (fn [x]
                              (when (= selection-key :sensors)
                                (let [[type _] (str/split new-selected #"-")
                                      unit     (:unit (first (filter #(= (:type %) type) (:readings x))))]
                                  (om/update! chart :unit unit)))
                              (om/update! data (conj path :data) x)
                              (om/update! data (conj path :selected) new-selected))
                  :headers {"Accept" content-type}
                  :response-format :text}
                 (cond-> (= content-type "application/json")
                         (merge {:response-format :json :keywords? true}))))))
    (recur)))

(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event (<! history-channel)]
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
              entity-id        (get ids :properties)
              sensor-id        (get ids :sensors)
              [type device-id] (str/split sensor-id #"-")]

          (om/update! data :range {:start-date start-date :end-date end-date})
          (om/update! data :sensors sensor-id)
          (om/update! data :measurements [])

          ;; TODO ajax call should not be made on each change, only on this particular cursor update.
          (when (and (not (empty? start-date))
                     (not (empty? end-date))
                     (not (nil? device-id))
                     (not (nil? entity-id))
                     (not (nil? type)))

            ;; FIXME Should be a multimethod
            (let [url (case (interval start-date end-date)
                        :raw (str "/4/entities/" entity-id "/devices/" device-id "/measurements/"
                                  type "?startDate=" start-date "&endDate=" end-date)
                        :hourly-rollups (str "/4/entities/" entity-id "/devices/" device-id "/hourly_rollups/"
                                             type "?startDate=" start-date "&endDate=" end-date)
                        :daily-rollups (str "/4/entities/" entity-id "/devices/" device-id "/daily_rollups/"
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

(defn title-for [cursor & {:keys [title-key] :or {title-key :name}}]
  (let [row (row-for cursor)]
    (get-in row (if (vector? title-key) title-key (vector title-key)))))

(defn title-for-sensor [{:keys [selected]}]
  (let [[type _] (str/split selected #"-")]
    type))

;; our banner is 50px so we need to tweak the scrolling
(defn fixed-scroll-to-element [element]
  (-> (.getElementById js/document element)
      .scrollIntoView)
  (.scrollBy js/window 0 -50))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; programmes
(defn programmes-table [programmes owner]
  (reify
    om/IRender
    (render [_]
      (let [table-id   "programme-table"
            history    (om/get-shared owner :history)]
        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr [:th "ID"] [:th "Organisations"] [:th "Name"] [:th "Created At"]]]
          [:tbody
           (for [row (sort-by :id (:data programmes))]
             (let [{:keys [id lead-organisations name description created-at]} row]
               [:tr {:onClick (fn [_ _]
                                (om/update! programmes :selected id)
                                (history/update-token-ids! history :programmes id)
                                (fixed-scroll-to-element "projects-div"))
                     :className (if (= id (:selected programmes)) "success")
                     :id (str table-id "-selected")}
                [:td id [:a {:id (str "row-" id)}]] [:td lead-organisations] [:td name] [:td created-at]]))]])))))

(defn programmes-div [data owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [programmes active-components]} data
            programme-id (:programmes active-components)]

        (when-not programme-id
          (om/update! programmes :selected nil))

        (if (not (seq (:data programmes)))
          (GET (str "/4/programmes/")
               {:handler  (fn [x]
                            (println "Fetching programmes.")
                            (om/update! programmes :data x)
                            (om/update! programmes :selected nil))
                ;; TODO: Add Error Handler
                :headers {"Accept" "application/edn"}
                :response-format :text})
          (println "Not fetching programmes!"))

        ;; handle selection on programmes table (this should be nil if called)
        (om/update! programmes :selected (:programmes active-components))))
    om/IRender
    (render [_]
      (let [programmes (-> data :programmes)]
        (html
         [:div {:id "programmes-div"}
          [:h1 "Programmes"]
          (om/build programmes-table programmes)])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; projects
(defn projects-table [projects owner]
  (reify
    om/IRender
    (render [_]
      (let [table-id   "projects-table"
            history    (om/get-shared owner :history)]
        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr [:th "Name"] [:th "Type"] [:th "Description"] [:th "Created At"] [:th "Organisation"] [:th "Project Code"]]]
          [:tbody
           (for [row (sort-by :id (:data projects))]
             (let [{:keys [id name type-of description created-at organisation project-code]} row]
               [:tr {:onClick (fn [_ _]
                                (om/update! projects :selected id)
                                (history/update-token-ids! history :projects id)
                                (fixed-scroll-to-element "properties-div"))
                     :className (if (= id (:selected projects)) "success")
                     :id (str table-id "-selected")}
                [:td name]
                [:td type-of]
                [:td description]
                [:td created-at]
                [:td organisation]
                [:td project-code]]))]])))))

(defn projects-div [data owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [programmes projects active-components]} data
            new-programme-id (:programmes active-components)]

        ;; handle selection in programme table
        (when-not new-programme-id
          (om/update! projects :data [])
          (om/update! projects :selected nil))

        ;; get the data if we have a new id
        (if (and new-programme-id
                   (not (= (:programme-id projects) new-programme-id)))
          (GET (str "/4/programmes/" new-programme-id "/projects/")
               {:handler  (fn [x]
                            (println "Fetching projects for programme: " new-programme-id)
                            (om/update! projects :data x)
                            (om/update! projects :selected nil))
                ;; TODO: Add Error Handler
                :headers {"Accept" "application/edn"}
                :response-format :text})
          (println "Not fetching projects!"))

        ;; update our current id with the new one
        (om/update! projects :programme-id new-programme-id)

        ;; handle selection in projects table
        (om/update! projects :selected (:projects active-components))))
    om/IRender
    (render [_]
      (let [{:keys [programmes projects active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div {:id "projects-div"}
          [:div {:class (if (:programme-id projects) "" "hidden")}
           [:h2 "Projects"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:onClick (back-to-programmes history)}
                  (title-for programmes)]]]
           (om/build projects-table projects {:opts {:histkey :projects}})]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; properties
(defn properties-table [properties owner]
  (reify
    om/IRender
    (render [_]
      (let [table-id   "properties-table"
            history    (om/get-shared owner :history)]
        (if (:fetching properties)
          (html
           [:p "Fetching data..."])
          (html
           [:table {:className "table table-hover"}
            [:thead
             [:tr [:th "Address"] [:th "Region"] [:th "Country"]]]
            [:tbody
             (for [row (sort-by :address-street-two (:data properties))]
               (let [{:keys [id address-street-two address-country address-region]} row]
                 [:tr {:onClick (fn [_ _]
                                  (om/update! properties :selected id)
                                  (history/update-token-ids! history :properties id)
                                  (fixed-scroll-to-element "devices-div"))
                       :className (if (= id (:selected properties)) "success")
                       :id (str table-id "-selected")}
                  [:td address-street-two]
                  [:td address-region]
                  [:td address-country]]))]]))))))

(defn properties-div [data owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [projects properties active-components]} data
            new-project-id (:projects active-components)]

        (println "Active Components: " active-components)

        ;; handle selection in projects table
        (when-not new-project-id
          (println "Cleaering projects data.")
          (om/update! properties :data [])
          (om/update! properties :selected nil))
        
        (if (and new-project-id
                 (not (= (:project-id properties) new-project-id)))
          (do
            (om/update! properties :fetching true)
            (GET (str "/4/projects/" new-project-id "/properties/")
                 {:handler  (fn [x]
                              (println "Fetching properties for project: " new-project-id)
                              (om/update! properties :fetching false)
                              (om/update! properties :data x)
                              (om/update! properties :selected nil))
                  :error-handler (fn [{:keys [status status-text]}]
                                   (om/update! properties :fetching false)
                                   (om/update! properties :error-status status)
                                   (om/update! properties :error-text status-text))
                  :headers {"Accept" "application/edn"}
                  :response-format :text}))
          (println "Not fetching properties!"))
        (om/update! properties :project-id new-project-id)

        ;; handle selection on properties table
        (om/update! properties :selected (:properties active-components))))
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div {:id "properties-div"}
          [:div {:class (if (:project-id properties) "" "hidden")}
           [:h2 "Properties"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:onClick (back-to-programmes history)}
                  (title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (title-for projects)] " " (when (:fetching properties) [:span {:class "glyphicon glyphicon-cloud-download spinner"}])]]
           (om/build properties-table properties {:opts {:histkey :properties}})]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; devices
(defn devices-table [devices owner]
  (reify
    om/IRender
    (render [_]
      (let [table-id   "devices-table"
            history    (om/get-shared owner :history)]
        (html
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
                [:td privacy]]))]])))))

(defn devices-div [data owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [properties devices active-components]} data
            new-property-id (:properties active-components)]

        (println "Active Components: " active-components)

        ;; handle selection perties table
        (when-not new-property-id
          (om/update! devices :data [])
          (om/update! devices :selected nil))
        
        (if (and new-property-id
                 (not (= (:property-id devices) new-property-id)))
          (do
            (om/update! devices :fetching true)
            (GET (str "/4/entities/" new-property-id "/devices/")
                 {:handler  (fn [x]
                              (println "Fetching devices for property: " new-property-id)
                              (om/update! devices :fetching false)
                              (om/update! devices :data x)
                              (om/update! devices :selected nil))
                  :error-handler (fn [{:keys [status status-text]}]
                                   (om/update! devices :fetching false)
                                   (om/update! devices :error-status status)
                                   (om/update! devices :error-text status-text))
                  ;; FIXME: This should be application/edn
                  :headers {"Accept" "application/json"}
                  :response-format :json
                  :keywords? true}))
          (println "Not fetching devices!"))
        (om/update! devices :property-id new-property-id)

        ;; handle selection in devices table
        (om/update! devices :selected (:devices active-components))))
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties devices active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div {:id "devices-div"}
          [:div {:class (if (:property-id devices) "" "hidden")}
           [:h2  "Devices"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:onClick (back-to-programmes history)}
                  (title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (title-for projects)]]
            [:li [:a
                  {:onClick (back-to-properties history)}
                  (title-for properties :title-key :address-street-two)]
             " " (when (:fetching devices) [:span {:class "glyphicon glyphicon-cloud-download spinner"}])]]
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
            history (om/get-shared owner :history)]

        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr [:th "Type"] [:th "Unit"] [:th "Period"] [:th "Device"] [:th "Status"]]]
          [:tbody
           (for [row (sort-by :type (-> sensors :data :readings))]
             (let [{:keys [deviceId type unit period status]} row
                   id (str type "-" deviceId)]
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
                [:td deviceId]
                [:td (status-label status)]]))]])))))

(defn sensors-div [data owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [sensors active-components]} data
            new-device-id                       (:devices active-components)
            property-id                         (:properties active-components)]

        (println "Active Components: " active-components)

        ;; handle selection perties table
        (when-not new-device-id
          (om/update! sensors :data [])
          (om/update! sensors :selected nil))
        
        (if (and new-device-id
                 (not (= (:device-id sensors) new-device-id)))
          (do
            (om/update! sensors :fetching true)
            ;; "/4/entities/:properties/devices/:devices"
            (GET (str "/4/entities/" property-id "/devices/" new-device-id)
                 {:handler  (fn [x]
                              (println "Fetching sensors for device: " new-device-id)
                              (om/update! sensors :fetching false)
                              (om/update! sensors :data x)
                              (om/update! sensors :selected nil))
                  :error-handler (fn [{:keys [status status-text]}]
                                   (om/update! sensors :fetching false)
                                   (om/update! sensors :error-status status)
                                   (om/update! sensors :error-text status-text))
                  ;; FIXME: This should be application/edn
                  :headers {"Accept" "application/json"}
                  :response-format :json
                  :keywords? true}))
          (println "Not fetching sensors!"))
        (om/update! sensors :device-id new-device-id)

        ;; handle selection in sensors table
        (om/update! sensors :selected (:sensors active-components))))
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties devices sensors active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div {:id "sensors-div"}
          [:div {:class (if (:device-id sensors) "" "hidden")}
           [:h2 {:id "sensors"} "Sensors"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:onClick (back-to-programmes history)}
                  (title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (title-for projects)]]
            [:li [:a
                  {:onClick (back-to-properties history)}
                  (title-for properties :title-key :address-street-two)]]
            [:li [:a
                  {:onClick (back-to-devices history)}
                  (title-for devices :title-key [:location :name])]]]
           (om/build sensors-table data {:opts {:histkey :sensors
                                                :path    :readings}})
           [:div {:id "chart-div"}
            [:div {:id "date-picker"}
             (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
            (om/build chart-feedback-box (get-in data [:chart :message]))
            [:div {:className "well" :id "chart" :style {:width "100%" :height 600}}
             (om/build chart/chart-figure (:chart data))]]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main View
(defn programmes-tab [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [history     (om/get-shared owner :history)
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
             (om/build devices-div data)
             ;; (om/build device-detail devices)
             (om/build sensors-div data)
             ;; (om/build sensor/define-data-set-button data)

             ]))))


