(ns kixi.hecuba.user
  (:require
   jig
   [liberator.core :refer (defresource)]
   [bidi.bidi :refer (->Redirect)]
   [kixi.hecuba.protocols :refer (upsert!)]
   [kixi.hecuba.webutil :refer (read-edn-body)]
   [jig.bidi :refer (add-bidi-routes)]
   [kixi.hecuba.security :refer (create-hash authorized-with-basic-auth? make-rng)])
  (:import
   (jig Lifecycle)))

(defn get-upsert-properties-from-body [rng body]
  (assert (:password body))
  (-> body
      (merge (create-hash rng (:password body)))
      (dissoc :password)))

(defresource users-resource [{:keys [commander querier restricted rng] :as m} handlers]
  :allowed-methods #{:post}
  :authorized? (fn [{request :request}]
                 (or (not restricted)
                     (authorized-with-basic-auth? request querier)))
  :known-content-type? #{"application/edn"}
  :available-media-types ["text/html"]
  :handle-ok "Users resource"
  :post! (fn [{{body :body} :request}]
           (let [body (read-edn-body body)]
             (upsert! commander :user (get-upsert-properties-from-body rng body)))))

;; Resource for the profile of a specific user
(defresource user-profile-resource [{:keys [commander querier]} handlers]
  :available-media-types ["text/html"]
  :handle-ok "User profile resource (for someone)")

(defn make-handlers [opts]
  (let [p (promise)]
    @(deliver p
              {:users (users-resource opts p)
               :user-profile (user-profile-resource opts p)})))

;; Tight regexes always preferred to defend against injection attacks
(def username-regex #"[a-z][a-z0-9]+")

(defn make-routes [handlers]
  ["/" [
       ["users/" (:users handlers)]
       ["users" (->Redirect 307 (:users handlers))]
       [["users/" [username-regex :username] "/profile"] (:user-profile handlers)]
       ]])

(deftype ApiService [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    ;; The reason we need to put this into the start (rather than init)
    ;; phase is that commander and querier and sometimes not bound until
    ;; the start phase (they in turn depend on various side-effects,
    ;; such as C* schema creation). Eventually we won't have a different
    ;; init and start phase.
    (let [handlers (make-handlers (merge config
                                         (select-keys system [:commander :querier])
                                         {:rng (make-rng)}))]
      (-> system
          (add-bidi-routes config (make-routes handlers)))))
  (stop [_ system] system)
  )
