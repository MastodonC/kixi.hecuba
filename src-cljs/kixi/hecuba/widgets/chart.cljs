(ns kixi.hecuba.widgets.chart
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [cljs.core.async :refer [<! >! chan put!]]
   [cljs-time.core :as t]
   [cljs-time.format :as tf]
   [cljs-time.coerce :as tc]
   [om.core :as om :include-macros true]
   [clojure.string :as str]
   [kixi.hecuba.common :refer (log)]
   [cljs.reader :as reader]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

(defn parse-measurements [measurements]
  (->> measurements
       (map #(let [v (:value %)]
               (assoc % :value
                      (cond (nil? v) nil
                            (number? v) v
                            (re-matches #"[-+]?\d+(\.\d+)?" v) (js/parseFloat v)))))
       (filter #(number? (:value %)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Info box

(defn info-row [id type value timestamp]
  (str "<p>Device ID: " id "</br>Type: " type "</br>Timestamp: " timestamp "</br>Value: " value "</p>"))

(defn update-infobox [div info]
  (let [{:keys [id type value timestamp style]} info]
    (doto div
      (-> (.html (info-row id type value timestamp))
          (.style "border" (str "thin solid " style))
          (.style "border-radius" "8px")))))

(defn create-infobox [div left top]
  (-> js/d3 (.select div) (.append "div")
      (.attr "id" "tooltip")
      (.attr "class" "my-tooltip")
      (.style "opacity" 1)
      (.style "left" left)
      (.style "top" top)
      (.style "background" "white")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SVG

(defn create-svg [div width height margin]
  (-> js/d3 (.select div) (.append "svg:svg")
      (.attr #js {:width  (+ width (:left margin) (:right margin))
                  :height (+ height (:top margin) (:bottom margin))})
      (.append "svg:g")
      (.attr #js {:transform (str "translate(" (:left margin) "," (:top margin) ")")})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Colours

(defn color-scale [series theme]
  (-> js/d3 .-scale (.ordinal) (.domain (to-array (distinct (map :sensor series)))) (.range theme)))

(defn color-domain [series color-scale]
  (-> color-scale (.domain) (.map (fn [sensor]
                                    #js {:sensor sensor
                                         :values (to-array (filter #(= (:sensor %) sensor) series))}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Min and max

(defn min-max-dates
  [measurements]
  (reduce (fn [acc {:keys [timestamp]}]
            (if (:min-date acc)
              (assoc acc
                :min-date (min timestamp (:min-date acc))
                :max-date (max timestamp (:max-date acc)))
              (assoc acc
                :min-date timestamp
                :max-date timestamp)))
          {}
          measurements))

(defn min-max-timestamps [data1 data2]
  (if (every? seq [data1 data2])
    (let [data1-range (min-max-dates data1)
          data2-range (min-max-dates data2)
          min         (min (:min-date data1-range) (:min-date data2-range))
          max         (max (:max-date data1-range) (:max-date data2-range))]
      [min max])
    (let [{:keys [min-date max-date]} (min-max-dates data1)]
      [min-date max-date])))

(defn min-max-values [series]
  (if (seq series)
    (reduce (fn [{:keys [minimum maximum]} new]
              {:minimum (min minimum (:value new))
               :maximum (max maximum (:value new))})
            {:minimum 0 :maximum 2}
            series)
    {:minimum 0 :maximum 0}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Draw chart

(defn y-scale [min max height]
  (-> js/d3 .-scale (.linear) (.domain (to-array [min max])) (.range (to-array [height 0]))))

(defn line-fn [x-scale y-scale]
  (doto (-> js/d3 .-svg (.line))
    (.interpolate "basis")
    (.x (fn [d]
          (x-scale (:timestamp d))))
    (.y (fn [d]
          (y-scale (:value d))))))

(defn draw-line [svg axis-div class color-fn line-fn data]
  (-> (.selectAll svg axis-div)
      (.data data)
      (.enter)
      (.append "g")
      (.attr "class" class)
      (.append "svg:path")
      (.attr "class" "line")
      (.attr "d" (fn [d] (line-fn (aget d "values"))))
      (.style "stroke" (fn [d] (color-fn (aget d "sensor"))))))

(defn- draw-chart [measurements infobox-chan]
  (let [margin          {:top 10 :right 90 :bottom 100 :left 90}
        width           (-> (.getElementById js/document "chart") .-clientWidth (- (:left margin) (:right margin)))
        height          (-> (.getElementById js/document "chart") .-clientHeight (- (:top margin) (:bottom margin)))
        [data1 data2]   measurements
        ;; Left data
        series-left     (parse-measurements data1)
        unit-left       (-> series-left first :unit)
        ;; Right data
        series-right    (parse-measurements data2)
        unit-right      (-> series-right first :unit)
        ;; Min and max for all scales
        [min-date max-date]   (min-max-timestamps series-left series-right)
        series1-min-max (min-max-values series-left)
        series2-min-max (min-max-values series-right)

        ;; X
        x-scale         (-> js/d3 .-time (.scale) (.domain (to-array [min-date max-date])) (.range (to-array [0 width])))
        x-axis          (-> js/d3 (.-svg) (.axis) (.scale x-scale) (.orient "bottom")
                            (.ticks 10) (.tickFormat (.format js/d3.time "%Y-%m-%d %H:%M:%S")))
        ;; Left
        color-left      (color-scale series-left (to-array ["#6baed6" "#4292c6" "#2171b5" "#08519c" "#08306b"]))
        y1-scale        (y-scale (:minimum series1-min-max) (:maximum series1-min-max) height)
        line1           (line-fn x-scale y1-scale)
        y-axis-left     (-> js/d3 (.-svg) (.axis) (.scale y1-scale) (.ticks 8) (.orient "left"))
        sensors-left    (color-domain series-left color-left)
        ;; Right
        color-right     (color-scale series-right (to-array ["#fd8d3c" "#f16913" "#d94801" "#a63603" "#7f2704"]))
        y2-scale        (y-scale (:minimum series2-min-max) (:maximum series2-min-max) height)
        line2           (line-fn x-scale y2-scale)
        y-axis-right    (-> js/d3 (.-svg) (.axis) (.scale y2-scale) (.ticks 8) (.orient "right"))
        sensors-right   (color-domain series-right color-right)
        ;; Main chart
        svg             (create-svg "#chart" width height margin)]

    (doto svg
      (-> (.append "svg:g")
          (.attr "class" "x axis")
          (.attr "transform" (str "translate(0," height ")"))
          (.call x-axis)
          (.selectAll "text")
          (.style "text-anchor" "start")
          (.attr "class" "x axis text")
          (.attr "dx" ".5em")
          (.attr "dy" ".20em")
          (.attr "transform" (fn [d] "rotate(45)")))
      (-> (.append "svg:g")
          (.attr "class" "y axis axisLeft")
          (.attr "transform" "translate(-15,0)")
          (.call y-axis-left)
          (.append "svg:text")
          (.attr "y" 0)
          (.attr "dx" "-.7em")
          (.style "text-anchor" "end")
          (.text unit-left))
      (-> (.append "svg:g")
          (.attr "class" "y axis axisRight")
          (.attr "transform" (str "translate(" (+ 15 width) ",0)"))
          (.call y-axis-right)
          (.append "svg:text")
          (.attr "y" 0)
          (.attr "dx" ".7em")
          (.style "text-anchor" "start")
          (.text unit-right)))

    (draw-line svg ".sensor-left" "sensor-left" color-left line1 sensors-left)
    (draw-line svg ".sensor-right" "sensor-right" color-right line2 sensors-right)

    (-> svg
        (.append "rect")
        (.attr "class" "overlay")
        (.attr "width" width)
        (.attr "height" height)
        (.on "mouseover" (fn [_]
                           (put! infobox-chan {:event :mouseover :v true})))
        (.on "mouseout" (fn [_]
                          (put! infobox-chan {:event :mouseover :v false})))
        (.on "mousemove" (fn [_]
                           (let [[x y]     (js->clj (-> js/d3 (.mouse (js* "this"))))
                                 timestamp (.invert x-scale x)]
                             (put! infobox-chan {:event :timestamp :v timestamp})))))))

(defn chart-item [chart owner opts]
  (reify
    om/IRender
    (render [_]
      (html [:div]))
    om/IDidMount
    (did-mount [_]
      (let [n (.getElementById js/document "chart")]
        (while (.hasChildNodes n)
          (.removeChild n (.-lastChild n))))
      (let [measurements (:measurements chart)]
        (draw-chart measurements (:chan opts))))))

;;;;;;;;;;; Bootstrap ;;;;;;;;;;;;

(defn chart-figure [chart owner opts]
  (reify
    om/IRender
    (render [_]
      (html
       [:div
        (om/build chart-item chart {:opts opts})]))))
