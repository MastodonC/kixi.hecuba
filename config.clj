{:jig/components
 {
  #_:hecuba.dev/refstore
  #_{:jig/component kixi.hecuba.dev/RefStore
     :jig/project "../kixi.hecuba/project.clj"
     :doc "An in-memory store for testing, this binds Commander and Querier in the system at :querier and :commander respectively"
     }

  :hecuba.dev/cassandra-cluster
  {:jig/component kixi.hecuba.dev/CassandraCluster
   :jig/project "../kixi.hecuba/project.clj"
   :doc "A Cassandra cluster, for dev purposes"
   :contact-points ["127.0.0.1"]
   :port 9042
   }

  :hecuba.dev/cassandra-session
  {:jig/component kixi.hecuba.dev/CassandraSession
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba.dev/cassandra-cluster]
   :doc "A Cassandra session"
   :keyspace :test
   }

  :hecuba.dev/cassandra-schema
  {:jig/component kixi.hecuba.dev/CassandraSchema
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba.dev/cassandra-session]
   :doc "A schema established in Cassandra, for schema development and testing purposes"
   }

  :hecuba.dev/cassandra-store
  {:jig/component kixi.hecuba.dev/CassandraDirectStore
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba.dev/cassandra-session :hecuba.dev/cassandra-schema]
   :doc "A store instance of Cassandra, for dev purposes"
   }

  :hecuba/website
  {:jig/component kixi.hecuba.web/Website
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba.dev/cassandra-store]
   :doc "This depends on types that satisfy Commander and Querier being
   bound in the system at :querier and :commander respectively" }

  :hecuba/cljs-builder
  {:jig/component jig.cljs-builder/Builder
   :jig/project "../kixi.hecuba/project.clj"
   :output-dir "../kixi.hecuba/target/js"
   :output-to "../kixi.hecuba/target/js/main.js"
   :source-map "../kixi.hecuba/target/js/main.js.map"
   :optimizations :none
   ;; :pretty-print true
   }

  :hecuba/cljs-server
  {:jig/component jig.bidi/ClojureScriptRouter
   :jig/dependencies [:hecuba/cljs-builder]
   :jig.web/context "/hecuba-js/"
   }

  :amon/api
  {:jig/component kixi.hecuba.amon/ApiServiceV3
   :jig/dependencies []
   :jig.web/context "/3"
   }

  ;; The reason why an API has been chosen for user admin (as opposed to
  ;; putting user creds directly in to C*) is because the area of user
  ;; admin is notoriously time-consuming to implement, and having
  ;; multiple 'routes' to creating users often leads to
  ;; discrepancies. Also, a design goal is to make the API the point at
  ;; which authorization is handled (including salting, hashing), rather
  ;; than the database. If all data is protected behind a Liberator API,
  ;; the Liberator can be the authorisation gateway too - in that case
  ;; only one implementation of authentiation/authorization can serve
  ;; for all deployment scenarios.
  :hecuba/user-admin-api
  {
   :jig/component kixi.hecuba.user/ApiService
   :jig/dependencies [:hecuba.dev/cassandra-store ; Need a commander to upsert users to
                      ]
   :doc "An API to facilitate the creation, update and deletion of users."
   ;; In production, set this to true, once initial users are loaded
   ;; Instead of true, it might be the set of roles allowed to access
   ;; this API (although I don't intend to implement this feature just
   ;; yet on the basis of YAGNI pending further discussions with the team)
   :restricted false
   }

  :hecuba/routing
  {:jig/component jig.bidi/Router
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/cljs-server :amon/api :hecuba/user-admin-api :hecuba/website]
   ;; Optionally, route systems can be mounted on a sub-context
   ;;:jig.web/context "/services"
   }

  :hecuba/webserver
  {:jig/component jig.http-kit/Server
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/routing]
   :port 8000}

  :hecuba/pipeline
  {:jig/component kixi.hecuba.pipeline/Pipeline
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba.dev/cassandra-session]}

  :hecuba/scheduler
  {:jig/component kixi.hecuba.scheduler/Scheduler
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/pipeline]
   :schedule {:process-job-schedule
              ; s m   h  d  M D
              {"0 0  10  *  * ?" {:dest :data-quality :type :median-calculation :period "INSTANT"}
               "0 30 10  *  * ?" {:dest :data-quality :type :median-calculation :period "CUMULATIVE"}
               "0 0  11  *  * ?" {:dest :calculated-datasets :type :difference-series}
               "0 30 11  *  * ?" {:dest :data-quality :type :mislabelled-sensors}
               "0 0  12  *  * ?" {:dest :data-quality :type :spike-check}}         
              }}

  :hecuba.dev/user-data
  {
   :jig/component kixi.hecuba.dev.etl/UserDataLoader
   :jig/project  "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/routing ; We used the routes to form our URIs
                      :hecuba/webserver ; Which we use to send data to the web server
                      :hecuba.dev/cassandra-schema ; which inserts data into our C* schema
                      ]
   :doc "ETL loading user data from edn. It's in config here for dev
   purposes but can eventually be put elsewhere and loaded up from a
   file."
   :users [{:name "Bob"
            :username "bob"
            :password "123465"}
           {:name "Alice"
            :username "alice"
            :password "password"
            ;; The admin role allows Alice to use the user api, if restricted
            :roles #{:admin}}]
   }

  ;; Commented while doing user store
  :hecuba.dev/etl
  {
     :jig/component kixi.hecuba.dev.etl/CsvLoader
     :jig/project  "../kixi.hecuba/project.clj"
     :jig/dependencies [:hecuba/routing ; We used the routes to form our URIs
                        :hecuba/webserver ; Which we use to send data to the web server
                        :hecuba.dev/cassandra-schema ; which inserts data into our C* schema
                        :hecuba.dev/user-data ; we need users to be loaded first, otherwise we won't be able to access the service
                        ]
     :doc "ETL loading contextual data from CSV files"
     :data-directory "../kixi.hecuba.migration/data"
     }

  ;; Deprecated
  #_:hecuba.dev/example-data-loader
  #_{:jig/component kixi.hecuba.dev/ExampleDataLoader
     :jig/project "../kixi.hecuba/project.clj"
     :jig/dependencies [:hecuba/webserver :hecuba/routing]}

  #_:hecuba.dev/liberator-client-tests
  #_{:jig/doc "For sanity while developing, run some tests to make sure our Liberator resources are working."
     :jig/component kixi.hecuba.dev/HttpClientChecks
     :jig/project "../kixi.hecuba/project.clj"
     :jig/dependencies [:hecuba.dev/example-data-loader]}

  }}
