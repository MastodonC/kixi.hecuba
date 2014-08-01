(ns kixi.hecuba.data.projects
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.webutil :as webutil]
            [kixi.hecuba.data.entities :as entities]))

(defn encode [project]
  (-> project
      webutil/stringify-values))

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

(defn insert
  ([session project]
     (db/execute session (hayt/insert :projects (hayt/values project)))))

(defn update [session id project]
  (db/execute session (hayt/update :projects
                                   (hayt/set-columns (encode (dissoc project :id)))
                                   (hayt/where [[= :id id]]))))
