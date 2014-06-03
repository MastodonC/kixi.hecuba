(ns kixi.hecuba.security
  (:require
   [clojure.tools.logging :as log]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [cemerick.friend :as friend]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])))

(defn get-user [user-id]
  (log/infof "Getting user: %s" user-id)
  {:username "alice"
   :password "$2a$10$J9CJ8xfy1SrJxj0XwT5Eq.cKCKXAqR.4Cb81ikr6ilvNpszdWEVee"
   })

(defn friend-middleware
  "Returns a middleware that enables authentication via Friend."
  [handler]
  (let [friend-m {:credential-fn (partial creds/bcrypt-credential-fn get-user)
                  :workflows
                  ;; Note that ordering matters here. Basic first.
                  [(workflows/http-basic :realm "/")
                   ;; The tutorial doesn't use this one, but you
                   ;; probably will.
                   (workflows/interactive-form :login-uri "/login")]}]
    (-> handler
        (friend/authenticate friend-m))))

;; TODO: Get it from the key where authorized? puts it
(defn get-username
  "Get the username from the context"
  [ctx]
  nil)

(defn authorized? [username password store]
  true)

