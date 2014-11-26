(ns kixi.hecuba.api.entities.property-map
    (:require
   [clojure.core.match :refer (match)]
   [clojure.tools.logging :as log]
   [kixi.hecuba.security :refer (has-admin? has-programme-manager? has-project-manager? has-user?) :as sec]
   [kixi.hecuba.api :as api :refer (authorized?)]
   [liberator.core :refer (defresource)]
   [kixi.hecuba.storage.db :as db]
   [kixi.hecuba.data.programmes :as programmes]
   [kixi.hecuba.data.projects :as projects]
   [kixi.hecuba.data.entities :as entities]
   [kixi.hecuba.data.entities.search :as search]
   [clojurewerkz.elastisch.native.response :as esr]))

(def default-page-size 500)

(defn remove-private-data [entity]
  (if-not (:editable entity)
      (-> entity
          (api/dissoc-in [:property_data :address_street])
          (api/dissoc-in [:property_data :address_street_two])
          (api/dissoc-in [:property_data :address_city])
          (api/dissoc-in [:property_data :address_code])
          (api/dissoc-in [:property_data :fuel_poverty])
          (assoc-in [:documents] (filter :public? (:documents entity)))
          (dissoc :notes))
      entity))

(defn editable? [programme_id project_id allowed-programmes allowed-projects role]
  (log/infof "editable? programme-id: %s project-id: %s allowed-programmes: %s allowed-projects: %s role: %s"
             programme_id project_id allowed-programmes allowed-projects role)
  (match [(has-admin? role)
          (has-programme-manager? programme_id allowed-programmes)
          (has-project-manager? project_id allowed-projects)
          (has-user? programme_id allowed-programmes project_id allowed-projects)]

         [true _ _ _] true
         [_ true _ _] true
         [_ _ true _] true
         [_ _ _ true] false ;; atm I don't think the user on a programme or project can edit, just view
         :else false))

(defn update-editable [e allowed-programmes allowed-projects role]
  (update-in e [:full_entity] assoc :editable (editable? (:programme_id e) (:project_id e) allowed-programmes allowed-projects role)))

(defn clean-entity
  "Get rid of the keys that we don't want the user to see."
  [entity file-bucket]
  (-> entity
      remove-private-data
      (dissoc :user_id)
      (api/enrich-media-uris file-bucket :photos)))

(defn parse-entities [results allowed-programmes allowed-projects role file-bucket]
  (->> results
       esr/hits-from
       (map :_source)
       (map #(update-editable %  allowed-programmes allowed-projects role))
       (map :full_entity)
       (map #(clean-entity % file-bucket))))

(defn should-terms [allowed-programmes allowed-projects]
  (vec
   (conj
    (concat (map #(hash-map :term {:programme_id %}) (keys allowed-programmes))
            (map #(hash-map :term {:project_id %}) (keys allowed-projects)))
    {:term {:location true}}
    {:term {:public_access "true"}})))

(defn must-term [allowed-programmes allowed-projects]
  (when (every? empty? [allowed-programmes allowed-projects])
    {:term {:public_access "true"}}))

(defn search-filter [must should must-not]
  (let [shoulds (if must (conj (or should []) must) should)]
    {:bool {:must (or must {})
            :should shoulds
            :must_not (or must-not {})}}))

(defn filter-entities
  ([params role store]
     (let [query-string   (or (:q params) "*")
           page-number    (or (:page params) 0)
           page-size      (or (:size params) default-page-size)
           from           (* page-number page-size)
           results        (search/search-entities query-string from page-size (:search-session store))
           total_hits     (esr/total-hits results)
           file-bucket    (-> store :s3 :file-bucket)
           parsed-results (parse-entities results nil nil role file-bucket)]
       {:entities {:total_hits total_hits
                   :page       page-number
                   :entities   parsed-results}}))
  ([params allowed-programmes allowed-projects role store]
   (let [query-string   (or (:q params) "*")
         page-number    (or (:page params) 0)
         page-size      (or (:size params) default-page-size)
         from           (* page-number page-size)
         shoulds        (should-terms allowed-programmes allowed-projects)
         filter-terms   (search-filter (must-term allowed-programmes allowed-projects) shoulds nil)
         results        (search/search-entities query-string filter-terms from page-size (:search-session store))
         total_hits     (esr/total-hits results)
         file-bucket    (-> store :s3 :file-bucket)
         parsed-results (parse-entities results allowed-programmes allowed-projects role file-bucket)]
     {:entities {:total_hits total_hits
                 :page       page-number
                 :entities   parsed-results}})))

(defn allowed?* [programmes projects role request-method params store]
     ;; All entities
     (log/infof "allowed?* allowed-programmes: %s allowed-projects: %s role: %s request-method: %s"
                programmes projects role request-method)
     (match [(has-admin? role)
             request-method]

            [true :get] [true {::items (filter-entities params role store)}]
            [_    :get] [true {::items (filter-entities params programmes projects role store)}]
            :else false))

(defn index-allowed? [store]
  (fn [ctx]
    (let [{:keys [request-method session params]} (:request ctx)
          {:keys [projects programmes role]}     (sec/current-authentication session)
          entity_id    (:entity_id params)
          project_id   (when entity_id (:project_id (entities/get-by-id (:hecuba-session store) entity_id)))
          programme_id (when project_id (:programme_id (projects/get-by-id (:hecuba-session store) project_id)))]
      (allowed?* programmes projects role request-method params store))))

(defn index-handle-ok [store ctx]
  (let [file-bucket (-> store :s3 :file-bucket)]
    (db/with-session [session (:hecuba-session store)]
      (let [items    (::items ctx)
            entities (-> items :entities :entities)]
        {:entities entities}))))

(defresource index [store]
  :allowed-methods #{:get}
  :available-media-types #{"application/edn"}
  :authorized? (authorized? store)
  :allowed? (index-allowed? store)
  :handle-ok (partial index-handle-ok store))
