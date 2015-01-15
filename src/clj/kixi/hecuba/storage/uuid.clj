(ns kixi.hecuba.storage.uuid
  "Functions to generate uuid keys"
  (:require [clojure.tools.logging :as log]
            [schema.core :as s]))

(defn uuid-str [] (str (java.util.UUID/randomUUID)))

(defn uuid [] (java.util.UUID/randomUUID))

(def KeyableEntity
  {:project_id s/Str
   :property_code s/Str
   s/Any s/Any})

(defn add-entity-id [entity]
  (try
    (s/validate KeyableEntity entity)
    (assoc entity :entity_id (uuid-str))
    (catch Throwable t
      (log/errorf t "Tried to add id to %s" entity)
      (throw t))))

(def KeyableProfile
  {:entity_id s/Str
   :profile_data {:event_type s/Str
                  s/Any s/Any}
   s/Any s/Any})

(defn add-profile-id [profile]
  (s/validate KeyableProfile profile)
  (assoc profile :profile_id (uuid-str)))
