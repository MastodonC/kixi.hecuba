(ns kixi.hecuba.tabs.status
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [ajax.core :refer (GET)]
            [kixi.hecuba.common :as common]))

(defn upload-status [programme_id project_id entity_id]
  (fn [cursor owner]
    (reify
      om/IWillMount
      (will-mount [_]
        (when (and programme_id project_id)
          (let [url (str "/4/uploads/for-username/programme/" programme_id "/project/" project_id "/entity/" entity_id "/status")]
            (GET url {:handler #(om/update! cursor  %)
                      :headers {"Accept" "application/edn"}}))))
      om/IRender
      (render [_]
        (html
         [:div
          [:table.table.table-hover.table-condensed
           [:thead
            [:tr 
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
                         [:span {:class "label label-danger"} status])]]))]]]])))))

(defn download-status [cursor owner]
  (reify
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
