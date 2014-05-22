(ns kixi.hecuba.api.projects
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex routes-from)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))


(defn- programme_id-from [ctx]
  (get-in ctx [:request :route-params :programme_id]))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-handle-ok [querier handlers ctx]
  (let [request (:request ctx)
        coll    (->> (if (programme_id-from ctx)
                       (hecuba/items querier :project [[= :programme_id (-> (:route-params request) :programme_id)]])
                       (hecuba/items querier :project))
                     (map #(-> %
                               (assoc :properties (bidi/path-for (routes-from ctx) (:properties @handlers) :project_id (:id %))
                                      :href (bidi/path-for (routes-from ctx) (:project @handlers) :project_id (:id %))))))]

    (util/render-items request coll)))

(defn index-post! [querier commander ctx]
  (let [request (:request ctx)
        [username _]  (sec/get-username-password request querier)
        user_id       (-> (hecuba/items querier :user [[= :username username]]) first :id)
        project       (-> request decode-body stringify-values)]
      {::project_id (hecuba/upsert! commander
                                   :project (assoc project :user_id user_id))}))

(defn index-handle-created [handlers ctx]
    (let [request  (:request ctx)
          routes   (:modular.bidi/routes request)
          location (bidi/path-for routes (:project @handlers)
                                  :project_id (::project_id ctx))]
      (ring-response {:headers {"Location" location}
                      :body (json/encode {:location location
                                          :status "OK"
                                          :version "4"})})))

(defn resource-exists? [querier ctx]
    (when-let [item (hecuba/item querier :project (project_id-from ctx))]
      {::item item}))

(defn resource-handle-ok [handlers ctx]
    (let [request (:request ctx)]
      (util/render-item request
                        (as-> (::item ctx) item
                              (assoc item
                                :properties (bidi/path-for (routes-from ctx)
                                                           (:properties @handlers) :project_id (:id item)))
                              (dissoc item :user_id)))))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :project)
  :handle-ok (partial index-handle-ok querier handlers)
  :post! (partial index-post! querier commander)
  :handle-created (partial index-handle-created handlers))

(defresource resource [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :project)
  :exists? (partial resource-exists? querier)
  :handle-ok (partial resource-handle-ok handlers))
