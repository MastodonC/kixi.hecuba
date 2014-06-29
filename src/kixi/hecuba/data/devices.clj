(ns kixi.hecuba.data.devices
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item parse-list]]))

(defn parse-device [device session]
  (let [device_id (:id device)
        sensors   (sensors/->clojure device_id session)]
    (-> device
        (assoc :readings sensors)
        (parse-item :metadata)
        (assoc :device_id device_id)
        (dissoc :id))))

(defn get-devices [entity_id session]
  (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]]))))

(defn ->clojure [entity_id session]
  (let [devices (get-devices entity_id session)]
    (mapv #(parse-device % session) devices)))
