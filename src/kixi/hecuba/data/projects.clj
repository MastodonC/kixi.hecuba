(ns kixi.hecuba.data.projects
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.entities :as entities]))

(defn delete [project_id session]
  (let [entities         (entities/get-all session project_id)
        entity_ids       (map :id entities)
        deleted-entities (doall (map #(entities/delete % session) entity_ids))
        deleted-project  (db/execute
                          session
                          (hayt/delete :projects
                                       (hayt/where [[= :id project_id]])))]
    {:devices deleted-entities
     :project deleted-project}))

(defn get-by-id
  ([session id]
     (first (db/execute session
                        (hayt/select :projects
                                     (hayt/where [[= :id id]]))))))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :projects))))
  ([session programme_id]
     (->> (db/execute session (hayt/select :projects (hayt/where [[= :programme_id programme_id]]))))))
