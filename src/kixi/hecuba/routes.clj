(ns kixi.hecuba.routes
  (:require
   [clojure.java.io :as io]
   [clojure.tools.logging :as log]
   [ring.util.response :refer (redirect)]
   [compojure.core :refer (defroutes ANY GET POST)]
   [compojure.handler :as handler]
   [compojure.route :as route]
   [cemerick.friend :as friend]
   [com.stuartsierra.component :as component]
   [org.httpkit.server :refer (run-server)]
   [kixi.hecuba.web-paths :refer (compojure-route)]
   [kixi.hecuba.security :as sec]))

(defn index-page [req]
  (log/infof "Index Session: %s" (:session req))
  {:status 200
   :session (assoc (:session req) :index-hit "Y")
   :body (slurp (io/resource "site/index.html"))})

(defn not-found-page [req]
  (log/infof "404 Request: %s" (:uri req))
  {:status 404 :body (slurp (io/resource "site/not-found.html"))})

(defn login-form [req]
  {:status 200 :body (slurp (io/resource "site/login.html"))})

(defn app-page [req]
  (log/infof "App Session: %s" (:session req))
  {:status 200 :body (slurp (io/resource "site/app.html"))})

(defroutes web-routes
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

  ;; 404
  (route/not-found not-found-page))

(def app
  (-> web-routes
      sec/friend-middleware
      handler/site))

(defrecord Routes []
  component/Lifecycle
  (start [this]
    (let [server (run-server app {:port 8010})]
      (assoc this ::server server)))
  (stop [this]
    ((::server this))))

(defn new-web-app []
  (->Routes))
