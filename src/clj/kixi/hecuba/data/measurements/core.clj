(ns kixi.hecuba.data.measurements.core
  (:require [cheshire.core         :as json]
            [clj-time.coerce       :as tc]
            [clojure.tools.logging :as log]
            [kixipipe.ioplus       :as ioplus]
            [kixi.hecuba.time      :as time]
            [kixipipe.storage.s3   :as s3]))

(def columns-in-order [:device_id
                       :type
                       :customer_ref
                       :description
                       :location
                       :unit
                       :accuracy
                       :resolution
                       :frequency
                       :period
                       :entity_id ; labelled as Parent UUID
                       :max
                       :min])

(def extract-columns-in-order (apply juxt columns-in-order))

(def headers-in-order ["Device UUID"
                       "Reading Type"
                       "Customer Ref"
                       "Description"
                       "Location"
                       "Unit"
                       "Accuracy (percent)"
                       "Sample Interval (seconds)"
                       "Frequency"
                       "Period"
                       "Parent UUID"
                       "Sensor Range Max"
                       "Sensor Range Min"])

(defn keywordise [label]
  (get
   {"Unit" :unit
    "Period" :period
    "Sample Interval (seconds)" :resolution
    "Customer Ref" :customer_ref
    "Sensor Range Max" :max
    "Parent UUID" :entity_id
    "Location" :location
    "Description" :description
    "Reading Type" :type
    "Device UUID" :device_id
    "Accuracy (percent)" :accuracy
    "Frequency" :frequency
    "Sensor Range Min" :min}
   label))

(defn get-status [{:keys [s3]} item]
  ;; TODO there should be a better way to do this in s3 ns.
  (let [s3-key (s3/s3-key-from (assoc item :suffix "status"))]
    (when (s3/item-exists? s3 s3-key)
      (with-open [in (s3/get-object-by-metadata s3 {:key s3-key})]
        (:status (json/parse-string (slurp in) keyword))))))

(defn write-status [store item]
  (let [status-file (ioplus/mk-temp-file! "hecuba" ".tmp")]
    (try
      (spit status-file (json/generate-string (select-keys item [:status :data])))
      (s3/store-file (:s3 store) (-> item
                                     (assoc
                                         :dir (.getParent status-file)
                                         :filename (.getName status-file)
                                         :suffix "status"
                                         :content-type "application/json")
                                     (dissoc :content-disposition)))
      (finally (ioplus/delete! status-file)))))

(defn transpose [xs]
  (if (seq xs)
    (apply map vector xs)
    []))

(defn prepare-measurement [m sensor date-parser]
  (try
    (let [t  (tc/to-date (date-parser (:timestamp m)))]
      {:device_id        (:device_id sensor)
       :sensor_id        (:sensor_id sensor)
       :timestamp        t
       :value            (str (:value m))
       :error            (str (:error m))
       :month            (time/get-month-partition-key t)
       :reading_metadata {}})
    (catch Throwable t
      (log/errorf t "For sensor %s Unable to prepare measurement: %s" sensor m)
      (throw (Exception. (format "For sensor %s:%s Unable to prepare measurement: %s" (:device_id sensor) (:type sensor) m) t)))))
