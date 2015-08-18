(ns ^:figwheel-always kixi.tapestry.widgets.core
    (:require-macros [hiccups.core :as hiccups]
                     [cljs.core.async.macros :refer [go]])
    (:require  [cljs.core.async :refer [<! >! chan close! sliding-buffer put! alts!]]
               [cljs.reader :refer [read-string]]
               [om.core :as om :include-macros true]
               [om.dom :as dom :include-macros true]
               [hiccups.runtime :as hiccupsrt]
               [cljs-http.client :as http]
               [kixi.tapestry.widgets.xy :as xy]
               [kixi.tapestry.widgets.heatmap :as heatmap]))

(defonce xy-data-chan (chan))
(defonce heatmap-data-chan (chan))
(defonce app-state (atom {:xy {}
                          :heatmap {:data nil}
                          :heatmap-controls {:lcb   {:default 10}
                                             :ucb   {:default 30}
                                             :grads {:default 20}}}))

(enable-console-print!)

;;;;;;;;;;
;; Heatmap
(if-let [container (. js/document (getElementById "heatmap"))]

  (do (go (let [resp (<! (http/get "/data/heatmap.edn"))
                new-data (->> resp :body :data (map :value))
                new-default-lcb (.floor js/Math (apply min new-data))
                new-default-ucb (.ceil js/Math (apply max new-data))]
            (om/update! (om/root-cursor app-state) [:heatmap-controls :lcb :default] new-default-lcb)
            (om/update! (om/root-cursor app-state) [:heatmap-controls :ucb :default] new-default-ucb)
            (put! heatmap-data-chan {:data new-data
                                     :lcb new-default-lcb
                                     :ucb new-default-ucb
                                     :grads (-> @app-state :heatmap-controls :grads :default)})))

      (om/root (heatmap/chart
                {:width 800
                 :height 600
                 :data-chan heatmap-data-chan
                 :fill-out-of-range-cells? false
                 :x-label "Days of the Week"
                 :y-label "Time of the Day"})
               app-state
               {:target container
                :path [:heatmap]})


      ;; controls

      (defn update-chart-settings
        [owner cursor]
        (let [raw-lcb   (read-string (.-value (om/get-node owner "lcb-input")))
              raw-ucb   (read-string (.-value (om/get-node owner "ucb-input")))
              raw-grads (read-string (.-value (om/get-node owner "grads-input")))
              lcb       (if (nil? raw-lcb) (-> cursor :lcb :default) raw-lcb)
              ucb       (if (nil? raw-ucb) (-> cursor :ucb :default) raw-ucb)
              grads     (if (nil? raw-grads) (-> cursor :grads :default) raw-grads)]
          (put! heatmap-data-chan {:data (-> @app-state :heatmap :data)
                                   :lcb lcb
                                   :ucb ucb
                                   :grads grads}))
        nil)

      (if-let [container (. js/document (getElementById "heatmap-controls"))]
        (om/root
         (fn
           [cursor owner]
           (om/component
            (dom/div nil
                     (dom/span nil "Lower colour bound")
                     (dom/input #js {:ref "lcb-input" :placeholder (-> cursor :lcb :default)})
                     (dom/span nil "Upper colour bound")
                     (dom/input #js {:ref "ucb-input" :placeholder (-> cursor :ucb :default)})
                     (dom/span nil "Gradations")
                     (dom/input #js {:ref "grads-input" :placeholder (-> cursor :grads :default)})
                     (dom/button #js {:onClick
                                      #(update-chart-settings owner cursor)} "Refresh"))))
         app-state
         {:target container
          :path [:heatmap-controls]})
        (println "Heatmap controls container not found.")))
  (println "Heatmap container not found"))

;;;;;;;;;;
;; XY plot
(if-let [container (. js/document (getElementById "xy-plot"))]

  (do (go (let [resp (<! (http/get "/data/xyplot.edn"))
                new-data (:data (:body resp))]
            (put! xy-data-chan new-data)))

      (om/root (xy/chart
                {:width 800
                 :height 600
                 :x-range [0 200]
                 :y-range [0 200]
                 :data-chan xy-data-chan
                 :x-label "X Label"
                 :y-label "Y Label"})
               app-state
               {:target container
                :path [:xy]}))
  (println "XY plot container not found"))

;;
(defn on-js-reload [])
