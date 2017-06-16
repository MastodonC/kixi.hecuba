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
   [kixi.hecuba.time :as time]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clojure.java.io :as io]
   [kixi.hecuba.api :refer (decode-body)]
   [liberator.representation :refer (ring-response)]))

(derive ::user ::public)
(derive ::project-manager ::user)
(derive ::programme-manager ::project-manager)
(derive ::admin ::programme-manager)
(derive ::super-admin ::admin)

(comment

  ;; below are examples for what the data in the users table will look
  ;; like for an admin and a user who has programme-manager on one
  ;; programme, user on a project and project-manager on a different
  ;; project in a different programme.

  ;; an example admin
  {:role :kixi.hecuba.security/admin,
   :programmes {},
   :projects {}}

  ;; an example user
  {:role :kixi.hecuba.security/user,
   :programmes {"bfb6e716f87d4f1a333fd37d5c3679b2b4b6d87f" :kixi.hecuba.security/programme-manager},
   :projects {"01f6a45fc620c7f6347bf1995f8eb96d45f66df3" :kixi.hecuba.security/user
              "032b8c0ae96805375c1c7779ab01639ce3ad6156" :kixi.hecuba.security/project-manager}}
  )

(defn has-super-admin? [role] (isa? role ::super-admin))
(defn has-admin? [role] (isa? role ::admin))

(defn has-programme-manager? [programme_id programmes]
  (when-let [programme-role (get programmes programme_id)]
    (isa? programme-role ::programme-manager)))

(defn has-project-manager? [project_id projects]
  (when-let [project-role (get projects project_id)]
    (isa? project-role ::project-manager)))

(defn has-user?
  ([id permissions]
     (when-let [permission-role (get permissions id)]
       (isa? permission-role ::user)))
  ([programme_id programmes project_id projects]
     (or (has-user? programme_id programmes)
         (has-user? project_id projects))))

(defn add-user!
  ([store name username password role programmes projects]
     (db/with-session [session (:hecuba-session store)]
       (db/execute
        session
        (hayt/update :users
                     (hayt/set-columns
                      {:username username
                       :password (creds/hash-bcrypt password)
                       :data     (pr-str {:name       name
                                          :role       role
                                          :programmes programmes
                                          :projects   projects})})
                     (hayt/where [[= :id username]])))))
  ([store name username password role]
     (add-user! store name username password role {} {})))

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
                     (let [perms (edn/read-string (:data user))]
                       (assoc perms :roles (set (conj (concat (vals (:programmes perms)) (vals (:projects perms))) (:role perms))))))
              nil)))
        (log/warn "Get user called empty username.")))))

(defn session-username [session]
  (-> session :cemerick.friend/identity :current))

(defn session-authentications [session]
  (-> session :cemerick.friend/identity :authentications))

(defn current-authentication [session]
  (if-let [identity-map (:cemerick.friend/identity session)]
    (-> identity-map
        :authentications
        (get (:current identity-map)))
    {:projects {} :programmes {}}))

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
      (do (add-user! store name email password ::user)
          (assoc-in (redirect-after-post "/app")
                    [:session ::friend/identity]
                    {:current email
                     :authentications {email
                                       {:identity email
                                        :name name
                                        :projects {}
                                        :programmes {}
                                        :role ::user
                                        :roles #{::user}
                                        :username email
                                        :id email}}}))
      (redirect-after-post "/registration-error"))))

(defn reset-email-text [username link]
  {:html-content (str
                  "<p> Hello " username "</p>"
                  "<p>Someone has requested a link to change your password, and you can do this through the link below.</p>"
                  "<p>" link "</p>"
                  "<p>If you didn't request this, please ignore this email.</p>"
                  "<p>Your password won't change until you access the link above and create a new one.</p>")
   :text-content (str
                  "Hello " username "\n"
                  "Someone has requested a link to change your password, and you can do this through the link below.\n"
                  link "\n"
                  "If you didn't request this, please ignore this email.\n"
                  "Your password won't change until you access the link above and create a new one.\n")}
  )

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
                              :reset_timestamp (time/now->timestamp)})
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
