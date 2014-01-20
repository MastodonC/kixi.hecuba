(ns kixi.hecuba.dev-test
  (:use clojure.test
        kixi.hecuba.protocols
        kixi.hecuba.dev))

(deftest query
  (let [r (ref {})
        commander (->RefCommander r)
        querier (->RefQuerier r)]
    (upsert! commander {:hecuba/name "Project X" :hecuba/type :project})
    (upsert! commander {:hecuba/name "Project Y" :hecuba/type :project})
    (upsert! commander {:hecuba/name "Falling Water" :hecuba/type :property})
    (is (= (count (items querier)) 3))
    (is (= (count (items querier {:hecuba/type :project})) 2))))
