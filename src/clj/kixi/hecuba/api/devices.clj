(ns kixi.hecuba.api.devices
  (:require
   [clojure.core.match :refer (match)]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.data :as data]
   [kixi.hecuba.api :as api :refer (decode-body authorized? stringify-values)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.uuid :refer (uuid-str)]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.devices :as devices]
   [kixi.hecuba.data.sensors :as sensors]
   [kixi.hecuba.data.entities.search :as search]
   [schema.core :as s]
   [kixi.amon-schema :as schema]))

(def ^:private device-resource (p/resource-path-string :entity-device-resource))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects role request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s role: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects role request-method)
  (match  [(has-admin? role)
           (has-programme-manager? programme-id allowed-programmes)
           (has-project-manager? project-id allowed-projects)
           (has-user? programme-id allowed-programmes project-id allowed-projects)
           request-method]

          [true _ _ _ _]    [true {:editable true}]
          [_ true _ _ _]    [true {:editable true}]
          [_ _ true _ _]    [true {:editable true}]
          [_ _ _ true :get] true
          :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INDEX

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [body request-method session params]} (:request ctx)
          {:keys [projects programmes role]} (sec/current-authentication session)
          entity_id (:entity_id params)
          {:keys [project_id programme_id]} (when entity_id
                                              (search/get-by-id entity_id (:search-session store)))]
      (when (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects role request-method)))))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request        (:request ctx)
          method         (:request-method request)
          route-params   (:route-params request)
          entity_id      (:entity_id route-params)
          entity         (search/get-by-id entity_id (:search-session store))]
      (case method
        :post (seq entity)
        :get (let [items (devices/get-devices session entity_id)
                   editable (:editable ctx)]
               {::items (mapv #(assoc % :editable editable) items)})))))

(defn should-calculate-fields? [sensors]
  (log/info "> K.H.api.devices/should-calculate-fields?")
  (not (some #(and (= (:period %) "CUMULATIVE")
                   (or (= (:actual_annual_calculation %) true)
                       (= (:normalised_annual_calculation %) true))) sensors)))

(def user-visible-keys [:device_id :sensor_id :type :accuracy :alias :actual_annual
                        :corrected_unit :correction
                        :correction_factor :correction_factor_breakdown
                        :frequency :max :min :period
                        :resolution :unit :user_metadata :synthetic :alias_sensor])

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request
        entity_id (:entity_id route-params)]
    (case request-method
      :post (let [body (update-in (decode-body request) [:readings]
                                  (fn [readings] (map #(select-keys % user-visible-keys)
                                                      readings)))]
              (if (or
                   (s/check schema/BaseDevice body)
                   (not= (:entity_id body) entity_id)
                   (not (should-calculate-fields? (:readings body))))
                true
                [false {:body body}]))
      false)))

(defn- ext-type [sensor type-ext]
  (-> sensor
      (update-in [:type] #(str % "_" type-ext))
      (assoc :period "PULSE" :synthetic true :sensor_id (uuid-str))))

(defmulti calculated-sensor (fn [sensor] (when-let [unit (:unit sensor)]
                                           (.toUpperCase unit))))

(defmethod calculated-sensor "KWH" [sensor]
  (log/info "> K.H.api.devices/calculated-sensor KWH")
  (-> sensor
      (assoc :unit "co2" :synthetic true :sensor_id (uuid-str))
      (update-in [:type] #(sensors/output-type-for % "KWH2CO2"))))

(defmethod calculated-sensor "M^3" [sensor]
  (log/info "> K.H.api.devices/calculated-sensor M^3")
  (let [kwh-sensor (-> sensor
                       (assoc :unit "kWh" :synthetic true :sensor_id (uuid-str))
                       (update-in [:type] #(sensors/output-type-for % "VOL2KWH")))]
    [kwh-sensor (calculated-sensor kwh-sensor)]))

(defmethod calculated-sensor "FT^3" [sensor]
  (log/info "> K.H.api.devices/calculated-sensor FT^3")
  (let [kwh-sensor (-> sensor
                       (assoc :unit "kWh" :synthetic true :sensor_id (uuid-str))
                       (update-in [:type] #(sensors/output-type-for % "VOL2KWH")))]
    [kwh-sensor (calculated-sensor kwh-sensor)]))

(defmethod calculated-sensor :default [sensor])

(defn create-default-sensors
  "Creates default sensors whenever new device is added: *_differenceSeries for CUMULATIVE,
   and *_co2 for kwh PULSE, etc."
  [body]
  (log/info "> K.H.api.devices/create-default-sensors")
  (let [sensors        (:readings body)
        new-sensors    (map #(case (:period %)
                               "CUMULATIVE" (ext-type % "differenceSeries")
                               "PULSE"      (calculated-sensor %)
                               "INSTANT"    nil
                               nil) sensors)]
    (log/info "    >> sensors: " sensors)
    (log/info "    >> new-sensors: " new-sensors)
    (log/info "    >> Updates readings with new-sensors...")
    (update-in body [:readings] (fn [readings] (into [] (remove nil? (flatten (concat readings new-sensors))))))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request body]} ctx
          entity_id              (-> request :route-params :entity_id)
          username               (sec/session-username (-> ctx :request :session))
          user_id                (:id (users/get-by-username session username))]

      (let [device_id    (uuid-str)
            new-body     (create-default-sensors (assoc body :readings
                                                        (mapv #(assoc % :sensor_id (uuid-str)) (:readings body))))]
        (devices/insert session entity_id (-> new-body
                                              (assoc :device_id device_id :user_id user_id)
                                              (dissoc :readings)))
        (doseq [reading (:readings new-body)]
          (sensors/insert session (assoc reading :device_id device_id :user_id user_id)))
        (-> (search/searchable-entity-by-id entity_id session)
            (search/->elasticsearch (:search-session store)))
        {:device_id device_id :entity_id entity_id}))))

(defn index-handle-ok [ctx]
  (api/render-items ctx (::items ctx)))

(defn index-handle-created [ctx]
  (let [entity_id (-> ctx :entity_id)
        device_id (-> ctx :device_id)
        location  (format device-resource entity_id device_id)]
    (ring-response {:headers {"Location" location}
                    :body (json/encode {:location location
                                        :status "OK"
                                        :version "4"})})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCE

(defn resource-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes role]}     (sec/current-authentication session)
          entity_id (:entity_id params)
          {:keys [project_id programme_id]} (when entity_id
                                              (search/get-by-id entity_id (:search-session store)))]
      (when (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects role request-method)))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [device_id (-> ctx :request :route-params :device_id)
          item      (devices/get-by-id session device_id)]
      (if-not (empty? item)
        {::item item}
        false))))

;; Should be device-response etc and it should do the delete in delete!,
;; that should put something in the context which is then checked here.
(defn resource-delete-enacted? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item}   ctx
          device_id       (:device_id item)
          entity_id       (:entity_id item)
          response        (devices/delete device_id false session)
          search-response (-> (search/searchable-entity-by-id entity_id session)
                              (search/->elasticsearch (:search-session store)))]
      "Delete Accepted")))

(defn sensor-metadata
  "Updates sensor's metadata: resets dirty dates for calculations to lower_ts and upper_ts."
  [sensor_id device_id range]
  (log/info "> K.H.api.devices/sensor-metadata")
  (let [{:keys [start-date end-date]} range]
    (when (and start-date end-date)
      (-> {:sensor_id sensor_id
           :device_id device_id}
          (merge (mapcat #(hash-map % {"start" start-date "end" end-date})
                         [:difference_series :co2 :kwh]))
          (merge {:upper_ts end-date :lower_ts start-date})))))

(defn get-sensors-to-delete
  "Takes old device data and a sequence of sensors and returns
  a sequence of sensors that should be deleted."
  [old-device sensors]
  (keep (fn [s] (when (:synthetic s)
                  ;; there should only ever be one sensor of a particular type per device
                  (when-let [corresponding-sensor_id (:sensor_id (some #(when (= (:type %) (:type s)) %) (:readings old-device)))]
                    {:device_id (:device_id old-device)
                     :sensor_id corresponding-sensor_id})))
        sensors))

(defn delete-old-synthetic-sensors
  "Takes a sequence of sensors and deletes them."
  [session sensors]
  (log/info "> K.H.api.devices/delete-old-synthetic-sensors")
  (doseq [s sensors]
    (log/infof "Deleting synthetic sensor: %s : %s" (:device_id s) (:type s))
    (sensors/delete s session)))

(defn get-sensors-to-insert
  "Takes a sequence of sensors and enriches it with synthetic sensors"
  [user_id sensors]
  (log/info "> K.H.api.devices/get-sensors-to-insert")
  (keep (fn [s]
         (when (:synthetic s)
           (let [sensor_id (uuid-str)
                 sensor    (assoc s :device_id (:device_id s) :user_id user_id :sensor_id sensor_id)]
             sensor)))
       sensors))

(defn insert-new-synthetic-sensors
  "Takes a sequence of sensors and inserts new synthetic sensors"
  [session sensors]
  (log/info "> K.H.api.devices/insert-new-synthetic-sensors")
  (doseq [s sensors]
    (log/infof "Inserting sensor: %s : %s" (:device_id s) (:type s))
    (let [metadata  {:sensor_id (:sensor_id s) :device_id (:device_id s)}]
      (sensors/insert session s metadata))))

(defn update-original-sensor
  "Updates original sensor and resets its dirty dates to lower_ts and upper_ts
   so that calculations for new synthetic sensors can calculate over all data."
  [session device_id user_id new-sensor range]
  (log/info "> K.H.api.devices/update-original-sensor")
  (let [refreshed-metadata (sensor-metadata (:sensor_id new-sensor) device_id range)]
    (log/infof "Updating original sensor: %s" new-sensor)
    (if refreshed-metadata
      (sensors/update session device_id (assoc new-sensor :device_id device_id :user_id user_id) refreshed-metadata)
      (sensors/update session device_id (assoc new-sensor :device_id device_id :user_id user_id)))))

(defn tidy-up-sensors
  "Takes session, old sensor data, new sensor data. Updates original sensor and
   recreates synthetic sensors. "
  [session old-device user_id old-sensor new-sensor range]
  (log/info "> K.H.api.devices/tidy-up-sensors")
  (let [device_id                     (:device_id old-device)
        alleged-old-synthetic-sensors (create-default-sensors {:readings [old-sensor]})
        new-synthetic-sensors         (create-default-sensors {:readings [(dissoc (data/deep-merge old-sensor new-sensor)
                                                                                  :lower_ts :upper_ts :median :status)]})
        sensors-to-delete             (get-sensors-to-delete old-device (:readings alleged-old-synthetic-sensors))
        sensors-to-insert             (get-sensors-to-insert user_id (:readings new-synthetic-sensors))]
    (delete-old-synthetic-sensors session sensors-to-delete)
    (insert-new-synthetic-sensors session sensors-to-insert)
    (update-original-sensor session device_id user_id new-sensor range)))

(defn update-existing-sensors
  "Updates original and synthetic sensors (used when unit or period remain the same."
  [session device_id user_id old-sensor new-sensor]
  (log/info "> K.H.api.devices/update-existing-sensors")
  ;; it's not a synthetic sensor - create synthetic sensors
  (if-not (:synthetic new-sensor)
    (let [new-synthetic-sensors (create-default-sensors {:readings [(dissoc (data/deep-merge old-sensor new-sensor)
                                                                            :lower_ts :upper_ts :median :status)]})]
      (doseq [s (:readings new-synthetic-sensors)]
        (log/infof "Updating sensor: %s : %s" device_id (:type s))
        (sensors/update session device_id (assoc s :device_id device_id))))
    ;; it's a synthetic sensor - don't create synthetic sensors for that sensor, just update its information
    (let [updated-synthetic-sensor (dissoc (data/deep-merge old-sensor new-sensor)
                                           :lower_ts :upper_ts :median :status)]
      (log/infof "Updating sensor: %s : %s" device_id (:type updated-synthetic-sensor))
      (sensors/update session device_id (assoc updated-synthetic-sensor :device_id device_id)))))

(def user-edited-keys [:type :unit :resolution :period :alias :actual_annual :min :user_metadata :accuracy
                       :frequency :corrected_unit :correction_factor :correction :max :correction_factor_breakdown :alias_sensor])

(defn recreate-sensor
  "Depending on edited fields, either deletes/iserts new synthetic sensors or updates
  the existing onews."
  [session old-device user_id old-sensor new-sensor range]
  (log/info "> K.H.api.devices/recreate-sensor")
  (let [old-sensor-map (select-keys old-sensor user-edited-keys)
        new-sensor-map (select-keys new-sensor user-edited-keys)
        ;; Get a sequence of keys that has been updated
        updated-keys   (-> (clojure.data/diff old-sensor-map new-sensor-map) second keys)
        device_id      (:device_id old-device)]
    ;; Update only if user editable keys have changed
    (when (seq updated-keys)
      (if (some (into #{} updated-keys) #{:unit :period :type})
        ;; Recreate because unit, type or period has changed
        (tidy-up-sensors session old-device user_id old-sensor new-sensor range)
        ;; Update existing one since nothing crucial has changed
        (update-existing-sensors session device_id user_id old-sensor new-sensor)))))

(defn add-new-sensor
  "Creates new sensors and respective synthetic sensors."
  [session device_id user_id new-sensor]
  (log/info "> K.H.api.devices/add-new-sensor")
  (let [new-sensors (create-default-sensors
                     {:readings [(-> new-sensor
                                     (dissoc :lower_ts :upper_ts
                                             :median :status))]})]
    (doseq [s (:readings new-sensors)]
      (log/infof "Inserting new sensor: %s : %s" device_id (:type s))
      (sensors/insert session (assoc s :device_id device_id :user_id user_id :sensor_id (uuid-str))))))

(defn resource-put! [store ctx]
  (log/info "> K.H.api.devices/resource-put!")
  (db/with-session [session (:hecuba-session store)]
    (let [{request :request} ctx]
      (if-let [item (::item ctx)]
        (let [body          (decode-body request)
              entity_id     (-> item :entity_id)
              username      (sec/session-username (-> ctx :request :session))
              user_id       (:id (users/get-by-username session username))
              device_id     (-> item :device_id)]
          (let [edited-device-data (-> body (dissoc :editable :readings))]
            ;; Don't update device if nothing has been changed
            (when (seq edited-device-data)
              (devices/update session entity_id device_id (assoc edited-device-data
                                                            :user_id user_id
                                                            :device_id device_id))))
          (doseq [incoming-sensor (:readings body)]
            (let [existing-sensor (first (filter #(= (:sensor_id %) (:sensor_id incoming-sensor)) (:readings item)))]
              (if existing-sensor
                ;; sensor already exists - need to update/recreate synthetic sensors
                (let [sensor_id (:sensor_id existing-sensor)
                      {:keys [lower_ts upper_ts]} (sensors/all-sensor-information store device_id sensor_id)]
                  (recreate-sensor session item user_id existing-sensor incoming-sensor {:start-date lower_ts :end-date upper_ts}))
                ;; sensor doesn't exist - adding new sensor
                (add-new-sensor session device_id user_id incoming-sensor))))
          (-> (search/searchable-entity-by-id entity_id session)
              (search/->elasticsearch (:search-session store)))
          (ring-response {:status 404 :body "Please provide valid entity_id and device_id"}))))))

(defn resource-handle-ok [ctx]
  (let [req (:request ctx)]
    (api/render-item ctx (::item ctx))))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
     :else true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; SENSOR RESOURCE

(defn sensor-resource-exists? [store ctx]
  (log/info "> K.H.api.devices/sensor-resource-exists?")
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [entity_id device_id type]} (-> ctx :request :route-params)
          sensor (sensors/get-by-type {:device_id device_id :type type} session)]
      (if-not (empty? sensor)
        {:sensor sensor :entity_id entity_id}
        false))))

(defn sensor-resource-delete! [store ctx]
  (log/info "> K.H.api.devices/sensor-resource-delete!")
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [sensor entity_id]} ctx
          {:keys [sensors sensors-metadata]} (sensors/delete sensor session)
          successful? (and (empty? sensors) (empty? sensors-metadata))]
      (when successful?
        (-> (search/searchable-entity-by-id entity_id session)
            (search/->elasticsearch (:search-session store))))
      {:delete-successful? successful?})))

(defn sensor-resource-delete-enacted? [store ctx]
  (:delete-successful? ctx))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :exists? (partial index-exists? store)
  :malformed? index-malformed?
  :can-post-to-missing? (constantly false)
  :post! (partial index-post! store)
  :handle-ok index-handle-ok
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (resource-allowed? store)
  :exists? (partial resource-exists? store)
  :delete-enacted? (partial resource-delete-enacted? store)
  :respond-with-entity? resource-respond-with-entity
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok resource-handle-ok)

(defresource sensor-resource [store]
  :allowed-methods #{:delete}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (resource-allowed? store)
  :exists? (partial sensor-resource-exists? store)
  :delete! (partial sensor-resource-delete! store)
  :delete-enacted? (partial sensor-resource-delete-enacted? store))
