(ns kixi.hecuba.data.entities
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn- parse-entity [session x]
  ;; TODO - do we need to parse?
  (identity x))

(defn get
  ([session m]
     (->> (db/execute session
                      (hayt/select :entities
                                   (hayt/where [[= :id (:entity_id m)]])))
          (mapv (partial parse-entity session))
          first)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :devices))
          (mapv (partial parse-entity session))))
  ([session entity_id]
     (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
          (mapv (partial parse-entity session)))))
