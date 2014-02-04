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

;; Resources

(defn make-uuid [] (java.util.UUID/randomUUID))

(defresource entities-index [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}
  :post! (fn [{{body :body} :request}]
           {:hecuba/id (upsert! commander (assoc (decode body) :hecuba/id (make-uuid)))})
  :handle-created (fn [{{routes :jig.bidi/routes} :request id :hecuba/id}]
                    (ring-response
                     {:headers {"Location" (path-for routes (:entities-specific @handlers) :hecuba/id id)}})))

(defn downcast-to-json
  "For JSON serialization we need to widen the types contained in the structure."
  [x]
  (postwalk
   #(cond
     (instance?  java.util.UUID %) (str %)
     (keyword? %) (name %)
     (coll? %) %
     :otherwise (throw (ex-info (format "No JSON type for %s"
                                        (type %))
                                {:value %
                                 :type (type %)})))
   x))

(defresource entities-specific [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :delete}
  :available-media-types #{"application/json"}
  :exists? (fn [{{{id :hecuba/id} :route-params} :request}]
             {::item (item querier (UUID/fromString id))})
  :handle-ok (fn [{item ::item}]
               (-> {:entityId (:hecuba/id item)}
                   downcast-to-json))
  :delete! (fn [{{id :hecuba/id} ::item}]
             (delete! commander id)))

;; Routes

(defn make-handlers [opts]
  (let [p (promise)]
    @(deliver p {:entities-index (entities-index opts p)
                 :entities-specific (entities-specific opts p)})))

(def uuid-regex #"[0-9a-f-]+")

(defn make-routes [handlers]
  ;; AMON API here
  ["" [["/entities" (:entities-index handlers)]
       [["/entities/" :hecuba/id] (:entities-specific handlers)]
       ]]
  ;;["/entities/" [uuid-regex :amon/entity-id] "/devices/" [uuid-regex :amon/device-id] "/measurements"] {:entities-index handlers}

)

(deftype ApiServiceV3 [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (add-bidi-routes system config
                     (make-routes (make-handlers (select-keys system [:commander :querier])))))
  (stop [_ system] system))
