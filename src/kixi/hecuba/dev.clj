;; Some utilities that help speed up development

(ns kixi.hecuba.dev
  (:require
   jig
   kixi.hecuba.protocols
   [bidi.bidi :refer (path-for match-route)]
   [kixi.hecuba.hash :refer (sha1)]
   [clojure.tools.logging :refer :all]
   [clojure.java.io :as io]
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

(defn get-port [system]
  (-> system :jig/config :jig/components :hecuba/webserver :port))

(defn get-routes [system]
  (-> system :hecuba/routing :jig.bidi/routes))

(defn get-handlers [system]
  (-> system :handlers))

(defn put-items [system typ items]
  {:post [(every? (fn [resp] (= (:status resp) 201)) %)]}
  (->>
   ;; We could get this sequence from somewhere else
   items

   ;; PUT them over HTTP
   (map (partial post-resource
                 (format "http://localhost:%d%s"
                         (get-port system)
                         (path-for (get-routes system) (typ (get-handlers system))))))
   ;; wait for all promises to be delivered (all responses to arrive)
   (map deref) doall
   ))

(defn get-id-from-path [system path]
  (get-in (match-route (get-routes system) path) [:params :hecuba/id]))

(deftype ExampleDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (println
     (let [[p1 p2 p3]
           (for [response
                 (put-items system
                            :projects
                            [{:hecuba/name "Eco-retrofit Ealing"
                              :project-code "IRR"
                              :leaders ["/users/1" "/users/2"]}

                             {:hecuba/name "Eco-retrofit Bolton"
                              :project-code "IRR"
                              :leaders ["/users/1" "/users/2"]}

                             {:hecuba/name "The Glasgow House"
                              :project-code "IRR"
                              :leaders ["/users/3"]}])]
             (get-id-from-path system (get-in response [:headers :location]))
             )]
       (put-items system
                  :properties
                  [{:hecuba/name "Falling Water"
                    :address "1491 Mill Run Rd, Mill Run, PA"
                    :rooms 4
                    :date-of-construction 1937
                    :hecuba/parent p1}
                   {:hecuba/name "The Empire State Building"
                    :address "New York"
                    :rooms 100
                    :date-of-construction 1930
                    :hecuba/parent p1}
                   {:hecuba/name "Buckingham Palace"
                    :address "London SW1A 1AA, United Kingdom"
                    :rooms 775
                    :hecuba/parent p2}
                   {:hecuba/name "The ODI"
                    :address "3rd Floor, 65 Clifton Street, London EC2A 4JE"
                    :rooms 13
                    :hecuba/parent p3}])
       ))


    system)
  (stop [_ system] system))

(deftype RefCommander [r]
  Commander
  (upsert! [_ payload]
    (assert (every? payload #{:hecuba/name :hecuba/type}))
    (infof "upserting... %s" payload)
    (let [id (-> payload ((juxt :hecuba/type :hecuba/name)) pr-str sha1)]
      (dosync (alter r assoc-in [id] (assoc payload :hecuba/id id)))
      id)
    ))

(defrecord RefQuerier [r]
  Querier
  (item [_ id] (get @r id))
  (items [_] (vals @r))
  (items [this where] (filter #(= where (select-keys % (keys where))) (.items this))))

(deftype RefStore [config]
  Lifecycle
  (init [_ system]
    (let [r (ref {})]
      (-> system
       (assoc :commander (->RefCommander r))
       (assoc :querier (->RefQuerier r)))))
  (start [_ system] system)
  (stop [_ system] system))

(deftype HttpClientChecks [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [uri (format "http://localhost:%d%s"
                      (get-port system)
                      (path-for (get-routes system) (:projects (get-handlers system))))]
      (let [projects-response
            @(http-request
              {:method :get
               :url uri
               :headers {"Accept" "application/edn"}
               }
              identity)
            projects
            (clojure.edn/read (java.io.PushbackReader. (io/reader (:body projects-response))))]
        (when-not
            (and
             (= (get-in projects-response [:headers :content-type]) "application/edn;charset=UTF-8")
             (= 3 (count projects)))
          (println "Warning: HTTP client checks failed"))))
    system)
  (stop [_ system] system))
