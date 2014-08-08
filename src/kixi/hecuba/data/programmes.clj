(ns kixi.hecuba.data.programmes
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.projects :as projects]
            [kixi.hecuba.webutil :as webutil]))

(defn encode [programme]
  (-> programme
      (assoc :id (:programme_id programme))
      (dissoc :programme_id)
      webutil/stringify-values))

(defn decode [programme]
  (-> programme
      (assoc :programme_id (:id programme))
      (dissoc :id :user_id)))

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
     (-> (db/execute session
                     (hayt/select :programmes
                                  (hayt/where [[= :id id]])))
         first
         decode)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :programmes))
          (map decode))))

(defn update [session id programme]
  (db/execute session (hayt/update :programmes
                                   (hayt/set-columns (dissoc (encode programme) :id))
                                   (hayt/where [[= :id id]]))))

(defn insert
  ([session programme]
     (db/execute session (hayt/insert :programmes
                                      (hayt/values (encode programme))))))
