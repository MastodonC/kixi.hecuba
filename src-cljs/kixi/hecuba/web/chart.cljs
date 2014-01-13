(ns kixi.hecuba.web.chart
  (:require
   [mrhyde.core :as mrhyde]
   ))

(def dimple (this-as ct (aget ct "dimple")))

(mrhyde/bootstrap)

(defn create-svg
  [div width height]
  (.newSvg dimple div width height))

(defn dimple-chart 
  [div width height data]
  (let [Chart (.-chart dimple)
        svg   (create-svg div width height)]
    (Chart. svg (clj->js data))))

(defn init-bar-chart
  [div width height data x-axis-title y-axis-title]
 ; (.log js/console "Height: " height)
  (let [chart (dimple-chart div width height data)]
    (.addCategoryAxis chart "x" x-axis-title)
    (.addMeasureAxis chart "y" y-axis-title)
    (.addSeries chart nil js/dimple.plot.bar)
    (.draw chart)))

(def data  [{"Word" "Hello" "Awesomeness" 2000}
            {"Word" "World" "Awesomeness" 3000}])

(init-bar-chart "#chart" 450 350 data "Word" "Awesomeness")

