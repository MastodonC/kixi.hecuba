(ns migrate
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.api.measurements :as measurements]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]))

(defn do-map
  "Map with side effects."
  [f & lists]
  (apply mapv f lists) nil)

(defn convert-metadata
  "Reads stringified metadata into a clojure map."
  [m]
  (let [metadata (:metadata m)]
    (if metadata
      (clojure.walk/stringify-keys (read-string metadata))
      nil)))

(defn migrate-reading-metadata
  "Works on a lazy sequence of all measurements for all sensors in the database and
  populates (new) reading_metadata with data coming from (old) metadata."
  [{:keys [store]}]
  (log/info "Migrating reading metadata.")
  (db/with-session [session (:hecuba-session store)]
    (let [sensors (db/execute session (hayt/select :sensors))]
      (doseq [s sensors]
        (let [measurements (measurements/all-measurements store s)]
          (when measurements
            (doseq [m measurements]
               (db/execute session
                           (hayt/insert :partitioned_measurements
                                        (hayt/values {:device_id  (:device_id m)
                                                      :type       (:type m)
                                                      :month      (:month m)
                                                      :timestamp  (:timestamp m)
                                                      :value      (:value m)
                                                      :error      (:error m)
                                                      :reading_metadata (convert-metadata m)})))))))))
  (log/info "Finished migrating reading metadata."))

(defn fill-sensor-bounds
  "Gets very first measurement row for each sensor and updates lower_ts in sensor_metadata with its timestamp.
  Upper_ts is populated with current date."
  [{:keys [store]}]
  (log/info "Populating sensor bounds.")
  (db/with-session [session (:hecuba-session store)]
    (let [sensors (db/execute session (hayt/select :sensors))]
      (doseq [s sensors]
        (let [where [[= :device_id (:device_id s)]
                     [= :type (:type s)]]
              first-ts (:timestamp (first (db/execute session
                                                      (hayt/select :measurements
                                                                   (hayt/where where)
                                                                   (hayt/limit 1)))))
              last-ts  (:timestamp (first (db/execute session
                                                      (hayt/select :measurements
                                                                   (hayt/where where)
                                                                   (hayt/order-by [:type :desc])
                                                                   (hayt/limit 1)))))]
          (db/execute session (hayt/update :sensor_metadata
                                           (hayt/set-columns :upper_ts last-ts
                                                             :lower_ts first-ts)
                                           (hayt/where where)))))))
  (log/info "Finished populating sensor bounds."))
