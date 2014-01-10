(ns kixi.hecuba.web
  (:require
   jig
   kixi.hecuba.web.project
   [jig.util :refer (get-dependencies satisfying-dependency)]
   [jig.bidi :refer (add-bidi-routes)]
   [bidi.bidi :refer (match-route path-for ->Resources ->Redirect ->Alternates)]
   [ring.middleware.params :refer (wrap-params)]
   [clojure.java.io :as io]
   [clojure.tools.logging :refer :all]
   [clojure.edn :as edn]
   [liberator.core :refer (resource defresource)]
   [kixi.hecuba.hash :refer (sha1)]
   [kixi.hecuba.kafka :as kafka]

   )
  (:import (jig Lifecycle)))

(def base-media-types ["application/json"])

(defresource reading-resource [producer-config]
  :allowed-methods #{:post}
  :available-media-types base-media-types
  :post! (fn [ctx]
           (let [uuid (str (java.util.UUID/randomUUID))
                 reading (-> ctx :request :form-params)]
           (kafka/send-msg (str {(keyword uuid) reading}) "readings" producer-config)))
  :post-redirect? (fn [ctx] {:location (format "/readings/reading")}))



(defn index [req]
  {:status 200 :body (slurp (io/resource "hecuba/index.html"))})

(defn readings [req]
  {:status 200 :body (slurp (io/resource "reading.html"))})

(defn make-routes [producer-config querier commander]
  ["/"
   [["" (->Redirect 307 index)]
    ["overview.html" index]

    ["readings" {"/reading" (->Redirect 307 readings)
                 "/new" { "" readings
                          "/post" (wrap-params (reading-resource producer-config))}}]

    (kixi.hecuba.web.project/create-routes querier commander)

    ;; Static resources
    [(->Alternates ["stylesheets/" "images/" "javascripts/"])
     (->Resources {:prefix "hecuba/"})]

    ]])

(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (add-bidi-routes system config
                     (make-routes (first (:kixi.hecuba.kafka/producer-config (:hecuba/kafka system)))
                                  (:querier system)
                                  (:commander system))))
  (stop [_ system] system))
