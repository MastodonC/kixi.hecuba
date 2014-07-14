(ns kixi.hecuba.data.measurements.download
  (:require [kixi.hecuba.storage.db :as db]
            [qbits.hayt             :as hayt])
  )

(defn- relocate-customer-ref [m]
  (-> m
      (dissoc :metadata)
      (assoc :customer_ref (get-in m [:metadata :customer_ref]))))

(defn- relocate-location [m]
  (assoc m :location (get-in m [:location :name])))

(def columns-in-order [:device_id
                       :type
                       :customer_ref
                       :description
                       :location
                       :accuracy
                       :resolution
                       :frequency
                       :period
                       :entity_id ;; NOTE: This is shown as "Parent
                                  ;; UUID" in the samples. Assuming
                                  ;; parent_id column is old API and
                                  ;; using entity_id instead
                       :max
                       :min])

(def ^:private extract-columns-in-order (apply juxt columns-in-order))

(def ^:private headers-in-order ["Device UUID"
                                 "Reading Type"
                                 "Customer Ref"
                                 "Description"
                                 "Location"
                                 "Accuracy (percent)"
                                 "Sample Interval (seconds)"
                                 "Frequency"
                                 "Period"
                                 "Parent UUID"
                                 "Sensor Range Max"
                                 "Sensor Range Min"])

(defn- join-to-device [devices x]
  (let [device_id (:device_id x)]
    (merge (get devices device_id) x)))

(defn get-header [store entity_id]
  (db/with-session [session (:hecuba-session store)]
    (let [devices         (->> (db/execute session (hayt/select :devices (hayt/where [[= :entity_id entity_id]])))
                               (map (fn [x] [(:id x) x]))
                               (into {}))
          sensors         (if (seq devices)
                            (db/execute session (hayt/select :sensors (hayt/where [[:in :device_id (keys devices)]])))
                            [])
          device-and-type #(str (:device_id %) "-" (:type %))]
      (if (seq sensors)
        (->> sensors
             (map (partial join-to-device devices))
             (sort-by device-and-type)
             (map relocate-customer-ref)
             (map relocate-location)
             (map extract-columns-in-order)
             (apply map vector)
             (map #(apply vector %1 %2) headers-in-order))
        []))))


(defn download-item [store item])
