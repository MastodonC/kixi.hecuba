(ns kixi.hecuba.api.datasets
  (:require
   [bidi.bidi :as bidi]
   [clojure.string :as string]
   [kixi.hecuba.protocols :as hecuba]
   [kixi.hecuba.queue :as q]
   [kixi.hecuba.security :as sec]
   [kixi.hecuba.webutil :as util]
   [kixi.hecuba.webutil :refer (decode-body authorized? uuid routes-from)]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]))

(defn synthetic-device [entity-id description]
  (hash-map :description     description
            :parent-id       (str (uuid))
            :entity-id       entity-id
            :location        "{\"name\": \"Synthetic\", \"latitude\": \"0\", \"longitude\": \"0\"}"
            :metadata        nil
            :privacy         "private"
            :meteringPointId (str (uuid))
            :synthetic       true
            ))

(defn synthetic-sensor [device-id]
  {:device-id                 device-id
   :type                      "Synthetic"
   :unit                      "TBD"
   :resolution                "0" ;; TODO
   :accuracy                  "0" ;; TODO
   :period                    "CUMULATIVE"
   :min                       "0"
   :max                       "100"
   :correction                nil
   :correctedUnit             nil
   :correctionFactor          nil
   :correctionFactorBreakdown nil
   :events                    0
   :errors                    0
   :status                    "Not enough data"
   :median                    0.0
   :synthetic                 true
   })

(defn index-post! [commander req]
  (let [route-params           (:route-params req)
        entity-id              (:entity-id route-params)
        {:keys [members name]} (decode-body req)
        members-str            (string/join \, members)
        ds                     {:entity_id entity-id
                                :name      name
                                :members   members-str}
        device-id              (hecuba/upsert! commander
                                        :device
                                        (synthetic-device entity-id "Synthetic"))]
    (hecuba/upsert! commander :sensor (synthetic-sensor device-id))
    (hecuba/upsert! commander :dataset ds)
    (hash-map :name name
              :entity-id entity-id)))

(defn index-handle-ok [querier req]
  (let [route-params (:route-params req)]
    (util/render-items req (hecuba/items querier
                             :dataset
                             (select-keys route-params [:entity-id])))))

(defn index-handle-created [handlers req]
  (let [route-params (:route-params req)
        {:keys [name entity-id]} route-params
        name         (:name route-params)
        location     (bidi/path-for (routes-from ctx)
                               (:dataset @handlers)
                               :entity-id entity-id
                               :name name)]
    (when-not location
      (throw (ex-info "No path resolved for Location header"
                      {:entity-id entity-id
                       :name :name})))
    (ring-response {:headers {"Location" location}})))

(defresource index [{:keys [commander querier]} handlers]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "text/html"}
  :authorized?           (authorized? querier :datasets)
  :post!                 (partial index-post! querier)
  :handle-ok             (partial index-handle-ok querier)
  :handle-created        (partial index-handle-created handlers))

(defn resource-exists? [querier req]
  (let [{route-params :route-params} req
        {:keys [entity-id name]} route-params]
    (when-let [item (first (hecuba/items querier :dataset {:entity-id entity-id
                                                           :name name}))]
        {::item item}
        #_(throw (ex-info (format "Cannot find item of id %s"))))))

(defn resource-post! [commander req]
  (let [{route-params :route-params} req
        {:keys [entity-id name]} route-params
        {:keys [members name]} (decode-body req)
        ds {:entity_id entity-id
            :name      name
            :members   (string/join \, members)}]
    (hecuba/upsert! commander :dataset ds)))

(defn resource-handle-ok [req]
  (let [item (::item req)]
    (util/render-item req item)))

(defresource resource [{:keys [commander querier]} handlers]
  :allowed-methods       #{:get :post}
  :available-media-types #{"application/edn" "text/html"}
  :authorized?           (authorized? querier :datasets)
  :exists?               (partial resource-exists? querier)
  :post!                 (partial resource-post! commander)
  :handle-ok             resource-handle-ok)
