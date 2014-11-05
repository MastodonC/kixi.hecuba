(ns kixi.hecuba.data.datasets
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.devices :as devices]
            [kixi.hecuba.data.sensors :as sensors]))

(defn decode [dataset]
  (-> dataset
      (assoc :dataset_id (:id dataset))
      (dissoc :user_id :id)))

(defn encode [dataset]
  (-> dataset
      (assoc :id (:dataset_id dataset))
      (dissoc :dataset_id)))

(defn get-by-id
  [dataset_id session]
  (->> (db/execute session
                   (hayt/select :datasets
                                (hayt/where [[= :id dataset_id]])))
       first
       decode))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :datasets))
          (map decode)))
   ([entity_id session]
     (->> (db/execute session
                     (hayt/select :datasets
                                  (hayt/where [[= :entity_id entity_id]])))
          (map decode))))

(defn insert
  ([dataset session]
     (db/execute session (hayt/insert :datasets (hayt/values (encode dataset))))))

(defn update
  ([{:keys [dataset dataset_id]} session]
     (update dataset dataset_id session))
  ([dataset dataset_id session]
     (db/execute session (hayt/update :datasets
                                      (hayt/set-columns (dissoc (encode dataset) :device_id :entity_id :id :dataset_id))
                                      (hayt/where [[= :id dataset_id]])))))

(defn insert-sensor [synthetic-sensor synthetic-sensor-metadata session]
  (db/execute session (hayt/insert :sensors (hayt/values synthetic-sensor)))
  (db/execute session (hayt/insert :sensor_metadata (hayt/values synthetic-sensor-metadata))))

(defn delete-sensors [device_id measurements? session]
  (let [sensors         (sensors/get-sensors device_id session)
        sensor-types    (map :type sensors)
        deleted-sensors (doall (map #(sensors/delete {:device_id device_id :type %} measurements? session) sensor-types))]
    {:sensors deleted-sensors}))

(defn delete
  ([dataset_id entity_id device_id measurements? session]
     (let [dataset         (db/execute session (hayt/delete :datasets
                                                            (hayt/where [[= :id dataset_id]])))
           device          (devices/get-by-id session device_id)
           deleted-sensors (delete-sensors device_id measurements? session)
           deleted-device  (db/execute session (hayt/delete :devices
                                                            (hayt/where [[= :id device_id]])))
           entity-response (db/execute session (hayt/delete :entities
                                                            (hayt/columns {:devices device_id})
                                                            (hayt/where [[= :id entity_id]])))]
       (merge deleted-sensors
              {:devices deleted-device :entities entity-response}))))
