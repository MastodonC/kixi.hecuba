(ns kixi.hecuba.data.devices
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item parse-list]]))

(defn add-readings [session devices]
  (mapv #(assoc % :readings (sensors/->clojure (:id %) session)) devices))

(defn get-devices [entity_id session]
  (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]]))))

(defn ->clojure [entity_id session]
  (let [devices (get-devices entity_id session)]
    (->> devices
         (add-readings session)
         (map #(assoc % :device_id (:id %)))
         (map #(dissoc % :id)))))
