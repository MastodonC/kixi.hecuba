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
   [kixi.hecuba.model :refer (add-project! get-project list-projects)]
   )
  (:import (jig Lifecycle)))

;;(def base-media-types ["text/html" "application/json" "application/edn"])
(def base-media-types ["application/json"])

(defresource projects-resource [store project-resource]
  :allowed-methods #{:get}
  :available-media-types base-media-types
  :exists? (fn [{{{id :id} :route-params body :body routes :jig.bidi/routes} :request :as ctx}]
             {::projects
              (map (fn [m] (assoc m :href (path-for routes project-resource :id (:id m)))) (list-projects store))})
  :handle-ok (fn [{projects ::projects {mt :media-type} :representation :as ctx}]
               (case mt
                 "text/html" projects ; do something here to render the href as a link
                 projects)))

(defresource project-resource [store]
  :allowed-methods #{:get :put}
  :available-media-types base-media-types
  :exists? (fn [{{{id :id} :route-params body :body routes :jig.bidi/routes} :request :as ctx}]
             (if-let [p (get-project store id)] {::project p} false))
  :handle-ok (fn [ctx] (::project ctx))
  :put! (fn [{{{id :id} :route-params body :body} :request}]
          (let [details (io! (edn/read (java.io.PushbackReader. (io/reader body))))]
            (add-project! store id details))))

(defn index [req]
  {:status 200 :body (slurp (io/resource "hecuba/index.html"))})

(defn make-routes [store]
  (let [project (project-resource store)
        projects (projects-resource store project)]
    ["/"
     [["" (->Redirect 307 index)]
      ["overview.html" index]

      ["projects/" projects]
      ["projects" (->Redirect 307 projects)]
      [["projects/" :id] project]

      ;; Static resources
      [(->Alternates ["stylesheets/" "images/" "javascripts/"])
       (->Resources {:prefix "hecuba/"})]

      ]]))

(defn lookup-store [system config]
  (or
   (some (comp :kixi.hecuba.model/store system) (:jig/dependencies config))
   (throw (ex-info "No store found in system" {:dependencies (:jig/dependencies config)
                                                :searched-in (map system (:jig/dependencies config))}))))

(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (println "My channel is " (satisfying-dependency system config 'jig.async/Channel))
    (add-bidi-routes system config (make-routes (lookup-store system config))))
  (stop [_ system] system))
