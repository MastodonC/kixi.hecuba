(ns kixi.hecuba.web.project
  (:require
   [liberator.core :refer (defresource)]
   [kixi.hecuba.protocols :refer (upsert! item items)]
   [bidi.bidi :refer (->Redirect path-for)]
   [hiccup.core :refer (html)]
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(def base-media-types ["text/html" "application/json" "application/edn"])

(defresource projects-resource [querier commander project-resource]
  :allowed-methods #{:get :post}
  :available-media-types base-media-types
  :exists? (fn [{{{id :id} :route-params body :body routes :jig.bidi/routes} :request :as ctx}]
             {::projects
              (map (fn [m] (assoc m :href (path-for routes project-resource :id (:id m))))
                   (items querier))})
  :handle-ok (fn [{projects ::projects {mt :media-type} :representation :as ctx}]
               (case mt
                 "text/html"
                 (html [:table
                        (for [p projects]
                          [:tr
                           [:td [:a {:href (:href p)} (:name p)]]
                           [:td (:project-code p)]])])
                 "application/edn" (pr-str (vec projects))
                 projects))
  :post! (fn [{{body :body} :request}]
           (let [payload (io! (edn/read (java.io.PushbackReader. (io/reader body))))]
             (upsert! commander payload))))

(defresource project-resource [querier]
  :allowed-methods #{:get}
  :available-media-types base-media-types
  :exists? (fn [{{{id :id} :route-params body :body routes :jig.bidi/routes} :request :as ctx}]
             (if-let [p (item querier id)] {::project p} false))
  :handle-ok (fn [{project ::project {mt :media-type} :representation :as ctx}]
               (case mt
                 "text/html" (html [:h1 (:name project)])
                 project)))

(defn create-routes [querier commander]
  (let [project (project-resource querier)
        projects (projects-resource querier commander project)]
    [""
     [["projects/" projects]
      ["projects" (->Redirect 307 projects)]
      [["project/" :id] project]]]))
