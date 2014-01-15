;; Some utilities that help speed up development

(ns kixi.hecuba.dev
  (:require
   jig
   kixi.hecuba.protocols
   [bidi.bidi :refer (path-for)]
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
   ;; check each returns a status of 201
   (every? #(= (:status %) 201))
   ;; fail otherwise!
   assert))

(deftype ExampleDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
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
                 :leaders ["/users/3"]}])

    (put-items system
               :properties
               [{:hecuba/name "Falling Water"
                 :address "1491 Mill Run Rd, Mill Run, PA"}
                {:hecuba/name "Buckingham Palace"
                 :address "London SW1A 1AA, United Kingdom"}
                {:hecuba/name "The ODI"
                 :address "3rd Floor, 65 Clifton Street, London EC2A 4JE"}])
    system)
  (stop [_ system] system))

(deftype RefCommander [r]
  Commander
  (upsert! [_ payload]
    (assert (every? payload #{:hecuba/name :hecuba/type}))
    (infof "upserting... %s" payload)
    (let [id (-> payload ((juxt :hecuba/type :hecuba/name)) pr-str sha1)]
      (dosync (alter r assoc-in [id] (assoc payload :hecuba/id id))))))

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
