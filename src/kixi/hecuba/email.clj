(ns kixi.hecuba.email
  (:require [clojure.tools.logging :as log]
            [clj-http.client :as client]
            [com.stuartsierra.component :as component]
            [cheshire.core :as json]))

(defn send-email [session to subject message]
  (try
    (let [{:keys [api-url key]} session]
      (client/post
       api-url
       {:accept :json
        :body (json/generate-string
               {:key key
                :message {:from_email "support@mastodonc.com"
                          :from_name "support@mastodonc.com"
                          :to [{:email to
                                :name to
                                :type "to"}]
                          :subject subject
                          :html message
                          :headers {"Reply-To" "support@mastodonc.com"}}})}))
    (catch Exception e (log/errorf "Caught exception : %s" e))))

(defrecord Email [opts]
  component/Lifecycle
  (start [this]
    this)
  (stop [this] this))

(defn new-email [opts]
  (->Email opts))
