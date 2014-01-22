;; Some utilities that help speed up development

(ns kixi.hecuba.dev
  (:require
   jig
   kixi.hecuba.protocols
   [bidi.bidi :refer (path-for match-route)]
   [kixi.hecuba.hash :refer (sha1)]
   [kixi.hecuba.data :as data]
   [clojure.tools.logging :refer :all]
   [clojure.java.io :as io]
   [clojure.walk :refer (postwalk)]
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

(defn get-id-from-path [system path]
  (get-in (match-route (get-routes system) path) [:params :hecuba/id]))

(deftype ExampleDataLoader [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    (let [remove-nil-parent (fn [m] (if (:hecuba/parent m) m (dissoc m :hecuba/parent)))

          ;; This function provides a non-lazy recursive
          ;; sequence-comprehension over the data in order to POST it
          ;; into the application.
          post-resource-tree
          (fn this [data pairs parent]
            (doseq [item ((ffirst pairs) data)]
              (let [handler ((ffirst pairs) (get-handlers system))
                    _ (assert handler)
                    response @(post-resource
                               (format "http://localhost:%d%s"
                                       (get-port system)
                                       (path-for (get-routes system) handler :hecuba/parent parent))
                               (-> item
                                   (dissoc (first (second pairs)))
                                   ;;(assoc :hecuba/parent parent)
                                   remove-nil-parent))
                    id (get-id-from-path system (get-in response [:headers :location]))]
                {:item id
                 :children (when-let [n (next pairs)] (this item n id))})))]

      ;; Some dummy data - you can add any fields in here, it's
      ;; schema-less. There are some keys that have special meaning,
      ;; they are in the hecuba namespace to denote this.

      (-> {:programmes
           [{:hecuba/name "America"
             :leaders "Bush"
             :projects
             [{:hecuba/name "Green Manhattan"
               :properties [{:hecuba/name "The Empire State Building"
                             :address "New York"
                             :rooms 100
                             :date-of-construction 1930}]}


              {:hecuba/name "The Historical Buildings Project"
               :properties [{:hecuba/name "Falling Water"
                             :address "1491 Mill Run Rd, Mill Run, PA"
                             :rooms 4
                             :date-of-construction 1937}]}

              {:hecuba/name "Area 51 Conservation Project"}]}

            {:hecuba/name "London"
             :leaders "Blair"
             :projects
             [{:hecuba/name "Monarchy Energy Savings"
               :properties [{:hecuba/name "Buckingham Palace"
                             :address "London SW1A 1AA, United Kingdom"
                             :rooms 775}

                            {:hecuba/name "Windsor Castle"
                             :rooms 175}]}

              {:hecuba/name "Carbon Neutral Tech City"
               :properties [{:hecuba/name "The ODI"
                             :address "3rd Floor, 65 Clifton Street, London EC2A 4JE"
                             :rooms 13
                             }]}]}]}
          (post-resource-tree data/hierarchy nil)))
    system)
  (stop [_ system] system))

(deftype RefCommander [r]
  Commander
  (upsert! [_ payload]
    (assert (every? payload #{:hecuba/name :hecuba/type}))
    (infof "upserting... %s" payload)
    (let [id (-> payload ((juxt :hecuba/type :hecuba/name)) pr-str sha1)]
      (dosync (alter r assoc-in [id] (assoc payload :hecuba/id id)))
      id)))

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
                      (path-for (get-routes system) (:programmes (get-handlers system))))]
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
             (<= 2 (count projects)))
          (println "Warning: HTTP client checks failed" (count projects)))))
    system)
  (stop [_ system] system))
