(ns kixi.hecuba.cassandra-test
  (:require
   [kixi.hecuba.db :as db]
   [clojurewerkz.cassaforte.client :as client]
   [clojurewerkz.cassaforte.query :as query]
   [clojurewerkz.cassaforte.cql :as cql])
  )

(defn uuid [] (java.util.UUID/randomUUID))

(let [session
      (db/create-db-session
       (-> (-> user/system :hecuba/db ::db/cluster )
           (dissoc :credentials)))
      ]
  (binding [client/*default-session* session]
    (cql/create-table "measurements"
                      (query/column-definitions {:device_id :uuid
                                                 :type :varchar
                                                 :month :varchar
                                                 :value :varchar
                                                 :error :varchar
                                                 :timestamp :timestamp
                                                 :primary-key [[:device_id :type :month] :timestamp]}))

    (let [id (uuid)]
      (cql/insert "measurements" {:device_id id
                                  :type :temperature
                                  :month "2014-01"
                                  :value "50C"
                                  :timestamp (java.util.Date.)
                                  })

      (println "The value is " (cql/select "measurements" (query/where :device_id id :type :temperature :month "2014-01")))
      )

    ;; Poll for the inserted value


    (cql/drop-table "measurements")
    )
  )
