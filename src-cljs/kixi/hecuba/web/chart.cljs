(ns kixi.hecuba.web.chart
  (:require
   [mrhyde.core :as mrhyde]
   ))

(def dimple (this-as ct (aget ct "dimple")))

(mrhyde/bootstrap)

(def width 450)
(def height 350)

(def svg (-> dimple (.newSvg "#chart" width height)))

(def data  [{"Word" "Hello" "Awesomeness" 2000}
            {"Word" "World" "Awesomeness" 3000}])

(let [Chart        (.-chart dimple)
      dimple-chart (Chart. svg (clj->js data))]

  (.log js/console "Chart: " dimple-chart)
  (.addCategoryAxis dimple-chart "x" "Word")
  (.addMeasureAxis dimple-chart "y" "Awesomeness")
  (.addSeries dimple-chart nil js/dimple.plot.bar)
  (.draw dimple-chart))
