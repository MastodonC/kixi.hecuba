(ns kixi.hecuba.web.amon
  (:require
   [kixi.hecuba.protocols :refer (upsert!)]
   jig
   [bidi.bidi :refer (path-for)]
   [jig.bidi :refer (add-bidi-routes)]
   [liberator.core :refer (defresource)]
   [cheshire.core :refer (decode)]
   [liberator.representation :refer (ring-response)])
  (:import (jig Lifecycle))
)

;; Resources

(defn make-uuid [] (java.util.UUID/randomUUID))

(defresource entities-index [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}
  :post! (fn [{{body :body} :request}]
           {:hecuba/id (upsert! commander (assoc (decode body) :hecuba/id (make-uuid)))})
  :handle-created (fn [{{routes :jig.bidi/routes} :request id :hecuba/id}]
                    (ring-response
                     {:headers {"Location" (path-for routes (:entities-specific @handlers) :hecuba/id id)}})
                    ))

(defresource entities-specific)

;; Routes

(defn make-handlers [opts]
  (let [p (promise)]
    @(deliver p {:entities-index (entities-index opts p)
                 :entities-specific entities-specific})))

(def uuid-regex #"[0-9a-f-]+")

(defn make-routes [handlers]
  ;; AMON API here
  ["" [["/entities" (:entities-index handlers)]
       [["/entities/" :hecuba/id] (:entities-specific handlers)]]]
  ;;["/entities/" [uuid-regex :amon/entity-id] "/devices/" [uuid-regex :amon/device-id] "/measurements"] {:entities-index handlers}

)

(deftype ApiServiceV3 [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    ;; TODO call to make-handlers and make-routes
    (add-bidi-routes system config (make-routes (fn [req] {:status 200 :body "Thank you for that measurement!"}))))
  (stop [_ system] system))
