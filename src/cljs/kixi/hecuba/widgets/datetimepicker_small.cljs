(ns kixi.hecuba.widgets.datetimepicker-small
  (:require [clojure.string :as str]
            [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [ajax.core :refer [GET]]
            [sablono.core :as html :refer-macros [html]]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.hierarchy.data :as data]
            [kixi.hecuba.model :refer (app-model)]))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Datetimepicker

(defn datetime-input-control [owner key label]
  (let [id-for (str (name key))]
    [:div
     [:label.control-label {:for id-for} label]
     [:input {:ref (name :key)
              :value (om/get-state owner key)
              :placeholder "YYYY-MM-DD"
              :on-change #(om/set-state! owner key (.. %1 -target -value))
              :class "form-control"
              :id id-for
              :type "date"}]]))

(defmulti calculate-new-date (fn [event _] event))

(defmethod calculate-new-date :day-minus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/minus d (t/days 1)))))
(defmethod calculate-new-date :day-plus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/plus d (t/days 1)))))
(defmethod calculate-new-date :week-minus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/minus d (t/weeks 1)))))
(defmethod calculate-new-date :week-plus [event current-date]
  (let [d (tf/parse current-date)]
    (tf/unparse (tf/formatter "yyyy-MM-dd") (t/plus d (t/weeks 1)))))

(defn control [cursor owner]
  (reify
    om/IInitState
    (init-state [_]
      {:date nil})
    om/IWillReceiveProps
    (will-receive-props [_ {:keys [date]}]
      (om/set-state! owner :date date))
    om/IRenderState
    (render-state [_ {:keys [date event-chan]}]
      (html
       (let [inactive? (nil? (:date cursor))
             icon-style {:margin-top "10px" }]
         [:div.row
          {:style {:margin-bottom "20px"}}
          [:div {:style {:display "inline-block" :vertical-align "middle"}}
           [:div
            {:class (str "col-md-1 fa fa-fast-backward " (if inactive? "disabled" "datemover"))
             :title "- 1 Week"
             :style icon-style
             :on-click (fn [_] (when-not inactive?
                                 (put! event-chan {:event :date-selected :value (calculate-new-date :week-minus date)})))}]
           [:div
            {:class (str "col-md-1 fa fa-step-backward " (if inactive? "disabled" "datemover"))
             :style icon-style
             :title "- 1 Day"
             :on-click (fn [_] (when-not inactive?
                                 (put! event-chan {:event :date-selected :value (calculate-new-date :day-minus date)})))}]]

          ;; input
          [:div {:style {:display "inline-block" :margin-left "-8px"}}
           (datetime-input-control owner :date nil)]

          [:div {:style {:display "inline-block" :vertical-align "middle"}}
           [:div
            {:class (str "col-md-1 fa fa-step-forward " (if inactive? "disabled" "datemover"))
             :style icon-style
             :title "+ 1 Day"
             :on-click (fn [_] (when-not inactive?
                                 (put! event-chan {:event :date-selected :value (calculate-new-date :day-plus date)})))}]
           [:div
            {:class (str "col-md-1 fa fa-fast-forward " (if inactive? "disabled" "datemover"))
             :style (merge icon-style {:margin-left "-6px"})
             :title "+ 1 Week"
             :on-click (fn [_] (when-not inactive?
                                 (put! event-chan {:event :date-selected :value (calculate-new-date :week-plus date)})))}]]

          [:div {:style {:display "inline-block"}}
           [:button.btn.btn-primary
            {:type "button"
             :id "raw-data-get-dates"
             :style {:margin-top "10px"}
             :on-click (fn [e _]
                         (if-not (nil? date)
                           (put! event-chan {:event :fetch-data :value date}))
                         (.preventDefault e))}
            "Get Data"]]]
       )))))
