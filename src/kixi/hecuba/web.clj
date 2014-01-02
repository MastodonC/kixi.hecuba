(ns kixi.hecuba.web
  (:require
   jig
   [jig.bidi :refer (add-bidi-routes)]
   [bidi.bidi :refer (path-for ->Resources)]
   [ring.middleware.params :refer (wrap-params)]
   [clojure.java.io :as io]
   [liberator.core :refer (resource defresource)]
   [cemerick.friend :as friend]
   [cemerick.friend.workflows :as workflows]
   [cemerick.friend.credentials :as creds]
   )
  (:import (jig Lifecycle)))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}
            "jane" {:username "jane"
                    :password (creds/hash-bcrypt "user_password")
                    :roles #{::user}}})

(defresource houses
  :available-media-types ["application/json"]
  :handle-ok {:uuid "599269be-0d47-4be6-986d-20da7129ece4"
              :postcode "NN15 6SF"})

(defresource name-resource [system]
  :allowed-methods #{:get :post}
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx] (-> system :names deref))
  :authorized? (fn [{req :request}] (friend/authorized? [::user ::admin] (friend/identity req)))
  :post! (fn [ctx] (let [uuid (str (java.util.UUID/randomUUID))]
                     (do (swap! (-> system :names) assoc uuid (-> ctx :request :form-params))
                         {::uuid uuid})))
  :handle-created (fn [ctx] (::uuid ctx)))

(defn make-routes [system]
  ["/"
   [["api" houses]
    ["js/" (->Resources {:prefix ""})]
    ["name" (wrap-params (name-resource system))]
    ["index.html" (fn [req]
                    {:status 200 :body (slurp (io/resource "index.html"))})]]])

(deftype Website [config]
  Lifecycle
  (init [_ system]
    (assoc system :names (atom {})))
  (start [_ system]
    (add-bidi-routes system config (make-routes system)))
  (stop [_ system] system))
