(ns kixi.hecuba.data.measurements.upload
  (:require [clj-time.format           :as tf]
            [clojure.core.match        :refer (match)]
            [clojure.data.csv          :as csv]
            [clojure.java.io           :as io]
            [clojure.string            :as str]
            [clojure.tools.logging     :as log]
            [kixi.hecuba.logging       :as hl]
            [kixi.hecuba.parser        :as parser]
            [kixi.hecuba.data.devices  :as devices]
            [kixi.hecuba.data.measurements.core :refer (prepare-measurement headers-in-order columns-in-order transpose keywordise)]
            [kixi.hecuba.data.measurements.upload.status :as us]
            [kixi.hecuba.data.misc     :as misc]
            [kixi.hecuba.data.devices  :as devices]
            [kixi.hecuba.data.parents  :as parents]
            [kixi.hecuba.data.sensors  :as sensors]
            [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
            [kixi.hecuba.storage.db    :as db]
            [kixi.hecuba.time          :as time]
            [kixipipe.ioplus           :as ioplus]
            [kixipipe.storage.s3       :as s3]
            [qbits.hayt                :as hayt]
            [schema.core               :as s]))

(def full-header-row-count (count headers-in-order))

(defmethod kixipipe.storage.s3/s3-key-from "uploads" uploads-s3-key-from [item]
  (let [suffix   (get item :suffix "data")
        username (-> item :metadata :username)]
    (str "uploads/" username "/" (:entity_id item) "/" suffix)))

(defmethod kixipipe.storage.s3/item-from-s3-key "uploads" uploads-item-from-s3-key [key]
  (when-let [[src-name username entity_id uuid] (next (re-matches #"^([^/]+)/([^/]+)/([^/]+)/([^/]+)$" key))]
    {:src-name src-name
     :metadata {:username username}
     :entity_id entity_id
     :uuid uuid}))

(defn relocate-user-id [{:keys [auth] :as item} x]
  (assoc x :user_id (:id auth)))

(defn date-parser-fn [date-format]
  (if (not-empty date-format)
    (fn [d] (try (tf/parse (tf/formatter date-format) d) (catch Throwable t nil)))
    time/auto-parse))

(defn devices-exist?
  "Assoc the parent entity_id and update :metadata with existence."
  [store devices]
  (let [db-devices (->> (keep :device_id devices) (parents/devices store))
        add-exists (fn [m]
                     (log/infof "Checking exists %s" m)
                     ;; if it exists then just assoc the entity_id as
                     ;; we might be trying to update the device and
                     ;; sensor and don't want to overwrite that with
                     ;; what is from the database
                     (if-let [entity_id (db-devices (:device_id m))]
                       (-> (assoc-in m [:metadata :exists?] true)
                           (assoc :entity_id entity_id))
                       (assoc-in m [:metadata :exists?] false)))]
    (map add-exists devices)))

(defn blank-row? [cells]
  (every? #(re-matches #"\s*" %) cells))

(defn parse-header-rows [date-parser-fn rows]
  (let [invalid-date? (complement date-parser-fn)]
    (->> rows
         (take-while (comp invalid-date? first))
         (remove blank-row?))))

(defn parse-full-header [header-rows]
  (let [[field-labels & devices] (transpose header-rows)
        keywords (mapv keywordise field-labels)]
    (map #(zipmap keywords %) devices)))

(defn identify-file-type
  "Check if alias processing has been requested."
  [_ {:keys [aliases?]}]
  (if aliases?
    :with-aliases
    :full))

(defmulti get-header #'identify-file-type)

(defmethod get-header :full [store {:keys [dir filename date-format] :as item}]
  (let [date-parser (date-parser-fn date-format)
        header-rows (with-open [in (io/reader (io/file dir filename))]
                      (->> in
                           (csv/read-csv)
                           (parse-header-rows date-parser)
                           (doall)))
        header      (->> (parse-full-header header-rows)
                         (devices-exist? store)
                         (map (partial relocate-user-id item)))]
    {:metadata {:row-count (count header-rows)
                :update-devices-and-sensors? true
                :date-parser date-parser}
     :sensors header}))

(defmethod get-header :with-aliases [store item]
  (let [{:keys [dir filename entity_id date-format]} item
        date-parser                      (date-parser-fn date-format)
        header-rows                      (with-open [in (io/reader (io/file dir filename))]
                                           (->> in
                                                (csv/read-csv)
                                                (parse-header-rows date-parser)
                                                (doall)))]
    (db/with-session [session (:hecuba-session store)]
      (let [devices (->> (devices/get-devices session entity_id)
                         (map #(vector (:device_id %) %))
                         (into {}))
            sensors (->> (sensors/get-sensors-by-device_ids (keys devices) session)
                         (remove :synthetic)
                         (map #(vector (:alias %) %))
                         (into {}))
            header  (->> (map (partial str/join \|) (rest (transpose header-rows)))
                         (map #(if-let [s (get sensors %)]
                                 (assoc-in s [:metadata :exists?] true)
                                 {:alias % :metadata {:exists? false}}))
                         (map #(if-let [d (get devices (:device_id %))]
                                 (do (log/infof "merging device %s onto header sensor %s" d %) (merge d %))
                                 %))
                         (map (partial relocate-user-id item)))
            _       (log/infof "%s sensors in header %s" (count header) (vec header))]
        {:metadata {:row-count (count header-rows)
                    :update-devices-and-sensors? false
                    :date-parser date-parser}
         :sensors header}))))

(defn seq-of-seq-of-seqs->seq-of-seq-of-maps [header xs]
  (map #(map (partial zipmap header) %) xs))

(defn vertical-csv->sanity [xs]
  (map (comp transpose vector)
       (repeat (map first xs))
       (transpose (map rest xs))))

(defn update-sensor-data [store sensor]
  (log/info "Updating sensor " (str (:device_id sensor) "-" (:type sensor)))

  (db/with-session [session (:hecuba-session store)]
    (sensors/update session sensor)))

(defn update-device-data [store device]
  (log/info "Updating device " (:device_id device) " in entity " (:entity_id device))
  (db/with-session [session (:hecuba-session store)]
    (devices/update session device)))

(defn split-device-and-sensor [m]
  [(select-keys m [:device_id :description
                   :location :metadata :privacy :metering_point_id])
   (select-keys m [:device_id :type :alias :accuracy :actual_annual :corrected_unit
                   :correction :correction_factor :correction_factor_breakdown
                   :errors :events :frequency :max :median :min :period
                   :resolution :status :synthetic :unit :user_id])])

(def Device {(s/required-key :device_id) s/Str
             (s/optional-key :description)                (s/maybe s/Str)
             (s/optional-key :parent_id)                  (s/maybe s/Str)
             (s/optional-key :entity_id)                  (s/maybe s/Str)
             (s/optional-key :location)                   (s/maybe s/Any)
             (s/optional-key :metadata)                   (s/maybe s/Any)
             (s/optional-key :privacy)                    (s/maybe s/Str)
             (s/optional-key :metering_point_id)          (s/maybe s/Str)})

(def Sensor {(s/required-key :device_id)                   s/Str
             (s/required-key :type)                        s/Str
             (s/optional-key :alias)                       (s/maybe s/Str)
             (s/optional-key :accuracy)                    (s/maybe s/Str)
             (s/optional-key :actual_annual)               (s/maybe s/Bool)
             (s/optional-key :corrected_unit)              (s/maybe s/Str)
             (s/optional-key :correction)                  (s/maybe s/Str)
             (s/optional-key :correction_factor)           (s/maybe s/Str)
             (s/optional-key :correction_factor_breakdown) (s/maybe s/Str)
             (s/optional-key :frequency)                   (s/maybe s/Str)
             (s/optional-key :max)                         (s/maybe s/Str)
             (s/optional-key :median)                      double
             (s/optional-key :min)                         (s/maybe s/Str)
             (s/optional-key :period)                      (s/enum "INSTANT" "PULSE" "CUMULATIVE")
             (s/optional-key :resolution)                  (s/maybe s/Str)
             (s/optional-key :status)                      (s/maybe s/Str)
             (s/optional-key :synthetic)                   s/Bool
             (s/optional-key :unit)                        (s/maybe s/Str)
             ;; (s/enum "%RH" "Amps" "C" "Hz" "L" "Litres/5min" "Ls^-1"
             ;;         "V" "VA" "VAr" "W/m^2" "W/m^2.K" "degrees" "g/Kg"
             ;;         "kVArh" "kW" "kWh" "kWhth" "mA" "mV" "m^3" "ft^3"
             ;;         "m^3" "ft^3" "kWh" "m^3/h" "mbar" "millisecs"
             ;;         "ms^-1" "ppm" "wm-2" "" ;; allow blank for now.
             ;;         )
             (s/required-key :user_id)                     s/Str})

(defn validate-header
  "Takes a header and returns a vector of [device sensor] after splitting.
  meta data will be attached to the vector indicating whether the
  device/sensor is valid"
  [header]
  (let [validated-sensors
        (mapv (fn [device-and-sensor]
                (let [[device sensor] (split-device-and-sensor device-and-sensor)
                      invalid-device? (s/check Device device)
                      invalid-sensor? (s/check Sensor sensor)]
                  (-> device-and-sensor
                      (cond-> invalid-device? (assoc-in [:metadata :device-error] invalid-device?))
                      (cond-> invalid-sensor? (assoc-in [:metadata :sensor-error] invalid-sensor?)))))
              (:sensors header))]
    (assoc header :sensors validated-sensors)))

(defn- valid-header? [{:keys [metadata] :as sensor}]
  (and (:exists? metadata)
       (:allowed? metadata)
       (not (contains? metadata :device-error))
       (not (contains? metadata :sensor-error))))

(defn process-column [store update-devices-and-sensors? date-parser device-and-sensor measurements]
  (log/infof "Attempting to insert: %s" device-and-sensor)
  (let [[device sensor :as ds] (split-device-and-sensor device-and-sensor)]
    (try
      (let [page-size 10
            validated-measurements (->> measurements
                                        (filter #(seq (:timestamp %)))
                                        (map #(-> %
                                                  (prepare-measurement sensor date-parser)
                                                  (v/validate sensor))))]

        ;; Leaving this out for now. We can update via the UI
        ;; (when (and update-devices-and-sensors?
        ;;            (valid-header? device-and-sensor))
        ;;   (update-device-data store device)
        ;;   (update-sensor-data store sensor))

        (if (valid-header? device-and-sensor)
          (do
            (log/infof "Inserting measurements for Sensor: %s:%s metadata: %s" (:device_id sensor) (:type sensor) (:metadata device-and-sensor))
            (misc/insert-measurements store device-and-sensor page-size validated-measurements)
            (assoc-in device-and-sensor [:metadata :inserted] true))
          (do
            (log/infof "Skipping insert for: %s metadata %s" device-and-sensor (:metadata device-and-sensor))
            (assoc-in device-and-sensor [:metadata :inserted] false))))
      (catch Throwable t
        (-> device-and-sensor
            (assoc-in [:metadata :inserted] false)
            (assoc-in [:metadata :error-message] (.getMessage t)))))))

(defn- get-data [header-row-count in]
  (->> in
       (csv/read-csv)
       (drop header-row-count)
       (vertical-csv->sanity)
       (seq-of-seq-of-seqs->seq-of-seq-of-maps [:timestamp :value])))

(defn add-projects [header store]
  (let [sensors (:sensors header)
        entity_ids (set (keep
                         #(when-let [id (:entity_id %)]
                            (-> id str/trim not-empty))
                         sensors))
        properties-to-project (parents/entities store entity_ids)
        add-project (fn [m]
                      (if-let [project_id (properties-to-project (:entity_id m))]
                        (assoc m :project_id project_id)
                        m))]
    (assoc header :sensors (mapv add-project sensors))))

(defn add-programmes [header store]
  (let [sensors (:sensors header)
        project_ids (set (keep
                          #(when-let [id (:project_id %)]
                             (-> id str/trim not-empty))
                          sensors))
        projects-to-programme (parents/projects store project_ids)
        add-programme (fn [m]
                        (if-let [programme_id (projects-to-programme (:project_id m))]
                          (assoc m :programme_id programme_id)
                          m))]
    (assoc header :sensors (mapv add-programme sensors))))

(defn allowed? [{:keys [programme_id project_id]} programmes projects role]
  (log/infof "allowed? programme_id: %s project_id: %s programmes: %s projects: %s roles: %s"
             programme_id project_id programmes projects role)
  (match  [(has-admin? role)
           (has-programme-manager? programme_id programmes)
           (has-project-manager? project_id projects)]
          ;; super-admin - do everything
          [true _ _] true
          ;; programme-manager for this programme - do everything
          [_ true _] true
          ;; project-manager for this project - do everything
          [_ _ true] true
          :else false))

(defn- enrich-with-authz [header store {:keys [projects programmes role]}]
  (let [sensors (:sensors header)
        add-allowed (fn [m]
                      (if (get-in m [:metadata :exists?])
                        (assoc-in m [:metadata :allowed?] (allowed? m programmes projects role))
                        m))]
    (assoc header :sensors (map #(add-allowed %) (:sensors header)))))

(defn- parse-file-to-db [store header in-file]
  (with-open [in (io/reader in-file)]
    (log/info "Processing " (count (:sensors header)) " data columns")
    (let [{:keys [row-count update-devices-and-sensors? date-parser]} (:metadata header)
          data   (get-data row-count in)
          report (mapv
                  (partial process-column store update-devices-and-sensors? date-parser)
                  (:sensors header) data)]
      (log/info "Finished Processing " (count (:sensors header)) " data columns")
      report)))

(defn db-store [store item]
  (let [header  (-> (get-header store item)
                    (validate-header)
                    (add-projects store)
                    (add-programmes store)
                    (enrich-with-authz store (:auth item)))
        in-file (io/file (:dir item) (:filename item))]
    (try
      (parse-file-to-db store header in-file)
      (finally (ioplus/delete! in-file)))))

(defn write-status [item store]
  (let [{:keys [metadata entity_id date-format date status report]} item
        {:keys [username filename]} metadata]
    (us/insert {:entity_id entity_id
                :event_time date
                :filename filename
                :status status
                :username username
                :report (or report {})}
               store)))

(defn has-errors? [report]
  (log/infof "has-errors report: %s" report)
  (every? seq (map #(get-in % [:metadata :error-message]) report)))

(defn upload-item [store item]
  (log/infof "Storing upload: %s" item)
  (write-status (assoc item :status "STORING") store)
  (s3/store-file (:s3 store) item)
  (write-status (assoc item :status "STORED") store)
  (log/infof "Stored upload: %s" item)
  (let [tmpfile (ioplus/mk-temp-file! "hecuba" "measurements")
        newitem (assoc item
                  :dir      (.getParent tmpfile)
                  :filename (.getName tmpfile))]
    (try
      (log/infof "Processing upload: %s" newitem)
      (write-status (assoc newitem :status "PROCESSING") store)
      (parser/normalize-line-endings! (io/file (:dir item)
                                               (:filename item)) tmpfile)
      (.delete (io/file (:dir item) (:filename item)))
      (let [report (db-store store newitem)]
        (if (has-errors? report)
          (do (log/infof "Processing upload FAILED: %s" newitem)
              (write-status (assoc newitem :status "FAILURE" :report report) store))
          (do (log/infof "Processing upload COMPLETE: %s" newitem)
              (write-status (assoc newitem :status "COMPLETE" :report report) store))))
      (catch Throwable t
        (log/errorf t "Uploading item %s failed." newitem)
        (write-status (assoc newitem :status "FAILURE" :report (str (ex-data t))) store)))))
