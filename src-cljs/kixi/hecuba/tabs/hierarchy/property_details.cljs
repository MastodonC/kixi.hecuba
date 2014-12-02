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
            [kixi.hecuba.history :as history]
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
            [kixi.hecuba.widgets.measurementsupload :as measurementsupload]
            [kixi.hecuba.tabs.hierarchy.tech-icons :as icons]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Property Details Helpers

(defn error-handler [properties]
  (fn [{:keys [status status-text]}]
    (om/update! properties :alert {:status true
                                   :class "alert alert-danger"
                                   :text "Failed to delete property."})))

(defn put-resource [refresh-chan event-chan property_id property]
  (PUT  (str "/4/entities/" property_id)
        {:headers {"Content-Type" "application/edn"}
         :handler (fn [_]
                    (put! event-chan {:event :edit :value false})
                    (put! refresh-chan {:event :property}))
         :error-handler (fn [{:keys [status status-text]}]
                          (put! event-chan {:event :error :value status-text}))
         :params property}))

(defn save-form [refresh-chan event-chan entity property_id]
  (put-resource refresh-chan event-chan property_id entity))

(defn delete-property [properties entity_id history refresh-chan]
  (common/delete-resource (str "/4/entities/" entity_id)
                          (fn []
                            (put! refresh-chan {:event :properties})
                            (history/update-token-ids! history :properties nil)
                            (om/update! properties :editing false))
                          (error-handler properties)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; property-details-form

(defn parse-label [label]
  (zipmap [:name :device_id :type] (clojure.string/split (name label) #":")))

(defn extract-calcs [property_details]
  (mapv (fn [[k v]]
          (let [{:keys [name device_id type]} (parse-label k)
                device-description (->> property_details
                                        :devices
                                        (filter #(= (:device_id %) device_id))
                                        first
                                        :description)]
            {:label device-description
             :name name
             :type type
             :last-calc (if-let [timestamp (get (:calculated_fields_last_calc property_details) k)]
                          (common/unparse-date timestamp "yyyy-MM-dd")
                          "No date.")
             :value v}))
        (:calculated_fields_values property_details)))

(defn calculation-type [type]
  (case type
    "actual_annual_12months" "Last 12 Months"
    "actual_annual_1month" "Last Month"
    "Type Missing"))

(defn fix-sensor-type [sensor-type]
  (some-> sensor-type
          (clojure.string/replace "__SPACE__" " ")
          (clojure.string/replace "__AMPERSAND__" "&")
          (clojure.string/replace "__SLASH__" "/")))

;; Change property_data to calcs and remove calcs from the let and all will be fine.
(defn summary-stats [property_data owner]
  (om/component
   (html
    [:div.col-md-12
     [:h3 "Summary Statistics"]
     (let [rows (extract-calcs property_data)]
       [:table.table.table-hover.table-condensed
        [:thead [:tr [:th "Device"] [:th "Sensor"] [:th "Calculation Type"] [:th "Last Calculated"] [:th "Value"]]]
        [:tbody
         (for [r (sort-by (juxt :label :type :name) rows)]
           (let [_ (log "Row: " r)]
             [:tr [:td (:label r)] [:td (fix-sensor-type (:type r))] [:td (calculation-type (:name r))] [:td (:last-calc r)] [:td.number (:value r)]]))]])])))

(defn property-details-form [property owner {:keys [event-chan]}]
  (reify
    om/IRenderState
    (render-state [_ state]
      (let [history                 (om/get-shared owner :history)
            refresh-chan            (om/get-shared owner :refresh)
            selected-property-id    (:entity_id (:property property))
            {:keys [project_id
                    property_data
                    editable]} property]
        (html
         [:div

          [:form.form-horizontal {:role "form"}
           [:h3 "Overview"]
           [:div.row
            [:div.col-md-6
             [:div.btn-toolbar
              [:button.btn.btn-default
               {:type "button"
                :class (str "btn btn-success")
                :onClick (fn [_ _]
                           (let [edited-data            (om/get-state owner :property)
                                 existing-property      (:property @property)
                                 existing-property-data (:property_data existing-property)
                                 new-propery-data       (:property_data edited-data)
                                 merged-property-data   (common/deep-merge existing-property-data
                                                                           new-propery-data)
                                 entity                 (-> edited-data
                                                            (cond-> (seq new-propery-data) (assoc-in [:property_data]
                                                                                                     merged-property-data)))]
                             (save-form refresh-chan event-chan entity selected-property-id)))} "Save"]
              [:button.btn.btn-default {:type "button"
                                        :class (str "btn btn-danger")
                                        :onClick (fn [_ _] (put! event-chan {:event :edit :value false}))} "Cancel"]]]
            [:button {:type "button"
                      :class (str "btn btn-danger pull-right")
                      :onClick (fn [_]
                                 (put! event-chan {:event :delete :value selected-property-id}))}
             "Delete Property"]]
           [:div {:id "overview-alert"}]
           [:div.col-md-6
            (bs/text-input-control property owner [:property :property_code] "Property Code")
            (bs/address-control property owner [:property :property_data])
            (bs/text-input-control property owner [:property :property_data :property_type] "Property Type")
            (bs/text-input-control property owner [:property :property_data :built_form] "Built Form")
            (bs/text-input-control property owner [:property :property_data :age] "Age")
            (bs/text-input-control property owner [:property :property_data :ownership] "Ownership")
            (bs/text-input-control property owner [:property :property_data :project_phase] "Project Phase")
            (bs/text-input-control property owner [:property :property_data :monitoring_hierarchy] "Monitoring Hierarchy")
            (bs/text-input-control property owner [:property :property_data :practical_completion_date] "Practical Completion Date")
            (bs/text-input-control property owner [:property :property_data :construction_date] "Construction Date")
            (bs/text-input-control property owner [:property :property_data :conservation_area] "Conservation Area")
            (bs/text-input-control property owner [:property :property_data :listed] "Listed Building")
            (bs/text-input-control property owner [:property :property_data :terrain] "Terrain")
            (bs/text-input-control property owner [:property :property_data :degree_day_region] "Degree Day Region")
            (bs/text-area-control property owner [:property :property_data :description] "Description")
            (bs/text-area-control property owner [:property :property_data :project_summary] "Project Summary")
            (bs/text-area-control property owner [:property :property_data :project_team] "Project Team")
            (bs/text-area-control property owner [:property :property_data :design_strategy] "Design Strategy")
            (bs/text-area-control property owner [:property :property_data :energy_strategy] "Energy Strategy")
            (bs/text-area-control property owner [:property :property_data :monitoring_policy] "Monitoring Policy")
            (bs/text-area-control property owner [:property :property_data :other_notes] "Other Notes")]]])))))

(defn property-details-display [property owner {:keys [event-chan]}]
  (reify
    om/IInitState
    (init-state [_]
      {:editing false})
    om/IRenderState
    (render-state [_ state]
      (let [history                 (om/get-shared owner :history)
            refresh-chan            (om/get-shared owner :refresh)
            selected-property-id    (:entity_id property)
            {:keys [project_id
                    property_data
                    editable]} (:property property)]
        (html
         [:div
          [:form.form-horizontal {:role "form"}
           [:h3 "Overview"
            [:div.col-md-12
             [:div.form-group
              (when editable
                [:div.pull-right
                 [:div.btn-toolbar
                  [:button.btn.btn-default.fa.fa-pencil-square-o
                   {:type "button"
                    :title "Edit"
                    :class (str "btn btn-primary")
                    :onClick (fn [_ _]
                               (put! event-chan {:event :edit :value true}))}]
                  [:a {:class (str "btn btn-primary fa fa-download")
                       :title "Download"
                       :role "button"
                       :href (str "/4/entities/" selected-property-id "?type=csv")}]]])]]]
           [:div.col-md-6
            (bs/static-text property [:property :property_code] "Property Code")
            (bs/address-static-text property_data)
            (bs/static-text property_data [:property_type] "Property Type")
            (bs/static-text property_data [:built_form] "Built Form")
            (bs/static-text property_data [:age] "Age")
            (bs/static-text property_data [:ownership] "Ownership")
            (bs/static-text property_data [:project_phase] "Project Phase")
            (bs/static-text property_data [:monitoring_hierarchy] "Monitoring Hierarchy")
            (bs/static-text property_data [:practical_completion_date] "Practical Completion Date")
            (bs/static-text property_data [:construction_date] "Construction Date")
            (bs/static-text property_data [:conservation_area] "Conservation Area")
            (bs/static-text property_data [:listed] "Listed Building")
            (bs/static-text property_data [:terrain] "Terrain")
            (bs/static-text property_data [:degree_day_region] "Degree Day Region")
            (bs/static-text property_data [:description] "Description")
            (bs/static-text property_data [:project_summary] "Project Summary")
            (bs/static-text property_data [:project_team] "Project Team")
            (bs/static-text property_data [:design_strategy] "Design Strategy")
            (bs/static-text property_data [:energy_strategy] "Energy Strategy")
            (bs/static-text property_data [:monitoring_policy] "Monitoring Policy")
            (bs/static-text property_data [:other_notes] "Other Notes")]
           [:div.col-md-6
            (when (seq (:calculated_fields_values (:property property)))
              (om/build summary-stats
                        (select-keys property
                                     [:calculated_fields_values :calculated_fields_labels :calculated_fields_last_calc :devices])))
            (when-let [tech-icons (seq (:technology_icons property_data))]
              [:div.col-md-12
               [:h3 "Technologies"]
               [:span.tech-icon-container-md
                (for [ti tech-icons]
                  (icons/tech-icon ti))]])
            (when-let [photos (seq (:photos (:property property)))]
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
      {:datetimepicker-chan (chan)
       :event-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (let [history      (om/get-shared owner :history)
            refresh-chan (om/get-shared owner :refresh)]

        ;; Chart data button
        (go-loop []
          (let [datetimepicker-chan (om/get-state owner :datetimepicker-chan)
                {:keys [start-date end-date] :as range} (<! datetimepicker-chan)
                sensors   (-> @properties :sensors :selected)
                entity_id (-> @properties :selected)]
            (log "Fetching for sensors: " sensors "start: " start-date "end: " end-date "id: " entity_id)
            (when (and sensors end-date start-date)
              (om/update! properties [:chart :range] range)
              (data/fetch-measurements properties entity_id sensors start-date end-date)))
          (recur))

        ;;  Event chan
        (go-loop []
          (let [event-chan (om/get-state owner :event-chan)
                {:keys [event value]} (<! event-chan)]
            (case event
              :delete (delete-property properties value history refresh-chan)
              :edit   (om/update! properties :editing value)
              :error  (om/update! properties :alert {:status true
                                                     :class "alert alert-danger"
                                                     :text value})))
          (recur))))
    om/IDidUpdate
    (did-update [_ prev-props _]
      (when (and (or (not= (:selected prev-props) (:selected properties))
                     (not= (:data prev-props) (:data properties)))
                 (-> properties :data seq)
                 (-> properties :selected seq))
        (common/fixed-scroll-to-element "property-details")))
    om/IRenderState
    (render-state [_ state]
      (let [active-tab                 (:active-tab properties)
            programme-id               (-> properties :programme_id)
            project-id                 (-> properties :project_id)
            property-id                (-> properties :selected)
            property-details           (-> properties :selected-property :property)
            {:keys [editable devices]} property-details
            property_data              (:property_data property-details)
            event-chan                 (om/get-state owner :event-chan)
            datetimepicker-chan        (om/get-state owner :datetimepicker-chan)
            refresh-chan               (om/get-shared owner :refresh)]
        (html
         [:div {:id "property-details-div"}
          (when (seq property-id)
            [:div#property-details.col-md-12
             [:h2 "Property Details" [:br] [:small (:slug property-details)]]
             [:div ;; Tab Container
              [:ul.nav.nav-tabs {:role "tablist"}
               [:li {:class (if (= active-tab :overview) "active" nil)}
                [:a {:onClick (fn [_ _] (om/update! properties :active-tab :overview))}
                 "Overview"]]
               [:li {:class (if (= active-tab :profiles) "active" nil)}
                [:a {:onClick (fn [_ _] (om/update! properties :active-tab :profiles))}
                 "Profiles"]]
               [:li {:class (if (= active-tab :devices) "active" nil)}
                [:a {:onClick (fn [_ _] (om/update! properties :active-tab :devices))}
                 "Devices"]]
               [:li {:class (if (= active-tab :datasets) "active" nil)}
                [:a {:onClick (fn [_ _]
                                (om/update! properties [:datasets :sensors] (data/fetch-sensors property-id @properties))
                                (om/update! properties [:datasets :editable] editable)
                                (data/fetch-datasets property-id [:datasets :datasets] properties)
                                (om/update! properties :active-tab :datasets))}
                 "Datasets"]]
               [:li {:class (if (= active-tab :sensors) "active" nil)}
                [:a {:onClick (fn [_ _] (om/update! properties :active-tab :sensors))}
                 "Sensor Charts"]]
               [:li {:class (if (= active-tab :raw-data) "active" nil)}
                [:a {:onClick (fn [_ _]
                                (om/update! properties [:raw-data :sensors] (into [] (data/fetch-sensors property-id @properties)))
                                (om/update! properties :active-tab :raw-data))}
                 "Raw Sensor Data"]]
               (when editable
                 [:li {:class (if (= active-tab :upload) "active" nil)}
                  [:a {:onClick (fn [_ _]
                                  (let [refresh-chan (om/get-shared owner :refresh)]
                                    (put! refresh-chan {:event :upload-status})
                                    (om/update! properties :active-tab :upload)))}
                   "Uploads"]])]
              ;; Overview
              (when (= active-tab :overview)
                [:div.col-md-12
                 (om/build bs/alert (:alert properties))
                 (if (:editing properties)
                   (om/build property-details-form (:selected-property properties) {:opts {:event-chan event-chan}})
                   (om/build property-details-display (:selected-property properties) {:opts {:event-chan event-chan}}))])
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
                [:div.col-md-12 (om/build raw/raw-data-div {:entity_id (:selected properties)
                                                            :raw-data  (:raw-data properties)})])
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
                      [:h4 "Upload status" [:div.pull-right
                                            [:i {:onClick (fn [d] (put! refresh-chan {:event :upload-status}))
                                                 :class (str "fa fa-refresh"
                                                             (if (= :fetching (-> properties :uploads :fetching))
                                                               " fa-spin"
                                                               ""))}]]]
                      (om/build (status/upload-status programme-id project-id property-id) (:files (:uploads properties)))]]]]]])]])])))))
