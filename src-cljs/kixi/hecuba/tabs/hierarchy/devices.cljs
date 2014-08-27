(ns kixi.hecuba.tabs.hierarchy.devices
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [<! >! chan put!]]
            [om.core :as om :include-macros true]
            [sablono.core :as html :refer-macros [html]]
            [kixi.hecuba.bootstrap :refer (static-text) :as bs]
            [ajax.core :refer [GET POST PUT]]
            [kixi.hecuba.tabs.hierarchy.data :refer (fetch-property fetch-devices fetch-sensors)]
            [clojure.string :as string]
            [kixi.hecuba.common :refer (log) :as common]))

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
                  :type "text"
                  :id "location_latitude"}]]]
       [:div.form-group
        [:label.control-label.col-md-2 {:for "longitude"} "Longitude"]
        [:div.col-md-8
         [:input {:defaultValue (get location :longitude "")
                  :on-change #(om/set-state! owner [:device :location :longitude] (.-value (.-target %1)))
                  :class "form-control"
                  :type "text"
                  :id "location_longitude"}]]]]]]))

(defn error-handler [devices]
  (fn [{:keys [status status-text]}]
    (om/update! devices :alert {:status true
                                :class "alert alert-danger"
                                :text status-text})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Adding new sensor

(defn put-new-sensor [devices refresh-chan owner sensor property_id device_id]
  (common/put-resource (str "/4/entities/" property_id "/devices/" device_id)
                       {:readings [sensor]}
                       (fn [_]
                         (put! refresh-chan {:event :property})
                         (om/update! devices :adding-sensor false)
                         (om/update! devices :alert {:status true
                                                     :class "alert alert-success"
                                                     :text "Sensor was added successfully."}))
                       (error-handler devices)))

(defn valid-sensor? [sensor]
  (and sensor
       (:type sensor)
       (:period sensor)
       (:unit sensor))) ;; device_id comes from the selection above

(defn new-sensor-form [devices property_id device_id]
  (fn [cursor owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (html
         (let [refresh-chan (om/get-shared owner :refresh)]
             [:div
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
                                           (put-new-sensor devices refresh-chan owner sensor property_id device_id)
                                           (om/update! devices :alert
                                                       {:status true
                                                        :class "alert alert-danger"
                                                        :text  " Please enter required sensor data."}))))}
                   "Save"]
                  [:button {:type "button"
                            :class "btn btn-danger"
                            :onClick (fn [_]
                                       (om/update! devices :adding-sensor false))}
                   "Cancel"]]]
                [:div.form-group
                 [:label.control-label.col-md-2 {:for "device_id"} "Device Id"]
                 [:p {:class "form-control-static col-md-10"} device_id]]
                (bs/text-input-control nil owner :sensor :type "Type" true)
                (bs/text-input-control nil owner :sensor :alias "Alias")
                (bs/text-input-control nil owner :sensor :unit "Unit" true)
                (bs/text-input-control nil owner :sensor :period "Period" true)
                (bs/text-input-control nil owner :sensor :resolution "Resolution")
                (bs/checkbox nil owner :sensor :actual_annual "Calculated Field")]]]))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Editing device

(defn put-edited-device [devices refresh-chan owner device property_id device_id]
  (common/put-resource (str "/4/entities/" property_id "/devices/" device_id)
                       device
                       (fn [_]
                         (put! refresh-chan {:event :property})
                         (om/update! devices :editing false)
                         (om/update! devices :alert {:status true
                                                     :class "alert alert-success"
                                                     :text "Device was edited successfully."}))
                       (error-handler devices)))


(defn save-edited-device [devices existing-device refresh-chan owner property_id device_id]
  (let [{:keys [device sensors]} (om/get-state owner)
        device (common/deep-merge @existing-device device)
        readings (into [] (map (fn [[k v]] (assoc v :type k)) sensors))]
    (put-edited-device devices refresh-chan owner (assoc device :readings readings)
                       property_id device_id)
    (om/update! devices :editing false)))

(defn sensor-edit-div [cursor owner]
  (let [sensor-type (:type cursor)]
    [:li.list-group-item
     (static-text cursor :type "Type")
     (text-input-control cursor owner [:sensors sensor-type]  :alias "Alias")
     (text-input-control cursor owner [:sensors sensor-type] :unit "Unit")
     (text-input-control cursor owner [:sensors sensor-type] :period "Period")
     (text-input-control cursor owner [:sensors sensor-type] :resolution "Resolution")
     (checkbox cursor owner [:sensors sensor-type] :actual_annual "Calculated Field")]))

(defn edit-device-form [devices property_id]
  (fn [cursor owner]
    (reify
      om/IRenderState
      (render-state [_ state]
        (let [device_id (:device_id cursor)
              refresh-chan (om/get-shared owner :refresh)]
          (html
           [:div
            [:h3 "Editing device"]
            [:form.form-horizontal {:role "form"}
             [:div.col-md-6
              [:div.form-group
               [:div.btn-toolbar
                [:button {:type "button"
                          :class (str "btn btn-success")
                          :onClick (fn [_]
                                     (save-edited-device devices cursor refresh-chan owner property_id device_id))} "Save"]
                [:button {:type "button"
                          :class (str "btn btn-danger")
                          :onClick (fn [_]
                                     (om/update! devices :editing false))} "Cancel"]]]
              [:h3 "Device"]
              (static-text cursor :device_id "Device ID")
              (text-input-control cursor owner [:device] :name "Parent Device Name")
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

(defn post-new-device [devices refresh-chan owner device property_id]
  (common/post-resource (str "/4/entities/" property_id "/devices/")
                        device
                        (fn [_]
                          (put! refresh-chan {:event :property})
                          (om/update! devices :adding-device false)
                          (om/update! devices :alert {:status true
                                                      :class "alert alert-success"
                                                      :text "Device was created successfully."}))
                        (error-handler devices)))

(defn valid-device? [device]
  (not (nil? (:description device)))) ;; entity_id comes from the selection above

(defn new-device-form [devices property_id]
  (fn [cursor owner]
    (om/component
     (html
      (let [refresh-chan (om/get-shared owner :refresh)]
        [:div
         [:h3 "Add new device"]
         [:form.form-horizontal {:role "form"}
          [:div.col-md-6
           [:div.form-group
            [:div.btn-toolbar
             [:button {:class "btn btn-success"
                       :type "button"
                       :onClick (fn [_] (let [device (->  (om/get-state owner :device)
                                                          (assoc :entity_id property_id))]
                                          (if (valid-device? device)
                                            (post-new-device devices refresh-chan owner device property_id)
                                            (om/update! devices :alert
                                                        {:status true
                                                         :class "alert alert-danger"
                                                         :text " Please enter description."}))))}
              "Save"]
             [:button {:type "button"
                       :class "btn btn-danger"
                       :onClick (fn [_]
                                  (om/update! devices :adding-device false))}
              "Cancel"]]]
           (bs/text-input-control cursor owner :device :description "Description" true)
           (location-input cursor owner)
           (bs/text-input-control cursor owner :device :metadata "Metadata")
           (bs/text-input-control cursor owner :device :name "Name")
           (bs/checkbox cursor owner :device :privacy "Private")]]])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Devices table

(defn form-row [properties table-id editing-chan]
  (fn [cursor owner]
    (reify
      om/IRender
      (render [_]
        (html
         (let [{:keys [description privacy location name
                       device_id editable metadata privacy]} cursor
               devices   (:devices properties)
               selected? (= (:selected devices) device_id)]
           [:tr {:onClick (fn [_] (om/update! devices :selected (if selected? nil device_id)))
                 :class (when selected? "success")
                 :id (str table-id "-selected")}
            [:td description]
            [:td device_id]
            [:td (common/location-col location)]
            [:td metadata]
            [:td name]
            [:td (privacy-label privacy)]
            [:td (count (filter #(not (:synthetic %)) (:readings cursor)))]]))))))


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
               (sorting-th owner "Description" :description)
               (sorting-th owner "Device ID" :device_id)
               (sorting-th owner "Location" :location)
               (sorting-th owner "Metadata" :metadata)
               (sorting-th owner "Name" :name)
               (sorting-th owner "Privacy" :privacy)
               (sorting-th owner "No. of Sensors" :count)]]
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
      {:editing-chan (chan)})
    om/IWillMount
    (will-mount [_]
      (go-loop []
        (let [{:keys [editing-chan]} (om/get-state owner)
              edited-row             (<! editing-chan)]
          (om/update! properties [:sensors :editing] true)
          (om/update! properties [:sensors :row] edited-row)
          (common/fixed-scroll-to-element "sensor-edit-div"))
        (recur)))
    om/IRenderState
    (render-state [_ {:keys [editing-chan]}]
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
          (when (and (not adding-sensor)
                     (not adding-device)
                     (not editing)
                     (:editable property)
                     (not (:selected devices)))
            [:div [:button {:type "button"
                            :class "btn btn-primary"
                            :onClick (fn [_]
                                       (om/update! properties [:devices :adding-device] true))}
                   "Add new device"]])
          (when (and (not adding-sensor)
                     (not adding-device)
                     (not editing)
                     (:editable property)
                     device_id)
            [:div.btn-toolbar
             [:button {:type "button"
                       :class "btn btn-primary"
                       :onClick (fn [_]
                                  (let [device (first (filter #(= (:device_id %) device_id) (fetch-devices property_id @properties)))]
                                    (om/update! properties [:devices :edited-device] device)
                                    (om/update! properties [:devices :editing] true)))}
              [:div {:class  "fa fa-pencil-square-o"} " Edit device"]]
             [:button {:type "button"
                       :class "btn btn-primary"
                       :onClick (fn [_] (om/update! properties [:devices :adding-sensor] true))}
              [:div {:class  "fa fa-plus"} " Add sensor"]]])
          [:div {:id "alert-div" :style {:padding-top "10px"}}
           (om/build bs/alert (:alert devices))]
          [:div {:id "devices-div"
                 :class (if (or editing adding-sensor adding-device) "hidden" "")
                 :style {:padding-top "10px"}}
           (om/build (devices-table editing-chan properties) devices)]
          [:div {:id "sensor-add-div" :class (if adding-sensor "" "hidden")}
           (om/build (new-sensor-form (:devices properties) property_id device_id) nil)]
          [:div {:id "device-add-div" :class (if adding-device "" "hidden")}
           (om/build (new-device-form (:devices properties) property_id) nil)]
          [:div {:id "device-edit-div" :class (if editing "" "hidden")}
           (om/build (edit-device-form (:devices properties) property_id) (:edited-device devices))]])))))
