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
      (doseq [a active-components]
        (om/update! data (vector a :active) true))
      
      ;; (println "Nav Event: " nav-event)
      ;; (println "To Clear: " to-clear)
      ;; (println "URI: " uri " for path " path " selection-key " selection-key)
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

;; TODO histkey is really id key. resolve name confusion.
(defn table [cursor owner {:keys [histkey path]}]
  (reify
    om/IRender
    (render [_]
      ;; Select the first row
      ;;(put! out {:type :row-selected :row (first (om/get-state owner :data))})
      (let [cols (get-in cursor [:header :cols])
            table-id (str (name histkey) "-table")
            history (om/get-shared owner :history)]
        (dom/div #js {:id (str table-id "-container")}
         (dom/table
          #js {:id table-id
               :className "table table-hover hecuba-table"} ;; table-hover table-stripedso,
          (dom/thead
           nil
           (dom/tr
            nil
            (into-array
             (for [[_ {:keys [label]}] cols]
               (dom/th nil label)))))
          (dom/tbody
           nil
           (into-array
            (for [{:keys [id href ] :as row} (-> cursor :data
                                                (cond-> path path))]
              (dom/tr
               #js {:onClick (fn [_ _ ]
                               (om/update! cursor :selected id)
                               (history/update-token-ids! history histkey id))
                    :className (if (= id (:selected cursor)) "success")
                    ;; TODO use this to scroll the row into view.
                    ;; Possible solution here: http://stackoverflow.com/questions/1805808/how-do-i-scroll-a-row-of-a-table-into-view-element-scrollintoview-using-jquery
                    :id (str table-id "-selected")}
               (into-array
                (for [[k {:keys [href]}] cols]
                  (let [k (if (vector? k) k (vector k))]
                    (dom/td nil (if href
                                  (dom/a #js {:href (get row href)} (get-in row k))
                                  (get-in row k))))))))))))))))

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

(defn programmes-div [programmes owner]
  (reify
    om/IRender
    (render [_]
      (html
       ;; hide div if we've already chosen something
       [:div {:id "programmes" :class (if (:active programmes) "hidden" "")}
        [:h1 "Programmes"]
        (om/build programmes-table programmes)]))))

(defn programmes-table [cursor owner]
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
           (for [row (sort-by :id (:data cursor))]
             (let [{:keys [id lead-organisations name description created-at]} row]
               [:tr {:onClick (fn [_ _]
                                (om/update! cursor :selected id)
                                (history/update-token-ids! history :programmes id))
                     :className (if (= id (:selected cursor)) "success")
                     :id (str table-id "-selected")}
                [:td id [:a {:id (str "row-" id)}]] [:td lead-organisations] [:td name] [:td created-at]]))]])))))

(defn projects-div [tables owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects]} tables
            history (om/get-shared owner :history)]
        (html
         [:div {:id "projects" :class (if (:active projects) "hidden" "")}
          [:h2 "Projects"]
          [:ul {:class "breadcrumb"}
           [:li [:a
                 {:onClick (fn projects-div-history-change
                             [_ _]
                             (history/update-token-ids! history :programmes nil))}
                 (title-for programmes)]]]
          (om/build table projects {:opts {:histkey :projects}})])))))

(defn properties-div [tables owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties]} tables
            history (om/get-shared owner :history)]
        (html
         [:div [:h2  {:id "properties"} "Properties"]
          [:ul {:class "breadcrumb"}
           [:li [:a
                 {:onClick (fn [_ _]
                             (history/update-token-ids! history :projects nil)
                             (history/update-token-ids! history :programmes nil))}
                 (title-for programmes)]]
           [:li [:a
                 {:onClick (fn [_ _]
                             (history/update-token-ids! history :projects nil))}
                 (title-for projects)]]]
          (om/build table properties {:opts {:histkey :properties}})])))))

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
                                                   :selection-key :range})
          ))
      om/IRender
      (render [_]
        ;; Note dynamic titles for each of the sections.

        ;; TODO sort out duplication here, wrap (on/build table ...) calls probably.
        ;;
        (let [{:keys [programmes projects properties devices sensors sensor-select]} tables]
          ;; (println "Tables: " tables)
          (html [:div
                 
                 (om/build programmes-div programmes)

                 (om/build projects-div tables)
                 
                 (om/build properties-div tables)

                 [:h2 {:id "devices"} "Devices"]
                 [:ul {:class "breadcrumb"}
                  [:li (title-for programmes)]
                  [:li (title-for projects)]
                  [:li (title-for properties :title-key :addressStreetTwo)]]
                 (om/build table devices {:opts {:histkey :devices}})
                 
                 (om/build device-detail devices)

                 [:h2 {:id "sensors"} "Sensors"]
                 [:ul {:class "breadcrumb"}
                  [:li (title-for programmes)]
                  [:li (title-for projects)]
                  [:li (title-for properties :title-key :addressStreetTwo)]
                  [:li (title-for devices :title-key [:location :name])]]
                 (om/build sensor/table data {:opts {:histkey :sensors
                                                     :path    :readings}})
                 (om/build sensor/define-data-set-button data)

                 [:h2 "Chart"]
                 [:ul {:class "breadcrumb"}
                  [:li (title-for programmes)]
                  [:li (title-for projects)]
                  [:li (title-for properties :title-key :addressStreetTwo)]
                  [:li (title-for devices :title-key [:location :name])]
                  [:li (title-for-sensor sensors)]]
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
                                                     :keywords?       true}))}})]))))))


