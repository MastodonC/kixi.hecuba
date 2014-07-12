(ns kixi.hecuba.data.measurements.upload
  (:require [clojure.data.csv          :as csv]
            [clojure.java.io           :as io]
            [clojure.set               :as set]
            [clojure.tools.logging     :as log]
            [kixi.hecuba.api.templates :as templates]
            [kixi.hecuba.data.misc     :as misc]
            [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.webutil       :as util]
            [kixipipe.storage.s3       :as s3]
            [qbits.hayt                :as hayt]
            [kixi.hecuba.storage.db    :as db]
            [clj-time.format           :as tf]
            [clj-time.coerce           :as tc]
            [schema.core               :as s]
            ))

(def header-row-count 12)

(defn convert-to-maps [xs]
  (map (partial zipmap templates/columns-in-order) xs))

(defn transpose [xs]
  (apply map vector xs))

(defn get-header [item]
  (with-open [in (io/reader (io/file (:dir item) (:filename item)))]
    (->> in
         (csv/read-csv)
         (take header-row-count)
         (map (partial drop 1)) ;; first column is header headings ;-/
         (transpose)
         (convert-to-maps)
         (doall))))

(defn seq-of-seq-of-seqs->seq-of-seq-of-maps [header xs]
  (map #(map (partial zipmap header) %) xs))

(defn vertical-csv->sanity [xs]
  (map (comp transpose vector)
       (repeat (map first xs))
       (transpose (map rest xs))))

;; TODO - this is copied from api.measurements.
(defn prepare-measurement [m sensor]
  (let [t  (tc/to-date (tf/parse (:timestamp m)))]
    {:device_id        (:device_id sensor)
     :type             (:type sensor)
     :timestamp        t
     :value            (str (:value m))
     :error            (str (:error m))
     :month            (util/get-month-partition-key t)
     :reading_metadata {}}))

(defn prepare-sensor [sensor]
  ;;TODO - what preparation should we do?
  sensor)

(defn prepare-device [device]
  (-> device
      (dissoc :device_id)
      (assoc :id (:device_id device)))  )

(defn update-sensor-data [store sensor]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session
                (hayt/update :sensors
                             (hayt/set-columns (dissoc sensor :device_id :type))
                             (hayt/where (select-keys sensor [:device_id :type]))))))

(defn update-device-data [store device]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session
                (hayt/update :devices
                             (hayt/set-columns (dissoc device :id))
                             (hayt/where (select-keys device [:id]))))))

(defn split-device-and-sensor [m]
  [(select-keys m [:device_id :description :parent_id :entity_id
                   :location :metadata :privacy :metering_point_id])
   (select-keys m [:device_id :type :accuracy :actual_annual :corrected_unit
                   :correction :correction_factor :correction_factor_breakdown
                   :errors :events :frequency :max :median :min :period
                   :resolution :status :synthetic :unit :user_id])])

(defn programme-ids-from
  "For the given project ids returns a map {<project-id> <programme_id>}"
  [store project-ids]
  (->> (db/with-session [session (:hecuba-session store)]
         (db/execute (hayt/select :projects
                                  (hayt/columns [:id :programme_id])
                                  (hayt/where [[:in :id project-ids]]))))
       (map #((juxt [:id :programme_id] %)))
       (into {})))

(defn- parse-file-to-db [store header in-file ]
  (with-open [in (io/reader in-file)]
    (let [data (->> in
                    (csv/read-csv)
                    (drop header-row-count)
                    (vertical-csv->sanity)
                    (seq-of-seq-of-seqs->seq-of-seq-of-maps [:timestamp :value]))]
      (loop [[device-and-sensor & more-headers] header
             [measurements & more-measurements] data]
        (when (and device-and-sensor measurements)
          (let [[device sensor]        (split-device-and-sensor device-and-sensor)
                validated-measurements (map #(-> %
                                                 (prepare-measurement sensor)
                                                 (v/validate sensor))
                                            measurements)
                page-size              10]
            (update-device-data store (prepare-device device))
            (update-sensor-data store (prepare-sensor sensor))
            (misc/insert-measurements store sensor validated-measurements page-size)
            (recur more-headers more-measurements)))))))

(defn- devices-by-project-id [header]
  (reduce (fn [a [k v]] (update-in a [k] conj v)) {} header))

(defn- devices-by-programme-id [store devices-by-project-id]
  (let [projects-to-programmes (programme-ids-from store (keys devices-by-project-id))]
    (into {} (map #(vector (get projects-to-programmes %2) %1) devices-by-project-id))))

(defn- disallowed [{:keys [projects programmes]}
                   devices-by-programme-id devices-by-project-id]
  (let [not-allowed-projects   (set/difference (keys devices-by-project-id) projects)
        not-allowed-programmes (set/difference (keys devices-by-programme-id) programmes)]
    (when-not (and (empty? not-allowed-projects)
                   (empty? not-allowed-programmes))
      {:projects not-allowed-projects
       :programmes not-allowed-programmes})))

(defn db-store [store item]
  (let [header     (get-header item)
        in-file    (io/file (:dir item) (:filename item))
        disallowed (disallowed (:auth item)
                               (devices-by-programme-id store devices-by-project-id)
                               (devices-by-project-id header))]
    (if-not disallowed
      (parse-file-to-db store header in-file)
      ;; Here it would be nice to report 'device 123 is not allowed
      ;; 'cos not authorized on project 456 and/or programme 789, but
      ;; for not we just report the projects/programmes
      (throw (ex-info "Not Authorized" disallowed)))
    (.delete in-file)))

(defn write-status [store item]
  (let [status-file (java.io.File/createTempFile "hecuba" ".tmp")]
    (spit status-file (str  "{\"result\": \"" (:status item) "\"}") )
    (s3/store-file (:s3 store) (assoc item
                                 :dir (.getParent status-file)
                                 :filename (.getName status-file)))
    (.delete status-file)))

(defn upload-item [store item]
  (log/info "UUID:" item)
  (s3/store-file (:s3 store) (update-in item [:uuid] str "/data"))
  (try
    (db-store store item)
    (write-status store (assoc (update-in item [:uuid] str "/status") :status "SUCCESS"))
    (catch Throwable t
      (write-status store (assoc (update-in item [:uuid] str "/status") :status "FAILURE" :data (ex-data t))))))
