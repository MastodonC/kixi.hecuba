(ns kixi.hecuba.data.projects
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.entities :as entities]
            [kixi.hecuba.data.api :as api]))

(defn encode [project]
  (-> project
      (assoc :id (:project_id project))
      (dissoc :project_id)
      api/stringify-values))

(defn decode [project]
  (-> project
      (assoc :project_id (:id project))
      (dissoc :id :user_id)))

(defn delete [project_id session]
  (let [entities         (entities/get-all session project_id)
        entity_ids       (map :entity_id entities)
        deleted-entities (doall (map #(entities/delete % session) entity_ids))
        deleted-project  (db/execute
                          session
                          (hayt/delete :projects
                                       (hayt/where [[= :id project_id]])))]
    {:devices deleted-entities
     :entities entity_ids
     :project deleted-project}))

(defn get-by-id
  ([session id]
     (-> (db/execute session
                     (hayt/select :projects
                                  (hayt/where [[= :id id]])))
         first
         decode)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :projects))
          (map decode)))
  ([session programme_id]
     (->> (db/execute session (hayt/select :projects (hayt/where [[= :programme_id programme_id]])))
          (map decode))))

(defn insert
  ([session project]
     (db/execute session (hayt/insert :projects (hayt/values (encode project))))))

(defn update [session id project]
  (db/execute session (hayt/update :projects
                                   (hayt/set-columns (dissoc (encode project) :id))
                                   (hayt/where [[= :id id]]))))
