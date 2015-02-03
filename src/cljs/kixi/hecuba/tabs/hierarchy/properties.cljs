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
   [cljs.reader :as reader]
   [kixi.hecuba.tabs.hierarchy.tech-icons :as icons]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; properties

(defn error-handler [owner]
  (fn [{:keys [status status-text]}]
    (om/set-state! owner :alert {:status true
                                 :class "alert alert-danger"
                                 :text status-text})))

(defn valid-property? [property properties]
  (let [property_code (:property_code property)]
    (and (seq property_code) (empty? (filter #(= (:property_code %) property_code) properties)))))

(defn post-new-property [properties refresh-chan owner property project_id]
  (common/post-resource "/4/entities/"
                        property
                        (fn [response]
                          (let [response-edn      (js->clj response)
                                headers           (get response-edn "headers")
                                [_ _ _ entity_id _] (str/split (get headers "Location") #"/")]
                            (put! refresh-chan {:event :new-property :id entity_id}))
                          (om/update! properties :adding-property false)
                          (om/update! properties :new-property {}))
                        (error-handler owner)))

(defn property-add-form [properties refresh-chan project_id]
  (fn [cursor owner]
    (reify
      om/IInitState
      (init-state [_]
        {:property {}
         :property_data {}
         :alert {}})
      om/IRenderState
      (render-state [_ state]
        (html
         [:div
          [:div
           [:h3 "Upload Properties"]
           (let [div-id "properties-upload-form"]
             (om/build (file/file-upload (str "/4/projects/" project_id "/entities/")
                                         div-id)
                       nil {:opts {:method "POST" :refresh? true}}))]
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
                                             (om/set-state! owner :alert {:status true
                                                                          :class "alert alert-danger"
                                                                          :text "Please enter unique property code"}))))}
               "Save"]
              [:button {:type "button"
                        :class "btn btn-danger"
                        :onClick (fn [_]
                                   (om/update! properties :adding-property false))}
               "Cancel"]]]
            [:div {:id "properties-add-alert"} (bs/alert owner)]
            (bs/text-input-control owner [:property :property_code] "Property Code" true)
            (bs/address-control owner [:property_data])
            (bs/text-input-control owner [:property_data :property_type] "Property Type")
            (bs/text-input-control owner [:property_data :built_form] "Built Form")
            (bs/text-input-control owner [:property_data :age] "Age")
            (bs/text-input-control owner [:property_data :ownership] "Ownership")
            (bs/text-input-control owner [:property_data :project_phase] "Project Phase")
            (bs/text-input-control owner [:property_data :monitoring_hierarchy] "Monitoring Hierarchy")
            (bs/text-input-control owner [:property_data :practical_completion_date] "Practical Completion Date")
            (bs/text-input-control owner [:property_data :construction_date] "Construction Date")
            (bs/text-input-control owner [:property_data :conservation_area] "Conservation Area")
            (bs/text-input-control owner [:property_data :listed] "Listed Building")
            (bs/text-input-control owner [:property_data :terrain] "Terrain")
            (bs/text-input-control owner [:property_data :degree_day_region] "Degree Day Region")
            (bs/text-area-control owner [:property_data :description] "Description")
            (bs/text-area-control owner [:property_data :project_summary] "Project Summary")
            (bs/text-area-control owner [:property_data :project_team] "Project Team")
            (bs/text-area-control owner [:property_data :design_strategy] "Design Strategy")
            (bs/text-area-control owner [:property_data :energy_strategy] "Energy Strategy")
            (bs/text-area-control owner [:property_data :monitoring_policy] "Monitoring Policy")
            (bs/text-area-control owner [:property_data :other_notes] "Other Notes")]]])))))

(defn back-to-projects [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (history/update-token-ids! history :projects nil)
    (common/fixed-scroll-to-element "projects-div")))

(defn property-row [property owner {:keys [table-id selected-row-chan]}]
  (om/component
   (let [property_data (:property_data property)
         entity_id     (:entity_id property)
         history       (om/get-shared owner :history)]
     (html  [:tr
             {:onClick   (fn [_ _]
                           (history/update-token-ids! history :properties entity_id)
                           (put! selected-row-chan @property)
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
                                           (icons/tech-icon ti))]
             [:td (:monitoring_hierarchy property_data)]]))))

(defmulti sort-function (fn [k] k))
(defmethod sort-function :address [k]
  (comp #(slugs/postal-address %) :property_data))
(defmethod sort-function :region [k]
  (comp :address_region :property_data))
(defmethod sort-function :ownership [k]
  (comp :ownership :property_data))
(defmethod sort-function :technologies [k]
  (comp count :technology_icons :property_data))
(defmethod sort-function :hierarchy [k]
  (comp :monutoring_hierarchy :property_data))
(defmethod sort-function :default [k]
  k)

(defmulti properties-table (fn [properties owner] (:fetching properties)))

(defmethod properties-table :fetching [properties owner]
  (om/component
   (html
    (bs/fetching-row properties))))

(defmethod properties-table :no-data [properties owner]
  (om/component
   (html
    (bs/no-data-row properties))))

(defmethod properties-table :error [properties owner]
  (om/component
   (html
    (bs/error-row properties))))

(defmethod properties-table :has-data [properties owner]
  (reify
    om/IInitState
    (init-state [_]
      {:th-chan (chan)
       :selected-row-chan (chan)})
    om/IWillMount
    (will-mount [_]
      ;; Put selected property into :selected-property cursor
      (go-loop []
        (let [selected-row-chan (om/get-state owner :selected-row-chan)
              row               (<! selected-row-chan)]
          (om/update! properties [:selected-property :property] row))
        (recur))
      (go-loop []
        (let [{:keys [th-chan]}                   (om/get-state owner)
              {:keys [sort-key sort-fn sort-asc]} (:sort-spec @properties)
              th-click                            (<! th-chan)]
          (if (= th-click sort-key)
            (om/update! properties :sort-spec {:sort-key th-click
                                               :sort-fn  (sort-function th-click)
                                               :sort-asc (not sort-asc)})
            (om/update! properties :sort-spec {:sort-key th-click
                                               :sort-fn  (sort-function th-click)
                                               :sort-asc true})))
        (recur)))
    om/IRenderState
    (render-state [_ state]
      (html
       (let [table-id "properties-table"
             history  (om/get-shared owner :history)
             th-chan (:th-chan state)
             {:keys [sort-spec]} properties
             {:keys [sort-fn sort-asc]} (:sort-spec properties)]
         [:div.col-md-12
          [:table {:className "table table-hover"}
           [:thead
            [:tr
             [:th "Photo"]
             (bs/sorting-th sort-spec th-chan "Property Code" :property_code)
             (bs/sorting-th sort-spec th-chan "Type" :property_type)
             (bs/sorting-th sort-spec th-chan "Address" :address)
             (bs/sorting-th sort-spec th-chan "Region" :region)
             (bs/sorting-th sort-spec th-chan "Ownership" :ownership)
             (bs/sorting-th sort-spec th-chan "Technologies" :technologies)
             (bs/sorting-th sort-spec th-chan "Monitoring Hierarchy" :hierarchy)]]
           [:tbody
            (om/build-all property-row (if sort-asc
                                         (sort-by sort-fn (:data properties))
                                         (reverse (sort-by sort-fn (:data properties))))
                          {:opts {:table-id table-id
                                  :selected-row-chan (om/get-state owner :selected-row-chan)}})]]])))))

(defmethod properties-table :default [properties owner]
  (om/component
   (html
    [:div.row [:div.col-md-12]])))

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
            (om/build (property-add-form properties refresh-chan project_id) (:new-property properties))]
           [:div {:id "property-div" :class (if adding-property "hidden" "")}
            (om/build properties-table properties)]]])))))
