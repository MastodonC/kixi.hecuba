(ns kixi.hecuba.chart
  (:require
   [mrhyde.core :as mrhyde]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.string :as str]))

(def dimple (this-as ct (aget ct "dimple")))
(def d3 (this-as ct (aget ct "d3")))

(mrhyde/bootstrap)
(enable-console-print!)


;; Should be called after the draw function
(defn clean-axis [axis interval]
  (if (> (.-length (.-shapes axis)) 0)
    (let [del (atom 0)]
      (when (> interval 1)
        (let [text (.selectAll (.-shapes axis) "text")]
          (.each text (fn [d]
                        (when (not= (mod @del interval) 0)
                          (.remove (js* "this"))
                          (.each (.selectAll (.-shapes axis) "line") (fn [d2]
                                                                       (if (= d d2)
                                                                         (.remove (js* "this"))))))
                        (swap! del inc))))))))

(defn chart-item
  [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_])
    om/IRender
    (render [_]
       (dom/div nil))
    om/IDidMount
    (did-mount [_]
      (let [Chart        (.-chart dimple)
            svg          (.newSvg dimple "#chart" "100%" 600)
            data         []
            dimple-chart (.setBounds (Chart. svg) "5%" "15%" "80%" "50%")
            x            (.addCategoryAxis dimple-chart "x" "timestamp")
            y            (.addMeasureAxis dimple-chart "y" "value")
            s            (.addSeries dimple-chart "type" js/dimple.plot.line (clj->js [x y]))]
        (aset s "data" (clj->js data))
        (.addLegend dimple-chart "5%" "10%" "20%" "10%" "right")
        (.draw dimple-chart)))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [Chart            (.-chart dimple)
            svg              (.newSvg dimple "#chart" "100%" 600)
            data             (get-in cursor [:measurements :measurements])
            _ (prn data)
            unit             (get-in cursor [:unit])
            dimple-chart     (.setBounds (Chart. svg) "5%" "15%" "80%" "50%")
            [type device-id] (->> (get-in cursor [:sensor])
                                  (re-matches #"(.*?)-(.*?)")
                                  next)
            x                (.addCategoryAxis dimple-chart "x" "timestamp")
            y                (.addMeasureAxis dimple-chart "y" "value")
            s                (.addSeries dimple-chart "type" js/dimple.plot.line (clj->js [x y]))]
        (aset s "data" (clj->js data))
        (.addLegend dimple-chart "5%" "10%" "20%" "10%" "right")
        (.draw dimple-chart)
        (.text (.-titleShape y) unit)
        (let [n (count data)] (clean-axis x (Math/round (+ (/ n 50) 0.5))))
        (.attr (.selectAll (.-shapes x) "text") "transform" (fn [d]
                                                              (let [transform (.attr (.select d3 (js* "this")) "transform")]
                                                                (when-not (empty?
                                                                transform)
                                                                    (str transform " rotate(-45)")))))))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-figure [cursor owner]
  (reify
    om/IInitState
    (init-state [_])
    om/IRenderState
    (render-state [_ {:keys [chans]}]
      (dom/div nil
           (om/build chart-item cursor {:key :hecuba/name})))))
