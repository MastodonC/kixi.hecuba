(ns kixi.hecuba.amon-test
  (:use clojure.test)
  (:require
   [kixi.hecuba.dev :refer (->RefCommander ->RefQuerier)]
   [kixi.hecuba.web.amon :as amon]
   [cheshire.core :refer (generate-string parse-string)]
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
        path (-> routes (bidi/path-for (:entities handlers)))
        handler (-> routes bidi/make-handler (wrap-routes routes))
        response (-> (make-entity)
                     (add-devices (uuid))
                     (as-json-body-request path)
                     handler)]
    (is (not (nil? response)))
    (is (= (:status response) 201))
    (is (= (count @db) 1))
    (let [{handler :handler {uuid :hecuba/entity-id} :params}
          (bidi/match-route routes (get-in response [:headers "Location"]))
          uuid (UUID/fromString uuid)]
      (is (= handler (:entity handlers)))
      (is (contains? @db uuid))
      ;; Return the uuid
      uuid)))

(defn get-entity [db id]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:entity handlers) :hecuba/entity-id (str id)))
        handler (-> routes bidi/make-handler (wrap-routes routes))]
    (let [orig-db @db]
      (is (= (count @db) 1))
      (let [response (-> (request :get path) handler)]
        (is (not (nil? response)))
        (is (= (:status response) 200))
        (is (= orig-db @db)) ; ensure we didn't modify the database
        (let [json (parse-string (:body response))]
          (is (contains? json "entityId")))))))

(defn delete-entity [db id]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:entity handlers) :hecuba/entity-id (str id)))
        handler (-> routes bidi/make-handler (wrap-routes routes))]
    (is (= (count @db) 1))
    (let [response (-> (request :delete path) handler)]
      (is (not (nil? response)))
      (is (= (:status response) 204))
      (is (= (count @db) 0)))))

;; TODO Try deleting an entity that doesn't exist! (should get a 404)

(defn create-device [db entity-id]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:devices handlers) :hecuba/entity-id entity-id))
        _ (is (= path (str "/entities/" entity-id "/devices")))
        handler (-> routes bidi/make-handler (wrap-routes routes))
        response (-> (make-entity)
                     (add-devices (uuid))
                     (as-json-body-request path)
                     handler)]
    (is (not (nil? response)))
    (is (= (:status response) 201))))

(deftest amon-api-tests
  ;; Test create entity
  (-> (ref {}) create-entity)

  ;; Test delete entity
  (let [db (ref {})]
    (create-entity db)
    (get-entity db (-> @db keys first str)))

  ;; Test delete entity
  (let [db (ref {})]
    (create-entity db)
    (delete-entity db (-> @db keys first str)))

  ;; Test create device
  (let [db (ref {})
        entity-id (create-entity db)]
    (create-device db entity-id)))
