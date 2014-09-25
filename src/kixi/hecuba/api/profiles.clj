(ns kixi.hecuba.api.profiles
  (:require
   [clojure.core.match :refer (match)]
   [cheshire.core :as json]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? stringify-values update-stringified-lists sha1-regex content-type-from-context)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.profiles :as profiles]
   [clojure.java.io           :as io]
   [clojure.data.csv          :as csv]
   [kixi.hecuba.data.entities.search :as search]
   [kixi.hecuba.api.parser :as parser]
   [kixi.hecuba.api.profiles.schema :as ps]))

(def ^:private entity-profiles-resource (p/resource-path-string :entity-profiles-resource))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects role request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects role request-method)
  (match  [(has-admin? role)
           (has-programme-manager? programme-id allowed-programmes)
           (has-project-manager? project-id allowed-projects)
           (has-user? programme-id allowed-programmes project-id allowed-projects)
           request-method]

          [true _ _ _ _]    true
          [_ true _ _ _]    true
          [_ _ true _ _]    true
          [_ _ _ true :get] true
          :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; INDEX

(defn index-allowed? [store]
  (fn [ctx]
    (let [request (:request ctx)
          {:keys [body request-method session params route-params]} request
          {:keys [projects programmes role]} (sec/current-authentication session)
          entity_id (:entity_id route-params)
          {:keys [project_id programme_id]} (search/get-by-id entity_id (:search-session store))]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {:request request}]
        true))))

(defn index-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request        (:request ctx)
          method         (:request-method request)
          route-params   (:route-params request)
          entity_id      (:entity_id route-params)
          entity         (:entity ctx)]
      (case method
        :post (seq entity)
        :get (let [items (profiles/get-profiles entity_id session)]
               (if (empty? items)
                 false
                 [true {::items items
                        ::entity_id entity_id}]))))))

(defn add-profile-keys [& pairs]
  (->> pairs
       (map-indexed
         (fn [index pair]
           (let [k (first pair)
                 v (last  pair)]
           {(str "profile_" index "_key")   k
            (str "profile_" index "_value") v})))
       (into {})))

(defmulti index-malformed? content-type-from-context)

(defmethod index-malformed? "multipart/form-data" [ctx]
  (let [file-data (-> ctx :request :multipart-params (get "data"))
        {:keys [tempfile content-type]} file-data
        dir       (.getParent tempfile)
        filename  (.getName tempfile)
        in-file   (io/file dir filename)
        request   (:request ctx)
        {:keys [route-params]} request
        entity_id (:entity_id route-params)]
    (with-open [in (io/reader in-file)]
      (try
        (let [data (->> in
                        (csv/read-csv)
                        (parser/csv->maps ps/profile-schema)
                        (map sha1/add-profile-id))]
          (if (and data
                   (every? identity (map (fn [d] (let [e (:entity_id d)] (= e entity_id))) data)))
            [false {:profiles data}]
            true))
        (catch Throwable t
          (log/error t "Unparsable CSV.")
          true)))))

(defmethod index-malformed? :default [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request
        entity_id (:entity_id route-params)]
    (case request-method
      :post (let [decoded-body (decode-body request)
                  body         (if (= "text/csv" (:content-type request))
                                 (parser/csv->maps ps/profile-schema decoded-body)
                                 [decoded-body])]
              ;; TODO It's not working as body is a seq and we can post to multiple entities
              ;; test file is in examples/csv-upload/profiles.csv
              ;; curl command used:
              ;; curl -v -H "Content-Type: text/csv; charset=utf-8" -H "Accept: text/csv" -X POST -u "username:password" --data-binary "@profiles.csv" http://localhost:8010/4/entities/821e6367f385d82cc71b2afd9dc2df3b2ec5b81c/profiles/
              (if (not (every? #(= (:entity_id %) entity_id) body))
                true
                [false {:profiles (map sha1/add-profile-id body)}]))
      false)))

(defn index-handle-ok-text-csv* [ctx]
  ;; serving tall csv style profiles
  (let [{items ::items
         entity_id ::entity_id} ctx
         exploded-items         (map #(parser/explode-and-sort-by-schema % ps/profile-schema) items)]
    (ring-response {:headers (util/headers-content-disposition
                              (str entity_id "_all_profiles.csv"))
                    :body    (util/render-items
                              ctx
                              (apply util/map-longest
                                     add-profile-keys ["" ""] exploded-items))})))

(defmulti index-handle-ok content-type-from-context)

(defmethod index-handle-ok :default [ctx]
  (if-let [ctx (util/maybe-representation-override-in-url ctx)]
    (index-handle-ok-text-csv* ctx)
    (let [{items ::items} ctx]
      (util/render-items ctx (->> items
                                  (map #(update-in % [:timestamp] str))
                                  (map #(dissoc % :user_id)))))))

(defmethod index-handle-ok "text/csv" [ctx]
  (index-handle-ok-text-csv* ctx))

(defn index-handle-created [ctx]
  (let [entity_id  (-> ctx :request :route-params :entity_id)
        profiles (:profiles ctx)
        profile_ids (map #(:profile_id %) profiles)]
    (if (seq profile_ids)
      (let [locations (map #(format entity-profiles-resource entity_id %) profile_ids)]
        (when-not (seq locations)
          (throw (ex-info "No path resolved for Location header"
                          {:entity_id entity_id
                           :profile_ids (vec profile_ids)})))
        (ring-response {:headers {"Location" (first locations)}
                        :body (json/encode {:location (vec locations)
                                            :status "OK"
                                            :version "4"})}))
      (ring-response {:status 422
                      :body "Provide valid entity_id and timestamp."}))))

(defn store-profile [profile username store]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [entity_id]} profile]
      (when entity_id
        (profiles/insert session (assoc profile :user_id username))
        (-> (search/searchable-entity-by-id entity_id session)
            (search/->elasticsearch (:search-session store)))
        {:profile_id (:profile_id profile)}))))

(defn index-post! [store ctx]
  (let [{:keys [request profiles]} ctx
        username (sec/session-username (:session request))]
    (doall (map #(store-profile % username store) profiles))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; RESOURCE

(defn resource-allowed? [store]
  (fn [ctx]
    (let [request (:request ctx)
          {:keys [request-method session params route-params]} request
          {:keys [projects programmes role]}     (sec/current-authentication session)
          entity_id (:entity_id route-params)
          entity (search/get-by-id entity_id (:search-session store))
          {:keys [project_id programme_id]} entity]
      (if (and project_id programme_id)
        [(allowed?* programme_id project_id programmes projects role request-method)
         {:entity entity :request request}]
        true))))

(defn resource-exists? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request    (:request ctx)
          _          (log/infof "resource-exists? request: %s" request)
          entity_id  (-> request :params :entity_id)
          profile_id (-> request :params :profile_id)
          item       (profiles/get-by-id session profile_id)]
      (if-not (empty? item)
        {::item item
         ::profile_id profile_id}
        false))))

(defn resource-delete-enacted? [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item} ctx
          profile_id (:profile_id item)
          response (profiles/delete session profile_id)]
      (empty? response))))

(defn respond-with-entity? [ctx]
  (let [request        (:request ctx)
        method         (:request-method request)]
    (if (= :put method) true false)))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [request (:request ctx)]
      (if-let [item (::item ctx)]
        (let [decoded-body  (decode-body request)
              body     (if (= "text/csv" (:content-type request))
                         (let [body-map (reduce conj {} decoded-body)]
                           (parser/parse-by-schema body-map ps/profile-schema))
                         decoded-body)
              {:keys [entity_id profile_id]} item
              username   (sec/session-username (-> ctx :request :session))]
          (profiles/insert session (assoc body :user_id username))
          (-> (search/searchable-entity-by-id entity_id session)
              (search/->elasticsearch (:search-session store)))
          {::item body})
        (ring-response {:status 404 :body "Please provide valid entity_id and timestamp"})))))

(defn resource-handle-ok-text-csv* [store ctx]
  (let [{:keys [::item ::profile_id]} ctx
        exploded-item (parser/explode-and-sort-by-schema item ps/profile-schema)]
    (ring-response {:headers (util/headers-content-disposition
                              (str profile_id "_profile_data.csv"))
                    :body (util/render-item
                           ctx
                           exploded-item)})))

(defmulti resource-handle-ok content-type-from-context)

(defmethod resource-handle-ok :default resource-handle-ok-default [store ctx]
  (if-let [ctx (util/maybe-representation-override-in-url ctx)]
    (resource-handle-ok-text-csv* store ctx)
    (util/render-item ctx (-> (::item ctx)
                              (update-in [:timestamp] str)
                              (dissoc :user_id)))))

(defmethod resource-handle-ok "text/csv" resource-handle-text-csv [store ctx]
  (resource-handle-ok-text-csv* store ctx))

(defresource index [store]
  :allowed-methods #{:get :post}
  :available-media-types #{"text/csv" "application/json" "application/edn"}
  :known-content-type? #{"text/csv" "application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :exists? (partial index-exists? store)
  :malformed? #(index-malformed? %)
  :post! (partial index-post! store)
  :handle-ok index-handle-ok
  :handle-created index-handle-created)

(defresource resource [store]
  :allowed-methods #{:get :delete :put}
  :available-media-types #{"text/csv" "application/json" "application/edn"}
  :known-content-type? #{"text/csv" "application/json" "application/edn"}
  :authorized? (authorized? store)
  :allowed? (resource-allowed? store)
  :exists? (partial resource-exists? store)
  :delete-enacted? (partial resource-delete-enacted? store)
  :respond-with-entity? respond-with-entity?
  :new? (constantly false)
  :can-put-to-missing? (constantly false)
  :put! (partial resource-put! store)
  :handle-ok (partial resource-handle-ok store))
