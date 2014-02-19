(ns kixi.hecuba.web
  (:require
   jig
   kixi.hecuba.web.device
   kixi.hecuba.web.messages
   [kixi.hecuba.data :as data]
   [jig.util :refer (get-dependencies satisfying-dependency)]
   [jig.bidi :refer (add-bidi-routes)]
   [bidi.bidi :refer (match-route path-for ->Resources ->Redirect ->Alternates)]
   [ring.middleware.params :refer (wrap-params)]
   [clojure.java.io :as io]
   [clojure.tools.logging :refer :all]
   [liberator.core :refer (resource defresource)])
  (:import (jig Lifecycle)))

(defn index [req]
  {:status 200 :body (slurp (io/resource "sb-admin/index.html"))})

(defn tables [req]
  {:status 200 :body (slurp (io/resource "sb-admin/tables.html"))})

(defn chart [req]
  {:status 200 :body (slurp (io/resource "hecuba/chart.html"))})

(defn maps [req]
  {:status 200 :body (slurp (io/resource "hecuba/map.html"))})

(defn counters [req]
  {:status 200 :body (slurp (io/resource "hecuba/counters.html"))})

(defn readings [req]
  {:status 200 :body (slurp (io/resource "reading.html"))})

(defn make-routes [producer-config {:keys [querier commander]}]
  ["/"
   [["" (->Redirect 307 tables)]
    ["index.html" index]
    ["tables.html" tables]
    ["chart.html" chart]
    ["counters.html" counters]
    ["map.html" maps]

    (kixi.hecuba.web.device/create-routes producer-config)
    (kixi.hecuba.web.messages/create-routes querier commander)

    ["hecuba-js/react.js" (->Resources {:prefix "sb-admin/"})]
    ["" (->Resources {:prefix "sb-admin/"})]
    ]])

(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [commander (:commander system)
          querier (:querier system)
          routes (make-routes (first (:kixi.hecuba.kafka/producer-config (:hecuba/kafka system)))
                              {:querier querier :commander commander})]
      (-> system
          (add-bidi-routes config routes))))

  (stop [_ system] system))
