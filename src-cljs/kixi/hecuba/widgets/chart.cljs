(ns kixi.hecuba.widgets.chart
  (:require
   [cljs-time.core :as t]
   [cljs-time.format :as tf]
   [mrhyde.core :as mrhyde]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.string :as str]))

(def dimple (this-as ct (aget ct "dimple")))
(def d3 (this-as ct (aget ct "d3")))

(mrhyde/bootstrap)
(enable-console-print!)

(def amon-date (tf/formatter "yyyy-MM-ddTHH:mm:ssZ"))

(defn- draw-chart [cursor measurements]
  (let [Chart            (.-chart dimple)
        svg              (.newSvg dimple "#chart" "100%" 600)
        [type device_id] (-> (get-in cursor [:sensor])
                             str
                             (str/split #"-"))
        data             (into [] (->> measurements
                                       (filter #(number? (:value %)))
                                       (map #(assoc % :id device_id))
                                       (map #(assoc % :timestamp (tf/parse amon-date (:timestamp %))))))
        unit             (get-in cursor [:unit])
        dimple-chart     (.setBounds (Chart. svg) "5%" "15%" "80%" "50%")
        x                (.addTimeAxis dimple-chart "x" "timestamp")
        y                (.addMeasureAxis dimple-chart "y" "value")
        s                (.addSeries dimple-chart type js/dimple.plot.line (clj->js [x y]))]
    (aset s "data" (clj->js data))
    (set! (.-tickFormat x) "%a %d %b %Y %H:%M")
    (set! (.-title x) "Time")
    (.addLegend dimple-chart "5%" "10%" "20%" "10%" "right")
    (.draw dimple-chart)
    (.attr (.-titleShape x) "y" 515)
    (.text (.-titleShape y) unit)
    (.attr (.selectAll (.-shapes x) "text") "transform" "rotate(45,0,12.6015625) translate(5, 0)")
    (.attr (.selectAll (.-shapes x) "text") "style" "text-anchor: start; font-family: sans-serif; font-size: 10px;")))

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
     (dom/div #js {:className "well well-lg"} "Chart"))
    om/IDidUpdate
    (did-update [_ _ _]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [measurements (get-in cursor [:measurements :measurements])]
        (when-not (empty? measurements)
          (draw-chart cursor measurements))))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-figure [cursor owner]
  (reify
    om/IInitState
    (init-state [_])
    om/IRenderState
    (render-state [_ {:keys [chans]}]
      (dom/div nil
           (om/build chart-item cursor {:key :hecuba/name})))))
