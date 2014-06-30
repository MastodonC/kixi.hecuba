(ns kixi.hecuba.api.measurements
  (:require
   [clj-time.coerce :as tc]
   [clj-time.core :as t]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.data.misc :as m]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.data.sensors :as sensor]
   [kixi.hecuba.data.measurements :as measurements]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex time-range)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]))

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
    (let [items (:items ctx)
          sensor (sensor/sensor-exists? session items)]
      sensor)))

(defn measurements-slice-handle-ok [store ctx]
  (let [db-session   (:hecuba-session store)
        {:keys [start-date end-date device_id type]} (:items ctx)]
    (let [measurements (measurements/retrieve-measurements db-session start-date end-date device_id type)]
      (format-measurements ctx measurements))))

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
      (if-let [sensor-and-metadata (sensor/sensor-and-metadata device_id type)]
        (let [validated-measurements (map #(-> %
                                               (prepare-measurement sensor-and-metadata)
                                               (v/validate sensor-and-metadata))
                                          measurements)
              {:keys [min-date max-date]} (m/min-max-dates validated-measurements)]
          (m/insert-measurements store sensor-and-metadata validated-measurements 100)
          {:response {:status 202 :body "Accepted"}})
        {:response {:status 400 :body "Provide valid device_id and type."}}))))

(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

(defn measurements-by-reading-handle-ok [store ctx]
  (let [{:keys [request]} ctx
        {:keys [route-params]} request
        {:keys [device_id type timestamp]} route-params
        t (util/to-db-format timestamp)
        measurement (format-measurements ctx (db/with-session [session (:hecuba-session store)] ;; TODO db stuff to data.* ns.
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
