(ns kixi.hecuba.dev
  (:require
   jig
   [clojure.tools.logging :refer :all]
   [org.httpkit.client :refer (request) :rename {request http-request}])
  (:import
   (jig Lifecycle)))

(defn put-resource [uri-prefix data]
  (http-request
   {:method :put
    :url (str uri-prefix (:id data))
    :body [(:details data)]}
   identity))

(deftype ExampleDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (->>
     ;; We could get this sequence from somewhere else
     [{:id 1 :details {:project-code "IRR"
                       :name "Eco-retrofit Ealing"
                       :leaders ["/users/1" "/users/2"]}}

      {:id 2 :details {:project-code "IRR"
                       :name "Eco-retrofit Bolton"
                       :leaders ["/users/1" "/users/2"]}}

      {:id 3 :details {:project-code "IRR"
                       :name "The Glasgow House"
                       :leaders ["/users/3"]}}]

     (map (partial put-resource "http://localhost:8000/projects/")) ; PUT them over HTTP
     (map deref) doall ; wait for all promises to be delivered (all responses to arrive)
     (every? #(= (:status %) 201))  ; check each returns a status of 201
     assert                         ; fail otherwise!

     )

    system)
  (stop [_ system] system))
