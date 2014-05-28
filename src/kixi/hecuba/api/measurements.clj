(ns kixi.hecuba.api.measurements
  (:require
   [bidi.bidi :as bidi]
   [clj-time.coerce :as tc]
   [clj-time.core :as t]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.data.misc :as m]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.storage.dbnew :as dbnew]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex time-range)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   ))

(defn sensor_metadata-for [store sensor_id]
  (let [{:keys [type device_id]} sensor_id]
    (dbnew/with-session [session (:hecuba-session store)]
      (first (dbnew/execute session
                            (hayt/select
                             :sensor_metadata
                             (hayt/where [[= :device_id device_id]
                                          [= :type type]])))))))

(defn- mk-bounds-from [store type device_id]
  (let [sm  (sensor_metadata-for store {:type type :device_id device_id})
        {:keys [lower_ts upper_ts]} sm]
    (vector (atom (or (tc/to-date-time lower_ts) (org.joda.time.DateTime. Long/MAX_VALUE) ))
            (atom (or (tc/to-date-time upper_ts) (org.joda.time.DateTime. 0))))))

(defn resolve-start-end [store type device_id start end]
  (mapv tc/to-date-time
       (if (not (and start end))
         (let [sm (sensor_metadata-for store {:type type :device_id device_id})
               _ (log/info "sm: " sm)
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
           [start end] (resolve-start-end store type device_id start end )
           _ (log/info "s: " start ", e:" end)
           next-start (t/plus start page)]
       (dbnew/with-session [session (:hecuba-session store)]
         (lazy-cat (dbnew/execute session
                                  (hayt/select :measurements
                                               (hayt/where [[= :device_id device_id]
                                                            [= :type type]
                                                            [= :month (m/get-month-partition-key start)]
                                                            [>= :timestamp start]
                                                            [< :timestamp next-start]]))
                                  nil)
                   (when (t/before? next-start end)
                     (all-measurements store sensor_id (merge opts {:start next-start :end end}))))))))

(defn retrieve-measurements 
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [querier start-date end-date device-id reading-type]
  (let [range  (time-range start-date end-date (t/months 1))
        months (map #(util/get-month-partition-key (tc/to-date %)) range)
        where  [[= :device_id device-id]
                [= :type reading-type]
                [>= :timestamp (tc/to-date start-date)]
                [<= :timestamp (tc/to-date end-date)]]]
    (mapcat (fn [month] (hecuba/items querier :measurement (conj where [= :month month]))) months)))

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

(defn measurements-slice-handle-ok [store store-new ctx]
  (let [request                (:request ctx)
        querier                (:querier store)
        {:keys [route-params
                query-string]} request
        {:keys [device_id
                reading-type]} route-params
        decoded-params         (util/decode-query-params query-string)
        start-date             (util/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
        end-date               (util/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
        measurements           (retrieve-measurements querier start-date end-date device_id reading-type)]

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
  (first (dbnew/execute session
                  (hayt/select :sensors
                               (hayt/where [[= :device_id device_id]
                                            [= :type type]])))))

(defn index-post! [store store-new queue ctx]
  (let [{:keys [commander querier]} store
        request       (:request ctx)
        route-params  (:route-params request)
        device_id     (:device_id route-params)
        topic         (get-in queue ["measurements"])
        measurements  (:measurements (decode-body request))
        type          (-> measurements first :type)]
    (dbnew/with-session [session (:hecuba-session store-new)]
      (if (sensor-exists? session device_id type)
        (let [[lower upper] (mk-bounds-from store-new type device_id)
              update-bounds! (fn [t] (swap! lower min-date t) (swap! upper max-date t))]
          (doseq [measurement measurements]
            (let [t  (util/db-timestamp (:timestamp measurement))
                  m  (stringify-values measurement)
                  m2 {:device_id device_id
                      :type      type
                      :timestamp t
                      :value     (:value m)
                      :error     (:error m)
                      :month     (util/get-month-partition-key t)
                      :metadata  "{}"}]
              (->> m2
                   (v/validate commander querier)
                   (hecuba/upsert! commander :measurement))

              (update-bounds! t)
              (q/put-on-queue topic m2)))
          (hecuba/upsert! commander :sensor_metadata {:device_id device_id
                                                      :type type
                                                      :lower_ts @lower
                                                      :upper_ts @upper})

          (hecuba/upsert! commander :sensor_metadata {:device_id device_id
                                                      :type type
                                                      :lower_ts @lower
                                                      :upper_ts @upper})
          {:response {:status 202 :body "Accepted"}})
        {:response {:status 400 :body "Provide valid device_id and type."}}))))

(defn index-handle-ok [store store-new ctx]
  (let [{:keys [querier]} store
        request (:request ctx)
        route-params (:route-params request)
        device_id (:device_id route-params)
        where [[= :device_id device_id]]
        measurements (hecuba/items querier :measurement where)]
    {:measurements (->> measurements
                        (map #(-> %
                                  util/parse-value
                                  (update-in [:timestamp] util/db-to-iso)
                                  (dissoc :metadata :device_id :month))))}))

(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

(defn measurements-by-reading-handle-ok [store store-new ctx]
  (let [{:keys [request]} ctx
        {:keys [route-params]} request
        {:keys [device_id sensor-type timestamp]} route-params
        measurement (dbnew/with-session [session (:hecuba-session store)]
                      (dbnew/execute session
                                     (hayt/select :measurements
                                                  (hayt/where [[= :device_id device_id]
                                                               [= :type sensor-type]
                                                               [= :timestamp timestamp]]))))]
    (util/render-item request measurement)))


(defresource measurements-slice [store store-new handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "text/csv"}
  :known-content-type? #{"application/json" "text/csv"}
  :authorized? (authorized? (:querier store) :measurement)
  :handle-ok (partial measurements-slice-handle-ok store store-new))

(defresource index [store store-new queue handlers]
  :allowed-methods #{:post :get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? (:querier store) :measurement)
  :post! (partial index-post! store store-new queue)
  :handle-ok (partial index-handle-ok store store-new )
  :handle-created index-handle-created)

(defresource measurements-by-reading [store store-new handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :authorized? (authorized? (:querier store) :measurement)
  :handle-ok (partial measurements-by-reading-handle-ok store store-new))
