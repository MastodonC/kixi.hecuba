;; Some utilities that help speed up development

(ns kixi.hecuba.dev
  (:require
   jig
   kixi.hecuba.protocols
   [bidi.bidi :refer (path-for)]
   [kixi.hecuba.hash :refer (sha1)]
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
    (let [routes (-> system :hecuba/routing :jig.bidi/routes)
          handlers (-> system :handlers)
          port (-> system :jig/config :jig/components :hecuba/webserver :port)]

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

       ;; PUT them over HTTP
       (map (partial post-resource
                     (format "http://localhost:%d%s"
                             port
                             (path-for routes (:projects handlers)))))
       ;; wait for all promises to be delivered (all responses to arrive)
       (map deref) doall
       ;; check each returns a status of 201
       (every? #(= (:status %) 201))
       ;; fail otherwise!
       assert))
    system)
  (stop [_ system] system))



(deftype RefCommander [r]
  Commander
  (upsert! [_ payload]
    (infof "upserting... %s" payload)
    (let [id (sha1 (:name payload))]
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
