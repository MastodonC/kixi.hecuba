(ns kixi.hecuba.storage.search
  "Elasticsearch session."
  (:require [clojure.tools.logging :as log]
            [com.stuartsierra.component :as component]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.document :as doc]
            [kixi.hecuba.protocols :as hecuba]))

(defrecord ElasticsearchConnection [opts]
  component/Lifecycle
  (start [this]
    (assoc this :search-session
           (es/connect  [[(:host opts) 9300]]
                        {"cluster.name" (:name opts)})))
  (stop [this]
    (dissoc this :search-session))
  hecuba/Elasticsearch
  (hecuba/-upsert [this index mapping-type id doc]
    (try
      (doc/upsert (:search-session this) index mapping-type id doc)
      (catch Throwable t
        (log/errorf t "Could not upsert index: %s mapping-type: %s id: %s doc: %s" index mapping-type id doc)
        (throw t))))
  (hecuba/-search [this index mapping-type args]
    (try
      (apply doc/search (:search-session this) index mapping-type args)
      (catch Throwable t
        (log/errorf t "Could not search index: %s mapping-type: %s args: %s" index mapping-type args)
        (throw t)))))

(defn new-search-session [opts]
  (->ElasticsearchConnection opts))

(defn upsert [session index mapping-type id doc]
  (hecuba/-upsert session index mapping-type id doc))
(defn search [session index mapping-type & args]
  (hecuba/-search session index mapping-type args))