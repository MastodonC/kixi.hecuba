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
            [clojure.tools.logging :as log]))

(defn delete [entity_id session]
  (let [devices              (devices/get-devices session entity_id)
        device_ids           (map :id devices)
        delete-measurements? true
        deleted-devices      (doall (map #(devices/delete % delete-measurements? session) device_ids))
        deleted-entity       (db/execute
                              session
                              (hayt/delete :entities
                                           (hayt/where [[= :id entity_id]])))]
    {:devices deleted-devices
     :entities deleted-entity}))

(defn- tech-icons [property_data]
  (if-let [icons (:technology_icons property_data)]
    (assoc
        property_data
      :technology_icons (->> (hickory/parse-fragment icons)
                             (keep (fn [ti] (-> ti hickory/as-hickory :attrs :src)))
                             (map #(clojure.string/replace % ".jpg" ".png"))))
    property_data))

(defn encode-property-data [property_data]
  (-> property_data
   tech-icons))

(defn encode [entity]
  (cond-> (dissoc entity :device_ids)
          (get-in entity [:notes]) (update-stringified-list :notes)
          (get-in entity [:metering_point_ids]) (update-in [:metering_point_ids] str)
          (get-in entity [:property_data]) (update-in [:property_data] encode-property-data)))

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
  (let [icon-str
        (if (re-find #"<img" icon-string)
          (->> (hickory/parse-fragment icon-string)
               (map (fn [ti] (-> ti hickory/as-hickory :attrs :src)))
               (keep identity)
               (map #(string/replace % ".jpg" ".png")))
          (->> (string/split icon-string #" ")
               (map #(string/replace % ".jpg" ".png"))))]
    icon-str))

(defn scrub-icon-list [entity ks]
  (let [dirty-icons (get-in entity ks)]
    (assoc-in entity ks (split-icon-string dirty-icons))))

(defn tech-icons [entity]
  (if (get-in entity [:property_data :technology_icons])
    (scrub-icon-list entity [:property_data :technology_icons])
    entity))

(defn decode-property-data [entity]
  (-> entity
      (assoc :property_data (json/parse-string (:property_data entity) keyword))
      (tech-icons)))

(defn decode [entity]
  (cond-> entity
          (:property_data entity) (decode-property-data)
          (:notes entity) (decode-list :notes)
          (:documents entity) (decode-list :documents)
          (:photos entity) (decode-list :photos)
          (:devices entity) (decode-edn-map :devices)
          (:metering_point_ids entity) (decode-entry :metering_point_ids)))

(defn insert [session entity]
  (db/execute session (hayt/insert :entities (hayt/values (encode entity)))))

(defn update [session id entity]
  (db/execute session (hayt/update :entities
                                   (hayt/set-columns (encode (dissoc entity :id)))
                                   (hayt/where [[= :id id]]))))

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
                                     (hayt/set-columns {:photos [+ [key]]})
                                     (hayt/where [[= :id id]]))))

(defn add-document [session id key]
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:documents [+ [key]]})
                                     (hayt/where [[= :id id]]))))

(defn has-location? [{:keys [property_data] :as e}]
  (when (and property_data
             (contains? property_data :latitude)
             (contains? property_data :longitude))
    e))

(defn get-entities-having-location [session]
  (let [data
        (->> (db/execute session (hayt/select :entities) (hayt/columns [:id :name :property_data]))
             (map (fn [e] (update-in e [:property_data] #(json/decode % keyword))))
             (map encode)
             (keep has-location?))]
    data))
