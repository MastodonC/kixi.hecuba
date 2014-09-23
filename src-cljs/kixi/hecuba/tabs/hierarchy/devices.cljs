(ns kixi.hecuba.tabs.hierarchy.devices
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (static-text) :as bs]
            [kixi.hecuba.tabs.hierarchy.data :refer (fetch-property fetch-devices fetch-sensors)]
            [clojure.string :as string]
            [kixi.hecuba.common :refer (log delete-resource) :as common]))

(defn sorting-th [owner label header-key]
  (let [{:keys [sort-spec th-chan]} (om/get-state owner)
        {:keys [sort-key sort-asc]} sort-spec]
    [:th {:onClick (fn [_ _] (put! th-chan header-key))}
     (str label " ")
     (if (= sort-key header-key)
       (if sort-asc
         [:i.fa.fa-sort-asc]
         [:i.fa.fa-sort-desc]))]))

(defn privacy-label [privacy]
  [:div
   (when (= "true" privacy) [:div {:class "fa fa-key"}])])

(defn text-input-control [cursor owner path key label & required]
  [:div.form-group
   [:label.control-label.col-md-2 {:for (name key)} label]
   [:div {:class (str (if required "required " "") "col-md-10")}
    [:input {:defaultValue (get cursor key "")
             :on-change #(om/set-state! owner (conj path key) (.-value (.-target %1)))
             :class "form-control"
             :type "text"}]]])

(defn checkbox [cursor owner path key label]
  [:div.form-group
   [:label.control-label.col-md-2 {:for (str key)} label]
   [:input {:type "checkbox"
            :defaultChecked (get cursor key "")
            :on-change #(om/set-state! owner (conj path key) (.-checked (.-target %1)))}]])

(defn location-input [cursor owner]
  (let [location (-> cursor :location)]
    [:div
     [:div.form-group
      [:label.control-label.col-md-2 {:for "location"} "Location"]
      [:div.col-md-10
       [:div.form-group
        [:label.control-label.col-md-2 {:for "name"} "Name"]
        [:div.col-md-8
         [:input {:defaultValue (get location :name "")
                  :on-change #(om/set-state! owner [:device :location :name] (.-value (.-target %1)))
                  :class "form-control"
                  :type "text"
                  :id "location_name"}]]]
       [:div.form-group
        [:label.control-label.col-md-2 {:for "latitude"} "Latitude"]
        [:div.col-md-8
         [:input {:defaultValue (get location :latitude "")
                  :on-change #(om/set-state! owner [:device :location :latitude] (.-value (.-target %1)))
                  :class "form-control"
                  :type "number"
                  :id "location_latitude"}]]]
       [:div.form-group
        [:label.control-label.col-md-2 {:for "longitude"} "Longitude"]
        [:div.col-md-8
         [:input {:defaultValue (get location :longitude "")
                  :on-change #(om/set-state! owner [:device :location :longitude] (.-value (.-target %1)))
                  :class "form-control"
                  :type "number"
                  :id "location_longitude"}]]]]]]))

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

(defn sensor-period-dropdown [owner]
  [:div.form-group
   [:label.control-label.col-md-2 {:for "period-select"} "Period"]
   [:div.col-md-10.required
    [:select.form-control {:on-change #(om/set-state! owner [:sensor :period] (.-value (.-target %)))}
     (for [item ["Select period" "CUMULATIVE" "INSTANT" "PULSE"]]
       [:option item])]]])

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

(defn new-sensor-form [property_id device_id]
  (fn [cursor owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         (let [refresh-chan (om/get-shared owner :refresh)
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
              (bs/text-input-control nil owner :sensor :type "Type" true)
              (bs/text-input-control nil owner :sensor :alias "Header Rows")
              (bs/text-input-control nil owner :sensor :unit "Unit" true)
              (sensor-period-dropdown owner)
              (bs/text-input-control nil owner :sensor :resolution "Resolution")
              (bs/checkbox nil owner :sensor :actual_annual "Calculated Field")]]]))))))

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
        device (common/deep-merge @existing-device device)
        readings (into [] (map (fn [[k v]] (assoc v :type k)) sensors))]
    (put-edited-device event-chan refresh-chan owner (assoc device :readings readings)
                       property_id device_id)
    (put! event-chan {:event :editing :value false})))

(defn delete-device [entity_id device_id event-chan refresh-chan]
  (delete-resource (str "/4/entities/" entity_id "/devices/" device_id)
                   #(log "Deleted " entity_id ":" device_id))
  (js/alert (str "Deleted! " device_id))
  (put! refresh-chan {:event :property})
  (put! event-chan {:event :editing :value false})
  (put! event-chan {:event :selected :value nil}))

(defn sensor-edit-div [cursor owner]
  (let [sensor-type (:type cursor)]
    [:li.list-group-item
     (static-text cursor :type "Type")
     (text-input-control cursor owner [:sensors sensor-type] :alias "Header Rows")
     (text-input-control cursor owner [:sensors sensor-type] :unit "Unit")
     (text-input-control cursor owner [:sensors sensor-type] :period "Period")
     (text-input-control cursor owner [:sensors sensor-type] :resolution "Resolution")
     (checkbox cursor owner [:sensors sensor-type] :actual_annual "Calculated Field")]))

(defn edit-device-form [property_id]
  (fn [cursor owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (let [device_id    (:device_id cursor)
              refresh-chan (om/get-shared owner :refresh)
              event-chan   (om/get-state owner :event-chan)
              {:keys [status text]} state]
          (html
           [:div
            (alert "alert alert-danger" [:p text] status "edit-device-form-alert" owner)
            [:h3 "Editing device"]
            [:form.form-horizontal {:role "form"}
             [:div.col-md-6
              [:div.form-group
               [:div.btn-toolbar
                [:button {:type "button"
                          :class (str "btn btn-success")
                          :onClick (fn [_]
                                     (save-edited-device event-chan cursor refresh-chan owner property_id device_id))} "Save"]
                [:button {:type "button"
                          :class (str "btn btn-danger")
                          :onClick (fn [_]
                                     (put! event-chan {:event :editing :value false}))} "Cancel"]
                [:button {:type "button"
                          :class "btn btn-danger"
                          :onClick (fn [_]
                                     (delete-device property_id device_id event-chan refresh-chan))}
                 "Delete Device"]]]
              [:h3 "Device"]
              (static-text cursor :device_id "Device ID")
              (static-text cursor :description "Unique Description")
              (text-input-control cursor owner [:device] :name "Further Description")
              (location-input cursor owner)
              (bs/checkbox cursor owner :device :privacy "Private")
              [:h3 "Sensors"]
              (let [sensors (remove #(:synthetic %) (:readings cursor))]
                (if (seq sensors)
                  [:ul.list-group
                   (for [sensor  sensors]
                     (sensor-edit-div sensor owner))]
                  [:p "No sensors found."]))]]]))))))

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

(defn new-device-form [property_id]
  (fn [cursor owner]
    (om/component
     (html
      (let [refresh-chan (om/get-shared owner :refresh)
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
            (bs/text-input-control cursor owner :device :description "Unique Description" true)
            (bs/text-input-control cursor owner :device :name "Further Description")
            (location-input cursor owner)
            (bs/checkbox cursor owner :device :privacy "Private")]]]
         [:div.col-md-12
          [:h3 "Create sensor:"]
          [:form.form-horizontal {:role "form"}
           [:div.col-md-6
            (bs/text-input-control nil owner :sensor :type "Type" true)
            (bs/text-input-control nil owner :sensor :alias "Header Rows")
            (bs/text-input-control nil owner :sensor :unit "Unit" true)
            (sensor-period-dropdown owner)
            (bs/text-input-control nil owner :sensor :resolution "Resolution")
            (bs/checkbox nil owner :sensor :actual_annual "Calculated Field")]]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Devices table

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
           [:tr {:onClick (fn [_] (om/update! devices :selected (if selected? nil device_id)))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td description]
            [:td name]
            [:td (common/location-col location)]
            [:td (privacy-label privacy)]
            [:td (count (filter #(not (:synthetic %)) (:readings cursor)))]
            [:td (string/join ", " (mapv :type (filter #(not (:synthetic %)) (:readings cursor))))]
            [:td device_id]]))))))


(defn devices-table [editing-chan properties]
  (fn [cursor owner]
    (reify
      om/IInitState
      (init-state [_]
        {:th-chan (chan)
         :sort-spec {:sort-key :type
                     :sort-asc true}})
      om/IWillMount
      (will-mount [_]
        (go-loop []
          (let [{:keys [th-chan sort-spec]} (om/get-state owner)
                {:keys [sort-key sort-asc]} sort-spec
                th-click                    (<! th-chan)]
            (if (= th-click sort-key)
              (om/update-state! owner #(assoc %
                                         :sort-spec {:sort-key th-click
                                                     :sort-asc (not sort-asc)}))
              (om/update-state! owner #(assoc %
                                         :sort-spec {:sort-key th-click
                                                     :sort-asc true}))))
          (recur)))
      om/IRenderState
      (render-state [_ state]
        (let [{:keys [sort-key sort-asc]} (:sort-spec state)
              devices                     (fetch-devices (-> properties :selected) properties)
              table-id                    "sensors-table"]
          (html
           [:div.col-md-12 {:style {:overflow "auto"}}
            [:table {:class "table table-hover table-condensed"}
             [:thead
              [:tr
               (sorting-th owner "Unique Description" :description)
               (sorting-th owner "Further Description" :name)
               (sorting-th owner "Location" :location)
               (sorting-th owner "Privacy" :privacy)
               (sorting-th owner "No. of Sensors" :count)
               (sorting-th owner "Sensor Types" :sensor_types)
               (sorting-th owner "Device ID" :device_id)]]
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
            editing        (-> devices :editing)
            adding-sensor  (-> devices :adding-sensor)
            adding-device  (-> devices :adding-device)
            project_id     (-> properties :project_id)
            property_id    (-> properties :selected)
            device_id      (-> devices :selected)
            property       (-> (filter #(= (:entity_id %) property_id) (:data properties))
                               first)]
        (html
         [:div.col-md-12
          [:h3 "Devices"]
          ;; Buttons
          [:div.btn-toolbar
           ;; Add new device
           [:button {:type "button"
                     :class (str "btn btn-primary " (when (or adding-sensor
                                                              adding-device
                                                              editing
                                                              (not (:editable property)))
                                                      "hidden"))
                     :onClick (fn [_]
                                (om/update! properties [:devices :adding-device] true))}
            [:div {:class "fa fa-plus"}] " Add device"]
           ;; Edit device
           [:button {:type "button"
                     :class (str "btn btn-primary " (when-not (and (not adding-sensor)
                                                                   (not adding-device)
                                                                   (not editing)
                                                                   (:editable property)
                                                                   device_id) "hidden"))
                     :onClick (fn [_]
                                (let [device (first (filter #(= (:device_id %) device_id) (fetch-devices property_id @properties)))]
                                  (om/update! properties [:devices :edited-device] device)
                                  (om/update! properties [:devices :editing] true)))}
            [:div {:class "fa fa-pencil-square-o"}]  " Edit device"]
           ;; Add sensor
           [:button {:type "button"
                     :class (str  "btn btn-primary " (when-not (and (not adding-sensor)
                                                                    (not adding-device)
                                                                    (not editing)
                                                                    (:editable property)
                                                                    device_id) "hidden"))
                     :onClick (fn [_] (om/update! properties [:devices :adding-sensor] true))}
            [:div {:class "fa fa-plus"}]  " Add sensor"]]
          [:div {:id "alert-div" :style {:padding-top "10px"}}
           (om/build bs/alert (:alert devices))]
          [:div {:id "devices-div"
                 :class (if (or editing adding-sensor adding-device) "hidden" "")
                 :style {:padding-top "10px"}}
           (om/build (devices-table editing-chan properties) devices)]
          [:div {:id "sensor-add-div" :class (if adding-sensor "" "hidden")}
           (om/build (new-sensor-form property_id device_id) {} {:init-state {:event-chan event-chan}})]
          [:div {:id "device-add-div" :class (if adding-device "" "hidden")}
           (om/build (new-device-form property_id) {} {:init-state {:event-chan event-chan}})]
          [:div {:id "device-edit-div" :class (if editing "" "hidden")}
           (om/build (edit-device-form property_id) (:edited-device devices) {:init-state {:event-chan event-chan}})]])))))
