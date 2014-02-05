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

(defn make-device
  ([] {})
  ([uuid] {:deviceId uuid}))

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
  {:commander (->RefCommander r :amon/id)
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
    (let [{handler :handler {entity-id :amon/entity-id} :params}
          (bidi/match-route routes (get-in response [:headers "Location"]))
          entity-id (UUID/fromString entity-id)]
      (is (= handler (:entity handlers)))
      (is (contains? @db entity-id))
      ;; Return the entity-id, might be useful
      entity-id
      )))

(defn get-entity [db id]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:entity handlers) :amon/entity-id (str id)))
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
        path (-> routes (bidi/path-for (:entity handlers) :amon/entity-id (str id)))
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
        path (-> routes (bidi/path-for (:devices handlers) :amon/entity-id (str entity-id)))
        _ (is (= path (str "/entities/" entity-id "/devices")))
        handler (-> routes bidi/make-handler (wrap-routes routes))
        response (-> {"entityId" entity-id}
                     (as-json-body-request path)
                     handler)]
    (is (not (nil? response)))
    (is (= (:status response) 201))
    (let [location (get-in response [:headers "Location"])
          {handler :handler {entity-id :amon/entity-id
                             device-id :amon/device-id} :params}
          (bidi/match-route routes location)
          device-id (java.util.UUID/fromString device-id)]
      (is (not (nil? (java.util.UUID/fromString entity-id))))
      ;; Return the device-id to the caller, might be useful
      device-id
      )))

(defn create-device-with-bad-entity-in-body [db entity-id]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:devices handlers) :amon/entity-id (str entity-id)))
        _ (is (= path (str "/entities/" entity-id "/devices")))
        handler (-> routes bidi/make-handler (wrap-routes routes))
        response (-> {"entityId" (str entity-id "BAD_DATA")}
                     (as-json-body-request path)
                     handler)]
    (is (not (nil? response)))
    (is (= (:status response) 400))))

(defn send-measurements [db entity-id device-id measurements]
  (let [handlers (-> db make-mock-records amon/make-handlers)
        routes (-> handlers amon/make-routes)
        path (-> routes (bidi/path-for (:measurements handlers)
                                       :amon/entity-id (str entity-id)
                                       :amon/device-id (str device-id)))
        handler (-> routes bidi/make-handler (wrap-routes routes))
        response (-> {"measurements" measurements}
                     (as-json-body-request path)
                     handler)]
    (is (not (nil? response)))
    (is (= (:status response) 202))))

(defn amon-api-tests []
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
    (create-device db entity-id))

  ;; Test create device with wrong entity in body
  (let [db (ref {})
        entity-id (create-entity db)]
    (create-device-with-bad-entity-in-body db entity-id))

  ;; Send in some measurements to a new device
  (let [db (ref {})
        entity-id (create-entity db)
        device-id (create-device db entity-id)]
    (send-measurements db entity-id device-id [{:type :temperature :value 50}
                                               {:type :temperature :value 60}
                                               {:type :temperature :value 55}])))


(deftest amon-api-tests-ref
  (amon-api-tests))
