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
(enable-console-print!)

(defn nodelist-to-seq
  "Converts nodelist to (not lazy) seq."
  [nl]
  (let [result-seq (map #(.item nl %) (range (.-length nl)))]
    (doall result-seq)))

(def app-state
  (atom {:data []}))

;;;;;;;;;;; Data ;;;;;;;;;;;

(def data  [{"Word" "Hello" "Awesomeness" 2000}
            {"Word" "World" "Awesomeness" 3000}])

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
       (.log js/console "I'm rendering chart form.")
       (dom/input #js {:id "external_temp" :type "checkbox" :ref "external_temp" :value "external_temp" :name "external_temp"})
       (dom/label #js {:htmlFor "external_temp" } "External Temp")
       (dom/input #js {:id "external_humidity" :type "checkbox" :value "external_humidity" :name "external_humidity" :ref "external_humidity"})
       (dom/label #js {:htmlFor "external_humidity"} "External Humidity")
       (dom/input #js {:type "submit" :value "Update"})))))

;;;;;;;;;; Chart ;;;;;;;;;;

(defn remove-chart []
  (.remove (first (nodelist-to-seq (.getElementsByTagName js/document "svg")))))

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
      (dom/div #js {:id "form"} 
                (om/build chart-form app)
                (dom/h4 nil "Select reading types below.")))
    om/IDidMount
    (did-mount [_ owner]
      (let [Chart        (.-chart dimple)
            svg          (.newSvg dimple "#chart" 450 350)
            dimple-chart (Chart. svg (clj->js data))]
          (.addCategoryAxis dimple-chart "x" "Word")
          (.addMeasureAxis dimple-chart "y" "Awesomeness")
          (.addSeries dimple-chart nil js/dimple.plot.bar)
         (.draw dimple-chart))
      (.log js/console "I did mount (chart-component)"))))


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



