(ns kixi.hecuba.api.programme
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

(defresource programmes [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :programme)
  :handle-ok
  (fn [{{mime :media-type} :representation {routes :modular.bidi/routes} :request :as req}]
    (let [{:keys [projects programme]} @handlers
          _ (log/info "projects:" projects)
          _ (log/info "programme:" programme)
          _ (log/info "FOO:"  (hecuba/items querier :programme))
          items (->> (hecuba/items querier :programme)
                     (map #(-> %
                              (assoc :projects (bidi/path-for routes projects :programme-id (:id %)))
                              ;; TODO Rename :href to :self
                              (assoc :href (bidi/path-for routes programme :programme-id (:id %)))))
                    )]
      (util/render-items req items)))

  :post!
  (fn [{request :request}]
    (let [body          (-> request :body)
          [username _]  (sec/get-username-password request querier)
          user-id       (-> (hecuba/items querier :user {:username username}) first :id)
          programme     (-> request decode-body stringify-values)]
      {:programme-id (hecuba/upsert! commander :programme (assoc programme :user-id user-id))}))

  :handle-created
  (fn [{id :programme-id {routes :modular.bidi/routes} :request}]
    (let [location (bidi/path-for routes (:programme @handlers) :programme-id id)]
      (ring-response {:headers {"Location" location} :body (json/encode {:location location :status "OK" :version "4"})}))))

(defresource programme [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :programme)

  :exists?
  (fn [{{{programme-id :programme-id} :route-params} :request}]
    (when-let [item (hecuba/item querier :programme programme-id)]
      (prn "Programme id for looking up programme: " programme-id)
      (prn "Programme found: " item)
      {::item item}))

  :handle-ok
  (fn [{item ::item {mime :media-type} :representation {routes :modular.bidi/routes} :request :as req}]
    (let [item (assoc item :projects (bidi/path-for routes (:projects @handlers) :programme-id (:id item)))]
      (util/render-item req item))))
