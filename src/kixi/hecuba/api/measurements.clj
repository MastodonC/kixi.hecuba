(ns kixi.hecuba.api.measurements
  (:require
   [clj-time.coerce :as tc]
   [clj-time.core :as t]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.data.misc :as m]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex time-range)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]))

(defn sensor_metadata-for [store sensor_id]
  (let [{:keys [type device_id]} sensor_id]
    (db/with-session [session (:hecuba-session store)]
      (first (db/execute session
                         (hayt/select
                          :sensor_metadata
                          (hayt/where [[= :device_id device_id]
                                       [= :type type]])))))))

(defn resolve-start-end [store type device_id start end]
  (mapv tc/to-date-time
        (if (not (and start end))
          (let [sm (sensor_metadata-for store {:type type :device_id device_id})
                [lower upper] ((juxt :lower_ts :upper_ts) sm)]
            [(or start lower)
             (or end upper)])
          [start end])))

(defn all-measurements
  "Returns a sequence of all the measurements for a sensor
   matching (type,device_id). The sequence pages to the database in the
   background. The page size is a clj-time Period representing a range
   in the timestamp column. page size defaults to (clj-time/hours 1)"
  ([store sensor_id & [opts]]
     (let [{:keys [type device_id]} sensor_id
           {:keys [page start end] :or {page (t/hours 1)}} opts
           [start end] (resolve-start-end store type device_id start end)]
       (when (and start end)
         (let  [next-start (t/plus start page)]
           (db/with-session [session (:hecuba-session store)]
             (lazy-cat (db/execute session
                                   (hayt/select :partitioned_measurements
                                                (hayt/where [[= :device_id device_id]
                                                             [= :type type]
                                                             [= :month (m/get-month-partition-key start)]
                                                             [>= :timestamp start]
                                                             [< :timestamp next-start]]))
                                   nil)
                       (when (t/before? next-start end)
                         (all-measurements store sensor_id (merge opts {:start next-start :end end}))))))))))

(defn retrieve-measurements
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [session start-date end-date device-id reading-type]
  (let [range  (time-range start-date end-date (t/months 1))
        months (map #(util/get-month-partition-key (tc/to-date %)) range)
        where  [[= :device_id device-id]
                [= :type reading-type]
                [>= :timestamp (tc/to-date start-date)]
                [<= :timestamp (tc/to-date end-date)]]]
    (mapcat (fn [month] (db/execute session
                                    (hayt/select :partitioned_measurements
                                                 (hayt/where (conj where [= :month month]))))) months)))

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

(defn measurements-slice-malformed? [ctx]
 (let [params (-> ctx :request :params)
       {:keys [startDate endDate device_id type]} params]
   (if (and startDate endDate)
     [false {:items {:start-date (util/to-db-format startDate)
                      :end-date (util/to-db-format endDate)
                      :device_id device_id
                      :type type}}]
     true)))

(defn measurements-slice-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [device_id type]} (:items ctx)
          sensor (first (db/execute session (hayt/select :sensors
                                                         (hayt/where [[= :device_id device_id]
                                                                      [= :type type]]))))]
      (not (nil? sensor)))))

(defn measurements-slice-handle-ok [store ctx]
  (let [db-session   (:hecuba-session store)
        {:keys [start-date end-date device_id type]} (:items ctx)]
    (let [measurements (retrieve-measurements db-session start-date end-date device_id type)]
      (format-measurements ctx measurements))))

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

(defn index-post! [store ctx]
  (let [request       (:request ctx)
        route-params  (:route-params request)
        device_id     (:device_id route-params)
        measurements  (:measurements (decode-body request))
        type          (-> measurements first :type)]
    (db/with-session [session (:hecuba-session store)]
      (if-let [sensor (sensor-exists? session device_id type)]
        (let [validated-measurements (map #(-> %
                                               (prepare-measurement sensor)
                                               (v/validate sensor))
                                          measurements)
              {:keys [min-date max-date]} (m/min-max-dates validated-measurements)]
          (m/insert-measurements store sensor validated-measurements 100)
          {:response {:status 202 :body "Accepted"}})
        {:response {:status 400 :body "Provide valid device_id and type."}}))))

(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

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
                                                                                     [= :month (m/get-month-partition-key t)]
                                                                                     [= :timestamp t]])))))]
    (-> measurement
        (dissoc :reading_metadata))))


(defresource measurements-slice [store]
  :allowed-methods #{:get}
  :malformed? measurements-slice-malformed?
  :exists? (partial measurements-slice-exists? store)
  :available-media-types #{"application/json" "text/csv" "application/edn"}
  :known-content-type? #{"application/json" "text/csv" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial measurements-slice-handle-ok store))

(defresource index [store]
  :allowed-methods #{:post}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :post! (partial index-post! store)
  :handle-created index-handle-created)

(defresource measurements-by-reading [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial measurements-by-reading-handle-ok store))
