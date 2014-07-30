(ns kixi.hecuba.tabs.hierarchy.property-details
  (:import goog.net.XhrIo)
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [goog.userAgent :as agent]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.tabs.slugs :as slugs]
            [kixi.hecuba.tabs.hierarchy.sensors :as sensors]
            [kixi.hecuba.tabs.hierarchy.profiles :as profiles]
            [kixi.hecuba.tabs.hierarchy.programmes :as programmes]
            [kixi.hecuba.tabs.hierarchy.status :as status]
            [kixi.hecuba.common :refer (log)]
            [ajax.core :refer [GET PUT]]
            [kixi.hecuba.widgets.fileupload :as file]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Property Details Helpers

;; FIXME: This is a dupe in sensors.cljs
(defn get-property-details [selected-property-id data]
  (->>  data
        :properties
        :data
        (filter #(= (:id %) selected-property-id))
        first))

(defn post-resource [data property_id property-data project_id]
  (PUT  (str "/4/entities/" property_id)
        {:headers {"Content-Type" "application/edn"}
         :handler #(programmes/fetch-properties project_id data)
         :params property-data}))

(defn save-form [data property-details owner property_id project_id]
  (let [property-data (merge (:property_data @property-details) (om/get-state owner [:property_data]))]
    (post-resource data property_id {:property_data property-data} project_id)
    (om/set-state! owner :editing false)))

(defn handle-change [owner key e]
  (let [value (.-value (.-target e))]
    (om/set-state! owner [:property_data key] value)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; property-details-form
(defn text-control [data state owner key label]
  [:div.form-group
   [:label.control-label.col-md-2 {:for (name key)} label]
   (if (:editing state)
     [:div.col-md-10
      [:input {:defaultValue (get data key "")
               :class "form-control"
               :on-change #(handle-change owner key %1)
               :type "text"
               :id (name key)}]]
     [:p {:class "form-control-static col-md-10"} (get data key "")])])

(defn text-area-control [data state owner key title]
  (let [text (get data key)]
    (if (:editing state)
      [:div
       [:div.form-group
        [:label.control-label.col-md-2  title]
        [:form.col-md-10 {:role "form"}
         [:textarea.form-control {:on-change #(handle-change owner key %1) :rows 2} text]]]]
      (if (and text (re-find #"\w" text))
        [:div [:h3 title]
         [:p text]]
        [:div {:class "hidden"} [:p {:class "form-control-static col-md-10"} text]]))))

(defn address-control [property_data owner state]
  (if (:editing state)
    [:div
     [:div.form-group
      [:label.control-label.col-md-2 {:for "address_street_two"} "Street Address"]
      [:div.col-md-10
       [:input {:defaultValue (get property_data :address_street_two "")
                :on-change #(handle-change owner :address_street_two %1)
                :class "form-control"
                :type "text"
                :id "address_street_two"}]]]
     [:div.form-group
      [:label.control-label.col-md-2 {:for "address_city"} "City"]
      [:div.col-md-10
       [:input {:defaultValue (get property_data :address_city "")
                :on-change #(handle-change owner :address_city  %1)
                :class "form-control"
                :type "text"
                :id "address_city"}]]]
     [:div.form-group
      [:label.control-label.col-md-2 {:for "address_code"} "Postal Code"]
      [:div.col-md-10
       [:input {:defaultValue (get property_data :address_code "")
                :on-change #(handle-change owner :address_code %1)
                :class "form-control"
                :type "text"
                :id "address_code"}]]]
     [:div.form-group
      [:label.control-label.col-md-2 {:for "address_country"} "Country"]
      [:div.col-md-10
       [:input {:defaultValue (get property_data :address_country "")
                :on-change #(handle-change owner :address_country %1)
                :class "form-control"
                :type "text"
                :id "address_country"}]]]]
    [:div.form-group
     [:label.control-label.col-md-2 {:for "address"} "Address"]
     [:p {:class "form-control-static col-md-10"} (slugs/postal-address-html property_data)]]))

(defn property-details-form [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ state]
      (let [selected-property-id    (-> data :active-components :properties)
            property-details        (get-property-details selected-property-id data)
            {:keys [project_id
                    property_data
                    editable]} property-details]
        (html
         [:div
          [:h3 "Overview"]
          [:form.form-horizontal {:role "form"}
           [:div.col-md-6
            (when editable
              [:div.form-group
               [:div.btn-toolbar
                [:button.btn.btn-default {:type "button"
                                          :class (str "btn btn-primary " (if (om/get-state owner :editing) "hidden" ""))
                                          :onClick (fn [_ _] (om/set-state! owner {:editing true}))} "Edit"]
                [:button.btn.btn-default {:type "button"
                                          :class (str "btn btn-success " (if (om/get-state owner :editing) "" "hidden"))
                                          :onClick (fn [_ _] (save-form data property-details
                                                                        owner selected-property-id project_id))} "Save"]
                [:button.btn.btn-default {:type "button"
                                          :class (str "btn btn-danger " (if (om/get-state owner :editing) "" "hidden"))
                                          :onClick (fn [_ _] (om/set-state! owner {:editing false}))} "Cancel"]]])
            (text-control property-details state owner :property_code "Property Code")
            (address-control property_data owner state)
            (text-control property_data state owner :property_type "Property Type")
            (text-control property_data state owner :built_form "Built Form")
            (text-control property_data state owner :age "Age")
            (text-control property_data state owner :ownership "Ownership")
            (text-control property_data state owner :project_phase "Project Phase")
            (text-control property_data state owner :monitoring_hierarchy "Monitoring Hierarchy")
            (text-control property_data state owner :practical_completion_date "Practical Completion Date")
            (text-control property_data state owner :construction_date "Construction Date")
            (text-control property_data state owner :conservation_area "Conservation Area")
            (text-control property_data state owner :listed "Listed Building")
            (text-control property_data state owner :terrain "Terrain")
            (text-control property_data state owner :degree_day_region "Degree Day Region")
            (text-area-control property_data state owner :description "Description")
            (text-area-control property_data state owner :project_summary "Project Summary")
            (text-area-control property_data state owner :project_team "Project Team")
            (text-area-control property_data state owner :design_strategy "Design Strategy")
            (text-area-control property_data state owner :energy_strategy "Energy Strategy")
            (text-area-control property_data state owner :monitoring_policy "Monitoring Policy")
            (text-area-control property_data state owner :other_notes "Other Notes")]
           [:div.col-md-2
            (when-let [pic (:path (first (:photos property-details)))]
              [:img.img-thumbnail.tmg-responsive
               {:src (str "https://s3-us-west-2.amazonaws.com/get-embed-data/" pic)}])]
           [:div.col-md-4
            (for [ti (:technology_icons property_data)]
              [:img.tmg-responsive {:src ti :width 80 :height 80}])]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; CSV measurements template (with & without data)

(defn measurements-template [entity_id]
  (fn [cursor owner]
    (om/component
      (html
       [:div {:id "measurements-template"}
        [:form.form-horizontal {:role "form" :style {:padding-left "15px"}}
         [:div.form-group
          [:div.btn-toolbar
           [:a {:class "btn btn-primary"
                :type "button"
                :href (str "/4/templates/for-entity/" entity_id "?data=false")}
            "Download"]]]]]))))

(defn alert [id class {:keys [body display]} owner]
  [:div {:id id :class class :style {:display (if display "block" "none")}}
   [:button.close {:type "button"
                   :onClick (fn [_]
                              (om/set-state! owner :status {:body "" :display false}))}
    [:span {:class "fa fa-times"}]]
   body])

(defn measurements-template-with-data [programme_id project_id entity_id]
  (fn [cursor owner]
    (reify
      om/IInitState
      (init-state [_]
        {:status {:body "" :display false}})
      om/IWillMount
      (will-mount [_]
        (when (and programme_id project_id entity_id)
          (let [url (str "/4/downloads/programme/" programme_id "/project/" project_id "/entity/" entity_id "/status")]
            (GET url {:handler #(om/update! cursor :files %)
                      :headers {"Accept" "application/edn"}}))))
      om/IRenderState
      (render-state [_ {:keys [status] :as state}]
        (html
         (let [id "download-status-alert"
               pending-file? (some #{"PENDING"} (map :status (:files cursor)))]
           [:div {:id "measurements-template-with-data"}
            [:div 
             ;; Status alert
             (alert id "alert alert-info" status owner)]
            
            ;; Allow to trigger download when there are no pending files
            (when-not pending-file?
              [:button {:type "button"
                        :class "btn btn-primary"
                        :onClick (fn [_]
                                   (GET (str "/4/templates/for-entity/" entity_id "?data=true")
                                        {:headers {"Accept" "application/edn"}
                                         :handler #(let [status (:status (:response %))]
                                                     (om/set-state! owner :status
                                                                    (case status
                                                                      202 {:body "File will be generated shortly. Please check back later for the download link"
                                                                           :display true}
                                                                      303 {:body "File is currently being generated. Please check back later for the download link."
                                                                           :display true})))}))}
               "Trigger download with data"])

            [:div {:id "download-status" :class (if-not pending-file? "hidden" "")}
             "File is being currently generated. Please check back later for the download link."]

            ;; Show link and status of generated file
            [:div {:style {:padding-top "10px"}}
             (when (seq (:files cursor))
               (om/build status/download-status (:files cursor)))]]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; property-details
(defn property-details-div [data owner]
  (reify
    om/IInitState
    (init-state [_]
      {:active-tab :overview})
    om/IRenderState
    (render-state [_ state]
      (let [active-tab        (:active-tab state)
            programme-id      (-> data :active-components :programmes)
            project-id        (-> data :active-components :projects)
            property-id       (-> data :active-components :properties)
            property-details  (get-property-details property-id data)
            {:keys [editable devices]}  property-details
            property_data        (:property_data property-details)]
        (html [:div {:class (str "col-md-12" (if property-id "" " hidden"))}
               [:h2 "Property Details"]
               (bs/panel
                (:slug property-details)
                [:div ;; Tab Container
                 [:ul.nav.nav-tabs {:role "tablist"}
                  [:li {:class (if (= active-tab :overview) "active" nil)}
                   [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :overview))}
                    "Overview"]]
                  [:li {:class (if (= active-tab :profiles) "active" nil)}
                   [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :profiles))}
                    "Profiles"]]
                  [:li {:class (if (= active-tab :sensors) "active" nil)}
                   [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :sensors))}
                    "Sensor Data"]]
                  (when editable
                    [:li {:class (if (= active-tab :upload) "active" nil)}
                     [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :upload))}
                      "CSV"]])]
                 ;; Overview
                 [:div {:class (if (not= active-tab :overview) "hidden" "col-md-12")}
                  (om/build property-details-form data)]
                 ;; Sensors
                 [:div {:class (if (not= active-tab :sensors) "hidden" nil)}
                  (om/build sensors/sensors-div data)]
                 ;; Profiles
                 [:div {:class (if (not= active-tab :profiles) "hidden" "col-md-12")}
                  (om/build profiles/profiles-div data)]
                 ;; CSV Management
                 [:div {:class (if (not= active-tab :upload) "hidden" "col-md-12")}
                  [:div.col-md-6
                   [:div {:style {:padding-top "15px"}}
                    ;; Download measurements template
                    [:div {:class (if (seq devices) "panel panel-default" "hidden")}
                     [:div.panel-body
                      [:div [:h4 "Download measurements CSV template"]]
                      (om/build (measurements-template property-id) nil)
                      (om/build (measurements-template-with-data programme-id project-id property-id) (:downloads data))]]]
                   ;; Upload measurements
                   [:div {:class (if (seq devices) "panel panel-default" "hidden")}
                    [:div.panel-body
                     [:div
                      [:h4 "Upload measurements CSV"]
                      (let [div-id "measurements-upload"]
                        (om/build (file/file-upload (str "/4/measurements/for-entity/" property-id "/")
                                                    div-id)
                                  nil {:opts {:method "POST"}}))]]]
                   ;; Upload profile data
                   [:div.panel.panel-default
                    [:div.panel-body
                     [:div
                      [:h4 "Upload CSV profile data"]
                      (let [div-id "file-form"]
                        (om/build (file/file-upload (str "/4/entities/" property-id "/profiles/")
                                                    div-id)
                                  nil {:opts {:method "POST"}}))]]]
                   ;; Upload property details
                   [:div.panel.panel-default
                    [:div.panel-body
                     [:div
                      [:h4 "Upload CSV property details"]
                      (let [div-id "property-details-form"]
                        (om/build (file/file-upload (str "/4/entities/" property-id) div-id)
                                  nil {:opts {:method "PUT"}}))]]]]
                  [:div.col-md-6
                   ;; Upload status
                   [:div {:style {:padding-top "15px"}}
                    [:div.panel.panel-default
                     [:div.panel-body
                      [:div
                       [:h4 "Upload status"]
                       (om/build (status/upload-status programme-id project-id property-id) (:uploads data))]]]]]]])])))))
