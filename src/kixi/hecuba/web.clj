(ns kixi.hecuba.web
  (:require
   jig
   [jig.util :refer (get-dependencies satisfying-dependency)]
   [jig.bidi :refer (add-bidi-routes)]
   [bidi.bidi :refer (match-route path-for ->Resources ->Redirect ->Alternates)]
   [ring.middleware.params :refer (wrap-params)]
   [clojure.java.io :as io]
   [clojure.tools.logging :refer :all]
   [clojure.edn :as edn]
   [liberator.core :refer (resource defresource)]
   [kixi.hecuba.kafka :as kafka]
   [kixi.hecuba.protocols :refer (upsert! item items)]
   )
  (:import (jig Lifecycle)))

(def base-media-types ["application/json"])

(defresource reading-resource [reading producer-config]
  :allowed-methods #{:post :get}
  :available-media-types ["application/json"]
  :handle-ok (fn [ctx] @reading)
  :post! (fn [ctx]
           (let [uuid (str (java.util.UUID/randomUUID))]
             (do (swap! reading assoc uuid (-> ctx :request :form-params))
                 {::uuid uuid}))
           ;; Send to kafka
           (println (str "Reading " @reading))
           (kafka/send-msg (str @reading) "readings" producer-config)
           ;; Return the offset
           )
  :handle-created (fn [ctx] (::uuid ctx)))

(defresource projects-resource [querier project-resource]
  :allowed-methods #{:get}
  :available-media-types base-media-types
  :exists? (fn [{{{id :id} :route-params body :body routes :jig.bidi/routes} :request :as ctx}]
             {::projects
              (map (fn [m] (assoc m :href (path-for routes project-resource :id (:id m)))) (items querier))})
  :handle-ok (fn [{projects ::projects {mt :media-type} :representation :as ctx}]
               (case mt
                 "text/html" projects ; do something here to render the href as a link
                 projects)))

(defresource project-resource [querier commander]
  :allowed-methods #{:get :put}
  :available-media-types base-media-types
  :exists? (fn [{{{id :id} :route-params body :body routes :jig.bidi/routes} :request :as ctx}]
             (if-let [p (item querier id)] {::project p} false))
  :handle-ok (fn [ctx] (::project ctx))
  :put! (fn [{{{id :id} :route-params body :body} :request}]
          (let [details (io! (edn/read (java.io.PushbackReader. (io/reader body))))]
            (upsert! commander (assoc details :id id)))))

(defn index [req]
  {:status 200 :body (slurp (io/resource "hecuba/index.html"))})

(defn make-routes [names producer-config querier commander]
  (let [project (project-resource querier commander)
        projects (projects-resource querier project)]
    ["/"
     [["" (->Redirect 307 index)]
      ["overview.html" index]

      ;;["name" (wrap-params (name-resource names))]
      ["reading" (wrap-params (reading-resource names producer-config))]

      ["projects/" projects]
      ["projects" (->Redirect 307 projects)]
      [["projects/" :id] project]

      ;; Static resources
      [(->Alternates ["stylesheets/" "images/" "javascripts/"])
       (->Resources {:prefix "hecuba/"})]

      ]]))

(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (add-bidi-routes system config
                     (make-routes (:names system)
                                  (first (:kixi.hecuba.kafka/producer-config (:hecuba/kafka system)))
                                  (:querier system)
                                  (:commander system))))
  (stop [_ system] system))
