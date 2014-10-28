(ns kixi.hecuba.widgets.datetimepicker
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require        [om.core :as om :include-macros true]
                   [om.dom :as dom :include-macros true]
                   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
                   [cljs-time.core :as t]
                   [kixi.hecuba.history :as history]
                   [cljs-time.format :as tf]
                   [kixi.hecuba.common :refer (log) :as common]
                   [ajax.core :refer [GET]]
                   [clojure.string :as str]
                   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

(defn handle-change
  ([data owner key e]
     (let [value (.. e -target -value)]
       (om/set-state! owner key value)))
  ([data key e]
     (let [value (.. e -target -value)]
       (om/update! data key value))))

(defn- invalid-dates [data start end]
  (om/update! data :alert {:status true
                           :class "alert alert-danger"
                           :text "End date must be later than start date."}))

(defn- valid-dates [data history start end date-range-chan]
  (put! date-range-chan {:start-date start :end-date end})
  (history/set-token-search! history [start end]))

(defn evaluate-dates
  [data history start-date end-date date-range-chan]
  (let [formatter (tf/formatter "yyyy-MM-dd")
        start     (tf/parse formatter start-date)
        end       (tf/parse formatter end-date)]

    (cond
     (t/after? start end)       (invalid-dates data start-date end-date)
     (= start-date end-date)    (valid-dates data history start-date end-date date-range-chan)
     (not= start-date end-date) (valid-dates data history start-date end-date date-range-chan))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Moves dates with +/- buttons

(defn date-mover [data owner {:keys [k]}]
  (reify
    om/IRenderState
    (render-state [_ {:keys [chevron-chan]}]
      (html
       [:div
        [:table.table.borderless {:style {:font-size "90%"}}
         [:tbody
          [:tr
           [:td.datemover {:on-click (fn [_] (put! chevron-chan {:event :day-plus :k k}))} "+1 Day"]
           [:td.datemover {:on-click (fn [_] (put! chevron-chan {:event :week-plus :k k}))} "+1 Week"]
           [:td.datemover {:on-click (fn [_] (put! chevron-chan {:event :month-plus :k k}))} "+1 Month"]]
          [:tr
           [:td.datemover {:on-click (fn [_] (put! chevron-chan {:event :day-minus :k k}))} "-1 Day"]
           [:td.datemover {:on-click (fn [_] (put! chevron-chan {:event :week-minus :k k}))} "-1 Week"]
           [:td.datemover {:on-click (fn [_] (put! chevron-chan {:event :month-minus :k k}))} "-1 Month"]]]]]))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Date-time picker

(defn datetime-input-control
  ([data owner key label]
     (let [id-for (str (name key))]
       [:input {:ref (name :key)
                :value (om/get-state owner key)
                :placeholder "YYYY-MM-DD"
                :on-change #(handle-change data owner  key %1)
                :class "form-control"
                :style {:margin-bottom "10px" :margin-right "5px"}
                :id id-for
                :type "date"}]))
  ([data key label]
     (let [id-for (str (name key))]
       [:input {:ref (name :key)
                :value (get data key "")
                :placeholder "YYYY-MM-DD"
                :on-change #(handle-change data key %1)
                :class "form-control"
                :style {:margin-bottom "10px" :margin-right "5px"}
                :id id-for
                :type "date"}])))

(defmulti calculate-new-range (fn [event _] event))
(defmethod calculate-new-range :day-minus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/minus d (t/days 1)))))
(defmethod calculate-new-range :day-plus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/plus d (t/days 1)))))
(defmethod calculate-new-range :week-minus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/minus d (t/weeks 1)))))
(defmethod calculate-new-range :week-plus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/plus d (t/weeks 1)))))
(defmethod calculate-new-range :month-minus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/minus d (t/months 1)))))
(defmethod calculate-new-range :month-plus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/plus d (t/months 1)))))

(defn datetime-picker [data owner {:keys [div-id]}]
  (reify
    om/IInitState
    (init-state [_]
      {:start-date (-> data :start-date)
       :end-date   (-> data :end-date)
       :chevron-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [chevron-chan]} (om/get-state owner)
              {:keys [event k]}      (<! chevron-chan)
              current-date           (k (om/get-state owner))
              date (calculate-new-range event current-date)]
          (om/set-state! owner k date))
        (recur)))
    om/IWillReceiveProps
    (will-receive-props [_ {:keys [start-date end-date]}]
      (om/set-state! owner :start-date start-date)
      (om/set-state! owner :end-date end-date))
    om/IRenderState
    (render-state [_ {:keys [date-range-chan chevron-chan]}]
      (html
       (let [history (om/get-shared owner :history)]
         [:div
          [:div.col-md-12
           [:form.form-inline {:role "form"}
            [:div.col-md-5
             (datetime-input-control data owner :start-date "Start Date")]
            [:div.col-md-5
             (datetime-input-control data owner :end-date "End Date")]
            [:div.col-md-2
             [:button.btn.btn-primary
              {:type "button"
               :id div-id
               :on-click (fn [_ _] (let [{:keys [start-date end-date]} (om/get-state owner)]
                                     (evaluate-dates data history start-date end-date date-range-chan)))}
              "Chart Data"]]]]
          [:div.col-md-12
           [:form.form-inline {:role "form"}
            [:div.col-md-5
             (om/build date-mover data {:opts {:div-id "date-mover" :k :start-date}
                                        :init-state {:chevron-chan chevron-chan}})]
            [:div.col-md-5
             (om/build date-mover data {:opts {:div-id "date-mover" :k :end-date}
                                        :init-state {:chevron-chan chevron-chan}})]]]])))))
