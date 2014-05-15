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
     [kixi.hecuba.sensor :as sensor]
     [sablono.core :as html :refer-macros [html]]))

(enable-console-print!)

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
          active-components (-> nav-event :args :ids keys set)
          components [:programmes :projects :properties :devices :sensors :measurements] ; FIXME
          to-clear (remove #(active-components %) components)
          [new-selected uri] (uri-for-selection-change (:selected @data)
                                                       selection-key
                                                       template
                                                       nav-event)]


      (om/update! data :active-components (-> nav-event :args :ids))
      
      (doseq [a active-components]
        (om/update! data (vector a :active) true))
      
      (doseq [c to-clear]
        (om/update! data (vector c :active) false)
        (om/update! data (vector c :data) [])
        (om/update! data (vector c :selected) nil))
      
      (when uri
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

(defn programmes-div [tables owner]
  (reify
    om/IRender
    (render [_]
      (let [programmes (-> tables :programmes)]
        (html
         ;; hide div if we've already chosen something
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

(defn projects-div [tables owner]
  (reify
    om/IDidUpdate
    (did-update [_ prev-props prev-state]
      (let [{:keys [programmes projects active-components]} tables]
        ;;(println "Active Components: " active-components)
        ))
    om/IRender
    (render [_]
      (let [{:keys [programmes projects active-components]} tables
            history (om/get-shared owner :history)]
        (html
         [:div {:id "projects-div"}
          [:h2 "Projects"]
          [:ul {:class "breadcrumb"}
           [:li [:a
                 {:onClick (fn projects-div-history-change
                             [_ _]
                             (history/update-token-ids! history :programmes nil)
                             (fixed-scroll-to-element "programmes-div"))}
                 (title-for programmes)]]]
          (om/build projects-table projects {:opts {:histkey :projects}})])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; properties
(defn properties-table [properties owner]
  (reify
    om/IRender
    (render [_]
      (let [table-id   "properties-table"
            history    (om/get-shared owner :history)]
        (html
         [:table {:className "table table-hover"}
          [:thead
           [:tr [:th "Address"] [:th "Country"]]]
          [:tbody
           (for [row (sort-by :name (:data properties))]
             (let [{:keys [id addressStreetTwo country]} row]
               [:tr {:onClick (fn [_ _]
                                (om/update! properties :selected id)
                                (history/update-token-ids! history :properties id)
                                (fixed-scroll-to-element "devices-div"))
                     :className (if (= id (:selected properties)) "success")
                     :id (str table-id "-selected")}
                [:td addressStreetTwo]
                [:td country]]))]])))))

(defn properties-div [tables owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties active-components]} tables
            history (om/get-shared owner :history)]
        (html
         [:div {:id "properties-div"}
          [:h2 "Properties"]
          [:ul {:class "breadcrumb"}
           [:li [:a
                 {:onClick (fn [_ _]
                             (history/update-token-ids! history :projects nil)
                             (history/update-token-ids! history :programmes nil)
                             (fixed-scroll-to-element "programmes-div"))}
                 (title-for programmes)]]
           [:li [:a
                 {:onClick (fn [_ _]
                             (history/update-token-ids! history :projects nil)
                             (fixed-scroll-to-element "projects-div"))}
                 (title-for projects)]]]
          (om/build properties-table properties {:opts {:histkey :properties}})])))))

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

(defn devices-div [tables owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties devices active-components]} tables
            history (om/get-shared owner :history)]
        (html
         [:div {:id "devices-div"}
          [:h2  "Devices"]
          [:ul {:class "breadcrumb"}
           [:li [:a
                 {:onClick (fn [_ _]
                             (history/update-token-ids! history :properties nil)
                             (history/update-token-ids! history :projects nil)
                             (history/update-token-ids! history :programmes nil)
                             (fixed-scroll-to-element "programmes-div"))}
                 (title-for programmes)]]
           [:li [:a
                 {:onClick (fn [_ _]
                             (history/update-token-ids! history :properties nil)
                             (history/update-token-ids! history :projects nil)
                             (fixed-scroll-to-element "projects-div"))}
                 (title-for projects)]]
           [:li [:a
                 {:onClick (fn [_ _]
                             (history/update-token-ids! history :properties nil)
                             (fixed-scroll-to-element "properties-div"))}
                 (title-for properties :title-key :addressStreetTwo)]]]
          (om/build devices-table devices {:opts {:histkey :devices}})])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sensors
(defn sensors-div [data owner]
  (let [tables (:tables data)]
    (reify
      om/IRender
      (render [_]
        (let [{:keys [programmes projects properties devices active-components]} tables
              history (om/get-shared owner :history)]
          (html
           [:div {:id "sensors-div"}
            [:h2 {:id "sensors"} "Sensors"]
            [:ul {:class "breadcrumb"}
             [:li [:a
                   {:onClick (fn [_ _]
                               (history/update-token-ids! history :devices nil)
                               (history/update-token-ids! history :properties nil)
                               (history/update-token-ids! history :projects nil)
                               (history/update-token-ids! history :programmes nil)
                               (fixed-scroll-to-element "programmes-div"))}
                   (title-for programmes)]]
             [:li [:a
                   {:onClick (fn [_ _]
                               (history/update-token-ids! history :devices nil)
                               (history/update-token-ids! history :properties nil)
                               (history/update-token-ids! history :projects nil)
                               (fixed-scroll-to-element "projects-div"))}
                   (title-for projects)]]
             [:li [:a
                   {:onClick (fn [_ _]
                               (history/update-token-ids! history :devices nil)
                               (history/update-token-ids! history :properties nil)
                               (fixed-scroll-to-element "properties-div"))}
                   (title-for properties :title-key :addressStreetTwo)]]
             [:li [:a
                   {:onClick (fn [_ _]
                               (history/update-token-ids! history :devices nil)
                               (fixed-scroll-to-element "devices-div"))}
                   (title-for devices :title-key [:location :name])]]]
            (om/build sensor/table data {:opts {:histkey :sensors
                                                :path    :readings}})]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main View
(defn programmes-tab [data owner]
  (let [{tables :tables} data]
    (reify
      om/IWillMount
      (will-mount [_]
        (let [history     (om/get-shared owner :history)
              m           (mult (history/set-chan! history (chan)))
              tap-history #(tap m (chan))]
          
          (ajax (tap-history) tables [:programmes] {:template      "/4/programmes/"
                                                    :content-type  "application/edn"
                                                    :selection-key :programmes})
          (ajax (tap-history) tables [:projects] {:template      "/4/programmes/:programmes/projects/"
                                                  :content-type  "application/edn"
                                                  :selection-key :projects})
          (ajax (tap-history) tables [:properties] {:template      "/4/projects/:projects/properties/"
                                                    :content-type "application/json"
                                                    :selection-key :properties})
          (ajax (tap-history) tables [:devices] {:template      "/4/entities/:properties/devices/"
                                                 :content-type  "application/json"
                                                 :selection-key :devices})
          (ajax (tap-history) tables [:sensors] {:template "/4/entities/:properties/devices/:devices"
                                                 :content-type "application/json"
                                                 :selection-key :sensors} (:chart data))
          ;; (ajax (tap-history) tables [:sensor-select] {:template     "/4/entities/:properties/sensors"
          ;;                                              :content-type "application/json"
          ;;                                              :selection-key :sensor-select})
          (chart-ajax (tap-history) (:chart data) {:template "/4/entities/:properties/devices/:devices/measurements?startDate=:start-date&endDate=:end-date"
                                                   :content-type  "application/json"
                                                   :selection-key :range})))
      om/IRender
      (render [_]

        (let [{:keys [programmes projects properties devices sensors sensor-select]} tables]
          (html [:div
                 (om/build programmes-div tables)
                 (om/build projects-div tables)
                 (om/build properties-div tables)
                 (om/build devices-div tables)
                 ;; (om/build device-detail devices)
                 (om/build sensors-div data)
                 ;; (om/build sensor/define-data-set-button data)

                 [:div {:id "chart-div"}
                  [:h2 "Chart"]
                  [:div {:id "date-picker"}
                   (om/build dtpicker/date-picker data {:opts {:histkey :range}})]
                  (om/build chart-feedback-box (get-in data [:chart :message]))
                  [:div {:className "well" :id "chart" :style {:width "100%" :height 600}}
                   (om/build chart/chart-figure (:chart data))]
                  (om/build sensor/selection-dialog (:tables data)
                            {:opts {:id "sensor-selection-dialog"
                                    :handler (fn [e]
                                               (.preventDefault e)
                                               (POST (str "/4/entities/" (:selected @properties) "/datasets")
                                                     {:params          (:sensor-group @sensor-select)
                                                      :handler         #(println "Yah!")
                                                      :error-handler   #(println "Error!")
                                                      :response-format "application/edn"
                                                      :keywords?       true}))}})]]))))))


