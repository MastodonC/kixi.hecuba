(ns kixi.hecuba.tabs.properties
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require  [om.core :as om :include-macros true]
             [om.dom :as dom :include-macros true]
             [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
             [kixi.hecuba.properties :as properties]
             [kixi.hecuba.bootstrap :as bootstrap]
             [ajax.core :refer (GET POST)]))


(defn get-properties [projects data]
  (doseq [project projects]
    (GET (str "/4/projects/" (:id project) "/properties")
         {:handler #(om/update! data [:properties :properties] (concat (-> @data :properties :properties) %))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))


(defn properties-tab [data owner {:keys [properties-history]}]
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
                                          ;;build chart component
                                          )))
               ))))
