(ns kixi.hecuba.data.devices
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item parse-list]]
            [kixi.hecuba.webutil :refer (stringify-values)]
            [cheshire.core :as json]
            [schema.core :as s]))

(def Device {(s/required-key :device_id) s/Str
             (s/optional-key :description)                (s/maybe s/Str)
             (s/optional-key :parent_id)                  (s/maybe s/Str)
             (s/optional-key :entity_id)                  (s/maybe s/Str)
             (s/optional-key :location)                   (s/maybe s/Str) ;; TODO - is really a nested map
             (s/optional-key :metadata)                   (s/maybe s/Str)
             (s/optional-key :privacy)                    (s/maybe s/Str)
             (s/optional-key :metering_point_id)          (s/maybe s/Str)})

(defn invalid? [device]
  (s/check Device device))

(def user-editable-keys [:id :description :entity_id
                        :location :metadata :metering_point_id
                        :name :parent_id :privacy])

(defn parse-device [device session]
  (let [device_id (:id device)
        sensors   (sensors/->clojure device_id session)]
    (-> device
        (parse-item :metadata)
        (assoc :device_id device_id :readings sensors)
        (dissoc :id))))

(defn encode
  ([device]
     (encode device false))
  ([device remove-pk?]
                (-> device
                    (select-keys user-editable-keys)
                    (update-in [:location] json/encode)
                    (update-in [:metadata] json/encode)
                    (cond-> remove-pk? (dissoc :id))
                    stringify-values)))

(defn insert [session entity_id device]
  (let [id             (:id device)
        encoded-device (encode device)]
    (db/execute session (hayt/insert :devices
                                     (hayt/values encoded-device)))
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:devices [+ {id (str encoded-device)}]})
                                     (hayt/where [[= :id entity_id]])))))

(defn get-by-id [session id]
  (first (db/execute session (hayt/select :devices
                                          (hayt/where [[= :id id]])))))

(defn update
  ([session device]
     (update session (:entity_id device) (:id device) device))
  ([session entity_id id device]
     (let [encoded-device (encode device :remove-pk)
           entity_id (or entity_id (:entity_id (get-by-id session id)))]
       (db/execute session (hayt/update :devices
                                        (hayt/set-columns encoded-device)
                                        (hayt/where [[= :id id]])))
       (db/execute session (hayt/update :entities
                                        (hayt/set-columns {:devices [+ {id (str encoded-device)}]})
                                        (hayt/where [[= :id entity_id]]))))))

(defn delete-measurements [session device_id]
  (doall (->> (sensors/get-sensors device_id session)
              (map :type)
              (map #(sensors/delete-measurements session device_id %)))))

(defn delete-sensors [device_id measurements? session]
  (let [sensors         (sensors/get-sensors device_id session)
        sensor-types    (map :type sensors)
        deleted-sensors (doall (map #(sensors/delete {:device_id device_id :type %} measurements? session) sensor-types))]
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
  (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]]))))



(defn ->clojure [entity_id session]
  (let [devices (get-devices session entity_id)]
    (mapv #(parse-device % session) devices)))
