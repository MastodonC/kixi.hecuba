(ns kixi.hecuba.data.devices-test
  (:use clojure.test)
  (:require [kixi.hecuba.data.devices :refer :all]))

(deftest encode-test
  (testing "Encode device"
    (is (= {:id "8c077c2c3eac472d153886244e7b8aa6cad6a7e7" :entity_id "821e6367f385d82cc71b2afd9dc2df3b2ec5b81c"
            :description "Internal air temperature sensor"
            :metadata "{\"passivrole\":\"zone 1 temperature,zone 1 temperature\"}"
            :location "{\"name\":\"Kitchen\"}"}
           (encode {:device_id "8c077c2c3eac472d153886244e7b8aa6cad6a7e7" :user_id "support@mastodonc.com"
                    :entity_id "821e6367f385d82cc71b2afd9dc2df3b2ec5b81c" :description "Internal air temperature sensor"
                    :metadata {:passivrole "zone 1 temperature,zone 1 temperature"} :location {:name "Kitchen"}})))))

(deftest decode-test
  (testing "Decode device"
    (is (= {:device_id "8c077c2c3eac472d153886244e7b8aa6cad6a7e7"
            :entity_id "821e6367f385d82cc71b2afd9dc2df3b2ec5b81c" :description "Internal air temperature sensor"
            :metadata {"passivrole" "zone 1 temperature,zone 1 temperature"} :location {"name" "Kitchen"}}
           (decode {:id "8c077c2c3eac472d153886244e7b8aa6cad6a7e7" :user_id "support@mastodonc.com"
                    :location "{\"name\":\"Kitchen\"}"
                    :entity_id "821e6367f385d82cc71b2afd9dc2df3b2ec5b81c"
                    :description "Internal air temperature sensor"
                    :metadata "{\"passivrole\":\"zone 1 temperature,zone 1 temperature\"}"})))))
