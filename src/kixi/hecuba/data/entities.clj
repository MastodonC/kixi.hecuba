(ns kixi.hecuba.data.entities
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [cheshire.core :as json]
            [kixi.hecuba.data.devices :as devices]
            [kixi.hecuba.webutil :refer (update-stringified-lists)]))

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

(defn encode [entity]
  (-> entity
      (dissoc :device_ids)
      (update-stringified-lists [:documents
                                 :photos
                                 :notes])
      (update-in [:metering_point_ids] str)
      (update-in [:property_data] json/encode)))

(defn insert [session entity]
  (db/execute session (hayt/insert :entities (hayt/values (encode entity)))))

(defn update [session id entity]
  (db/execute session (hayt/update :entities
                                   (hayt/set-columns (encode (dissoc entity :id)))
                                   (hayt/where [[= :id id]]))))

(defn get-by-id
  ([session entity_id]
     (first (db/execute session
                        (hayt/select :entities
                                     (hayt/where [[= :id entity_id]]))))))

(defn get-all
  ([session]
     (db/execute session (hayt/select :entities)))
  ([session project_id]
     (db/execute session (hayt/select :entities (hayt/where [[= :project_id project_id]])))))
