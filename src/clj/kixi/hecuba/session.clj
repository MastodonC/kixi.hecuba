(ns kixi.hecuba.session
  (:require
   [clojure.tools.logging :as log]
   [clojure.edn :as edn]
   [ring.middleware.session.store :refer (SessionStore)]
   [qbits.hayt :as hayt]
   [clj-time.core :as t]
   [kixi.hecuba.storage.db :as db]))

(deftype CassandraStore [store read-session-cql write-session-cql]
  SessionStore
  (read-session [_ key]
    (if key
      (db/with-session [db-session (:hecuba-session store)]
        (log/debugf "Retrieving session for key: %s" key)
        (let [user-session (first (db/execute-prepared
                                   db-session
                                   read-session-cql
                                   {:values [key]}))]
          (log/debugf "Retrieved session %s" user-session)
          (clojure.edn/read-string (:data user-session))))
      {}))
  (write-session [_ key data]
    (log/debugf "Key: %s Data: %s" key (pr-str data))
    (let [key (or key (str (java.util.UUID/randomUUID)))]
      (log/debugf "Writing session id: %s data: %s" key data)
      (db/with-session [db-session (:hecuba-session store)]
        (db/execute-prepared
         db-session
         write-session-cql
         {:consistency :quorum
          :values [(pr-str data) key]}))
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
  (let [cassandra-session (:hecuba-session store)
        read-session-cql
        (db/prepare-statement cassandra-session "select * from sessions where id = ?;")
        write-session-cql
        (db/prepare-statement cassandra-session "update sessions set data = ? where id = ?;")]
    (CassandraStore. store read-session-cql write-session-cql)))
