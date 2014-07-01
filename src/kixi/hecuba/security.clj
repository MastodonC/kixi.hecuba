 (ns kixi.hecuba.security
  (:require
   [clojure.tools.logging :as log]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [cemerick.friend :as friend]
   [clojure.edn :as edn]
   (cemerick.friend [workflows :as workflows]
                    [credentials :as creds])))

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
  ([store username password roles programmes projects]
     (db/with-session [session (:hecuba-session store)]
       (db/execute
        session
        (hayt/update :users
                     (hayt/set-columns
                      {:username username
                       :password (creds/hash-bcrypt password)
                       :data     (pr-str {:roles      (set roles)
                                          :programmes (set programmes)
                                          :projects   (set projects)})})
                     (hayt/where [[= :id username]])))))
  ([store username password roles]
     (add-user! store username password roles #{} #{})))
;; (kixi.hecuba.security/add-user! (:store system) "support@example.com" "<password>" #{:kixi.hecuba.security/super-admin})

(defn get-user [store]
  (fn [username]
    (log/debugf "Getting user: %s" username)
    (db/with-session [session (:hecuba-session store)]
      (let [user (first
                  (db/execute
                   session
                   (hayt/select :users
                                (hayt/where [[= :username username]]))))]
        (log/debugf "Got user: %s" user)
        (if user
          (merge {:username (:username user)
                  :id       (:id user)
                  :password (:password user)}
                 (edn/read-string (:data user)))
          nil)))))

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
