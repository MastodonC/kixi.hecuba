(ns kixi.hecuba.tabs.properties
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require  [om.core :as om :include-macros true]
             [om.dom :as dom :include-macros true]
             [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
             [kixi.hecuba.properties :as properties]
             [ajax.core :refer (GET POST)]))


(defn get-properties [projects data]
  (doseq [project projects]
    (GET (str "/4/projects/" (:id project) "/properties")
         {:handler #(om/update! data [:properties :properties] (concat (-> @data :properties :properties) %))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))


(defn properties-tab [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chan {:clicked (chan (sliding-buffer 1))}})
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
           (dom/div #js {:className "panel panel-default"}
              (dom/div #js {:className "panel-heading"}
                (dom/h3 #js {:className "panel-title"}
                  (dom/a #js {:data-toggle "collapse" :data-parent "#accordion" :href "#collapseOne"}
                         "Properties")))
              (dom/div #js {:id "collapseOne" :className "panel-collapse collapse in"}
                (dom/div #js {:className "panel-body"}
                  (om/build properties/properties-select-table (:properties data) {:init-state chan}))))
           (dom/div #js {:className "panel panel-default"}
             (dom/div #js {:className "panel-heading"}
               (dom/h3 #js {:className "panel-title"}
                  (dom/a #js {:data-toggle "collapse" :data-parent "#accordion" :href "#collapseTwo"}
                          "Devices")))
             (dom/div #js {:id "collapseTwo" :className "panel-collapse collapse in"}
                (dom/div #js {:className "panel-body"}
                   (om/build properties/devices-select-table (:devices data) {:init-state chan})))))
         (dom/br nil)
         (dom/div #js {:className "panel-group"}
           (dom/div #js {:className "panel panel-default"}
              (dom/div #js {:className "panel-heading"}
                (dom/h3 #js {:className "panel-title"} "Chart"))
              (dom/div #js {:className "panel-body"}
                ;;build chart component
                                    )))
         ))))
