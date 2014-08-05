(ns kixi.hecuba.tabs.hierarchy.data
  (:require [ajax.core :refer (GET)]
            [om.core :as om :include-macros true]
            [kixi.hecuba.common :refer (log) :as common]
            [kixi.hecuba.tabs.slugs :as slugs]))

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
  ([programme-id data error-handler]
     (om/update! data [:projects :fetching] :fetching)
     (GET (str "/4/programmes/" programme-id "/projects/")
          {:handler  (fn [x]
                       (log "Fetching projects for programme: " programme-id)
                       (om/update! data [:projects :data] (mapv slugs/slugify-project x))
                       (om/update! data [:projects :fetching] (if (empty? x) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([programme-id data]
     (fetch-projects programme-id data (fn [{:keys [status status-text]}]
                                         (om/update! data [:projects :fetching] :error)
                                         (om/update! data [:projects :error-status] status)
                                         (om/update! data [:projects :error-text] status-text)))))

(defn fetch-properties
  ([project-id data error-handler]
     (om/update! data [:properties :fetching] :fetching)
     (GET (str "/4/projects/" project-id "/properties/")
          {:handler  (fn [x]
                       (log "Fetching properties for project: " project-id)
                       (om/update! data [:properties :data] (mapv slugs/slugify-property x))
                       (om/update! data [:properties :fetching] (if (empty? x) :no-data :has-data)))
           :error-handler error-handler
           :headers {"Accept" "application/edn"}
           :response-format :text}))
  ([project-id data]
     (fetch-properties project-id data
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
        (filter #(= (:id %) selected-property-id))
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
