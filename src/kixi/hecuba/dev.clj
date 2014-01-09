;; Some utilities that help speed up development

(ns kixi.hecuba.dev
  (:require
   jig
   kixi.hecuba.protocols
   [clojure.tools.logging :refer :all]
   [org.httpkit.client :refer (request) :rename {request http-request}])
  (:import
   (jig Lifecycle)
   (kixi.hecuba.protocols Commander Querier)))

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

(deftype RefCommander [r]
  Commander
  (upsert! [_ payload]
    (infof "upserting... %s" payload)
    (dosync (alter r assoc (:id payload) payload))))

(defrecord RefQuerier [r]
  Querier
  (item [_ id] (get @r id))
  (items [_] (vals @r)))

(deftype RefStore [config]
  Lifecycle
  (init [_ system]
    (let [r (ref {})]
      (-> system
       (assoc :commander (->RefCommander r))
       (assoc :querier (->RefQuerier r)))))
  (start [_ system] system)
  (stop [_ system] system))
