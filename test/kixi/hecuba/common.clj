(ns kixi.hecuba.common
  (:require [kixi.hecuba.protocols :as hecuba]
            [com.stuartsierra.component :as component]))

(deftype RefCommander [r keyfn]
  hecuba/Commander
  (upsert! [_ typ payload]
    (let [id (keyfn payload)]
      (dosync (alter r assoc-in [typ id] (-> payload (assoc :id id) (dissoc :type))))
      id))
  (delete! [_ typ id]
    (dosync (alter r update-in [typ] dissoc id))))

(defrecord RefQuerier [r]
  hecuba/Querier
  (item [_ typ id] (get-in @r [typ id]))
  (items [_ typ]
    (vals (get @r typ)))
  (items [this typ where] (filter #(= where (select-keys % (keys where))) (.items this typ))))

(defn create-ref-store [r keyfn]
  {:commander (->RefCommander r keyfn)
   :querier (->RefQuerier r)})

(defrecord RefStore []
    component/Lifecycle
  (start [this]
    (assoc this)
    )
  (stop [this])
    )

(defn test-system []
      (-> (component/system-map
         :cluster (new-cluster (:cassandra-cluster cfg))
         :session (new-session (:cassandra-session cfg))
         :store (new-direct-store)
         :pipeline (new-pipeline)
         :scheduler (kixipipe.scheduler/mk-session cfg)
         :queue (new-queue (:queue cfg))
         :queue-worker (new-queue-worker)
         :cljs-builder (new-cljs-builder)
         :web-server (new-webserver (:web-server cfg))
         :bidi-ring-handler (new-bidi-ring-handler-provider)
         :main-routes (new-main-routes)
         :amon-api (new-amon-api "/4")
         :user-api (new-user-api)
         :cljs-routes (new-cljs-routes (:cljs-builder cfg)))

        (mod/system-using
         {:main-routes [:store]
          :amon-api [:store :queue :queue-worker]
          :user-api [:store]
          :store [:session]
          :queue-worker [:queue :store]
          :pipeline [:store]
          :scheduler [:pipeline]
          :session [:cluster]}))
  )
