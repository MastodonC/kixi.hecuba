(ns kixi.hecuba.security-test
  (:require [kixi.hecuba.security :refer :all :as sec]
            [clojure.test :refer :all]))

(def programmes {"g1" ::sec/programme-manager
                 "g2" ::sec/project-manager
                 "g3" ::sec/user})

(def projects {"j1" ::sec/programme-manager
               "j2" ::sec/project-manager
               "j3" ::sec/user})

(deftest has-programme-manager-test
  (testing "Programme Managers for a Programme are Programme Managers"
    (is (has-programme-manager? "g1" programmes))
    (is (not (has-programme-manager? "g2" programmes)))
    (is (not (has-programme-manager? "g3" programmes)))
    (is (not (has-programme-manager? "g4" programmes)))
    (is (not (has-programme-manager? "g1" nil)))
    (is (not (has-programme-manager? "g1" {})))))


(deftest has-project-manager-test
  (testing "Project Managers for a Project are Project Managers"
    (is (has-project-manager? "j1" projects))
    (is (has-project-manager? "j2" projects))
    (is (not (has-project-manager? "j3" projects)))
    (is (not (has-project-manager? "j4" projects)))
    (is (not (has-project-manager? "j1" nil)))
    (is (not (has-project-manager? "j1" {})))))

(deftest has-user-test
  (testing "Users for a project are users"
    (is (has-user? "g1" programmes "j1" projects))
    (is (has-user? "g1" programmes "j2" projects))
    (is (has-user? "g1" programmes "j3" projects))
    (is (has-user? "g2" programmes "j1" projects))
    (is (has-user? "g2" programmes "j2" projects))
    (is (has-user? "g2" programmes "j3" projects))
    (is (has-user? "g3" programmes "j1" projects))
    (is (has-user? "g3" programmes "j2" projects))
    (is (has-user? "g3" programmes "j3" projects))
    (is (has-user? "g4" programmes "j3" projects))
    (is (has-user? "g3" programmes "j5" projects))
    (is (has-user? "g3" programmes "j5" nil))
    (is (has-user? "g4" nil "j3" projects))
    (is (not (has-user? "g3" nil "j3" nil)))
    (is (not (has-user? nil programmes "j5" projects)))
    (is (has-user? nil programmes "j3" projects))
    (is (not (has-user? "g5"programmes  nil projects)))
    (is (has-user? "g3" programmes nil projects))
    (is (not (has-user? nil nil nil nil)))
    (is (not (has-user? "g4" programmes "j4" projects)))))
