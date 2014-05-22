(ns kixi.hecuba.api.programmes
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

(defn index-handle-ok [querier handlers ctx]
  (let [request (:request ctx)
        routes (:modular.bidi/routes request)
        {:keys [projects programme]} @handlers
        items (->> (hecuba/items querier :programme)
                   (map #(-> %
                             (dissoc :user_id)
                             (assoc :projects (bidi/path-for routes projects :programme_id (:id %))
                                    :href (bidi/path-for routes programme :programme_id (:id %))))))]
    (util/render-items request items)))


(defn index-post! [querier commander ctx]
  (let [request (:request ctx)
        [username _]  (sec/get-username-password request querier)
        user_id       (-> (hecuba/items querier :user [[= :username username]]) first :id)
        programme     (-> request decode-body stringify-values)]
    {::programme_id (hecuba/upsert! commander
                                   :programme (assoc programme :user_id user_id))}))

(defn index-handle-created [handlers ctx]
    (let [request (:request ctx)
          routes (:modular.bidi/routes request)
          location (bidi/path-for routes (:programme @handlers)
                                  :programme_id (::programme_id ctx))]
      (ring-response {:headers {"Location" location}
                      :body (json/encode {:location location
                                          :status "OK" :version "4"})})))


(defn resource-exists? [querier ctx]
  (when-let [item (hecuba/item querier
                               :programme (get-in ctx [:request :route-params :programme_id]))]
    {::item item}))

(defn resource-handle-ok [handlers ctx]
  (let [request (:request ctx)]
      (util/render-item request
                        (as-> (::item ctx) item
                              (dissoc item :user_id)
                              (assoc item
                                :projects (bidi/path-for (routes-from ctx) (:projects @handlers)
                                                         :programme_id (:id item)))
                              (dissoc item :user_id)))))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :programme)
  :handle-ok (partial index-handle-ok querier handlers)
  :post! (partial index-post! querier commander)
  :handle-created (partial index-handle-created handlers))

(defresource resource [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :programme)
  :exists? (partial resource-exists? querier)
  :handle-ok (partial resource-handle-ok handlers)
)
