(ns kixi.hecuba.data.entities
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

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
