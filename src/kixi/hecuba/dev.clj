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

(defn post-resource [post-uri data]
  (http-request
   {:method :post
    :url post-uri
    :body [data]}
   identity))

;; TODO: Need to depend on routes and use them to path-for the uri, otherwise we tied ourselves to these URIS prematurely

(deftype ExampleDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (->>
     ;; We could get this sequence from somewhere else
     [{:project-code "IRR"
       :name "Eco-retrofit Ealing"
       :leaders ["/users/1" "/users/2"]}

      {:project-code "IRR"
       :name "Eco-retrofit Bolton"
       :leaders ["/users/1" "/users/2"]}

      {:project-code "IRR"
       :name "The Glasgow House"
       :leaders ["/users/3"]}]

     (map (partial post-resource "http://localhost:8000/projects/")) ; PUT them over HTTP
     (map deref) doall ; wait for all promises to be delivered (all responses to arrive)
     #_(every? #(= (:status %) 201))  ; check each returns a status of 201
     println)                          ; fail otherwise!
    system)
  (stop [_ system] system))



(deftype RefCommander [r]
  Commander
  (upsert! [_ payload]
    (infof "upserting... %s" payload)
    (let [id (str (java.util.UUID/randomUUID))]
      (dosync (alter r assoc id (assoc payload :id id))))))

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
