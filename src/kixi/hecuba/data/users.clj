(ns kixi.hecuba.data.users
    (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.data :refer [parse-item parse-list]]))

(defn- parse-user [session m]
  (identity m))

(defn get
  ([session m]
     (->> (db/execute session
                      (hayt/select :devices
                                   (hayt/where [[= :id (:device_id m)]])))
          (mapv (partial parse-user session))
          first)))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :devices))
          (mapv (partial parse-user session))))
  ([session entity_id]
     (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
          (mapv (partial parse-user session)))))
