(ns kixi.hecuba.tabs.upload-status
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [ajax.core :refer (GET)]
            [kixi.hecuba.common :as common]))

(defn upload-status [programme_id project_id]
  (fn [cursor owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (let [url (str "/4/programme/" programme_id "/project/" project_id "/uploads/username")]
          (GET url {:handler #(om/update! cursor  %)
                    :headers {"Accept" "application/edn"}})))
      om/IRender
      (render [_]
        (html
         [:div
          [:table.table.table-hover.table-condensed
           [:thead
            [:tr 
             [:thead
              [:th "File"]
              [:th "ID"]
              [:th "Timestamp"]
              [:th "Status"]]
             [:tbody
              (for [item cursor]
                (let [status (:status item)]
                  [:tr
                   [:td (:filename item)]
                   [:td (:id item)]
                   [:td (common/unparse-date-str (:timestamp item) "yyyy-MM-dd HH:mm:ss")]
                   [:td (if (= status "SUCCESS")
                          [:span {:class "label label-success"} status]
                          [:span {:class "label label-danger"} status])]]))]]]]])))))
