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
   [kixi.hecuba.tabs.hierarchy.data :as data]
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
          history-status                                   (-> nav-event :args)
          [start-date end-date]                            (:search history-status)
          {:keys [programmes projects properties sensors]} (:ids history-status)
          old-nav                                          (:active-components @data)
          old-programmes                                   (-> old-nav :ids :programmes)
          old-projects                                     (-> old-nav :ids :projects)
          old-properties                                   (-> old-nav :ids :properties)
          [old-start-date old-end-date]                    (-> old-nav :search)]

      ;; Clear down
      (when (or (nil? programmes)
                (empty? (-> @data :programmes :data)))
        (log "Clearing projects.")
        (om/update! data [:programmes :selected] nil)
        (om/update! data [:projects :data] [])
        (om/update! data [:projects :programme_id] nil)
        (data/fetch-programmes data))

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
        (om/update! data [:properties :devices :data] [])
        (om/update! data [:properties :sensors :data] [])
        (om/update! data [:properties :chart :measurements] []))

      (when-not sensors
        (log "Clearing measurements")
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :units] {})
        (om/update! data [:properties :sensors :selected] #{}))

      (when (and (not= programmes old-programmes)
                 programmes)
        (log "Setting selected programme to: " programmes)
        (om/update! data [:programmes :selected] programmes)
        (om/update! data [:projects :programme_id] programmes)
        (om/update! data [:properties :programme_id] programmes)
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :sensors :alert] {})
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :units] {})
        (om/transact! data [:programmes :data] (fn [d] (mapv #(cond
                                                               (= old-programmes (:programme_id %)) (assoc % :selected false)
                                                               (= programmes (:programme_id %)) (assoc % :selected true)
                                                               :else %)
                                                             d)))
        (data/fetch-projects programmes data))

      (when (and (not= projects old-projects)
                 projects)
        (log "Setting selected project to: " projects)
        (om/update! data [:projects :selected] projects)
        (om/update! data [:properties :project_id] projects)
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :sensors :alert] {})
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :measurements] [])
        (om/update! data [:properties :chart :units] {})
        (om/update! data [:properties :editable] (-> @data :projects :editable))
        (om/transact! data [:projects :data] (fn [d] (mapv #(cond
                                                             (= old-projects (:project_id %)) (assoc % :selected false)
                                                             (= projects (:project_id %)) (assoc % :selected true)
                                                             :else %)
                                                           d)))
        (om/update! data [:properties :can-add-properties] (-> (filter #(= (:project_id %) projects) (-> @data :projects :data))
                                                               first
                                                               :editable))
        (data/fetch-properties projects data))

      (when (and (not= properties old-properties)
                 properties)
        (log "Setting property details to: " properties)
        (om/update! data [:properties :selected] properties)
        (om/update! data [:properties :sensors :selected] #{})
        (om/update! data [:properties :sensors :alert] {})
        (om/update! data [:properties :chart :sensors] #{})
        (om/update! data [:properties :chart :units] {})
        (om/update! data [:properties :chart :measurements] [])
        (om/transact! data [:properties :data] (fn [d] (mapv #(cond
                                                             (= old-properties (:entity_id %)) (assoc % :selected false)
                                                             (= properties (:entity_id %)) (assoc % :selected true)
                                                             :else %)
                                                           d))))

      (when sensors
        (log "Setting selected sensors to: " sensors)
        (let [sensors-hashmap (into #{} (str/split sensors #";"))]
          (om/update! data [:properties :chart :sensors] sensors-hashmap)
          (om/update! data [:properties :sensors :selected] sensors-hashmap)
          (when (and sensors old-end-date old-start-date)
            (data/fetch-measurements data properties sensors old-start-date old-end-date))))

      (when (or (not= start-date old-start-date)
                (not= end-date old-end-date))
        (log "Setting date range to: " start-date end-date)
        (om/update! data [:properties :chart :range] {:start-date start-date :end-date end-date})
        (when (and (not (every? empty? [start-date end-date]))
                   sensors)
          (data/fetch-measurements data properties sensors start-date end-date)))

      ;; Update the new active components
      (om/update! data :active-components history-status))
    (recur)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Main View

(defn main-tab [data owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [history       (om/get-shared owner :history)
            refresh       (om/get-shared owner :refresh)
            property-chan (om/get-shared owner :property-chan)
            m             (mult (history/set-chan! history (chan)))
            tap-history   #(tap m (chan))]

        ;; handle navigation changes
        (history-loop (tap-history) data)

        ;; go loop listening for requests to refresh tables
        (go-loop []
          (let [{:keys [event id]}  (<! refresh)]

            (cond
             (= event :programmes)     (data/fetch-programmes data)
             (= event :projects)       (data/fetch-projects (-> @data :active-components :ids :programmes) data)
             (= event :properties)     (data/fetch-properties (-> @data :active-components :ids :projects) data)
             (= event :property)       (data/fetch-property (-> @data :active-components :ids :properties) data)
             (= event :new-property)   (data/fetch-new-property id data)))
          (recur))))
    om/IRender
    (render [_]
      (html
       [:div
        (om/build programmes/programmes-div (:programmes data))
        (om/build projects/projects-div (:projects data))
        (om/build properties/properties-div (:properties data))
        (om/build property-details/property-details-div (:properties data))]))))
