(ns kixi.hecuba.data.sensors
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn get-sensors [device_id session]
  (db/execute session
              (hayt/select :sensors
                           (hayt/where [[= :device_id device_id]]))))

(defn ->clojure
  "Sensors are called readings in the API."
  [device_id session]
  (let [sensors (get-sensors device_id session)]
    (mapv #(dissoc % :user_id) sensors)))
