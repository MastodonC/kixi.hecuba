(ns kixi.hecuba.web.property
  (:require
   [liberator.core :refer (defresource)]
   [kixi.hecuba.protocols :refer (upsert! item items)]
   [bidi.bidi :refer (->Redirect path-for)]
   [hiccup.core :refer (html)]
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(def base-media-types ["text/html" "application/json" "application/edn"])

(defn properties-resource []
  {:status 200 :body (format (str "<html> Properties"))})

(defn chart []
  {:status 200 :body (slurp (io/resource "hecuba/chart.html"))})

(defresource property-resource [querier]
  :allowed-methods #{:get}
  :available-media-types base-media-types
  :handle-ok (fn [{{{id :id} :route-params body :body} :request :as ctx}]
               ;(format (str "<html> Property: " id))
               (slurp (io/resource "hecuba/chart.html"))
              ))

(defn create-routes [querier commander]
  (let [property (property-resource querier)
        properties (properties-resource) ]
    [""
     [["properties" {"/new" (->Redirect 307 properties)
                     [:id "data"] property}]]]))
