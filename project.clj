(def jig-version "2.0.3")

(defproject hecuba "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 
                 [org.clojure/clojurescript "0.0-2173" :scope "provided"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha" :scope "provided"]

                 [liberator "0.10.0"]
                 [cheshire "5.3.1"]

                 ;; Required for Cassandra (possibly only OSX)
                 [org.xerial.snappy/snappy-java "1.0.5"]

                 ;; ClojureScript dependencies
                 [prismatic/dommy "0.1.1"]
                 [cljs-ajax "0.2.3"]
                 [om "0.2.3"]
                 [net.drib/mrhyde "0.5.2"]

                 [jig/protocols ~jig-version]
                 [jig/async ~jig-version]
                 [jig/cljs-builder ~jig-version]
                 [jig/http-kit ~jig-version]
                 [jig/bidi ~jig-version]

                 [clj-kafka "0.1.2-0.8" :exclusions [org.slf4j/slf4j-simple]]
                 [camel-snake-kebab "0.1.2"]
                 [clojurewerkz/cassaforte "1.3.0-beta9" :exclusions [[com.datastax.cassandra/cassandra-driver-core]]]
                 [com.datastax.cassandra/cassandra-driver-core "1.0.5" :exclusions [[org.slf4j/slf4j-log4j12]
                                                                                    [log4j/log4j]]]

                 [reiddraper/simple-check "0.5.6"]
                 [roul "0.2.0"]
                 [com.stuartsierra/frequencies "0.1.0"]
                 [clj-time "0.6.0"]
                 [org.clojure/data.json "0.2.4"]

                 [clojurewerkz/quartzite "1.1.0"]
                 [pipejine "0.1.2" :exclusions [org.slf4j/slf4j-simple]]]

  :source-paths ["src" "src-cljs"]

  :jvm-opts ["-Duser.timezone=UTC"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [
                                  [ring-mock "0.1.5"]
                                  [com.cemerick/austin "0.1.3"]
                                  ]
                   }
             :uberjar {:main kixi.hecuba :aot [kixi.hecuba]}}
  
  :exclusions [[org.clojure/clojurescript]
               [org.clojure/core.async]
               ]
  
  )
