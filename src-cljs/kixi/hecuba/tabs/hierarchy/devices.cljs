(ns kixi.hecuba.tabs.hierarchy.devices
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :as bs]
            [kixi.hecuba.tabs.hierarchy.data :refer (fetch-property fetch-devices fetch-sensors)]
            [clojure.string :as string]
            [kixi.hecuba.common :refer (log delete-resource) :as common]
            [kixi.hecuba.model :refer (app-model)]))

(defn current-selections []
  (om/ref-cursor (-> (om/root-cursor app-model) :active-components :ids)))

(defn devices []
  (om/ref-cursor (-> (om/root-cursor app-model) :properties :devices)))

(defn privacy-label [privacy]
  [:div
   (when (= "true" privacy) [:div {:class "fa fa-key"}])])

(defn text-input-control [cursor owner path key label & required]
  [:div.form-group {:style {:padding-left "15px"}}
   [:label {:for (name key)} label]
   [:div {:class (str (if required "required " ""))}
    [:input {:defaultValue (get cursor key "")
             :on-change #(om/set-state! owner (conj path key) (.-value (.-target %1)))
             :class "form-control"
             :type "text"}]]])

(defn static-text [data keys label]
  [:div.form-group {:style {:padding-left "15px"}}
   [:label {:for label} label]
   [:p {:class "form-control-static"} (get-in data keys "")]])

(defn checkbox [cursor owner path key label]
  [:div.checkbox {:style {:padding-left "15px"}}
   [:label
    [:input {:type "checkbox"
             :defaultChecked (get cursor key "")
             :on-change #(om/set-state! owner (conj path key) (.-checked (.-target %1)))}
     label]]])

(defn location-input [cursor owner]
  (let [location (-> cursor :device :location)]
    [:div
     [:div.form-group
      [:label.control-label {:for "name"} "Location Name"]
      [:input {:defaultValue (get location :name "")
               :on-change #(om/set-state! owner [:device :location :name] (.-value (.-target %1)))
               :class "form-control"
               :type "text"
               :id "location_name"}]]
     [:div.form-group
      [:label.control-label {:for "latitude"} "Latitude"]
      [:input {:defaultValue (get location :latitude "")
               :on-change #(om/set-state! owner [:device :location :latitude] (.-value (.-target %1)))
               :class "form-control"
               :type "number"
               :id "location_latitude"}]]
     [:div.form-group
      [:label.control-label {:for "longitude"} "Longitude"]
      [:input {:defaultValue (get location :longitude "")
               :on-change #(om/set-state! owner [:device :location :longitude] (.-value (.-target %1)))
               :class "form-control"
               :type "number"
               :id "location_longitude"}]]]))

(defn alert [class body status id owner]
  [:div {:id id :class class :style {:display (if status "block" "none")}}
   [:button.close {:type "button"
                   :onClick (fn [e] (om/set-state! owner :alert {:status false}))}
    [:span {:class "fa fa-times"}]]
   body])

(defn error-handler [event-chan]
  (fn [{:keys [status status-text]}]
    (put! event-chan {:event :alert :value {:status true
                                            :class "alert alert-danger"
                                            :text status-text}})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Adding new sensor

(defn sensor-period-dropdown [owner keys default]
  [:div.form-group
   [:label.control-label {:for "period-select"} "Period"]
   [:div.required
    [:select.form-control {:default-value default
                           :on-change #(om/set-state! owner keys (.-value (.-target %)))}
     (for [item ["Select period" "CUMULATIVE" "INSTANT" "PULSE"]]
       [:option {:value item} item])]]])

(defn put-new-sensor [event-chan refresh-chan owner sensor property_id device_id]
  (common/put-resource (str "/4/entities/" property_id "/devices/" device_id)
                       {:readings [sensor]}
                       (fn [_]
                         (put! refresh-chan {:event :property})
                         (put! event-chan {:event :adding-sensor :value false})
                         (put! event-chan {:event :alert :value {:status true
                                                                 :class "alert alert-success"
                                                                 :text "Sensor was added successfully."}}))
                       (error-handler event-chan)))

(defn valid-sensor? [sensor]
  (let [period (:period sensor)]
    (and sensor
         (seq (:type sensor))
         (and (seq period) (some #{period} ["CUMULATIVE" "INSTANT" "PULSE"])) ;; Prevents from selecting "Select period"
         (seq (:unit sensor))))) ;; device_id comes from the selection above

(defn new-sensor-form [device_id]
  (fn [cursor owner]
    (reify
      om/IDidMount
      (did-mount [_]
        (let [event-chan   (om/get-state owner :event-chan)]
          (put! event-chan {:event :alert :value {:status false}})))
      om/IRenderState
      (render-state [_ state]
        (html
         (let [property_id  (:properties (om/observe owner (current-selections)))
               refresh-chan (om/get-shared owner :refresh)
               event-chan   (om/get-state owner :event-chan)
               {:keys [status text]} (:alert state)]
           [:div
            (alert "alert alert-danger" [:p text] status "new-sensor-form-alert" owner)
            [:h3 "Add new sensor"]
            [:form.form-horizontal {:role "form"}
             [:div.col-md-6
              [:div.form-group
               [:div.btn-toolbar
                [:button {:type "button"
                          :class "btn btn-success"
                          :onClick (fn [_]
                                     (let [sensor (om/get-state owner :sensor)]
                                       (if (valid-sensor? sensor)
                                         (put-new-sensor event-chan refresh-chan owner sensor property_id device_id)
                                         (om/set-state! owner :alert {:status true
                                                                      :class "alert alert-danger"
                                                                      :text  " Please enter required sensor data."}))))}
                 "Save"]
                [:button {:type "button"
                          :class "btn btn-danger"
                          :onClick (fn [_]
                                     (put! event-chan {:event :adding-sensor :value false}))}
                 "Cancel"]]]
              [:div.form-group
               [:label.control-label.col-md-2 {:for "device_id"} "Device Id"]
               [:p {:class "form-control-static col-md-10"} device_id]]
              (bs/text-input-control cursor owner [:sensor :type] "Type" true)
              (bs/text-input-control cursor owner [:sensor :alias] "Header Rows")
              (bs/text-input-control cursor owner [:sensor :unit] "Unit" true)
              (sensor-period-dropdown owner [:sensor :period] "Select period")
              (bs/text-input-control cursor owner [:sensor :resolution] "Resolution")
              (bs/checkbox cursor owner [:sensor :actual_annual] "Calculated Field")]]]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Editing device

(defn put-edited-device [event-chan refresh-chan owner device property_id device_id]
  (common/put-resource (str "/4/entities/" property_id "/devices/" device_id)
                       device
                       (fn [_]
                         (put! refresh-chan {:event :property})
                         (put! event-chan {:event :editing :value false})
                         (put! event-chan {:event :alert :value {:status true
                                                                 :class "alert alert-success"
                                                                 :text "Device was edited successfully."}}))
                       (error-handler event-chan)))

(defn save-edited-device [event-chan existing-device refresh-chan owner property_id device_id]
  (let [{:keys [device sensors]} (om/get-state owner)
        device (-> device
                   (cond-> (:privacy device) (update-in [:privacy] str)))
        readings (into [] (map (fn [[k v]] (assoc v :sensor_id k)) sensors))]
    (put-edited-device event-chan refresh-chan owner (assoc device :readings readings)
                       property_id device_id)
    (put! event-chan {:event :editing :value false})))

(defn delete-device [entity_id device_id event-chan refresh-chan]
  (delete-resource (str "/4/entities/" entity_id "/devices/" device_id)
                   (fn []
                     (js/alert (str "Device " device_id " deleted!"))
                     (put! refresh-chan {:event :property})
                     (put! event-chan {:event :editing :value false})
                     (put! event-chan {:event :selected :value nil}))
                   #(js/alert (str "Unable to delete " device_id))))

(defn delete-sensor [entity_id device_id sensor-type event-chan refresh-chan]
  (delete-resource (str "/4/entities/" entity_id "/devices/" device_id "/" sensor-type)
                   (fn []
                     (js/alert (str "Sensor " sensor-type " deleted!"))
                     (put! refresh-chan {:event :property})
                     (put! event-chan {:event :editing :value false})
                     (put! event-chan {:event :selected :value nil}))
                   #(js/alert (str "Unable to delete " sensor-type))))

(defn sensor-edit-div [property_id cursor owner event-chan refresh-chan]
  (let [sensor_id   (:sensor_id cursor)
        sensor-type (:type cursor)
        device_id   (:device_id cursor)]
    [:li.list-group-item
     [:div.row
      [:div.col-md-12 {:style {:padding-bottom "10px"}}
       [:button {:type "button"
                 :class "btn btn-danger pull-right"
                 :onClick (fn [_]
                            (delete-sensor property_id device_id sensor-type event-chan refresh-chan))}
        "Delete Sensor"]]]
     (text-input-control cursor owner [:sensors sensor_id] :type "Type")
     (text-input-control cursor owner [:sensors sensor_id] :alias "Header Rows")
     (text-input-control cursor owner [:sensors sensor_id] :unit "Unit")
     [:div {:style {:padding-left "15px"}}
      (sensor-period-dropdown owner [:sensors sensor_id :period] (:period cursor))]
     (text-input-control cursor owner [:sensors sensor_id] :resolution "Resolution")
     (checkbox cursor owner [:sensors sensor_id] :actual_annual "Calculated Field")]))

(defn synthetic-sensor-edit-div [cursor owner]
  (let [sensor_id   (:sensor_id cursor)]
    [:div
     [:li.list-group-item
      (static-text cursor [:type] "Type")
      (static-text cursor [:unit] "Unit")
      (static-text cursor [:period] "Period")
      (checkbox cursor owner [:sensors sensor_id] :actual_annual "Calculated Field")]]))

(defn privacy-checkbox [cursor owner keys label]
  [:div.checkbox
   [:label
    [:input {:type "checkbox"
             :defaultChecked (when (= (get-in cursor keys) "true") true)
             :on-change #(om/set-state! owner keys (.-checked (.-target %1)))}
     label]]])

(defn edit-device-form [cursor owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [event-chan   (om/get-state owner :event-chan)]
        (put! event-chan {:event :alert :value {:status false}})))
    om/IRenderState
    (render-state [_ state]
      (let [property_id  (:properties (om/observe owner (current-selections)))
            device_id    (-> cursor :device :device_id)
            refresh-chan (om/get-shared owner :refresh)
            event-chan   (om/get-state owner :event-chan)
            {:keys [status text]} state
            synthetic?   (-> cursor :device :synthetic)]
        (html
         [:div
          (alert "alert alert-danger" [:p text] status "edit-device-form-alert" owner)
          [:h3 "Editing device"]
          [:form {:role "form"}
           [:div.col-md-6
            [:div.form-group
             [:div.btn-toolbar
              [:button {:type "button"
                        :class (str "btn btn-success")
                        :onClick (fn [_]
                                   (let [edited-data (om/get-state owner :device)]
                                     (save-edited-device event-chan (:device @cursor) refresh-chan owner
                                                         property_id device_id)))} "Save"]
              [:button {:type "button"
                        :class (str "btn btn-danger")
                        :onClick (fn [_]
                                   (put! event-chan {:event :editing :value false}))} "Cancel"]
              (when-not synthetic?
                [:button {:type "button"
                          :class "btn btn-danger pull-right"
                          :onClick (fn [_]
                                     (delete-device property_id device_id event-chan refresh-chan))}
                 "Delete Device"])]]
            [:h3 "Device"]
            (bs/static-text cursor [:device :device_id] "Device ID")
            (if-not synthetic?
              [:div
               (bs/text-input-control cursor owner [:device :description] "Unique Description")
               (bs/text-input-control cursor owner [:device :name] "Further Description")
               (location-input cursor owner)
               (privacy-checkbox cursor owner [:device :privacy] "Private")]
              [:div
               (bs/static-text cursor [:device :description] "Unique Description")])
            [:h3 "Sensors"]
            (let [sensors (into [] (:readings (:device cursor)))]
              (if (seq sensors)
                [:ul.list-group
                 (for [sensor  sensors]
                   (if (:synthetic sensor)
                     (synthetic-sensor-edit-div sensor owner)
                     (sensor-edit-div property_id sensor owner event-chan refresh-chan)))]
                [:p "No sensors found."]))]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Adding new device

(defn post-new-device [event-chan refresh-chan owner device property_id]
  (common/post-resource (str "/4/entities/" property_id "/devices/")
                        (-> device
                            (update-in [:privacy] str)
                            (cond-> (-> device :location :latitude) (update-in [:location :latitude] js/parseFloat))
                            (cond-> (-> device :location :longitude) (update-in [:location :longitude] js/parseFloat)))
                        (fn [_]
                          (put! refresh-chan {:event :property})
                          (put! event-chan {:event :adding-device :value false})
                          (put! event-chan {:event :alert :value {:status true
                                                                 :class "alert alert-success"
                                                                 :text "Device was created successfully."}}))
                        (error-handler event-chan)))

(defn valid-device? [device]
  (not (nil? (:description device)))) ;; entity_id comes from the selection above

(defn new-device-form [cursor owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (let [event-chan   (om/get-state owner :event-chan)]
        (put! event-chan {:event :alert :value {:status false}})))
    om/IRender
    (render [_]
      (html
       (let [property_id  (:properties (om/observe owner (current-selections)))
             refresh-chan (om/get-shared owner :refresh)
             event-chan   (om/get-state owner :event-chan)
             {:keys [text status]} (om/get-state owner :alert)]
         [:div
          [:div.col-md-12
           (alert "alert alert-danger" [:p text] status "new-device-form-alert" owner)
           [:h3 "Add new device"]
           [:form.form-horizontal {:role "form"}
            [:div.col-md-6
             [:div.form-group
              [:div.btn-toolbar
               [:button {:class "btn btn-success"
                         :type "button"
                         :onClick (fn [_] (let [device (-> (om/get-state owner :device)
                                                           (assoc :entity_id property_id))
                                                sensor (om/get-state owner :sensor)]
                                            (if (or (and (not (seq sensor))
                                                         (valid-device? device))
                                                    (and (seq sensor)
                                                         (valid-device? device)
                                                         (valid-sensor? sensor)))
                                              (post-new-device event-chan refresh-chan owner
                                                               (-> device
                                                                   (cond-> (seq sensor) (assoc :readings [sensor]))) property_id)
                                              (om/set-state! owner :alert {:status true
                                                                           :class "alert alert-danger"
                                                                           :text " Please enter the required fields."}))))}
                "Save"]
               [:button {:type "button"
                         :class "btn btn-danger"
                         :onClick (fn [_]
                                    (put! event-chan {:event :adding-device :value false}))}
                "Cancel"]]]
             (bs/text-input-control cursor owner [:device :description] "Unique Description" true)
             (bs/text-input-control cursor owner [:device :name] "Further Description")
             (location-input cursor owner)
             (bs/checkbox cursor owner [:device :privacy] "Private")]]]
          [:div.col-md-12
           [:h3 "Create sensor:"]
           [:form.form-horizontal {:role "form"}
            [:div.col-md-6
             (bs/text-input-control nil owner [:sensor :type] "Type" true)
             (bs/text-input-control nil owner [:sensor :alias] "Header Rows")
             (bs/text-input-control nil owner [:sensor :unit] "Unit" true)
             (sensor-period-dropdown owner [:sensor :period] "Select period")
             (bs/text-input-control nil owner [:sensor :resolution] "Resolution")
             (bs/checkbox nil owner [:sensor :actual_annual] "Calculated Field")]]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Devices table

(defn expanded-device-view [device owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (common/fixed-scroll-to-element "expanded_device_view"))
     om/IDidUpdate
     (did-update [_ _ _]
       (common/fixed-scroll-to-element "expanded_device_view"))
    om/IRender
    (render [_]
      (html
       (let [devices-cursor (devices)]
         [:div
          [:h3 "Device Details"]
          [:div.panel.panel-default {:id "expanded_device_view"}
           [:div.panel-body
            [:div.btn-toolbar.pull-right
             ;; Edit device
             [:button {:type "button"
                       :class (str "btn btn-primary " (when-not (:editable device) "hidden"))
                       :onClick (fn [_]
                                  (om/update! devices-cursor [:edited-device :device] device)
                                  (om/update! devices-cursor :editing true))}
              [:div {:class "fa fa-pencil-square-o"}]  " Edit device"]
             ;; Add sensor
             [:button {:type "button"
                       :class (str  "btn btn-primary " (when-not (:editable device) "hidden"))
                       :onClick (fn [_] (om/update! devices-cursor :adding-sensor true))}
              [:div {:class "fa fa-plus"}]  " Add sensor"]]
            [:div
             (bs/static-text device [:description] "Description")
             (bs/static-text device [:name] "Name")
             [:div.form-group [:label "Location"] (common/location-col (:location device))]
             [:div.form-group [:label "Privacy"] (privacy-label (:privacy device))]]
            [:div
             [:h3 "Sensors"]
             [:table.table.borderless
              [:thead
               [:tr [:th "Type"] [:th "Synthetic?"] [:th "Header Rows"] [:th "Unit"] [:th "Period"]
                [:th "Resolution"] [:th "Calculated Field"]]]
              [:tbody
               (for [sensor (:readings device)]
                 (let [{:keys [alias actual_annual]} sensor]
                   [:tr
                    [:td (:type sensor)]
                    [:td (if (:synthetic sensor) "Yes" "No")]
                    [:td alias]
                    [:td (:unit sensor)]
                    [:td (:period sensor)]
                    [:td (:resolution sensor)]
                    [:td (when actual_annual [:div {:class "fa fa-magic"}])]]))]]]]]])))))

(defn form-row [properties table-id editing-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [description privacy location name
                       device_id editable privacy]} cursor
                       devices   (:devices properties)
                       selected? (= (:selected devices) device_id)]
           [:tr {:onClick (fn [_]
                            (om/update! devices :selected (if selected? nil device_id))
                            (om/update! devices :selected-device cursor))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td description]
            [:td name]
            [:td (common/location-col location)]
            [:td (privacy-label privacy)]
            [:td (count (filter #(not (:synthetic %)) (:readings cursor)))]
            [:td (string/join ", " (mapv :type (:readings cursor)))]
            [:td device_id]]))))))


(defn devices-table [editing-chan properties]
  (fn [cursor owner]
    (reify
      om/IInitState
      (init-state [_]
        {:th-chan (chan)})
      om/IWillMount
      (will-mount [_]
        (go-loop []
          (let [{:keys [th-chan]}           (om/get-state owner)
                sort-spec                   (:sort-spec @cursor)
                {:keys [sort-key sort-asc]} sort-spec
                th-click                    (<! th-chan)]
            (if (= th-click sort-key)
              (om/update! cursor :sort-spec {:sort-key th-click
                                             :sort-asc (not sort-asc)})
              (om/update! cursor :sort-spec {:sort-key th-click
                                             :sort-asc true})))
          (recur)))
      om/IDidMount
      (did-mount [_]
        (common/fixed-scroll-to-element "devices-tab"))
      om/IRenderState
      (render-state [_ state]
        (let [sort-spec                   (:sort-spec cursor)
              {:keys [sort-key sort-asc]} sort-spec
              th-chan                     (om/get-state owner :th-chan)
              devices                     (into [] (fetch-devices (-> properties :selected) properties))
              table-id                    "sensors-table"]
          (html
           [:div.col-md-12 {:style {:overflow "auto"}}
            [:table {:class "table table-hover table-condensed"}
             [:thead
              [:tr
               (bs/sorting-th sort-spec th-chan "Unique Description" :description)
               (bs/sorting-th sort-spec th-chan "Further Description" :name)
               (bs/sorting-th sort-spec th-chan "Location" :location)
               (bs/sorting-th sort-spec th-chan "Privacy" :privacy)
               (bs/sorting-th sort-spec th-chan "No. of Sensors" :count)
               (bs/sorting-th sort-spec th-chan "Sensor Types" :sensor_types)
               (bs/sorting-th sort-spec th-chan "Device ID" :device_id)]]
             [:tbody
              (for [row (if sort-asc
                          (sort-by sort-key devices)
                          (reverse (sort-by sort-key devices)))]
                (om/build (form-row properties table-id editing-chan) row) {:key :device_id})]]]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Entire devices tab view

(defn devices-div [properties owner]
  (reify
    om/IInitState
    (init-state [_]
      {:editing-chan (chan)
       :event-chan   (chan)})
    om/IWillMount
    (will-mount [_]
      (let [{:keys [editing-chan event-chan]} (om/get-state owner)]
        (go-loop []
          (let [edited-row (<! editing-chan)]
            (om/update! properties [:sensors :editing] true)
            (om/update! properties [:sensors :row] edited-row)
            (common/fixed-scroll-to-element "sensor-edit-div"))
          (recur))
        (go-loop []
          (let [{:keys [event value]}  (<! event-chan)]
            (om/update! properties [:devices event] value))
          (recur))))
    om/IRenderState
    (render-state [_ {:keys [editing-chan event-chan]}]
      (let [{:keys [devices]} properties
            {:keys [editing adding-sensor adding-device]} devices
            device_id      (-> devices :selected)
            project_id     (-> properties :project_id)
            property_id    (-> properties :selected)
            property       (-> properties :selected-property :property)]
        (html
         [:div.col-md-12 {:id "devices-tab"}
          [:h3 "Devices"
           [:div.btn-toolbar.pull-right
            ;; Add new device
            [:button {:type "button"
                      :class (str "btn btn-primary " (when (or adding-sensor
                                                               adding-device
                                                               editing
                                                               (not (:editable property)))
                                                       "hidden"))
                      :onClick (fn [_]
                                 (om/update! properties [:devices :adding-device] true))}
             [:div {:class "fa fa-plus"}] " Add device"]]]

          [:div {:id "alert-div" :style {:padding-top "10px"}}
           (om/build bs/alert (:alert devices))]
          (when (and (not editing) (not adding-sensor) (not adding-device))
            [:div {:id "devices-div" :style {:padding-top "10px"}}
             (om/build (devices-table editing-chan properties) devices)])
          [:div
           (when (and (seq device_id) (not editing) (not adding-device))
             (om/build expanded-device-view (-> (filter #(= (:device_id %) device_id) (:devices property))
                                                first
                                                (assoc :editable (:editable property)))))]
          (when adding-sensor
            [:div {:id "sensor-add-div"}
             (om/build (new-sensor-form device_id) {} {:init-state {:event-chan event-chan}})])
          (when adding-device
            [:div {:id "device-add-div"}
             (om/build new-device-form (:new-device devices) {:init-state {:event-chan event-chan}})])
          (when editing
            [:div {:id "device-edit-div"}
             (om/build edit-device-form (:edited-device devices) {:init-state {:event-chan event-chan}})])])))))
