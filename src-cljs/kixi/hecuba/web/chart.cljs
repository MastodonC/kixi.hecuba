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

(def mock-data {"01"
                [{"device_id" "01" "device_name" "External temperature" "month" "01/01/2011" "reading" 0.8}
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
                "02"
                [{"device_id" "02" "device_name" "External humidity" "month" "01/01/2011" "reading" 6}
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

(def truthy? (complement #{"false"}))

;;;;;;;;;;; Component 1:  Form containing list of devices for both axis plots ;;;;;;;;;;

(defn device-form
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [clicked]}]
      (dom/table #js {:id "form-table" :cellSpacing "10"}
           (let [cols {:left "Left axis plots" :right "Right axis plots"}]
             (dom/tr nil
                  (into-array
                   (for [col cols]
                     (dom/td nil
                          (dom/h4 nil (val col))
                          (dom/form nil
                               (into-array
                                (for [device cursor]
                                  (let [id (str (:hecuba/name device))
                                        name (str (:name device))
                                        axis (first col)]
                                    (dom/div nil
                                         (dom/input #js
                                              {:type "checkbox"
                                               :onChange
                                               (fn [e]
                                                 (let [checked (= (.. e -target -checked) true)]
                                                   (put! clicked
                                                         {:id id
                                                          :axis axis
                                                          :checked checked})))})
                                         (dom/label nil name)
                                         (dom/br #js {})))))))))))))))

;;;;;;;;;;;;; Component 2: Chart ;;;;;;;;;;;;;;;;

(defn chart-item
  [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [clicked-items (om/get-state owner [:clicked])]
        (go (while true
              (let [sel (<! clicked-items)
                    checked         (str (get sel :checked))
                    axis            (get sel :axis)
                    id              (str (get sel :id))]
                (println "Consumed axis: " (str axis)
                         ". Id " (str id)
                         ". Checked: " (str checked))
                (om/set-state! owner [:selected axis id] (truthy? checked)))))))

    om/IRender
    (render [_]
      (dom/div #js {:id "chart" :width 500 :height 550}))

    om/IDidUpdate
    (did-update [this prev-props prev-state root-node]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [Chart (.-chart dimple)
            svg          (.newSvg dimple "#chart" 500 500)
            data-keys    (map first (filter second (om/get-state owner [:selected :left])))
            measurements (apply concat (vals (select-keys mock-data data-keys)))
            dimple-chart (Chart. svg (clj->js measurements))
            x (.addCategoryAxis dimple-chart "x" "month")
            y1 (.addMeasureAxis dimple-chart "y" "reading")
            y2 (.addMeasureAxis dimple-chart "y" "reading")]
        (.setBounds dimple-chart 60 30 350 350)
        (.addSeries dimple-chart "device_id" js/dimple.plot.line (clj->js [x y1]))
        (.addSeries dimple-chart "device_id" js/dimple.plot.line (clj->js [x y2]))
        (.addLegend dimple-chart 60 10 300 20 "right")
        (.draw dimple-chart)))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-figure [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chans {:clicked (chan (sliding-buffer 1))}})
    om/IRenderState
    (render-state [_ {:keys [chans]}]
      (dom/div nil
           (dom/h3 #js {:key "head"} (str "Metering data - " (get-in cursor [:property])))
           (dom/p nil "Note: When you select something to plot on a given axis, you will only be able to plot other items of the same unit on that axis.")
           ;; Builds chart component
           (om/build chart-item cursor {:key :hecuba/name :init-state chans})
           ;; Builds table containing form components for left and right axis
           (println "Devices is" (:devices cursor))
           (dom/div #js {:id "device-form"}
                (om/build device-form (:devices cursor)
                    {:key :hecuba/name :init-state chans}))))))
