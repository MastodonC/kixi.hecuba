(ns kixi.hecuba.storage.sha1
  "Functions to generate sha1 keys."
  (:require [kixi.hecuba.hash :refer (sha1)]
            [clojure.tools.logging :as log]))

(defn sha1-keyfn
  "From a given payload, compute an id that is a SHA1 dependent on the given types."
  [& types]
  (fn [payload]
    (assert payload "payload cannot be nil")
    (assert (every? payload (set types))
            (format "Cannot form a SHA1 because required types (%s) are missing from the payload: %s"
                    (apply str (interpose ", " (clojure.set/difference (set types) (set (keys payload)))))
                    payload))
    (-> payload ((apply juxt types)) pr-str sha1)))

(defmulti gen-key (fn [typ payload] typ))
(defmethod gen-key :programme [typ payload] ((sha1-keyfn :name) payload))
(defmethod gen-key :project [typ payload] ((sha1-keyfn :name :programme_id) payload))
(defmethod gen-key :entity [typ payload] ((sha1-keyfn :property_code :project_id) payload))
(defmethod gen-key :device [typ payload] ((sha1-keyfn :description :entity_id) payload))
(defmethod gen-key :profile [typ payload] ((sha1-keyfn :timestamp :entity_id) payload))

(defmethod gen-key :sensor [typ payload] nil)
(defmethod gen-key :sensor_metadata [typ payload] nil)
(defmethod gen-key :measurement [typ payload] nil)
(defmethod gen-key :hourly_rollups [typ payload] nil)
(defmethod gen-key :daily_rollups [typ payload] nil)

(defmethod gen-key :user [typ payload] (:username payload))
(defmethod gen-key :user-session [typ payload] (:id payload))
(defmethod gen-key :dataset [typ payload] nil)
