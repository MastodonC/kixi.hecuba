(ns kixi.hecuba.chart
  (:require
   [mrhyde.core :as mrhyde]  
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.string :as str]))

(def dimple (this-as ct (aget ct "dimple")))

(mrhyde/bootstrap)
(enable-console-print!)


(defn chart-item
  [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_])
    om/IRender
    (render [_] 
       (dom/div nil
                (dom/div #js {:id "chart" :width 500 :height 550})))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [Chart            (.-chart dimple)
            svg              (.newSvg dimple "#chart" "100%" 600)
            [type device-id] (str/split (get-in cursor [:sensor]) #"-")
            data             (get-in cursor [:measurements])
            dimple-chart     (.setBounds (Chart. svg) "10%" "15%" "80%" "50%")
            x (.addCategoryAxis dimple-chart "x" "timestamp")
            y (.addMeasureAxis dimple-chart "y" "value")
            s (.addSeries dimple-chart (name type) js/dimple.plot.line (clj->js [x y]))]
       ; (.-attr (.selectAll (.-shapes x) "text") "transform" (fn [d] .select ))
        (aset s "data" (clj->js data))
        (.addLegend dimple-chart "5%" "10%" "20%" "10%" "right")
        (.draw dimple-chart)))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-figure [cursor owner]
  (reify
    om/IInitState
    (init-state [_])
    om/IRenderState
    (render-state [_ {:keys [chans]}]
      (dom/div nil
           (om/build chart-item cursor {:key :hecuba/name})))))


