(ns kixi.hecuba.tabs.hierarchy.properties
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [ajax.core :refer (PUT)]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.bootstrap :refer (text-input-control static-text) :as bs]
   [kixi.hecuba.common :refer (log) :as common]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; properties

(defn back-to-projects [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (history/update-token-ids! history :projects nil)
    (common/fixed-scroll-to-element "projects-div")))

(defmulti properties-table-html (fn [properties owner] (:fetching properties)))
(defmethod properties-table-html :fetching [properties owner]
  (bs/fetching-row properties))

(defmethod properties-table-html :no-data [properties owner]
  (bs/no-data-row properties))

(defmethod properties-table-html :error [properties owner]
  (bs/error-row properties))

(defmethod properties-table-html :has-data [properties owner]
  (let [table-id "properties-table"
        history  (om/get-shared owner :history)]
    [:div.col-md-12
     [:table {:className "table table-hover"}
      [:thead
       [:tr [:th "Photo"] [:th "Property Code"] [:th "Type"] [:th "Address"]
        [:th "Region"] [:th "Ownership"] [:th "Technologies"] [:th "Monitoring Hierarchy"]]]
      (for [property-details (sort-by #(-> % :property_code) (:data properties))]
        (let [property_data (:property_data property-details)
              id            (:id property-details)]
          [:tr
           {:onClick (fn [_ _]
                       (om/update! properties :selected id)
                       (history/update-token-ids! history :properties id))
            :className (if (= id (:selected properties)) "success")
            :id (str table-id "-selected")}
           [:td (when-let [pic (:path (first (:photos property-details)))]
                  [:img.img-thumbnail.tmg-responsive
                   {:src (str "https://s3-us-west-2.amazonaws.com/get-embed-data/" pic)}])]
           [:td (:property_code property-details)]
           [:td (:property_type property_data)]
           [:td (slugs/postal-address property_data)]
           [:td (:address_region property_data)]
           [:td (:ownership property_data)]
           [:td (for [ti (:technology_icons property_data)]
                  [:img.tmg-responsive {:src ti :width 40 :height 40}])]
           [:td (:monitoring_hierarchy property_data)]]))]]))

(defmethod properties-table-html :default [properties owner]
  [:div.row [:div.col-md-12]])

(defn properties-table [properties owner]
  (reify
    om/IRender
    (render [_]
      (html (properties-table-html properties owner)))))

(defn properties-div [data owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [programmes projects properties active-components]} data
            history (om/get-shared owner :history)]
        (html
         [:div.row#properties-div
          [:div {:class (str "col-md-12 " (if (:project_id properties) "" "hidden"))}
           [:h2 "Properties"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (common/title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (common/title-for projects)]]]
           (om/build properties-table properties {:opts {:histkey :properties}})]])))))
