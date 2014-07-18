(ns kixi.hecuba.widgets.chart
  (:require
   [cljs-time.core :as t]
   [cljs-time.format :as tf]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.string :as str]))

(def amon-date (tf/formatter "yyyy-MM-ddTHH:mm:ssZ"))

(defn- draw-chart [cursor measurements]
  (let [Chart            (.-chart js/dimple)
        svg              (.newSvg js/dimple "#chart" "100%" 600)
        [type device_id] (-> (get-in cursor [:sensor])
                             str
                             (str/split #"-"))
        data             (into [] (->> measurements
                                       (filter #(or (number? (:value %))
                                                    (re-matches #"[^A-DF-Za-z]+" (:value %))))
                                       (map #(assoc % :id device_id))
                                       (map #(assoc % :timestamp (tf/parse amon-date (:timestamp %))))))
        unit             (get-in cursor [:unit])
        dimple-chart     (.setBounds (Chart. svg) "5%" "15%" "80%" "50%")
        x                (.addTimeAxis dimple-chart "x" "timestamp")
        y                (.addMeasureAxis dimple-chart "y" "value")
        s                (.addSeries dimple-chart "sensor" js/dimple.plot.line (clj->js [x y]))]
    (aset s "data" (clj->js data))
    (set! (.-tickFormat x) "%a %-d %b %Y %H:%M")
    (set! (.-title x) "Time")
    (.addLegend dimple-chart "20%" "5%" "25%" "10%" "right")
    (.draw dimple-chart)
    (.text (.-titleShape y) unit)))

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
      (let [measurements (get-in cursor [:measurements])]
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
