(ns kixi.hecuba.web.chart
  (:require
   [mrhyde.core :as mrhyde]
   [dommy.core :as dommy])
  (:use-macros
   [dommy.macros :only [node sel sel1]]))

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

(defn reload-chart [e]
 (.log js/console e)
 (.log js/console (dommy/value (sel1 :#devices)))
 (dommy/append! (sel1 :#chart) (init-bar-chart "#chart" 450 350 data "Word" "Awesomeness"))
 )

(dommy/append! (sel1 :#title) (node [:h3 "Metering data"]))
(dommy/listen! (sel1 :#submit) :click reload-chart)
;(init-bar-chart "#chart" 450 350 data "Word" "Awesomeness")
