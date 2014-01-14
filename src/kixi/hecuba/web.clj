(ns kixi.hecuba.web
  (:require
   jig
   kixi.hecuba.web.project
   kixi.hecuba.web.device
   [jig.util :refer (get-dependencies satisfying-dependency)]
   [jig.bidi :refer (add-bidi-routes)]
   [bidi.bidi :refer (match-route path-for ->Resources ->Redirect ->Alternates)]
   [ring.middleware.params :refer (wrap-params)]
   [clojure.java.io :as io]
   [clojure.tools.logging :refer :all]
   [liberator.core :refer (resource defresource)])
  (:import (jig Lifecycle)))

(def base-media-types ["application/json"])

(defn index [req]
  {:status 200 :body (slurp (io/resource "sb-admin/index.html"))})

(defn index-om [req]
  {:status 200 :body (slurp (io/resource "hecuba/index-om.html"))})

(defn chart [req]
  {:status 200 :body (slurp (io/resource "hecuba/chart.html"))})

(defn maps [req]
  {:status 200 :body (slurp (io/resource "hecuba/map.html"))})

(defn counters [req]
  {:status 200 :body (slurp (io/resource "hecuba/counters.html"))})

(defn readings [req]
  {:status 200 :body (slurp (io/resource "reading.html"))})

(defn make-routes [producer-config querier commander]
  ["/"
   [["" (->Redirect 307 index)]
    ["overview.html" index]
    ["overview-om.html" index-om]
    ["chart.html" chart]
    ["counters.html" counters]
    ["map.html" maps]

    (kixi.hecuba.web.device/create-routes producer-config)
    (kixi.hecuba.web.project/create-routes querier commander)

    ["" (->Resources {:prefix "sb-admin/"})]

    ;; Static resources
    #_[(->Alternates ["stylesheets/" "images/" "javascripts/"])
       (->Resources {:prefix "hecuba/"
                   :mime-types {"map" "text/javascript"}})]]])

(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (add-bidi-routes system config
                     (make-routes (first (:kixi.hecuba.kafka/producer-config (:hecuba/kafka system)))
                                  (:querier system)
                                  (:commander system))))
  (stop [_ system] system))
