(ns kixi.hecuba.tabs.hierarchy.status
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [chan put!]]
            [kixi.hecuba.common :refer (log) :as common]))

(defn panel-status [status inserted-devices devices]
  (cond
   (and (= inserted-devices devices) (or (= status "COMPLETE") (= status "SUCCESS"))) "panel-success"
        (and (< inserted-devices devices) (or (= status "COMPLETE") (= status "SUCCESS"))) "panel-warning"
        (= status "FAILURE") "panel-danger"
        :else "panel-info"))

(defn skip-reason [sensor]
  (let [metadata (:metadata sensor)
        {:keys [exists? allowed?]} metadata]
    (cond (not exists?) "No matching sensor found."
          (not allowed?) "Not allowed to upload to this sensor."
          :else (str "Exists: " exists? " Allowed: " allowed?))))

(defn upload-status [programme_id project_id entity_id]
  (fn [cursor owner]
    (reify
      om/IDisplayName
      (display-name [_]
        "Measurement Upload Status")
      om/IRender
      (render [_]
        (let [sorted-statuses (sort-by :event_time #(* -1 (compare %1 %2)) cursor)]
          (html
           [:div.col-md-12
            (for [item sorted-statuses]
              (let [status (:status item)
                    all-sensors (group-by #(-> % :metadata :inserted) (:report item))
                    inserted-sensors (get all-sensors true)
                    skipped-sensors (get all-sensors false)
                    inserted-sensor-count (count inserted-sensors)
                    total-sensor-count (count (:report item))]
                [:div.row
                 [:div {:class (str "panel " (panel-status status inserted-sensor-count total-sensor-count))}
                  [:div.panel-heading (:filename item) [:div.pull-right [:strong status]]]
                  [:div.panel-body
                   [:table.table.table-hover.table-condensed
                    [:thead
                     [:tr [:th "Uploaded by"] [:th "Timestamp"] [:th "Inserted"]]]
                    [:tbody
                     [:tr [:td (:username item)]
                      [:td (common/unparse-date (:event_time item) "yyyy-MM-dd HH:mm")]
                      [:td (str inserted-sensor-count "/" total-sensor-count)]]]]
                   (when (< inserted-sensor-count total-sensor-count)
                     [:div.col-md-12
                      (when (seq inserted-sensors)
                        [:div.row
                         [:h4 "Inserted Devices"]
                         [:table.table.table-hover.table-condensed
                          [:thead
                           [:tr [:th "Device Name"] [:th "Sensor Type"] [:th "API Sensor ID"]]]
                          [:tbody
                           (for [sensor inserted-sensors]
                             [:tr
                              [:td (:description sensor)]
                              [:td (:type sensor)]
                              [:td (str (:device_id sensor) ":" (:type sensor))]])]]])
                      (when (seq skipped-sensors)
                        [:div.row
                         [:h4 "Skipped Devices"]
                         [:table.table.table-hover.table-condensed
                          [:thead
                           [:tr [:th "Identifier"] [:th "Skip Reasons"]]]
                          [:tbody
                           (for [sensor skipped-sensors]
                             [:tr
                              [:td (or (:alias sensor) (str (:device_id sensor) ":" (:type sensor)))]
                              [:td
                               (skip-reason sensor)]])]]])])]]]))]))))))

(defn download-status [cursor owner]
  (reify
    om/IDisplayName
      (display-name [_]
        "Download Status")
    om/IRender
    (render [_]
      (html
       [:div
        [:table.table.table-hover.table-condensed
         [:thead
          [:tr
           [:th "File"]
           [:th "Requested On"]
           [:th "Status"]]]
         [:tbody
          (for [item cursor]
            (let [{:keys [filename status]} item]
              [:tr
               [:td (if (= "SUCCESS" status) [:a {:href (:link item)} filename] filename)]
               [:td (common/unparse-date-str (:timestamp item) "yyyy-MM-dd HH:mm:ss")]
               [:td [:span {:class (case status
                                     "PENDING" "label label-info"
                                     "SUCCESS" "label label-success"
                                     "FAILURE" "label label-danger")} status]]]))]]]))))
