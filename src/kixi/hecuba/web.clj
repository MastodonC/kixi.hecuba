(ns kixi.hecuba.web
  (:require
   [modular.bidi :refer (new-bidi-routes)]

   kixi.hecuba.web.messages
   [kixi.hecuba.data :as data]

   [bidi.bidi :refer (match-route path-for ->WrapMiddleware ->Resources ->ResourcesMaybe ->Redirect ->Alternates Matched resolve-handler unresolve-handler)]
   [ring.middleware.params :refer (wrap-params)]
   [ring.middleware.cookies :refer (wrap-cookies)]
   [ring.middleware.keyword-params :refer (wrap-keyword-params)]
   [ring.middleware.nested-params :refer (wrap-nested-params)]
   [ring.middleware.session :refer (wrap-session)]
   [ring.middleware.session.cookie :refer (cookie-store)]
   [clojure.java.io :as io]
   [cemerick.friend :as friend]
   [hiccup.core :refer (html)]
   [kixi.hecuba.security :as sec]
   [clojure.tools.logging :as log]
   [liberator.core :refer (resource defresource)]
   [com.stuartsierra.component :as component]))

(defn index [req]
  (log/infof "Index Session: %s" (:session req))
  {:status 200
   :session (assoc (:session req) :index-hit "Y")
   :body (slurp (io/resource "site/index.html"))})

(defn programmes [req]
  {:status 200 :body (slurp (io/resource "site/programmes.html"))})

(defn charts [req]
  {:status 200 :body (slurp (io/resource "site/charts.html"))})

(defn not-found [req]
  {:status 404 :body (slurp (io/resource "site/not-found.html"))})

(defn logout [req]
  {:status 302
   :headers {"Location" "/"}
   :body "Logging out."})

(defn parse-int [s]
  (when s
    (try
      (Integer/parseInt s)
      (catch NumberFormatException e 0))))

(defn login-form []
  (fn [{{{requested-uri :value} "requested-uri"
         {attempts :value} "login-attempts" :as cookies} :cookies
         routes :modular.bidi/routes :as req}]
    {:status 200
     :body (html
            [:body
             [:h1 "Login"]
             (when attempts
               (condp #(%1 %2) (parse-int attempts)
                 odd? [:p "No, that's not right"]
                 even? [:p "Still wrong!"]))
             [:form {:method "POST" :action "/login"}
              [:input {:type "hidden" :name :requested-uri :value requested-uri}]
              [:p "Username " [:input {:type :text :name :username}]]
              [:p "Password " [:input {:type :password :name :password}]]
              [:p [:input {:type :submit}]]]])}))



(defn make-handlers [store]
  (let [p (promise)]
    @(deliver p
              {:index index
               :not-found not-found
               :logout logout
               :login-form (login-form)
               :programmes programmes})))

(defn make-routes [handlers store]
  ["/" (->WrapMiddleware
        [["" (:index handlers)]
         ["index.html" (:index handlers)]

         ["login" (:login-form handlers)]
         ["logout" (->WrapMiddleware (:logout handlers) friend/logout)]

         ["programmes/" (:programmes handlers)]
         ["programmes" (->Redirect 301 programmes)]

         ["charts/" charts]
         ["charts" (->Redirect 301 charts)]

         ["" (->ResourcesMaybe {:prefix "site/"})]
         [#".*" (:not-found handlers)]
         
         ]
        (fn [routes]
          (-> routes
              sec/friend-middleware
              wrap-session
              wrap-cookies
              wrap-keyword-params
              wrap-nested-params
              wrap-params)))])

(defrecord MainRoutes [context]
  component/Lifecycle
  (start [this]
    (log/info "MainRoutes starting")
    (if-let [store (get-in this [:store])]
      (assoc this :routes (make-routes (make-handlers store) store))
      (throw (ex-info "No store!" {:this this}))))
  (stop [this]
    (log/info "MainRoutes starting")
    this)

  modular.bidi/BidiRoutesContributor
  (routes [this] (:routes this))
  (context [this] context))

(defn new-main-routes []
  (->MainRoutes ""))
