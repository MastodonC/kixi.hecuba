(ns kixi.hecuba.api.programmes
    (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? allowed? uuid stringify-values sha1-regex routes-from)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]))

(defn index-handle-ok [store handlers ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          ;; routes  (:modular.bidi/routes request)
          ;; {:keys  [projects programme]} @handlers
          items   (db/execute session (hayt/select :programmes))]
      (log/infof "index-handle-ok ctx: %s" ctx)
      items
      ;; (util/render-items request (map #(-> %
      ;;                                      (dissoc :user_id)
      ;;                                      (assoc :projects (bidi/path-for routes projects :programme_id (:id %))
      ;;                                             :href (bidi/path-for routes programme :programme_id (:id %)))) items))
      )))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          username     (sec/get-username ctx)
          ;; FIXME why user_id?
          user_id      (->  (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
          programme    (-> request decode-body stringify-values)
          programme_id (if-let [id (:id programme)] id (sha1/gen-key :programme programme))]
      (db/execute session (hayt/insert :programmes (hayt/values (assoc programme :user_id user_id :id programme_id))))
      {::programme_id programme_id})))

(defn index-handle-created [handlers ctx]
    (let [request (:request ctx)
          routes (:modular.bidi/routes request)
          location (bidi/path-for routes (:programme @handlers)
                                  :programme_id (::programme_id ctx))]
      (ring-response {:headers {"Location" location}
                      :body (json/encode {:location location
                                          :status "OK" :version "4"})})))


(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (when-let [item (-> (db/execute session 
                                    (hayt/select :programmes 
                                                 (hayt/where [[= :id (get-in ctx [:request :route-params :programme_id])]])))
                        first)]
      {::item item})))

(defn resource-handle-ok [handlers ctx]
  (let [request (:request ctx)]
      (util/render-item request
                        (as-> (::item ctx) item
                              (dissoc item :user_id)
                              (assoc item
                                :projects (bidi/path-for (routes-from ctx) (:projects @handlers)
                                                         :programme_id (:id item)))
                              (dissoc item :user_id)))))

(defresource index [store handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? store :programme)
  :allowed? (allowed? store :programme)
  :handle-ok (partial index-handle-ok store handlers)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created handlers))

(defresource resource [store handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? store :programme)
  :allowed? (allowed? store :programme)  
  :exists? (partial resource-exists? store)
  :handle-ok (partial resource-handle-ok handlers))
