(ns kixi.hecuba.api.datasets
  (:require
   [bidi.bidi :as bidi]
   [clojure.string :as string]
   [clojure.tools.logging :as log]
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

(defn- entity-id-from [ctx]
  (get-in ctx [:request :route-params :entity-id]))

(defn- name-from [ctx]
  (get-in ctx [:request :route-params :name]))

(defn index-post! [commander ctx]
   (log/info "index-post!")

   (let [request                (:request ctx)
        {:keys [members name]} (decode-body request)
        entity-id              (entity-id-from ctx)
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

(defn index-handle-ok [querier ctx]
  (let [entity-id   (entity-id-from ctx)]
    (log/info "index-handle-ok")
    (util/render-items ctx (hecuba/items querier
                             :dataset
                              [[= :entity-id entity-id]]))))

(defn index-handle-created [handlers ctx]
  (let [entity-id   (entity-id-from ctx)
        name        (name-from ctx)
        location     (bidi/path-for (routes-from ctx)
                               (:dataset @handlers)
                               :entity-id entity-id
                               :name name)]
    (log/info "index-handle-created!")
    (when-not location
      (throw (ex-info "No path resolved for Location header"
                      {:entity-id entity-id
                       :name :name})))
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
        {:keys [entity-id name]} route-params]
    (log/infof "resource-exists? :%s:%s" entity-id name)

    (when-let [item (first (hecuba/items querier :dataset [[= :entity-id entity-id]
                                                           [= :name name]]))]
        {::item item}
        #_(throw (ex-info (format "Cannot find item of id %s"))))))

(defn resource-post! [commander ctx]
  (let [request (:request ctx)
        {:keys [members name type]} (decode-body request)
        ds {:entity_id (entity-id-from ctx)
            :name      (name-from ctx)
            :members   (string/join \, members)
            :type      type}]
    (log/infof "resource-post! %s" ds)

    (hecuba/upsert! commander :dataset ds)))

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
