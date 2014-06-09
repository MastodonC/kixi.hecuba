(ns kixi.hecuba.api.programmes
  (:require
   [clojure.tools.logging :as log]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [cheshire.core :as json]
   [cemerick.friend :as friend]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? allowed? uuid stringify-values sha1-regex)]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]))

(def programmes-index (p/index-path-string :programmes-index))
(def programme-resource (p/resource-path-string :programme-resource))
(def programme-projects-index (p/index-path-string :programme-projects-index))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          items   (db/execute session (hayt/select :programmes))]
      (map #(-> %
                (dissoc :user_id)
                (assoc :href (format programme-resource (:id %))
                       :projects (format programme-projects-index (:id %))))
           items))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          username     (sec/session-username (-> ctx :request :session))
          ;; FIXME why user_id?
          user_id      (->  (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
          programme    (-> request decode-body stringify-values)
          programme_id (if-let [id (:id programme)] id (sha1/gen-key :programme programme))]
      (db/execute session (hayt/insert :programmes (hayt/values (assoc programme :user_id user_id :id programme_id))))
      {::programme_id programme_id})))

(defn index-handle-created [ctx]
  (let [request  (:request ctx)
        location (format programme-resource (::programme_id ctx))]
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

(defn resource-handle-ok [ctx]
  (let [request (:request ctx)]
    (util/render-item request
                      (as-> (::item ctx) item
                            (dissoc item :user_id)
                            (assoc item :projects (format programme-projects-index (:id item)))))))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? store)
  :allowed? (allowed? store)
  :handle-ok (partial index-handle-ok store)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created))

(defresource resource [store]
  :allowed-methods #{:get}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? store)
  :allowed? (allowed? store)  
  :exists? (partial resource-exists? store)
  :handle-ok (partial resource-handle-ok))
