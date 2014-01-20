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

(defn- fetch-data
  "The data need to be a vector."
  [cursor opts]
  (om/update!
   cursor #(assoc % :data (get mock-data (:selected cursor)))))

(defn- remove-chart []
  (.remove (first (nodelist-to-seq (.getElementsByTagName js/document "svg")))))

(defn- select-device!
  [cursor device-id]
  (.log js/console device-id)
 ; (remove-chart)
  (om/update!
   cursor #(assoc % :selected (vector device-id))))

;;;;;;;;;; Components ;;;;;;;;;;;;;;;;;

(def chart-state
  (atom {:selected ["01"]
         :property "rad003"
         :devices [{:hecuba/name "01"
                    :name "External temperature"}
                   {:hecuba/name "02"
                    :name "External humidity"}
                   {:hecuba/name "03"
                    :name "Ambient temperature"}]
         :data (get mock-data "01")}))

;;;;;;;;;;; Component 1:  List of devices for axis plots ;;;;;;;;;;

(defn device-list-item
  [devices]
  (fn [cursor owner]
    (om/component
    (let [device-details  (for [device devices] (get cursor device))
          device-id       (nth device-details 0) ;; TODO Sersiously need to change the way elements are accessed
          device-name     (str (nth device-details 1))
          selected        (= device-id (first (:selected cursor)))]
      (apply dom/input #js {:className (when selected "selected")
                            :type "checkbox"
                            :onClick #(select-device! data device-id)}
            device-name)))))

;;;;;;;;;; Component 2: Chart UI ;;;;;;;;;;

(defn chart-item
  [device-details]
  (fn [cursor opts]
    (reify
      om/IInitState
      (init-state [_]
        (om/update! cursor #(assoc % :selected "01")))
      om/IWillMount
      (will-mount [_]
        (go (fetch-data cursor opts)))
      om/IRender
      (render [this]
        (dom/div #js {:id "chart"}))
      om/IDidMount
      (did-mount [_ owner]
        (let [Chart        (.-chart dimple)
              svg          (.newSvg dimple "#chart" 400 350)
              measurements (:data cursor)
              dimple-chart (Chart. svg (clj->js measurements))]
          (.log js/console measurements)
          (.setBounds dimple-chart 60 30 300 300)
          (.addCategoryAxis dimple-chart "x" "month")
          (.addMeasureAxis dimple-chart "y" "reading")
          (.addSeries dimple-chart nil js/dimple.plot.line)
          (.draw dimple-chart) )))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn create-form-and-chart [model-path device-item chart-item]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (dom/div #js {:className "devices"}
                 (dom/h3 nil (str "Metering data - " (get-in cursor [:property])))
                 (dom/h4 nil "Select devices to be plotted on the chart:")
                 (dom/form #js {:className "devices-form"}
                           (om/build-all device-item
                                         (get-in cursor model-path)
                                         {:key :hecuba/name})
                           (om/build chart-item cursor)))))))


(om/root chart-state (create-form-and-chart [:devices]
                                                   (device-list-item [:hecuba/name :name])
                                                   (chart-item [:hecuba/name :name :data]))
                 (.getElementById js/document "app"))





