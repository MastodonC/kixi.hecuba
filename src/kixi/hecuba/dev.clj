;; Some utilities that help speed up development

(ns kixi.hecuba.dev
  (:require
   jig
   kixi.hecuba.model
   [clojure.tools.logging :refer :all]
   [org.httpkit.client :refer (request) :rename {request http-request}])
  (:import
   (jig Lifecycle)
   (kixi.hecuba.model Store)))

(defn put-resource [uri-prefix data]
  (http-request
   {:method :put
    :url (str uri-prefix (:id data))
    :body [(:details data)]}
   identity))

;; TODO: Need to depend on routes and use them to path-for the uri, otherwise we tied ourselves to these URIS prematurely

(deftype ExampleDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (->>
     ;; We could get this sequence from somewhere else
     [{:id "1" :details {:project-code "IRR"
                         :name "Eco-retrofit Ealing"
                         :leaders ["/users/1" "/users/2"]}}

      {:id "2" :details {:project-code "IRR"
                         :name "Eco-retrofit Bolton"
                         :leaders ["/users/1" "/users/2"]}}

      {:id "3" :details {:project-code "IRR"
                         :name "The Glasgow House"
                         :leaders ["/users/3"]}}]

     (map (partial put-resource "http://localhost:8000/projects/")) ; PUT them over HTTP
     (map deref) doall ; wait for all promises to be delivered (all responses to arrive)
     (every? #(= (:status %) 201))  ; check each returns a status of 201
     assert)                          ; fail otherwise!
    system)
  (stop [_ system] system))

(defrecord RefBasedStore [r]
  Store
  (add-project! [_ id details]
    (infof "Adding project: %s" id)
    (dosync (alter r assoc id details)))
  (list-projects [_]
    (for [[k v] (seq (deref r))] (assoc v :id k)))
  (get-project [_ id] (get @r id)))

(deftype RefBasedStoreComponent [config]
  Lifecycle
  (init [_ system]
    (assoc-in system
              [(:jig/id config) :kixi.hecuba.model/store]
              (->RefBasedStore (ref {}))))
  (start [_ system] system)
  (stop [_ system] system))
