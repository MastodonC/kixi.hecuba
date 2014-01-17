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

(def mock-data {:external-humidity     [{"month" "Jan" "reading" 0.8}
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
                :external-temperature  [{"month" "january" "reading" 6}
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

(def app-state
  {:selected false
   :devices []})

(defn remove-chart []
  (.remove (first (nodelist-to-seq (.getElementsByTagName js/document "svg")))))

(defn select-device [app device]
  (.log js/console "select-device")
  (remove-chart)
  (om/update! app [:selected] (constantly device))
  )

;;;;;;;;;;; List of devices for axis plots ;;;;;;;;;;

(defn device-list-item
  [devices]
  (fn [data owner]
    (.log js/console "device-list-item")
    (om/component
     (apply dom/input #js {:type "checkbox" :onClick #(select-device app (:hecuba/name device))}
            (for [device devices]
              (str (get data device)))))))

;;;;;;;;;; Chart UI ;;;;;;;;;;

(defn chart-view [data opts]
  (reify
   om/IRender
   (render [this] (dom/div #js {:id "chart"}))
   om/IDidMount
   (did-mount [_ owner]
     (let [Chart        (.-chart dimple)
           svg          (.newSvg dimple "#chart" 400 350)
           dimple-chart (Chart. svg (clj->js (:external-humidity mock-data)))]
       (.setBounds dimple-chart 60 30 300 300)
       (.addCategoryAxis dimple-chart "x" "month")
       (.addMeasureAxis dimple-chart "y" "reading")
       (.addSeries dimple-chart nil js/dimple.plot.line)
       (.draw dimple-chart)))))

(defn chart-item
  [data]
  (fn [data opts]
    (reify
      om/IRender
      (render [this]
        (.log js/console "[chart] I render")
        (dom/div #js {:id "chart"}))
      om/IDidMount
      (did-mount [_ owner]
        (let [Chart        (.-chart dimple)
              svg          (.newSvg dimple "#chart" 400 350)
              dimple-chart (Chart. svg (clj->js (:external-humidity mock-data)))]
          (.setBounds dimple-chart 60 30 300 300)
          (.addCategoryAxis dimple-chart "x" "month")
          (.addMeasureAxis dimple-chart "y" "reading")
          (.addSeries dimple-chart nil js/dimple.plot.line)
          (.draw dimple-chart) )))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn create-form-and-chart [model-path device-item chart-item]
  (fn [data owner]
    (reify
      om/IRender
      (render [_]
        (om/build chart-item data)
        (dom/div #js {:className "devices"}
                 (dom/form #js {:className "devices-form"}
                           (om/build-all device-item
                                         (get-in data model-path)
                                         {:key :hecuba/name})))))))


(go (let [external-temp     {:hecuba/name "01"
                             :name "External temperature"
                             :data (:external-temperature mock-data)}
          external-humidity {:hecuba/name "02"
                             :name "External humidity"
                             :data (:external-humidity mock-data)}]
      (let [init-state (update-in app-state [:devices]
                                  #(vec (concat % [external-temp external-humidity])))]
        (om/root init-state (create-form-and-chart [:devices]
                                                   (device-list-item [:hecuba/name :name])
                                                   (chart-item [:hecuba/name :name]))
                 (.getElementById js/document "app")))))




