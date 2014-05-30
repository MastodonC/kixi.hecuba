(ns kixi.hecuba.api.projects
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex routes-from)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.dbnew :as db]
   [kixi.hecuba.storage.sha1 :as sha1]))


(defn- programme_id-from [ctx]
  (get-in ctx [:request :route-params :programme_id]))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-handle-ok [store handlers ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          coll    (->> (db/execute session
                                   (if (programme_id-from ctx)
                                     (hayt/select :projects (hayt/where [[= :programme_id (-> (:route-params request) :programme_id)]]))
                                     (hayt/select :projects)))
                       (map #(-> %
                                 (assoc :properties (bidi/path-for (routes-from ctx) (:properties @handlers) :project_id (:id %))
                                        :href (bidi/path-for (routes-from ctx) (:project @handlers) :project_id (:id %))))))]

      (util/render-items request coll))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          [username _]  (sec/get-username-password request store)
          user_id       (-> (db/execute session (hayt/select :users (hayt/where [[= :username username]]))) first :id)
          project       (-> request decode-body stringify-values)
          project_id    (if-let [id (:id project)] id (sha1/gen-key :project project))]
      (db/execute session
                  (hayt/insert :projects (hayt/values (assoc project :user_id user_id :id project_id))))
      {::project_id project_id})))

(defn index-handle-created [handlers ctx]
    (let [request  (:request ctx)
          routes   (:modular.bidi/routes request)
          location (bidi/path-for routes (:project @handlers)
                                  :project_id (::project_id ctx))]
      (ring-response {:headers {"Location" location}
                      :body (json/encode {:location location
                                          :status "OK"
                                          :version "4"})})))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (when-let [item (-> (db/execute session (hayt/select :projects (hayt/where [[= :id (project_id-from ctx)]]))) first)]
      {::item item})))

(defn resource-handle-ok [handlers ctx]
    (let [request (:request ctx)]
      (util/render-item request
                        (as-> (::item ctx) item
                              (assoc item
                                :properties (bidi/path-for (routes-from ctx)
                                                           (:properties @handlers) :project_id (:id item)))
                              (dissoc item :user_id)))))

(defresource index [store handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store :project)
  :handle-ok (partial index-handle-ok store handlers)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created handlers))

(defresource resource [store handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store :project)
  :exists? (partial resource-exists? store)
  :handle-ok (partial resource-handle-ok handlers))
