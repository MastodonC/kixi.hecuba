(ns kixi.hecuba.api.parse-test
  (:use clojure.test)
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check :as tc]
            [schema.core :as s]
            [schema_gen.core :as sg]
            [clojure.test.check.properties :as prop]))

(defn non-empty? [n] (if (coll? n) (seq n) n))

;; This is a function copied from new_profile.cljs. Test shoudl be moved to cljs tests once they are set up.
(defn parse
  "Remove all empty elements from the nested data structure and flatten :_value elements."
  [cursor]
  (clojure.walk/postwalk (fn [m]
                          (cond
                           (:_value m)
                           (:_value m)

                           (map? m)
                           (reduce-kv (fn [agg k v] (if (non-empty? v) (assoc agg k v) agg)) {} m)

                           (and (coll? m) (not (keyword? (first m))))
                           (into (empty m) (filter non-empty? m))

                           :else
                           m))
                         cursor))


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
                       {:construction 5}]} (parse m1)))
      (is (= {:bar 1
              :moo {:event 2
                    :ter 1}
              :floors [{:area 10}]} (parse m2))))))

(def value {:_value (s/one s/Str "s")})

(def profile
  {:timestamp value
   :profile_data {:ber value :ter {}}
   :conservatories [{:area value}]})

(defn gen-profile [] (sg/generate-examples profile))

(deftest parse-generated-profiles-test
  (testing
      (let [profiles (gen-profile)]
        (doseq [profile profiles]
          (let [parsed (parse profile)]
            (is (and (nil? (get-in parsed [:timestamp :_value]))
                     (nil? (get-in parsed [:conservatories :area :_value]))
                     (nil? (get-in parsed [:profile_data :ber :_value]))
                     (nil? (get-in parsed [:profile_data :ter :_value])))))
          (is (false? (and (nil? (get-in profile [:timestamp :_value]))
                           (nil? (get-in profile [:conservatories :area :_value]))
                           (nil? (get-in profile [:profile_data :ber :_value]))
                           (nil? (get-in profile [:profile_data :ter :_value])))))))))
