(ns kixi.hecuba.user
  (:require
   [liberator.core :refer (defresource)]
   [bidi.bidi :refer (->Redirect)]
   [modular.bidi :refer (new-bidi-routes)]
   [kixi.hecuba.webutil :refer (read-edn-body)]
   [kixi.hecuba.security :as sec]
   [com.stuartsierra.component :as component]
   [clojure.tools.logging :as log]
   [kixi.hecuba.storage.db :as db]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.sha1 :as sha1]))

;; (defn get-upsert-properties-from-body [rng body]
;;   (assert (:password body))
;;   (-> body
;;       (merge (create-hash rng (:password body)))
;;       (dissoc :password)))

;; (defresource users-resource [{:keys [store restricted rng] :as m} handlers]
;;   :allowed-methods #{:post}
;;   :authorized? (fn [{request :request}]
;;                  (or (not restricted)
;;                      (authorized-with-basic-auth? request store)))
;;   :known-content-type? #{"application/edn"}
;;   :available-media-types ["text/html"]
;;   :handle-ok "Users resource"
;;   :post! (fn [{{body :body} :request}]
;;            (let [body (read-edn-body body)
;;                  id   (sha1/gen-key :user body)]
;;              (db/with-session [session (:hecuba-session store)]
;;                (db/execute session (hayt/insert :users (hayt/values (assoc (get-upsert-properties-from-body rng body) :id id))))))))

;; ;; Resource for the profile of a specific user
;; (defresource user-profile-resource [store handlers]
;;   :available-media-types ["text/html"]
;;   :handle-ok "User profile resource (for someone)")

;; (defn make-handlers [opts]
;;   (let [p (promise)]
;;     @(deliver p
;;               {:users (users-resource opts p)
;;                :user-profile (user-profile-resource opts p)})))

;; ;; Tight regexes always preferred to defend against injection attacks
;; (def username-regex #"[a-z][a-z0-9]+")

;; (defn make-routes [handlers]
;;   ["/" [
;;        ["users/" (:users handlers)]
;;        ["users" (->Redirect 307 (:users handlers))]
;;        [["users/" [username-regex :username] "/profile"] (:user-profile handlers)]
;;        ]])

;; (defrecord UserApi [context]
;;   component/Lifecycle
;;   (start [this]
;;     (log/info "UserApi starting")
;;     (if-let [store (get-in this [:store])]
;;       (let [handlers (make-handlers (merge {:store store} {:rng (make-rng)}))]
;;         (assoc this :handlers handlers :routes (make-routes handlers)))
;;       (throw (ex-info "No store!" {:this this}))))
;;   (stop [this]
;;     (log/info "CassandraDirectStore stopping")
;;     this)

;;   modular.bidi/BidiRoutesContributor
;;   (routes [this] (:routes this))
;;   (context [this] context))

;; (defn new-user-api
;;   ([] (new-user-api ""))
;;   ([context] (->UserApi context)))
