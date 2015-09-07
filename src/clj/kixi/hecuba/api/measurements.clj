(ns kixi.hecuba.api.measurements
  (:require
   [clj-time.core :as t]
   [clojure.core.match :refer (match)]
   [clojure.tools.logging :as log]
   [kixipipe.pipeline :as pipe]
   [kixi.hecuba.data.measurements :as measurements]
   [kixi.hecuba.data.measurements.core :as mc]
   [kixi.hecuba.data.measurements.upload :as upload]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.data.sensors :as sensors]
   [kixi.hecuba.time :as time]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.api :as api :refer (decode-body authorized? content-type-from-context request-method-from-context)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [schema.core :as s]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.entities.search :as search]))

(def Measurement
  "A schema for a sensor measurements."
  (s/either
   {(s/required-key :type) s/Str
    (s/required-key :timestamp) s/Str
    (s/required-key :value) (s/either s/Str s/Num)
    (s/optional-key :error) s/Str}
   {(s/required-key :type) s/Str
    (s/required-key :timestamp) s/Str
    (s/required-key :error) s/Str
    (s/optional-key :value) (s/either s/Str s/Num)}))

(def ^:private uploads-status-resource-path (p/resource-path-string :uploads-status-resource))

(defn- parse-measurements [measurements]
  (map (fn [m]
         (-> m
             measurements/parse-value
             (update-in [:timestamp] time/db-to-iso)
             (dissoc :month :metadata :device_id))) measurements))

(defmulti format-measurements (fn [ctx measurements] (-> ctx :representation :media-type)))

(defmethod format-measurements "application/json" [ctx measurements]
  {:measurements (->> (parse-measurements measurements)
                      (map #(dissoc % :reading_metadata)))})

(defmethod format-measurements "text/csv" [ctx measurements]
  (->> measurements
       parse-measurements
       (api/render-items (:request ctx))))

(defn- sensor-exists? [store device_id type]
  (db/with-session [session (:hecuba-session store)]
    (let [sensor (sensors/get-by-type {:device_id device_id :type type} session)]
      (when (seq sensor)
        (sensors/merge-sensor-metadata store sensor)))))

(defn prepare-measurement [m sensor]
  (let [t  (time/db-timestamp (:timestamp m))]
    {:device_id        (:device_id sensor)
     :sensor_id        (:sensor_id sensor)
     :timestamp        t
     :value            (str (:value m))
     :error            (str (:error m))
     :month            (time/get-month-partition-key t)
     :reading_metadata {}}))

(def csv-mime-type #{"text/comma-separated-values"
                     "text/csv"
                     "application/csv"
                     "application/excel"
                     "application/vnd.ms-excel"
                     "application/vnd.msexcel"})

(defn template-upload->item [entity_id ^String username date-format aliases? {:keys [size tempfile content-type filename]}]
  (if (and (csv-mime-type content-type)
           (.endsWith filename ".csv"))
    (let [timestamp (t/now)]
      {:dest        :upload
       :type        :measurements
       :entity_id   entity_id
       :src-name    "uploads"
       :feed-name   "measurements"
       :dir         (.getParent tempfile)
       :date        timestamp
       :date-format date-format
       :aliases?    aliases?
       :filename    (.getName tempfile)
       :metadata    {:timestamp    timestamp
                     :content-type "text/csv"
                     :username     username
                     :filename     filename}})
    (throw (Exception. (str  "Must be text/csv or other csv type and end with .csv, not " content-type " " filename)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-malformed?

(defn ensure-vector [data]
  (if (vector? data) data [data]))

(defmulti index-malformed? content-type-from-context)

(defmethod index-malformed? "multipart/form-data" [ctx]
  (let [multipart-params (-> ctx :request :multipart-params)
        file-data        (some-> multipart-params (get "data") ensure-vector)
        date-format      (get multipart-params "dateformat")
        aliases?         (get multipart-params "aliases")]
    (log/info "date-format:" date-format)
    (log/info "file-data:" file-data)
    (if (and file-data date-format)
      [false  {::file-data   file-data
               ::date-format date-format
               ::aliases? (= "on" aliases?)}]
      true)))

(defmethod index-malformed? :default [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request
        {:keys [entity_id device_id]} route-params]
    (case request-method
      :post (let [body (decode-body request)
                  measurement (first (:measurements body))]
              (if (s/check Measurement measurement) ;;TODO should we check all measurements or only the first one?
                true
                [false {:body body}])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; measurements-slice-malformed?

(defn measurements-slice-malformed? [ctx]
 (let [params (-> ctx :request :params)
       {:keys [startDate endDate device_id type]} params]
   (if (and startDate endDate)
     [false {:items {:start-date (time/to-db-format startDate)
                     :end-date (time/to-db-format endDate)
                     :device_id device_id
                     :type type}}]
     true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; measurements-slice-exists?

(defn measurements-slice-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [sensor (sensors/get-by-type (:items ctx) session)]
      [(not (nil? sensor)) {:sensor sensor}])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; measurements-slice-handle-ok

(defn measurements-slice-handle-ok [store ctx]
  (let [db-session   (:hecuba-session store)
        {:keys [start-date end-date device_id type]} (:items ctx)
        sensor_id (-> (:sensor ctx) :sensor_id)]
    (let [measurements (measurements/retrieve-measurements db-session start-date end-date device_id sensor_id)]
      (println ctx)
      (format-measurements ctx measurements))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-post!

(defn allowed?* [programme_id project_id programmes projects role request-method]
  (match [(has-admin? role)
          (has-programme-manager? programme_id programmes)
          (has-project-manager? project_id projects)
          (has-user? programme_id project_id programmes projects)
          request-method]

         [true _ _ _ _    ] true
         [_ true _ _ _    ] true
         [_ _ true _ _    ] true
         [_ _ _ true :get ] true
         :else false))

(defn index-allowed? [store ctx]
  (let [{:keys [request-method session params]} (:request ctx)
        {:keys [projects programmes role]}     (sec/current-authentication session)
        entity_id (:entity_id params)
        {:keys [project_id programme_id]} (when entity_id
                                              (search/get-by-id entity_id (:search-session store)))]
    (allowed?* programme_id project_id programmes projects role request-method)))

(defmulti index-post! content-type-from-context)

(defmethod index-post! "multipart/form-data" [store pipe ctx]
  (let [file-data    (::file-data ctx)
        date-format  (::date-format ctx)
        aliases?     (::aliases? ctx)
        session      (-> ctx :request :session)
        username     (sec/session-username session)
        route-params (:route-params (:request ctx))
        entity_id    (:entity_id route-params)
        auth         (sec/current-authentication session)
        items        (map (partial template-upload->item entity_id username date-format aliases?) file-data)]
    (doseq [item items]
      (log/infof "Accepted upload: %s" item)
      (upload/write-status (assoc item :status "ACCEPTED") store)
      (pipe/submit-item pipe (assoc item :auth auth)))
    ;; We don't have emough info to return a Location header here. So
    ;; we return nothing. This seems to be in line with the HTTP spec.
    {:response {:status 202
                :body "Accepted"}}))

(defmethod index-post! :default [store pipe ctx]
  (let [request      (:request ctx)
        route-params (:route-params request)
        device_id    (:device_id route-params)
        measurements (:measurements (:body ctx))
        type         (-> measurements first :type)
        page-size    10]
    (db/with-session [session (:hecuba-session store)]
      (if-let [sensor (sensor-exists? store device_id type)]
        (let [validated-measurements (map #(-> %
                                               (prepare-measurement sensor)
                                               (v/validate sensor))
                                          measurements)
              {:keys [min-date max-date]} (time/min-max-dates validated-measurements)]
          (measurements/insert-measurements store sensor page-size validated-measurements)
          (sensors/update-sensor-metadata session sensor min-date max-date)
          {:response {:status 202 :body "Accepted"}})
        {:response {:status 400 :body "Provide valid device_id and type."}}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-created
(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; measurements-by-reading-handle-ok

(defn measurements-by-reading-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request]} ctx
          {:keys [route-params]} request
          {:keys [device_id type timestamp]} route-params
          sensor_id (:sensor_id (sensors/get-by-type {:device_id device_id :type type} session))
          t (time/to-db-format timestamp)
          measurement (format-measurements ctx (db/execute session
                                                           (hayt/select :partitioned_measurements
                                                                        (hayt/where [[= :device_id device_id]
                                                                                     [= :sensor_id sensor_id]
                                                                                     [= :month (time/get-month-partition-key t)]
                                                                                     [= :timestamp t]]))))]
      (-> measurement
          (dissoc :reading_metadata)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCES

(defresource measurements-slice [store]
  :allowed-methods #{:get}
  :malformed? measurements-slice-malformed?
  :exists? (partial measurements-slice-exists? store)
  :available-media-types #{"application/json" "text/csv" "application/edn"}
  :known-content-type? #{"application/json" "text/csv" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial measurements-slice-handle-ok store))

(defresource index [store s3 pipeline]
  :allowed-methods #{:post}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn" "multipart/form-data"}
  :authorized? (authorized? store)
  :malformed? #(index-malformed? %) ;; wierd - can't just pass
                                    ;; index-malformed?
                                    ;; directly. presumaby because of
                                    ;; varargs dispatch-fn - TBC
  :allowed? (partial index-allowed? store)
  :post! (partial index-post! store pipeline)
  :handle-created index-handle-created)

(defresource measurements-by-reading [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial measurements-by-reading-handle-ok store))
