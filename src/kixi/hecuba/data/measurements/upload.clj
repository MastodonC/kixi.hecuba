(ns kixi.hecuba.data.measurements.upload
  (:require [cheshire.core             :as json]
            [clj-time.coerce           :as tc]
            [clj-time.format           :as tf]
            [clojure.core.match        :refer (match)]
            [clojure.data.csv          :as csv]
            [clojure.java.io           :as io]
            [clojure.set               :as set]
            [clojure.string            :as str]
            [clojure.tools.logging     :as log]
            [kixi.hecuba.api.templates :as templates]
            [kixi.hecuba.data.devices  :as devices]
            [kixi.hecuba.data.measurements.core :refer (headers-in-order columns-in-order write-status transpose)]
            [kixi.hecuba.data.misc     :as misc]
            [kixi.hecuba.data.entities :as entities]
            [kixi.hecuba.data.devices  :as devices]
            [kixi.hecuba.data.sensors  :as sensors]
            [kixi.hecuba.data.validate :as v]
            [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
            [kixi.hecuba.storage.db    :as db]
            [kixi.hecuba.webutil       :as util]
            [kixipipe.ioplus           :as ioplus]
            [kixipipe.storage.s3       :as s3]
            [qbits.hayt                :as hayt]
            [schema.core               :as s]))

(def full-header-row-count 12)

(defn merge-meta [obj meta]
  (with-meta obj (merge (meta obj) meta)))

(defn convert-to-maps [xs]
  (map (partial zipmap columns-in-order) xs))

(defn identify-file-type
  "Assumption is that if cell r0,c0 doesn't have the name of the first
  header and cell r1,c0 has a date then we've got a file with aliases,
  otherwise it's a full header."
  [_ {:keys [dir filename]}]
  (let [[r0 r1] (with-open [in (io/reader (io/file dir filename))]
                  (->> in
                       (csv/read-csv)
                       (take 2)
                       (doall)))
        r0c0 (get r0 0 ::dummy-value)]
    (if-not (= (first headers-in-order) r0c0)
      :with-aliases
      :full)))

(defn relocate-user-id [{:keys [auth] :as item} x]
  (assoc x :user_id (:id auth)))

(defmulti get-header #'identify-file-type)

(defmethod get-header :full [_ item]
  (let [{:keys [dir filename]} item]
    (with-meta
      (with-open [in (io/reader (io/file dir filename))]
        (->> in
             (csv/read-csv)
             (take full-header-row-count)
             (map (partial drop 1)) ;; first column is header headings ;-/
             (transpose)
             (convert-to-maps)
             (map (partial relocate-user-id item))
             (doall)))
      {::row-count full-header-row-count
       ::update-devices-and-sensors? true})))

(defmethod get-header :with-aliases [store item]
  (let [{:keys [dir filename entity_id]} item
        blank-row? (fn [cells] (every? #(re-matches #"\s*" %) cells))
        header-rows   (with-open [in (io/reader (io/file dir filename))]
                                     (->> in
                                          (csv/read-csv)
                                          (take-while (complement (comp tf/parse first)))
                                          (remove blank-row?)
                                          (doall)))]
    (db/with-session [session (:hecuba-session store)]
      (let [entity                   (entities/get-by-id session entity_id)
            devices                  (devices/get-devices session entity_id)
            sensors                  (sensors/get-sensors-by-device_ids (map :id devices) session)
            aliases                  (map (partial str/join \|) (rest (transpose header-rows)))
            sensors-in-alias-order   (for [a aliases s sensors :when (= a (:alias s))] s)
            ds-and-ss-in-alias-order (for [s sensors-in-alias-order
                                           d devices
                                           :when (= (:id d) (:device_id s))]
                                       (merge d s))
            header (map relocate-user-id item ds-and-ss-in-alias-order)]
        (with-meta
          header
          {::row-count (count header-rows)
           ::update-devices-and-sensors? false})))))

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
  (log/info "Updating sensor " (str (:device_id sensor) "-" (:type sensor)))

  (db/with-session [session (:hecuba-session store)]
    (sensors/update session sensor)))

(defn update-device-data [store device]
  (log/info "Updating device " (:id device))
  (db/with-session [session (:hecuba-session store)]
    (devices/update session device)))

(defn split-device-and-sensor [m]
  [(select-keys m [:device_id :description
                   :location :metadata :privacy :metering_point_id])
   (select-keys m [:device_id :type :alias :accuracy :actual_annual :corrected_unit
                   :correction :correction_factor :correction_factor_breakdown
                   :errors :events :frequency :max :median :min :period
                   :resolution :status :synthetic :unit :user_id])])

(defn- get-ids->parent [store table key ids]
    (let [result
        (->> (db/with-session [session (:hecuba-session store)]
               (db/execute session (hayt/select table
                                                (hayt/columns :id key)
                                                (hayt/where [[:in :id (vec ids)]]))))
             (keep #(when-let [v (not-empty (get % key))] [(:id %) v]))
             (into {}))]
    result))


(defn projects-to-programme
  "For the given project ids returns a map {<project-id> <programme_id>}"
  [store project-ids]
  (get-ids->parent store :projects :programme_id project-ids))

(defn properties-to-project
  "For the given device ids returns a map {<property_id> <project-id>}"
  [store entity-ids]
  (get-ids->parent store :entities :project_id entity-ids))

(defn devices-to-property
  "For the given device ids returns a map {<device-id> <property_id>} (property_id is really entity_id"
  [store device-ids]
  (get-ids->parent store :devices :entity_id device-ids))

(defn validate-header
  "Takes a header and returns a vector of [device sensor] after splitting.
   meta data will be attached to the vector indicating
   whether the device/sensor is valid" [header]

   (map (fn [device-and-sensor]
          (let [[device sensor] (split-device-and-sensor device-and-sensor)
                invalid-device? (devices/invalid? device)
                invalid-sensor? (sensors/invalid? sensor)]
            (-> device-and-sensor
                (cond-> invalid-device? (merge-meta {:device-error invalid-device?}))
                (cond-> invalid-sensor? (merge-meta {:sensor-error invalid-sensor?}))))) header))

(defn- valid-header? [x]
  (let [m (meta x)]
    (not (or (:non-existent? x)
             (:not-allowed? x)
             (contains? x :device-error)
             (contains? x :sensor-error)))))

(defn process-column [store page-size update-devices-and-sensors? device-and-sensor measurements ]
  (let [[device sensor :as ds] (split-device-and-sensor device-and-sensor)
        validated-measurements (map #(-> %
                                         (prepare-measurement sensor)
                                         (v/validate sensor))
                                    measurements)]

    (when (and update-devices-and-sensors? (valid-header? device-and-sensor)
               (update-device-data store (prepare-device device))
               (update-sensor-data store (prepare-sensor sensor))))

    (misc/insert-measurements store device-and-sensor page-size validated-measurements)))

(defn- get-data [header-row-count in]
  (->> in
       (csv/read-csv)
       (drop header-row-count)
       (vertical-csv->sanity)
       (seq-of-seq-of-seqs->seq-of-seq-of-maps [:timestamp :value])))

(defn parse-errors
  "Returns a map of device_id-type -> errors"
  [header]
  (into {} (for [{:keys [device_id type] :as x} header
                 :let [m  (meta x)
                       id (str device_id "-" type)]
                 :when (::update-devices-and-sensors? m)]
             [id m])))

(defn- parse-file-to-db [store header in-file ]
  (with-open [in (io/reader in-file)]
    (let [{:keys [::row-count ::update-devices-and-sensors?]} (meta header)
          data      (get-data row-count in)
          page-size 10]
      (dorun (map (partial process-column store page-size update-devices-and-sensors?) header data))
      (when-let [errors (not-empty (parse-errors header))]
        (throw (ex-info "Errors found" errors))))))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects roles request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects roles request-method)
  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (some #(= % programme-id) allowed-programmes)
          (has-project-manager? roles)
          (some #(= % project-id) allowed-projects)
          (has-user? roles)
          request-method]
         ;; super-admin - do everything
         [true _ _ _ _ _ _] true
         ;; programme-manager for this programme - do everything
         [_ true true _ _ _ _] true
         ;; project-manager for this project - do everything
         [_ _ _ true true _ _] true
         ;; user with this programme - get allowed
         [_ _ true _ _ true :get] true
         ;; user with this project - get allowed
         [_ _ _ _ true true :get] true
         :else false))

(defn devices-exist? [store header]
  (let [header-device-ids (map :device_id header)
        db-devices        (devices-to-property store header-device-ids)
        add-non-existent  (fn [m]
                            (cond-> m
                                    (not (contains? db-devices (:device_id m))) (merge-meta {:non-existent? true})))]
    (map add-non-existent header)))

(defn- enrich-with-authz [header store {:keys [projects programmes roles]}]
  (let [header                (devices-exist? store header)
        device-ids            (keep #(when-not (:non-existent? (meta %))
                                       (:device_id %)) header)
        devices-to-property   (devices-to-property store device-ids)
        properties-to-project (properties-to-project store (set (vals devices-to-property)))
        projects-to-programme (projects-to-programme store (set (vals properties-to-project)))
        devices-to-project    (reduce (fn [a [k v]] (assoc a k (get properties-to-project v))) {} devices-to-property)
        devices-to-programme  (reduce (fn [a [k v]] (assoc a k (get projects-to-programme v))) {} devices-to-project)
        not-allowed?          #(not (allowed?* (get devices-to-programme %)
                                               (get devices-to-project %)
                                               programmes
                                               projects-to-programme
                                               roles
                                               nil))
        add-not-allowed       (fn [m]
                                (cond-> m (not-allowed? (:device_id m))
                                        (merge-meta {:not-allowed? true})))]
    (map add-not-allowed
         header)))

(defn db-store [store item]
  (let [raw-header (get-header store item)
        header     (with-meta  (-> raw-header
                                   (validate-header)
                                   (enrich-with-authz store (:auth item)))
                     (meta raw-header))
        in-file    (io/file (:dir item) (:filename item))]
    (try
      (parse-file-to-db store header in-file)
      (finally (ioplus/delete! in-file)))))

(defn upload-item [store item]
  (let [username (-> item :metadata :user)]
    (s3/store-file (:s3 store) (update-in item [:uuid] #(str username "/" % "/data")))
    (try
      (db-store store item)
      (write-status store (assoc (update-in item [:uuid] #(str username "/" % "/status")) :status "SUCCESS"))
      (catch Throwable t
        (log/error t "failed")
        (write-status store (assoc (update-in item [:uuid] #(str username "/" % "/status")) :status "FAILURE" :data (str (ex-data t))))))))
