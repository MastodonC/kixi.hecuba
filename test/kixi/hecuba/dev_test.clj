(ns kixi.hecuba.dev-test
  (:use clojure.test
        kixi.hecuba.protocols
        kixi.hecuba.dev))


(deftest query
  (let [r (ref {})
        commander (->RefCommander r)
        querier (->RefQuerier r)]

    ;; Add some data
    (upsert! commander {:hecuba/name "Project X" :hecuba/type :project})
    (upsert! commander {:hecuba/name "Project Y" :hecuba/type :project})
    (upsert! commander {:hecuba/name "Falling Water" :hecuba/type :property})

    (is (= (count (items querier)) (count @r))
        "Without a constraint argument, items should return all the items in the ref")

    (is (= (count (items querier {:hecuba/type :project}))
           (count (filter #(= :project (:hecuba/type %)) (vals @r))))
        "With a constraint, just the items that satisfy the constraint"
        )))
