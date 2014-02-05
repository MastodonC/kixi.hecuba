{:jig/components
 {
  :hecuba/refstore
  {:jig/component kixi.hecuba.dev/RefStore
   :jig/project "../kixi.hecuba/project.clj"
   :doc "An in-memory store for testing, this binds Commander and Querier in the system at :querier and :commander respectively"
   }

  :hecuba/cassandra-direct-store
  {:jig/component kixi.hecuba.dev/RefStore
   :jig/project "../kixi.hecuba/project.clj"
   :doc "An in-memory store for testing, this binds Commander and Querier in the system at :querier and :commander respectively"
   }

  :hecuba/kafka
  {:jig/component kixi.hecuba.kafka/Kafka
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies []
   :producer  {"metadata.broker.list" "localhost:9092"
               "serializer.class" "kafka.serializer.DefaultEncoder"
               "Partitioner.Class" "kafka.producer.DefaultPartitioner"}
   :consumer  {"zookeeper.connect"  "localhost:2181"
               "group.id" "clj-kafka.consumer"
               "auto.offset.reset" "smallest"
               "auto.commit.enable" "true"}
   }

  :hecuba/db
  {:jig/component kixi.hecuba.db/Database
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies []
   :hosts ["127.0.0.1"]
   :keyspace "m7"
   :port 9161
   :credentials {:username "vmfest" :password "vmfest"}}

  :hecuba/website
  {:jig/component kixi.hecuba.web/Website
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/refstore :hecuba/kafka]
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

  :hecuba.dev/example-data-loader
  {:jig/component kixi.hecuba.dev/ExampleDataLoader
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/webserver :hecuba/routing]}

  :hecuba.dev/liberator-client-tests
  {:jig/doc "For sanity while developing, run some tests to make sure our Liberator resources are working."
   :jig/component kixi.hecuba.dev/HttpClientChecks
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba.dev/example-data-loader]}

}}
