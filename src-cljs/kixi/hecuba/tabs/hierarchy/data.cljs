(ns kixi.hecuba.tabs.hierarchy.data
  (:require [ajax.core :refer (GET)]
            [om.core :as om :include-macros true]
            [kixi.hecuba.common :refer (log interval) :as common]
            [kixi.hecuba.tabs.slugs :as slugs]
            [clojure.string :as str]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Fetchers

(defn fetch-programmes
  ([data error-handler]
     (om/update! data [:programmes :fetching] :fetching)
     (GET (str "/4/programmes/")
          {:handler  (fn [x]
                       (log "Fetching programmes.")
                       (om/update! data [:programmes :data] (mapv slugs/slugify-programme x))
                       (om/update! data [:programmes :fetching] (if (empty? x) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([data]
     (fetch-programmes data (fn [{:keys [status status-text]}]
                              (om/update! data [:programmes :fetching] :error)
                              (om/update! data [:programmes :error-status] status)
                              (om/update! data [:programmes :error-text] status-text)))))

(defn fetch-projects
  ([programme-id data handler error-handler]
     (om/update! data [:projects :fetching] :fetching)
     (GET (str "/4/programmes/" programme-id "/projects/")
          {:handler handler
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([programme-id data error-handler]
     (fetch-projects programme-id data
                     (fn [x]
                       (log "Fetching projects for programme: " programme-id)
                       (om/update! data [:projects :data] (mapv slugs/slugify-project x))
                       (om/update! data [:projects :fetching] (if (empty? x) :no-data :has-data)))
                     error-handler))
  ([programme-id data]
     (fetch-projects programme-id data (fn [{:keys [status status-text]}]
                                         (om/update! data [:projects :fetching] :error)
                                         (om/update! data [:projects :error-status] status)
                                         (om/update! data [:projects :error-text] status-text)))))

(defn fetch-properties
  ([project-id data error-handler]
     (om/update! data [:properties :fetching] :fetching)
     (GET (str "/4/projects/" project-id "/entities/")
          {:handler  (fn [x]
                       (let [entities (:entities x)]
                         (log "Fetching properties for project: " project-id)
                         (om/update! data [:properties :data] (mapv slugs/slugify-property entities))
                         (om/update! data [:properties :fetching] (if (empty? entities) :no-data :has-data))))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([project-id data]
     (fetch-properties project-id data
                       (fn [{:keys [status status-text]}]
                         (om/update! data [:properties :fetching] :error)
                         (om/update! data [:properties :error-status] status)
                         (om/update! data [:properties :error-text] status-text)))))

(defn fetch-usernames
  ([data error-handler]
     (om/update! data [:users :fetching] :fetching)
     (GET (str "/4/usernames/")
          {:handler  (fn [x]
                       (om/update! data [:users :data] x)
                       (om/update! data [:users :fetching] (if (empty? x) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([data]
     (fetch-usernames data (fn [{:keys [status status-text]}]
                             (om/update! data [:users :fetching] :error)
                             (om/update! data [:users :error-status] status)
                             (om/update! data [:users :error-text] status-text)))))


(defn update-entity [entity refreshed-entity entity_id]
  (if (= entity_id (:entity_id entity))
    (slugs/slugify-property refreshed-entity)
    entity))

(defn fetch-property
  ([entity-id data error-handler]
     (om/update! data [:properties :fetching] :fetching)
     (GET (str "/4/entities/" entity-id)
          {:handler (fn [entity]
                      (om/update! data [:properties :data] (mapv #(update-entity % entity entity-id) (-> @data :properties :data)))
                      (om/update! data [:properties :fetching] (if (empty? entity) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([entity-id data]
     (fetch-property entity-id data
                     (fn [{:keys [status status-text]}]
                       (om/update! data [:properties :fetching] :error)
                       (om/update! data [:properties :error-status] status)
                       (om/update! data [:properties :error-text] status-text)))))

;; Extract and flatten sensors from properties data
(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (map #(assoc % :parent-device parent-device
                 :id (str (:type %) "-" (:device_id %))) readings)))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

(defn get-property-details [selected-property-id data]
  (->>  data
        :properties
        :data
        (filter #(= (:entity_id %) selected-property-id))
        first))

(defn fetch-sensors [selected-property-id data]
  (if selected-property-id
    (if-let [property-details (get-property-details selected-property-id data)]
      (let [editable (:editable property-details)
            sensors  (extract-sensors (:devices property-details))]
        (map #(assoc % :editable editable) sensors))
      [])
    []))

(defn fetch-devices [selected-property-id data]
   (if selected-property-id
    (if-let [property-details (get-property-details selected-property-id data)]
      (let [editable (:editable property-details)
            devices  (:devices property-details)]
        (map #(assoc % :editable editable) devices))
      [])
    []))

(defmulti url-str (fn [start end entity_id device_id type measurements-type] measurements-type))
(defmethod url-str :raw [start end entity_id device_id type _]
  (str "/4/entities/" entity_id "/devices/" device_id "/measurements/"
       type "?startDate=" start "&endDate=" end))
(defmethod url-str :hourly_rollups [start end entity_id device_id type _]
  (str "/4/entities/" entity_id "/devices/" device_id "/hourly_rollups/"
       type "?startDate=" start "&endDate=" end))
(defmethod url-str :daily_rollups [start end entity_id device_id type _]
  (str "/4/entities/" entity_id "/devices/" device_id "/daily_rollups/"
       type "?startDate=" start "&endDate=" end))

(defn fetch-measurements [data entity_id sensors start-date end-date]
  (log "Fetching measurements for sensors: " sensors)
  (doseq [sensor (str/split sensors #";")]
    (let [[type device_id] (str/split sensor #"-" )
          measurements-type (interval start-date end-date)
          url (url-str start-date end-date entity_id device_id type measurements-type)]
      (om/update! data [:chart :measurements] [])
      (om/update! data [:fetching :measurements] true)
      (GET url {:handler (fn [response]
                           (om/update! data [:chart :measurements]
                                       (concat (:measurements (:chart @data))
                                               (into []
                                                     (map (fn [m]
                                                            (assoc m "sensor" sensor))
                                                          (:measurements response)))))
                           (om/update! data [:fetching :measurements] false))
                :headers {"Accept" "application/json"}
                :response-format :json
                :keywords? true}))))
