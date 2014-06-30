(ns kixi.hecuba.data.devices
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item parse-list]]
            [kixi.hecuba.webutil :refer (stringify-values)]
            [cheshire.core :as json]))

(defn parse-device [device session]
  (let [device_id (:id device)
        sensors   (sensors/->clojure device_id session)]
    (-> device
        (assoc :readings sensors)
        (parse-item :metadata)
        (assoc :device_id device_id)
        (dissoc :id))))

(defn encode [device]
  (-> device
      (dissoc :readings :device_id)
      (update-in [:location] json/encode)
      (update-in [:metadata] json/encode)
      stringify-values))

(defn insert [session entity_id device]
  (let [id (:id device)]
    (db/execute session (hayt/insert :devices
                                     (hayt/values (encode device))))
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:devices [+ {id (str device)}]})
                                     (hayt/where [[= :id entity_id]])))))

(defn update [session entity_id id device]
  (db/execute session (hayt/update :devices
                                   (hayt/set-columns (encode device))
                                   (hayt/where [[= :id id]])))
  (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:devices [+ {id (str device)}]})
                                     (hayt/where [[= :id entity_id]]))))

(defn delete [session entity_id id]
  (let [response1 (db/execute session (hayt/delete :devices
                                         (hayt/where [[= :id id]])))
        response2 (db/execute session (hayt/delete :sensors
                                         (hayt/where [[= :device_id id]])))
        response3 (db/execute session (hayt/delete :sensor_metadata
                                         (hayt/where [[= :device_id id]])))
        response4 (db/execute session (hayt/delete :entities
                                         (hayt/columns {:devices id})
                                         (hayt/where [[= :id entity_id]])))]
    [response1 response2 response3 response4]))

(defn get-devices [session entity_id]
  (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]]))))

(defn get-by-id [session id]
  (first (db/execute session (hayt/select :devices
                                          (hayt/where [[= :id id]])))))

(defn ->clojure [entity_id session]
  (let [devices (get-devices session entity_id)]
    (mapv #(parse-device % session) devices)))
