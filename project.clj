(defproject kixi/hecuba "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-cljsbuild "1.0.3"]]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/data.csv "0.1.2"]
                 [org.clojure/core.match "0.2.2"]

                 [joda-time "2.4"]

                 ;; Testing POST and GET data
                 [kixi/schema_gen "0.1.6" :exclusions [schema-contrib]]
                 [schema-contrib "0.1.5"]
                 [kixi/amon-schema "0.1.12" :exclusions [schema-contrib]]
                 [clj-http "1.0.0"]

                 ;; logging
                 [org.clojure/tools.logging "0.3.0"]
                 [org.slf4j/slf4j-api "1.7.7"]
                 [org.slf4j/jcl-over-slf4j "1.7.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/jul-to-slf4j "1.7.7" :exclusions [org.slf4j/slf4j-api]]
                 [org.slf4j/log4j-over-slf4j "1.7.7" :exclusions [org.slf4j/slf4j-api]]
                 [ch.qos.logback/logback-classic "1.1.2" :exclusions [org.slf4j/slf4j-api]]
                 [commons-logging "1.1.3"]

                 ;; tools.trace for liberator
                 [org.clojure/tools.trace "0.7.8"]

                 [org.clojure/clojurescript "0.0-2356"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha" :scope "provided"]

                 [compojure "1.1.8"]

                 ;; to deal with some legacy html data
                 [hickory "0.5.4"]

                 ;; elasticsearch integration
                 [clojurewerkz/elastisch "2.1.0-beta4"]

                 ;; authn and authz
                 [com.cemerick/friend "0.2.1" :exclusions [org.clojure/core.cache
                                                           commons-codec
                                                           commons-logging]]

                 ;; Modular
                 [juxt/modular "0.2.0" :exclusions [ch.qos.logback/logback-classic
                                                    org.slf4j/jcl-over-slf4j
                                                    org.slf4j/jul-to-slf4j
                                                    org.slf4j/log4j-over-slf4j]]
                 [juxt.modular/http-kit "0.2.0"]

                 ;; EDN reader with location metadata - for configuration
                 [org.clojure/tools.reader "0.8.8"]

                 ;; using a patched liberator pending
                 ;; https://github.com/clojure-liberator/liberator/issues/162
                 [kixi/liberator "0.12.1-p0"]
                 [cheshire "5.3.1"]

                 ;; Required for Cassandra (possibly only OSX)
                 [org.xerial.snappy/snappy-java "1.1.1.3"]

                 ;; ClojureScript dependencies
                 [cljs-ajax "0.2.3"]
                 ;; [cljs-ajax "0.2.6"]
                 [om "0.7.1"]
                 [com.andrewmcveigh/cljs-time "0.1.6"]
                 [sablono "0.2.22"]

                 [ankha "0.1.4"]

                 [cc.qbits/alia "2.1.2"]
                 ;; add lz4 to avoid startup warning.
                 [net.jpountz.lz4/lz4 "1.2.0"]

                 [org.clojure/tools.macro "0.1.5"]

                 [roul "0.2.0"]
                 [com.stuartsierra/frequencies "0.1.0"]
                 [clj-time "0.8.0"]
                 [org.clojure/data.json "0.2.5"]

                 [kixi/pipe "0.17.12"]]

  :source-paths ["src" "src-cljs"]
  :resource-paths ["resources" "out"]

  :jvm-opts ["-Duser.timezone=UTC" "-XX:MaxPermSize=128m" "-Xmx2G" "-XX:+UseCompressedOops" "-XX:+HeapDumpOnOutOfMemoryError"]
  ;; "-XX:+PrintGC"  "-XX:+PrintGCDetails" "-XX:+PrintGCTimeStamps"

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [
                                  [ring-mock "0.1.5"]
                                  [org.clojure/tools.namespace "0.2.5"]
                                  [javax.servlet/servlet-api "2.5"]
                                  [org.clojure/test.check "0.5.9"]]
                   :plugins [[com.cemerick/austin "0.1.4"]]}

             :uberjar {:main kixi.hecuba.controller.main :aot [kixi.hecuba.controller.main]}}

  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]
               [org.clojure/core.async]
               [org.clojure/tools.trace]
               [org.clojure/tools.logging]
               [joda-time]]

  ;; FIXME: We need to define a cljsbuild test key for the hooks to work
  ;; :hooks [leiningen.cljsbuild]
  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src-cljs"]
                        :jar true
                        :compiler {:output-to "out/cljs/hecuba.js"
                                   :source-map "out/cljs/hecuba.map.js"
                                   :output-dir "out/cljs"
                                   :optimizations :none
                                   :preamble ["react/react.min.js"]
                                   :externs ["react/externs/react.js"]}}]}

  ;; lein test - runs default
  ;; lein test :http-tests  - runs just http-tests
  ;; lein test :data-tests  - runs just data-tests
  ;; lein test :all - runs all tests
  :test-selectors {:default (fn [m] (not (or (:http-tests m) (:data-tests m))))
                   :http-tests :http-tests
                   :data-tests :data-tests
                   :all (constantly true)})
