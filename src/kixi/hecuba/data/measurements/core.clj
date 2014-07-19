(ns kixi.hecuba.data.measurements.core
  (:require [cheshire.core :as json]
            [kixipipe.ioplus               :as ioplus]
            [kixipipe.storage.s3           :as s3]))

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

(def extract-columns-in-order (apply juxt columns-in-order))

(def headers-in-order ["Device UUID"
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

(defn write-status [store item]
  (let [status-file (ioplus/mk-temp-file! "hecuba" ".tmp")]
    (try
      (spit status-file (json/generate-string (select-keys item [:status :data])))
      (s3/store-file (:s3 store) (assoc item
                                   :dir (.getParent status-file)
                                   :filename (.getName status-file)))
      (finally (ioplus/delete! status-file)))))
