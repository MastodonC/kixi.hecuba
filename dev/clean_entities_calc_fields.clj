(ns clean-entities-calc-fields
  (:require [kixi.hecuba.data.entities :as entities]
            [kixi.hecuba.data.entities.search :as search]))

(defn clean-calc-fields [entity_id store]
  (entities/update (:hecuba-session store)
                   entity_id
                   {:user_id "admin@getembed.com"
                    :calculated_fields_labels {}
                    :calculated_fields_last_calc {}
                    :calculated_fields_values {}})

  (search/delete-by-id entity_id (:search-session store))
  (-> entity_id
      (search/searchable-entity-by-id (:hecuba-session store))
      (search/->elasticsearch (:search-session store))))
