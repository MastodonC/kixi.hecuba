(ns kixi.hecuba.api.devices-test
  (:use clojure.test)
  (:require [kixi.hecuba.api.devices :refer :all]
            [clojure.test :refer :all]))

;; Dissoc-ing :sensor_id as uuid is not testable
(deftest calculated-sensor-test
  (testing "Testing calculated-sensor"
    (is (= {:unit "co2" :type "gasConsumption_co2" :synthetic true}
           (dissoc (calculated-sensor {:type "gasConsumption" :unit "kWh"}) :sensor_id)))
    (is (= [{:unit "kWh" :type "gasConsumption_kwh" :synthetic true}
            {:unit "co2" :type "gasConsumption_kwh_co2" :synthetic true}]
           (map #(dissoc % :sensor_id) (calculated-sensor {:type "gasConsumption" :unit "m^3"}))))
    (is (= [{:unit "kWh" :type "gasConsumption_kwh" :synthetic true}
            {:unit "co2" :type "gasConsumption_kwh_co2" :synthetic true}]
           (map #(dissoc % :sensor_id) (calculated-sensor {:type "gasConsumption" :unit "ft^3"}))))))

(defn remove-sensor-ids [device]
  (-> device
      (update-in [:readings] (fn [readings] (map #(dissoc % :sensor_id) readings)))))

(deftest create-default-sensors-test
  (testing "Testing create-default-sensors"
    (is (= {:readings [{:type "electricityConsumption" :unit "kWh" :period "CUMULATIVE"}
                       {:type "electricityConsumption_differenceSeries" :unit "kWh" :period "PULSE" :synthetic true}]}
           (remove-sensor-ids (create-default-sensors {:readings [{:type "electricityConsumption"
                                                                   :unit "kWh"
                                                                   :period "CUMULATIVE"}]}))))
    (is (= {:readings [{:type "electricityConsumption" :unit "kWh" :period "PULSE"}
                       {:type "electricityConsumption_co2" :unit "co2" :period "PULSE" :synthetic true}]}
           (remove-sensor-ids (create-default-sensors {:readings
                                                       [{:type "electricityConsumption"
                                                         :unit "kWh"
                                                         :period "PULSE"}]}))))
    (is (= {:readings [{:type "electricityConsumption" :unit "m^3" :period "PULSE"}
                       {:type "electricityConsumption_kwh" :unit "kWh" :period "PULSE" :synthetic true}
                       {:type "electricityConsumption_kwh_co2" :unit "co2" :period "PULSE" :synthetic true}]}
           (remove-sensor-ids (create-default-sensors {:readings
                                                       [{:type "electricityConsumption"
                                                         :unit "m^3"
                                                         :period "PULSE"}]}))))
    (is (= {:readings [{:type "temperature" :unit "C" :period "INSTANT"}]}
           (create-default-sensors {:readings [{:type "temperature" :unit "C" :period "INSTANT"}]})))
    (is (= {:readings [{:type "gasConsumption" :unit "kWh" :period "INSTANT"}]}
           (create-default-sensors {:readings [{:type "gasConsumption" :unit "kWh" :period "INSTANT"}]})))))

(deftest get-sensors-to-delete-test
  (testing "Testing getting a list of synthetic sensors that should be deleted."
    (let [device {:device_id "12345" :readings [{:sensor_id "1" :type "electricityConsumption"
                                                 :unit "kWh" :period "PULSE"}
                                                {:sensor_id "2" :type "electricityConsumption_co2"
                                                 :unit "co2" :period "PULSE"}
                                                {:sensor_id "3" :type "electricityConsumption"
                                                 :unit "kWh" :period "CUMULATIVE"}
                                                {:sensor_id "4" :type "electricityConsumption_differenceSeries"
                                                 :unit "kWh" :period "INSTANT"}
                                                {:sensor_id "5" :type "electricityConsumption_kwh"
                                                 :unit "kWh" :period "PULSE"}
                                                {:sensor_id "6" :type "electricityConsumption_kwh_co2"
                                                 :unit "co2" :period "PULSE"}]}]
      (is (= '({:device_id "12345", :sensor_id "2"})
             (get-sensors-to-delete device (:readings (create-default-sensors
                                                       {:readings [{:sensor_id "1"
                                                                    :type "electricityConsumption"
                                                                    :unit "kWh" :period "PULSE"}]})))))
      (is (= '({:device_id "12345" :sensor_id "5"}
               {:device_id "12345" :sensor_id "6"})
             (get-sensors-to-delete device (:readings (create-default-sensors
                                                       {:readings [{:sensor_id "1"
                                                                    :device_id "12345"
                                                                    :type "electricityConsumption"
                                                                    :unit "m^3" :period "PULSE"}]})))))
      (is (= '({:device_id "12345", :sensor_id "4"})
             (get-sensors-to-delete device (:readings (create-default-sensors
                                                       {:readings [{:sensor_id "4"
                                                                    :type "electricityConsumption"
                                                                    :unit "kWh" :period "CUMULATIVE"}]})))))
      (is (empty? (get-sensors-to-delete device (:readings (create-default-sensors
                                                            {:readings [{:sensor_id "4"
                                                                         :type "temperature"
                                                                         :unit "C" :period "INSTANT"}]}))))))))

(deftest get-sensors-to-delete-from-bad-sensor-defs
  (testing "Testing that we can properly delete sensors from bad sensor definitions"
    (let [device {:device_id "12345" :readings [{:sensor_id "1" :type "electricityConsumption"
                                                 :unit "kWh" :period "CUMULATIVE"}]}]
      (is (= []
             (get-sensors-to-delete device (:readings (create-default-sensors
                                                       {:readings [{:sensor_id "1"
                                                                    :type "electricityConsumption"
                                                                    :unit "kWh" :period "CUMULATIVE"}]}))))))))

(deftest get-sensors-to-insert-test
  (testing "Testing getting a list of synthetic sensors that should be inserted."
    (let [device {:device_id "12345" :readings [{:sensor_id "1" :type "electricityConsumption"
                                                 :unit "kWh" :period "PULSE"}
                                                {:sensor_id "2" :type "electricityConsumption_co2"
                                                 :unit "co2" :period "PULSE"}
                                                {:sensor_id "3" :type "electricityConsumption"
                                                 :unit "kWh" :period "CUMULATIVE"}
                                                {:sensor_id "4" :type "electricityConsumption_differenceSeries"
                                                 :unit "kWh" :period "INSTANT"}]}]
      (is (= '({:type "electricityConsumption_co2" :unit "co2" :period "PULSE" :synthetic true :user_id "test@mastodonc.com"
                :device_id "12345"})
             (map #(dissoc % :sensor_id)
                  (get-sensors-to-insert "test@mastodonc.com" (:readings (create-default-sensors
                                                                          {:readings [{:sensor_id "1"
                                                                                       :device_id "12345"
                                                                                       :type "electricityConsumption"
                                                                                       :unit "kWh" :period "PULSE"}]}))))))
      (is (= '({:type "electricityConsumption_kwh" :unit "kWh" :period "PULSE" :synthetic true :user_id "test@mastodonc.com"
                :device_id "12345"}
               {:type "electricityConsumption_kwh_co2" :unit "co2" :period "PULSE" :synthetic true :user_id "test@mastodonc.com"
                :device_id "12345"})
             (map #(dissoc % :sensor_id)
                  (get-sensors-to-insert "test@mastodonc.com" (:readings (create-default-sensors
                                                                          {:readings [{:sensor_id "1"
                                                                                       :device_id "12345"
                                                                                       :type "electricityConsumption"
                                                                                       :unit "m^3" :period "PULSE"}]}))))))
      (is (= '({:type "electricityConsumption_differenceSeries" :unit "kWh" :period "PULSE"
                :synthetic true :user_id "test@mastodonc.com"  :device_id "12345"})
             (map #(dissoc % :sensor_id)
                  (get-sensors-to-insert "test@mastodonc.com" (:readings (create-default-sensors
                                                                          {:readings [{:sensor_id "4"
                                                                                       :device_id "12345"
                                                                                       :type "electricityConsumption"
                                                                                       :unit "kWh" :period "CUMULATIVE"}]}))))))
      (is (empty?
             (map #(dissoc % :sensor_id)
                  (get-sensors-to-insert "test@mastodonc.com" (:readings (create-default-sensors
                                                                          {:readings [{:sensor_id "4"
                                                                                       :device_id "12345"
                                                                                       :type "temperature"
                                                                                       :unit "C" :period "INSTANT"}]})))))))))
