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

(defn sensor-metadata-for [store sensor-id]
  (let [{:keys [type device-id]} sensor-id]
    (dbnew/with-session [session (:hecuba-session store)]
      (first (dbnew/execute session
                            (hayt/select
                             :sensor_metadata
                             (hayt/where [[= :device_id device-id]
                                          [= :type type]])))))))

(defn- mk-bounds-from [store type device-id]
  (let [sm  (sensor-metadata-for store {:type type :device-id device-id})
        {:keys [lower_ts upper_ts]} sm]
    (vector (atom (or (tc/to-date-time lower_ts) (org.joda.time.DateTime. Long/MAX_VALUE) ))
            (atom (or (tc/to-date-time upper_ts) (org.joda.time.DateTime. 0))))))

(defn resolve-start-end [store type device-id start end]
  (mapv tc/to-date-time
       (if (not (and start end))
         (let [sm (sensor-metadata-for store {:type type :device-id device-id})
               _ (log/info "sm: " sm)
               [lower upper] ((juxt :lower_ts :upper_ts) sm)]
           [(or start lower)
            (or end upper)])
         [start end])))

(defn all-measurements
  "Returns a sequence of all the measurements for a sensor
   matching (type,device-id). The sequence pages to the database in the
   background. The page size is a clj-time Period representing a range
   in the timestamp column. page size defaults to (clj-time/hours 1)"
  ([store sensor-id & [opts]]
     (let [{:keys [type device-id]} sensor-id
           {:keys [page start end] :or {page (t/hours 1)}} opts
           [start end] (resolve-start-end store type device-id start end )
           _ (log/info "s: " start ", e:" end)
           next-start (t/plus start page)]
       (dbnew/with-session [session (:hecuba-session store)]
         (lazy-cat (dbnew/execute session
                                  (hayt/select :measurements
                                               (hayt/where [[= :device_id device-id]
                                                            [= :type type]
                                                            [= :month (m/get-month-partition-key start)]
                                                            [>= :timestamp start]
                                                            [< :timestamp next-start]]))
                                  nil)
                   (when (t/before? next-start end)
                     (all-measurements store sensor-id (merge opts {:start next-start :end end}))))))))

(defn measurements-slice-handle-ok [store store-new ctx]
  (let [request                (:request ctx)
        {:keys [route-params
                query-string]} request
        {:keys [device-id
                reading-type]} route-params
        decoded-params         (util/decode-query-params query-string)
        start-date             (util/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
        end-date               (util/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
        measurements           nil ;; TODO
        ]
    (util/downcast-to-json {:measurements (->> measurements
                                               (map (fn [m]
                                                      (-> m
                                                          util/parse-value
                                                          (update-in [:timestamp] util/db-to-iso)
                                                          (dissoc :month :metadata :device-id)
                                                          util/camelify))))})))





(defn- min-date [dt1 dt2]
  (let [dt1' (tc/to-date-time dt1)
        dt2' (tc/to-date-time dt2)]
    (if (t/before? dt1' dt2') dt1' dt2')))

(defn- max-date [dt1 dt2]
  (let [dt1' (tc/to-date-time dt1)
        dt2' (tc/to-date-time dt2)]
    (if (t/before? dt1' dt2') dt2' dt1')))

(defn index-post! [store store-new queue ctx]
  (let [{:keys [commander querier]} store
        request       (:request ctx)
        route-params  (:route-params request)
        device-id     (:device-id route-params)
        topic         (get-in queue ["measurements"])
        measurements  (:measurements (decode-body request))
        type          (get (first  measurements) "type")]
    (if (and device-id type (not (empty? (first (hecuba/items querier :sensor [[= :device-id device-id] [= :type type]])))))
      (let [[lower upper] (mk-bounds-from store-new type device-id)
            update-bounds! (fn [t] (swap! lower min-date t) (swap! upper max-date t))]
        (doseq [measurement measurements]
          (let [t  (util/db-timestamp (get measurement "timestamp"))
                m  (stringify-values measurement)
                m2 {:device-id device-id
                    :type      type
                    :timestamp t
                    :value     (get m "value")
                    :error     (get m "error")
                    :month     (util/get-month-partition-key t)
                    :metadata  "{}"}]
            (->> m2
                 (v/validate commander querier)
                 (hecuba/upsert! commander :measurement))
            (update-bounds! t)
            (q/put-on-queue topic m2)))
          (hecuba/upsert! commander :sensor-metadata {:device-id device-id
                                                      :type type
                                                      :lower_ts @lower
                                                      :upper_ts @upper})

        (hecuba/upsert! commander :sensor-metadata {:device-id device-id
                                                    :type type
                                                    :lower_ts @lower
                                                    :upper_ts @upper})
        {:response {:status 202 :body "Accepted"}})
      {:response {:status 400 :body "Provide valid deviceId and type."}})))

(defn index-handle-ok [store store-new ctx]
  (let [{:keys [querier]} store
        request (:request ctx)
        route-params (:route-params request)
        device-id (:device-id route-params)
        where [[= :device_id device-id]]
        measurements (hecuba/items querier :measurement where)]
    (util/downcast-to-json {:measurements (->> measurements
                                               (map #(-> %
                                                         util/parse-value
                                                         (update-in [:timestamp] util/db-to-iso)
                                                         (dissoc :metadata :device-id :month)
                                                         util/camelify)))})))

(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

(defn measurements-by-reading-handle-ok [store store-new ctx]
  (let [{:keys [request]} ctx
        {:keys [route-params]} request
        {:keys [device-id sensor-type timestamp]} route-params
        measurement (dbnew/with-session [session (:hecuba-session store)]
                      (dbnew/execute session
                                     (hayt/select :measurements
                                                  (hayt/where [[= :device_id device-id]
                                                               [= :type sensor-type]
                                                               [= :timestamp timestamp]]))))]
    (util/render-item request measurement)))


(defresource measurements-slice [store store-new handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
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
