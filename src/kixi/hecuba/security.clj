 (ns kixi.hecuba.security
  (:require
   [clojure.tools.logging :as log]
   [ring.util.response :refer (redirect-after-post)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [cemerick.friend :as friend]
   [clojure.edn :as edn]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.email :as email]
   [kixi.hecuba.webutil :as util]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clojure.java.io :as io]
   [kixi.hecuba.webutil :refer (decode-body)]
   [liberator.representation :refer (ring-response)]))

(derive ::user ::public)
(derive ::project-manager ::user)
(derive ::programme-manager ::project-manager)
(derive ::admin ::programme-manager)
(derive ::super-admin ::admin)

(defn has-super-admin? [roles] (some #(isa? % ::super-admin) roles))
(defn has-admin? [roles] (some #(isa? % ::admin) roles))
(defn has-programme-manager? [roles] (some #(isa? % ::project-manager) roles))
(defn has-project-manager? [roles] (some #(isa? % ::project-manager) roles))
(defn has-user? [roles] (some #(isa? % ::user) roles))

(defn add-user!
  ([store name username password roles programmes projects]
     (db/with-session [session (:hecuba-session store)]
       (db/execute
        session
        (hayt/update :users
                     (hayt/set-columns
                      {:username username
                       :password (creds/hash-bcrypt password)
                       :data     (pr-str {:name       name
                                          :roles      (set roles)
                                          :programmes (set programmes)
                                          :projects   (set projects)})})
                     (hayt/where [[= :id username]])))))
  ([store name username password roles]
     (add-user! store name username password roles #{} #{})))
;; (kixi.hecuba.security/add-user! (:store system) "support@example.com" "<password>" #{:kixi.hecuba.security/super-admin})

;; FIXME We should be using k.h.d.users for queries now
(defn get-user [store]
  (let [user-cql (db/prepare-statement (:hecuba-session store) "select * from users where id = ?;")]
    (fn [username]
      (if (seq username)
        (db/with-session [session (:hecuba-session store)]
          (log/debugf "Getting user: [%s]" username)
          (let [user (first
                      (db/execute-prepared
                       session
                       user-cql
                       {:values [username]}))]
            (log/debugf "Got user: [%s]" user)
            (if user
              (merge {:username (:username user)
                      :id       (:id user)
                      :password (:password user)}
                     (edn/read-string (:data user)))
              nil)))
        (log/warn "Get user called empty username.")))))

(defn update-user-data! [store username user-data]
  (db/with-session [session (:hecuba-session store)]
    (db/execute
     session
     (hayt/update :users
                  (hayt/set-columns
                   {:data (pr-str user-data)})
                  (hayt/where [[= :id username]])))))

(defn add-programme! [store username programme_id]
  (let [{:keys [projects programmes roles]} ((get-user store) username)]
    (update-user-data! store username {:roles      roles
                                       :programmes (conj programmes programme_id)
                                       :projects   projects})))

(defn remove-programme! [store username programme_id]
  (let [{:keys [projects programmes roles]} ((get-user store) username)]
    (update-user-data! store username {:roles      roles
                                       :programmes (set (remove #(= programme_id %) programmes))
                                       :projects   projects})))

(defn add-project! [store username project_id]
  (let [{:keys [projects programmes roles]} ((get-user store) username)]
    (update-user-data! store username {:roles      roles
                                       :programmes programmes
                                       :projects   (conj projects project_id)})))

(defn remove-project! [store username project_id]
  (let [{:keys [projects programmes roles]} ((get-user store) username)]
    (update-user-data! store username {:roles      roles
                                       :programmes programmes
                                       :projects   (set (remove #(= project_id %) projects))})))

(defn friend-middleware
  "Returns a middleware that enables authentication via Friend."
  [handler store]
  (let [friend-m {:credential-fn (partial creds/bcrypt-credential-fn (get-user store))
                  :default-landing-uri "/app"
                  :workflows
                  ;; Note that ordering matters here. Basic first.
                  [(workflows/http-basic :realm "/")
                   (workflows/interactive-form :login-uri "/login")]}]
    (-> handler
        (friend/authenticate friend-m))))

(defn session-username [session]
  (-> session :cemerick.friend/identity :current))

(defn session-authentications [session]
  (-> session :cemerick.friend/identity :authentications))

(defn current-authentication [session]
  (if-let [identity-map (:cemerick.friend/identity session)]
    (-> identity-map
        :authentications
        (get (:current identity-map)))
    {:projects #{} :programmes #{} :roles #{}}))

(defn not-registered? [username store]
  (db/with-session [session (:hecuba-session store)]
    (empty? (db/execute
             session
             (hayt/select :users (hayt/where [[= :id username]]))))))

(defn register-user [request store]
  (let [params (:params request)
        {:keys [name email password confirm_password]} params]
    (log/infof "Attempting to register name: %s email: %s passwords equal? %s" name email (= password confirm_password))
    (if (and (= password confirm_password)
             (every? identity [name email password confirm_password])
             (not-registered? email store))
      (do (add-user! store name email password #{::user} #{} #{})
          (assoc-in (redirect-after-post "/app")
                    [:session ::friend/identity]
                    {:current email
                     :authentications {email
                                       {:identity email
                                        :name name
                                        :projects #{}
                                        :programmes #{}
                                        :roles #{::user}
                                        :username email
                                        :id email}}}))
      (redirect-after-post "/registration-error"))))

(defn reset-email-text [username link]
  (str
   "<p> Hello " username "</p>"
   "<p>Someone has requested a link to change your password, and you can do this through the link below.</p>"
   "<p>" link "</p>"
   "<p>If you didn't request this, please ignore this email.</p>"
   "<p>Your password won't change until you access the link above and create a new one.</p>"))

(defn reset-password-email [request store]
  (let [uuid (java.util.UUID/randomUUID)
        {:keys [form-params]} request
        username (get form-params "username")
        e-mail-session (-> store :e-mail :opts)
        link           (str (:hostname e-mail-session)
                            "/reset/" uuid)]
    (when (users/get-by-username (:hecuba-session store) username)
      (users/update (:hecuba-session store)
                    username {:reset_uuid uuid
                              :reset_timestamp (util/now->timestamp)})
      (email/send-email e-mail-session username "Password Reset" (reset-email-text username link))
      {:status 200 :body (slurp (io/resource "site/reset_ack.html"))})))

(defn valid?
  "Checks if uuid has been generated within last 24 hours."
  [item]
  (let [{:keys [reset_timestamp]} item
        now (t/now)]
    (t/within? (t/interval (t/minus now (t/days 1)) now) (tc/from-date reset_timestamp))))

(defn reset-password [uuid store]
  (let [item (users/get-by-uuid (:hecuba-session store) uuid)]
    (if (and item (valid? item))
      {:status 200 :body (slurp (io/resource "site/password_change.html"))}
      (redirect-after-post "/reset-error"))))

(defn post-new-password [request store]
  (let [{:keys [route-params]} request
        uuid     (:uuid route-params)
        password (:password (decode-body request))
        item     (users/get-by-uuid (:hecuba-session store) uuid)]
    (if (and item (valid? item))
      (do (users/update (:hecuba-session store)
                        (:username item)
                        {:password password
                         :reset_uuid nil
                         :reset_timestamp nil})
          {:status 200 :body "Password has been changed successfully."})
      {:status 400 :body "Password reset link has expired."})))
