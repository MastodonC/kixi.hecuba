(ns migrate
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.api.measurements :as measurements]
            [kixi.hecuba.data.misc :as misc]
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
  [_ m]
  (let [metadata (:metadata m)]
    (if metadata
      (clojure.walk/stringify-keys (read-string metadata))
      nil)))

(def processed-file "/tmp/processed_sensors.txt")

(defn file-exists? [filename]
  (.exists (clojure.java.io/as-file filename)))

(defn migrate-reading-metadata
  "Works on a lazy sequence of all measurements for all sensors in the database and
  populates (new) reading_metadata with data coming from (old) metadata."
  [{:keys [store]}]
  (log/info "Migrating reading metadata.")
  (db/with-session [session (:hecuba-session store)]
    (let [all-sensors (db/execute session (hayt/select :sensors))
          processed-sensors (if (file-exists? processed-file)
                                (map clojure.edn/read-string (clojure.string/split-lines (slurp processed-file)))
                                [])
          sensors (remove (set processed-sensors) all-sensors)]
      (doseq [s sensors]
        (let [measurements (measurements/all-measurements store s)
              measurements-with-metadata (map #(update-in % [:reading_metadata] convert-metadata %)
                                              measurements)]
          (when measurements-with-metadata
            (misc/insert-measurements store measurements-with-metadata 100)))
        (spit "/tmp/processed_sensors.txt" (str s "\n") :append true))))
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
