(ns kixi.hecuba.amon-test
  (:use clojure.test)
  (:require
   [kixi.hecuba.dev :refer (->RefCommander ->RefQuerier)]
   [kixi.hecuba.web.amon :as amon]
   [cheshire.core :refer (generate-string)]
   [bidi.bidi :as bidi]
   [jig.bidi :refer (wrap-routes)]
   [ring.mock.request :refer (request)])
  (:import (java.util UUID)))

(defn uuid [] (java.util.UUID/randomUUID))

(defn make-entity
  ([] {})
  ([uuid] {:entityId uuid}))

(defn add-devices [entity & uuids]
  (update-in entity [:deviceIds] concat uuids))

(defn add-metering-points [entity & uuids]
  (update-in entity [:meteringPointIds] concat uuids))

(defn add-json-content-type [req]
  (update-in req [:headers] conj ["Content-Type" "application/json"]))

(defn as-json-body-request [body path]
  (-> (request :post path)
      add-json-content-type
      (assoc-in [:body] (generate-string body))))

(defn make-mock-records [r]
  {:commander (->RefCommander r :hecuba/id)
   :querier (->RefQuerier r)})

;; TODO: Can this be simplified with Prismatic graph?
(defn create-entity [db]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:entities-index handlers)))
        handler (-> routes bidi/make-handler (wrap-routes routes))
        response (-> (make-entity)
                     (add-devices (uuid))
                     (as-json-body-request path)
                     handler)]
    (is (not (nil? response)))
    (is (= (:status response) 201))
    (is (= (count @db) 1))
    (let [{handler :handler {uuid :hecuba/id} :params}
          (bidi/match-route routes (get-in response [:headers "Location"]))
          uuid (UUID/fromString uuid)]
      (is (= handler (:entities-specific handlers)))
      (is (contains? @db uuid))
      )
    db))

(defn delete-entity [db id]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:entities-specific handlers) :hecuba/id id))
        handler (-> routes bidi/make-handler (wrap-routes routes))]
    (is (= (count @db) 1))
    (let [response (-> (request :delete path) handler)]
      (is (not (nil? response)))
      (is (= (:status response) 204))
      (is (= (count @db) 0))
      db)))

;; Try deleting an entity that doesn't exist! (should get a 404)

(deftest amon-api-tests
  (-> (ref {}) create-entity)
  (let [db (ref {})
        db (create-entity db)]
    (-> db (delete-entity (-> db deref keys first str)))))
