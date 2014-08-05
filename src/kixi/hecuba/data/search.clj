(ns kixi.hecuba.data.search
  "This transforms the entities in Cassandra to ones that can be
  searched in Elasticsearch and retrieves them from Elasticsearch"
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.document :as doc]
            [clojurewerkz.elastisch.query :as q]
            [kixi.hecuba.storage.search :as search]
            [kixi.hecuba.data.entities :as entities]
            [kixi.hecuba.data.profiles :as profiles]
            [kixi.hecuba.data.projects :as projects]
            [kixi.hecuba.data.devices :as devices]))

(defn postal-address [property_data]
  (let [{:keys [address_street_two address_city address_code address_country]} property_data]
    (->> (vector address_street_two address_city address_code address_country)
         (map (fnil clojure.string/trim ""))
         (filter #(when % (re-seq #"[A-Za-z0-9]" %)))
         (str/join ", ")
         (str/trim))))

(defn property-search-fields [entity]
  (let [{:keys [property_data property_code project_id id]} entity
        {:keys [property_type built_form age address_region project_team]} property_data]
    (-> {}
        (assoc :full_entity entity)
        (assoc :address (postal-address property_data))
        (assoc :property_code property_code)
        (assoc :entity_id id)
        (assoc :project_team project_team)
        (assoc :project_id project_id)

        ;; filters
        (assoc :property_type property_type)
        (assoc :built_form built_form)
        (assoc :age age)
        (assoc :address_region address_region))))

(defn project-search-fields [entity session]
  (let [project_id (:project_id entity)
        {:keys [name organisation type_of programme_id]} (projects/get-by-id session project_id)]
    (-> entity
        (assoc :programme_id programme_id)
        (assoc :type_of type_of)
        (assoc :project_name name)
        (assoc :project_organisation organisation))))

(defn has-technology? [profile technology]
  (-> profile (get technology) seq nil? not))

(defn profile-search-fields [entity session]
  (let [{:keys [entity_id]} entity
        profiles (profiles/get-profiles entity_id session)
        last_profile (last profiles)
        profile_data (-> last_profile :profile_data)]
    (-> entity
        (assoc-in [:full_entity :profiles] profiles)
        (assoc :bedroom_count (get profile_data :bedroom_count 0))
        (assoc :heating_type (-> last_profile :heating_systems first :heating_type))
        (assoc :walls_construction (-> last_profile :walls first :construction))
        (assoc :ventilation_systems (has-technology?  last_profile :ventilation_systems))
        (assoc :photovoltaics (has-technology? last_profile :photovoltaics))
        (assoc :solar_thermals (has-technology?  last_profile :solar_thermals))
        (assoc :wind_turbines (has-technology? last_profile :wind_turbines))
        (assoc :small_hydros (has-technology? last_profile :small_hydros))
        (assoc :heat_pumps (has-technology? last_profile :heat_pumps))
        (assoc :chps (has-technology? last_profile :chps)))))

(defn entity-devices [entity session]
  (let [devices (devices/get-devices session (:entity_id entity))]
    (assoc-in entity [:full_entity :devices] devices)))

(defn searchable-entity [entity session]
  (-> entity
      property-search-fields
      (project-search-fields session)
      (profile-search-fields session)
      (entity-devices session)))

(defn searchable-entities [session]
  (->> (entities/get-all session)
       (map #(searchable-entity % session))))

(defn ->elasticsearch [entity search-session]
  (let [entity_id (:entity_id entity)]
    (search/upsert search-session "entities" "entity" entity_id entity)))

(defn upsert->elasticsearch [search-session entities]
  (doseq [entity entities]
    (->elasticsearch entity search-session)))

(defn refresh-search [db-session search-session]
  (->> (searchable-entities db-session)
       (upsert->elasticsearch search-session)))

;; See http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/query-dsl-query-string-query.html
(defn search-entities
  ([query-string from size search-session]
     (search/search search-session "entities" "entity" :query {:query_string {:query query-string}} :size size :from from))
  ([query-string search-session]
     (search-entities query-string 0 20 search-session)))
