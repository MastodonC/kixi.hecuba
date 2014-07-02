(ns kixi.hecuba.data.projects
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

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
