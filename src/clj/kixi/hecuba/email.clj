(ns kixi.hecuba.email
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [com.stuartsierra.component :as component]
            [amazonica.aws.simpleemail :as ses]
            [cheshire.core :as json]))

(defn send-email [session to subject message]
  (try
    (let [{:keys [api-url key]} session]
      (ses/send-email :destination {:to-addresses [to]}
                      :source "support@mastodonc.com"
                      :reply-to-addresses ["support@mastodonc.com"]
                      :message {:subject subject
                                :body {:html (:html-content message)
                                       :text (:text-content message)}}))
    (catch Exception e (log/errorf "Caught exception : %s" e))))

(defrecord Email [opts]
  component/Lifecycle
  (start [this]
    this)
  (stop [this] this))

(defn new-email [opts]
  (->Email opts))
