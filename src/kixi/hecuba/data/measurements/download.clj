(ns kixi.hecuba.data.measurements.download
  (:require [clj-time.coerce               :as tc]
            [clojure.data.csv              :as csv]
            [clojure.java.io               :as io]
            [clojure.tools.logging         :as log]
            [kixi.hecuba.data.calculate    :as calculate]
            [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.measurements.core :refer (headers-in-order extract-columns-in-order)]
            [kixi.hecuba.storage.db        :as db]
            [kixi.hecuba.webutil           :refer (uuid)]
            [kixipipe.ioplus               :as ioplus]
            [kixipipe.storage.s3           :as s3]
            [qbits.hayt                    :as hayt]
            [clj-time.core                 :as t]
            [clj-time.coerce               :as tc]
            [kixi.hecuba.data.measurements.core :refer (write-status)]))

(defn- relocate-customer-ref [m]
  (-> m
      (dissoc :metadata)
      (assoc :customer_ref (get-in m [:metadata :customer_ref]))))

(defn- relocate-location [m]
  (assoc m :location (get-in m [:location :name])))

(defn mk-temp-directory! [prefix & [attrs]]
  (java.nio.file.Files/createTempDirectory
   prefix
   (into-array java.nio.file.attribute.FileAttribute attrs)))

(defn- join-to-device [devices x]
  (let [device_id (:device_id x)]
    (merge (get devices device_id) x)))

(defn min-date-time [& ts]
  (reduce (fn [t1 t2]  (if (t/before? t1 t2) t1 t2)) (map (fnil tc/to-date-time (t/date-time 3000)) ts))) ;; TODO a Y3K problem?

(defn max-date-time [& ts]
  (reduce (fn [t1 t2]  (if (t/after? t1 t2) t1 t2)) (map (fnil tc/to-date-time (t/date-time 1970) ) ts)))

(defn- find-ts-range [xs]
  (when (seq xs)
   (let [tss (map (juxt :lower_ts :upper_ts) xs)]
     (reduce (fn [[l1 u1] [l2 u2]] [(min-date-time l1 l2)
                                   (max-date-time u1 u2)])
             tss))))

(defn- get-devices-and-sensors-for [session entity_id]
  (let [devices         (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
                             (map (fn [x] [(:id x) x]))
                             (into {}))
        sensors         (if (seq devices)
                          (db/execute session (hayt/select :sensors (hayt/where [[:in :device_id (keys devices)]])))
                          [])
        sensor-metadata (if (seq sensors)
                          (db/execute session (hayt/select :sensor_metadata
                                                           (hayt/columns :lower_ts :upper_ts)
                                                           (hayt/where [[:in :device_id (keys devices)]]))))
        device-and-type #(str (:device_id %) "-" (:type %))]
    (with-meta (if (seq sensors)
                 (->> sensors
                      (map (partial join-to-device devices))
                      (sort-by device-and-type))
                 [])
      {:ts-range       (find-ts-range sensor-metadata)})))

(defn format-header [devices-and-sensors]
  (->> devices-and-sensors
       (map relocate-customer-ref)
       (map relocate-location)
       (map extract-columns-in-order)
       (apply map vector)
       (map #(apply vector %1 %2) headers-in-order)))

(defn get-header [store entity_id]
  (db/with-session [session (:hecuba-session store)]
    (->> entity_id
         (get-devices-and-sensors-for session)
         (format-header))))

(defn indexes-of [v xs]
  (set (keep-indexed (fn [i x] (when (= x v) i)) xs)))

(defn filled-measurements [measurements]
  (let [sentinel         (Object.)
        is-sentinel?     #(identical? % sentinel)
        next-or-sentinel #(if-let [n (next %)] n [sentinel])
        replace-empty    (partial map (fn [x] (if (seq x) x [sentinel])))
        next-row         (fn [xs]
                           (when-let [row (first (apply map vector (replace-empty xs)))]
                             (when (not-every? is-sentinel? row)
                               row)))]
    (loop [ms measurements data (list)]
      (let [row (next-row ms)]
        (if-not row
          (if (seq data)
            (apply map vector data)
            (list))
          (let [row-timestamps (mapv :timestamp row)
                row-timestamp  (first (sort (remove nil? row-timestamps)))
                data-indexes   (indexes-of row-timestamp row-timestamps)
                has-data?      #(contains? data-indexes %)
                out-row        (map-indexed (fn [i x] (if (has-data? i)
                                                       (assoc x :timestamp row-timestamp)
                                                       (hash-map :timestamp row-timestamp :value nil))) row)
                fns            (map-indexed (fn [i _] (if (has-data? i) next-or-sentinel identity)) row)
                step           (fn [xs] (map (fn [f v] (f v)) fns xs))]
            (recur (step ms) (concat data [out-row]))))))))

(defn generate-file [store item]
  (let [{:keys [header data]} item
        tmpfile (ioplus/mk-temp-file! "hecuba" ".csv-download")]
    (try
      (with-open [out (io/writer tmpfile)]
        (csv/write-csv out header)
        (csv/write-csv out (filled-measurements data))
        (s3/store-file (:s3 store) (update-in item [:uuid] str "/data")))
      (write-status store (assoc (update-in item [:uuid] str "/status") :status "SUCCESS"))
      (catch Throwable t
        (log/error t "failed")
        (write-status store (assoc (update-in item [:uuid] str "/status") :status "FAILURE" :data (ex-data t))))
      (finally (.delete tmpfile)))))

(defn download-item [store item]
  (db/with-session [session (:hecuba-session store)]
    (let [devices-and-sensors (get-devices-and-sensors-for session (:entity_id item))
          formatted-header    (format-header devices-and-sensors)
          [lower upper]       (:ts-range (meta devices-and-sensors))
          {:keys [start-date
                  end-date] :or {start-date lower end-date upper}} item
          _ (log/info "S:" start-date ", E:" end-date)
          measurements        (map (fn [m] (measurements/retrieve-measurements session
                                                                              start-date
                                                                              end-date
                                                                              (:device_id m)
                                                                              (:type m)))
                                   devices-and-sensors)
          uuid                (uuid)]
      (generate-file store
                     (-> item
                         (assoc :header formatted-header
                                :data measurements))))))
