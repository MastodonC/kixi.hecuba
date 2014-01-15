(ns kixi.hecuba.web
  (:require
   jig
   [kixi.hecuba.web.resources :refer (projects-resource project-resource)]
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

(defn make-routes [producer-config querier commander project projects]
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
    [["project/" :id] project]
    ["projects/" projects]
    ["projects" (->Redirect 307 projects)]

    ["hecuba-js/react.js" (->Resources {:prefix "sb-admin/"})]
    ["" (->Resources {:prefix "sb-admin/"})]

    ;; Static resources
    #_[(->Alternates ["stylesheets/" "images/" "javascripts/"])
       (->Resources {:prefix "hecuba/"})]]])

(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [querier (:querier system)
          commander (:commander system)
          project (project-resource querier)
          projects (projects-resource querier commander project)]
      (-> system
          (add-bidi-routes
           config
           (make-routes (first (:kixi.hecuba.kafka/producer-config (:hecuba/kafka system)))
                        querier
                        commander
                        project
                        projects))
          (update-in [:handlers] merge {:project project :projects projects}))))
  (stop [_ system] system))
