(ns kixi.hecuba.api.projects
  (:require
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid stringify-values sha1-regex)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]))

(def ^:private programme-projects-index (p/index-path-string :programme-projects-index))
(def ^:private programme-projects-resource (p/resource-path-string :programme-projects-resource))
(def ^:private projects-index (p/index-path-string :projects-index))
(def ^:private project-resource (p/resource-path-string :project-resource))
(def ^:private project-properties-index (p/index-path-string :project-properties-index))

(defn- programme_id-from [ctx]
  (get-in ctx [:request :route-params :programme_id]))

(defn- project_id-from [ctx]
  (get-in ctx [:request :route-params :project_id]))

(defn index-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          coll    (->> (db/execute session
                                   (if-let [programme_id (programme_id-from ctx)]
                                     (projects/get-all session programme_id)
                                     (projects/get-all session)))
                       (map #(-> %
                                 (assoc :href (format programme-projects-resource (:programme_id %) (:id %))
                                        :properties (format project-properties-index (:id %))))))]
      (util/render-items request coll))))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)
          username  (sec/session-username (-> ctx :request :session))
          ;; FIXME: Why user_id?
          user_id       (-> (users/get-by-username session username) :id)
          project       (-> request decode-body stringify-values)
          project_id    (if-let [id (:id project)] id (sha1/gen-key :project project))]
      (db/execute session
                  (hayt/insert :projects (hayt/values (assoc project :user_id user_id :id project_id))))
      {::project_id project_id})))

;; FIXME: Should return programmes/%s/projects/%s
(defn index-handle-created [ctx]
  (let [request  (:request ctx)
        location (format project-resource (::project_id ctx))]
    (ring-response {:headers {"Location" location}
                    :body (json/encode {:location location
                                        :status "OK"
                                        :version "4"})})))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (when-let [item (-> (db/execute session (hayt/select :projects (hayt/where [[= :id (project_id-from ctx)]]))) first)]
      {::item item})))

(defn resource-handle-ok [ctx]
  (let [request (:request ctx)]
    (util/render-item request
                      (as-> (::item ctx) item
                            (assoc item
                              :properties (format project-properties-index :project_id (:id item)))
                            (dissoc item :user_id)))))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store)
  :handle-ok (partial index-handle-ok store)
  :post! (partial index-post! store)
  :handle-created (partial index-handle-created))

(defresource resource [store]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/json"}
  :authorized? (authorized? store)
  :exists? (partial resource-exists? store)
  :handle-ok (partial resource-handle-ok))
