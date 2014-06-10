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
                                   (hayt/select :measurements
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
                                    (hayt/select :measurements
                                                 (hayt/where (conj where [= :month month]))))) months)))

(defn- parse-measurements [measurements]
  (map (fn [m]
         (-> m
             util/parse-value
             (update-in [:timestamp] util/db-to-iso)
             (dissoc :month :metadata :device_id))) measurements))

(defmulti format-measurements (fn [ctx measurements] (-> ctx :representation :media-type)))

(defmethod format-measurements "application/json" [ctx measurements]
  {:measurements (parse-measurements measurements)})

(defmethod format-measurements "text/csv" [ctx measurements]
  (->> measurements
       parse-measurements
       (util/render-items (:request ctx))))

(defn measurements-slice-handle-ok [store ctx]
  (let [db-session   (:hecuba-session store)
        params       (-> ctx :request :params)
        _            (log/infof "measurements-slice-handle-ok params: %s" params)
        device_id    (:device_id params)
        type (:type params)
        start-date   (util/to-db-format (:startDate params))
        end-date     (util/to-db-format (:endDate params))
        measurements (retrieve-measurements db-session start-date end-date device_id type)]
    
    (format-measurements ctx measurements)))

(defn- min-date [dt1 dt2]
  (let [dt1' (tc/to-date-time dt1)
        dt2' (tc/to-date-time dt2)]
    (if (t/before? dt1' dt2') dt1' dt2')))

(defn- max-date [dt1 dt2]
  (let [dt1' (tc/to-date-time dt1)
        dt2' (tc/to-date-time dt2)]
    (if (t/before? dt1' dt2') dt2' dt1')))

(defn- sensor-exists? [session device_id type]
  (first (db/execute session
                     (hayt/select :sensors
                                  (hayt/where [[= :device_id device_id]
                                               [= :type type]])))))

(defn- insert-measurement [session m]
  (db/execute session (hayt/insert :measurements (hayt/values m))))

(defn index-post! [store queue ctx]
  (let [request       (:request ctx)
        route-params  (:route-params request)
        device_id     (:device_id route-params)
        topic         (get-in queue ["measurements"])
        measurements  (:measurements (decode-body request))
        type          (-> measurements first :type)]
    (db/with-session [session (:hecuba-session store)]
      (if (sensor-exists? session device_id type)
        (doseq [measurement measurements]
          (let [t  (util/db-timestamp (:timestamp measurement))
                m  (stringify-values measurement)
                m2 {:device_id device_id
                    :type      type
                    :timestamp t
                    :value     (:value m)
                    :error     (:error m)
                    :month     (util/get-month-partition-key t)
                    :reading_metadata  {}}]
            (->> m2
                 (v/validate store)
                 (insert-measurement session))
            (q/put-on-queue topic m2))
          {:response {:status 202 :body "Accepted"}})
        {:response {:status 400 :body "Provide valid device_id and type."}}))))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          route-params (:route-params request)
          device_id    (:device_id route-params)
          where        [[= :device_id device_id]]
          measurements (db/execute session
                                   (hayt/select :measurements
                                                (hayt/where where)))]
      {:measurements (->> measurements
                          (map #(-> %
                                    util/parse-value
                                    (update-in [:timestamp] util/db-to-iso)
                                    (dissoc :reading_metadata :metadata :device_id :month))))})))

(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

(defn measurements-by-reading-handle-ok [store ctx]
  (let [{:keys [request]} ctx
        {:keys [route-params]} request
        {:keys [device_id sensor-type timestamp]} route-params
        measurement (db/with-session [session (:hecuba-session store)]
                      (db/execute session
                                  (hayt/select :measurements
                                               (hayt/where [[= :device_id device_id]
                                                            [= :type sensor-type]
                                                            [= :timestamp timestamp]]))))]
    (util/render-item request measurement)))


(defresource measurements-slice [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "text/csv" "application/edn"}
  :known-content-type? #{"application/json" "text/csv" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial measurements-slice-handle-ok store))

(defresource index [store queue]
  :allowed-methods #{:post :get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :post! (partial index-post! store queue)
  :handle-ok (partial index-handle-ok store)
  :handle-created index-handle-created)

(defresource measurements-by-reading [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :handle-ok (partial measurements-by-reading-handle-ok store))
