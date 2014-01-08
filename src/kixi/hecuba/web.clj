(ns kixi.hecuba.web
  (:require
   jig
   [jig.bidi :refer (add-bidi-routes)]
   [bidi.bidi :refer (match-route path-for ->Resources ->Redirect ->Alternates)]
   [ring.middleware.params :refer (wrap-params)]
   [clojure.java.io :as io]
   [clojure.tools.logging :refer :all]
   [clojure.edn :as edn]
   [liberator.core :refer (resource defresource)]
   )
  (:import (jig Lifecycle)))

(defn add-project [r id m]
  (infof "Adding project: %s" id)
  (alter r assoc id m))

(defresource houses
  :available-media-types ["application/json"]
  :handle-ok {:uuid "599269be-0d47-4be6-986d-20da7129ece4"
              :postcode "NN15 6SF"})

(defresource project-resource [projects]
  :allowed-methods #{:put}
  :available-media-types ["application/edn"]
  :exists false
  :put! (fn [{{{id :id} :route-params body :body} :request}]
          (let [details (io! (edn/read (java.io.PushbackReader. (io/reader body))))]
            (dosync
             (add-project projects id details)))))

(defresource name-resource [names]
  :allowed-methods #{:get :post}
  :available-media-types ["application/json" "application/edn"]
  :handle-ok (fn [ctx] @names)
  :post! (fn [ctx] (let [uuid (str (java.util.UUID/randomUUID))]
                     (do (swap! names assoc uuid (-> ctx :request :form-params))
                         {::uuid uuid})))
  :handle-created (fn [ctx] (::uuid ctx)))

(defn index [req]
  {:status 200 :body (slurp (io/resource "hecuba/index.html"))})

(defn make-routes [names]
  ["/"
   [["" (->Redirect 307 index)]
    ["overview.html" index]

    ["api" houses]
    ["name" (wrap-params (name-resource names))]

    [["projects/" :id] (project-resource (ref {}))]

    ;; Static resources
    [(->Alternates ["stylesheets/" "images/" "javascripts/"])
     (->Resources {:prefix "hecuba/"})]

    ]])

(deftype Website [config]
  Lifecycle
  (init [_ system]
    (assoc system :names (atom {})))
  (start [_ system]
    (add-bidi-routes system config (make-routes (:names system))))
  (stop [_ system] system))
