(ns kixi.hecuba.web.amon
  (:require
   [clojure.walk :refer (postwalk)]
   [kixi.hecuba.protocols :refer (upsert! delete! item)]
   jig
   [bidi.bidi :refer (path-for)]
   [jig.bidi :refer (add-bidi-routes)]
   [liberator.core :refer (defresource)]
   [cheshire.core :refer (decode)]
   [liberator.representation :refer (ring-response)])
  (:import
   (jig Lifecycle)
   (java.util UUID)))

;; Utility

(defn make-uuid [] (java.util.UUID/randomUUID))

(defn downcast-to-json
  "For JSON serialization we need to widen the types contained in the structure."
  [x]
  (postwalk
   #(cond
     (instance?  java.util.UUID %) (str %)
     (keyword? %) (name %)
     (or (coll? %) (string? %)) %
     :otherwise (throw (ex-info (format "No JSON type for %s"
                                        (type %))
                                {:value %
                                 :type (type %)})))
   x))

;; Resources

(defresource entities [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}

  :post!
  (fn [{{body :body} :request}]
    (let [uuid (make-uuid)]
      {:amon/entity-id (upsert! commander
                                (assoc (decode body)
                                  :amon/id uuid ; we need this to indicate the unique id
                                  :amon/entity-id uuid
                                  ))}))

  :handle-created
  (fn [{{routes :jig.bidi/routes} :request id :amon/entity-id}]
    (let [location (path-for routes (:entity @handlers)
                             ;; TODO: id is a uuid here, so we str it, but I
                             ;; think this should bidi's responsiblity

                             ;; TODO: Raise an issue against bidi
                             :amon/entity-id (str id)
                             )]
      (when-not location
        (throw (ex-info "No path resolved for Location header"
                        {:amon/entity-id id})))
      (ring-response {:headers {"Location" location}}))))

(defresource entity [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :delete}
  :available-media-types #{"application/json"}

  :exists?
  (fn [{{{id :amon/entity-id} :route-params} :request}]
    {::item (item querier (UUID/fromString id))})

  :handle-ok
  (fn [{item ::item}]
    (-> {"entityId" (:amon/entity-id item)}
        downcast-to-json))

  :delete! (fn [{{id :amon/entity-id} ::item}]
             (delete! commander id)))

(defresource devices [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}

  :malformed?
  (fn [{{body :body {entity-id :amon/entity-id} :route-params} :request}]
    (let [body (decode body)]
      ;; We need to assert a few things
      (or
       (not= (get body "entityId") entity-id))))

  :post!
  (fn [{{body :body {entity-id :amon/entity-id} :route-params} :request}]
    (let [body (decode body)
          uuid (make-uuid)]
      {:amon/device-id (upsert! commander (assoc body :amon/id uuid :amon/device-id uuid))}))

  :handle-created
  (fn [{{routes :jig.bidi/routes {entity-id :amon/entity-id} :route-params} :request device-id :amon/device-id}]
    (let [location
          (path-for routes (:device @handlers)
                    :amon/entity-id entity-id
                    :amon/device-id (str device-id))]
      (when-not location (throw (ex-info "No path resolved for Location header"
                                         {:amon/entity-id entity-id
                                          :amon/device-id device-id})))
      (ring-response {:headers {"Location" location}}))))

(defresource device [{:keys [commander querier]} handlers]
  :allowed-methods #{})

(defresource measurements [{:keys [commander querier]} handlers]
  :allowed-methods #{:post})

;; Handlers and Routes

(defn make-handlers [opts]
  (let [p (promise)]
    @(deliver p {:entities (entities opts p)
                 :entity (entity opts p)
                 :devices (devices opts p)
                 :device (device opts p)
                 :measurements (measurements opts p)
                 })))

(def uuid-regex #"[0-9a-f-]+")

(defn make-routes [handlers]
  ;; AMON API here
  ["" [["/entities" (:entities handlers)]
       [["/entities/" [uuid-regex :amon/entity-id]] (:entity handlers)]
       [["/entities/" [uuid-regex :amon/entity-id] "/devices"] (:devices handlers)]
       [["/entities/" [uuid-regex :amon/entity-id] "/devices/" [uuid-regex :amon/device-id]] (:device handlers)]
       [["/entities/" [uuid-regex :amon/entity-id] "/devices/" [uuid-regex :amon/device-id] "/measurements"] (:measurements handlers)]
       ]])


(deftype ApiServiceV3 [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (add-bidi-routes system config
                     (make-routes (make-handlers (select-keys system [:commander :querier])))))
  (stop [_ system] system))


;; TODO Entity PUTs
;; TODO Device PUTs
;; TODO Measurement GETs
