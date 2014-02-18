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

  :hecuba/kafka
  {:jig/component kixi.hecuba.kafka/Kafka
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies []
   :producer {"metadata.broker.list" "localhost:9092"
              "serializer.class" "kafka.serializer.DefaultEncoder"
              "Partitioner.Class" "kafka.producer.DefaultPartitioner"}
   :consumer {"zookeeper.connect"  "localhost:2181"
              "group.id" "clj-kafka.consumer"
              "auto.offset.reset" "smallest"
              "auto.commit.enable" "true"}
   }

  ;; Malcolm: This is fine for grabbing config and assoc it into the
  ;; system, but problem is how to inject the system to other
  ;; components. The fact that nothing connects with this component is a
  ;; smell. See :hecuba.dev/cassandra-cluster,
  ;; :hecuba.dev/cassandra-session, :hecuba.dev/cassandra-schema &
  ;; :hecuba.dev/cassandra-store
  #_:hecuba/db
  #_{:jig/component kixi.hecuba.db/Database
     :jig/project "../kixi.hecuba/project.clj"
     :jig/dependencies []
     :hosts ["127.0.0.1"]
     :keyspace "m7"
     :port 9161
     :credentials {:username "vmfest" :password "vmfest"}}

  :hecuba/website
  {:jig/component kixi.hecuba.web/Website
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba.dev/cassandra-store :hecuba/kafka]
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
  {:jig/component kixi.hecuba.web.amon/ApiServiceV3
   :jig/dependencies []
   ;; TODO Malcolm fix the fact that this doesn't work because add-bidi-routes doesn't look for it
   ;;:jig.web/context "/3/"
   }

  :hecuba/routing
  {:jig/component jig.bidi/Router
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/cljs-server :amon/api :hecuba/website]
   ;; Optionally, route systems can be mounted on a sub-context
   ;;:jig.web/context "/services"
   }

  :hecuba/webserver
  {:jig/component jig.http-kit/Server
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/routing]
   :port 8000}

  :hecuba.dev/etl
  {
   :jig/component kixi.hecuba.dev.etl/CsvLoader
   :jig/project  "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/routing ; We used the routes to form our URIs
                      :hecuba/webserver ; Which we use to send data to the web server
                      :hecuba.dev/cassandra-schema ; which inserts data into our C* schema
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
