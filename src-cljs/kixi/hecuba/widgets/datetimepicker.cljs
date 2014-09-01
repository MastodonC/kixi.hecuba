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

(defn handle-change [data key e]
  (let [value (.. e -target -value)]
    (om/update! data key value)))

(defn- invalid-dates [data start end]
  (om/update! data :alert {:status true
                           :class "alert alert-danger"
                           :text "End date must be later than start date."}))

(defn- valid-dates [data history start end]
  (history/set-token-search! history [start end]))

(defn evaluate-dates
  [data history start-date end-date]
  (let [formatter (tf/formatter "yyyy-MM-dd")
        start     (tf/parse formatter start-date)
        end       (tf/parse formatter end-date)]

    (cond
     (t/after? start end)       (invalid-dates data start-date end-date)
     (= start-date end-date)    (invalid-dates data start-date end-date)
     (not= start-date end-date) (valid-dates data history start-date end-date))))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Date-time picker

(defn datetime-input-control [data key label]
  (let [id-for (str (name key))]
    [:div
     [:label.control-label {:for id-for} label]
     [:input {:ref (name :key)
              :value (get data key "")
              :placeholder "YYYY-MM-DD"
              :on-change #(handle-change data key %1)
              :class "form-control"
              :id id-for
              :type "date"}]]))

(defn datetime-picker [data owner {:keys [div-id]}]
  (reify
    om/IRenderState
    (render-state [_ state]
      (html
       (let [history (om/get-shared owner :history)]
         [:div
          (datetime-input-control data :start-date "Start Date")
          (datetime-input-control data :end-date "End Date")
          [:div.form-group
           [:button.btn.btn-primary
            {:type "button"
             :id div-id
             :on-click (fn [_ _] (let [{:keys [start-date end-date]} @data]
                                   (evaluate-dates data history start-date end-date)))}
            "Get Data"]]])))))
