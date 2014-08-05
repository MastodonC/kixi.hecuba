(ns kixi.hecuba.api.entities
  (:require
   [clojure.java.io :as io]
   [clojure.data.csv :as csv]
   [clojure.core.match :refer (match)]
   [clojure.tools.logging :as log]
   [cheshire.core :as json]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [qbits.hayt :as hayt]
   [clojurewerkz.elastisch.native.response :as esr]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? content-type-from-context)]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.data.programmes :as programmes]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.users :as users]
   [kixi.hecuba.data.search :as search]
   [kixi.hecuba.storage.sha1 :as sha1]
   [kixi.hecuba.web-paths :as p]))

(def ^:private entities-index-path (p/index-path-string :entities-index))
(def ^:private entity-resource-path (p/resource-path-string :entity-resource))

(defn allowed?* [programme-id project-id allowed-programmes allowed-projects roles request-method]
  (log/infof "allowed?* programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s roles: %s request-method: %s"
             programme-id project-id allowed-programmes allowed-projects roles request-method)
  (match [(has-admin? roles)
          (has-programme-manager? roles)
          (some #(= % programme-id) allowed-programmes)
          (has-project-manager? roles)
          (some #(= % project-id) allowed-projects)
          (has-user? roles)
          request-method]
         ;; super-admin - do everything
         [true _ _ _ _ _ _] true
         ;; programme-manager for this programme - do everything
         [_ true true _ _ _ _] true
         ;; project-manager for this project - do everything
         [_ _ _ true true _ _] true
         ;; user with this programme - get allowed
         [_ _ true _ _ true :get] true
         ;; user with this project - get allowed
         [_ _ _ _ true true :get] true
         :else false))

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes roles]} (sec/current-authentication session)
          project_id (:project_id (:entity ctx))
          programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects roles request-method)
        true))))

(defn resource-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes roles]}     (sec/current-authentication session)
          entity_id (:entity_id params)
          project_id (when entity_id (:project_id (entities/get-by-id (:hecuba-session store) entity_id)))
          programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
      (if (and project_id programme_id)
        (allowed?* programme_id project_id programmes projects roles request-method)
        true))))

;; hang on a minute, do we need the whole schema or shall we aggregate everything
;; in the property_data field?!
(def property-data-schema
  [:description
   :monitoring_policy
   :address_street
   :address_city
   :address_county
   :address_code
   :created_at
   :updated_at
   :address_country
   :terrain
   :address_region
   :degree_day_region
   :ownership
   :fuel_poverty
   :property_value
   :property_value_basis
   :retrofit_start_date
   :retrofit_completion_date
   :project_summary
   :energy_strategy
   :project_team
   :design_strategy
   :other_notes
   :property_type
   :property_type_other
   :built_form
   :built_form_other
   :age
   :construction_date
   :conservation_area
   :listed
   :address_street_two
   :property_code
   :monitoring_hierarchy
   :project_phase
   :construction_start_date
   :practical_completion_date
   :photo_file_name
   :photo_content_type
   :photo_file_size
   :photo_updated_at
   :completeness
   :entity_completeness_6m
   :latitude
   :longitude
   :technology_icons
   :address_code_masked])

(def content-only-schema
  [])

(def document-schema
  [:id
   :content-type
   :name
   :file_name])

(def photo-schema
  [:path])

(def entity-schema
  [:id
   :address_country
   :address_county
   :address_region
   :address_street_two
   :name
   :project_id
   :user_id
   :property_code
   :retrofit_completion_date
   { :name :property_data
     :type :nested-item
     :schema property-data-schema }
   { :name :documents
     :type :associated-items
     :schema document-schema }
   { :name :notes
     :type :associated-items
     :schema content-only-schema }
   { :name :photos
     :type :associated-items
     :schema photo-schema }
   ])

(defn attribute-type [attr]
  (if (keyword? attr)
    :attribute
    (:type attr)))

(defn explode-nested-item [association item-string]
  "Explodes a nested item, that is represented in the object coming from
  the datastore as a json encoded string. Returns a list of vectors of
  two elements, the first being the attribute key, and the second the value.
  The key is expanded to <nested item name>_<attribute name>"
  (let [item (json/decode item-string)
        association-name   (:name   association)
        association-schema (:schema association)]
    (map
     (fn [attr]
       [(str (name association-name) "_" (name attr)) (item (name attr))])
     association-schema)))

(defn explode-associated-items [association items]
  "Explodes the elements of a (one to many) association, that is represented
  in the object coming from the datastore as a list of json encoded strings.
  Returns a list of vectors of two elements, the first being the attribute key,
  and the second the value.
  The keys are expanded like <association name>_<associated item index>_<attribute name>"
  (let [association-name   (name (:name association))
        association-schema (:schema association)]
    (apply concat
    (map-indexed
      (fn [index item-string]
         (let [item-name         (str association-name "_" index)
               named-association (assoc association :name item-name)]
           (if (empty? association-schema)
             [item-name item-string]
             (explode-nested-item named-association item-string))))
      items))))

(defn explode-and-sort-by-schema [item schema]
  "Take a (profile) item from the datastore and converts into a list
  of pairs (represented as a vector) where the first element is the
  exploded key for the attribute and the second is the value"
  (let [exploded-item
         (mapcat
           (fn [attr]
             (let [t (attribute-type attr)]
               (case t
                 :attribute          (list [(name attr) (item attr)])
                 :nested-item        (explode-nested-item attr (item (:name attr)))
                 :associated-items   (explode-associated-items attr (item (:name attr))))))
           schema)]
    exploded-item))

(defn extract-attribute [attr-key input]
  "Extracts a hash-map containing a single key and its value from the input.
  The key is expected to be a keyword, while input is supposed to be a hash-map
  with strings as keys"
  (let [attr-name (name attr-key)
        attr-value (input attr-name)]
  {attr-key attr-value}))

(defn extract-nested-item [attr input]
  "Extracts a nested item from input, returning a hashmap with a single pair,
  where the key is the nested item association name, and the value is a json
  string representing all the attributes of the nested item.
  attr is expected to be a hash-map with at least :name and :schema keys,
  while input is expected to be a hash-map representing the profile, with strings as keys"
  (let [association-name   (:name   attr)
        association-schema (:schema attr)
        nested-item (reduce
                      (fn [nested-item nested-attr]
                        (let [nested-attr-name (name nested-attr)
                              exploded-attr-name (str (name association-name) "_" nested-attr-name)]
                          (conj nested-item { nested-attr (input exploded-attr-name)})))
                      {}
                      association-schema)]
    {association-name nested-item}))

(defn extract-associated-item [association-name association-schema input index]
  "Extracts the item belonging to a 'has many' association from input, at position index."
  (reduce
    (fn [associated-item associated-item-attr]
      (let [associated-item-attr-name (name associated-item-attr)
            exploded-attr-name (str (name association-name) "_" index "_" associated-item-attr-name)]
        (conj associated-item {associated-item-attr (input exploded-attr-name)})))
    {}
    association-schema))

(defn extract-associated-items [attr input]
  "Extracts a collection representing a 'has many' association from input.
  It returns a list of hash-maps, each representing one of the items from
  the association.
  attr is expected to be a hash-map with at least :name and :schema keys,
  while input is expected to be a hash-map representing the whole profile, with strings as keys"
  (let [association-name   (:name attr)
        association-schema (:schema attr)
        attribute-names    (doall (keys input))
        items-id-pattern   (re-pattern (str (name association-name) "_(\\d+)_"))
        items-ids (into #{} (->> attribute-names
                                 (map #(when-some [x (re-find items-id-pattern %)] (last x)))
                                 (filter #(not (nil? %)))))
        associated-items (map #(extract-associated-item association-name association-schema input %) items-ids) ]
    {association-name associated-items}))

(defn parse-by-schema [input schema]
  "Parses input according to schema, assuming it was shaped as a
  'tall' CSV profile.
  This means that the first column contains attribute names, and the
  second column contains values. Attribute names are presented in
  'exploded' format, in order to properly address associations and
  nesting.
  Example:

  attribute type | attribute name              | exploded name              |
  standard       | timestamp                   | timestamp                  |
  nested item    | profile_data, bedroom_count | profile_data_bedroom_count |
  association    | storeys, first storey_type  | storeys_0_storey_type      |"
  (try
    (reduce
     (fn [item attr]
       (let [t (attribute-type attr)
             imploded-attribute (case t
                                  :attribute               (extract-attribute attr input)
                                  :nested-item             (extract-nested-item attr input)
                                  :associated-items        (extract-associated-items attr input))]
         (conj item imploded-attribute)))
     {}
     schema)
    (catch Throwable t
      (log/error "Got malformed CSV. " t)
      nil)))

(defn process-file [ctx]
  (let [file-data (-> ctx :request :multipart-params (get "data"))
        {:keys [tempfile content-type]} file-data
        dir       (.getParent tempfile)
        filename  (.getName tempfile)
        in-file   (io/file dir filename)]
    (with-open [in (io/reader in-file)]
      (try
        (let [data (->> in
                        (csv/read-csv)
                        (into {}))]
          (parse-by-schema data entity-schema))
        (catch Throwable t
          (log/error t "Unparsable CSV.")
          nil)))))

(defmulti malformed? content-type-from-context)

(defmethod malformed? "multipart/form-data" [ctx]
  (let [request   (:request ctx)
        {:keys [route-params request-method]} request
        project_id (:project_id route-params)]
    (case request-method
      :post (let [parsed-csv    (process-file ctx)
                  property_code (:property_code parsed-csv)]
              (if (and property_code parsed-csv (= project_id (:project_id parsed-csv)))
               [false {:entity parsed-csv}]
               true))
      :put  (let [entity_id  (:entity_id route-params)
                  parsed-csv (process-file ctx)]
              (if (and parsed-csv (= entity_id (:id parsed-csv)))
                [false {:entity parsed-csv}]
                true))
      false)))

(defmethod malformed? :default [ctx]
  (let [request (:request ctx)
        {:keys [route-params request-method]} request]
    (case request-method
      :post (let [decoded-body  (decode-body request)
                  entity        (if (= "text/csv" (:content-type request))
                                  (let [body-map (into {} decoded-body)]
                                    (parse-by-schema body-map entity-schema))
                                  decoded-body)
                  {:keys [property_code project_id]} entity]
              (if (and property_code project_id)
                [false {:entity entity}]
                true))
      :put (let [decoded-body  (decode-body request)
                 entity_id     (:entity_id route-params)
                 entity        (if (= "text/csv" (:content-type request))
                                 (let [body-map (into {} decoded-body)]
                                   (parse-by-schema body-map entity-schema))
                                 decoded-body)]
             (if entity
               [false {:entity (assoc entity :id entity_id)}]
               true))
       false)))

(defn index-post! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request entity]} ctx
          project_id    (:project_id entity)
          username      (sec/session-username (:session request))
          user_id       (:id (users/get-by-username session username))]
      (when (projects/get-by-id session project_id)
        (let [entity_id (sha1/gen-key :entity entity)
              query-entity (assoc entity
                             :user_id user_id
                             :id entity_id)]
          (entities/insert session query-entity)
          {::entity_id entity_id})))))

(defn index-handle-created [ctx]
  (let [request (:request ctx)
        id      (::entity_id ctx)]
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

(defn resource-exists? [store ctx]
  (let [id (get-in ctx [:request :route-params :entity_id])
        search-results (search/search-entities id 0 1 (:search-session store))]
    (when-let [item (->> search-results esr/hits-from (map #(-> % :_source :full_entity)) first)]
      {::item item})))

(defn resource-handle-ok [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{item ::item
          {mime :media-type} :representation} ctx
          request      (:request ctx)
          route-params (:route-params request)
          ids          (map :id (db/execute session (hayt/select :devices (hayt/where [[= :entity_id (:entity_id route-params)]]))))
          clean-item   (-> item
                           (assoc :device_ids ids)
                           (dissoc :user_id))
          formatted-item (if (= "text/csv" mime)
                           (let [exploded-item (explode-and-sort-by-schema clean-item entity-schema)]
                             exploded-item)
                           clean-item)]
      (util/render-item request formatted-item))))

(defn resource-put! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (let [{:keys [request entity]} ctx
          username  (sec/session-username (-> ctx :request :session))]
      (when entity
        (entities/update session (:id entity) (assoc entity :user_id username))))))

(defn resource-delete! [store ctx]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session (hayt/delete :entities (hayt/where [[= :id (get-in ctx [::item :id])]])))))

(defn resource-respond-with-entity [ctx]
  (let [request (:request ctx)
        method  (:request-method request)]
    (cond
     (= method :delete) false
      :else true)))

;; curl -v -H "Content-Type: application/json" -H 'Accept: application/edn' -X GET -u support-test@mastodonc.com:password 127.0.0.1:8010/4/entities/?q=TSB119

(defn index-response [query page-number page-size allowed-programmes allowed-projects search-session]
  (let [from (* page-number page-size)
        search-results (search/search-entities query from page-size search-session)
        total_hits (esr/total-hits search-results)
        hits (->> search-results esr/hits-from (map #(-> % :_source :full_entity)))]
    {:total_hits total_hits
     :page page-number
     :entities hits}))

(defn index-handle-ok [store ctx]
  (let [request (:request ctx)
        params (:params request)
        query-string (or (:q params) "*")
        page-number (or (:page params) 0)
        page-size (or (:size params) 20)]
    (index-response query-string page-number page-size nil nil (:search-session store))))

(defresource index [store]
  :allowed-methods #{:post :get}
  :available-media-types #{"text/csv" "application/json" "application/edn"}
  :known-content-type? #{"text/csv" "application/json"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :malformed? #(malformed? %)
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
  :malformed? #(malformed? %)
  :put! (partial resource-put! store)
  :respond-with-entity? (partial resource-respond-with-entity)
  :delete! (partial resource-delete! store))
