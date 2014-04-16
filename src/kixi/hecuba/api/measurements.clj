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

  (let [{{{:keys [device-id reading-type]} :route-params
          query-string :query-string :as request} :request
          {mime :media-type} :representation} ctx
        decoded-params (util/decode-query-params query-string)
        formatter      (java.text.SimpleDateFormat. "dd-MM-yyyy HH:mm")
        start-date     (.parse formatter (string/replace (get decoded-params "startDate") "%20" " "))
        end-date       (.parse formatter (string/replace (get decoded-params "endDate") "%20" " "))
        measurements   (hecuba/items querier :measurement [:device-id device-id
                                                           :type reading-type
                                                           :month (util/get-month-partition-key start-date)
                                                           :timestamp [>= start-date]
                                                           :timestamp [<= end-date]])]
    (util/render-items request measurements)))

(defn index-post! [commander querier queue ctx]
  (let [{{{:keys [device-id]} :route-params} :request :as req} ctx
        topic         (get-in queue ["measurements"])
        measurements  (:measurements (decode-body req))
        type          (get (first  measurements) "type")]
    (if (and device-id type (not (empty? (first (hecuba/items querier :sensor {:device-id device-id :type type})))))
      (do
        (doseq [measurement measurements]
          (let [t        (util/db-timestamp (get measurement "timestamp"))
                m        (stringify-values measurement)
                m2       {:device-id device-id
                          :type type
                          :timestamp t
                          :value (get m "value")
                          :error (get m "error")
                          :month (util/get-month-partition-key t)
                          :metadata "{}"}]
            (->> m2
                 (v/validate commander querier)
                 (hecuba/upsert! commander :measurement))
            (q/put-on-queue topic m2)))
        {:response {:status 202 :body "Accepted"}})
      {:response {:status 400 :body "Provide valid deviceId and type."}})))

(defn index-handle-ok [querier ctx]
  (let [{:keys [representation request]} ctx
        route-params (:route-params request)
        device-id (:device-id route-params)
        mime (:media-type representation)
        measurements (hecuba/items querier :measurement {:device-id device-id})]
    (util/render-items request measurements)))

(defn index-handle-created [{response :response {routes :modular.bidi/routes} :request}]
  (ring-response response))

(defn measurements-by-reading-handle-ok [querier ctx]
  (let [{{{:keys [device-id sensor-type timestamp]} :route-params} :request {mime :media-type} :representation :as req} ctx
        measurement (first (hecuba/items querier :measurement {:device-id device-id :type sensor-type :timestamp timestamp}))]
    (util/render-item req measurement)))

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
