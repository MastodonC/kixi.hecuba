(ns kixi.hecuba.api.programmes
  (:require
   [clojure.tools.logging :as log]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [cheshire.core :as json]
   [cemerick.friend :as friend]
   [kixi.hecuba.api :refer (decode-body authorized? allowed?) :as api]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.uuid :refer (uuid-str)]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]
   [clojure.core.match :refer (match)]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.data.programmes :as programmes]
   [kixi.hecuba.data.projects :as projects]
   [schema.core :as s]
   [kixi.amon-schema :as schema]))

(def ^:private programmes-index (p/index-path-string :programmes-index))
(def ^:private programme-resource (p/resource-path-string :programme-resource))
(def ^:private programme-projects-index (p/index-path-string :programme-projects-index))

(defmulti enrich-by-role (fn [role resource] role))
(defmethod enrich-by-role :kixi.hecuba.security/user [_ resource]
  (assoc resource :editable false))
(defmethod enrich-by-role :kixi.hecuba.security/programme-manager [_ resource]
  (assoc resource :editable true))

(defn filter-programmes [allowed-programmes allowed-projects programme-ids-for-projects all-programmes]
  (let [allowed-programme-ids      (into #{} (keys allowed-programmes))
        allowed-project-ids        (into #{} (keys allowed-projects))]
    (->> all-programmes
         (map (fn [programme]
                (let [programme-id (:programme_id programme)]
                  (cond
                   (some #{programme-id} allowed-programme-ids) (enrich-by-role (get allowed-programmes programme-id) programme)
                   (some #{programme-id} programme-ids-for-projects) (enrich-by-role :kixi.hecuba.security/user programme)
                   (:public_access programme) (assoc programme :editable false)))))
         (remove nil?))))

(defn allowed?*
  ([allowed-programmes allowed-projects role request-method store]
     (log/infof "allowed?* allowed-programmes: %s allowed-projects: %srole: %s request-method: %s"
                allowed-programmes allowed-projects role request-method)
     (db/with-session [session (:hecuba-session store)]
       (let [all-programmes             (programmes/get-all session)
             programme-ids-for-projects (into #{} (map #(-> (projects/get-by-id session %) :programme_id) (keys allowed-projects)))]
         (match [(has-admin? role)
                 request-method]

                [true _] [true {::items (mapv #(assoc % :editable true :admin true) all-programmes)}]
                [_ :get] [true {::items (filter-programmes allowed-programmes allowed-projects programme-ids-for-projects
                                                           all-programmes)}]
                :else false))))
  ([programme_id allowed-programmes allowed-projects role request-method store]
     (log/infof "allowed?* programme_id: %s allowed-programmes: %s allowed-projects: %s role: %s request-method: %s"
                programme_id allowed-programmes allowed-projects role request-method)
     (db/with-session [session (:hecuba-session store)]
       (let [programme-ids-for-projects (into #{} (map #(-> (projects/get-by-id session %) :programme_id) (keys allowed-projects)))]
         (match [(has-admin? role)
                 (has-programme-manager? programme_id allowed-programmes)
                 (has-user? programme_id allowed-programmes nil nil)
                 request-method]

                [true _ _ _]    [true {::item (assoc (programmes/get-by-id session programme_id) :editable true :admin true)}]
                [_ true _ _]    [true {::item (filter-programmes allowed-programmes allowed-projects programme-ids-for-projects
                                                                 [(programmes/get-by-id session programme_id)])}]
                [_ _ true :get] [true {::item (filter-programmes allowed-programmes allowed-projects programme-ids-for-projects
                                                                 [(programmes/get-by-id session programme_id)])}]
                :else false)))))

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [programmes projects role]} (sec/current-authentication session)]
      (allowed?* programmes projects role request-method store))))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [web-session (-> ctx :request :session)
          items       (::items ctx)]
      (->> items
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
          programme_id (uuid-str)]
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
        {:keys [programmes projects role]} (sec/current-authentication session)
        programme_id (programme_id-from ctx)]
    (if programme_id
      (allowed?* programme_id programmes projects role request-method store)
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
  (::item ctx))

(defn resource-handle-ok [ctx]
  (api/render-item ctx
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
  :allowed? (index-allowed? store)
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
