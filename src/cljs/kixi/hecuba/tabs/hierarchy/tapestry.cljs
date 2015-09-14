(ns kixi.hecuba.tabs.hierarchy.tapestry
  (:require-macros [cljs.core.async.macros :refer [go go-loop]]
                   [hiccups.core :as hiccups])
  (:require [clojure.string :as str]
            [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [ajax.core :refer [GET]]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [cljs-time.predicates :as tp]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.model :refer (app-model)]
            [cljs.core.async :refer [<! >! chan close! sliding-buffer put! alts!]]
            [cljs.reader :refer [read-string]]
            [om.dom :as dom :include-macros true]
            [thi.ng.geom.viz.core :as viz]
            [thi.ng.geom.svg.core :as svg]
            [thi.ng.geom.core.vector :as v]
            [thi.ng.geom.core :as g]
            [thi.ng.geom.core.utils :as gu]
            [thi.ng.math.simplexnoise :as n]
            [thi.ng.math.core :as m :refer [PI]]
            [thi.ng.color.gradients :as grad]
            [thi.ng.ndarray.core :as nd]
            [hiccups.runtime :as hiccupsrt]
            [goog.string :as gstring]
            [goog.string.format]
            [bardo.interpolate :refer [interpolate pipeline]]
            [kixi.hecuba.widgets.datetimepicker-small :as dtp]
            [kixi.hecuba.tabs.hierarchy.raw-data :as raw]
            [kixi.hecuba.tabs.hierarchy.data :as data]))


(def x-readings 14)
(def y-readings 24)
(def chart-width (atom 1000))
(def chart-height (atom 600))
(def chart-data-chan (atom nil))
(def chart-fill-oor-cells? (atom true))
(def default-gradations 20)
(def invalid-cell-colour "#AAAAAA")

(def colour-scheme
  "http://colorbrewer2.org/?type=diverging&scheme=RdYlBu&n=10"
  [[49 54 149] [69 117 180] [116 173 209] [171 217 233] [224 243 248] [254 224 144] [253 174 97] [244 109 67] [215 48 39] [165 0 38]])

(defn generate-palette
  "Generates n colours based on the gradations and color range specified - EXPENSIVE"
  [grads colours]
  (let [times (map #(/ (+ % 1) grads) (range grads))
        rgb-funcs {:red first :green second :blue last}]
    (mapv (fn [t] (mapv (fn [[k v]] ((pipeline (map #(/ (v %) 255) colours)) t) ) rgb-funcs)) times)))

(defn svg-heatmap-with-title
  "Custom heatmap function, as we add titles to the polygons"
  [{:keys [x-axis y-axis project]}
   {:keys [matrix value-domain clamp palette palette-scale attribs shape]
    :or {value-domain [0.0 1.0]
         palette-scale viz/linear-scale
         shape #(conj (svg/polygon [%1 %2 %3 %4] {:fill %5}) [:title %6])}
    :as d-spec}]
  (let [scale-x   (:scale x-axis)
        scale-y   (:scale y-axis)
        pmax      (dec (count palette))
        scale-v   (palette-scale value-domain [0 pmax])]
    (svg/group
     attribs
     (for [p (nd/position-seq matrix)
           :let [[y x] p
                 v (nd/get-at matrix y x)]]
       (shape
        (project [(scale-x x) (scale-y y)])
        (project [(scale-x (inc x)) (scale-y y)])
        (project [(scale-x (inc x)) (scale-y (inc y))])
        (project [(scale-x x) (scale-y (inc y))])
        (if (or clamp (m/in-range? value-domain v))
          (palette (m/clamp (int (scale-v v)) 0 pmax))
          invalid-cell-colour)
        v)))))

(defn heatmap-spec
  [id heatmap-data size-x size-y lcb ucb grads]
  (let [matrix (viz/matrix-2d size-x size-y heatmap-data)]
    {:matrix        matrix
     :value-domain  [lcb ucb]
     :palette       (generate-palette grads colour-scheme)
     :palette-scale viz/linear-scale
     :layout        svg-heatmap-with-title
     }))

(defn int-to-tod
  [num]
  (let [hrs (mod num 24)
        mins 0]
    (gstring/format "%02d:%02d" hrs mins)))

(defn set-new-heatmap-data!
  [cursor data lcb ucb gradations dates]
  (let [lcb (if-not lcb (.floor js/Math (apply min data)) lcb)
        ucb (if-not ucb (.ceil js/Math (apply max data)) ucb)
        midcb ((interpolate lcb ucb) 0.5)
        gradations (if-not gradations default-gradations gradations)
        adjusted-data (if @chart-fill-oor-cells? (map #(m/clamp % lcb ucb) data) data)]
    (om/update! cursor :dates dates)
    (om/update! cursor :data data)
    (om/update! cursor :element {:x-axis (viz/linear-axis
                                          {:domain [0 x-readings]
                                           :range [55 (+ @chart-width 5)]
                                           :major 1
                                           :pos 30
                                           :label-dist -10
                                           :major-size -5
                                           :format #(if (< % (count dates))
                                                      (str (first (nth dates %))" - " (second (nth dates %)))
                                                      "")
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
                                                  :format #(condp = %
                                                             0 lcb
                                                             gradations ucb
                                                             (/ gradations 2) midcb
                                                             nil)})
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

(defn matrix [r c data]
  (map-indexed
   (fn[i _]
     ((comp (partial take c) (partial drop (* c i))) data)) (range r)))

(defn- data-loop [cursor input-chan]
  (go-loop [{:keys [data dates lcb ucb grads]} (<! input-chan)]
      (set-new-heatmap-data!
       (:heatmap cursor)
       data
       lcb
       ucb
       grads
       dates)
      (recur (<! input-chan))))

(defn chart
  [{:keys [width height data-chan fill-out-of-range-cells? x-label y-label]
    :or {width @chart-width
         height @chart-height}}]
  (if-not data-chan
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
        (if-let [main-data-cursor (-> cursor :heatmap :element)]
          (let [main-data (->> main-data-cursor
                               (viz/svg-plot2d-cartesian)
                               (svg/svg {:width @chart-width :height @chart-height}))
                legend-data (->> (-> cursor :heatmap :element-legend)
                                 (viz/svg-plot2d-cartesian)
                                 (svg/svg {:width @chart-width :height @chart-height}))]
            (dom/div #js {:style #js {:position "relative"
                                      :overflow "hidden"
                                      :whiteSpace "nowrap"
                                      :fontFamily "sans-serif"
                                      :fontSize "11px"
                                      :textAlign "center"
                                      :width (str (+ 100 @chart-width) "px")}}
                     (dom/div nil (dom/span nil x-label))
                     (dom/span #js {:style #js {:float "left"
                                                ;;:height (str (/ @chart-height 2) "px")
                                                :margin (str (/ @chart-height 2) "px -30px 0px -30px")

                                                :WebkitTransform "rotate(-90deg)"
                                                :MozTransform "rotate(-90deg)"
                                                :msTransform "rotate(-90deg)"
                                                :OTransform "rotate(-90deg)"
                                                :filter "progid:DXImageTransform.Microsoft.BasicImage(rotation=3)"}} y-label)
                     (dom/div #js {:style #js {:display "inline-block"}
                                   :dangerouslySetInnerHTML #js
                                   {:__html (hiccups/html main-data)}})
                     (dom/div #js {:style #js {:display "inline-block"}
                                   :dangerouslySetInnerHTML #js
                                   {:__html (hiccups/html legend-data)}}))))))))

(defn calculate-end-date [start-date]
  (let [d (tf/parse (tf/formatter "yyyy-MM-dd") start-date)]
    (->> (t/plus d (t/weeks 2))
         (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss")))))

(defn rewind-one-hour [date]
  (let [d (tf/parse (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ") date)]
    (->> (t/minus d (t/hours 1))
         (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss")))))

(defn process-data
  "This will currently only work for hourly data"
  [start-date data]
  ;; step 1
  (let [fmt (tf/formatter "yyyy-MM-dd")
        start-date-ts (tf/parse fmt start-date)
        dates (map (fn [d] (tf/unparse fmt d)) (map #(t/plus start-date-ts (t/days %)) (range 0 x-readings)))
        ;;
        rewind-time  (map #(assoc % :timestamp (rewind-one-hour (:timestamp %))) data) ;; rewind timestamps by an hour
        remove-times (map #(-> %
                                (assoc-in [:value] (js/parseFloat (:value %)))
                                (assoc-in [:date] (.slice (:timestamp %) 0 10))
                                (assoc-in [:time] (.slice (:timestamp %) 11 19))
                                (dissoc :sensor_id)
                                (dissoc :timestamp)) rewind-time)
        grouped-data  (take x-readings (group-by :date remove-times))
        grouped-data-map (into {} (map #(apply hash-map %) grouped-data))
        grouped-data-patched (into {} (map (fn [date] (hash-map date (if (contains? grouped-data-map date) (get grouped-data-map date) []))) dates))
        with-hours    (apply merge
                            (map (fn [[k v]]
                                   (hash-map k (map (fn [m] {:hour (js/parseInt (.slice (:time m) 0 2))
                                                             :value (:value m)}) v))) grouped-data-patched))
       without-blank (apply merge
                            (map (fn [[k v]]
                                   (let [hours (->> v (map :hour) set)]
                                     (hash-map k (apply merge v
                                                        (remove nil? (map (fn [i]
                                                                            (if-not (contains? hours i) {:hour i :value nil})) (range 0 y-readings))))))) with-hours)) ;; add nil values for missing times
       sorted-groups (apply merge
                            (map (fn [[k v]]
                                   (hash-map k (sort-by :hour v))) without-blank))
       sorted-top-map (into (sorted-map) sorted-groups)
       flattened (reduce concat (map (fn [[k v]]
                                       (map :value v)) sorted-top-map))
       rotated (flatten (reverse (apply map list (matrix x-readings y-readings flattened))))]
    rotated))

(defn load-heatmap-data [heatmap-data-chan tapestry-data start-date resp]
  (om/update! tapestry-data [:error] false)
  (try
    (let [new-data (process-data start-date (:measurements resp))
          new-dates (distinct (map (fn [{:keys [timestamp]}]
                                     (let [date-part (.substring timestamp 0
                                                                 (.indexOf timestamp "T"))
                                           date-split (re-find #"\d{4}-(\d\d)-(\d\d)" date-part)
                                           dt (tf/parse (tf/formatter "yyyy-MM-dd") date-part)
                                           dow (tf/days (- (t/day-of-week (tf/parse (tf/formatter "yyyy-MM-dd") date-part)) 1))
                                           dow_abrv (.substring dow 0 3)]
                                       [(str (nth date-split 2) "/" (nth date-split 1)) dow_abrv]))
                                   (:measurements resp)))
          data-range (map js/parseFloat (remove nil? new-data))
          number-data (map js/parseFloat new-data)
          new-default-lcb (.floor js/Math (apply min data-range))
          new-default-ucb (.ceil js/Math (apply max data-range))]
      (om/update! tapestry-data [:heatmap-controls :lcb :default] new-default-lcb)
      (om/update! tapestry-data [:heatmap-controls :ucb :default] new-default-ucb)
      (put! heatmap-data-chan {:data number-data
                               :dates (vec new-dates)
                               :lcb new-default-lcb
                               :ucb new-default-ucb
                               :grads (-> @tapestry-data :heatmap-controls :grads :default)}))
    (catch js/Object e
      (log "An exception occurred whilst fetching data: " e)
      (om/update! tapestry-data [:error] true)
      (throw e))))

(defn process-get-data-click [tapestry-data start-date entity_id sensor data-chan]
  (let [[type device_id] (str/split sensor #"~")
        start-ts (data/date->amon-timestamp start-date)
        end-ts   (calculate-end-date start-date)
        url (data/url-str start-ts end-ts entity_id device_id type :hourly_rollups)]
    (om/update! tapestry-data :date start-date)
    (GET url
         {:handler #(load-heatmap-data data-chan tapestry-data start-date %)
          :error-handler (fn [{:keys [status status-text]}]
                           (log "There was an error. Status " status " Text " status-text))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))

(defn update-chart-settings
  [owner cursor heatmap-data-chan]
  (let [raw-lcb   (read-string (.-value (om/get-node owner "lcb-input")))
        raw-ucb   (read-string (.-value (om/get-node owner "ucb-input")))
        raw-grads nil ;; (read-string (.-value (om/get-node owner "grads-input")))
        lcb       (if-not raw-lcb (-> cursor :heatmap-controls :lcb :default) raw-lcb)
        ucb       (if-not raw-ucb (-> cursor :heatmap-controls :ucb :default) raw-ucb)
        grads     (if-not raw-grads (-> cursor :heatmap-controls :grads :default) raw-grads)]
    (put! heatmap-data-chan {:data (-> @cursor :heatmap :data)
                             :dates (-> @cursor :heatmap :dates)
                             :lcb lcb
                             :ucb ucb
                             :grads grads}))
  nil)

(defn sensors-table [sensors owner {:keys [event-chan]}]
  (reify
    om/IInitState
    (init-state [_]
      {:th-chan (chan)
       :sort-spec {:sort-key :type
                   :sort-asc true}})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [th-chan sort-spec]} (om/get-state owner)
              {:keys [sort-key sort-asc]} sort-spec
              th-click                    (<! th-chan)]
          (if (= th-click sort-key)
            (om/update-state! owner #(assoc %
                                       :sort-spec {:sort-key th-click
                                                   :sort-asc (not sort-asc)}))
            (om/update-state! owner #(assoc %
                                       :sort-spec {:sort-key th-click
                                                   :sort-asc true}))))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [sort-spec]}]
      (let [{:keys [sort-key sort-asc]} sort-spec
            history                     (om/get-shared owner :history)
            table-id                    "sensors-table"
            sensors-with-data (filter #(every? seq [(:lower_ts %) (:upper_ts %)]) sensors)]
        (html
         [:div.col-md-12 {:style {:overflow "auto"}}
          [:table {:class "table table-hover table-condensed"}
           [:thead
            [:tr
             (raw/sorting-th owner "Description" :description)
             (raw/sorting-th owner "Type" :type)
             (raw/sorting-th owner "Unit" :unit)
             (raw/sorting-th owner "Period" :period)
             (raw/sorting-th owner "Resolution" :resolution)
             (raw/sorting-th owner "Device ID" :device_id)
             (raw/sorting-th owner "Location" :location)
             (raw/sorting-th owner "Earliest Event" :lower_ts)
             (raw/sorting-th owner "Last Event" :upper_ts)
             (raw/sorting-th owner "Status" :status)]]
           [:tbody
            (for [row (if sort-asc
                        (sort-by sort-key sensors-with-data)
                        (reverse (sort-by sort-key sensors-with-data)))]
              (om/build raw/sensor-row row {:opts {:event-chan event-chan}}))]]])))))

(defn tapestry-div [{:keys [raw-data entity_id tapestry-data]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:event-chan (chan)
       :data-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [event-chan data-chan]}  (om/get-state owner)
              {:keys [event value]} (<! event-chan)]
          (case event
            :default-date (om/update! tapestry-data :date value)
            :sensor-click (om/update! raw-data :selected value)
            :date-selected (om/update! tapestry-data :date value)
            :fetch-data (process-get-data-click tapestry-data value entity_id (:selected @raw-data) data-chan)))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [event-chan data-chan]}]
      (html
       [:div [:h3 "Tapestry Charts"]
        (om/build sensors-table (:sensors raw-data) {:opts {:event-chan event-chan}})
        [:div.row.row-centered
         (om/build dtp/control tapestry-data {:init-state {:event-chan event-chan}})]
        (if (:error tapestry-data)
          [:div.row.row-centered
           [:h4 "There was an error displaying this chart."]]
          [:div
           {:style {:width "1000px" :margin "0 auto"}}
           (om/build (chart {:data-chan data-chan
                             :fill-out-of-range-cells? true
                             :x-label "Dates"
                             :y-label "Time of the Day"}) tapestry-data)])

        (if (and (not (:error tapestry-data)) (-> tapestry-data :heatmap :element))
          [:div.row.row-centered
           (let [form-style  {:margin-right "10px" :float "none"}]
             [:form.form-inline {:style {:margin-left "30px"}}
              [:div.form-group
               [:label {:style form-style} "Lower Bound"]
               [:input.form-control {:style (merge {:width "60px"} form-style) :ref "lcb-input" :placeholder (-> tapestry-data :heatmap-controls :lcb :default)}]
               [:div.form-group
                [:label {:style form-style} "Upper Bound"]
                [:input.form-control {:style (merge {:width "60px"} form-style) :ref "ucb-input" :placeholder (-> tapestry-data :heatmap-controls :ucb :default)}]]
               ;;[:span "Gradations"]
               ;;[:input {:ref "grads-input" :placeholder (-> tapestry-data :heatmap-controls :grads :default)}]
               [:button.btn.btn-primary.fa.fa-refresh {:onClick
                                                       #(do
                                                          (update-chart-settings owner tapestry-data data-chan)
                                                          (.preventDefault %))}]]])])]))))
