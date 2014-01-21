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


;;;;;;;;;;; Data ;;;;;;;;;;;

(def mock-data {"01"     [{"month" "Jan" "reading" 0.8}
                          {"month" "Feb" "reading" 0.9}
                          {"month" "Mar" "reading" 0.8}
                          {"month" "Apr" "reading" 0.75}
                          {"month" "May" "reading" 0.65}
                          {"month" "Jun" "reading" 0.50}
                          {"month" "Jul" "reading" 0.55}
                          {"month" "Aug" "reading" 0.6}
                          {"month" "Sep" "reading" 0.66}
                          {"month" "Oct" "reading" 0.68}
                          {"month" "Nov" "reading" 0.71}
                          {"month" "Dec" "reading" 0.9}]
                "02"     [{"month" "january" "reading" 6}
                          {"month" "february" "reading" 10}
                          {"month" "march" "reading" 12}
                          {"month" "april" "reading" 15}
                          {"month" "may" "reading" 18}
                          {"month" "june" "reading" 20}
                          {"month" "july" "reading" 25}
                          {"month" "august" "reading" 31}
                          {"month" "september" "reading" 20}
                          {"month" "october" "reading" 17}
                          {"month" "november" "reading" 12}
                          {"month" "december" "reading" 9}]})

;;;;;;;;; Utils ;;;;;;;;;;;;;;;;;;;;;;

(defn- remove-chart []
  (.remove (first (nodelist-to-seq (.getElementsByTagName js/document "svg")))))

(defn- fetch-data [device-id]
  (get mock-data device-id))

;;;;;;;;;; Application state ;;;;;;;;;;;;;;;;;

(def chart-state
  (atom {:selected #{}
         :property "rad003"
         :devices [{:hecuba/name "01"
                    :name "External temperature"}
                   {:hecuba/name "02"
                    :name "External humidity"}]
         :data []}))

;;;;;;;;;; Chart Component ;;;;;;;;;;

(defn chart-component
  [device-details]
  (fn [cursor opts]
    (reify
      om/IWillMount
      (will-mount [this]
        (.log js/console "I will mount")
        (om/update! cursor update-in [:data] (fn [data] (get mock-data (:selected cursor)))))
      om/IRender
      (render [this]
        (.log js/console "I render")
        (dom/div nil
                 (dom/p nil (apply str (interpose "," (get-in cursor [:selected]))))
                 (dom/form nil
                           (for [device (get cursor :devices)]
                             (let [device-id   (str (:hecuba/name device))
                                   device-name (str (:name device))]
                               (.log js/console "I did mount" device-id device-name)
                               (dom/input #js {:type "checkbox"
                                               :onClick (fn [e]
                                                          (.dir js/console e)
                                                          (om/update! cursor update-in [:selected]
                                                                      conj device-id))}
                                          device-name))))
                 (dom/div #js {:id "chart"})))
      om/IDidUpdate
      (did-update [this prev-props prev-state root-node]
        (.log js/console "I did update")
        (let [n (.getElementById js/document "chart")]
             (while (.hasChildNodes n)
               (.removeChild n (.-lastChild n))))
         (let [Chart        (.-chart dimple)
              svg          (.newSvg dimple "#chart" 400 350)
              measurements []
              dimple-chart (Chart. svg (clj->js measurements))]
          (.setBounds dimple-chart 60 30 300 300)
          (.addCategoryAxis dimple-chart "x" "month")
          (.addMeasureAxis dimple-chart "y" "reading")
          (.addSeries dimple-chart nil js/dimple.plot.line)
          (.draw dimple-chart))))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn create-form-and-chart [model-path chart-comp]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [this]
        (dom/div nil
                 (dom/h3 nil (str "Metering data - " (get-in cursor [:property])))
                 (dom/h4 nil "Select devices to be plotted on the chart:")
                 (om/build chart-comp cursor))))))


(om/root chart-state (create-form-and-chart [:devices] (chart-component [:hecuba/name :name :data]))
                 (.getElementById js/document "app"))





