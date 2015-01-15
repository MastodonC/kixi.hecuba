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

(deftest explode-nested-item-test
  (is (= nil
         (let [association {:schema [:window_type :frame_type :frame_type_other
                                     :percentage_glazing :area :location :uvalue
                                     :created_at :updated_at],
                            :name "window_sets_0" :type :associated-items}
               item nil]
           (explode-nested-item association item))))
  (is (= [["roofs_0_roof_type" nil]
          ["roofs_0_construction" "Pitched access to loft"]
          ["roofs_0_construction_other" nil]
          ["roofs_0_insulation_location_one" nil]
          ["roofs_0_insulation_location_one_other" nil]
          ["roofs_0_insulation_location_two" nil]
          ["roofs_0_insulation_location_two_other" nil]
          ["roofs_0_insulation_thickness_one" nil]
          ["roofs_0_insulation_thickness_one_other" nil]
          ["roofs_0_insulation_thickness_two" nil]
          ["roofs_0_insulation_thickness_two_other" nil]
          ["roofs_0_insulation_date" nil]
          ["roofs_0_insulation_type" nil]
          ["roofs_0_insulation_product" nil]
          ["roofs_0_uvalue" nil]
          ["roofs_0_uvalue_derived" nil]
          ["roofs_0_created_at" nil]
          ["roofs_0_updated_at" nil]]
         (let [association {:name "roofs_0" :type :associated-items,
                            :schema [:roof_type :construction :construction_other :insulation_location_one
                                     :insulation_location_one_other :insulation_location_two
                                     :insulation_location_two_other :insulation_thickness_one
                                     :insulation_thickness_one_other :insulation_thickness_two
                                     :insulation_thickness_two_other :insulation_date :insulation_type
                                     :insulation_product :uvalue :uvalue_derived :created_at :updated_at]}

               item        {:construction "Pitched access to loft"}]
           (explode-nested-item association item))))
  (is (= [["window_sets_0_window_type" "Double glazed"]
          ["window_sets_0_frame_type" "uPVC"]
          ["window_sets_0_frame_type_other" nil]
          ["window_sets_0_percentage_glazing" "12%"]
          ["window_sets_0_area" "29.98"]
          ["window_sets_0_location" nil]
          ["window_sets_0_uvalue" "1.4"]
          ["window_sets_0_created_at" nil]
          ["window_sets_0_updated_at" nil]]
         (let [association {:name "window_sets_0" :type :associated-items,
                            :schema [:window_type :frame_type :frame_type_other :percentage_glazing
                                     :area :location :uvalue :created_at :updated_at]}
               item {:percentage_glazing "12%" :frame_type "uPVC"
                     :window_type "Double glazed" :area "29.98" :uvalue "1.4"}]
           (explode-nested-item association item)))))

(deftest explode-associated-items-test
  (is (= [["walls_0_wall_type" nil]
          ["walls_0_construction" "Cavity"]
          ["walls_0_construction_other" nil]
          ["walls_0_insulation" nil]
          ["walls_0_insulation_date" nil]
          ["walls_0_insulation_type" nil]
          ["walls_0_insulation_thickness" nil]
          ["walls_0_insulation_product" nil]
          ["walls_0_uvalue" nil]
          ["walls_0_location" nil]
          ["walls_0_area" nil]
          ["walls_0_created_at" nil]
          ["walls_0_updated_at" nil]]
         (let [association {:name :walls, :type :associated-items,
                            :schema [:wall_type :construction :construction_other :insulation
                                     :insulation_date :insulation_type :insulation_thickness
                                     :insulation_product :uvalue :location :area :created_at :updated_at]}
               items       [{:construction "Cavity"}]]
           (explode-associated-items association items))))
  (is (= [["door_sets_0_door_type" "Solid (< 30% glazing)"]
          ["door_sets_0_door_type_other" nil]
          ["door_sets_0_frame_type" nil]
          ["door_sets_0_frame_type_other" nil]
          ["door_sets_0_percentage_glazing" nil]
          ["door_sets_0_area" nil]
          ["door_sets_0_location" nil]
          ["door_sets_0_uvalue" "1"]
          ["door_sets_0_created_at" nil]
          ["door_sets_0_updated_at" nil]
          ["door_sets_1_door_type" "Solid (< 30% glazing)"]
          ["door_sets_1_door_type_other" "Test"]
          ["door_sets_1_frame_type" nil]
          ["door_sets_1_frame_type_other" nil]
          ["door_sets_1_percentage_glazing" nil]
          ["door_sets_1_area" nil]
          ["door_sets_1_location" nil]
          ["door_sets_1_uvalue" nil]
          ["door_sets_1_created_at" nil]
          ["door_sets_1_updated_at" nil]]

       (let  [association {:name :door_sets, :type :associated-items,
                           :schema [:door_type :door_type_other :frame_type
                                    :frame_type_other :percentage_glazing :area
                                    :location :uvalue :created_at :updated_at]}
              items [{:uvalue "1" :door_type "Solid (< 30% glazing)"}
                     {:door_type "Solid (< 30% glazing)", :door_type_other "Test"}]]
         (explode-associated-items association items))))
  (is (= []
         (let [association {:name :storeys, :type :associated-items,
                            :schema [:storey_type :storey :heat_loss_w_per_k
                                     :heat_requirement_kwth_per_year :created_at
                                     :updated_at]}
               items []]
           (explode-associated-items association items))))
  (is (= []
         (let [association {:name :storeys, :type :associated-items,
                            :schema [:storey_type :storey :heat_loss_w_per_k
                                     :heat_requirement_kwth_per_year :created_at
                                     :updated_at]}
               items nil]
           (explode-associated-items association items)))))

(deftest attribute-type-test
  (is (= :attribute (attribute-type :id)))
  (is (= :associated-items (attribute-type {:name :storeys, :type :associated-items,
                                            :schema [:storey_type :storey :heat_loss_w_per_k
                                                     :heat_requirement_kwth_per_year :created_at
                                                     :updated_at]}))))
