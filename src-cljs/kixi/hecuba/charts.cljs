(ns kixi.hecuba.charts
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [ajax.core :refer (GET POST)]
   [clojure.string :as str]
   [kixi.hecuba.bootstrap :as bootstrap]
   [kixi.hecuba.multiple-properties-charts :as properties]
   [kixi.hecuba.widgets.datetimepicker :as dtpicker]
   [kixi.hecuba.history :as history]))

(def data-model
  (atom
   {:properties {:properties []}
    :sensors {:sensors []}
    :chart {:unit ""
            :range {}
            :sensors #{}
            :measurements []
            :message ""}}))

(defn get-properties [projects data]
  (doseq [project projects]
    (GET (str "/4/projects/" (:id project) "/properties")
         {:handler #(om/update! data [:properties :properties] (concat (-> @data :properties :properties) %))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))

(defn selected-range-change
  [selected selection-key {{ids :ids search :search} :args}]
  (let [new-selected (get ids selection-key)]
    (when (or (nil? selected)
              (not= selected new-selected))
      (vector new-selected ids search))))

#_(defn ajax [in data {:keys [selection-key content-type]}]
  (go-loop []
    (when-let [[new-range ids search] (selected-range-change (:range @data)
                                                             selection-key
                                                             (<! in))]
      (let [[start-date end-date] search
            entity-id        (get ids :property)
            sensor-id        (get ids :sensor)
            [type device-id] (str/split sensor-id #"-")]
        ;; TODO ajax call should not be made on each change, only on this particular cursor update.
        (when (and (not (empty? start-date))
                   (not (empty? end-date))
                   (not (nil? device-id))
                   (not (nil? entity-id))
                   (not (nil? type)))
          (om/update! data :range {:start-date start-date :end-date end-date})
          (om/update! data :sensor sensor-id)
          (let [url (case (interval start-date end-date)
                      :raw (str "/4/entities/" entity-id "/devices/" device-id "/measurements/"
                                type "?startDate=" start-date "&endDate=" end-date)
                      :hourly-rollups (str "/4/entities/" entity-id "/devices/" device-id "/hourly_rollups/"
                                           type "?startDate=" start-date "&endDate=" end-date)
                      :daily-rollups (str "/4/entities/" entity-id "/devices/" device-id "/daily_rollups/"
                                          type "?startDate=" start-date "&endDate=" end-date))]
            (GET url {:handler #(om/update! data :measurements %)
                      :headers {"Accept" "application/json"}
                      :response-format :json
                      :keywords? true})))))
    (recur)))

(defn chart-feedback-box [cursor owner]
  (om/component
   (dom/div nil cursor)))

(defn multiple-properties-chart [data owner {:keys [properties-history]}]
  (reify
    om/IInitState
    (init-state [_]
      {:chan {:clicked-properties (chan (sliding-buffer 1))
              :clicked-devices    (chan (sliding-buffer 1))
              :clicked-sensors    (chan (sliding-buffer 1))}})
    om/IWillMount
    (will-mount [this]
      (GET "/4/projects/" {:handler #(get-properties % data)
                           :headers {"Accept" "application/json"}
                           :response-format :json
                           :keywords? true}))
    om/IRenderState
    (render-state [_ {:keys [chan]}]
      (dom/div nil
               (dom/div #js {:className "panel-group" :id "accordion"}
                        (bootstrap/accordion-panel  "#collapseOne" "collapseOne" "Properties"
                                                    (om/build properties/properties-select-table
                                                              (:properties data)
                                                              {:init-state chan}))

                        (bootstrap/accordion-panel  "#collapseThree" "collapseThree" "Sensors"
                                                    (om/build properties/sensors-select-table
                                                              (:sensors data)
                                                              {:init-state chan})))
               (dom/br nil)
               (dom/div #js {:className "panel-group"}
                        (dom/div #js {:className "panel panel-default"}
                                 (dom/div #js {:className "panel-heading"}
                                          (dom/h3 #js {:className "panel-title"} "Chart"))
                                 (dom/div #js {:className "panel-body"}
                                          (dom/div #js {:id "date-picker"}
                                                   (om/build dtpicker/date-picker data))
                                          (om/build chart-feedback-box (get-in data [:chart :message]))
                                          (dom/div #js {:className "well" :id "chart" :style {:width "100%" :height 600}}
                                                   (om/build properties/chart 
                                                             (:chart data)
                                                             {:init-state chan})
                                                   )
                                          )))

               ))))


(om/root multiple-properties-chart data-model {:target (.getElementById js/document "charting")
                                               :shared {:history (history/new-history [:properties :sensors :range])}})
