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

(defn as-json-body-request [body path]
  (-> (request :post path)
      (update-in [:headers] conj ["Content-Type" "application/json"])
      (assoc-in [:body] (generate-string body))))

(defn make-mock-records [r]
  {:commander (->RefCommander r :hecuba/id)
   :querier (->RefQuerier r)})

(deftest amon-api-tests
  (testing "Create a new entity"
    (let [database (ref {})
          resource-key :entities-index
          handlers (-> database make-mock-records amon/make-handlers)
          routes (-> handlers amon/make-routes)
          path (-> routes (bidi/path-for (resource-key handlers)))
          handler (-> routes bidi/make-handler (wrap-routes routes))
          response (-> (make-entity)
                       (add-devices (uuid))
                       (as-json-body-request path)
                       handler)]
      (is (not (nil? response)))
      (is (= (:status response) 201))
      (is (= (count @database) 1))
      (let [{handler :handler {uuid :hecuba/id} :params}
            (bidi/match-route routes (get-in response [:headers "Location"]))
            uuid (UUID/fromString uuid)]
        (is (= handler (:entities-specific handlers)))
        (is (contains? @database uuid))
        ))))

;; TODO: Can the above be simplified with Prismatic graph?
