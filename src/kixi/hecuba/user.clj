(ns kixi.hecuba.user
  (:require
   [liberator.core :refer (defresource)]
   [bidi.bidi :refer (->Redirect)]
   [modular.bidi :refer (new-bidi-routes)]
   [kixi.hecuba.protocols :refer (upsert!)]
   [kixi.hecuba.webutil :refer (read-edn-body)]
   [kixi.hecuba.security :refer (create-hash authorized-with-basic-auth? make-rng)]))

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

(defn new-api-service [opts]
  (-> (merge (select-keys opts [:commander :querier])
             {:rng (make-rng)})
      make-handlers
      make-routes
      new-bidi-routes))
