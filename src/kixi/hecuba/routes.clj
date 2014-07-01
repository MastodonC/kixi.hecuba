(ns kixi.hecuba.routes
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [ring.util.response :refer (redirect)]
   [compojure.core :refer (routes ANY GET POST)]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [ring.middleware.session.cookie :refer (cookie-store)]
   [cemerick.friend :as friend]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [com.stuartsierra.component :as component]
   [org.httpkit.server :refer (run-server)]
   [kixi.hecuba.web-paths :refer (compojure-route amon-index-route amon-resource-route)]
   [kixi.hecuba.security :as security]
   [kixi.hecuba.session :refer (cassandra-store)]

   [kixi.hecuba.api.programmes :as programmes]
   [kixi.hecuba.api.projects :as projects]
   [kixi.hecuba.api.properties :as properties]
   [kixi.hecuba.api.entities :as entities]
   [kixi.hecuba.api.profiles :as profiles]
   [kixi.hecuba.api.devices :as devices]
   [kixi.hecuba.api.measurements :as measurements]
   [kixi.hecuba.api.rollups :as rollups]
   [kixi.hecuba.api.datasets :as datasets]
   [kixi.hecuba.api.templates :as templates]
   [kixi.hecuba.api.uploads :as uploads]))

(defn index-page [req]
  {:status 200
   :body (slurp (io/resource "site/index.html"))})

(defn not-found-page [req]
  (log/infof "404 Request: %s" (:uri req))
  {:status 404 :body (slurp (io/resource "site/not-found.html"))})

(defn login-form [req]
  {:status 200 :body (slurp (io/resource "site/login.html"))})

(defn app-page [req]
  (log/infof "App Session: %s" (:session req))
  {:status 200 :body (slurp (io/resource "site/app.html"))})

(defn index-routes
  ([route handler]
     (routes
      (ANY (amon-index-route route)
           []
           handler)
      ;; index redirect
      (ANY (amon-resource-route route)
           []
           (fn [request]
             (let [redirect-route (str (:uri request) "/")]
               (log/infof "Redirecting to: %s" redirect-route)
               (redirect redirect-route))))))
  ([route keys handler]
     (let [params (mapv #(symbol (name %)) keys)]
       (routes
        (ANY (amon-index-route route keys)
             params
             handler)
        ;; index redirect
        (ANY (amon-resource-route route keys)
             params
             (fn [request]
               (let [redirect-route (str (:uri request) "/")]
                 (log/infof "Redirecting to: %s" redirect-route)
                 (redirect redirect-route))))))))

(defn resource-route [route keys handler]
  (let [params (mapv #(symbol (name %)) keys)]
    (ANY (amon-resource-route route keys)
         params
         handler)))

(defn amon-api-routes [store s3 pipeline-head]
  (routes
   ;; API
   ;; Programmes
   (index-routes :programmes-index (programmes/index store))
   (resource-route :programme-resource [:programme_id] (programmes/resource store))

   ;; Programmes/Projects
   (index-routes :programme-projects-index [:programme_id] (projects/index store))
   (resource-route :programme-projects-resource [:programme_id :project_id] (projects/resource store))

   ;; Projects/Properties
   (index-routes :project-properties-index [:project_id] (properties/index store))

   ;; Entities
   (index-routes :entities-index (entities/index store))
   (resource-route :entity-resource [:entity_id] (entities/resource store))

   ;; Datasets
   (index-routes :entity-datasets-index [:entity_id] (datasets/index store))
   (resource-route :entity-resource [:entity_id :name] (datasets/resource store))

   ;; Profiles
   (index-routes :entity-profiles-index [:entity_id] (profiles/index store))
   (resource-route :entity-profiles-resource [:entity_id :profile_id] (profiles/resource store))

   ;; Entity/Devices
   (index-routes :entity-devices-index [:entity_id] (devices/index store))
   (resource-route :entity-device-resource [:entity_id :device_id] (devices/resource store))

   ;; Measurements
   (index-routes :entity-device-measurement-index [:entity_id :device_id] (measurements/index store s3 pipeline-head))

   ;; Readings
   ;; FIXME This should be an index route with the start/stop times
   (resource-route :entity-device-measurement-readingtype-index [:entity_id :device_id :type] (measurements/measurements-slice store))

   ;; Readings by Time Range
   ;; FIXME - This should be an index route with the start/stop times
   (resource-route :entity-device-measurement-readingtype-index [:entity_id :device_id :type] (measurements/measurements-slice store))
   (resource-route :entity-device-measurement-readingtype-resource [:entity_id :device_id :type :timestamp] (measurements/measurements-by-reading store))

   ;; Hourly Measurements FIXME - should be a auto chosen with a type=raw for a force
   (resource-route :entity-device-measurement-readingtype-hourly-rollups [:entity_id :device_id :type] (rollups/hourly_rollups store))

   ;; Daily Measurements
   (resource-route :entity-device-measurement-readingtype-daily-rollups [:entity_id :device_id :type] (rollups/daily_rollups store))

   ;; Templates
   (index-routes :templates-index (templates/index store))
   (resource-route :templates-resource [:template_id] (templates/resource store))
   (resource-route :entity-templates-resource [:entity_id] (templates/entity-resource store))
   (index-routes :measurements [] (measurements/index store s3 pipeline-head))

   ;; Uploads
   (resource-route :uploads-status-resource [:upload_id] (uploads/status-resource store))
   (resource-route :uploads-data-resource [:upload_id] (uploads/data-resource store))
   ))

(defn all-routes [store s3 pipeline-head]
  (routes

   ;; landing page
   (GET "/" [] index-page)

   ;; clojurescript
   (route/resources "/cljs/src/public/js/out" {:root "cljs/src/public/js/out/"})
   (route/resources "/cljs" {:root "cljs/"})

   ;; Log In and Log Out
   (GET (compojure-route :login) [] login-form)
   (friend/logout (ANY (compojure-route :logout) request (redirect "/")))

   ;; Main Application
   (GET (compojure-route :app) []
        (friend/wrap-authorize
         app-page
         #{:kixi.hecuba.security/user}))

   ;; AMON API Routes
   (amon-api-routes store s3 pipeline-head)

   ;; js/css/etc
   (route/resources "/" {:root "site/"})

   ;; 404 - if nothing else matches, then this is the end, my friend.
   (route/not-found not-found-page)))

(defrecord Routes [context]
  component/Lifecycle
  (start [this]
    (let [store         (:store this)
          s3            (:s3 this)
          pipeline-head (:head (:pipeline this))
          all-routes    (handler/site
                         (friend/authenticate
                          (all-routes store s3 pipeline-head)
                          {:credential-fn (partial creds/bcrypt-credential-fn (security/get-user store))
                           :allow-anon? true
                           :default-landing-uri "/app"
                           :workflows
                           ;; Note that ordering matters here. Basic first.
                           [(workflows/http-basic :realm "/")
                            (workflows/interactive-form :login-uri "/login")]})
                         ;; Don't forget that Cassandra Session Store
                         {:session {:store (cassandra-store store)}})
          server        (run-server all-routes {:port 8010})]
      (assoc this ::server server)))
  (stop [this]
    ((::server this))))

(defn new-web-app [context]
  (->Routes context))
