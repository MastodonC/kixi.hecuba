(ns kixi.hecuba.api.programmes
  (:require
   [clojure.tools.logging :as log]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [cheshire.core :as json]
   [cemerick.friend :as friend]
   [kixi.hecuba.webutil :refer (decode-body authorized? allowed? uuid stringify-values sha1-regex) :as util]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]
   [clojure.core.match :refer (match)]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.data.programmes :as programmes]
   [schema.core :as s]
   [kixi.amon-schema :as schema]))

(def ^:private programmes-index (p/index-path-string :programmes-index))
(def ^:private programme-resource (p/resource-path-string :programme-resource))
(def ^:private programme-projects-index (p/index-path-string :programme-projects-index))

(defn allowed?* [programme-id allowed-programmes roles request-method]
  (log/infof "allowed?* programme-id: %s allowed-programmes: %s roles: %s request-method: %s" programme-id allowed-programmes roles request-method)
  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (some #(= % programme-id) allowed-programmes)
          (has-user? roles)
          request-method]

         [true _ _ _ _] true
         [_ true true _ _] true
         [_ _ true true :get] true
         :else false))

(defn editable-programmes [programmes allowed-programmes roles]
  (map #(let [editable (allowed?* (:programme_id %) allowed-programmes roles :put)]
          (if editable
            (assoc % :editable editable)
            %)) programmes))

(defn items->authz-items [session items]
  (let [{:keys [roles programmes]} (sec/current-authentication session)]
    (log/debugf "Roles: %s Programmes: %s" roles programmes)
    (if (some #(isa? % ::sec/admin) roles)
      (map #(assoc % :editable true :admin true) items)
      (editable-programmes (filter #(or
                                     (= (:public_access %) "true")
                                     (programmes (:programme_id %))) items) programmes roles))))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [web-session (-> ctx :request :session)
          items       (programmes/get-all session)]
      (->> items
           (items->authz-items web-session)
           (map #(-> %
                     (dissoc :user_id)
                     (assoc :href (format programme-resource (:programme_id %))
                            :projects (format programme-projects-index (:programme_id %)))))))))

(defn index-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request]
    (case request-method
      :post (let [programme  (decode-body request)]
              (if (s/check schema/BaseProgramme programme)
                true
                [false {:programme programme}]))
       false)))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request      (:request ctx)
          username     (sec/session-username (-> ctx :request :session))
          user_id      (:id (users/get-by-username session username))
          programme    (:programme ctx)
          programme_id (sha1/gen-key :programme programme)]
      (programmes/insert session (assoc programme :user_id user_id :programme_id programme_id))
      {::programme_id programme_id})))

(defn index-handle-created [ctx]
  (let [request  (:request ctx)
        location (format programme-resource (::programme_id ctx))]
    (ring-response {:headers {"Location" location}
                    :body (json/encode {:location location
                                        :status "OK" :version "4"})})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource

(defn- programme_id-from [ctx]
  (get-in ctx [:request :route-params :programme_id]))

(defn resource-allowed? [store ctx]
  (let [{:keys [body request-method session params]} (:request ctx)
        {:keys [programmes roles]} (sec/current-authentication session)
        programme_id (programme_id-from ctx)]
    (if programme_id
      [(allowed?* programme_id programmes roles request-method)
       {:editable (allowed?* programme_id programmes roles :put)}]
      true)))

(defn resource-malformed? [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request]
    (case request-method
      :post (let [programme  (decode-body request)
                  {:keys [name]} programme]
              (if name
                [false {:programme programme}]
                true))
      :put (let [programme    (decode-body request)
                 programme_id (:programme_id route-params)]
             (if programme
               [false {:programme (assoc programme :programme_id programme_id)}]
               true))
       false)))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (when-let [item (programmes/get-by-id session (get-in ctx [:request :route-params :programme_id]))]
      {::item item})))

(defn resource-handle-ok [ctx]
  (util/render-item ctx
                    (as-> (::item ctx) item
                          (assoc item :projects (format programme-projects-index (:programme_id item))))))



(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request programme]} ctx
          username  (sec/session-username (-> ctx :request :session))]
      (when programme
        (programmes/update session (:programme_id programme) (assoc programme :user_id username))))))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (allowed? store)
  :malformed? index-malformed?
  :handle-ok (partial index-handle-ok store)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created))

(defresource resource [store]
  :allowed-methods #{:get :put}
  :available-media-types ["application/json" "application/edn"]
  :known-content-type? #{"application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (partial resource-allowed? store)
  :exists? (partial resource-exists? store)
  :malformed? resource-malformed?
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok))
