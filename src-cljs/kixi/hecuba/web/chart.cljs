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

(def mock-data {"01"     [{"device_id" "01" "device_name" "External temperature" "month" "01/01/2011" "reading" 0.8}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/02/2011" "reading" 0.9}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/03/2011" "reading" 0.8}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/04/2011" "reading" 0.75}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/05/2011" "reading" 0.65}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/06/2011" "reading" 0.50}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/07/2011" "reading" 0.55}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/08/2011" "reading" 0.6}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/09/2011" "reading" 0.66}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/10/2011" "reading" 0.68}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/11/2011" "reading" 0.71}
                          {"device_id" "01" "device_name" "External temperature" "month" "01/12/2011" "reading" 0.9}]
                "02"     [{"device_id" "02" "device_name" "External humidity" "month" "01/01/2011" "reading" 6}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/02/2011" "reading" 10}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/03/2011" "reading" 12}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/04/2011" "reading" 15}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/05/2011" "reading" 18}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/06/2011" "reading" 20}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/07/2011" "reading" 25}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/08/2011" "reading" 31}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/09/2011" "reading" 20}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/10/2011" "reading" 17}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/11/2011" "reading" 12}
                          {"device_id" "02" "device_name" "External humidity" "month" "01/12/2011" "reading" 9}]})

;;;;;;;;; Utils ;;;;;;;;;;;;;;;;;;;;;;

(defn- fetch-data [device-id]
  (get mock-data device-id))

;;;;;;;;;; Application state ;;;;;;;;;;;;;;;;;

(def chart-state
  (atom {:selected {:left #{}
                    :right #{}}
         :property "rad003"
         :devices [{:hecuba/name "01"
                    :name "External temperature"}
                   {:hecuba/name "02"
                    :name "External humidity"}]}))

;;;;;;;;;; Chart Component ;;;;;;;;;;

(defn chart-component
  [device-details]
  (fn [cursor opts]
    (reify
      om/IRender
      (render [this]
        (dom/div nil
                 (dom/div #js {:id "chart" :width 500 :height 450})
                 (dom/table #js {:id "form-table" :cellSpacing "10"}
                            (let [cols {:left "Left axis plots" :right "Right axis plots"}]
                               (dom/tr nil
                                       (for [col cols]
                                         (dom/td nil
                                                 (dom/h4 nil (val col))
                                                 (dom/form nil
                                                           (for [device (get cursor :devices)]
                                                             (let [device-id   (str (:hecuba/name device))
                                                                   device-name (str (:name device))
                                                                   device-ref  (str (:red device))]
                                                               (dom/div nil
                                                                        (dom/input #js {:type "checkbox"
                                                                                        :ref device-ref
                                                                                        :value device-id
                                                                                        :onChange (fn [e]
                                                                                                   (om/update! cursor update-in [:selected (key col)] (if (.. e -target -checked) conj disj) device-id))})
                                                                        (dom/label #js {:htmlFor device-id} device-name)
                                                                        (dom/br #js {}))))))))))))
      om/IDidUpdate
      (did-update [this prev-props prev-state root-node]
        (let [n (.getElementById js/document "chart")]
             (while (.hasChildNodes n)
               (.removeChild n (.-lastChild n))))
         (let [Chart        (.-chart dimple)
               svg          (.newSvg dimple "#chart" 500 450)
               measurements (apply concat (vals (select-keys mock-data (merge (:left (:selected cursor)) (:right (:selected cursor))))))
               l-axis-label (str (into [] (:left (:selected cursor))))
               r-axis-label (str (into [] (:right (:selected cursor))))
               dimple-chart (Chart. svg (clj->js measurements))
               x            (.addCategoryAxis dimple-chart "x" "month")
               y1           (.addMeasureAxis dimple-chart "y" "reading")
               y2           (.addMeasureAxis dimple-chart "y" "reading")]
          (.setBounds dimple-chart 60 30 300 300)
          (.addSeries dimple-chart "device_id" js/dimple.plot.line (clj->js [x y1]))
          (.addSeries dimple-chart "device_id" js/dimple.plot.line (clj->js [x y2]))
          (.addLegend dimple-chart 60 10 300 20 "right")
          (.draw dimple-chart))))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn create-form-and-chart [model-path chart-comp]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [this]
        (dom/div nil
                 (dom/h3 nil (str "Metering data - " (get-in cursor [:property])))
                 (dom/p nil "Note: When you select something to plot on a given axis, you will only be able to plot other items of the same unit on that axis.")
                 (om/build chart-comp cursor))))))


(om/root chart-state (create-form-and-chart [:devices] (chart-component [:hecuba/name :name :data]))
                 (.getElementById js/document "app"))





