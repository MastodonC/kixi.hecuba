(ns kixi.hecuba.web.amon
  (:require
   jig
   [jig.bidi :refer (add-bidi-routes)])
  (:import (jig Lifecycle))
)

(def uuid-regex #"[0-9a-f-]+")

(defn make-routes [measurements-handler]
  ;; AMON API here
  [["/entities/" [uuid-regex :amon/entity-id] "/devices/" [uuid-regex :amon/device-id] "/measurements"] measurements-handler]
)

(deftype ApiServiceV3 [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (add-bidi-routes system config (make-routes (fn [req] {:status 200 :body "Thank you for that measurement!"}))))
  (stop [_ system] system))
