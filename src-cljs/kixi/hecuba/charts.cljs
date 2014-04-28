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
   [kixi.hecuba.widgets.mult-charts :as chart]
   [kixi.hecuba.history :as history]))

(def data-model
  (atom
   {:properties {:properties []}
    :devices {:devices []}
    :sensors {:sensors []}
    :chart {:unit ""
            :range {}
            :measurements []
            :message ""}}))

(defn get-properties [projects data]
  (doseq [project projects]
    (GET (str "/4/projects/" (:id project) "/properties")
         {:handler #(om/update! data [:properties :properties] (concat (-> @data :properties :properties) %))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))

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

                        (bootstrap/accordion-panel  "#collapseTwo" "collapseTwo" "Devices"
                                                    (om/build properties/devices-select-table
                                                              (:devices data)
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
                                                   (om/build chart/multiple-properties-chart (:chart data)))
                                          )))
               ))))


(om/root multiple-properties-chart data-model {:target (.getElementById js/document "charting")
                                                          :shared {:history (history/new-history [:properties :devices :sensors])}})
