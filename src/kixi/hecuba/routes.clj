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
   [kixi.hecuba.api.entities :as entities]
   [kixi.hecuba.api.profiles :as profiles]
   [kixi.hecuba.api.devices :as devices]
   [kixi.hecuba.api.measurements :as measurements]
   [kixi.hecuba.api.rollups :as rollups]
   [kixi.hecuba.api.datasets :as datasets]
   [kixi.hecuba.api.templates :as templates]
   [kixi.hecuba.api.downloads :as downloads]
   [kixi.hecuba.api.uploads :as uploads]
   [kixi.hecuba.api.entities.upload :as entity-uploads]
   [kixi.hecuba.api.entities.property-map :as map]
   [kixi.hecuba.api.users :as users]
   [environ.core :refer [env]]
   [net.cgrand.enlive-html :refer [deftemplate append html]]))

(def is-dev? (env :is-dev))

(def inject-devmode-html
  (comp
    (append (html [:script {:type "text/javascript"} "goog.require('kixi.hecuba.env.dev')"]))))

(def inject-prodmode-html
  (comp
   (append (html [:script {:type "text/javascript"} "goog.require('kixi.hecuba.env.prod')"]))))

(defn index-page [req]
  {:status 200
   :body (slurp (io/resource "site/index.html"))})

(defn registration-error-page [req]
  {:status 200
   :body (slurp (io/resource "site/registration-error.html"))})

(defn not-found-page [req]
  (log/infof "404 Request: %s" (:uri req))
  {:status 404 :body (slurp (io/resource "site/not-found.html"))})

(defn login-form [req]
  {:status 200 :body (slurp (io/resource "site/login.html"))})

(defn reset-form [req]
  {:status 200 :body (slurp (io/resource "site/reset.html"))})

(defn new-password [req]
  {:status 200 :body (slurp (io/resource "site/password_change.html"))})

(defn reset-error-page [req]
  {:status 200
   :body (slurp (io/resource "site/password_change_error.html"))})

(deftemplate page
  (io/resource "site/app.html") [] [:html] (if is-dev? inject-devmode-html inject-prodmode-html))

(defn app-page [req]
  (log/infof "App Session: %s" (:session req))
  {:status 200 :body (page)})

(defn multiple-properties-comparison [req]
  (log/infof "App Session: %s" (:session req))
  {:status 200 :body (slurp (io/resource "site/charts.html"))})

(defn property_map [req]
  (log/infof "App Session: %s" (:session req))
  {:status 200 :body (slurp (io/resource "site/property_map.html"))})

(defn user-management [req]
  (log/infof "App Session: %s" (:session req))
  {:status 200 :body (slurp (io/resource "site/user-management.html"))})

(defn new-profile [req]
  (log/infof "App Session: %s" (:session req))
  {:status 200 :body (slurp (io/resource "site/new_profile.html"))})

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
   (index-routes :projects-index (projects/index store))

   ;; Projects/Properties
   (index-routes :project-entities-index [:project_id] (entities/index store))

   ;; Entities
   (index-routes :entities-index (entities/index store))
   (resource-route :entity-resource [:entity_id] (entities/resource store))

   ;; Datasets
   (index-routes :entity-datasets-index [:entity_id] (datasets/index store))
   (resource-route :entity-dataset-resource [:entity_id :dataset_id] (datasets/resource store))

   ;; Profiles
   (index-routes :entity-profiles-index [:entity_id] (profiles/index store))
   (resource-route :entity-profiles-resource [:entity_id :profile_id] (profiles/resource store))

   ;; Entity/Devices
   (index-routes :entity-devices-index [:entity_id] (devices/index store))
   (resource-route :entity-device-resource [:entity_id :device_id] (devices/resource store))
   (resource-route :entity-device-sensor-resource [:entity_id :device_id :type] (devices/sensor-resource store))

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
   (resource-route :entity-templates-resource [:entity_id] (templates/entity-resource store pipeline-head))
   (index-routes :measurements [:entity_id] (measurements/index store s3 pipeline-head))

   ;; Uploads
   (resource-route :uploads-status-resource [:user_id :upload_id] (uploads/uploads-status-resource store))
   (resource-route :uploads-data-resource [:user_id :upload_id] (uploads/uploads-data-resource store))
   (resource-route :upload-status-for-username [:programme_id :project_id :entity_id] (uploads/uploads-for-username store))

   ;; Downloads
   (resource-route :download-data-resource [:entity_id] (downloads/downloads-data-resource store))
   (resource-route :download-status [:programme_id :project_id :entity_id] (downloads/downloads-for-entity store))

   ;; Entity image/document upload (same code, different upload based on uri.
   (index-routes :entity-images-index [:entity_id] (entity-uploads/index store s3 pipeline-head))
   (index-routes :entity-documents-index [:entity_id] (entity-uploads/index store s3 pipeline-head))

   ;; Usernames
   (index-routes :username-index (users/index store))
   (resource-route :username-resource [:username] (users/resource store))

   (index-routes :entity-property-having-locations (map/index store))

   (index-routes :whoami-resource (users/whoami store))))

(defn all-routes [store s3 pipeline-head]
  (routes

   ;; landing page
   (GET "/" [] index-page)

   ;; clojurescript
   (route/resources "/cljs" {:root "cljs"})

   ;; Log In and Log Out
   (GET (compojure-route :login) [] login-form)
   (friend/logout (ANY (compojure-route :logout) request (redirect "/")))

   ;; Register User
   (GET "/register" [] (redirect "/app"))
   (POST "/register" request (security/register-user request store))
   (GET "/registration-error" [] registration-error-page)

   ;; Reset Password
   (GET "/reset" [] reset-form)
   (POST "/reset" request (security/reset-password-email request store))
   (GET "/reset/:uuid" [uuid] (security/reset-password uuid store))
   (POST "/reset/:uuid" request (security/post-new-password request store))
   (GET "/reset-error" [] reset-error-page)

   ;; Main Application
   (GET (compojure-route :app) []
        (friend/wrap-authorize
         app-page
         #{:kixi.hecuba.security/user}))

   ;; Multiple properties comparison
   (GET (compojure-route :multiple-properties-comparison) []
        (friend/wrap-authorize
         multiple-properties-comparison
         #{:kixi.hecuba.security/user}))

   ;; Property map
   (GET (compojure-route :property_map) []
        (friend/wrap-authorize
         property_map
         #{:kixi.hecuba.security/user}))

   ;; User management
   (GET (compojure-route :user-management) []
        (friend/wrap-authorize
         user-management
         #{:kixi.hecuba.security/project-manager}))

   ;; New profile
   (GET "/profile/:entity_id" [entity_id]
        (friend/wrap-authorize
         new-profile
         #{:kixi.hecuba.security/project-manager}))

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
          server        (run-server all-routes {:port 8010 :max-body (* 16 1024 1024)})]
      (assoc this ::server server)))
  (stop [this]
    ((::server this))))

(defn new-web-app [context]
  (->Routes context))
