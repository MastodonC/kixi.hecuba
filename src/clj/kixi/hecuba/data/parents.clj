(ns kixi.hecuba.data.parents
  (:require [qbits.hayt             :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn- get-ids->parent [store table key ids]
  (let [result
        (->> (db/with-session [session (:hecuba-session store)]
               (db/execute session (hayt/select table
                                                (hayt/columns :id key)
                                                (hayt/where [[:in :id (vec ids)]]))))
             (keep #(when-let [v (not-empty (get % key))] [(:id %) v]))
             (into {}))]
    result))

(defn projects
  "For the given project ids returns a map {<project-id> <programme_id>}"
  [store project-ids]
  (get-ids->parent store :projects :programme_id project-ids))

(defn entities
  "For the given device ids returns a map {<property_id> <project-id>}"
  [store entity-ids]
  (get-ids->parent store :entities :project_id entity-ids))

(defn devices
  "For the given device ids returns a map {<device-id> <entity_id>}"
  [store device-ids]
  (get-ids->parent store :devices :entity_id device-ids))
