(ns kixi.hecuba.data.entities
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn- parse-entity [session x]
  ;; TODO - do we need to parse?
  (identity x))

(defn get-by-id
  ([session entity_id]
     (->> (db/execute session
                      (hayt/select :entities
                                   (hayt/where [[= :id entity_id]])))
          (mapv (partial parse-entity session))
          first)))

(defn get-by-map
  ([session m]
     (get-by-map session :entity_id))
  ([session m key]
     (get-by-id session (get m key))))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :devices))
          (mapv (partial parse-entity session))))
  ([session entity_id]
     (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
          (mapv (partial parse-entity session)))))
