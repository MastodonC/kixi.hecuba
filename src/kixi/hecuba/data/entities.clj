(ns kixi.hecuba.data.entities
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [cheshire.core :as json]
            [kixi.hecuba.data.devices :as devices]
            [kixi.hecuba.webutil :refer (update-stringified-list)]
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

(defn encode [entity]
  (cond-> (dissoc entity :device_ids)
          (get-in entity [:notes]) (update-stringified-list :notes)
          (get-in entity [:metering_point_ids]) (update-in [:metering_point_ids] str)
          (get-in entity [:property_data]) (update-in [:property_data] json/encode)))

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

(defn add-image [session id key]
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:photos [+ [key]]})
                                     (hayt/where [[= :id id]]))))

(defn add-document [session id key]
    (db/execute session (hayt/update :entities
                                     (hayt/set-columns {:documents [+ [key]]})
                                     (hayt/where [[= :id id]]))))
