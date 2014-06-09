(ns migrate
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.api.measurements :as measurements]
            [clojure.tools.logging :as log]))

(defn do-map
  "Map with side effects."
  [f & lists]
  (apply mapv f lists) nil)

(defn convert-metadata 
  "Reads stringified metadata into a clojure map."
  [m]
  (let [metadata (:metadata m)]
    (if metadata
      (read-string metadata)
      nil)))

(defn migrate-reading-metadata 
  "Works on a lazy sequence of all measurements for all sensors in the database and
  populates (new) reading_metadata with data coming from (old) metadata."
  [{:keys [store]}]
  (log/info "Migrating reading metadata.")
  (db/with-session [session (:hecuba-session store)]
    (let [sensors (db/execute session (hayt/select :sensors))]
      (doseq [s sensors]
        (prn "Sensor:" (:device_id s) (:type s))
        (let [measurements (measurements/all-measurements store s)]
          (do-map #(db/execute session
                               (hayt/update :measurements
                                            (hayt/set-columns :reading_metadata (convert-metadata %))
                                            (hayt/where [[= :device_id (:device_id %)]
                                                         [= :type (:type %)]
                                                         [= :month (:month %)]
                                                         [= :timestamp (:timestamp %)]]))) measurements)))))
  (log/info "Finished migrating reading metadata."))
