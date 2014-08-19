(ns kixi.hecuba.api.entities
  (:require
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   [clojure.string :as string]
   [clojure.core.match :refer (match)]
   [clojure.tools.logging :as log]
   [cheshire.core :as json]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [clojurewerkz.elastisch.native.response :as esr]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.webutil :refer (decode-body authorized? content-type-from-context) :as util]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.data.programmes :as programmes]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.entities.search :as search]
   [kixi.hecuba.data.profiles :as profiles]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.api.parser :as parser]
   [kixi.hecuba.api.entities.schema :as es]))

(def ^:private entities-index-path (p/index-path-string :entities-index))
(def ^:private entity-resource-path (p/resource-path-string :entity-resource))

;; curl -v -H "Content-Type: application/json" -H 'Accept: application/edn' -X GET -u support-test@mastodonc.com:password 127.0.0.1:8010/4/entities/?q=TSB119

;; from https://github.com/weavejester/medley/blob/master/src/medley/core.cljx Thx @weavejester
(defn dissoc-in
  "Dissociate a value in a nested assocative structure, identified by a sequence
of keys. Any collections left empty by the operation will be dissociated from
their containing structures."
  [m ks]
  (if-let [[k & ks] (seq ks)]
    (if (seq ks)
      (let [v (dissoc-in (get m k) ks)]
        (if (empty? v)
          (dissoc m k)
          (assoc m k v)))
      (dissoc m k))
    m))

(defn remove-private-data [entity]
  (if-not (:editable entity)
      (-> entity
          (dissoc-in [:property_data :address_street])
          (dissoc-in [:property_data :address_street_two])
          (dissoc-in [:property_data :address_city])
          (dissoc-in [:property_data :address_code])
          (dissoc-in [:property_data :fuel_poverty])
          (dissoc :documents)
          (dissoc :notes))
      entity))

(defn editable? [programme_id project_id allowed-programmes allowed-projects roles]
  (log/infof "editable? programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s"
             programme_id project_id allowed-programmes allowed-projects roles)
  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (some #(= % programme_id) allowed-programmes)
          (has-project-manager? roles)
          (some #(= % project_id) allowed-projects)
          (has-user? roles)]

         [true _ _ _ _ _] true
         [_ true true _ _ _] true
         [_ _ _ true true _] true
         :else false))

(defn clean-entity
  "Get rid of the keys that we don't want the user to see."
  [entity]
  (-> entity
      remove-private-data
      (dissoc :user_id)))

(defn parse-entities [results allowed-programmes allowed-projects roles]
  (->> results
       esr/hits-from
       (map #(update-in % [:_source :full_entity] assoc :editable (editable? (:programme_id %) (:project_id %) allowed-programmes allowed-projects roles)))
       (map #(-> % :_source :full_entity))
       (map #(clean-entity %))))

;; TODO should accept either a set of ids or a single id
(defn search-filter [k ids]
  (let [should (mapv #(hash-map :term {k %}) ids)]
    {:bool {:must {}
            :should (conj should {:term {:public_access "true"}})
            :must_not {}}}))

(defmulti filter-entities (fn [programmes projects params store k roles & project_id] k))

(defmethod filter-entities :programme_id [programmes _ params store k roles & project_id]
  (let [search-session (:search-session store)
        query-string   (or (:q params) "*")
        page-number    (or (:page params) 0)
        page-size      (or (:size params) 20)
        filter         (if project_id
                         (search-filter :project_id #{project_id})
                         (search-filter :programme_id programmes))
        results        (search/search-entities query-string filter page-number page-size search-session)
        total_hits     (esr/total-hits results)
        parsed-results (parse-entities results programmes nil roles)]

    {:entities {:total_hits total_hits
                :page page-number
                :entities parsed-results}}))

(defmethod filter-entities :project_id [_ projects params store k roles & project_id]
  (let [search-session (:search-session store)
        query-string   (or (:q params) "*")
        page-number    (or (:page params) 0)
        page-size      (or (:size params) 20)
        filter         (if project_id
                         (search-filter :project_id #{project_id})
                         (search-filter :project_id projects))
        results        (search/search-entities query-string filter page-number page-size search-session)
        total_hits     (esr/total-hits results)
        parsed-results (parse-entities results nil projects roles)]

    {:entities {:total_hits total_hits
                :page page-number
                :entities parsed-results}}))

(defmethod filter-entities :default [_ _ params store k roles & project_id]
  (let [search-session (:search-session store)
        query-string   (or (:q params) "*") ;; need to add route params in as programme_id:<foo> project_id:<foo>
        page-number    (or (:page params) 0)
        page-size      (or (:size params) 20)
        results        (if project_id
                         (let [filter (search-filter :project_id #{project_id})]
                           (search/search-entities query-string filter page-number page-size search-session))
                         (search/search-entities query-string page-number page-size search-session))
        total_hits     (esr/total-hits results)
        parsed-results (parse-entities results nil nil roles)]

    {:entities {:total_hits total_hits
                :page page-number
                :entities parsed-results}}))

(defn allowed-all?* [programmes projects roles request-method params store]
  (log/infof "allowed-all?* allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s" programmes projects roles request-method)
  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (has-project-manager? roles)
          (has-user? roles)
          request-method]

         [true _ _ _ :get] [true (filter-entities programmes projects params store nil roles)]
         [_ true _ _ :get] [true (filter-entities programmes projects params store :programme_id roles)]
         [_ _ true _ :get] [true (filter-entities programmes projects params store :project_id roles)]
         [_ _ _ true :get] [true (filter-entities programmes projects params store :project_id roles)]
         :else false))

;; TODO Implement a better way of handling different requests
(defn allowed?*
  ([programme-id project-id allowed-programmes allowed-projects roles request-method params store]
     (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
                programme-id project-id allowed-programmes allowed-projects roles request-method)
     (match [(has-admin? roles)
             (has-programme-manager? roles)
             (some #(= % programme-id) allowed-programmes)
             (has-project-manager? roles)
             (some #(= % project-id) allowed-projects)
             (has-user? roles)
             request-method]

            [true _ _ _ _ _ :post] true
            [_ true true _ _ _ :post] true
            [_ _ _ true true _ :post] true
            [_ _ _ _ _ _ :get] [true (filter-entities allowed-programmes allowed-projects params store nil roles project-id)]
            :else false))
  ([programme-id project-id allowed-programmes allowed-projects roles request-method]
      (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
                 programme-id project-id allowed-programmes allowed-projects roles request-method)
      (match [(has-admin? roles)
              (has-programme-manager? roles)
              (some #(= % programme-id) allowed-programmes)
              (has-project-manager? roles)
              (some #(= % project-id) allowed-projects)
              (has-user? roles)
              request-method]

             [true _ _ _ _ _ _] true
             [_ true true _ _ _ _] true
             [_ _ _ true true _ _] true
             [_ _ true _ _ true :get] true
             [_ _ _ _ true true :get] true
             :else false)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Index
(defn index-allowed? [store ctx]
  (let [{:keys [request-method session params route-params]} (:request ctx)
        project_id (or (:project_id route-params) (-> ctx :entities first :project_id))
        programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))
        {:keys [projects programmes roles]} (sec/current-authentication session)]
    (if (and programme_id project_id)
      (allowed?* programme_id project_id programmes projects roles request-method params store)
      (allowed-all?* programmes projects roles request-method params store))))

(defn index-handle-ok [store ctx]
  (util/render-item ctx (:entities ctx)))

(defn index-handle-malformed [ctx]
  (util/render-item ctx (:malformed-msg ctx)))

(defn index-malformed? [ctx]
  (try
    (let [request (:request ctx)
          {:keys [request-method params route-params multipart-params]} request
          {:keys [project_id]} route-params
          content-type (content-type-from-context ctx) ;; multipart uploads give us weird content-types
          received-data (get multipart-params "data")]
      (match [request-method content-type (nil? project_id) (boolean (seq received-data))]
             [:post "multipart/form-data" false             true ] (es/malformed-multipart-data? received-data project_id)
             [:post "text/csv"            false             _    ] (es/malformed-data? (decode-body request) project_id)
             [:post _                     false             _    ] (-> (decode-body request) (es/malformed-entity-post? project_id))
             [:post _                     true              _    ] (-> (decode-body request) es/malformed-entity-post?)
             [:post "multipart/form-data" _                 false] [true {:malformed-msg "No multipart data sent."}]
             :else false))
    (catch Throwable t
      (log/error t "Malformed entity index.")
      [true {:malformed-msg "Could not parse entity data."}])))

(defn index-exists? [store ctx]
  (if (= :post (-> ctx :request :request-method))
    (if-let [project_id (-> ctx :request :route-params :project_id)]
      (projects/get-by-id (:hecuba-session store) project_id)
      false))
  true)

(defn store-profile [profile user_id store]
  (let [query-profile (assoc profile :user_id user_id)]
    (profiles/insert (:hecuba-session store) query-profile)))

(defn store-entity [entity user_id store]
  (let [query-entity (assoc entity :user_id user_id)]
    (entities/insert (:hecuba-session store) query-entity)
    (-> query-entity
        (search/searchable-entity (:hecuba-session store))
        (search/->elasticsearch (:search-session store)))
    (:entity_id entity)))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request entities profiles]} ctx
          username (sec/session-username (:session request))]
      (mapv #(store-profile % username store) profiles)
      (mapv #(store-entity % username store) entities)
      {:entity_id (-> entities first :entity_id)})))

(defn index-handle-created [ctx]
  (let [request (:request ctx)
        id      (:entity_id ctx)]
    (if id
      (let [location (format entity-resource-path id)]
        (when-not location
          (throw (ex-info "No path resolved for Location header"
                          {:entity_id id})))
        (ring-response {:headers {"Location" location}
                        :body (json/encode {:location location
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422
                      :body "Provide valid projectId and propertyCode."}))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Resource
(defn resource-malformed? [ctx]
  (try
    (let [request (:request ctx)
          {:keys [request-method content-type params route-params multipart-params]} request
          {:keys [entity_id]} route-params
          received-entity (get multipart-params "data")]
      (match
       [request-method content-type          (nil? entity_id) (boolean (seq received-entity))]
       [:put           "multipart/form-data" false            true ] (es/malformed-multipart-entity-put? received-entity entity_id)
       [:put           "text/csv"            false            _    ] (-> (decode-body request) (es/malformed-entity-csv-put? entity_id))
       [:put           _                     false            _    ] (-> (decode-body request) (es/malformed-entity-put? entity_id))
       [:put           "multipart/form-data" false            false] [true {:malformed-msg "No multipart data sent."}]
       [_              _                     true             _    ] [true {:malformed-msg "Missing entity_id"}]
       :else false))
    (catch Throwable t
      (log/error t "Malformed entity.")
      [true {:malformed-msg "Missing entity_id"}])))

(defn resource-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes roles]}     (sec/current-authentication session)
          entity_id (:entity_id params)
          project_id (when entity_id (:project_id (entities/get-by-id (:hecuba-session store) entity_id)))
          programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects roles request-method)
         {:editable (editable? programme_id project_id programmes projects roles)}]
        true))))

(defn resource-exists? [store ctx]
  (let [id (get-in ctx [:request :route-params :entity_id])]
    (when-let [item (search/get-by-id id (:search-session store))]
      {::item item})))

(defn resource-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item
           {mime :media-type} :representation} ctx
           {:keys [request editable]} ctx
           route-params (:route-params request)
           clean-item   (-> item
                            clean-entity
                            (cond-> editable (assoc :editable editable))
                            remove-private-data)
           formatted-item (if (= "text/csv" mime)
                            (let [exploded-item (parser/explode-and-sort-by-schema clean-item es/entity-schema)]
                              exploded-item)
                            clean-item)]
      (util/render-item ctx formatted-item))))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request entity]} ctx
          username  (sec/session-username (-> ctx :request :session))]
      (if-let [entity_id (:entity_id entity)]
        (do (entities/update session entity_id (assoc entity :user_id username))
            (-> (entities/get-by-id session entity_id)
                (search/searchable-entity session)
                (search/->elasticsearch (:search-session store))))
        (throw (Exception. "Missing entity_id.")))))

  (defn resource-delete! [store ctx]
    (db/with-session [session (:hecuba-session store)]
      (let [entity_id  (get-in ctx [::item :entity_id])]
        (entities/delete entity_id session)
        (search/delete-by-id entity_id (:search-session store))))))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

(defresource index [store]
  :allowed-methods #{:post :get}
  :available-media-types #{"text/csv" "application/json" "application/edn"}
  :known-content-type? #{"text/csv" "application/json"}
  :authorized? (authorized? store)
  :allowed? (partial index-allowed? store)
  :exists? (partial index-exists? store)
  :malformed? #(index-malformed? %)
  :handle-malformed index-handle-malformed
  :post! (partial index-post! store)
  :handle-created index-handle-created
  :handle-ok (partial index-handle-ok store))

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"text/csv" "application/json" "application/edn"}
  :known-content-type? #{"text/csv" "application/json"}
  :authorized? (authorized? store)
  :allowed? (resource-allowed? store)
  :exists? (partial resource-exists? store)
  :handle-ok (partial resource-handle-ok store)
  :malformed? #(resource-malformed? %)
  :handle-malformed #(select-keys % [:malformed-msg])
  :put! (partial resource-put! store)
  :respond-with-entity? (partial resource-respond-with-entity)
  :delete! (partial resource-delete! store))
