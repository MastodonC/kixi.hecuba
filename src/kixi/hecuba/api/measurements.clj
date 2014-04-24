(ns kixi.hecuba.api.measurements
  (:require
   [bidi.bidi :as bidi]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [clj-time.coerce :as tc]
   [clj-time.format :as tf]
   [clj-time.core :as t]
   [clj-time.periodic :as tp]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn parse-value 
  "AMON API specifies that when value is not present, error must be returned and vice versa."
  [measurement]
  (let [value (:value measurement)]
    (if-not (empty? value)
      (-> measurement
          (update-in [:value] read-string)
          (dissoc :error))
      (dissoc measurement :value))))

(def formatter (tf/formatter "yyyy-MM-dd'T'HH:mm:ssZ"))
(defn to-db-format [date]
  (tf/parse formatter date))
(defn db-to-iso [s]
  (let [date (misc/to-timestamp s)]
    (tf/unparse formatter (tc/from-date date))))

(defn time-range
  "Return a lazy sequence of DateTime's from start to end, incremented
  by 'step' units of time."
  [start end step]
  (let [start-date (t/first-day-of-the-month start)
        end-date   (t/last-day-of-the-month end)
        in-range-inclusive? (complement (fn [t] (t/after? t end-date)))]
    (take-while in-range-inclusive? (tp/periodic-seq start-date step))))

(defn retrieve-measurements 
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [querier start-date end-date device-id reading-type]
  (let [range  (time-range start-date end-date (t/months 1))
        months (map #(util/get-month-partition-key (tc/to-date %)) range)
        where  [[= :device-id device-id]
                [= :type reading-type]
                [>= :timestamp (tc/to-date start-date)]
                [<= :timestamp (tc/to-date end-date)]]]
    (mapcat (fn [month] (hecuba/items querier :measurement (conj where [= :month month]))) months)))

(defn measurements-slice-handle-ok [querier ctx]
  (let [request                (:request ctx)
        {:keys [route-params
                query-string]} request
        {:keys [device-id
                reading-type]} route-params
        decoded-params         (util/decode-query-params query-string)
        start-date             (to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
        end-date               (to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
        measurements           (retrieve-measurements querier start-date end-date device-id reading-type)]
    (util/downcast-to-json {:measurements (->> measurements
                                               (map (fn [m]
                                                      (-> m
                                                          parse-value
                                                          (update-in [:timestamp] db-to-iso)
                                                          (dissoc :month :metadata :device-id)
                                                          util/camelify))))})))

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
    (util/downcast-to-json {:measurements (->> measurements
                                               (map #(-> %
                                                         parse-value
                                                         (update-in [:timestamp] db-to-iso)
                                                         (dissoc :metadata :device-id :month)
                                                         util/camelify)))})))

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
