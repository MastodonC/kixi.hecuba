(ns kixi.hecuba.data.programmes
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.projects :as projects]))

(defn delete [programme_id session]
  (let [projects          (projects/get-all session programme_id)
        project_ids       (map :id projects)
        deleted-projects  (doall (map #(projects/delete % session) project_ids))
        deleted-programme (db/execute
                           session
                           (hayt/delete :programmes
                                        (hayt/where [[= :id programme_id]])))]
    {:projects deleted-projects
     :programme deleted-programme}))

(defn get-by-id
  ([session id]
     (first (db/execute session
                        (hayt/select :programmes
                                     (hayt/where [[= :id id]]))))))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :programmes)))))
