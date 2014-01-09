{:jig/components
 {
  :hecuba/refstore
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

  :hecuba/website
  {:jig/component kixi.hecuba.web/Website
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/refstore]
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
   :jig.web/context "/js/"
   }

  :hecuba/routing
  {:jig/component jig.bidi/Router
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/website :hecuba/cljs-server]
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
   :jig/dependencies [:hecuba/webserver]}

}}
