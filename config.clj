{:jig/components
 {
  :hecuba/store
  {:jig/component kixi.hecuba.dev/RefBasedStoreComponent
   :jig/project "../kixi.hecuba/project.clj"
   }

  :hecuba/channel
  {:jig/component jig.async/Channel
   :jig/project "../kixi.hecuba/project.clj"
   }

  :hecuba/kafka
  {:jig/component kixi.hecuba.kafka/Kafka
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies []
   :producer "localhost:9092"
   :consumer "localhost:2181"
  }
  
  :hecuba/website
  {:jig/component kixi.hecuba.web/Website
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies [:hecuba/store :hecuba/channel]
   :name "Bruce!!"
;;   :jig.web/context "/services"
   }

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
