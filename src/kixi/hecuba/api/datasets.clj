(ns kixi.hecuba.api.datasets
  (:require
   [bidi.bidi :as bidi]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
   [kixi.hecuba.protocols :as hecuba]
   [qbits.hayt :as hayt]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid routes-from)]
   [kixi.hecuba.storage.dbnew :as db]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn all-datasets [store]
  (db/with-session [session (:hecuba-session store)]
    (db/execute session
                (hayt/select :datasets))))

(defn synthetic-device [entity_id description]
  (hash-map :description     description
            :parent_id       (str (uuid))
            :entity_id       entity_id
            :location        "{\"name\": \"Synthetic\", \"latitude\": \"0\", \"longitude\": \"0\"}"
            :metadata        nil
            :privacy         "private"
            :metering_point_id (str (uuid))
            :synthetic       true
            ))

(defn synthetic-sensor [type device_id unit]
  {:device_id                 device_id
   :type                      type
   :unit                      unit
   :resolution                "0" ;; TODO
   :accuracy                  "0" ;; TODO
   :period                    "PULSE"
   :min                       "0"
   :max                       "100"
   :correction                nil
   :corrected_unit             nil
   :correction_factor          nil
   :correction_factor_breakdown nil
   :events                    0
   :errors                    0
   :status                    "Not enough data"
   :median                    0.0
   :synthetic                 true
   })

(defn synthetic-sensor_metadata [type device_id]
  {:type      type
   :device_id device_id})

(defn- entity_id-from [ctx]
  (get-in ctx [:request :route-params :entity_id]))

(defn- name-from [ctx]
  (get-in ctx [:request :route-params :name]))

;;TODO - duplication with calculate - resolve.
(defn output-unit-for [t]
  (case (.toUpperCase t)
    "VOL2KWH" "kWh"))

(defn output-type-for [t]
  (str "converted_" t))


(defn create-output-sensors [commander device_id unit members]
  (let [parse-sensor (comp next (partial re-matches #"(\w+)-(\w+)"))]
    (doseq [m members]
      (let [[type _ ] (parse-sensor m)
            converted-type (output-type-for type)]
        (hecuba/upsert! commander :sensor (synthetic-sensor converted-type device_id unit))
        (hecuba/upsert! commander :sensor_metadata (synthetic-sensor_metadata converted-type device_id)))))
  )
(defn index-post! [commander ctx]
   (log/info "index-post!")

   (let [request                     (:request ctx)
         {:keys [members name operation]} (decode-body request)
         members                     (into #{} members)
         entity_id                   (entity_id-from ctx)
         unit                        (output-unit-for operation)
         device_id                   (hecuba/upsert! commander
                                                     :device
                                                     (synthetic-device entity_id "Synthetic"))]
     (create-output-sensors commander device_id unit members)
     (hecuba/upsert! commander :dataset
                    {:entity_id entity_id
                     :name      name
                     :members   members
                     :operation operation
                     :device_id device_id}
                                      )
    (hash-map ::name name
              ::entity_id entity_id)))

(defn index-handle-ok [querier ctx]
  (let [entity_id   (entity_id-from ctx)]
    (log/info "index-handle-ok")
    (util/render-items ctx (hecuba/items querier
                             :dataset
                              [[= :entity_id entity_id]]))))

(defn index-handle-created [handlers ctx]
  (let [entity_id   (::entity_id ctx)
        name        (::name ctx)
        location     (bidi/path-for (routes-from ctx)
                               (:dataset @handlers)
                               :entity_id entity_id
                               :name name)
        ]
    (log/info "index-handle-created!")
    (when-not location
      (throw (ex-info "No path resolved for Location header"
                      {:entity_id entity_id
                       :name name})))
    (ring-response {:headers {"Location" location}})))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "text/html"}
  :authorized?           (authorized? querier :datasets)
  :post!                 (partial index-post! commander)
  :handle-ok             (partial index-handle-ok querier)
  :handle-created        (partial index-handle-created handlers))

(defn resource-exists? [querier ctx]
  (let [request      (:request ctx)
        route-params (:route-params request)
        {:keys [entity_id name]} route-params]
    (log/infof "resource-exists? :%s:%s" entity_id name)

    (when-let [item (first (hecuba/items querier :dataset [[= :entity_id entity_id]
                                                           [= :name name]]))]
        {::item item}
        #_(throw (ex-info (format "Cannot find item of id %s"))))))

(defn resource-post! [commander ctx]
  (let [request (:request ctx)
        {:keys [members name operation]} (decode-body request)
        converted-type              (output-type-for operation)]

    (hecuba/upsert! commander :dataset
                    {:entity_id (entity_id-from ctx)
                     :name      (name-from ctx)
                     :members   members
                     :operation operation})))

(defn resource-handle-ok [ctx]
  (let [item (::item ctx)]
    (log/info "resource-handle-ok")
    (util/render-item ctx item)))

(defresource resource [{:keys [commander querier]} handlers]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "text/html"}
  :authorized?           (authorized? querier :datasets)
  :exists?               (partial resource-exists? querier)
  :post!                 (partial resource-post! commander)
  :handle-ok             resource-handle-ok)
