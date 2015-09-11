(ns kixi.hecuba.tabs.hierarchy.xyplot
  (:require-macros [hiccups.core :as hiccups]
                   [cljs.core.async.macros :refer [go go-loop]])
  (:require [clojure.string :as str]
            [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [ajax.core :refer [GET]]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [kixi.hecuba.history :as history]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.model :refer (app-model)]
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
            [clojure.string :as string]
            [kixi.hecuba.widgets.datetimepicker-small :as dtp]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.tabs.hierarchy.raw-data :as raw-data]
            [kixi.hecuba.tabs.hierarchy.sensors :as sensors]))

(def chart-width (atom 800))
(def chart-height (atom 600))
(def chart-data-chan (atom nil))

(defn set-new-xy-plot-data!
  [cursor {:keys [data x-range y-range x-major x-minor y-major y-minor]}]
  (om/update! cursor :element {:x-axis (viz/linear-axis
                                        {:domain x-range
                                         :range [50 (- @chart-width 10)]
                                         :pos (- @chart-height 20)
                                         :major x-major
                                         :minor x-minor})
                               :y-axis (viz/linear-axis
                                        {:domain y-range
                                         :range [(- @chart-height 20) 20]
                                         :major y-major
                                         :minor y-minor
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

(defn calculate-end-date [start-date]
  (let [d (tf/parse (tf/formatter "yyyy-MM-dd") start-date)]
    (->> (t/plus d (t/weeks 2))
         (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss")))))

(defn- data-loop [cursor input-chan]
  (go (loop [new-data (<! input-chan)]
        (set-new-xy-plot-data! cursor new-data)
        (recur (<! input-chan)))))

(defn chart
  [{:keys [width height data-chan x-label y-label]
    :or {width 800
         height 600}}]
  (if-not data-chan
    (throw (js/Error. "XY Plot requires a data channel!"))
    (reset! chart-data-chan data-chan))
  (reset! chart-width width)
  (reset! chart-height height)
  (fn
    [cursor owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (data-loop cursor @chart-data-chan))
      om/IRender
      (render [_]
        (if (-> cursor :element)
          (dom/div #js {:style #js {:position "relative"
                                    :overflow "hidden"
                                    :whiteSpace "nowrap"
                                    :fontFamily "sans-serif"
                                    :fontSize "11px"
                                    :textAlign "center"
                                    :width (str (+ 50 @chart-width) "px")}}
                   (dom/span #js {:style #js {:float "left"
                                              :margin (str (/ @chart-height 2) "px -40px 0px -10px")
                                              :WebkitTransform "rotate(-90deg)"
                                              :MozTransform "rotate(-90deg)"
                                              :msTransform "rotate(-90deg)"
                                              :OTransform "rotate(-90deg)"
                                              :filter "progid:DXImageTransform.Microsoft.BasicImage(rotation=3)"}} (str "Unit: " y-label))
                   (dom/div #js {:dangerouslySetInnerHTML #js
                                 {:__html (->> @cursor
                                               :element
                                               viz/svg-plot2d-cartesian
                                               (svg/svg {:width @chart-width :height @chart-height})
                                               hiccups/html)}})
                   (dom/div #js {:style #js {:height "20px" :marginTop "5px"}} (dom/span nil (str "Unit: " x-label)))))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn sensor-row [{:keys [xyplot-data sensor]} owner {:keys [table-id event-chan]}]
  (reify
    om/IRender
    (render [_]
      (html
       (let [{:keys [device_id type unit period resolution status
                     parent-device lower_ts upper_ts actual_annual editable]} sensor
                     {:keys [description privacy location]} parent-device
                     id (str type "~" device_id)
                     selected-x? (= id (:current-x xyplot-data))
                     selected-y? (= id (:current-y xyplot-data))]
         [:tr {:on-click (fn [e]
                           (put! event-chan {:event :sensor-click :value id})
                           (when (and lower_ts upper_ts)
                             (put! event-chan {:event :default-date :value (common/unparse-date lower_ts "yyyy-MM-dd")}))
                           (.preventDefault e))
               :id (str table-id "-selected")}
          [:td [:input {:type "checkbox"
                        :checked selected-x?
                        :on-click (fn [e]
                                    (let [checked? (.. e -target -checked)]
                                      (put! event-chan {:event :x-row-click
                                                        :value {:id id :checked checked? :label unit}})
                                      (if (and checked? selected-y?)
                                        (put! event-chan {:event :y-row-click
                                                          :value {:id id :checked false}})))
                                    (.stopPropagation e))}]]
          [:td [:input {:type "checkbox"
                        :checked selected-y?
                        :on-click (fn [e]
                                    (let [checked? (.. e -target -checked)]
                                      (put! event-chan {:event :y-row-click
                                                        :value {:id id :checked checked? :label unit}})
                                      (if (and checked? selected-x?)
                                        (put! event-chan {:event :x-row-click
                                                          :value {:id id :checked false}})))
                                    (.stopPropagation e))}]]
          [:td description]
          [:td type]
          [:td unit]
          [:td period]
          [:td resolution]
          [:td device_id]
          [:td (common/location-col location)]
          [:td (if-let [t (common/unparse-date lower_ts "yyyy-MM-dd")] t "")]
          [:td (if-let [t (common/unparse-date upper_ts "yyyy-MM-dd")] t "")]])))))

(defn sensors-table [cursor owner {:keys [event-chan]}]
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
            sensors (:sensors cursor)
            sensors-data      (sensors/fetch-sensors (:property-details cursor) [])
            sensors-with-data (filter #(every? seq [(:lower_ts %) (:upper_ts %)]) sensors-data)
            table-id                    "sensors-table"]
        (html
         [:div.col-md-12 {:style {:overflow "auto"}}
          [:table {:class "table table-hover table-condensed"}
           [:thead
            [:tr
             (raw-data/sorting-th owner "X" :x)
             (raw-data/sorting-th owner "Y" :y)
             (raw-data/sorting-th owner "Description" :description)
             (raw-data/sorting-th owner "Type" :type)
             (raw-data/sorting-th owner "Unit" :unit)
             (raw-data/sorting-th owner "Period" :period)
             (raw-data/sorting-th owner "Resolution" :resolution)
             (raw-data/sorting-th owner "Device ID" :device_id)
             (raw-data/sorting-th owner "Location" :location)
             (raw-data/sorting-th owner "Earliest Event" :lower_ts)
             (raw-data/sorting-th owner "Last Event" :upper_ts)
             (raw-data/sorting-th owner "Status" :status)]]
           [:tbody
            (for [row (if sort-asc
                        (sort-by sort-key sensors-with-data)
                        (reverse (sort-by sort-key sensors-with-data)))]
              (om/build sensor-row {:sensor row :xyplot-data (:xyplot-data cursor)} {:opts {:event-chan event-chan}}))]]])))))

(defn calc-linear-regression-line
  [data]
  (let [n (->> data count)
        sum #(reduce + %)
        sum-x (->> data (map first) sum)
        sum-y (->> data (map second)  sum)
        sum-x-times-y (->> data (map (fn [[x y]] (* x y))) sum)
        sum-x-times-x (->> data (map (fn [[x y]] (* x x))) sum)
        a (/ (- (* sum-y sum-x-times-x) (* sum-x sum-x-times-y))
             (- (* n sum-x-times-x) (* sum-x sum-x)))
        b (/ (- (* n sum-x-times-y) (* sum-x sum-y))
             (- (* n sum-x-times-x) (* sum-x sum-x)))
        fn #(float (+ a (* b %)))
        min-x (->> data (map first) (apply min))
        max-x (->> data (map first) (apply max))
        xy1 [min-x (fn min-x)]
        xy2 [max-x (fn max-x)]]
    [xy1 xy2]))

(defn process-data
  [[x-data y-data]]
  (let [timestamp-key :timestamp
        same-timestamps (clojure.set/intersection (set (map timestamp-key x-data)) (set (map timestamp-key y-data)))
        filtered-x-data (filter (fn [m] (some #(= % (timestamp-key m)) same-timestamps)) x-data)
        filtered-y-data (filter (fn [m] (some #(= % (timestamp-key m)) same-timestamps)) y-data)
        x-raw-data (->> filtered-x-data (sort-by :timestamp) (mapv :value) (map js/parseFloat))
        y-raw-data (->> filtered-y-data (sort-by :timestamp) (mapv :value) (map js/parseFloat))
        data {:values (mapv vector x-raw-data y-raw-data)}
        reg-line (calc-linear-regression-line (:values data))
        data (assoc data :line reg-line)
        x-range [(->> data :values (map first) (apply min))
                 (* 1.05 (->> data :values (map first) (apply max)))] ;; add 5% to max just to leave a small gap
        y-range [(min (->> data :values (map second) (apply min)) (-> reg-line first second))
                 (* 1.05 (max (->> data :values (map second) (apply max)) (-> reg-line second second)))]
        x-major (.floor js/Math (/ (second x-range) 10))
        x-minor (/ x-major 2)
        y-major (.floor js/Math (/ (second y-range) 10))
        y-minor (/ y-major 2)]
    {:data data
     :x-range x-range
     :y-range y-range
     :x-minor x-minor
     :x-major x-major
     :y-minor y-minor
     :y-major y-major}))

(defn load-xyplot-data [key data-chan xyplot-data resp]
  (om/update! xyplot-data [:error] false)
  (let [state (-> @(om/update! xyplot-data key (:measurements resp)) :properties :xyplot)]
    (if (and (:data-x state) (:data-y state))
      (try
        (let [new-data (process-data ((juxt :data-x :data-y) state))]
          (put! data-chan new-data))
        (catch js/Object e
          (log "An exception occurred whilst fetching data: " e)
          (om/update! xyplot-data [:error] true))))))

(defn process-get-data-click
  [xyplot-data start-date entity_id data-chan]
  (let [sensors ((juxt :current-x :current-y) @xyplot-data)
        [type1 device_id1] (str/split (first sensors) #"~")
        [type2 device_id2] (str/split (second sensors) #"~")
        start-ts (data/date->amon-timestamp start-date)
        end-ts   (calculate-end-date start-date)
        url1 (data/url-str start-ts end-ts entity_id device_id1 type1 :hourly_rollups)
        url2 (data/url-str start-ts end-ts entity_id device_id2 type2 :hourly_rollups)]
    (om/update! xyplot-data :date start-date)
    (om/update! xyplot-data :data-x nil)
    (om/update! xyplot-data :data-y nil)
    (GET url1
         {:handler #(load-xyplot-data :data-x data-chan xyplot-data %)
          :error-handler (fn [{:keys [status status-text]}]
                           (log "There was an error. Status " status " Text " status-text))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})
    (GET url2
         {:handler #(load-xyplot-data :data-y data-chan xyplot-data %)
          :error-handler (fn [{:keys [status status-text]}]
                           (log "There was an error. Status " status " Text " status-text))
          :headers {"Accept" "application/json"}
          :response-format :json
          :keywords? true})))


(defn xyplot-div
  [{:keys [xyplot-data sensors property-details entity_id] :as cursor} owner]
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
            :default-date (om/update! xyplot-data :date value)
            :date-selected (om/update! xyplot-data :date value)
            :sensor-click nil
            :fetch-data (process-get-data-click xyplot-data value entity_id data-chan)
            :x-row-click (do
                           (let [{:keys [id checked label]} value]
                             (om/transact! xyplot-data #((if checked assoc dissoc) % :current-x id))
                             (if checked
                               (om/update! xyplot-data :label-x label))))
            :y-row-click (do
                           (let [{:keys [id checked label]} value]
                             (om/transact! xyplot-data #((if checked assoc dissoc) % :current-y id))
                             (if checked
                               (om/update! xyplot-data :label-y label))))))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [event-chan data-chan]}]
      (html
       [:div [:h3 "XY Plot"]
        [:div {:id "sensors-table"}
         (om/build sensors-table cursor {:opts {:event-chan event-chan}})]
        [:div.row.row-centered
         (om/build dtp/control xyplot-data {:init-state {:event-chan event-chan}})]
        (if (:error xyplot-data)
          [:div.row.row-centered
           [:h4 "There was an error displaying this chart."]]
          [:div
           {:style {:width "50%" :margin "0 auto"}}
           (om/build (chart {:data-chan data-chan
                             :x-label (:label-x xyplot-data)
                             :y-label (:label-y xyplot-data)
                             }) xyplot-data)])]))))
