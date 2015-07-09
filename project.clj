(def cider-nrepl-version "0.8.2")
(defproject kixi/hecuba "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :plugins [[lein-cljsbuild "1.0.4"]
            [lein-environ "1.0.0"]
            [com.palletops/uberimage "0.4.1"] ;; CAUTION - this has been known to cause some wierd dependency issues.
            ]

  ;; Enable the lein hooks for: clean, compile, test, and jar.
  ;; :hooks [leiningen.cljsbuild]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.macro "0.1.5"]
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

                 ;; liberator
                 [org.clojure/tools.trace "0.7.8"]
                 [compojure "1.3.2"]
                 [liberator "0.12.2"]

                 ;; Data
                 [org.clojure/data.csv "0.1.2"]
                 [cheshire "5.3.1"]
                 [org.clojure/data.json "0.2.5"]
                 [roul "0.2.0"]
                 [com.stuartsierra/frequencies "0.1.0"]
                 [clj-time "0.9.0"]
                 [hickory "0.5.4"]

                 ;; Cassandra
                 [cc.qbits/alia "2.3.1"]
                 ;; add lz4 to avoid startup warning.
                 [net.jpountz.lz4/lz4 "1.2.0"]
                 ;; Required for Cassandra (possibly only OSX)
                 [org.xerial.snappy/snappy-java "1.1.1.3"]

                 [kixi/pipe "0.17.12"]

                 ;; elasticsearch integration
                 [clojurewerkz/elastisch "2.1.0"]

                 ;; authn and authz
                 [com.cemerick/friend "0.2.1" :exclusions [org.clojure/core.cache
                                                           commons-codec
                                                           commons-logging]]

                 [http-kit "2.1.16"]

                 ;; EDN reader with location metadata - for configuration
                 [org.clojure/tools.reader "0.8.8"]

                 ;; ClojureScript dependencies

                 [org.clojure/clojurescript "0.0-2843" :scope "provided"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha" :scope "provided"]

                 [cljs-ajax "0.2.3"]
                 ;; [cljs-ajax "0.2.6"]
                 [org.omcljs/om "0.8.8"]
                 [com.andrewmcveigh/cljs-time "0.2.4"]
                 [sablono "0.2.22"]

                 ;; Dev environment
                 [lein-figwheel "0.2.5"]
                 [enlive "1.1.5"]
                 [environ "1.0.0"]
                 [ankha "0.1.4"]

                 [javax.servlet/servlet-api "2.5"]

                 ;; do this here to avoid clashes
                 ;; with local profiles.clj. We need
                 ;; to choose a consistent version so
                 ;; we can remote repl with completion.
                 [cider/cider-nrepl              ~cider-nrepl-version]]

  :source-paths ["src/clj" "src/cljs"]
  :test-paths ["test/clj" "test/cljs"]
  :resource-paths ["resources" "out"]
  ;; overridding protection on :clean-targets to allow for deleting "out"
  :clean-targets ^{:protect false} ["target" "out"]

  :min-lein-version "2.5.0"

  :jvm-opts ["-Duser.timezone=UTC" "-XX:MaxPermSize=128m" "-Xmx2G" "-XX:+UseCompressedOops" "-XX:+HeapDumpOnOutOfMemoryError"]
  ;; "-XX:+PrintGC"  "-XX:+PrintGCDetails" "-XX:+PrintGCTimeStamps"

  :uberimage {:base-image "mastodonc/basejava"
              :cmd ["/bin/bash" "/start-hecuba"]
              :files {"start-hecuba" "docker/start-hecuba.sh"}
              :tag "mastodonc/kixi.hecuba"}

  :profiles {:dev {:source-paths ["dev"]
                   :dependencies [[ring-mock "0.1.5"]
                                  [figwheel "0.2.5"]
                                  [figwheel-sidecar "0.2.5"]
                                  [com.cemerick/piggieback "0.1.5"]
                                  [weasel "0.6.0"]
                                  [org.clojure/tools.namespace "0.2.5"]
                                  [org.clojure/test.check "0.5.9"]]
                   :figwheel {:http-server-root "cljs"
                              :port 3449
                              :css-dirs ["resources/site/css"]}
                   :env {:is-dev true}
                   :plugins [[lein-figwheel "0.2.5"]]
                   :cljsbuild {:builds {:hecuba {:source-paths ["env/dev/cljs"]}}}}
             :uberjar {:hooks [leiningen.cljsbuild]
                       :env {:production true}
                       :main kixi.hecuba.main
                       :aot [kixi.hecuba.main]
                       :omit-source true
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}}

  :exclusions [[org.clojure/clojure]
               [org.clojure/clojurescript]
               [org.clojure/core.async]
               [org.clojure/tools.trace]
               [org.clojure/tools.logging]
               [joda-time]]

  :cljsbuild {:builds {:hecuba {:source-paths ["src/cljs"]
                                :jar true
                                :compiler {:output-to "out/cljs/hecuba.js"
                                           :output-dir "out/cljs"
                                           :optimizations :none
                                           :pretty-print true}}
                       :test {:source-paths ["test/cljs"]
                              :compiler {:output-to "target/testable.js"
                                         :preamble ["react/react.min.js" "vendor/d3.v3.min.js"]
                                         :optimizations :whitespace
                                         :pretty-print  false}}}
              :test-commands {"test" ["phantomjs" "phantom/unit-test.js" "phantom/unit-test.html"]}}

  ;; lein test - runs default
  ;; lein test :http-tests  - runs just http-tests
  ;; lein test :data-tests  - runs just data-tests
  ;; lein test :all - runs all tests
  :test-selectors {:default (fn [m] (not (or (:http-tests m) (:data-tests m))))
                   :http-tests :http-tests
                   :data-tests :data-tests
                   :all (constantly true)})
