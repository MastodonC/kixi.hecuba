(ns kixi.hecuba.tabs.hierarchy.properties
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put!]]
   [clojure.string :as str]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-properties)]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.bootstrap  :as bs]
   [kixi.hecuba.common :refer (log) :as common]
   [sablono.core :as html :refer-macros [html]]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; properties

(defn error-handler [owner]
  (fn [{:keys [status status-text]}]
    (om/set-state! owner :error true)
    (om/set-state! owner :http-error-response {:status status
                                               :status-text status-text})))
(defn valid-property? [property]
  (not (nil? (:property_code property)))) ;; project_id comes from the selection above

(defn post-new-property [data owner property project_id]
  (common/post-resource data  "/4/entities/"
                        property
                        (fn [_]
                          (fetch-properties project_id data)
                          (om/update! data [:properties :adding-property] false))
                        (error-handler owner)))

(defn property-add-form [data project_id]
  (fn [cursor owner]
    (om/component
     (let [{:keys [status-text]} (om/get-state owner :http-error-response)
            error      (om/get-state owner :error)
            alert-body (if status-text
                         (str " Server returned status: " status-text)
                         " Please enter property code.")]
       (html
        [:div
         [:h3 "Add new property"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button {:class "btn btn-success"
                       :type "button"
                       :onClick (fn [_] (let [property      (om/get-state owner :property)
                                              property_data (om/get-state owner :property_data)]
                                          (if (valid-property? property)
                                            (post-new-property data owner (assoc property
                                                                            :property_data property_data
                                                                            :project_id project_id)
                                                               project_id)
                                            (om/set-state! owner [:error] true))))}
              "Save"]
             [:button {:type "button"
                       :class "btn btn-danger"
                       :onClick (fn [_]
                                  (om/update! data [:properties :adding-property] false))}
              "Cancel"]]]
           (bs/alert "alert alert-danger "
                  [:div [:div {:class "fa fa-exclamation-triangle"} alert-body]]
                  error
                  (str "add-property-form-failure"))
           (bs/text-input-control cursor owner :property :property_code "Property Code" true)
           (bs/address-control cursor owner :property_data)
           (bs/text-input-control cursor owner :property_data :property_type "Property Type")
           (bs/text-input-control cursor owner :property_data :built_form "Built Form")
           (bs/text-input-control cursor owner :property_data :age "Age")
           (bs/text-input-control cursor owner :property_data :ownership "Ownership")
           (bs/text-input-control cursor owner :property_data :project_phase "Project Phase")
           (bs/text-input-control cursor owner :property_data :monitoring_hierarchy "Monitoring Hierarchy")
           (bs/text-input-control cursor owner :property_data :practical_completion_date "Practical Completion Date")
           (bs/text-input-control cursor owner :property_data :construction_date "Construction Date")
           (bs/text-input-control cursor owner :property_data :conservation_area "Conservation Area")
           (bs/text-input-control cursor owner :property_data :listed "Listed Building")
           (bs/text-input-control cursor owner :property_data :terrain "Terrain")
           (bs/text-input-control cursor owner :property_data :degree_day_region "Degree Day Region")
           (bs/text-area-control cursor owner :property_data :description "Description")
           (bs/text-area-control cursor owner :property_data :project_summary "Project Summary")
           (bs/text-area-control cursor owner :property_data :project_team "Project Team")
           (bs/text-area-control cursor owner :property_data :design_strategy "Design Strategy")
           (bs/text-area-control cursor owner :property_data :energy_strategy "Energy Strategy")
           (bs/text-area-control cursor owner :property_data :monitoring_policy "Monitoring Policy")
           (bs/text-area-control cursor owner :property_data :other_notes "Other Notes")]]])))))

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
      [:tbody
       (for [property-details (sort-by #(-> % :property_code) (:data properties))]
         (let [property_data (:property_data property-details)
               id            (:id property-details)]
           [:tr
            {:onClick (fn [_ _]
                        (log "selected property: " id)
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
            [:td (:monitoring_hierarchy property_data)]]))]]]))

(defmethod properties-table-html :default [properties owner]
  [:div.row [:div.col-md-12]])

(defn properties-table [properties owner]
  (reify
    om/IRender
    (render [_]
      (html (properties-table-html properties owner)))))

(defn properties-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:error nil
       :http-error-response nil})
    om/IRenderState
    (render-state [_ state]
      (let [{:keys [programmes projects properties active-components]} data
            history         (om/get-shared owner :history)
            project_id      (-> data :active-components :projects)
            project         (-> (filter #(= (:id %) project_id) (-> projects :data)) first)
            adding-property (-> properties :adding-property)]
        (html
         [:div.row#properties-div
          [:div {:class (str "col-md-12 " (if project_id "" "hidden"))}
           [:h2 "Properties"]
           [:ul {:class "breadcrumb"}
            [:li [:a
                  {:href "/app"}
                  (common/title-for programmes)]]
            [:li [:a
                  {:onClick (back-to-projects history)}
                  (common/title-for projects)]]]
           (when (and (not adding-property)
                      (:editable project))
             [:div.form-group
              [:div.btn-toolbar
               [:button {:type "button"
                         :class "btn btn-primary"
                         :onClick (fn [_]
                                    (om/update! data [:properties :adding-property] true))}
                "Add new"]]])
           [:div {:id "property-add-div" :class (if adding-property "" "hidden")}
            (om/build (property-add-form data project_id) nil)]
           [:div {:id "property-div" :class (if adding-property "hidden" "")}
            (om/build properties-table properties)]]])))))
