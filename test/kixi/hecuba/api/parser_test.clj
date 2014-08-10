(ns kixi.hecuba.api.parser-test
  (:require [kixi.hecuba.api.parser :refer :all]
            [clojure.test :refer :all]))

(def bad-profile {"bad" "data"})
(def simple-schema [:id
                    {:name :nested_item
                     :type :nested-item
                     :schema [:nested_id]}
                    {:name :association
                     :type :associated-items
                     :schema [:associated_id]}])

(deftest parse-by-schema-test
  (testing "bad input, return an empty schema"
    (is (= {}
           (parse-by-schema bad-profile simple-schema))))
  (testing "input with valid simple attribute"
    (is (= {:id "test"}
           (parse-by-schema {"id" "test"} simple-schema))))
  (testing "input with valid nested item"
    (is (= {:nested_item {:nested_id "test"}}
           (parse-by-schema { "nested_item_nested_id" "test" } simple-schema))))
  (testing "input with valid associated items"
    (is (= {:id nil
            :nested_item {:nested_id nil}
            :association [{:associated_id "test"} {:associated_id "test2"}]})
           (parse-by-schema {"association_0_associated_id" "test"
                             "association_1_associated_id" "test2" }
                            simple-schema))))
