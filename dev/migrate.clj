(ns migrate
  (:require [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.sensors :as sensors]
            [kixi.hecuba.time :as time]
            [clojure.tools.logging :as log]
            [clj-time.core :as t]
            [clj-time.format :as tf]
            [clj-time.coerce :as tc]
            [kixi.hecuba.data.parents :as parents]
            [kixi.hecuba.data.entities.search :as search]
            [clojure.edn :as edn]
            [cheshire.core :as json]))

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

(defn all-measurements
  "Returns a sequence of all the measurements for a sensor
   matching (type,device_id). The sequence pages to the database in the
   background. The page size is a clj-time Period representing a range
   in the timestamp column. page size defaults to (clj-time/hours 1)"
  ([store sensor_id & [opts]]
     (let [{:keys [type device_id]} sensor_id
           {:keys [page start end] :or {page (t/hours 1)}} opts
           [start end] (measurements/resolve-start-end store type device_id start end)]
       (when (and start end)
         (let  [next-start (t/plus start page)]
           (db/with-session [session (:hecuba-session store)]
             (lazy-cat (db/execute session
                                   (hayt/select :measurements
                                                (hayt/where [[= :device_id device_id]
                                                             [= :type type]
                                                             [= :month (time/get-month-partition-key start)]
                                                             [>= :timestamp start]
                                                             [< :timestamp next-start]]))
                                   nil)
                       (when (t/before? next-start end)
                         (all-measurements store sensor_id (merge opts {:start next-start :end end}))))))))))

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
        (let [measurements (all-measurements store s)
              measurements-with-metadata (->> measurements
                                              (map #(update-in % [:reading_metadata] convert-metadata %))
                                              (map #(dissoc % :metadata)))]
          (when measurements-with-metadata
            (db/with-session [session (:hecuba-session store)]
              (let [{:keys [min-date max-date]} (measurements/insert-measurements store s 100 measurements-with-metadata)]
                (sensors/update-sensor-metadata session s min-date max-date)))))
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

(defn migrate-types
  "Migrates sensor types from sensor_id column (old type column) to
  type column. Takes < 2 minutes"
  [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (let [sensors (db/execute session (hayt/select :sensors))]
      (doseq [s sensors]
        (let [where [[= :device_id (:device_id s)]
                     [= :sensor_id (:sensor_id s)]]
              typ   (:sensor_id s)]
          (db/execute session (hayt/update :sensors
                                           (hayt/set-columns :type typ)
                                           (hayt/where where)))))
      (search/refresh-search (:hecuba-session store) (:search-session store)))))

(defn reset-broken-location
  "Some devices have a strigified edn in location (should be json). This deletes it."
  [{:keys [store]}]
  (db/with-session [session (:hecuba-session store)]
    (let [devices (db/execute session (hayt/select :devices))]
      (doseq [d devices]
        (let [where    [[= :id (:id d)]]
              location (:location d)]
          (when (seq location)
            (try
              (json/decode location) ;; don't have to do anything when location is stringified json
              (catch Throwable t
                (log/errorf "Could not parse location %s for device %s. Attempting to parse as edn."
                            location (:id d))
                (let [location-edn (edn/read-string location)
                      location-json (json/encode location-edn)]
                  (db/execute session (hayt/update :devices
                                                 (hayt/set-columns :location location-json)
                                                 (hayt/where where)))))))))
      (search/refresh-search (:hecuba-session store) (:search-session store)))))
