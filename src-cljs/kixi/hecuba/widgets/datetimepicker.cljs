(ns kixi.hecuba.widgets.datetimepicker
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require        [om.core :as om :include-macros true]
                   [om.dom :as dom :include-macros true]
                   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
                   [cljs-time.core :as t]
                   [cljs-time.format :as tf]))

;; TODO datepicker's min and max functions don't work as expected.
;; Might be an issue with using jQuery for reading selected dates.
;; For now added the check below. But need to disable invalid dates
;; in the datepicker instead.
(defn evaluate-dates
  [start-date end-date]
  (prn start-date end-date)
  (let [formatter (tf/formatter "yyyy-MM-dd-HH:mm:ssZZ")
        start     (tf/parse formatter start-date)
        end       (tf/parse formatter end-date)]
    (cond
     (t/after? start end) :invalid
     (= start-date end-date) :invalid
     (not= start-date end-date) :valid)))


(defn date-picker
  [cursor owner {:keys [histkey]}]
  (reify
    om/IRender
    (render [_]
      (let [history (om/get-shared owner :history)]
        (dom/div nil
                 (dom/div #js {:className "container"}
                          (dom/div #js {:className "col-sm-3"}
                                   (dom/div #js {:className "form-group"}
                                            (dom/div #js {:className "input-group date" :id "dateFrom" }
                                                     (dom/input #js
                                                                {:type "text"
                                                                 :ref "dateFrom"
                                                                 :data-format "YYYY-MM-DDTHH:mm:ssZ"
                                                                 :className "form-control"
                                                                 :placeholder "Start date"
                                                                 :value (if (empty? (get-in cursor [:chart :range]))
                                                                          ""
                                                                          (get-in cursor [:chart :range :start-date]))})
                                                     (dom/span #js {:className "input-group-addon"}
                                                               (dom/span #js {:className "glyphicon glyphicon-calendar"})))))
                          (dom/div #js {:className "col-sm-3"}
                                   (dom/div #js {:className "form-group"}
                                            (dom/div #js {:className "input-group date" :id "dateTo"}
                                                     (dom/input #js
                                                                {:type "text"
                                                                 :data-format "YYYY-MM-DDTHH:mm:ssZ"
                                                                 :ref "dateTo"
                                                                 :className "form-control"
                                                                 :placeholder "End date"
                                                                 :value (if (empty? (get-in cursor [:chart :range]))
                                                                          ""
                                                                          (get-in cursor [:chart :range :end-date]))})
                                                     (dom/span #js {:className "input-group-addon"}
                                                               (dom/span #js {:className "glyphicon glyphicon-calendar"})))))
                          (dom/button #js {:type "button"
                                           :id "select-dates-btn"
                                           :className  "btn btn-primary btn-large"
                                           :onClick
                                           (fn [e]
                                             (let [start     (-> (om/get-node owner "dateFrom")
                                                                 .-value)
                                                   end       (-> (om/get-node owner "dateTo")
                                                                 .-value)]
                                               (if (= :valid (evaluate-dates start end)
                                                      )
                                                 (do
                                                   (history/set-token-search! history [start end])
                                                   (om/update! cursor [:chart :range] {:start-date start :end-date end})
                                                   (om/update! cursor [:chart :message] ""))
                                                 (do
                                                   (om/update! cursor [:chart :range] {:start-date start :end-date end})
                                                   (om/update! cursor [:chart :message] "End date must be later than start date."))
                                                 )))}
                                      "Select dates")))))))
