(ns kixi.hecuba.chart
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [mrhyde.core :as mrhyde]
   [dommy.core :as dommy]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [clojure.string :as str]
   [kixi.hecuba.history :as history])
  (:use-macros
   [dommy.macros :only [node sel sel1 by-tag]]))

(def dimple (this-as ct (aget ct "dimple")))

(mrhyde/bootstrap)
(enable-console-print!)

(defn nodelist-to-seq
  "Converts nodelist to (not lazy) seq."
  [nl]
  (let [result-seq (map #(.item nl %) (range (.-length nl)))]
    (doall result-seq)))


;;;;;;;;; Utils ;;;;;;;;;;;;;;;;;;;;;;

(def truthy? (complement #{"false"}))

(defn url [entity-id device-id start-date end-date]
  (str "/3/entities/" entity-id "/devices/" device-id "/measurements?startDate=" start-date "&endDate" end-date) )

;;;;; Date picker component ;;;;;;;




;;;;;;;;;;;;; Component 2: Chart ;;;;;;;;;;;;;;;;

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
      (prn "[I did update - chart]")
      (prn "type: " (get-in cursor [:sensor :type]))
      (let [Chart            (.-chart dimple)
            svg              (.newSvg dimple "#chart" 500 500)
            [type device-id] (str/split (get-in cursor [:sensor]) #"-")
            data             (get-in cursor [:measurements])
            dimple-chart     (.setBounds (Chart. svg) 60 30 350 350)
            x (.addCategoryAxis dimple-chart "x" "timestamp")
            y (.addMeasureAxis dimple-chart "y" "value")
            s (.addSeries dimple-chart (name type) js/dimple.plot.line (clj->js [x y]))]
        (.log js/console data)
        (aset s "data" (clj->js data))
        (.addLegend dimple-chart 60 10 300 20 "right")
        (.draw dimple-chart)))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-figure [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      ;{:chans {:selected (chan (sliding-buffer 1))}}
      )
    om/IRenderState
    (render-state [_ {:keys [chans]}]
      (dom/div nil
           (om/build chart-item cursor {:key :hecuba/name})))))


