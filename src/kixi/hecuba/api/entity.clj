(ns kixi.hecuba.api.entity
  (:require
   [bidi.bidi :as bidi]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defresource entities [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :entity)

  :post!
  (fn [{request :request}]
    (let [body          (-> request :body)
          entity        (-> request decode-body stringify-values)
          project-id    (-> entity :project-id)
          property-code (-> entity :property-code)
          [username _]  (sec/get-username-password request querier)
          user-id       (-> (hecuba/items querier :user {:username username}) first :id)]
      (when (and project-id property-code)
        (when-not (empty? (first (hecuba/items querier :project {:id project-id})))
          {:entity-id (hecuba/upsert! commander :entity (-> entity
                                                    (assoc :user-id user-id)
                                                    (dissoc :device-ids)))}))))

  :handle-created
  (fn [{{routes :modular.bidi/routes} :request id :entity-id}]
    (if-not (empty? id)
      (let [location (bidi/path-for routes (:entity @handlers) :entity-id id)]
        (when-not location
          (throw (ex-info "No path resolved for Location header"
                          {:entity-id id})))
        (ring-response {:headers {"Location" location} :body (json/encode {:location location :status "OK" :version "4"})}))
      (ring-response {:status 422 :body "Provide valid projectId and propertyCode."}))))

(defresource entity [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"application/json"}
  :authorized? (authorized? querier :entity)

  :exists?
  (fn [{{{id :entity-id} :route-params} :request}]
    (when-let [item (hecuba/item querier :entity id)]
      {::item item}))

  :handle-ok
  (fn [{item ::item {mime :media-type} :representation {routes :modular.bidi/routes route-params :route-params} :request :as req}]
    (let [item (assoc item :device-ids (map :id (hecuba/items querier :device route-params)))]
      (util/render-item req item)))

  :put!
  (fn [{request :request}]
    (let [entity        (-> request decode-body stringify-values)
          entity-id     (-> entity :entity-id)
          [username _]  (sec/get-username-password request querier)
          user-id       (-> (hecuba/items querier :user {:username username}) first :id)]
      (hecuba/upsert! commander :entity (-> entity
                                     (assoc :user-id user-id)
                                     (assoc :id entity-id)
                                     (dissoc :device-ids)))))

  :respond-with-entity? (constantly true)

  :delete! (fn [{{id :id :as i} ::item}]
             (hecuba/delete! commander :entity id)))
