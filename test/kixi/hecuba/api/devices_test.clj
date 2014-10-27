(ns kixi.hecuba.api.devices-test
  (:use clojure.test)
  (:require [kixi.hecuba.api.devices :refer :all]
            [clojure.test :refer :all]))

(deftest calculated-sensor-test
  (testing "Testing calculated-sensor"
    (is (= {:unit "co2" :type "gasConsumption_co2" :synthetic true} (calculated-sensor {:type "gasConsumption" :unit "kWh"})))
    (is (= [{:unit "kWh" :type "gasConsumption_kwh" :synthetic true}
            {:unit "co2" :type "gasConsumption_kwh_co2" :synthetic true}] (calculated-sensor {:type "gasConsumption" :unit "m^3"})))
    (is (= [{:unit "kWh" :type "gasConsumption_kwh" :synthetic true}
            {:unit "co2" :type "gasConsumption_kwh_co2" :synthetic true}] (calculated-sensor {:type "gasConsumption" :unit "ft^3"})))))

(deftest create-default-sensors-test
  (testing "Testing create-default-sensors"
    (is (= {:readings [{:type "electricityConsumption" :unit "kWh" :period "CUMULATIVE"}
                       {:type "electricityConsumption_differenceSeries" :unit "kWh" :period "PULSE" :synthetic true}]}
           (create-default-sensors {:readings [{:type "electricityConsumption" :unit "kWh" :period "CUMULATIVE"}]})))
    (is (= {:readings [{:type "electricityConsumption" :unit "kWh" :period "PULSE"}
                       {:type "electricityConsumption_co2" :unit "co2" :period "PULSE" :synthetic true}]}
           (create-default-sensors {:readings [{:type "electricityConsumption" :unit "kWh" :period "PULSE"}]})))
    (is (= {:readings [{:type "electricityConsumption" :unit "m^3" :period "PULSE"}
                       {:type "electricityConsumption_kwh" :unit "kWh" :period "PULSE" :synthetic true}
                       {:type "electricityConsumption_kwh_co2" :unit "co2" :period "PULSE" :synthetic true}]}
           (create-default-sensors {:readings [{:type "electricityConsumption" :unit "m^3" :period "PULSE"}]})))
    (is (= {:readings [{:type "temperature" :unit "C" :period "INSTANT"}]}
           (create-default-sensors {:readings [{:type "temperature" :unit "C" :period "INSTANT"}]})))
    (is (= {:readings [{:type "gasConsumption" :unit "kWh" :period "INSTANT"}]}
           (create-default-sensors {:readings [{:type "gasConsumption" :unit "kWh" :period "INSTANT"}]})))))
