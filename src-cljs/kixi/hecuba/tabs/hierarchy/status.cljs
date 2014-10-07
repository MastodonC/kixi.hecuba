(ns kixi.hecuba.tabs.hierarchy.status
  (:require [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [cljs.core.async :refer [chan put!]]
            [kixi.hecuba.common :refer (log) :as common]))

(defn upload-status [programme_id project_id entity_id]
  (fn [cursor owner]
    (reify
      om/IDisplayName
      (display-name [_]
        "Measurement Upload Status")
      om/IWillMount
      (will-mount [_]
        (let [refresh-chan (om/get-shared owner :refresh)]
          (put! refresh-chan {:event :upload-status})
          ;; (js/setInterval (fn [] (put! refresh-chan {:event :upload-status})) 60000)
          ))
      om/IRender
      (render [_]
        (html
         [:div
          [:table.table.table-hover.table-condensed
           [:thead
            [:tr
             [:th "File"]
             [:th "Timestamp"]
             [:th "Status"]]
            [:tbody
             (for [item cursor]
               (let [status (:status item)]
                 [:tr
                  [:td (:filename item)]
                  [:td (common/unparse-date-str (:timestamp item) "yyyy-MM-dd HH:mm:ss")]
                  [:td (cond (or (= status "COMPLETE") (= status "SUCCESS"))
                             [:span {:class "label label-success"} status]
                             (= status "FAILURE")
                             [:span {:class "label label-danger"} status]
                             :else
                             [:span {:class "label label-info"} status])]]))]]]])))))

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
