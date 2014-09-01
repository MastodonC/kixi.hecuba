(ns kixi.hecuba.api.entities.upload-test
  (:require [kixi.hecuba.api.entities.upload :refer :all]
            [clojure.test :refer :all]
            [kixipipe.storage.s3 :refer (s3-key-from item-from-s3-key)]))

(deftest s3-key-from-test
  (is (= (s3-key-from {:src-name "media-resources" :feed-name "photos" :entity_id 1234 :metadata {:filename "foo.png"}})
         "media-resources/1234/photos/foo.png")
      (= (s3-key-from {:src-name "media-resources" :feed-name "documents" :entity_id 1234 :metadata {:filename "war-n-peace.txt"}})
         "media-resources/1234/documents/war-n-peace.txt")))

(deftest item-from-s3-key-test
  (is (= {:src-name "media-resources" :feed-name "documents" :entity_id "1234" :metadata {:filename "war-n-peace.txt"}}
         (item-from-s3-key "media-resources/1234/documents/war-n-peace.txt"))
      (= nil
         (item-from-s3-key "media-resources/1231a/asdf"))))
