(ns kixi.hecuba.data.devices
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item]]))

(defn parse-device [session device]
  (let [device_id (:id device)
        sensors   (sensors/get-all session device_id)]
    (-> device
        (assoc :readings sensors)
        (parse-item :metadata)
        (assoc :device_id device_id)
        (dissoc :id))))

(defn get-by-id [session id]
  (->> (db/execute session
                   (hayt/select :devices
                                (hayt/where [[= :id id]])))
       (mapv (partial parse-device session))
       first))

(defn get-by-map
  ([session m]
     (get-by-map session m :device_id))
  ([session m key]
     (get-by-id (get m key))))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :devices))
          (mapv (partial parse-device session))))
  ([session entity_id]
     (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
          (mapv (partial parse-device session)))))
