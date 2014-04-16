(ns kixi.hecuba.api.rollup
  (:require
   [bidi.bidi :as bidi]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defresource hourly-rollups [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement) ;; TODO authorization for hourly-rollups

  :handle-ok (fn [{{{:keys [device-id reading-type]} :route-params query-string :query-string}
                  :request {mime :media-type} :representation :as req}]
               (let [decoded-params (util/decode-query-params query-string)
                     formatter      (java.text.SimpleDateFormat. "dd-MM-yyyy HH:mm")
                     start-date     (.parse formatter (string/replace (get decoded-params "startDate") "%20" " "))
                     end-date       (.parse formatter (string/replace (get decoded-params "endDate") "%20" " "))
                     measurements   (hecuba/items querier :hourly-rollups [:device-id device-id
                                                                    :type reading-type
                                                                    :year (util/get-year-partition-key start-date)
                                                                    :timestamp [>= start-date]
                                                                    :timestamp [<= end-date]])]
                 (util/render-items req measurements))))

(defresource daily-rollups [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement) ;;TODO authorization for daily-rollups

  :handle-ok (fn [{{{:keys [device-id reading-type]} :route-params query-string :query-string}
                   :request {mime :media-type} :representation :as req}]
               (let [decoded-params (util/decode-query-params query-string)
                     formatter      (java.text.SimpleDateFormat. "dd-MM-yyyy HH:mm")
                     start-date     (.parse formatter (string/replace (get decoded-params "startDate") "%20" " "))
                     end-date       (.parse formatter (string/replace (get decoded-params "endDate") "%20" " "))
                     measurements   (hecuba/items querier :daily-rollups [:device-id device-id
                                                                   :type reading-type
                                                                   :timestamp [>= start-date]
                                                                   :timestamp [<= end-date]])]
                 (util/render-items req measurements))))
