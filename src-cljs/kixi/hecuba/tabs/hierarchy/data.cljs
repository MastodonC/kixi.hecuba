(ns kixi.hecuba.tabs.hierarchy.data
  (:require [ajax.core :refer (GET)]
            [om.core :as om :include-macros true]
            [kixi.hecuba.common :refer (log interval) :as common]
            [kixi.hecuba.tabs.slugs :as slugs]
            [clojure.string :as str]
            [cljs-time.format :as tf]
            [cljs-time.core :as t]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Data Fetchers

;; Extract and flatten sensors from properties data
(defn flatten-device [device]
  (let [device-keys   (->> device keys (remove #(= % :readings)))
        parent-device (select-keys device device-keys)
        readings      (:readings device)]
    (map #(assoc % :parent-device parent-device
                 :id (str (:type %) "~" (:device_id %))) readings)))

(defn extract-sensors [devices]
  (vec (mapcat flatten-device devices)))

(defn get-property-details [selected-property-id properties]
  (->>  properties
        :data
        (filter #(= (:entity_id %) selected-property-id))
        first))

(defn fetch-sensors [selected-property-id properties]
  (if selected-property-id
    (if-let [property-details (get-property-details selected-property-id properties)]
      (let [editable (:editable property-details)
            sensors  (extract-sensors (:devices property-details))]
        (map #(assoc % :editable editable) sensors))
      [])
    []))

(defn fetch-devices [selected-property-id properties]
   (if selected-property-id
    (if-let [property-details (get-property-details selected-property-id properties)]
      (let [editable (:editable property-details)
            devices  (:devices property-details)]
        (map #(assoc % :editable editable) devices))
      [])
    []))

(defn fetch-programmes
  ([data error-handler]
     (om/update! data [:programmes :fetching] :fetching)
     (GET (str "/4/programmes/")
          {:handler (fn [x]
                      (log "Fetching programmes.")
                      (let [programme-id (-> @data :active-components :ids :programmes)]
                        (om/update! data [:programmes :data] (mapv (fn [programme]
                                                                     (-> (slugs/slugify-programme programme)
                                                                         (cond-> programme-id (assoc :selected (if (= programme-id (:programme_id programme))
                                                                                                                 true
                                                                                                                 false)))))
                                                                   x))
                        (when programme-id
                          (om/update! data [:projects :can-add-projects] (-> (filter #(= (:programme_id %) programme-id) x)
                                                                             first
                                                                             :editable)))
                        (om/update! data [:programmes :fetching] (if (empty? x) :no-data :has-data))))
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
                       (let [project-id (-> @data :active-components :ids :projects)]
                         (om/update! data [:projects :data] (mapv (fn [project]
                                                                    (-> (slugs/slugify-project project)
                                                                        (cond-> project-id (assoc :selected (if (= project-id (:project_id project))
                                                                                                              true
                                                                                                              false)))))
                                                                  x))
                         (when project-id
                           (om/update! data [:properties :can-add-properties] (-> (filter #(= (:project_id %) project-id) x)
                                                                                  first
                                                                                  :editable))))
                       (om/update! data [:projects :fetching] (if (empty? x) :no-data :has-data)))
                     error-handler))
  ([programme-id data]
     (fetch-projects programme-id data (fn [{:keys [status status-text]}]
                                         (om/update! data [:projects :fetching] :error)
                                         (om/update! data [:projects :error-status] status)
                                         (om/update! data [:projects :error-text] status-text)))))

(defn get-units [property-id selected-sensor-ids properties]
  (let [sensors-hashmap      (into #{} (str/split selected-sensor-ids #";"))
        sensors-for-property (fetch-sensors property-id properties)
        selected-sensors     (filter (fn [sensor] (some #(= (:id sensor) %) sensors-hashmap))
                                     sensors-for-property)]
    (into {} (map #(hash-map (:id %) (:unit %)) selected-sensors))))

(defn fetch-properties
  ([project-id data error-handler]
     (om/update! data [:properties :fetching] :fetching)
     (GET (str "/4/projects/" project-id "/entities/")
          {:handler  (fn [x]
                       (let [entities (:entities x)]
                         (log "Fetching properties for project: " project-id)
                         (let [property-id (-> @data :active-components :ids :properties)
                               sensors     (-> @data :active-components :ids :sensors)]
                           (om/update! data [:properties :data]
                                       (mapv (fn [property]
                                               (-> (slugs/slugify-property property)
                                                   (cond-> property-id (assoc :selected (if (= property-id (:entity_id property))
                                                                                          true
                                                                                          false)))))
                                             entities))
                           (when (seq property-id)
                             (om/update! data [:properties :selected-property :property] (get-property-details property-id
                                                                                                               (-> @data
                                                                                                                   :properties))))
                           (when (seq sensors)
                             (om/update! data [:properties :chart :units] (get-units property-id sensors (-> @data :properties)))))
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
     (om/update! data [:user :fetching] :fetching)
     (GET (str "/4/usernames/")
          {:handler  (fn [x]
                       (om/update! data [:user :data] x)
                       (om/update! data [:user :fetching] (if (empty? x) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([data]
     (fetch-usernames data (fn [{:keys [status status-text]}]
                             (om/update! data [:user :fetching] :error)
                             (om/update! data [:user :error-status] status)
                             (om/update! data [:user :error-text] status-text)))))


(defn update-entity [entity refreshed-entity entity_id]
  (if (= entity_id (:entity_id entity))
    (-> (slugs/slugify-property refreshed-entity)
        (assoc :selected true))
    entity))

(defn fetch-property
  ([entity_id data error-handler]
     (om/update! data [:properties :fetching] :fetching)
     (GET (str "/4/entities/" entity_id)
          {:handler (fn [entity]
                      (log "Fetching property for id: " entity_id)
                      (om/update! data [:properties :data] (mapv #(update-entity % entity entity_id)(-> @data :properties :data)))
                      (om/update! data [:properties :selected-property :property] (-> (slugs/slugify-property entity)
                                                                                      (assoc :selected true)))
                      (om/update! data [:properties :fetching] (if (empty? entity) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([entity_id data]
     (fetch-property entity_id data
                     (fn [{:keys [status status-text]}]
                       (om/update! data [:properties :fetching] :error)
                       (om/update! data [:properties :error-status] status)
                       (om/update! data [:properties :error-text] status-text)))))

(defn fetch-new-property
  ([entity_id data error-handler]
     (om/update! data [:properties :fetching] :fetching)
     (GET (str "/4/entities/" entity_id)
          {:handler (fn [entity]
                      (log "Fetching new property for id: " entity_id)
                      (om/update! data [:properties :data] (conj (-> @data :properties :data) entity))
                      (om/update! data [:properties :fetching] (if (empty? entity) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([entity_id data]
     (fetch-new-property entity_id data
                     (fn [{:keys [status status-text]}]
                       (om/update! data [:properties :fetching] :error)
                       (om/update! data [:properties :error-status] status)
                       (om/update! data [:properties :error-text] status-text)))))

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

(defn date->amon-timestamp [date]
  (->> date
       (tf/parse (tf/formatter "yyyy-MM-dd"))
       (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss"))))

(defn pad-end-date [date]
  (let [timestamp (tf/parse (tf/formatter "yyyy-MM-dd") date)
        padded    (t/plus timestamp (t/days 1))]
    (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss") padded)))

(def amon-date (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ"))

(defn get-description [sensors measurement]
  (-> (filter #(= (:sensor measurement) (:id %)) sensors) first :parent-device :description))

(defn parse
  "Enriches measurements with unit and description of device and parses timestamp into a JavaScript Date object"
  [measurements units sensors]
  (map (fn [measurements-seq] (map #(assoc % :unit (get units (-> % :sensor))
                                           :description (get-description sensors %)
                                           :timestamp (tf/parse amon-date (:timestamp %))) measurements-seq)) measurements))

(defn fetch-measurements [properties entity_id sensors start-date end-date]
  (log "Fetching measurements for sensors: " sensors)
  (om/update! properties [:chart :fetching] true)
  (om/update! properties [:chart :measurements] [])
  (doseq [sensor sensors]
    (let [[type device_id] (str/split sensor #"~")
          end (if (= start-date end-date) (pad-end-date end-date) (date->amon-timestamp end-date))
          start-date (date->amon-timestamp start-date)
          measurements-type (interval start-date end)
          url (url-str start-date end entity_id device_id type measurements-type)]
      (GET url {:handler (fn [response]
                           (om/update! properties [:chart :rollup-type] measurements-type)
                           (om/transact! properties [:chart :measurements]
                                         (fn [measurements]
                                           (conj measurements
                                                 (into [] (map (fn [m]
                                                                 (assoc m :sensor sensor))
                                                               (:measurements response))))))
                           (let [units          (-> @properties :chart :units)
                                 sensors        (fetch-sensors entity_id @properties)
                                 measurements   (-> @properties :chart :measurements)
                                 all-series     (parse (filter seq measurements) units sensors)
                                 unit-groups    (group-by #(-> % first :unit) all-series)
                                 all-groups     (vals unit-groups)]
                             (om/update! properties [:chart :all-groups] all-groups)
                             (om/update! properties [:chart :fetching] false)))
                :headers {"Accept" "application/json"}
                :response-format :json
                :keywords? true}))))

(defn fetch-datasets [property-id path data]
  (log "Fetching datasets for property: " property-id)
  (GET (str "/4/entities/" property-id "/datasets/")
       {:handler (fn [response]
                   (om/update! data path (into [] response)))
        :headers {"Accept" "application/edn"}
        :response-format :edn
        :keywords? true}))

(defn fetch-upload-status [programme_id project_id entity_id data]
  (log "Fetching upload status for entity: " entity_id)
  (om/update! data [:properties :uploads :fetching] :fetching)
  (GET (str "/4/uploads/for-username/programme/" programme_id "/project/" project_id "/entity/" entity_id "/status")
       {:handler (fn [d]
                   (om/update! data [:properties :uploads :fetching] (if (seq d) :has-data :no-data))
                   (om/update! data [:properties :uploads :files] d))
        :headers {"Accept" "application/edn"}}))

(defn search-properties
  ([data page size query]
   (log "Searching for: " query)
   (om/update! data :fetching true)
   (GET (str "/4/entities/?q=" (js/encodeURIComponent query) "&page=" page "&size=" size)
        {:handler (fn [response]
                    (let [entities (:entities response)]
                      (om/update! data :data (into [] entities))
                      (om/update! data :stats {:total_hits (:total_hits response)
                                               :page (:page response)})
                      (om/update! data :fetching false)))
         :headers {"Accept" "application/edn"}
         :response-format :text}))
  ([data page size query sort]
   (log "Searching for: " query "with sort: " sort)
   (om/update! data :fetching true)
   (GET (str "/4/entities/?q=" (js/encodeURIComponent query) "&page=" page "&size=" size sort)
        {:handler (fn [response]
                    (let [entities (:entities response)]
                      (om/update! data :data (into [] entities))
                      (om/update! data :stats {:total_hits (:total_hits response)
                                               :page (:page response)})
                      (om/update! data :fetching false)))
         :headers {"Accept" "application/edn"}
         :response-format :text})))
