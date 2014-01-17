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

(def mock-data {:external-humidity     [{"month" "january" "reading" 0.8}
                                        {"month" "february" "reading" 0.9}
                                        {"month" "march" "reading" 0.8}
                                        {"month" "april" "reading" 0.75}
                                        {"month" "may" "reading" 0.65}
                                        {"month" "june" "reading" 0.50}
                                        {"month" "july" "reading" 0.55}
                                        {"month" "august" "reading" 0.6}
                                        {"month" "september" "reading" 0.66}
                                        {"month" "october" "reading" 0.68}
                                        {"month" "november" "reading" 0.71}
                                        {"month" "december" "reading" 0.9}]
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
  [app {:keys [n]}]
  (.log js/console "device-list-item")
  (.log js/console (str "N: " n))
  (om/component
   (let [device   (get-in app [:devices n])
         selected (= n (:selected app))]
     (.log js/console (:name device))
     (dom/input #js {:id (:id device) :type "checkbox" :onClick #(select-device app n)} (:name device))
     )))

(defn devices-list [app]
  (om/component
   (.log js/console "devices-list")
   (dom/div #js {:id "devices"}
            (dom/h4 nil "Devlices")
            (dom/form nil (into-array
                           (map #(om/build device-list-item
                                           app
                                           {:opts {:key %}})
                                (range (count (:devices app)))))))))

;;;;;;;;;; Chart UI ;;;;;;;;;;

(defn chart-view [app opts]
  (reify
   om/IRender
   (render [this] (dom/div #js {:id "chart"}))
   om/IDidMount
   (did-mount [_ owner]
     (let [Chart        (.-chart dimple)
           svg          (.newSvg dimple "#chart" 450 450)
           dimple-chart (Chart. svg (clj->js (:external-humidity mock-data)))]
       (.addOrderRule (.addCategoryAxis dimple-chart "x" "month") "Date")
       (.addMeasureAxis dimple-chart "y" "reading")
       (.addSeries dimple-chart nil js/dimple.plot.line)
       (.draw dimple-chart)))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-ui [app]
  (om/component
   (dom/div nil
            (dom/header #js {:className "auth"})
            (dom/div #js {:id "data"}
                     (om/build devices-list app {:opts [:devices (:selected app)]}))
            (om/build chart-view app {:opts [:layers]}))))


(go (let [external-temp     {:name "External temperature"
                             :id "external_temp"
                             :data (:external-temperature mock-data)}
          external-humidity {:name "External humidity"
                             :id "external_humidity"
                             :data (:external-humidity mock-data)}]
      (let [init-state (update-in app-state [:devices]
                                  #(vec (concat % [external-temp external-humidity])))]
        (om/root init-state chart-ui (.getElementById js/document "app")))))




