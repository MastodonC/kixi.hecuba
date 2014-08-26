(ns kixi.hecuba.data.entities
  (:require [clojure.edn :as edn]
            [clojure.string :as string]
            [hickory.core :as hickory]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [cheshire.core :as json]
            [kixi.hecuba.data.devices :as devices]
            [kixi.hecuba.webutil :refer (update-stringified-list)]
            [clojure.tools.logging :as log]
            [hickory.core :as hickory]
            [clojure.tools.logging :as log]
            [kixi.hecuba.schema-utils :as su]
            [schema.core :as s]))

(defn delete [entity_id session]
  (let [devices              (devices/get-devices session entity_id)
        device_ids           (map :device_id devices)
        delete-measurements? true
        deleted-devices      (doall (map #(devices/delete % delete-measurements? session) device_ids))
        deleted-entity       (db/execute
                              session
                              (hayt/delete :entities
                                           (hayt/where [[= :id entity_id]])))]
    {:devices deleted-devices
     :entities deleted-entity}))

(defn- encode-tech-icons [property_data]
  (if-let [icons (:technology_icons property_data)]
    (assoc
        property_data
      :technology_icons (->> (hickory/parse-fragment icons)
                             (keep (fn [ti] (-> ti hickory/as-hickory :attrs :src)))
                             (map #(clojure.string/replace % ".jpg" ".png"))))
    property_data))

(defn encode-map-vals [m]
  (into {} (map (fn [[k v]] [k (json/encode v)]) m)))

(defn encode [entity]
  (-> entity
      (assoc :id (:entity_id entity))
      (dissoc entity :device_ids :entity_id)
      (cond-> (:notes entity) (update-stringified-list :notes)
              (:metering_point_ids entity) (update-stringified-list :metering_point_ids)
              (:devices entity) (assoc :devices (encode-map-vals (:devices entity)))
              (:property_data entity) (assoc :property_data (json/encode (:property_data entity))))))

(defn decode-list [entity key]
  (->> (get entity key [])
       (mapv #(json/parse-string % keyword))
       (assoc entity key)))

(defn decode-map [entity key]
  (->> (get entity key {})
       (map (fn [[k v]] (vector k (json/parse-string v keyword))))
       (into {})
       (assoc entity key)))

(defn decode-edn-map [entity key]
  (->> (get entity key {})
       (map (fn [[k v]] (vector k (edn/read-string v))))
       (into {})
       (assoc entity key)))

(defn decode-entry [entity key]
  (try
    (assoc entity key (json/parse-string (get entity key) keyword))
    (catch Throwable t
      (log/errorf t "Died trying to parse key %s on %s" key entity)
      (throw t))))

(defn split-icon-string [icon-string]
  (if (re-find #"<img" icon-string)
    (->> (hickory/parse-fragment icon-string)
         (map (fn [ti] (-> ti hickory/as-hickory :attrs :src)))
         (keep identity)
         (map #(string/replace % ".jpg" ".png")))
    (->> (string/split icon-string #" ")
         (map #(string/replace % ".jpg" ".png")))))

(defn decode-tech-icons [entity]
  (let [ks [:property_data :technology_icons]]
    (if-let [dirty-icons (get-in entity ks)]
      (assoc-in entity ks (split-icon-string dirty-icons))
      entity)))

(defn decode-property-data [entity]
  (-> entity
      (assoc :property_data
        (try
          (json/parse-string (:property_data entity) keyword)
          (catch Throwable t
            (log/errorf t "Could not parse property_data %s for entity %s" (:property_data entity) entity)
            {})))
      (decode-tech-icons)))

(defn decode [entity]
  (-> entity
      (assoc :entity_id (:id entity))
      (dissoc :id)
      (cond-> (:property_data entity) (decode-property-data)
              (:notes entity) (decode-list :notes)
              (:documents entity) (decode-list :documents)
              (:photos entity) (decode-list :photos)
              (:devices entity) (decode-edn-map :devices)
              (:metering_point_ids entity) (decode-entry :metering_point_ids))))

;; See hecuba-schema.cql
(def InsertableEntity
  {:entity_id s/Str
   (s/optional-key :address_country) s/Str
   (s/optional-key :address_county) s/Str
   (s/optional-key :address_region) s/Str
   (s/optional-key :address_street_two) s/Str
   (s/optional-key :calculated_fields_labels) {s/Str s/Str}
   (s/optional-key :calculated_fields_last_calc) {s/Str s/Str} ;; sc/ISO-Date-Time
   (s/optional-key :calculated_fields_values) {s/Str s/Str}
   (s/optional-key :csv_uploads) [s/Str]
   (s/optional-key :devices) {s/Str s/Any}
   (s/optional-key :documents) [s/Str]
   (s/optional-key :metering_point_ids) [s/Str]
   (s/optional-key :name) s/Str
   (s/optional-key :notes) [s/Str]
   (s/optional-key :photos) [s/Str]
   (s/optional-key :project_id) s/Str
   (s/optional-key :property_code) s/Str
   (s/optional-key :property_data) {s/Keyword s/Str}
   (s/optional-key :retrofit_completion_date) s/Str ;; sc/ISO-Date-Time
   :user_id s/Str})

(defn insert [session entity]
  (let [insertable-entity (su/select-keys-by-schema entity InsertableEntity)]
    (s/validate InsertableEntity insertable-entity)
    (db/execute session (hayt/insert :entities (hayt/values (encode insertable-entity))))))

(defn update [session id entity]
  (let [insertable-entity (su/select-keys-by-schema entity InsertableEntity)]
    (s/validate InsertableEntity insertable-entity)
       (db/execute session (hayt/update :entities
                                        (hayt/set-columns (dissoc (encode insertable-entity) :id))
                                        (hayt/where [[= :id id]])))))

(defn get-by-id
  ([session entity_id]
     (-> (db/execute session
                     (hayt/select :entities
                                  (hayt/where [[= :id entity_id]])))
         first
         decode)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :entities))
          (map decode)))
  ([session project_id]
     (->> (db/execute session (hayt/select :entities (hayt/where [[= :project_id project_id]])))
          (map decode))))

(defn add-image [session id key]
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:photos [+ [(json/encode key)]]})
                                     (hayt/where [[= :id id]]))))

(defn add-document [session id key]
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:documents [+ [(json/encode key)]]})
                                     (hayt/where [[= :id id]]))))

(defn has-location? [{:keys [property_data] :as e}]
  (when (and property_data
             (contains? property_data :latitude)
             (contains? property_data :longitude))
    e))

(defn get-entities-having-location [session]
  (let [data
        (->> (db/execute session (hayt/select :entities) (hayt/columns [:id :name :property_data]))
             (map decode)
             (keep has-location?))]
    data))
