(defproject kixi/hecuba "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-cljsbuild "1.0.3"]]

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/data.csv "0.1.2"]

                 ;; tools.trace for liberator
                 [org.clojure/tools.trace "0.7.8"]

                 [org.clojure/clojurescript "0.0-2202"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha" :scope "provided"]

                 [kixi/bidi "1.10.3-p0-b569a50"]

                 ;; Modular
                 [juxt/modular "0.2.0"]
                 [juxt.modular/http-kit "0.2.0"]
                 [juxt.modular/bidi "0.2.1"]
                 [cylon "0.1.0" :exclusions [ring/ring-core]]

                 ;; EDN reader with location metadata - for configuration
                 [org.clojure/tools.reader "0.8.3"]

                 ;; using a patched liberator pending
                 ;; https://github.com/clojure-liberator/liberator/pull/120
                 [kixi/liberator "0.12.0-p2-413ee59"]
                 [cheshire "5.3.1"]

                 ;; Required for Cassandra (possibly only OSX)
                 [org.xerial.snappy/snappy-java "1.1.0.1"]

                 ;; ClojureScript dependencies
                 [prismatic/dommy "0.1.2"]
                 [cljs-ajax "0.2.3"]
                 [om "0.6.1"]
                 [net.drib/mrhyde "0.5.3"]
                 [com.andrewmcveigh/cljs-time "0.1.3"]
                 [sablono "0.2.17"]

                 [ankha "0.1.2"]

                 [clj-kafka "0.2.0-0.8" :exclusions [org.slf4j/slf4j-simple org.apache.zookeeper/zookeeper]]
                 [camel-snake-kebab "0.1.4"]
                 [cc.qbits/alia "2.0.0-rc1" :exclusions [com.google.guava/guava org.flatland/useful]]
                 [cc.qbits/hayt "2.0.0-beta4"]


                 [org.clojure/tools.macro "0.1.5"]


                 [reiddraper/simple-check "0.5.6"]
                 [roul "0.2.0"]
                 [com.stuartsierra/frequencies "0.1.0"]
                 [clj-time "0.6.0"]
                 [org.clojure/data.json "0.2.4"]

                 [kixi/pipe "0.15.3"]

                 [thheller/shadow-build "0.5.0" :exclusions [org.clojure/clojurescript]]
                 ]

  :source-paths ["src" "src-cljs"]

  :jvm-opts ["-Duser.timezone=UTC"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [
                                  [ring-mock "0.1.5"]
                                  [org.clojure/tools.namespace "0.2.4"]
                                  ]
                   :plugins [[com.cemerick/austin "0.1.4"]] }

             :uberjar {:main kixi.hecuba.controller.main :aot [kixi.hecuba.controller.main]}}

  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]
               [org.clojure/core.async]
               [bidi]
               [org.clojure/tools.trace]]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src-cljs"]
              :compiler {:output-to "out/main.js"
                         :output-dir "out"
                         :optimizations :none
                         :source-map true}}]}

  )
