(ns kixi.hecuba.tabs.hierarchy.property-details
  (:import goog.net.XhrIo)
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [cljs.reader :as reader]
            [om.core :as om :include-macros true]
            [goog.userAgent :as agent]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.tabs.slugs :as slugs]
            [kixi.hecuba.tabs.hierarchy.sensors :as sensors]
            [kixi.hecuba.tabs.hierarchy.devices :as devices]
            [kixi.hecuba.tabs.hierarchy.datasets :as datasets]
            [kixi.hecuba.tabs.hierarchy.raw-data :as raw]
            [kixi.hecuba.tabs.hierarchy.profiles :as profiles]
            [kixi.hecuba.tabs.hierarchy.status :as status]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.hierarchy.data :refer (fetch-properties) :as data]
            [ajax.core :refer [GET PUT]]
            [kixi.hecuba.widgets.fileupload :as file]
            [kixi.hecuba.widgets.measurementsupload :as measurementsupload]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Property Details Helpers

;; FIXME: This is a dupe in sensors.cljs
(defn get-property-details [selected-property-id properties]
  (->>  properties
        :data
        (filter #(= (:entity_id %) selected-property-id))
        first))

(defn post-resource [refresh-chan property_id property-data project_id]
  (PUT  (str "/4/entities/" property_id)
        {:headers {"Content-Type" "application/edn"}
         :handler #(put! refresh-chan {:event :property})
         :params property-data}))

(defn save-form [refresh-chan property-details owner property_id project_id]
  (let [property-data (common/deep-merge (:property_data @property-details) (om/get-state owner [:property_data]))]
    (post-resource refresh-chan property_id {:entity_id property_id :property_data property-data} project_id)
    (om/set-state! owner :editing false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; property-details-form

(defn text-control [data state owner key label]
  (if (:editing state)
    (bs/text-input-control data owner :property_data key label)
    [:div.form-group
     [:label.control-label.col-md-2 {:for (name key)} label]
     [:p {:class "form-control-static col-md-10"} (get data key "")]]))

(defn text-area-control [data state owner key label]
  (let [text (get data key "")]
    (if (:editing state)
      (bs/text-area-control data owner :property_data key label)
      (if (and text (re-find #"\w" text))
        [:div [:h3 label]
         [:p text]]
        [:div {:class "hidden"} [:p {:class "form-control-static col-md-10"} text]]))))

(defn address-static-text [property_data]
  [:div.form-group
     [:label.control-label.col-md-2 {:for "address"} "Address"]
     [:p {:class "form-control-static col-md-10"} (slugs/postal-address-html property_data)]])

(defn address-control [property_data owner state]
  (if (:editing state)
    (bs/address-control property_data owner :property_data)
    [:div.form-group
     [:label.control-label.col-md-2 {:for "address"} "Address"]
     [:p {:class "form-control-static col-md-10"} (slugs/postal-address-html property_data)]]))

(defn parse-label [label]
  (zipmap [:name :device_id :type] (clojure.string/split (name label) #":")))

(defn extract-calcs [property_details]
  (into [] (map (fn [[k v]] (merge (parse-label k)
                                   {:label (get (:calculated_fields_labels property_details) k "No Label")
                                    :last-calc (if-let [timestamp (get (:calculated_fields_last_calc property_details) k)]
                                                 (common/unparse-date timestamp "yyyy-MM-dd")
                                                 "No date.")
                                    :value v}))
                (:calculated_fields_values property_details))))

;; Change property_data to calcs and remove calcs from the let and all will be fine.
(defn summary-stats [property_data owner]
  (om/component
   (html
    [:div.col-md-12
     [:h3 "Summary Statistics"]
     (let [rows (extract-calcs property_data)]
       [:table.table.table-hover.table-condensed
        [:thead [:tr [:th "Name"] [:th "Calculation Type"] [:th "Device Type"] [:th "Device ID"] [:th "Last Calculated"] [:th "Value"]]]
        [:tbody
         (for [r rows]
           [:tr [:td (:label r)] [:td (:name r)] [:td (:type r)] [:td (:device_id r)] [:td (:last-calc r)] [:td.number (:value r)]])]])])))


(defn property-details-form [properties owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ state]
      (let [selected-property-id    (-> properties :selected)
            property-details        (get-property-details selected-property-id properties)
            refresh-chan            (om/get-shared owner :refresh)
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
                                          :onClick (fn [_ _] (save-form refresh-chan property-details
                                                                        owner selected-property-id project_id))} "Save"]
                [:button.btn.btn-default {:type "button"
                                          :class (str "btn btn-danger " (if (om/get-state owner :editing) "" "hidden"))
                                          :onClick (fn [_ _] (om/set-state! owner {:editing false}))} "Cancel"]
                [:a {:class "btn btn-primary" :role "button"
                     :href (str "/4/entities/" selected-property-id "?type=csv")} "Download"]]])
            (bs/static-text property-details :property_code "Property Code")
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
           [:div.col-md-6
            (when (seq (:calculated_fields_values property-details))
              (om/build summary-stats
                        (select-keys property-details
                                     [:calculated_fields_values :calculated_fields_labels :calculated_fields_last_calc])))
            (when-let [tech-icons (seq (:technology_icons property_data))]
              [:div.col-md-12
               [:h3 "Technologies"]
               [:span.tech-icon-container-md
                (for [ti tech-icons]
                  [:img {:src ti}])]])
            (when-let [photos (seq (:photos property-details))]
              [:div.col-md-12
               [:h3 "Photos"]
               [:p
                (for [photo photos]
                  [:img.img-thumbnail {:src (:uri photo)}])]])]]])))))

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
(defn property-details-div [properties owner]
  (reify
    om/IInitState
    (init-state [_]
      {:active-tab :overview
       :datetimepicker-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [{:keys [datetimepicker-chan]} (om/get-state owner)]
        ;; Chart data button
        (go-loop []
          (let [chart-data                    (<! datetimepicker-chan)
                sensors                       (-> @properties :sensors :selected)
                {:keys [start-date end-date]} (-> @properties :chart :range)
                entity_id                     (-> @properties :selected)]
            (log "Fetching for sensors: " sensors "start: " start-date "end: " end-date "id: " entity_id)
            (when (and sensors end-date start-date)
              (data/fetch-measurements properties entity_id sensors start-date end-date)))
          (recur))))
    om/IRenderState
    (render-state [_ state]
       (let [active-tab                 (:active-tab state)
             programme-id               (-> properties :programme_id)
             project-id                 (-> properties :project_id)
             property-id                (-> properties :selected)
             property-details           (get-property-details property-id properties)
             {:keys [editable devices]} property-details
             property_data              (:property_data property-details)
             datetimepicker-chan        (om/get-state owner :datetimepicker-chan)]
        (html
         [:div {:id "property-details-div"}
          (when (seq property-id)
            [:div#property-details.col-md-12
             [:h2 "Property Details" [:br] [:small (:slug property-details)]]
             [:div ;; Tab Container
              [:ul.nav.nav-tabs {:role "tablist"}
               [:li {:class (if (= active-tab :overview) "active" nil)}
                [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :overview))}
                 "Overview"]]
               [:li {:class (if (= active-tab :profiles) "active" nil)}
                [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :profiles))}
                 "Profiles"]]
               [:li {:class (if (= active-tab :devices) "active" nil)}
                [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :devices))}
                 "Devices"]]
               [:li {:class (if (= active-tab :datasets) "active" nil)}
                [:a {:onClick (fn [_ _]
                                (om/update! properties [:datasets :sensors] (data/fetch-sensors property-id @properties))
                                (data/fetch-datasets property-id [:datasets :datasets] properties)
                                (om/set-state! owner :active-tab :datasets))}
                 "Datasets"]]
               [:li {:class (if (= active-tab :sensors) "active" nil)}
                [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :sensors))}
                 "Sensor Charts"]]
               [:li {:class (if (= active-tab :raw-data) "active" nil)}
                [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :raw-data))}
                 "Raw Sensor Data"]]
               (when editable
                 [:li {:class (if (= active-tab :upload) "active" nil)}
                  [:a {:onClick (fn [_ _] (om/set-state! owner :active-tab :upload))}
                   "Uploads"]])]
              ;; Overview
              (when (= active-tab :overview)
                [:div.col-md-12 (om/build property-details-form properties)])
              ;; Devices
              (when (= active-tab :devices)
                [:div.col-md-12 (om/build devices/devices-div properties)])
              ;; Datasets
              (when (= active-tab :datasets)
                [:div.col-md-12 (om/build datasets/datasets-div (:datasets properties))])
              ;; Sensors
              (when (= active-tab :sensors)
                [:div.col-md-12 (om/build sensors/sensors-div {:property-details property-details
                                                               :sensors (:sensors properties)
                                                               :chart (:chart properties)}
                                          {:opts {:datetimepicker-chan datetimepicker-chan}})])
              ;; Raw Data
              (when (= active-tab :raw-data)
                [:div.col-md-12 (om/build raw/raw-data-div properties)])
              ;; Profiles
              (when (= active-tab :profiles)
                [:div.col-md-12 (om/build profiles/profiles-div property-details)])
              ;; Uploads
              (when (= active-tab :upload)
                [:div.col-md-12
                 [:div.col-md-6
                  [:div {:style {:padding-top "15px"}}
                   ;; Download measurements template
                   [:div {:class (if (seq devices) "panel panel-default" "hidden")}
                    [:div.panel-body
                     [:div [:h4 "Download measurements CSV template"]]
                     (om/build (measurements-template property-id) nil)
                     (om/build (measurements-template-with-data programme-id project-id property-id) (:downloads properties))]]]
                  ;; Upload measurements
                  [:div {:class (if (seq devices) "panel panel-default" "hidden")}
                   [:div.panel-body
                    [:div
                     [:h4 "Upload measurements CSV"]
                     [:div {:id "sensors-unit-alert"}
                      (om/build bs/alert (-> properties :uploads :alert))]
                     (let [div-id "measurements-upload"]
                       (om/build (measurementsupload/measurements-upload (str "/4/measurements/for-entity/" property-id "/")
                                                                         div-id)
                                 (-> properties :uploads) {:opts {:method "POST"}}))]]]
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
                                 nil {:opts {:method "PUT"}}))]]]
                  ;; Upload image
                  [:div.panel.panel-default
                   [:div.panel-body
                    [:div
                     [:h4 "Upload an image"]
                     (let [div-id "image-upload-form"]
                       (om/build (file/file-upload (str "/4/entities/" property-id "/images/") div-id)
                                 nil {:opts {:method "POST"}}))]]]
                  ;; Upload document
                  [:div.panel.panel-default
                   [:div.panel-body
                    [:div
                     [:h4 "Upload a document"]
                     (let [div-id "document-upload-form"]
                       (om/build (file/file-upload (str "/4/entities/" property-id "/documents/") div-id
                                                   [:div {:class "checkbox"} [:label
                                                                              [:input {:type "checkbox" :name "public"}]
                                                                              "public?"]])
                                 nil {:opts {:method "POST"}}))]]]]
                 [:div.col-md-6
                  ;; Upload status
                  [:div {:style {:padding-top "15px"}}
                   [:div.panel.panel-default
                    [:div.panel-body
                     [:div
                      [:h4 "Upload status"]
                      (om/build (status/upload-status programme-id project-id property-id) (:files (:uploads properties)))]]]]]])]])])))))
