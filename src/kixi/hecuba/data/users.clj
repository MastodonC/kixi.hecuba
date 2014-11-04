(ns kixi.hecuba.data.users
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            (cemerick.friend [credentials :as creds])
            [kixi.hecuba.data.api :as api]))

(defn encode [user]
  (-> user
      (dissoc :username :id)
      api/stringify-values
      (cond-> (:password user) (update-in [:password] (fn [password] (creds/hash-bcrypt password))))))

(defn decode [user]
  (-> user
      (dissoc :password :id)
      (update-in [:data] #(edn/read-string %))))

(defn get-by-username [session username]
  (first (db/execute session
                     (hayt/select :users
                                  (hayt/where [[= :username username]])))))

(defn get-usernames [session]
  (db/execute session (hayt/select :users (hayt/columns :username))))

(defn get-all [session]
  (->> (db/execute session (hayt/select :users))
       (map decode)))

(defn update [session username user]
  (db/execute session (hayt/update :users
                                   (hayt/set-columns (encode user))
                                   (hayt/where [[= :id username]])))) ;; FIXME at the moment id and username are the same

(defn get-by-uuid [session uuid]
  (first (db/execute session (hayt/select :users
                                          (hayt/columns :reset_uuid
                                                        :reset_timestamp
                                                        :username)
                                          (hayt/where [[= :reset_uuid uuid]])))))
