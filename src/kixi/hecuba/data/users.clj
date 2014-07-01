(ns kixi.hecuba.data.users
    (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item parse-list]]))

(defn- parse-user [session m]
  (identity m))

(defn get-by-username
  ([session username]
     (->> (db/execute session
                      (hayt/select :users
                                   (hayt/where [[= :username username]])))
          (mapv (partial parse-user session))
          first)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :users))
          (mapv (partial parse-user session)))))
