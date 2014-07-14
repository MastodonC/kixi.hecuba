(ns kixi.hecuba.api.measurements
  (:require
   [clj-time.core :as t]
   [clojure.core.match :refer (match)]
   [clojure.tools.logging :as log]
   [kixipipe.pipeline :as pipe]
   [kixi.hecuba.data.measurements :as measurements]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex time-range content-type-from-context request-method-from-context)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [schema.core :as s]
   [kixi.hecuba.web-paths :as p]))

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
             util/parse-value
             (update-in [:timestamp] util/db-to-iso)
             (dissoc :month :metadata :device_id))) measurements))

(defmulti format-measurements (fn [ctx measurements] (-> ctx :representation :media-type)))

(defmethod format-measurements "application/json" [ctx measurements]
  {:measurements (->> (parse-measurements measurements)
                      (map #(dissoc % :reading_metadata)))})

(defmethod format-measurements "text/csv" [ctx measurements]
  (->> measurements
       parse-measurements
       (util/render-items (:request ctx))))

(defn- sensor-exists? [session device_id type]
  (let [where [[= :device_id device_id]
               [= :type type]]]
    (merge (first (db/execute session
                              (hayt/select :sensors
                                           (hayt/where where))))
           (first (db/execute session
                              (hayt/select :sensor_metadata
                                           (hayt/where where)))))))

(defn prepare-measurement [m sensor]
  (let [t  (util/db-timestamp (:timestamp m))]
    {:device_id        (:device_id sensor)
     :type             (:type sensor)
     :timestamp        t
     :value            (str (:value m))
     :error            (str (:error m))
     :month            (util/get-month-partition-key t)
     :reading_metadata {}}))

(defn template-upload->item [{:keys [size tempfile content-type filename]} username]
  (assert (= content-type "text/csv") "Must be text/csv")
  (let [timestamp (t/now)]
    {:dest :upload
     :type :measurements
     :src-name  "uploads"
     :feed-name "measurements"
     :dir       (.getParent tempfile)
     :date      timestamp
     :filename  (.getName tempfile)
     :metadata  {:timestamp timestamp
                 :content-type "text/csv"
                 :user      username}}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-malformed?
(defmulti index-malformed? content-type-from-context)

(defmethod index-malformed? "multipart/form-data" [ctx]
    (let [file-data (-> ctx :request :multipart-params (get "data"))]
      (if file-data
        [false  {::file-data file-data}]
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
     [false {:items {:start-date (util/to-db-format startDate)
                      :end-date (util/to-db-format endDate)
                      :device_id device_id
                      :type type}}]
     true)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; measurements-slice-exists?

(defn measurements-slice-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [device_id type]} (:items ctx)
          sensor (first (db/execute session (hayt/select :sensors
                                                         (hayt/where [[= :device_id device_id]
                                                                      [= :type type]]))))]
      (not (nil? sensor)))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; measurements-slice-handle-ok

(defn measurements-slice-handle-ok [store ctx]
  (let [db-session   (:hecuba-session store)
        {:keys [start-date end-date device_id type]} (:items ctx)]
    (let [measurements (measurements/retrieve-measurements db-session start-date end-date device_id type)]
      (format-measurements ctx measurements))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-post!

(defn allowed?* [roles request-method]
  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (has-project-manager? roles)
          (has-user? roles)
          request-method]

         [true _ _ _ _    ] true
         [_ true _ _ _    ] true
         [_ _ true _ _    ] true
         [_ _ _ true :get ] true
         :else false))

(defn index-allowed? [store ctx]
  (allowed?* (:roles (sec/current-authentication (-> ctx :request :session)))
             (request-method-from-context ctx)))

(defmulti index-post! content-type-from-context)

(defmethod index-post! "multipart/form-data" [store pipe ctx]
  (let [file-data (::file-data ctx)
        session   (-> ctx :request :session)
        username  (sec/session-username session)
        auth      (sec/current-authentication session)
        item      (template-upload->item file-data username)
        uuid      (uuid)
        location  (format uploads-status-resource-path uuid)]
    (pipe/submit-item pipe (assoc item
                             :uuid uuid
                             :auth auth))
    {:response {:status 202
                :headers {"Location" location}
                :body "Accepted"}}))

(defmethod index-post! :default [store pipe ctx]
  (let [request      (:request ctx)
        route-params (:route-params request)
        device_id    (:device_id route-params)
        measurements (:measurements (:body ctx))
        type         (-> measurements first :type)
        page-size    10]
    (db/with-session [session (:hecuba-session store)]
      (if-let [sensor (sensor-exists? session device_id type)]
        (let [validated-measurements (map #(-> %
                                               (prepare-measurement sensor)
                                               (v/validate sensor))
                                          measurements)
              {:keys [min-date max-date]} (misc/min-max-dates validated-measurements)]
          (misc/insert-measurements store sensor validated-measurements page-size)
          {:response {:status 202 :body "Accepted"}})
        {:response {:status 400 :body "Provide valid device_id and type."}}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-created
(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; measurements-by-reading-handle-ok

(defn measurements-by-reading-handle-ok [store ctx]
  (let [{:keys [request]} ctx
        {:keys [route-params]} request
        {:keys [device_id type timestamp]} route-params
        t (util/to-db-format timestamp)
        measurement (format-measurements ctx (db/with-session [session (:hecuba-session store)]
                                               (db/execute session
                                                           (hayt/select :partitioned_measurements
                                                                        (hayt/where [[= :device_id device_id]
                                                                                     [= :type type]
                                                                                     [= :month (misc/get-month-partition-key t)]
                                                                                     [= :timestamp t]])))))]
    (-> measurement
        (dissoc :reading_metadata))))

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
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index-handle-created
