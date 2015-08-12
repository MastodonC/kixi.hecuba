(ns kixi.hebcua.widgets.xy
    (:require-macros [hiccups.core :as hiccups]
                     [cljs.core.async.macros :refer [go]])
    (:require  [cljs.core.async :refer [<! >! chan close! sliding-buffer put! alts!]]
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
               [cljs-http.client :as http]))

(def chart-width (atom 800))
(def chart-height (atom 600))
(def chart-x-range (atom [0 200]))
(def chart-y-range (atom [0 200]))
(def chart-data-chan (atom nil))

(defn set-new-xy-plot-data!
  [cursor data]
  (om/update! cursor :element {:x-axis (viz/linear-axis
                                        {:domain @chart-x-range
                                         :range [50 (- @chart-width 10)]
                                         :pos (- @chart-height 20)
                                         :major 20
                                         :minor 10})
                               :y-axis (viz/linear-axis
                                        {:domain @chart-y-range
                                         :range [(- @chart-height 20) 20]
                                         :major 10
                                         :minor 5
                                         :pos 50
                                         :label-dist 15 :label {:text-anchor "end"}})
                               :grid   {:attribs {:stroke "#caa"}
                                        :minor-x true
                                        :minor-y true}
                               :data   [{:values  (:values data)
                                         :attribs {:fill "#06f" :stroke "#06f"}
                                         :shape   (viz/svg-square 2)
                                         :layout  viz/svg-scatter-plot}
                                        {:values (:line data)
                                         :attribs {:fill "none" :stroke "#f23"}
                                         :layout viz/svg-line-plot}]}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- data-loop [cursor input-chan]
  (go (loop [new-data (<! input-chan)]
        (set-new-xy-plot-data! cursor new-data)
        (recur (<! input-chan)))))

(defn chart
  [{:keys [width height x-range y-range data-chan]
    :or {width 800
         height 600
         x-range [0 200]
         y-range [0 200]}}]
  (if (nil? data-chan)
    (throw (js/Error. "XY Plot requires a data channel!"))
    (reset! chart-data-chan data-chan))
  (reset! chart-width width)
  (reset! chart-height height)
  (reset! chart-x-range x-range)
  (reset! chart-y-range y-range)
  (fn
    [cursor owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (data-loop cursor @chart-data-chan))
      om/IRender
      (render [_]
        (dom/div #js {:dangerouslySetInnerHTML #js
                      {:__html (->> @cursor
                                    :element
                                    viz/svg-plot2d-cartesian
                                    (svg/svg {:width @chart-width :height @chart-height})
                                    hiccups/html)}})))))
