(ns kixi.hecuba.data.measurements.upload.status
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [cheshire.core :as json]
            [schema.core :as s]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.logging :as hl]))

(def Status
  {(s/required-key :entity_id)  s/Str
   (s/optional-key :event_time) s/Any ;; should be a date
   (s/required-key :filename)   s/Str
   (s/required-key :username)   s/Str
   (s/required-key :status)     s/Str
   (s/required-key :report)     s/Any})

(defn decode [status]
  (-> status
      (assoc :report (json/parse-string (:report status) keyword))))

(defn encode [status]
  (-> status
      (assoc :report (json/encode (:report status)))))

(defn get-by-id [entity_id filename store]
  (db/with-session [session (:hecuba-session store)]
    (let [[status & rest] (db/execute
                           session
                           (hayt/select :upload_status
                                        (hayt/where [[= :entity_id entity_id]
                                                     [= :filename filename]])))]
      (decode status))))

(defn get-statuses [entity_id store]
  (db/with-session [session (:hecuba-session store)]
    (let [statuses (db/execute session
                               (hayt/select :upload_status
                                            (hayt/where [[= :entity_id entity_id]])))]
      (mapv decode statuses))))

(defn insert [status store]
  (s/validate Status status)
  (db/with-session [session (:hecuba-session store)]
    (try
      (let [encoded-status (encode status)]
        (db/execute session
                    (hayt/insert :upload_status
                                 (hayt/values encoded-status))))
      (catch Throwable t
        (log/errorf "Could not insert: %s" (pr-str status))
        (throw t)))))
