(ns kixi.hecuba.api.rollups
  (:require
   [clojure.string :as string]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]))

(defn retrieve-hourly-measurements
  "Iterate over a sequence of months and concatanate measurements retrieved from the database."
  [session start-date end-date device_id reading_type]
  (let [range  (util/time-range start-date end-date (t/years 1))
        months (map #(util/get-year-partition-key (tc/to-date %)) range)
        where  [[= :device_id device_id]
                [= :type reading_type]
                [>= :timestamp (tc/to-date start-date)]
                [<= :timestamp (tc/to-date end-date)]]]
    (mapcat (fn [month] (db/execute session (hayt/select :hourly_rollups (hayt/where (conj where [= :year month]))))) months)))

(defresource hourly_rollups [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json"  "application/edn"}
  :authorized? (authorized? store)

  :handle-ok (fn [ctx]
               (db/with-session [session (:hecuba-session store)]
                 (let [request        (:request ctx)
                       {:keys [route-params
                               query-string]} request
                       {:keys [device_id
                               reading_type]} route-params
                       decoded-params (util/decode-query-params query-string)
                       start-date     (util/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
                       end-date       (util/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
                       measurements   (retrieve-hourly-measurements session start-date end-date device_id reading_type)]
                   {:measurements (->> measurements
                                       (map (fn [m]
                                              (-> m
                                                  util/parse-value
                                                  (update-in [:timestamp] util/db-to-iso)
                                                  (dissoc :year :metadata :device_id)))))}))))

(defresource daily_rollups [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn"}
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)

  :handle-ok (fn [{{{:keys [device_id reading_type]} :route-params query-string :query-string}
                   :request {mime :media-type} :representation :as req}]
               (db/with-session [session (:hecuba-session store)]
                 (let [decoded-params (util/decode-query-params query-string)
                       start-date     (util/to-db-format (string/replace (get decoded-params "startDate") "%20" " "))
                       end-date       (util/to-db-format (string/replace (get decoded-params "endDate") "%20" " "))
                       measurements   (db/execute session (hayt/select :daily_rollups
                                                                       (hayt/where [[= :device_id device_id]
                                                                                    [= :type reading_type]
                                                                                    [>= :timestamp (tc/to-date start-date)]
                                                                                    [<= :timestamp (tc/to-date end-date)]])))]
                   {:measurements (->> measurements
                                       (map (fn [m]
                                              (-> m
                                                  util/parse-value
                                                  (update-in [:timestamp] util/db-to-iso)
                                                  (dissoc :metadata :device_id)))))}))))
