(ns kixi.hecuba.tabs.hierarchy.projects
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [ajax.core :refer (PUT)]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.common :refer (text-input-control static-text log) :as common]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; projects

(defmulti projects-table-html (fn [projects owner] (:fetching projects)))
(defmethod projects-table-html :fetching [projects owner]
  (common/fetching-row projects))

(defmethod projects-table-html :no-data [projects owner]
  (common/no-data-row projects))

(defmethod projects-table-html :error [projects owner]
  (common/error-row projects))

(defmethod projects-table-html :has-data [projects owner]
  (let [table-id   "projects-table"
        history    (om/get-shared owner :history)]
    [:div.row
     [:div.col-md-12
      [:table {:className "table table-hover"}
       [:thead
        [:tr [:th "Name"] [:th "Type"] [:th "Description"] [:th "Created At"] [:th "Organisation"] [:th "Project Code"]]]
       [:tbody
        (for [row (sort-by :id (:data projects))]
          (let [{:keys [id name type_of description created_at organisation project_code]} row]
            [:tr {:onClick (fn [_ _]
                             (om/update! projects :selected id)
                             (history/update-token-ids! history :projects id)
                             (common/fixed-scroll-to-element "properties-div"))
                  :className (if (= id (:selected projects)) "success")
                  :id (str table-id "-selected")}
             [:td name]
             [:td type_of]
             [:td description]
             [:td created_at]
             [:td organisation]
             [:td project_code]]))]]]]))

(defmethod projects-table-html :default [projects owner]
  [:div.row [:div.col-md-12]])

(defn projects-table [projects owner]
  (reify
    om/IRender
    (render [_]
      (html (projects-table-html projects owner)))))

(defn projects-div [data owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div.row#projects-div
          [:div {:class (str "col-md-12 " (if (:programme_id projects) "" "hidden"))}
           [:h2 "Projects"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (common/title-for programmes)]]]
           (om/build projects-table projects {:opts {:histkey :projects}})]])))))
