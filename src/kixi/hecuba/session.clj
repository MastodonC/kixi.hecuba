(ns kixi.hecuba.session
  (:require
   [clojure.tools.logging :as log]
   [clojure.edn :as edn]
   [ring.middleware.session.store :refer (SessionStore)]
   [qbits.hayt :as hayt]
   [clj-time.core :as t]
   [kixi.hecuba.storage.db :as db]))

(deftype CassandraStore [store]
  SessionStore
  (read-session [_ key]
    (if key
      (db/with-session [db-session (:hecuba-session store)]
        (log/debugf "Retrieving session for key: %s" key)
        (let [user-session (first (db/execute
                                   db-session
                                   (hayt/select :sessions
                                                (hayt/where [[= :id key]]))
                                   {:consitency :quorum}))]
          (log/debugf "Retrieved session %s" user-session)
          (clojure.edn/read-string (:data user-session))))
      {}))
  (write-session [_ key data]
    (log/debugf "Key: %s Data: %s" key (pr-str data))
    (let [key (or key (str (java.util.UUID/randomUUID)))
          session-data {:id key
                        :data (pr-str data)}]
      (log/debugf "Writing session: %s" session-data)
      (db/with-session [db-session (:hecuba-session store)]
        (db/execute
         db-session
         (hayt/update :sessions
                      (hayt/set-columns
                       (dissoc session-data :id))
                      (hayt/where [[= :id key]]))
         {:consitency :quorum}))
      key))
  (delete-session [_ key]
    (log/debugf "Deleting sessiong for key: %s" key)
    (db/with-session [db-session (:hecuba-session store)]
      (db/execute
       db-session
       (hayt/delete :sessions
                    (hayt/where [[= :id key]]))))
    nil))

(defn cassandra-store [store]
  (CassandraStore. store))
