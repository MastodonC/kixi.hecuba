(ns kixi.hecuba.data.users
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn get-by-username [session username]
  (first (db/execute session
                     (hayt/select :users
                                  (hayt/where [[= :username username]])))))
