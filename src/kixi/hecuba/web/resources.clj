(ns kixi.hecuba.web.resources
  (:require
   [liberator.core :refer (defresource)]
   [kixi.hecuba.protocols :refer (upsert! item items)]
   [bidi.bidi :refer (->Redirect path-for)]
   [hiccup.core :refer (html)]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [camel-snake-kebab :as csk]))

(def base-media-types ["text/html" "application/json" "application/edn"])

(defresource items-resource [typ querier commander detail-resource]
  :allowed-methods #{:get :post}
  :available-media-types base-media-types
  :exists? (fn [{{body :body routes :jig.bidi/routes} :request :as ctx}]
             {::items
              (map (fn [m] (assoc m :hecuba/href (path-for routes detail-resource :id (:hecuba/id m))))
                   (items querier {:hecuba/type typ}))})
  :handle-ok (fn [{items ::items {mt :media-type} :representation :as ctx}]
               (case mt
                 "text/html"
                 (let [debug false
                       fields (remove #{:hecuba/name :hecuba/id :hecuba/href :hecuba/type}
                                      (distinct (mapcat keys items)))]
                   (html [:body
                          [:h2 "Fields"]
                          [:ul (for [k fields] [:li (csk/->snake_case_string (name k))])]
                          [:h2 "Items"]
                          [:table
                           [:thead
                            [:tr
                             [:th "Name"]
                             (for [k fields] [:th (string/replace (csk/->Snake_case_string k) "_" " ")])
                             (when debug [:th "Debug"])]]
                           [:tbody
                            (for [p items]
                              [:tr
                               [:td [:a {:href (:hecuba/href p)} (:hecuba/name p)]]
                               (for [k fields] [:td (str (k p))])
                               (when debug [:td (pr-str p)])])]]]))
                 "application/edn" (pr-str (vec items))
                 items))
  :post! (fn [{{body :body} :request}]
           (let [payload (io! (edn/read (java.io.PushbackReader. (io/reader body))))]
             (upsert! commander (assoc payload :hecuba/type typ)))))

(defresource item-resource [querier]
  :allowed-methods #{:get}
  :available-media-types base-media-types
  :exists? (fn [{{{id :id} :route-params body :body routes :jig.bidi/routes} :request :as ctx}]
             (if-let [p (item querier id)] {::item p} false))
  :handle-ok (fn [{item ::item {mt :media-type} :representation :as ctx}]
               (case mt
                 "text/html" (html [:h1 (:hecuba/name item)])
                 item)))
