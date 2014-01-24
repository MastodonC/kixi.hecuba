(def jig-version "2.0.0-RC8-SNAPSHOT")

(defproject hecuba "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [liberator "0.10.0"]
                 [cheshire "5.3.1"]
                 ;; [com.cemerick/friend "0.2.0"]

                 ;; ClojureScript dependencies
                 [prismatic/dommy "0.1.1"]
                 [cljs-ajax "0.2.3"]
                 [om "0.2.3"]
                 [net.drib/mrhyde "0.5.2"]

                 [jig ~jig-version]
                 [jig/async ~jig-version]
                 [jig/cljs-builder ~jig-version]
                 [jig/http-kit ~jig-version]
                 [jig/bidi ~jig-version]

                 [clj-kafka "0.1.2-0.8" :exclusions [org.slf4j/slf4j-simple]]
                 [camel-snake-kebab "0.1.2"]
                 [clojurewerkz/cassaforte "1.3.0-beta7" :exclusions [[com.datastax.cassandra/cassandra-driver-core]]]
                 [com.datastax.cassandra/cassandra-driver-core "1.0.5" :exclusions [[org.slf4j/slf4j-log4j12]
                                                                                    [log4j/log4j]]]]

  :source-paths ["src" "src-cljs"]

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[ring-mock "0.1.5"]]}
             :uberjar {:main kixi.hecuba :aot [kixi.hecuba]}}

  )
