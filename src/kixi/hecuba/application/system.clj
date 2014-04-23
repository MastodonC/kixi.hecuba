(ns kixi.hecuba.application.system
  (:require
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :as log]

   ;; Hecuba custom components

   ;; Modular reusable components
   [modular.core :as mod]
   [modular.http-kit :refer (new-webserver)]
   #_[modular.ring :refer (resolve-handler-provider)]
   [modular.bidi :refer (new-bidi-ring-handler-provider #_resolve-routes-contributors)]
  ; [modular.cassandra :refer (new-session new-cluster)]

   kixi.hecuba.application.safe
   [kixi.hecuba.controller.pipeline :refer (new-pipeline)]
   [kixipipe.scheduler]
   [kixi.hecuba.queue :refer (new-queue)]
   [kixi.hecuba.data :refer (new-queue-worker)]
   [kixi.hecuba.web :refer (new-main-routes)]
   [kixi.hecuba.amon :refer (new-amon-api)]
   [kixi.hecuba.user :refer (new-user-api)]
   [kixi.hecuba.cljs :refer (new-cljs-routes)]
   [kixi.hecuba.transport.db :refer (new-cluster new-session new-direct-store)]

   [shadow.cljs.build :as cljs]

   ;; Misc
   clojure.tools.reader
   [clojure.pprint :refer (pprint)]
   [clojure.tools.reader.reader-types :refer (indexing-push-back-reader source-logging-push-back-reader)]
   [clojure.java.io :as io]))

(defn config []
  (let [f (io/file (System/getProperty "user.home") ".hecuba.edn")]
    (when (.exists f)
      (clojure.tools.reader/read
       (indexing-push-back-reader
        (java.io.PushbackReader. (io/reader f)))))))


(defn define-modules [state]
  (-> state
      (cljs/step-configure-module
       :cljs ;; module name
       ['cljs.core] ;; module mains, a main usually contains exported functions or code that just runs
       #{}) ;; module dependencies
      (cljs/step-configure-module :hecuba ['kixi.hecuba.main] #{:cljs})
      ))

(defn message [state message]
  (println message)
  state)

(defn compile-cljs
  "build the project, wait for file changes, repeat"
  [& args]
  (let [state (-> (cljs/init-state)
                  (cljs/enable-source-maps)
                  (assoc :optimizations :none
                         :pretty-print true
                         :work-dir (io/file "target/cljs-work") ;; temporary output path, not really needed
                         :public-dir (io/file "target/cljs") ;; where should the output go
                         :public-path "/cljs") ;; whats the path the html has to use to get the js?
                  (cljs/step-find-resources-in-jars) ;; finds cljs,js in jars from the classpath
                  (cljs/step-find-resources "lib/js-closure" {:reloadable false})
                  (cljs/step-find-resources "src-cljs") ;; find cljs in this path
                  (cljs/step-finalize-config) ;; shouldn't be needed but is at the moment
                  (cljs/step-compile-core) ;; compile cljs.core
                  (define-modules)
                  )]

    (-> state
        (cljs/step-compile-modules)
        (cljs/flush-unoptimized)))

  :done)

(defn spy [x]
  (println "System map is now")
  (pprint x)
  x)

(defn define-modules [state]
  (-> state
      (cljs/step-configure-module
       :cljs ;; module name
       ['cljs.core] ;; module mains, a main usually contains exported functions or code that just runs
       #{}) ;; module dependencies
      (cljs/step-configure-module :hecuba ['kixi.hecuba.main] #{:cljs})
      ))


(defn compile-cljs
  "build the project, wait for file changes, repeat"
  [& args]

  (let [state (if-let [s kixi.hecuba.application.safe/cljs-compiler-state]
                (do
                  (println "CLJS: Using existing state")
                  s)
                (do
                  (println "CLJS: Creating new state")
                  (-> (cljs/init-state)
                      (cljs/enable-source-maps)
                      (assoc :optimizations :none
                             :pretty-print true
                             :work-dir (io/file "target/cljs-work") ;; temporary output path, not really needed
                             :public-dir (io/file "target/cljs") ;; where should the output go
                             :public-path "/cljs") ;; whats the path the html has to use to get the js?
                      (cljs/step-find-resources-in-jars) ;; finds cljs,js in jars from the classpath
                      (cljs/step-find-resources "lib/js-closure" {:reloadable false})
                      (cljs/step-find-resources "src-cljs") ;; find cljs in this path
                      (cljs/step-finalize-config) ;; shouldn't be needed but is at the moment
                      (cljs/step-compile-core)    ;; compile cljs.core
                      (define-modules)
                      )))]

    (alter-var-root #'kixi.hecuba.application.safe/cljs-compiler-state
                    (fn [_]
                      (-> state
                          (cljs/step-reload-modified)
                          (cljs/step-compile-modules)
                          (cljs/flush-unoptimized))))

    (alter-var-root #'kixi.hecuba.application.safe/cljs-compiler-compile-count inc)

    (println (format "Compiled %d times" kixi.hecuba.application.safe/cljs-compiler-compile-count)))

  :done)

(defrecord ClojureScriptBuilder []
  component/Lifecycle
  (start [this]
    (log/info "ClojureScriptBuilder starting")
    (try
      (compile-cljs)
      this
      (catch Exception e
        (println "ClojureScript build failed:" e)
        (assoc this :error e))))
  (stop [this] this))

(defn new-cljs-builder []
  (->ClojureScriptBuilder))

(defn new-system []
  (let [cfg (config)]
    (-> (component/system-map
         :cluster (new-cluster (:cassandra-cluster cfg))
         :session (new-session (:cassandra-session cfg))
         :store (new-direct-store)
         :pipeline (new-pipeline)
         :scheduler (kixipipe.scheduler/mk-session cfg)
         :queue (new-queue (:queue cfg))
         :queue-worker (new-queue-worker)
         :cljs-builder (new-cljs-builder)
         :web-server (new-webserver (:web-server cfg))
         :bidi-ring-handler (new-bidi-ring-handler-provider)
         :main-routes (new-main-routes)
         :amon-api (new-amon-api "/4")
         :user-api (new-user-api)
         :cljs-routes (new-cljs-routes (:cljs-builder cfg)))

        (mod/system-using
         {:main-routes [:store]
          :amon-api [:store :queue :queue-worker :session]
          :user-api [:store]
          :store [:session]
          :queue-worker [:queue :store]
          :pipeline [:store]
          :scheduler [:pipeline]
          :session [:cluster]}))))
