(ns kixi.hecuba.web
  (:require
   jig
   kixi.hecuba.web.messages
   [kixi.hecuba.data :as data]
   [jig.util :refer (get-dependencies satisfying-dependency)]
   [jig.bidi :refer (add-bidi-routes)]
   [bidi.bidi :refer (match-route path-for ->WrapMiddleware ->Resources ->ResourcesMaybe ->Redirect ->Alternates Matched resolve-handler unresolve-handler)]
   [ring.middleware.params :refer (wrap-params)]
   [ring.middleware.cookies :refer (wrap-cookies)]
   [clojure.java.io :as io]
   [hiccup.core :refer (html)]
   [kixi.hecuba.security :as sec]
   [clojure.tools.logging :refer :all]
   [liberator.core :refer (resource defresource)])
  (:import (jig Lifecycle)))

(defn index [req]
  {:status 200 :body (slurp (io/resource "sb-admin/index.html"))})

(defn programmes [req]
  {:status 200 :body (slurp (io/resource "sb-admin/programmes.html"))})

(defn chart [req]
  {:status 200 :body (slurp (io/resource "hecuba/chart.html"))})

(defn maps [req]
  {:status 200 :body (slurp (io/resource "hecuba/map.html"))})

(defn counters [req]
  {:status 200 :body (slurp (io/resource "hecuba/counters.html"))})

(defn readings [req]
  {:status 200 :body (slurp (io/resource "reading.html"))})

(defn- ensure-authenticated [h querier login-form]
  (fn [req]
    (if (sec/authorized-with-cookie? req querier)
      (h req)
      {:status 302
       :headers {"Location" (path-for (:jig.bidi/routes req) login-form)}
       :body "Not authorized"
       :cookies {"requested-uri" (:uri req)}}
      )))

(defrecord Secure [routes querier login-form]
  Matched
  (resolve-handler [this m]
    (let [r (resolve-handler routes m)]
      (if (:handler r) (update-in r [:handler] (comp wrap-cookies
                                                     #(ensure-authenticated % querier login-form)))
          r)))
  (unresolve-handler [this m]
    (unresolve-handler routes m)))

(defn parse-int [s]
  (when s
    (try
      (Integer/parseInt s)
      (catch NumberFormatException e 0))))

(defn login-handler [{:keys [querier commander] :as opts} handlers]
  (fn [{{user "user" password "password" requested-uri "requested-uri"} :form-params
        routes :jig.bidi/routes
        {{attempts :value} "login-attempts"} :cookies}]
    (if (and user (not-empty user) (sec/authorized? (.trim user) password querier))
      {:status 302
       :headers {"Location" requested-uri}
       :cookies (sec/create-session-cookie (.trim user) opts)
       :body "Well done, you're coming in!"}
      {:status 302
       ;; TODO Don't like this coupling of :login-form
       :headers {"Location" (path-for routes (:login-form @handlers))}
       :cookies {"login-attempts" {:value ((fnil inc 0) (parse-int attempts))
                                   :max-age 10}}
       :body "You're not allowed!"})))

(defn login-form [login-handler]
  (fn [{{{requested-uri :value} "requested-uri"
         {attempts :value} "login-attempts" :as cookies} :cookies
         routes :jig.bidi/routes :as req}]
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

(defn make-handlers [{:keys [querier commander] :as opts}]
  (let [p (promise)
        lh (login-handler opts p)]
    @(deliver p
              {:index index
               :login-handler lh
               :login-form (login-form lh)
               :programmes programmes})))

(defn make-routes [handlers {:keys [querier commander]}]
  ["/"
   [["" (->Redirect 307 programmes)]
    ["index.html" (:index handlers)]

    ["login.html" (->WrapMiddleware (:login-form handlers) wrap-cookies)]
    ["auth" (->WrapMiddleware (:login-handler handlers) (comp wrap-params wrap-cookies))]

    [#"programmes(?:/.*)" (->Secure (:programmes handlers) querier (:login-form handlers))]

    ["chart.html" chart]
    ["counters.html" counters]
    ["map.html" maps]

    (kixi.hecuba.web.messages/create-routes querier commander)

    ["hecuba-js/react.js" (->Resources {:prefix "sb-admin/"})]
    ["" (->ResourcesMaybe {:prefix "sb-admin/"})]
    [#".*" (fn [req] {:status 404 :body "Not Found (Hecuba)"})] ; need a template!
    ]])

(deftype Website [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [commander (:commander system)
          querier (:querier system)
          routes (make-routes (make-handlers {:querier querier :commander commander})
                              {:querier querier :commander commander})]
      (-> system
          (add-bidi-routes config routes))))

  (stop [_ system] system))
