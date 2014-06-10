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
   [com.stuartsierra.component :as component]
   [org.httpkit.server :refer (run-server)]
   [kixi.hecuba.web-paths :refer (compojure-route amon-index-route amon-resource-route)]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.session :refer (cassandra-store)]
   
   [kixi.hecuba.api.programmes :as programmes]
   [kixi.hecuba.api.projects :as projects]
   [kixi.hecuba.api.properties :as properties]
   [kixi.hecuba.api.entities :as entities]
   [kixi.hecuba.api.profiles :as profiles]
   [kixi.hecuba.api.devices :as devices]
   [kixi.hecuba.api.measurements :as measurements]
   [kixi.hecuba.api.rollups :as rollups]
   [kixi.hecuba.api.datasets :as datasets]))

(defn index-page [req]
  (log/infof "Index Session: %s" (:session req))
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
           (let [redirect-route (str (:uri params) "/")]
             (log/infof "Redirecting to: %s" redirect-route)
             (redirect redirect-route)))))
  ([route keys handler]
     (let [params (mapv #(symbol (name %)) keys)]
       (routes
        (ANY (amon-index-route route keys)
             params
             handler)
        ;; index redirect
        (ANY (amon-resource-route route keys)
             params
             (let [redirect-route (str (:uri params) "/")]
               (log/infof "Redirecting to: %s" redirect-route)
               (redirect redirect-route)))))))

(defn resource-route [route keys handler]
  (let [params (mapv #(symbol (name %)) keys)]
    (ANY (amon-resource-route route keys)
         params
         handler)))

(defn app-routes [store measurements-queue]
  (routes
   ;; landing page
   (GET "/" [] index-page)
   ;; login/logout
   (GET (compojure-route :login) [] login-form)
   (friend/logout (ANY (compojure-route :logout) request (redirect "/")))
      
   ;; main application
   (GET (compojure-route :app) [] app-page)
   
   ;; clojurescript
   (route/resources "/cljs" {:root "cljs/"})
   
   ;; js/css/etc
   (route/resources "/" {:root "site/"})

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

   ;; Profiles
   (index-routes :entity-profiles-index [:entity_id] (profiles/index store))
   (resource-route :entity-profiles-resource [:entity_id :profile_id] (profiles/resource store))

   ;; Entity/Devices
   (index-routes :entity-devices-index [:entity_id] (devices/index store))
   (resource-route :entity-device-resource [:entity_id :device_id] (devices/resource store))

   ;; Measurements
   (index-routes :entity-device-measurement-index [:entity_id :device_id] (measurements/index store measurements-queue))

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
   (resource-route :entity-device-measurement-readingtype-daily-rollups [:entity_id :device_id :reading_type] (rollups/hourly_rollups store))

   ;; 404 - if nothing else matches, then this is the end, my friend.
   (route/not-found not-found-page)))

(defrecord Routes [context]
  component/Lifecycle
  (start [this]
    (let [store  (:store this)
          queue  (get-in this [:queue :queue])
          app    (-> (app-routes store queue)
                     (sec/friend-middleware store)
                     (handler/site {:session {:store (cassandra-store store)}}))
          server (run-server app {:port 8010})]
      (assoc this ::server server)))
  (stop [this]
    ((::server this))))

(defn new-web-app [context]
  (->Routes context))
