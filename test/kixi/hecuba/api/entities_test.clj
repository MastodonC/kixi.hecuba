(ns kixi.hecuba.api.entities-test
  (:require [kixi.hecuba.api.entities :refer :all]
            [clojure.test :refer :all]))

(deftest admin-can-get
  (is (allowed?* 1 5 #{} #{} #{:kixi.hecuba.security/admin} :get)))

(deftest admin-can-post
  (is (allowed?* 1 5 #{} #{} #{:kixi.hecuba.security/admin} :post)))

(deftest admin-can-delete
  (is (allowed?* 1 5 #{} #{} #{:kixi.hecuba.security/admin} :delete)))

(deftest super-admin-can-get
  (is (allowed?* 1 5 #{} #{} #{:kixi.hecuba.security/super-admin} :get)))

(deftest super-admin-can-post
  (is (allowed?* 1 5 #{} #{} #{:kixi.hecuba.security/super-admin} :post)))

(deftest super-admin-can-delete
  (is (allowed?* 1 5 #{} #{} #{:kixi.hecuba.security/super-admin} :delete)))

(deftest user-cannot-get
  (is (false?
       (allowed?* 1 5 #{} #{} #{:kixi.hecuba.security/user} :get))))

(deftest user-in-different-programme-can-not-get
  (is (false? (allowed?* 1 5 #{2} #{} #{:kixi.hecuba.security/user} :get))))

(deftest user-in-programme-can-get
  (is (allowed?* 1 5 #{1} #{} #{:kixi.hecuba.security/user} :get)))

(deftest user-in-different-project-can-not-get
  (is false? (allowed?* 1 5 #{} #{4}  #{:kixi.hecuba.security/user} :get)))

(deftest user-in-project-can-get
  (is (allowed?* 1 5 #{} #{5}  #{:kixi.hecuba.security/user} :get)))

(deftest user-in-project-can-not-post
  (is (false? (allowed?* 1 5 #{} #{5}  #{:kixi.hecuba.security/user} :post))))

(deftest user-in-programme-and-project-can-not-post
  (is (false? (allowed?* 1 5 #{1} #{5}  #{:kixi.hecuba.security/user} :post))))

(deftest user-in-programme-and-project-can-not-delete
  (is (false? (allowed?* 1 5 #{1} #{5}  #{:kixi.hecuba.security/user} :delete))))

(deftest programme-manager-in-project-and-programme-can-post
  (is (allowed?* 1 5 #{1} #{5}  #{:kixi.hecuba.security/programme-manager} :post)))

(deftest programme-manager-in-programme-can-post
  (is (allowed?* 1 5 #{1} #{}  #{:kixi.hecuba.security/programme-manager} :post)))

(deftest programme-manager-in-project-can-post
  (is (allowed?* 1 5 #{} #{5}  #{:kixi.hecuba.security/programme-manager} :post)))

(deftest programme-manager-in-different-programme-can-not-post
  (is (false? (allowed?* 1 5 #{2} #{688}  #{:kixi.hecuba.security/programme-manager} :post))))

(deftest project-manager-in-project-can-post
  (is (allowed?* 1 5 #{} #{5}  #{:kixi.hecuba.security/project-manager} :post)))

(deftest project-manager-in-different-project-can-not-post
  (is (false? (allowed?* 1 5 #{} #{4}  #{:kixi.hecuba.security/project-manager} :post))))
