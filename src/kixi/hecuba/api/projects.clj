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


(defn- programme-id-from [ctx]
  (get-in ctx [:request :route-params :programme-id]))

(defn- project-id-from [ctx]
  (get-in ctx [:request :route-params :project-id]))

(defn index-handle-ok [querier handlers ctx]
  (let [request (:request ctx)
        coll    (->> (if (programme-id-from ctx)
                       (hecuba/items querier :project [[= :programme-id (-> (:route-params request) :programme-id)]])
                       (hecuba/items querier :project))
                     (map #(-> %
                               (assoc :properties (bidi/path-for (routes-from ctx) (:properties @handlers) :project-id (:id %))
                                      :href (bidi/path-for (routes-from ctx) (:project @handlers) :project-id (:id %))))))]

    (util/render-items request coll)))

(defn index-post! [querier commander ctx]
  (let [request (:request ctx)
        [username _]  (sec/get-username-password request querier)
        user-id       (-> (hecuba/items querier :user [[= :username username]]) first :id)
        project       (-> request decode-body stringify-values)]
      {::project-id (hecuba/upsert! commander
                                   :project (assoc project :user-id user-id))}))

(defn index-handle-created [handlers ctx]
    (let [request  (:request ctx)
          routes   (:modular.bidi/routes request)
          location (bidi/path-for routes (:project @handlers)
                                  :project-id (::project-id ctx))]
      (ring-response {:headers {"Location" location}
                      :body (json/encode {:location location
                                          :status "OK"
                                          :version "4"})})))

(defn resource-exists? [querier ctx]
    (when-let [item (hecuba/item querier :project (project-id-from ctx))]
      {::item item}))

(defn resource-handle-ok [handlers ctx]
    (let [request (:request ctx)]
      (util/render-item request
                        (as-> (::item ctx) item
                              (assoc item
                                :properties (bidi/path-for (routes-from ctx)
                                                           (:properties @handlers) :project-id (:id item)))
                              (dissoc item :user-id)))))

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
