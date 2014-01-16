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

(def chart-svg)

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

(defn comment-list [app opts]
  (om/component
   (dom/div #js {:className "commentList"}
            (into-array
             (map #(om/build comment app
                             {:path [:comments %]
                              :key :id})
                  (range (count (:comments app))))))))

(defn handle-submit
  [e app]
    (.log js/console (str "Handled submit and got: " e))
    ())

(defn table-row [data owner]
  (om/component
      (dom/tr #js {:onClick (fn [e] (.log js/console "ooh!"))}
           (dom/td nil (:name data))
           (dom/td nil (apply str (interpose ", " (:leaders data)))))))

(defn table [app opts]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "table"}
               (dom/thead nil
                          (dom/tr nil
                                  (dom/th nil "Name")
                                  (dom/th nil "Leaders")))
               (dom/tbody nil
                          (om/build-all table-row
                                        (:projects data)
                                        (:key :name)))))))

(defn chart-form [app]
  (reify
    om/IRender
    (render [_]
      (dom/form
       #js {:className "chartForm" :onSubmit #(handle-submit % app)}
       (.log js/console "I'm rendering chart form.")
       (dom/input #js {:type "checkbox" :value "external_temp" :ref "external_temp"})
       (dom/input #js {:type "checkbox" :value "external_humidity" :ref "external_humidity"})
       (dom/input #js {:type "submit" :value "Update"})))))

(defn chart-component [app opts]
  (reify
    om/IWillMount
    (will-mount [_]
      (.log js/console "I'm about to mount"))
    om/IRender
    (render [_]
      (dom/div #js {:id "chart2"}
               (.log js/console "I'm rendering chart-component.")
               (dom/h2 nil "Metering data")
               (om/build chart-form app)))))

(defn chart-app [app]
  (reify
    om/IRender
    (render [_]
      (dom/div nil
               (om/build chart-component app
                         {:opts {:width 350
                                 :height 450
                                 :data []}})))))

;; Om version
(om/root app-state chart-app (.getElementById js/document "content"))

;; Dommy version
(dommy/listen! (sel1 :#submit) :click reload-chart)
(set! chart-svg (init-bar-chart "#chart" 450 350 data "Word" "Awesomeness"))

