(ns kixi.hecuba.api.entities.schema-test
  (:require [kixi.hecuba.api.entities.schema :refer :all]
            [clojure.test :refer :all]))


(deftest assoc-profiles-test
  (testing "Check that we can create entities with associated profiles and dummy entities as well."
    (is (= (assoc-profiles [{:property_code "1" :addr "one high st"}]
                           [{:property_code "1" :event_type "pre"}
                            {:property_code "1" :event_type "post"}
                            {:property_code "2" :event_type "pre"}
                            {:property_code "2" :event_type "planned"}])
           [{:profiles
             [{:property_code "1", :event_type "post"}
              {:property_code "1", :event_type "pre"}],
             :property_code "1",
             :addr "one high st"}
            {:profiles
             [{:property_code "2", :event_type "planned"}
              {:property_code "2", :event_type "pre"}],
             :property_code "2"}]))
    (is (= (assoc-profiles [{:property_code "1" :addr "one high st"}] [])
           [{:property_code "1", :addr "one high st"}]))

    (is (= (assoc-profiles [] [{:property_code "1" :event_type "pre"}
                               {:property_code "1" :event_type "post"}
                               {:property_code "2" :event_type "pre"}
                               {:property_code "2" :event_type "planned"}])
           [{:profiles
             [{:property_code "1", :event_type "post"}
              {:property_code "1", :event_type "pre"}],
             :property_code "1"}
            {:profiles
             [{:property_code "2", :event_type "planned"}
              {:property_code "2", :event_type "pre"}],
             :property_code "2"}]))
    (is (= (assoc-profiles [] [])
           []))))
