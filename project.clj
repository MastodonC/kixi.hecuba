(defproject kixi/hecuba "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-cljsbuild "1.0.2"]]

  :dependencies [[org.clojure/clojure "1.5.1"]

                 [org.clojure/clojurescript "0.0-2173" :scope "provided"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha" :scope "provided"]

                 ;; Modular
                 [juxt/modular "0.2.0-SNAPSHOT"]
                 [juxt.modular/http-kit "0.1.0-SNAPSHOT"]
                 [juxt.modular/bidi "0.1.0-SNAPSHOT"]
                 [juxt.modular/cassandra "0.1.0-SNAPSHOT"]

                 ;; EDN reader with location metadata - for configuration
                 [org.clojure/tools.reader "0.8.3"]

                 [liberator "0.11.0"]
                 [cheshire "5.3.1"]

                 ;; Required for Cassandra (possibly only OSX)
                 [org.xerial.snappy/snappy-java "1.1.0.1"]

                 ;; ClojureScript dependencies
                 [prismatic/dommy "0.1.2"]
                 [cljs-ajax "0.2.3"]
                 [om "0.5.1"]
                 [net.drib/mrhyde "0.5.3"]
                 [com.andrewmcveigh/cljs-time "0.1.1"]

                 [clj-kafka "0.2.0-0.8" :exclusions [org.slf4j/slf4j-simple]]
                 [camel-snake-kebab "0.1.4"]
                 [clojurewerkz/cassaforte "1.3.0-beta9" :exclusions [[com.datastax.cassandra/cassandra-driver-core]]]
                 [com.datastax.cassandra/cassandra-driver-core "1.0.5" :exclusions [[org.slf4j/slf4j-log4j12]
                                                                                    [log4j/log4j]]]


                 [org.clojure/tools.macro "0.1.2"]


                 [reiddraper/simple-check "0.5.6"]
                 [roul "0.2.0"]
                 [com.stuartsierra/frequencies "0.1.0"]
                 [clj-time "0.6.0"]
                 [org.clojure/data.json "0.2.4"]

                 [clojurewerkz/quartzite "1.2.0"]

                 [pipejine "0.1.2" :exclusions [org.slf4j/slf4j-simple]]


                 [thheller/shadow-build "0.5.0" :exclusions [org.clojure/clojurescript]]
                 ]

  :source-paths ["src" "src-cljs"]

  :jvm-opts ["-Duser.timezone=UTC"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [
                                  [ring-mock "0.1.5"]
                                  [com.cemerick/austin "0.1.4"]
                                  [org.clojure/tools.namespace "0.2.4"]
                                  ]}
             :uberjar {:main kixi.hecuba :aot [kixi.hecuba]}}

  :exclusions [[org.clojure/clojurescript]
               [org.clojure/core.async]]

  :cljsbuild {
    :builds [{:id "dev"
              :source-paths ["src-cljs"]
              :compiler {:output-to "out/main.js"
                         :output-dir "out"
                         :optimizations :none
                         :source-map true}}]}

  )
