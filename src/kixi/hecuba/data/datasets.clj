(ns kixi.hecuba.data.datasets
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn decode [dataset]
  (-> dataset
      (dissoc :user_id)))

(defn get-by-id
  ([session entity_id]
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
  ([session dataset]
     (db/execute session (hayt/insert :datasets (hayt/values dataset)))))

(defn update [session entity_id name dataset]
  (db/execute session (hayt/update :datasets
                                   (hayt/set-columns (dissoc dataset :device_id :entity_id :name))
                                   (hayt/where [[= :entity_id entity_id]
                                                [= :name name]]))))
