(ns kixi.hecuba.web.device
  (:require
   [liberator.core :refer (defresource)]
   [bidi.bidi :refer (->Redirect)]
   [kixi.hecuba.kafka :as kafka]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [kixi.hecuba.hash :refer (sha1)]))

(def base-media-types ["text/html" "application/json" "application/edn"])

(defn devices [req]
  {:status 200 :body (slurp (io/resource "device.html"))})

(defresource device-resource [producer-config]
  :allowed-methods #{:post}
  :available-media-types base-media-types
  :post! (fn [{{body :body} :request}]
           (let [payload (io! (edn/read (java.io.PushbackReader. (io/reader body))))
                 id (sha1 (str payload))]
           (kafka/send-msg (str {(keyword id) payload}) "readings" producer-config)))
 :post-redirect? (fn [ctx] {:location (format "/devices/new/confirm")})
 ;:post-redirect? (fn [{{ body :body routes :jig/bidi/routes} :request :as ctx}] (println "Request: " (:jig.bidi/routes (:request ctx))))
  )

(defresource device-confirmation []
  :allowed-methods #{:get}
  :available-media-types base-media-types
  :handle-ok (fn [ctx] (format (str "<html> Created new device.<br>\n"))))

(defn create-routes [producer-config]
  [""
   [["devices" {"/device" (->Redirect 307 devices)
                 "/new" { "" devices
                          "/post" (device-resource producer-config)
                          "/confirm" (device-confirmation)}}]]])
