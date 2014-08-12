(ns kixi.hecuba.tabs.hierarchy
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
   [om.core :as om :include-macros true]
   [cljs.core.async :refer [<! >! chan put! sliding-buffer close! pipe map< filter< mult tap map>]]
   [cljs.reader :as reader]
   [ajax.core :refer (GET)]
   [clojure.string :as str]
   [kixi.hecuba.bootstrap :as bs]
   [kixi.hecuba.history :as history]
   [kixi.hecuba.model :refer (app-model)]
   [kixi.hecuba.tabs.slugs :as slugs]
   [kixi.hecuba.tabs.hierarchy.programmes :as programmes]
   [kixi.hecuba.tabs.hierarchy.projects :as projects]
   [kixi.hecuba.tabs.hierarchy.properties :as properties]
   [kixi.hecuba.tabs.hierarchy.property-details :as property-details]
   [kixi.hecuba.tabs.hierarchy.data :refer (fetch-programmes fetch-projects fetch-properties) :as data]
   [kixi.hecuba.common :refer (log) :as common]
   [sablono.core :as html :refer-macros [html]]))

(defn back-to-programmes [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (history/update-token-ids! history :projects nil)
    (history/update-token-ids! history :programmes nil)
    (common/fixed-scroll-to-element "programmes-div")))

(defn back-to-properties [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (history/update-token-ids! history :properties nil)
    (common/fixed-scroll-to-element "properties-div")))

(defn back-to-devices [history]
  (fn [_ _]
    (history/update-token-ids! history :sensors nil)
    (history/update-token-ids! history :devices nil)
    (common/fixed-scroll-to-element "devices-div")))

(defn uri-for-selection-change
  "Returns the uri to load because of change of selection. Returns nil
   if no change to selection"
  [current-selected selection-key template nav-event]
  (let [ids          (-> nav-event :args :ids)
        new-selected (get ids selection-key)]
    (when (or (nil? current-selected)
              (nil? new-selected)
              (not= current-selected
                    new-selected))
      (vector new-selected
              (common/map-replace template ids)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; History loop - this drives the fetches and clear downs
(defn history-loop [history-channel data]
  (go-loop []
    (let [nav-event                                        (<! history-channel)
          history-status                                   (-> nav-event :args :ids)
          {:keys [programmes projects properties sensors]} history-status
          old-nav                                          (:active-components @data)
          old-programmes                                   (:programmes old-nav)
          old-projects                                     (:projects old-nav)
          old-properties                                   (:properties old-nav)]
      (log "Old Programmes: " old-programmes " Old Projects: " old-projects " Old Properties: " old-properties)
      (log "New Programmes: " programmes " New Projects: " projects " New Properties: " properties)

      ;; Clear down
      (when (or (nil? programmes)
                (empty? (-> @data :programmes :data)))
        (log "Clearing projects.")
        (om/update! data [:programmes :selected] nil)
        (om/update! data [:projects :data] [])
        (om/update! data [:projects :programme_id] nil)
        (fetch-programmes data))

      (when-not projects
        (log "Clearing properties.")
        (om/update! data [:projects :selected] nil)
        (om/update! data [:properties :data] [])
        (om/update! data [:properties :project_id] nil))

      (when-not properties
        (log "Clearing devices, sensors and measurements.")
        (om/update! data [:properties :selected] nil)
        (om/update! data [:property-details :data] {})
        (om/update! data [:property-details :property_id] nil)
        (om/update! data [:devices :data] [])
        (om/update! data [:sensors :data] [])
        (om/update! data [:measurements :data] []))

      (when (and (not= programmes old-programmes)
                 programmes)
        (log "Setting selected programme to: " programmes)
        (om/update! data [:programmes :selected] programmes)
        (om/update! data [:projects :programme_id] programmes)
        (fetch-projects programmes data))

      (when (and (not= projects old-projects)
                 projects)
        (log "Setting selected project to: " projects)
        (om/update! data [:projects :selected] projects)
        (om/update! data [:properties :project_id] projects)
        (fetch-properties projects data))

      (when (and (not= properties old-properties)
                 properties)
        (log "Setting property details to: " properties)
        (om/update! data [:properties :selected] properties)
        (om/update! data [:sensors :selected] #{})
        (om/update! data [:chart :sensors] #{}))

      (when sensors
        (log "Setting selected sensors to: " sensors)
        (om/update! data [:sensors :selected] (into #{} (str/split sensors #";"))))

      ;; Update the new active components
      (om/update! data :active-components history-status))
    (recur)))

(defn selected-range-change
  [selected selection-key {{ids :ids search :search} :args}]
  (let [new-selected (get ids selection-key)]
    (when (or (nil? selected)
              (not= selected new-selected))
      (vector new-selected ids search))))

;; updates chart cursor only
(defn chart-ajax [in data {:keys [selection-key content-type]}]
  (go-loop []
    (let [nav-event (<! in)]
      (when-let [[new-range ids search] (selected-range-change (-> @data :chart :range)
                                                               selection-key
                                                               nav-event)]
        (let [[start-date end-date] search
              entity_id  (get ids :properties)
              sensor_ids (get ids :sensors)]

          (om/update! data [:chart :range] {:start-date start-date :end-date end-date})
          (om/update! data [:chart :sensors] sensor_ids)
          (om/update! data [:chart :measurements] [])

          (when (and (not (empty? start-date))
                     (not (empty? end-date))
                     (not (empty? sensor_ids)))
            (log "Fetching measurements for sensors: " sensor_ids " and range: " start-date end-date)
            (data/fetch-measurements data entity_id sensor_ids start-date end-date)))))
    (recur)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main View

(defn main-tab [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [history     (om/get-shared owner :history)
            property-chan (om/get-shared owner :property-chan)
            m           (mult (history/set-chan! history (chan)))
            tap-history #(tap m (chan))]

        ;; handle navigation changes
        (history-loop (tap-history) data)

        (chart-ajax (tap-history)
                    data
                    {:template "/4/entities/:properties/devices/:devices/measurements?startDate=:start-date&endDate=:end-date"
                     :content-type  "application/json"
                     :selection-key :range})))
    om/IRender
    (render [_]

      (html [:div
             (om/build programmes/programmes-div data)
             (om/build projects/projects-div data)
             (om/build properties/properties-div data)
             (om/build property-details/property-details-div data)]))))
