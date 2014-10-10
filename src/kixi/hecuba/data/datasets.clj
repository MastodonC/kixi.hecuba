(ns kixi.hecuba.data.datasets
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.devices :as devices]
            [kixi.hecuba.data.sensors :as sensors]))

(defn decode [dataset]
  (-> dataset
      (dissoc :user_id)))

(defn get-by-id
  ([entity_id session]
     (->> (db/execute session
                     (hayt/select :datasets
                                  (hayt/where [[= :entity_id entity_id]])))
          (map decode)))
  ([session entity_id name]
     (-> (db/execute session
                     (hayt/select :datasets
                                  (hayt/where [[= :entity_id entity_id]
                                               [= :name name]])))
         first
         decode)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :datasets))
          (map decode))))

(defn insert
  ([dataset session]
     (db/execute session (hayt/insert :datasets (hayt/values dataset)))))

(defn update
  ([{:keys [dataset name entity_id]} session]
     (update dataset name entity_id session))
  ([dataset name entity_id session]
     (db/execute session (hayt/update :datasets
                                      (hayt/set-columns (dissoc dataset :device_id :entity_id :name))
                                      (hayt/where [[= :entity_id entity_id]
                                                   [= :name name]])))))
(defn insert-sensor [synthetic-sensor synthetic-sensor-metadata session]
  (db/execute session (hayt/insert :sensors (hayt/values synthetic-sensor)))
  (db/execute session (hayt/insert :sensor_metadata (hayt/values synthetic-sensor-metadata))))

(defn delete-sensors [device_id measurements? session]
  (let [sensors         (sensors/get-sensors device_id session)
        sensor-ids      (map :sensor_id sensors)
        deleted-sensors (doall (map #(sensors/delete {:device_id device_id :sensor_id %} measurements? session) sensor-ids))]
    {:sensors deleted-sensors}))

(defn delete
  ([name entity_id device_id measurements? session]
     (let [dataset         (db/execute session (hayt/delete :datasets
                                                            (hayt/where [[= :name name]
                                                                         [= :entity_id entity_id]])))
           device          (devices/get-by-id session device_id)
           deleted-sensors (delete-sensors device_id measurements? session)
           deleted-device  (db/execute session (hayt/delete :devices
                                                            (hayt/where [[= :id device_id]])))
           entity-response (db/execute session (hayt/delete :entities
                                                            (hayt/columns {:devices device_id})
                                                            (hayt/where [[= :id (:entity_id device)]])))]
       (merge deleted-sensors
              {:devices deleted-device :entities entity-response}))))
