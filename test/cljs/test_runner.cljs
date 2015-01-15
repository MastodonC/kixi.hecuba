(ns test-runner
  (:require [cljs.test :refer-macros [run-tests]]
            [kixi.hecuba.profiles.form-test]
            [kixi.hecuba.tabs.hierarchy.profiles-test]))

(enable-console-print!)

(defn runner []
  (println "Runner starts")
  (if (cljs.test/successful?
        (run-tests
         'kixi.hecuba.profiles.form-test
         'kixi.hecuba.tabs.hierarchy.profiles-test))
    0
    1))
