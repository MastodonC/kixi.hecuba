(ns kixi.hecuba.api.rollups
  (:require
   [bidi.bidi :as bidi]
   [clojure.string :as string]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn retrieve-hourly-measurements
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [querier start-date end-date device_id reading-type]
  (let [range  (util/time-range start-date end-date (t/years 1))
        months (map #(util/get-year-partition-key (tc/to-date %)) range)
        where  [[= :device_id device_id]
                [= :type reading-type]
                [>= :timestamp (tc/to-date start-date)]
                [<= :timestamp (tc/to-date end-date)]]]
    (mapcat (fn [month] (hecuba/items querier :hourly_rollups (conj where [= :year month]))) months)))

(defresource hourly_rollups [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement) ;; TODO authorization for hourly_rollups

  :handle-ok (fn [ctx]
               (let [request        (:request ctx)
                     {:keys [route-params
                             query-string]} request
                     {:keys [device_id
                             reading-type]} route-params
                     decoded-params (util/decode-query-params query-string)
                     start-date     (util/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
                     end-date       (util/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
                     measurements   (retrieve-hourly-measurements querier start-date end-date device_id reading-type)]
                 {:measurements (->> measurements
                                     (map (fn [m]
                                            (-> m
                                                util/parse-value
                                                (update-in [:timestamp] util/db-to-iso)
                                                (dissoc :year :metadata :device_id)))))})))

(defresource daily_rollups [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement) ;;TODO authorization for daily_rollups

  :handle-ok (fn [{{{:keys [device_id reading-type]} :route-params query-string :query-string}
                   :request {mime :media-type} :representation :as req}]
               (let [decoded-params (util/decode-query-params query-string)
                     start-date     (util/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
                     end-date       (util/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
                     measurements   (hecuba/items querier :daily_rollups [[= :device_id device_id]
                                                                          [= :type reading-type]
                                                                          [>= :timestamp (tc/to-date start-date)]
                                                                          [<= :timestamp (tc/to-date end-date)]])]
                 {:measurements (->> measurements
                                     (map (fn [m]
                                            (-> m
                                                util/parse-value
                                                (update-in [:timestamp] util/db-to-iso)
                                                (dissoc :metadata :device_id)))))})))
