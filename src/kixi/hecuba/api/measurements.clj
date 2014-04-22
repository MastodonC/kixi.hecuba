(ns kixi.hecuba.api.measurements
  (:require
   [bidi.bidi :as bidi]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn measurements-slice-handle-ok [querier ctx]
  (let [request                (:request ctx)
        {:keys [route-params
                query-string]} request
        {:keys [device-id
                reading-type]} route-params
        decoded-params         (util/decode-query-params query-string)
        formatter              (java.text.SimpleDateFormat. "dd-MM-yyyy HH:mm")
        start-date             (.parse formatter (string/replace (get decoded-params "startDate") "%20" " "))
        end-date               (.parse formatter (string/replace (get decoded-params "endDate") "%20" " "))
        measurements           (hecuba/items querier :measurement [[= :device-id device-id]
                                                                   [= :type reading-type]
                                                                   [= :month (util/get-month-partition-key start-date)]
                                                                   [>= :timestamp start-date]
                                                                   [<= :timestamp end-date]])]
    (->> measurements
         (map #(update-in % [:timestamp] str))
         (util/render-item request))))

(defn index-post! [commander querier queue ctx]
  (let [request      (:request ctx)
        route-params (:route-params request)
        device-id    (:device-id route-params)
        topic        (get-in queue ["measurements"])
        measurements (:measurements (decode-body request))
        type         (get (first  measurements) "type")]
    (if (and device-id type (not (empty? (first (hecuba/items querier :sensor [[= :device-id device-id] [= :type type]])))))
      (do
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
            (q/put-on-queue topic m2)))
        {:response {:status 202 :body "Accepted"}})
      {:response {:status 400 :body "Provide valid deviceId and type."}})))

(defn index-handle-ok [querier ctx]
  (let [request (:request ctx)
        route-params (:route-params request)
        device-id (:device-id route-params)
        where [[= :device-id device-id]]
        measurements (hecuba/items querier :measurement where)]
    (util/render-items request (->> measurements
                                    (map #(-> %
                                              (dissoc :metadata :device-id :month)))))))

(defn index-handle-created [ctx]
  (ring-response (:response ctx)))

(defn measurements-by-reading-handle-ok [querier ctx]
  (let [{:keys [request]} ctx
        {:keys [route-params]} request
        {:keys [device-id sensor-type timestamp]} route-params
        measurement (first (hecuba/items querier :measurement [[= :device-id device-id] [= :type sensor-type] [= :timestamp timestamp]]))]
    (util/render-item request measurement)))

(defresource measurements-slice [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)
  :handle-ok (partial measurements-slice-handle-ok querier))

(defresource index [{:keys [commander querier]} queue handlers]
  :allowed-methods #{:post :get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)
  :post! (partial index-post! commander querier queue)
  :handle-ok (partial index-handle-ok querier)
  :handle-created index-handle-created)

(defresource measurements-by-reading [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :authorized? (authorized? querier :measurement)
  :handle-ok (partial measurements-by-reading-handle-ok querier))
