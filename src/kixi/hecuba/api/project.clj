(ns kixi.hecuba.api.project
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defresource projects [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :project)

  :handle-ok
  (fn [{{mime :media-type} :representation {routes :modular.bidi/routes route-params :route-params} :request :as req}]
    (let [coll (->> (if (:programme-id route-params)
                       (hecuba/items querier :project route-params)
                       (hecuba/items querier :project))
                    (map #(-> %
                              (assoc :properties (bidi/path-for routes (:properties @handlers) :project-id (:id %)))
                              (assoc :href (bidi/path-for routes (:project @handlers) :project-id (:id %))))))]

      (util/render-items req coll)))

  :post!
  (fn [{request :request}]
    (let [body          (-> request :body)
          [username _]  (sec/get-username-password request querier)
          user-id       (-> (hecuba/items querier :user {:username username}) first :id)
          project       (-> request decode-body stringify-values)]
      {:project-id (hecuba/upsert! commander :project (assoc project :user-id user-id))}))

  :handle-created
  (fn [{id :project-id {routes :modular.bidi/routes} :request}]
    (let [location (bidi/path-for routes (:project @handlers) :project-id id)]
      (ring-response {:headers {"Location" location} :body (json/encode {:location location :status "OK" :version "4"})}))))

(defresource project [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :project)

  :exists?
  (fn [{{{project-id :project-id} :route-params} :request}]
    (when-let [item (hecuba/item querier :project project-id)]
      {::item item}))

  :handle-ok
  (fn [{item ::item {mime :media-type} :representation {routes :modular.bidi/routes} :request :as req}]
    (let [item (assoc item :properties (bidi/path-for routes (:properties @handlers) :project-id (:id item)))]
      (util/render-item req item))))
