(ns kixi.hecuba.web.chart
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [mrhyde.core :as mrhyde]
   [dommy.core :as dommy]
   [om.core :as om :include-macros true]
   [om.dom :as dom :include-macros true]
   [cljs.core.async :refer [<! chan put! sliding-buffer]])
  (:use-macros
   [dommy.macros :only [node sel sel1 by-tag]]))

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
  (let [chart (dimple-chart div width height data)]
    (.addCategoryAxis chart "x" x-axis-title)
    (.addMeasureAxis chart "y" y-axis-title)
    (.addSeries chart nil js/dimple.plot.bar)
    (.draw chart)))

(defn nodelist-to-seq
  "Converts nodelist to (not lazy) seq."
  [nl]
  (let [result-seq (map #(.item nl %) (range (.-length nl)))]
    (doall result-seq)))

(def data  [{"Word" "Hello" "Awesomeness" 2000}
            {"Word" "World" "Awesomeness" 3000}])

(defn reload-chart [e]
  (.log js/console e)
  (.log js/console (.getElementsByTagName js/document "form"))
  (.remove (first (nodelist-to-seq (.getElementsByTagName js/document "svg")))))



;; ----- Om + dimple -----

(enable-console-print!)

(def app-state
  (atom {:data []}))

;;;;;;;;;;; Data ;;;;;;;;;;;

(defn select-data
 [selection]
 )

(defn handle-submit
  [e app]
    (.log js/console (str "Handled submit and got: " e))
   ; (om/update! app )
    )

;;;;;;;;;;; Form ;;;;;;;;;;

(defn chart-form [app]
  (reify
    om/IRender
    (render [_]
      (dom/form
       #js {:className "chartForm" :onSubmit #(handle-submit % app)}
       (.log js/console "I render (chart-form)")
       (dom/input #js {:type "checkbox" :value "external_temp" :ref "external_temp"})
       (dom/input #js {:type "checkbox" :value "external_humidity" :ref "external_humidity"})
       (dom/input #js {:type "submit" :value "Update"})))))

;;;;;;;;;; Chart ;;;;;;;;;;

(defn chart-view [app opts]
  (reify
    om/IInitState
    (init-state [this]
      (.log js/console "I init state (chart-view)"))
    om/IWillMount
    (will-mount [_]
      (.log js/console "I will mount (chart-view)"))
    om/IRender
    (render [this]
      (.log js/console "I render (chart-component)")  
      (dom/div #js {:id "chart"} 
               (dom/h4 nil "Select reading types below.")))
    om/IDidMount
    (did-mount [_ owner]    
      (om/set-state! owner [:dimple-chart] (.newSvg dimple (:div opts) (:width opts) (:height opts)))
      (.log js/console "I did mount (chart-component)"))))


#_(let [svg   (om/get-state this [:dimple-chart])
                     Chart (.-chart dimple)]
                 (Chart. svg (clj->js data)))

(defn chart-app [app]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
            (om/build chart-view app
                      {:opts {:div "#chart"
                              :width 450
                              :heigth 350
                              :data []}})))))

(om/root app-state chart-app (.getElementById js/document "app"))



