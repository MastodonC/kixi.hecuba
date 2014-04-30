(ns kixi.hecuba.charts
  (:require-macros [cljs.core.async.macros :refer [go-loop go]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [ajax.core :refer (GET POST)]
   [clojure.string :as str]
   [cljs.reader :as reader]
   [kixi.hecuba.common :refer (interval)]
   [kixi.hecuba.bootstrap :as bootstrap]
   [kixi.hecuba.multiple-properties-charts :as properties]
   [kixi.hecuba.history :as history]))

(def data-model
  (atom
   {:properties {:data []}
    :sensors {:data []}
    :selected {:properties #{} :sensors #{}}
    :chart {:unit ""
            :range {}
            :sensors #{}
            :measurements []
            :message ""}}))

(defn chart-feedback-box [cursor owner]
  (om/component
   (dom/div nil cursor)))

(defn get-properties [projects data]
  (doseq [project projects]
    (GET (str "/4/projects/" (:id project) "/properties")
         {:handler #(om/update! data [:properties :data] (concat (-> @data :properties :data) %))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))

(defn get-measurements [data sensors start-date end-date]
  (when (and start-date end-date (not (empty? sensors)))
    (doseq [sensor sensors]
      (let [[device-id type entity-id] (str/split sensor #"-")
            resource (case (interval start-date end-date)
                       :raw "measurements"
                       :hourly-rollups "hourly_rollups"
                       :daily-rollups "daily_rollups")
            url      (str "/4/entities/" entity-id "/devices/" device-id "/" resource "/" type "?startDate="
                          start-date "&endDate=" end-date)
            sensor (str/join "-" [device-id type])]
        
        (GET url {:handler #(om/update! data [:chart :measurements]
                                        (concat (:measurements (:chart @data))
                                                (into []
                                                      (map (fn [m] 
                                                             (assoc m "sensor" sensor "entity-id" entity-id)) 
                                                           (get % "measurements")))))})))))

(defmulti update-measurements (fn [data checked] (:selection-key checked)))

(defmethod update-measurements :properties [data {:keys [checked new-selected]}]
  (om/update! data [:chart :measurements] (remove #(= (get % "entity-id") new-selected) (:measurements (:chart @data))))
  ;; TODO Clears sensors belonging to deselected property - there must be a better way of doing this.
  (let [sensors-to-keep (into #{} (remove (fn [s] (let [[device-id type entity-id] (str/split s #"-")]
                                                    (= new-selected entity-id))) (:sensors (:selected @data))))]
    (om/update! data [:selected :sensors]  sensors-to-keep)
    (om/update! data [:chart :sensors] sensors-to-keep)))

(defmethod update-measurements :range [data checked]
  (let [chart (:chart @data)
        sensors (:sensors chart)
        {:keys [start-date end-date]} (:range chart)]
    (get-measurements data sensors start-date end-date)))

(defmethod update-measurements :sensors [data {:keys [checked new-selected]}]
  (if checked
    (let [chart      (:chart @data)
          sensors    (:sensors chart)
          start-date (:start-date (:range chart))
          end-date   (:end-date (:range chart))]
      (get-measurements data sensors start-date end-date))
    (let [[device-id type entity-id] (str/split new-selected #"-")
          sensor                     (str/join "-" [device-id type])]
      (om/update! data [:chart :measurements] (remove #(= (get % "sensor") sensor) (:measurements (:chart @data)))))))

(defn update-chart-range [data history value]
  (let [start   (:start-date (:range value))
        end     (:end-date (:range value))
        message (:message value)]
    (if-not (empty? message)
      (om/update! data [:chart :message] message)
      (do (om/update! data [:chart :message] "")
          (om/update! data [:chart :range] {:start-date start :end-date end})
          (history/set-token-search! history [start end])))
    (update-measurements data {:selection-key :range :checked nil})))

(defn update-chart-sensors [data history new-selected checked]
  (let [[device-id type unit entity-id] (str/split new-selected #"-")
        new-sensor                      (str/join "-" [device-id type entity-id])
        current-unit                    (:unit (:chart @data))]

    (om/update! data [:selected :sensors] ((if checked conj disj) (:sensors (:selected @data)) new-sensor))
    (om/update! data [:chart :sensors] (:sensors (:selected @data)))

    (history/update-token-ids! history :sensors (str/join "&" (:sensors (:selected @data))))

    ;; Check unit - selected sensors must be of the same unit
    (if (or (empty? current-unit) (= current-unit unit))
      (do
        (om/update! data [:chart :unit] unit)
        (update-measurements data {:selection-key :sensors :checked checked :new-selected new-sensor}))
      (om/update! data [:chart :message] (if checked "Selected sensors must be of the same unit." "")))))

(defn update-sensors-form [data history new-selected checked]
  (if checked
    (let [all-properties (get-in @data [:properties :data])
          new-property   (into {} (filter #(= (:id %) new-selected) all-properties))
          new-devices    (map (fn [[k v]]
                                (merge {:id k} (reader/read-string v))) (reader/read-string (:devices new-property)))
          new-sensors    (mapcat (fn [device]
                                   (map (fn [reading]
                                          {:device-id (:id device)
                                           :type (get reading "type")
                                           :entity-id (:entity-id device)
                                           :unit (get reading "unit")
                                           :description (:description device)}) (:readings device))) new-devices)]
      (om/update! data [:sensors :data] (concat (:data (:sensors @data)) new-sensors))
      (om/update! data [:selected :properties] (conj (:properties (:selected @data)) new-selected))
      (history/update-token-ids! history :properties (str/join "&" (:properties (:selected @data)))))
    (do
      (om/update! data [:selected :properties] (disj (:properties (:selected @data)) new-selected))
      (history/update-token-ids! history :properties (str/join "&" (:properties (:selected @data))))
      (om/update! data [:sensors :data] (remove #(= (:entity-id %) new-selected) (:data (:sensors @data))))))
  (update-measurements data {:selection-key :properties :checked nil :new-selected new-selected}))

(defn multiple-properties-chart [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chans {:selection (chan (sliding-buffer 1))}})
    om/IWillMount
    (will-mount [this]    
      (let [clicked (om/get-state owner [:chans :selection])
            history (om/get-shared owner :history)]
        (GET "/4/projects/" {:handler #(get-properties % data)
                             :headers {"Accept" "application/json"}
                             :response-format :json
                             :keywords? true})
        (go (while true
              (let [sel           (<! clicked)
                    selection-key (:selection-key sel)
                    checked       (:checked sel)
                    value         (:value sel)]
                (case selection-key
                  :properties (update-sensors-form data history value checked)
                  :sensors (update-chart-sensors data history value checked)
                  :range (update-chart-range data history value)))))))

    om/IRenderState
    (render-state [_ {:keys [chans]}]
      (dom/div nil
               (dom/div #js {:className "panel-group" :id "accordion"}
                        (bootstrap/accordion-panel  "#collapseOne" "collapseOne" "Properties"
                                                    (om/build properties/properties-select-table
                                                              (:properties data) {:init-state chans
                                                                                  :opts {:histkey :properties}}))

                        (bootstrap/accordion-panel  "#collapseThree" "collapseThree" "Sensors"
                                                    (om/build properties/sensors-select-table
                                                              (:sensors data) {:init-state chans
                                                                               :opts {:histkey :sensors}})))
               (dom/br nil)
               (dom/div #js {:className "panel-group"}
                        (dom/div #js {:className "panel panel-default"}
                                 (dom/div #js {:className "panel-heading"}
                                          (dom/h3 #js {:className "panel-title"} "Chart"))
                                 (dom/div #js {:className "panel-body"}
                                          (dom/div #js {:id "date-picker"}
                                                   (om/build properties/date-picker data
                                                             {:init-state chans}))
                                          (om/build chart-feedback-box (get-in data [:chart :message]))
                                          (dom/div #js {:className "well" :id "chart" :style {:width "100%" :height 600}}
                                                   (om/build properties/chart (:chart data))))))))))


(om/root multiple-properties-chart data-model {:target (.getElementById js/document "charting")
                                               :shared {:history (history/new-history [:properties :sensors :range])}})
