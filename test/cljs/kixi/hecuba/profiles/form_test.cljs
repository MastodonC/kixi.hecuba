(ns kixi.hecuba.profiles.form-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [kixi.hecuba.profiles.form :as form]))

(deftest unparse-test
  (testing "Unparse function"
    (println "Testing unparsing")

    (let [m1 {:timestamp 1
              :profile_data {:event_type "Intervention"
                             :footprint 1}
              :floors [{:floor_type 10}
                       {:construction 5}]}
          m2 {:bar 1
              :moo {:event 2
                    :ter 1}
              :floors [{:area 10}]}]

      (is (= {:timestamp 1
              :profile_data {:event_type {:_value "Intervention"}
                             :footprint {:_value 1}}
              :floors [{:floor_type {:_value 10}}
                       {:construction {:_value 5}}]} (form/unparse m1)))
      (is (= {:bar 1
              :moo {:event {:_value 2} :ter {:_value 1}}
              :floors [{:area {:_value 10}}]} (form/unparse m2))))))

(deftest parse-test
  (testing "Parse function"
    (println "Testing parsing")

    (let [m1 {:timestamp 1
              :profile_data {:event_type {:_value "Intervention"}
                             :footprint {:_value 1}
                             :ber {}}
              :floors [{:floor_type {:_value 10}
                        :construction {}}
                       {:uvalue {}, :construction {:_value 5}}],
              :roofs [{:roof_type {}}]}
          m2 {:bar 1
              :moo {:event {:_value 2}
                    :ter {:_value 1} :ber {} :project {}}
              :floors [{:area {:_value 10} :height {}}]
              :roofs [{:area {}} {:area {}}]}]
      (is (= {:timestamp 1
              :profile_data {:event_type "Intervention"
                             :footprint 1}
              :floors [{:floor_type 10}
                       {:construction 5}]} (form/parse m1)))
      (is (= {:bar 1
              :moo {:event 2
                    :ter 1}
              :floors [{:area 10}]} (form/parse m2))))))
