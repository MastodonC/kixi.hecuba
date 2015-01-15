(ns kixi.hecuba.api.entities-test
  (:require [kixi.hecuba.api.entities :refer :all]
            [clojure.test :refer :all]))

(deftest extract-profile-ids
  (testing "Extract all the profile ids as a single seq from a seq of entities"
    (is (= #{"1:EID1pre" "2:EID2pre" "2:EID2planned" "2:EID2post"}
           (all-profile-ids [{:property_code "1",
                              :profiles
                              [{:property_code "1", :profile_id "EID1pre", :profile_data {:event_type "pre"}}],
                              :entity_id "EID1"}
                             {:property_code "2",
                              :profiles
                              [{:property_code "2", :profile_id "EID2pre", :profile_data {:event_type "pre"} :timestamp "2014-12-01 00:00"}
                               {:property_code "2", :profile_id "EID2planned", :profile_data {:event_type "planned"}}
                               {:property_code "2", :profile_id "EID2post", :profile_data {:event_type "post"}}],
                              :entity_id "EID2"}])))))

(deftest matching-entities
  (let [existing [{:property_code "1",
                   :profiles
                   [{:property_code "1", :profile_id "EID1pre", :profile_data {:event_type "pre"}}],
                   :entity_id "EID1"}
                  {:property_code "2",
                   :profiles
                   [{:property_code "2", :profile_id "EID2pre", :profile_data {:event_type "pre"} :timestamp "2014-12-01 00:00"}
                    {:property_code "2", :profile_id "EID2planned", :profile_data {:event_type "planned"}}
                    {:property_code "2", :profile_id "EID2post", :profile_data {:event_type "post"}}],
                   :entity_id "EID2"}]
        new {:property_code "2",
             :profiles
             [{:property_code "2", :profile_data {:event_type "pre"} :timestamp "2014-12-01 00:00"}]}]
    (testing "Enrich created entity with entity_id from existing entity"
      (is (= (enrich-with-ids new existing)
             {:property_code "2",
              :profiles
              [{:entity_id "EID2" :property_code "2", :profile_id "EID2pre", :profile_data {:event_type "pre"} :timestamp "2014-12-01 00:00"}],
              :entity_id "EID2"})))))

(deftest matching-profiles
  (let [existing [{:property_code "2", :profile_id "EID2pre", :profile_data {:event_type "pre"} :timestamp "2014-12-01 00:00"}
                  {:property_code "2", :profile_id "EID2planned", :profile_data {:event_type "planned"} :timestamp "2014-12-01 00:00"}
                  {:property_code "2", :profile_id "EID2post", :profile_data {:event_type "post"} :timestamp "2014-12-01 00:00"}]
        new {:property_code "2", :profile_data {:event_type "pre"} :timestamp "2014-12-01 00:00"}]
    (testing "Enrich profile with profile_id from existing"
      (is (= (enrich-with-profile-id new existing)
             {:property_code "2", :profile_id "EID2pre", :profile_data {:event_type "pre"} :timestamp "2014-12-01 00:00"})))
    (testing "New uuids for new profiles"
      (is (not (nil? (-> {:property_code "2", :profile_data {:event_type "ALLNEW"} :timestamp "2014-12-01 00:00"}
                         (enrich-with-profile-id existing)
                         :profile_id))))
      (is (let [enriched (-> new
                             (enrich-with-profile-id {})
                             :profile_id)]
            (and (not (= "EID2pre" enriched))
                 (not (nil? enriched))))))))
