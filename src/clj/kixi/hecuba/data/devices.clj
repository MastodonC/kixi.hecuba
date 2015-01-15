(ns kixi.hecuba.data.devices
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item parse-list]]
            [kixi.hecuba.api :refer (stringify-values)]
            [cheshire.core :as json]
            [schema.core :as s]))
(def LatLong
  {:latitude s/Str
   :longitude s/Str})

(def Location
  (s/either
   LatLong
   (merge {:name s/Str} LatLong)
   {:name s/Str}
   {}))

(def Device {(s/required-key :device_id)                  s/Str
             (s/optional-key :description)                s/Str
             (s/optional-key :entity_id)                  s/Str
             (s/optional-key :name)                       (s/maybe s/Str)
             (s/optional-key :parent_id)                  (s/maybe s/Str)
             (s/optional-key :location)                   (s/maybe Location)
             (s/optional-key :metadata)                   (s/maybe {s/Keyword s/Any})
             (s/optional-key :privacy)                    (s/maybe s/Str)
             (s/optional-key :metering_point_id)          (s/maybe s/Str)
             (s/optional-key :synthetic)                  (s/maybe s/Bool)
             (s/optional-key :user_id)                    (s/maybe s/Str)})

(def user-editable-keys [:device_id :description :entity_id :synthetic
                         :location :metadata :metering_point_id
                         :name :parent_id :privacy])

(defn encode
  ([device]
     (encode device false))
  ([device remove-pk?]
                (-> device
                    (select-keys user-editable-keys)
                    (cond-> (:location device) (update-in [:location] json/encode))
                    (cond-> (:metadata device) (update-in [:metadata] json/encode))
                    (assoc :id (:device_id device))
                    (dissoc :device_id)
                    (cond-> remove-pk? (dissoc :id)))))

(defn decode
  ([device]
     (-> device
         (update-in [:location] json/decode)
         (update-in [:metadata] json/decode)
         (assoc :device_id (:id device))
         (dissoc :user_id :id)))
  ([device session]
     (-> device
         (assoc :readings (map #(dissoc % :user_id) (sensors/get-sensors (:id device) session)))
         (update-in [:location] json/decode)
         (update-in [:metadata] json/decode)
         (assoc :device_id (:id device))
         (dissoc :user_id :id))))

(defn insert [session entity_id device]
  (s/validate Device device)
  (let [device_id       (:device_id device)
        user_id         (:user_id device)
        encoded-device  (assoc (encode device) :user_id user_id)]
    (db/execute session (hayt/insert :devices
                                     (hayt/values encoded-device)))
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:devices [+ {device_id (str encoded-device)}]})
                                     (hayt/where [[= :id entity_id]])))))

(defn get-by-id [session id]
  (when-let [device (first (db/execute session (hayt/select :devices
                                                            (hayt/where [[= :id id]]))))]
      (decode device session)))

(defn update
  ([session device]
     (update session (:entity_id device) (:device_id device) device))
  ([session entity_id id device]
     (s/validate Device device)
     (let [user_id         (:user_id device)
           encoded-device  (assoc (encode device :remove-pk) :user_id user_id)
           entity_id (or entity_id (:entity_id (get-by-id session id)))]
       (when (seq encoded-device)
         (db/execute session (hayt/update :devices
                                          (hayt/set-columns encoded-device)
                                          (hayt/where [[= :id id]])))
         (db/execute session (hayt/update :entities
                                          (hayt/set-columns {:devices [+ {id (str encoded-device)}]})
                                          (hayt/where [[= :id entity_id]])))))))

(defn delete-measurements [session device_id]
  (doall (->> (sensors/get-sensors device_id session)
              (map :sensor_id)
              (map #(sensors/delete-measurements {:device_id device_id :sensor_id %} session)))))

(defn delete-sensors [device_id measurements? session]
  (let [sensors         (sensors/get-sensors device_id session)
        sensor-types    (map :sensor_id sensors)
        deleted-sensors (doall (map #(sensors/delete {:device_id device_id :sensor_id %} measurements? session) sensor-types))]
    {:sensors deleted-sensors}))

(defn delete
  ([device_id measurements? session]
     (let [device          (get-by-id session device_id)
           deleted-sensors (delete-sensors device_id measurements? session)
           deleted-device  (db/execute session (hayt/delete :devices
                                                            (hayt/where [[= :id device_id]])))
           entity-response (db/execute session (hayt/delete :entities
                                                            (hayt/columns {:devices device_id})
                                                            (hayt/where [[= :id (:entity_id device)]])))]
       (merge deleted-sensors
              {:devices deleted-device :entities entity-response}))))

(defn get-devices [session entity_id]
  (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
       (map #(decode % session))))

(defn ->clojure [entity_id session]
  (get-devices session entity_id))
