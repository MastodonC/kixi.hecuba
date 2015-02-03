(ns kixi.hecuba.api.entities.schema
  (:require [clojure.tools.logging :as log]
            [schema.core :as s]
            [schema.utils :as su]
            [kixi.amon-schema :as schema]
            [kixi.hecuba.data.entities :as data]
            [kixi.hecuba.time :as ht]
            [kixi.hecuba.api.parser :as parser]
            [kixi.hecuba.api.profiles.schema :as ps]
            [kixi.hecuba.storage.uuid :as uuid]))

(def CoreEntity
  {(s/optional-key :entity_id) (s/maybe s/Str)
   (s/optional-key :property_code) (s/maybe s/Str)
   (s/optional-key :project_id) (s/maybe s/Str)

   (s/optional-key :address_country) (s/maybe s/Str)
   (s/optional-key :address_county) (s/maybe s/Str)
   (s/optional-key :address_region) (s/maybe s/Str)
   (s/optional-key :address_street_two) (s/maybe s/Str)
   (s/optional-key :name) (s/maybe s/Str)
   (s/optional-key :retrofit_completion_date) (s/maybe s/Str) ;; sc/ISO-Date-Time
   (s/optional-key :user_id) (s/maybe s/Str)
   (s/optional-key :profile_data_event_type) (s/maybe s/Str)

   (s/optional-key :property_data) {s/Keyword (s/maybe s/Any)}

   (s/optional-key :csv_uploads) (s/maybe [s/Str])
   (s/optional-key :device_ids) (s/maybe [s/Str])
   (s/optional-key :devices) (s/maybe [s/Any])
   (s/optional-key :documents) (s/maybe [s/Str])
   (s/optional-key :metering_point_ids) (s/maybe [s/Str])
   (s/optional-key :notes) (s/maybe [s/Str])
   (s/optional-key :photos) (s/maybe [s/Str])
   (s/optional-key :profiles) (s/maybe [s/Any])

   ;; calculated fields
   (s/optional-key :calculated_fields_labels) {s/Any s/Str}
   (s/optional-key :calculated_fields_last_calc) {s/Any s/Str}
   (s/optional-key :calculated_fields_values) {s/Any s/Str}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema (not prismatic) used to rip the data out of the csvs to make
;; entities.
(def property-data-schema
  [:description
   :monitoring_policy
   :address_street
   :address_city
   :address_county
   :address_code
   :created_at
   :updated_at
   :address_country
   :terrain
   :address_region
   :degree_day_region
   :ownership
   :fuel_poverty
   :property_value
   :property_value_basis
   :retrofit_start_date
   :retrofit_completion_date
   :project_summary
   :energy_strategy
   :project_team
   :design_strategy
   :other_notes
   :property_type
   :property_type_other
   :built_form
   :built_form_other
   :age
   :construction_date
   :conservation_area
   :listed
   :address_street_two
   :property_code
   :monitoring_hierarchy
   :project_phase
   :construction_start_date
   :practical_completion_date
   :photo_file_name
   :photo_content_type
   :photo_file_size
   :photo_updated_at
   :completeness
   :entity_completeness_6m
   :latitude
   :longitude
   :technology_icons
   :address_code_masked])

(def content-only-schema
  [])

(def document-schema
  [:id
   :content-type
   :name
   :file_name])

(def photo-schema
  [:path])

(def entity-schema
  [:entity_id
   :address_country
   :address_county
   :address_region
   :address_street_two
   :name
   :project_id
   :user_id
   :property_code
   :retrofit_completion_date
   :profile_data_event_type
   {:name :property_data
    :type :nested-item
    :schema property-data-schema}
   {:name :documents
    :type :associated-items
    :schema document-schema}
   {:name :notes
    :type :associated-items
    :schema content-only-schema}
   {:name :photos
    :type :associated-items
    :schema photo-schema}])

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Posts to <project_id>/entities
;;
;; This should allow for a whole project of entities and profiles to
;; be uploaded at once
(defn entity? [m]
  (and (seq (vals m)) ;; must have data
       (or (nil? (:profile_data_event_type m))
           (= (.toLowerCase (:profile_data_event_type m)) "create"))))

(defn profile? [m]
  (let [event_type (get-in m [:profile_data :event_type])]
    (and (not (nil? event_type))
         (not= (.toLowerCase event_type) "create"))))

(defn fix-timestamp [m]
  (if-let [ts (:timestamp m)]
    (assoc m :timestamp (ht/hecuba-date-time-string ts))
    m))

(defn extract-entities-and-profiles [rows]
  {:entities (->> rows
                  (parser/csv->maps entity-schema)
                  (filter #(entity? %))
                  (map fix-timestamp))
   :profiles (->> rows
                  (parser/csv->maps ps/profile-schema)
                  (filter #(profile? %))
                  (map fix-timestamp))})

(defn entity-validation-failures [entity project_id]
  (if-let [validation-errors (s/check CoreEntity entity)]
    (do
      (log/debugf "Entity: %s validation errors: %s" (pr-str entity) (su/validation-error-explain validation-errors))
      {:received-entity entity
       :proposed-project_id project_id
       :validation-errors (su/validation-error-explain validation-errors)})
    nil))

(defn profile-validation-failures [profile entities]
  (let [validation-errors (str (s/check schema/Profile profile))]
    (when (seq validation-errors)
      (log/debugf "Profile validation errors: %s" validation-errors)
      (log/debug "Failed profile " (pr-str profile))
      {:received-profile  profile
       :validation-errors validation-errors})))

;; Check to see if the property already exists in the project. If so,
;; then use that entity_id, if not then generate a new one.
(defn add-entity-id [entity project_id]
  (let [entities (data/get-all project_id)]
    (-> (assoc entity :project_id project_id)
        (uuid/add-entity-id))))

;; Check to see if the property_code exists in the passed in entities,
;; if so then use that entity_id, if the entity_id exists, use the
;; event type and date to see if an profile event exists and if so,
;; then use that profile id, if not, then generate a new one.
(defn add-profile-ids [profile project_id]
  (-> (assoc profile :project_id project_id)
      uuid/add-entity-id
      uuid/add-profile-id))

(defn assoc-profiles [entities profiles]
  (let [property_codes (clojure.set/union (set (map :property_code entities))
                                          (set (map :property_code profiles)))
        profile-map    (->> profiles
                            (reduce (fn [acc n]
                                      (assoc acc
                                        (:property_code n)
                                        (conj (get acc (:property_code n)) n)))
                                    {})
                            vals
                            (map #(hash-map
                                   :property_code (-> % first :property_code)
                                   :profiles (vec %)))
                            (map #(vector (:property_code %) %))
                            (into {}))
        entity-map (->> entities
                        (map #(vector (:property_code %) %))
                        (into {}))]
    (map #(merge (get entity-map %) (get profile-map %)) property_codes)))

(defn malformed-data? [rows project_id]
  (let [{:keys [entities profiles]} (extract-entities-and-profiles rows)
        broken-entities (keep #(entity-validation-failures % project_id) entities)
        broken-profiles (keep #(profile-validation-failures % entities) profiles)]
    (if (or (seq broken-entities)
            (seq broken-profiles))
      [true {:entities entities
             :profiles profiles
             :malformed-msg "Uploaded data validation failed."
             :errors {:entities broken-entities
                      :profiles broken-profiles}
             ;; we need to put in the representation so that liberator
             ;; knows how to render this item
             :representation {:media-type "application/json"}}]
      [false {:entities (->> (assoc-profiles entities profiles)
                             (map #(assoc % :project_id project_id)))}])))

(defn malformed-multipart-data? [file-data project_id]
  (-> (parser/temp-file->rows file-data)
      (malformed-data? project_id)))

(defn malformed-entity-post?
  ([entity project_id]
     (let [validation-errors (entity-validation-failures entity project_id)]
       (if validation-errors
         [true {:entities [entity]
                :malformed-msg "Uploaded data validation failed"
                :errors {:entities [validation-errors]}
                :representation {:media-type "application/json"}}]
         [false {:entities [(assoc entity
                              :project_id project_id
                              :entity_id (or (:entity_id entity) (uuid/uuid-str)))]
                 :representation {:media-type "application/json"}}])))
  ([entity]
     (if-let [project_id (:project_id entity)]
       (malformed-entity-post? entity project_id)
       [true {:malformed-msg "Missing project_id"
              :representation {:media-type "application/json"}}])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Puts to an entity resource
(defn extract-entity [rows]
  (parser/csv->maps entity-schema rows))

(defn malformed-entity-put? [entity entity_id]
  (let [validation-failures (s/check CoreEntity entity)]
    (if validation-failures
      [true {:entity entity
             :malformed-msg "Uploaded data validation failed"
             :errors validation-failures
             :representation {:media-type "application/json"}}]
      [false {:entity entity
              :representation {:media-type "application/json"}}])))

(defn malformed-entity-csv-put? [entity entity_id]
  (-> (extract-entity entity)
      (malformed-entity-put? entity_id)))

(defn malformed-multipart-entity-put? [file-data entity_id]
  (-> (parser/temp-file->rows file-data)
      (malformed-multipart-entity-put? entity_id)))
