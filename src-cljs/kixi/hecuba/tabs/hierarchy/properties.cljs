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
   [kixi.hecuba.common :refer (log assoc-if) :as common]
   [kixi.hecuba.widgets.fileupload :as file]
   [sablono.core :as html :refer-macros [html]]
   [cljs.reader :as reader]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; properties

(defn error-handler [properties]
  (fn [{:keys [status status-text]}]
    (om/update! properties :alert {:status true
                                   :class "alert alert-danger"
                                   :text status-text})))
(defn valid-property? [property properties]
  (let [property_code (:property_code property)]
    (and (seq property_code) (empty? (filter #(= (:property_code %) property_code) properties)))))

(defn post-new-property [properties refresh-chan owner property project_id]
  (common/post-resource "/4/entities/"
                        property
                        (fn [response]
                          (let [[_ _ _ entity_id] (str/split (get (js->clj response) "location") #"/")]
                            (put! refresh-chan {:event :new-property :id entity_id}))
                          (om/update! properties :adding-property false))
                        (error-handler properties)))

(defn property-add-form [properties refresh-chan project_id]
  (fn [cursor owner]
    (om/component

     (html
      [:div
       [:div
        [:h3 "Upload Properties"]
        (let [div-id "properties-upload-form"]
          (om/build (file/file-upload (str "/4/projects/" project_id "/entities/")
                                      div-id)
                    nil {:opts {:method "POST"}}))]
       [:h3 "Add new property"]
       [:form.form-horizontal {:role "form"}
        [:div.col-md-6
         [:div.form-group
          [:div.btn-toolbar
           [:button {:class "btn btn-success"
                     :type "button"
                     :onClick (fn [_] (let [property      (om/get-state owner :property)
                                            property_data (om/get-state owner :property_data)]
                                        (if (valid-property? property (:data @properties))
                                          (post-new-property properties refresh-chan owner (-> (assoc property :project_id project_id)
                                                                                  (assoc-if :property_data property_data))
                                                             project_id)
                                          (om/update! properties :alert {:status true
                                                                         :class "alert alert-danger"
                                                                         :text "Please enter unique property code."}))))}
            "Save"]
           [:button {:type "button"
                     :class "btn btn-danger"
                     :onClick (fn [_]
                                (om/update! properties :adding-property false))}
            "Cancel"]]]
         (om/build bs/alert (-> properties :alert))
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
         (bs/text-area-control cursor owner :property_data :other_notes "Other Notes")]]]))))

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

(defn property-row [property owner {:keys [table-id]}]
  (om/component
   (let [property_data (:property_data property)
         entity_id     (:entity_id property)
         history       (om/get-shared owner :history)]
     (html  [:tr
             {:onClick   (fn [_ _]
                           (history/update-token-ids! history :properties entity_id)
                           (common/fixed-scroll-to-element "property-details-div"))
              :className (if (:selected property) "success")
              :id        (str table-id "-selected")}
             [:td (when-let [uri (:uri (first (:photos property)))]
                    [:img.img-thumbnail.table-image
                     {:src uri}])]
             [:td (:property_code property)]
             [:td (:property_type property_data)]
             [:td (slugs/postal-address property_data)]
             [:td (:address_region property_data)]
             [:td (:ownership property_data)]
             [:td.tech-icon-container-sm (for [ti (:technology_icons property_data)]
                    [:img {:src ti}])]
             [:td (:monitoring_hierarchy property_data)]]))))

(defmethod properties-table-html :has-data [properties owner]
  (let [table-id "properties-table"
        history  (om/get-shared owner :history)]
    [:div.col-md-12
     [:table {:className "table table-hover"}
      [:thead
       [:tr [:th "Photo"] [:th "Property Code"] [:th "Type"] [:th "Address"]
        [:th "Region"] [:th "Ownership"] [:th "Technologies"] [:th "Monitoring Hierarchy"]]]
      [:tbody
       (om/build-all property-row (sort-by #(-> % :property_code) (:data properties)) {:opts {:table-id table-id}})]]]))

(defmethod properties-table-html :default [properties owner]
  [:div.row [:div.col-md-12]])

(defn properties-table [properties owner]
  (reify
    om/IRender
    (render [_]
      (html (properties-table-html properties owner)))))

(defn properties-div [properties owner]
  (reify
    om/IInitState
    (init-state [_]
      {:error nil
       :http-error-response nil})
    om/IRenderState
    (render-state [_ state]
      (let [history            (om/get-shared owner :history)
            project_id         (-> properties :project_id)
            can-add-properties (-> properties :can-add-properties)

            adding-property    (-> properties :adding-property)
            refresh-chan       (om/get-shared owner :refresh)]
        (html
         [:div.row#properties-div
          [:div {:class (str "col-md-12 " (if project_id "" "hidden"))}
           [:h1 "Properties"
            (when (and (not adding-property)
                       can-add-properties)
              [:button.btn.btn-primary.pull-right.fa.fa-plus
               {:type "button"
                :title "Add new"
                :onClick (fn [_]
                           (om/update! properties :adding-property true))}])]
           [:div {:id "property-add-div" :class (if adding-property "" "hidden")}
            (om/build (property-add-form properties refresh-chan project_id) nil)]
           [:div {:id "property-div" :class (if adding-property "hidden" "")}
            (om/build properties-table properties)]]])))))
