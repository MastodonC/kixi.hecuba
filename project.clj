(defproject hecuba "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [liberator "0.10.0"]
                 [com.cemerick/friend "0.2.0"]

                 ;; [jig/cljs "0.7.0"]
                 ;; [org.clojure/clojurescript "0.0-2030"] ; remove me
                 [prismatic/dommy "0.1.1"]
                 [cljs-ajax "0.2.3"]
                 ]

  :plugins [[lein-cljsbuild "1.0.1"]]

  ;; This isn't great, but until Malcolm gets the Jig cljs stuff working again
  :resource-paths ["resources" "target"]

  :cljsbuild {
              :builds [{
                        ;; The path to the top-level ClojureScript source directory:
                        :source-paths ["src-cljs"]
                        ;; The standard ClojureScript compiler options:
                        ;; (See the ClojureScript compiler documentation for details.)
                        :compiler {
                                   :output-to "target/cljsbuild-main.js" ; default: target/cljsbuild-main.js
                                   :optimizations :whitespace
                                   :pretty-print true}}]}
  )
