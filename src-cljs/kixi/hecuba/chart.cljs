(ns kixi.hecuba.chart
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
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


;;;;;;;;; Utils ;;;;;;;;;;;;;;;;;;;;;;

(def truthy? (complement #{"false"}))

(defn url [entity-id device-id start-date end-date]
  (str "/3/entities/" entity-id "/devices/" device-id "/measurements?startDate=" start-date "&endDate" end-date) )

;;;;; Date picker component ;;;;;;;

(defn date-picker
  [cursor owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [selected]}]
      (dom/table #js {:id "date-table"}
                 (dom/tr nil
                         (dom/td nil 
                                 (dom/h4 nil "Start date")
                                 (dom/input #js
                                            {:type "text"
                                             :id "dateFrom"
                                             :ref "dateFrom"}))
                         (dom/td nil
                                 (dom/h4 nil "End date")
                                 (dom/input #js
                                            {:type "text"
                                             :id "dateTo"
                                             :ref "dateTo"}))
                         (dom/td nil
                                 (dom/h4 nil)
                                 (dom/button #js {:type "button"
                                                  :onClick (fn [e]
                                                             (let [start (-> (om/get-node owner "dateFrom")
                                                                             .-value)
                                                                   end   (-> (om/get-node owner "dateTo")
                                                                             .-value)]
                                                               ;; TODO this doesn't work. Can't read cursor here.
                                                               ;; Can't read cursor in will-mount of chart-item either.
                                                               ;; the only place where cursor can be read is render.
                                                               ;; but this causes infinite loop because getting measurements updates
                                                               ;; the state and triggers re-render.
                                                               (put! selected {:entity-id (get-in cursor [:entity-id])
                                                                               :sensor (get-in cursor [:sensor])
                                                                               :start-date start :end-date end})
                                                               ))}
                                             "Select dates")))))))


;;;;;;;;;;;;; Component 2: Chart ;;;;;;;;;;;;;;;;

(defn chart-item
  [cursor owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [clicked-items (om/get-state owner [:selected])]
        (go (while true
              (let [sel        (<! clicked-items)
                    start-date (get sel :start-date)
                    end-date   (get sel :end-date)
                    entity-id  (get sel :entity-id)
                    device-id  (get-in sel [:sensor :deviceId])
                    url        (url entity-id device-id start-date end-date)
                    ]
                 (GET url {:handler #(om/transact! cursor [:measurements] (constantly %))
                                   :headers {"Accept" "application/json"}
                                   :response-format :json
                                   :keywords? true})
                )))))
    om/IRender
    (render [_] 
      (prn "[I render]")
       (dom/div nil
                 (dom/div #js {:id "chart" :width 500 :height 550})))
    om/IDidUpdate
    (did-update [this prev-props prev-state root-node]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (prn "[I did update]")
      (let [Chart      (.-chart dimple)
            svg        (.newSvg dimple "#chart" 500 500)
            type       (get-in cursor [:sensor :type])
            data       (get-in cursor [:measurements])
            dimple-chart (.setBounds (Chart. svg) 60 30 350 350)
            x (.addCategoryAxis dimple-chart "x" "timestamp")
            y (.addMeasureAxis dimple-chart "y" "value")
            s (if (not (empty? data )) (.addSeries dimple-chart (name type) js/dimple.plot.line (clj->js [x y])))]
        (.log js/console data)
        (if (not (nil? s)) (aset s "data" (clj->js data)))
        (.addLegend dimple-chart 60 10 300 20 "right")
        (.draw dimple-chart)))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-figure [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:chans {:selected (chan (sliding-buffer 1))}})
    om/IRenderState
    (render-state [_ {:keys [chans]}]
      (dom/div nil
           (dom/h3 #js {:key "head"} (str "Metering data - " (get-in cursor [:property])))
           (dom/p nil "Note: When you select something to plot on a given axis, you will only be able to plot other items of the same unit on that axis.")
           (om/build date-picker cursor {:key :hecuba/name :init-state chans})
           (om/build chart-item cursor {:key :hecuba/name :init-state chans})
          ))))


