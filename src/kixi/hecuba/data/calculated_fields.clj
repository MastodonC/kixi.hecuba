(ns kixi.hecuba.data.calculated-fields
  "Calcualated fields."
  (:require [kixi.hecuba.storage.db        :as db]
            [qbits.hayt                    :as hayt]
            [clojure.tools.logging         :as log]
            [clojure.string                :as string]
            [clj-time.core                 :as t]
            [clj-time.coerce               :as tc]
            [kixi.hecuba.data.measurements :as measurements]
            [kixi.hecuba.data.calculate    :as calculate]))

(defmulti calculate-field (fn [store device_id type period measurements operation] operation))

(defmethod calculate-field :actual-annual [store device_id type period measurements _]
  (let [calculate-fn (fn [measurements] (case period
                                          "INSTANT" (calculate/average-reading measurements)
                                          "PULSE" (reduce + measurements)))]
    (calculate-fn measurements)))

(defn escape [string]
  (string/escape string {\  "_SPACE_"
                         \&  "_AMPERSAND_"
                         \/  "_SLASH_"} ))

(defn calculate [store sensor operation calculation-name range]

  (let [{:keys [device_id type period]} sensor]
    (log/info "Calculating field: " operation "for sensor: " (str device_id "-" type) "and range: " range)

    (db/with-session [session (:hecuba-session store)]

      (let [measurements             (measurements/parse-measurements
                                      (measurements/measurements-for-range store sensor range (t/hours 1)))
            filtered                 (filter number? (map :value measurements))
            {:keys [entity_id name]} (first (db/execute session (hayt/select :devices (hayt/where [[= :id device_id]]))))
            value                    (calculate-field store device_id type period filtered operation)
            timestamp                (tc/to-date (t/now))
            field                    (str calculation-name ":" device_id ":" (escape type))]

        (db/execute session (hayt/update :entities
                                         (hayt/set-columns {:calculated_fields_values [+ {field (str value)}]
                                                            :calculated_fields_labels [+ {field (str name type)}]
                                                            :calculated_fields_last_calc [+ {field timestamp}]})
                                         (hayt/where [[= :id entity_id]])))))

    (log/info "Finished calculating field: " operation "for sensor: " (str device_id "-" type) "and range: " range)))
