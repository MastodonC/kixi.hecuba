(ns kixi.hecuba.web
  (:require
   [modular.bidi :refer (new-bidi-routes)]

   kixi.hecuba.web.messages
   [kixi.hecuba.data :as data]

   [bidi.bidi :refer (match-route path-for ->WrapMiddleware ->Resources ->ResourcesMaybe ->Redirect ->Alternates Matched resolve-handler unresolve-handler)]
   [ring.middleware.params :refer (wrap-params)]
   [ring.middleware.cookies :refer (wrap-cookies)]
   [clojure.java.io :as io]
   [hiccup.core :refer (html)]
   [kixi.hecuba.security :as sec]
   [clojure.tools.logging :as log]
   [liberator.core :refer (resource defresource)]
   [com.stuartsierra.component :as component]))

(defn index [req]
  {:status 200 :body (slurp (io/resource "site/index.html"))})

(defn programmes [req]
  {:status 200 :body (slurp (io/resource "site/programmes.html"))})

(defn charts [req]
  {:status 200 :body (slurp (io/resource "site/charts.html"))})

(defn not-found [req]
  {:status 404 :body (slurp (io/resource "site/not-found.html"))})

(defn parse-int [s]
  (when s
    (try
      (Integer/parseInt s)
      (catch NumberFormatException e 0))))

(defn login-handler [store handlers]
  (fn [{{user "user" password "password" requested-uri "requested-uri"} :form-params
        routes :modular.bidi/routes
        {{attempts :value} "login-attempts"} :cookies}]
    true))

(defn login-form [login-handler]
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
             [:form {:method "POST" :action (path-for routes login-handler)}
              [:input {:type "hidden" :name :requested-uri :value requested-uri}]
              [:p "Username " [:input {:type :text :name :user}]]
              [:p "Password " [:input {:type :password :name :password}]]
              [:p [:input {:type :submit}]]]])}))

(defn make-handlers [store]
  (let [p (promise)
        lh (login-handler store p)]
    @(deliver p
              {:index index
               :not-found not-found
               :login-handler lh
               :login-form (login-form lh)
               :programmes programmes})))

(defn make-routes [handlers store]
  ["/"
   [["" (:index handlers)]
    ["index.html" (:index handlers)]

    ["login.html" (->WrapMiddleware (:login-form handlers) wrap-cookies)]
    ["auth" (->WrapMiddleware (:login-handler handlers) (comp wrap-params wrap-cookies))]

    ["programmes/" (:programmes handlers) store (:login-form handlers)]
    ["programmes" (->Redirect 301 programmes)]

    ["charts/" charts]
    ["charts" (->Redirect 301 charts)]

    ["" (->ResourcesMaybe {:prefix "site/"})]
    [#".*" (:not-found handlers)]
    ]])

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
