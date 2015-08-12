(ns kixi.hecuba.widgets.heatmap
  (:require-macros [hiccups.core :as hiccups]
                   [cljs.core.async.macros :refer [go]])
  (:require [cljs.core.async :refer [<! >! chan close! sliding-buffer put! alts!]]
            [cljs.reader :refer [read-string]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [thi.ng.geom.viz.core :as viz]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.core.vector :as v]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.core.utils :as gu]
            [thi.ng.math.simplexnoise :as n]
            [thi.ng.math.core :as m :refer [PI]]
            [thi.ng.color.gradients :as grad]
            [hiccups.runtime :as hiccupsrt]
            [goog.string :as gstring]
            [goog.string.format]
            [cljs-http.client :as http]
            [bardo.interpolate :refer [pipeline]]))

(def x-readings 14)
(def y-readings 48)
;; TODO move this to local state
(def chart-width (atom 800))
(def chart-height (atom 600))
(def chart-data-chan (atom nil))
(def chart-fill-oor-cells? (atom true))
(def default-gradations 20)

(def colour-scheme
  "http://colorbrewer2.org/?type=diverging&scheme=RdYlBu&n=10"
  [[49 54 149] [69 117 180] [116 173 209] [171 217 233] [224 243 248] [254 224 144] [253 174 97] [244 109 67] [215 48 39] [165 0 38]])

(defn generate-palette
  "Generates n colours based on the gradations and color range specified - EXPENSIVE"
  [grads colours]
  (let [times (map #(/ (+ % 1) grads) (range grads))
        rgb-funcs {:red first :green second :blue last}]
    (mapv (fn [t] (mapv (fn [[k v]] ((pipeline (map #(/ (v %) 255) colours)) t) ) rgb-funcs)) times)))

(defn heatmap-spec
  [id heatmap-data size-x size-y lcb ucb grads]
  (let [matrix (viz/matrix-2d size-x size-y heatmap-data)]
    {:matrix        matrix
     :value-domain  [lcb ucb]
     :palette       (generate-palette grads colour-scheme)
     :palette-scale viz/linear-scale
     :layout        viz/svg-heatmap
     }))

(defn int-to-dow
  [num]
  (nth ["S" "M" "T" "W" "T" "F" "S"] (mod num 7)))

(defn int-to-tod
  [num]
  (let [hrs (mod (.floor js/Math (/ num 2)) 24)
        mins (if (zero? (mod num 2)) 0 30)]
    (gstring/format "%02d:%02d" hrs mins)))

(defn set-new-heatmap-data!
  [cursor data lcb ucb gradations]
  (let [lcb (if (nil? lcb) (.floor js/Math (apply min data)) lcb)
        ucb (if (nil? ucb) (.ceil js/Math (apply max data)) ucb)
        gradations (if (nil? gradations) default-gradations gradations)
        adjusted-data (if @chart-fill-oor-cells? (map #(m/clamp % lcb ucb) data) data)]
    (om/update! cursor :data data)
    (om/update! cursor :element {:x-axis (viz/linear-axis
                                          {:domain [0 x-readings]
                                           :range [55 (+ @chart-width 5)]
                                           :major 1
                                           :pos 30
                                           :label-dist -10
                                           :major-size -5
                                           :format #(int-to-dow (int %))
                                           :label {:text-anchor "right"}})
                                 :y-axis (viz/linear-axis
                                          {:domain [0 y-readings]
                                           :range [(- @chart-height 10) 35]
                                           :major 1
                                           :pos 50
                                           :label-dist 15
                                           :format #(int-to-tod (- y-readings %))
                                           :label {:text-anchor "end"}})
                                 :data     [(merge (heatmap-spec
                                                    :yellow-magenta-cyan
                                                    adjusted-data
                                                    x-readings
                                                    y-readings
                                                    lcb
                                                    ucb
                                                    gradations) nil)]})

    (om/update! cursor :element-legend {:x-axis (viz/linear-axis
                                                 {:domain [0 20]
                                                  :range [20 400]
                                                  :visible false})
                                        :y-axis (viz/linear-axis
                                                 {:domain [0 gradations]
                                                  :range [(- @chart-height 10) 35]
                                                  :major 1
                                                  :major-size 0
                                                  :pos 10
                                                  :label-dist -35
                                                  :label {:text-anchor "start"}
                                                  :format #(if (= % 0) lcb (if (= % gradations) ucb))})
                                        :data     [(merge (heatmap-spec
                                                           :yellow-magenta-cyan
                                                           (vec (for [x (map #(/ % gradations)(range 0 gradations))]
                                                                  (m/mix lcb ucb x)))
                                                           1
                                                           gradations
                                                           lcb
                                                           ucb
                                                           gradations) nil)]})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- data-loop [cursor input-chan]
  (go (loop [{:keys [data lcb ucb grads]} (<! input-chan)]
        (set-new-heatmap-data! cursor data lcb ucb grads)
        (recur (<! input-chan)))))

(defn chart
  [{:keys [width height data-chan fill-out-of-range-cells?]
    :or {width 800
         height 600}}]
  (if (nil? data-chan)
    (throw (js/Error. "Heatmap requires a data channel!"))
    (reset! chart-data-chan data-chan))
  (reset! chart-width width)
  (reset! chart-height height)
  (reset! chart-fill-oor-cells? fill-out-of-range-cells?)
  (fn
    [cursor owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (data-loop cursor @chart-data-chan))
      om/IRender
      (render [_]
        (dom/div #js {:style #js {:position "relative" :overflow "hidden" :whiteSpace "nowrap"}}
                    (dom/div #js {:style #js {:display "inline-block"}
                                  :dangerouslySetInnerHTML #js
                                  {:__html (->> (:element cursor)
                                                (viz/svg-plot2d-cartesian)
                                                (svg/svg {:width @chart-width :height @chart-height})
                                                (hiccups/html))}})
                    (dom/div #js {:style #js {:display "inline-block"}
                                  :dangerouslySetInnerHTML #js
                                  {:__html (->> (:element-legend cursor)
                                                (viz/svg-plot2d-cartesian)
                                                (svg/svg {:width @chart-width :height @chart-height})
                                                (hiccups/html))}}))))))
