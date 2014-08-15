(ns kixi.hecuba.api.users
  (:require [clojure.tools.logging :as log]
            [liberator.core :refer (defresource)]
            [liberator.representation :refer (ring-response)]
            [cheshire.core :as json]
            [cemerick.friend :as friend]
            [kixi.hecuba.webutil :refer (decode-body authorized?) :as util]
            [kixi.hecuba.storage.db :as db]
            [kixi.hecuba.data.users :as users]
            [kixi.hecuba.security :refer (has-admin?) :as sec]))

(defn allowed? [ctx]
  (let [request (:request ctx)
        {:keys [request-method session]} request
        {:keys [roles]} (sec/current-authentication session)]
    (log/infof "roles: %s request-method: %s" roles request-method)
    (has-admin? roles)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Index

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (util/render-items ctx (users/get-all session))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource

(defn resource-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request]
    (case request-method
      :put (let [user   (decode-body request)
                 {:keys [data username]} user]
             (if (and (seq data) username)
               [false {:user user :request request}]
               [true {:representation {:media-type (:content-type request)}}]))
       false)))

(defn resource-handle-malformed [ctx]
  {:error "Request must contain username and data."})

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (when-let [item (users/get-by-username session (get-in ctx [:request :route-params :username]))]
      {::item item})))

(defn resource-handle-ok [ctx]
  (util/render-item ctx (::item ctx)))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request user]} ctx]
      (when user
        (users/update session (:username user) user)))))

(defresource index [store]
  :allowed-methods #{:get}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? allowed?
  :handle-ok (partial index-handle-ok store))

(defresource resource [store]
  :allowed-methods #{:get :put}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? allowed?
  :exists? (partial resource-exists? store)
  :malformed? resource-malformed?
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok)
  :handle-malformed resource-handle-malformed)
