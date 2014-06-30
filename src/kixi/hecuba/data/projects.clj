(ns kixi.hecuba.data.projects
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn parse-project [session m]
  (identity m))

(defn get
  ([session m]
     (->> (db/execute session
                      (hayt/select :projects
                                   (hayt/where [[= :id (:project_id m)]])))
          (mapv (partial parse-project session))
          first)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :projects))
          (mapv (partial parse-project session))))
  ([session project_id]
     (->> (db/execute session (hayt/select :projects (hayt/where [[= :id project_id]])))
          (mapv (partial parse-project session)))))
