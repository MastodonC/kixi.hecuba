(ns kixi.hecuba.web
  (:require
   jig
   [kixi.hecuba.web.resources :refer (items-resource item-resource)]
   kixi.hecuba.web.property
   kixi.hecuba.web.device
   kixi.hecuba.web.messages
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

#_(defn create-handler-pairs
  "Pairs are of form [:home-key :object-key], like [:mice :mouse]. The
  singular form (:mouse) is used to form the where clause in the
  resource."
  [commander querier pairs]
  (->>
   (for [[plural singular] pairs]
           (let [detail (item-resource querier)]
             {singular detail
              plural (items-resource singular querier commander detail parent-resource)}))
   ;; Merge all the pairs together to form a single handler map
   (apply merge)))

(defn create-handlers [querier commander]
  (let [property-resource (item-resource querier nil)
        project-resource (item-resource querier property-resource)
        project-home (items-resource :project querier commander project-resource nil)
        property-home (items-resource :property querier commander property-resource project-resource)]
    {:projects project-home
     :project project-resource
     :properties property-home
     :property property-resource}))

(defn make-routes [producer-config querier commander handlers]
  ["/"
   [["" (->Redirect 307 tables)]
    ["index.html" index]
    ["tables.html" tables]
    ["chart.html" chart]
    ["counters.html" counters]
    ["map.html" maps]

    (kixi.hecuba.web.device/create-routes producer-config)
    (kixi.hecuba.web.property/create-routes querier commander)
    (kixi.hecuba.web.messages/create-routes querier commander)

    ;; Projects
    ["projects/" (:projects handlers)]
    ["projects" (->Redirect 307 (:projects handlers))]
    [["projects/" :hecuba/id] (:project handlers)]

    ;; Properties, with an 'X' suffix to avoid conflicting with Anna's
    ;; work until we integrate this.
    ;; Eventually these routes can be generated from the keyword pairs.
    ["propertiesX/" (:properties handlers)]
    ["propertiesX" (->Redirect 307 (:properties handlers))]
    [["propertiesX/" :hecuba/id] (:property handlers)]

    ["hecuba-js/react.js" (->Resources {:prefix "sb-admin/"})]
    ["" (->Resources {:prefix "sb-admin/"})]

    ;; Static resources
    #_[(->Alternates ["stylesheets/" "images/" "javascripts/"])
       (->Resources {:prefix "hecuba/"})]]])




(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [commander (:commander system)
          querier (:querier system)
          handlers (create-handlers  querier commander)]

      (-> system
          (add-bidi-routes
           config
           (make-routes (first (:kixi.hecuba.kafka/producer-config (:hecuba/kafka system)))
                        querier commander handlers))
          (update-in [:handlers] merge handlers))))
  (stop [_ system] system))
