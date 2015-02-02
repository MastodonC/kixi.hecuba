(ns kixi.hecuba.tabs.hierarchy.sensors
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put! sliding-buffer mult tap untap]]
            [om.core :as om :include-macros true]
            [goog.userAgent :as agent]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (static-text text-input-control checkbox) :as bs]
            [kixi.hecuba.history :as history]
            [kixi.hecuba.widgets.brewer :as brewer]
            [kixi.hecuba.widgets.chart :as chart]
            [kixi.hecuba.widgets.datetimepicker :as dtpicker]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [clojure.string :as string]
            [cljs-time.core :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [kixi.hecuba.common :refer (log) :as common]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; sensors

(def amon-date (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ"))

(defn form-row [sensors]
  (fn [sensor owner {:keys [table-id selected-chan]}]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [device_id type unit period resolution status synthetic
                       parent-device lower_ts upper_ts actual_annual selected]} sensor
                       {:keys [description privacy location]} parent-device
                       id (str type "~" device_id)
                       selected? (contains? (:selected sensors) id)]
           [:tr {:onClick (fn [e] (put! selected-chan {:id id
                                                       :unit unit
                                                       :upper_ts upper_ts
                                                       :lower_ts lower_ts}))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td description]
            [:td type]
            [:td unit]
            [:td period]
            [:td resolution]
            [:td device_id]
            [:td (common/location-col location)]
            [:td (if-let [t (common/unparse-date lower_ts "yyyy-MM-dd")] t "")]
            [:td (if-let [t (common/unparse-date upper_ts "yyyy-MM-dd")] t "")]
            [:td (bs/status-label status privacy actual_annual)]]))))))

(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (map #(assoc % :parent-device parent-device
                 :id (str (:type %) "~" (:device_id %))) readings)))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

(defn fetch-sensors [property selected-sensors]
  (let [editable (:editable property)
        sensors  (extract-sensors (:devices property))]
    (map #(assoc % :editable editable :selected (if (contains? selected-sensors (:id %)) true false)) sensors)))

(defn allowed-unit? [existing-units new-unit]
  (or (not (seq existing-units))
      (< (count existing-units) 2)
      (some #(= new-unit %) existing-units)))

(defn update-sensor [click chart sensors property-details history selected?]
  (let [{:keys [id unit lower_ts upper_ts]} click
        new-selected-sensors ((if selected? disj conj) (:selected @sensors) id)
        [type device_id] (string/split id #"~")]
    ;; update history
    (history/update-token-ids! history :sensors (if (seq new-selected-sensors)
                                                  (string/join ";" new-selected-sensors)
                                                  nil))
    ;; update chart default range
    (when (and lower_ts upper_ts (not selected?))
      (om/update! chart :range {:start-date (common/unparse-date lower_ts "yyyy-MM-dd")
                                :end-date (common/unparse-date upper_ts "yyyy-MM-dd")}))
    ;; update units in chart
    (om/transact! chart :units (fn [units]
                                 (if selected?
                                   (dissoc units id)
                                   (assoc units id unit))))))

(defn process-click [click property-details owner history]
  (let [{:keys [id unit]} click
        {:keys [sensors property-details chart]} property-details
        already-selected? (contains? (:selected @sensors) id)
        existing-units    (into #{} (vals (:units @chart)))]
    (if-not already-selected?
      (if (allowed-unit? existing-units unit)
        (update-sensor click chart sensors property-details history false)
        (om/set-state! owner :alert {:status true
                                     :class "alert alert-danger"
                                     :text "Please limit the number of different units to 2."}))
      (update-sensor click chart sensors property-details history true))))

(defn sensors-table [property-details owner]
  (reify
    om/IInitState
    (init-state [_]
      {:th-chan (chan)
       :selected-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [selected-chan  (om/get-state owner :selected-chan)
              history        (om/get-shared owner :history)
              click          (<! selected-chan)]
          (when click
            (process-click click property-details owner history)))
        (recur))
      (go-loop []
        (let [{:keys [th-chan]}           (om/get-state owner)
              {:keys [sensors]}           property-details
              {:keys [sort-key sort-asc]} (:sort-spec @sensors)
              th-click                    (<! th-chan)]
          (if (= th-click sort-key)
            (om/update! sensors :sort-spec {:sort-key th-click
                                            :sort-asc (not sort-asc)})
            (om/update! sensors :sort-spec {:sort-key th-click
                                            :sort-asc true})))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [sort-spec         (-> property-details :sensors :sort-spec)
            {:keys [sort-key sort-asc]} sort-spec
            selected-sensors  (-> property-details :sensors :selected)
            sensors-data      (fetch-sensors (:property-details property-details) selected-sensors)
            sensors-with-data (filter #(every? seq [(:lower_ts %) (:upper_ts %)]) sensors-data)
            history           (om/get-shared owner :history)
            th-chan           (om/get-state owner :th-chan)
            table-id          "sensors-table"]
        (html
         [:div.col-md-12 {:style {:overflow "auto"}}
          [:div {:id "sensors-table-alert"} (bs/alert owner)]
          [:table {:class "table table-hover table-condensed"}
           [:thead
            [:tr
             (bs/sorting-th sort-spec th-chan "Description" :description)
             (bs/sorting-th sort-spec th-chan "Type" :type)
             (bs/sorting-th sort-spec th-chan "Unit" :unit)
             (bs/sorting-th sort-spec th-chan "Period" :period)
             (bs/sorting-th sort-spec th-chan "Resolution" :resolution)
             (bs/sorting-th sort-spec th-chan "Device ID" :device_id)
             (bs/sorting-th sort-spec th-chan "Location" :location)
             (bs/sorting-th sort-spec th-chan "Earliest Event" :lower_ts)
             (bs/sorting-th sort-spec th-chan "Last Event" :upper_ts)
             (bs/sorting-th sort-spec th-chan "Status" :status)]]
           [:tbody
            (om/build-all (form-row (:sensors property-details)) (if sort-asc
                                                                   (sort-by sort-key sensors-with-data)
                                                                   (reverse (sort-by sort-key sensors-with-data)))
                          {:opts {:selected-chan (:selected-chan state)
                                  :table-id table-id}})]]])))))


(defn chart-summary
  "Show min, max, delta and average of chart data."
  [cursor owner {:keys [border]}]
  (reify
    om/IInitState
    (init-state [_]
      {:value {}
       :mouseover false
       :chan (chan (sliding-buffer 100))})
    om/IWillMount
    (will-mount [_]
      (tap (om/get-state owner :mult) (om/get-state owner :chan))
      (go-loop []
        (let [measurements       (-> cursor :measurements)
              event-chan         (om/get-state owner :chan)
              {:keys [event v]}  (<! event-chan)
              bisect             (-> js/d3 (.bisector (fn [d] (aget d "timestamp"))) .-right)]
          (cond
           (= event :timestamp) (let [index  (bisect measurements v 1)
                                      value  (js->clj (aget measurements index))]
                                  (om/set-state! owner :value {:value (get value "value" "N/A")
                                                               :timestamp (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm")
                                                                                      (tc/from-date v))}))
           (= event :mouseover) (om/set-state! owner :mouseover v)))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [measurements]}     cursor
            mouseover                 (:mouseover state)
            {:keys [value timestamp]} (:value state)
            [type device_id]          (-> measurements first (aget "sensor") (string/split #"~"))
            description               (-> measurements first (aget "description"))]
        (html
         [:div {:style {:font-size "80%" :padding 0}}
          (bs/panel border "panel-info"
           [:div [:p {:style {:word-wrap "break-word" :font-size "80%"}} type]
            [:p {:style {:word-wrap "break-word" :font-size "80%"}} description]]
           [:div
            (if mouseover
              [:dl
               [:dt "Timestamp:"] [:dd timestamp]
               [:dt "Value:"] [:dd value]]
              [:div.table-responsive
               (let [unit               (-> measurements first (aget "unit"))
                     series             (js->clj measurements)
                     values             (keep #(let [v (get % "value")]
                                                 (cond (nil? v) nil
                                                       (number? v) v
                                                       (re-matches #"[-+]?\d+(\.\d+)?" v) (js/parseFloat v))) series)
                     measurements-min   (apply min values)
                     measurements-max   (apply max values)
                     measurements-sum   (reduce + values)
                     measurements-count (count values)
                     measurements-mean  (if (not= 0 measurements-count) (/ measurements-sum measurements-count) "NA")]
                 [:table.table.table-hover.table-condensed {:style {:width "100%"}}
                  [:tr [:td "Minimum"] [:td.number (str (.toFixed (js/Number. measurements-min) 3))] [:td unit]]
                  [:tr [:td "Maximum"] [:td.number (str (.toFixed (js/Number. measurements-max) 3))] [:td unit]]
                  [:tr [:td "Average (Mean)"] [:td.number (str (.toFixed (js/Number. measurements-mean) 3))] [:td unit]]
                  [:tr [:td "Range"] [:td.number (str (.toFixed (js/Number. (- measurements-max measurements-min)) 3))] [:td unit]]])])] 5)])))
    om/IWillUnmount
    (will-unmount [_]
      (untap (om/get-state owner :mult) (om/get-state owner :chan)))))

(defn get-description [sensors measurement]
  (-> (filter #(= (:type measurement) (:type %)) sensors) first :parent-device :description))

(defn parse
  "Enriches measurements with unit and description of device and parses timestamp into a JavaScript Date object"
  [measurements units sensors]
  (map (fn [measurements-seq] (map #(assoc % :unit (get units (-> % :sensor))
                                           :description (get-description sensors %)
                                           :timestamp (tf/parse amon-date (:timestamp %))) measurements-seq)) measurements))

(defn rollup-indicator [cursor owner]
  (om/component
   (html
    (let [rollup-type (case cursor
                        :raw "raw data."
                        :hourly_rollups "hourly rollups."
                        :daily_rollups "daily rollups.")]
      [:h4 [:span.label.label-primary (str "Displaying " rollup-type)]]))))

(defn sensors-div [property-details owner opts]
  (reify
    om/IInitState
    (init-state [_]
      (let [hovering-chan (chan (sliding-buffer 100))
            m (mult hovering-chan)]
        {:hovering-chan hovering-chan
         :mult-chan m}))
    om/IRenderState
    (render-state [_ state]
      (let [sensors (:sensors property-details)
            {:keys [project_id entity_id]} (:property-details property-details)
            selected-sensors  (-> property-details :sensors :selected)]
        (html
         [:div.col-md-12
          [:h3 "Available sensor data"]
          [:div {:id "sensors-table"}
           (om/build sensors-table property-details)]

          ;; FIXME: We should have better handling for IE8 here.
          (if (or (not agent/IE)
                  (agent/isVersionOrHigher 9))
            [:div {:id "chart-div"}
             [:div.col-md-12
              [:div.col-md-2
               (when (-> property-details :chart :all-groups seq)
                 (when-let [rollup-type (-> property-details :chart :rollup-type)]
                   (om/build rollup-indicator rollup-type)))]
              [:div.col-md-6.col-md-offset-1
               (om/build dtpicker/datetime-picker (-> property-details :chart :range) {:opts {:div-id "chart-date-picker"}
                                                                                       :init-state {:date-range-chan
                                                                                                    (:datetimepicker-chan opts)}})]]

             (if (-> property-details :chart :fetching)
               [:div [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data."]]]
               ;; Chart and infoboxes
               (let [{:keys [all-groups units]} (get property-details :chart)
                     {:keys [hovering-chan mult-chan]} (om/get-state owner)
                     left-border-colours       (take 6 brewer/cat-12)
                     right-border-colours      (drop 6 brewer/cat-12)]
                 (if (and (some seq all-groups) (seq units))
                   (let [left-group        (first all-groups)
                         left-with-colours (zipmap left-group (cycle left-border-colours))]
                     [:div.col-md-12.last
                      [:div.col-md-2
                       (for [[series colours] left-with-colours]
                         (om/build chart-summary {:measurements (clj->js series)} {:init-state {:mult mult-chan}
                                                                                   :opts {:border colours}}))]
                      [:div.col-md-8
                       [:div#chart {:style {:width "100%" :height 600}}
                        (om/build chart/chart-figure {:measurements (mapv #(into [] (flatten %)) all-groups)}
                                  {:opts {:chan hovering-chan}})]]
                      [:div.col-md-2
                       (when (> (count all-groups) 1)
                         ;; Always max 2 groups as max 2 units
                         (let [right-group        (last all-groups)
                               right-with-colours (zipmap right-group (cycle right-border-colours))]
                           (for [[series colours] right-with-colours]
                             (om/build chart-summary {:measurements (clj->js series)} {:init-state {:mult mult-chan}
                                                                                       :opts {:border colours}}))))]])
                   [:div [:div.col-md-12.text-center.last [:p.lead {:style {:padding-top 30}} "No data."]]])))]
            [:div.col-md-12.text-center
             [:p.lead {:style {:padding-top 30}}
              "Charting in Internet Explorer version " agent/VERSION " coming soon."]])])))))
