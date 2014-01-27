(ns kixi.hecuba.amon-test
  (:use clojure.test)
  (:require
   [kixi.hecuba.dev :refer (->RefCommander ->RefQuerier)]
   [kixi.hecuba.web.amon :as amon]
   [cheshire.core :refer (generate-string)]
   [bidi.bidi :as bidi]
   [jig.bidi :refer (wrap-routes)]
   [ring.mock.request :refer (request)]))

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
    (let [r (ref {})
          handlers (-> r make-mock-records amon/make-handlers)
          routes (-> handlers amon/make-routes)
          handler (-> routes bidi/make-handler (wrap-routes routes))
          response (-> (make-entity)
                       (add-devices (uuid))
                       (as-json-body-request (bidi/path-for routes (:entities-index handlers)))
                       handler)]
      (is (not (nil? response)))
      (is (= (:status response) 201))
      (is (= (count @r) 1)))))
