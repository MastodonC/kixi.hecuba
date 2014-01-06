{:jig/components
 {
  :hecuba/website
  {:jig/component kixi.hecuba.web/Website
   :jig/project "../kixi.hecuba/project.clj"
   :jig/dependencies []
   :name "Bruce!!"
;;   :jig.web/context "/services"
   }

  :hecuba/cljs-builder
  {:jig/component jig.cljs-builder/Builder
   :jig/project "../kixi.hecuba/project.clj"
   :output-dir "../kixi.hecuba/target/js"
   :output-to "../kixi.hecuba/target/js/main.js"
   :source-map "../kixi.hecuba/target/js/main.js.map"
   ;; :optimizations :whitespace
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

}}
