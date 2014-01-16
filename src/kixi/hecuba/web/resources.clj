(ns kixi.hecuba.web.resources
  (:require
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   [kixi.hecuba.protocols :refer (upsert! item items)]
   [bidi.bidi :refer (->Redirect path-for)]
   [hiccup.core :refer (html)]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [camel-snake-kebab :as csk]))

(def base-media-types ["text/html" "application/json" "application/edn"])

(defn render-items-html
  "Render some HTML for the text/html representation. Mostly this is for
  debug, to be able to check the data without too much UI logic
  involved."
  [items]
  (let [fields (remove #{:hecuba/name :hecuba/id :hecuba/href :hecuba/type}
                       (distinct (mapcat keys items)))]
    (let [DEBUG false]
      (html [:body
             [:h2 "Fields"]
             [:ul (for [k fields] [:li (csk/->snake_case_string (name k))])]
             [:h2 "Items"]
             [:table
              [:thead
               [:tr
                [:th "Name"]
                (for [k fields] [:th (string/replace (csk/->Snake_case_string k) "_" " ")])
                (when DEBUG [:th "Debug"])]]
              [:tbody
               (for [p items]
                 [:tr
                  [:td [:a {:href (:hecuba/href p)} (:hecuba/name p)]]
                  (for [k fields] [:td (str (k p))])
                  (when DEBUG [:td (pr-str p)])])]]]))))

;; Now for the Liberator resources. These are heavily commented because
;; Liberator isn't well-known. Check the Liberator website for more
;; info: http://clojure-liberator.github.io/liberator/

;; REST resource for items (plural) - .
(defresource items-resource [typ querier commander item-resource]
  ;; acts as both an index of existing items and factory for new ones
  :allowed-methods #{:get :post}

  :exists? true ; This 'factory' resource ALWAYS exists
  :available-media-types base-media-types

  :handle-ok ; do this upon a successful GET
  (fn [{{mime :media-type} :representation {routes :jig.bidi/routes route-params :route-params} :request}]
    (let [items
          (->>
           {:hecuba/type typ} ; form a 'where' clause starting with the type
           (merge route-params) ; adding any route-params
           (items querier) ; to query items
           ;; which are adorned with hrefs, throwing bidi's path-for all the kv pairs we have!
           (map #(assoc % :hecuba/href (apply path-for routes item-resource (apply concat %)))))]
      (case mime
        "text/html" (render-items-html items)
        ;; Liberator's default rendering of application/edn seems wrong
        ;; (it wraps the data in 'clojure.lang.PersistentArrayMap', so
        ;; we override it here.
        "application/edn" (pr-str (vec items))

        ;; The default is to let Liberator render our data
        items)))

  :post! ; enact the 'side-effect' of creating an item
  (fn [{{body :body} :request}]
    {:hecuba/id ; add this entry to Liberator's context
     (upsert! commander
              (-> body
                  ;; Prepare to read the body
                  io/reader (java.io.PushbackReader.)
                  ;; Read the body (can't repeat this so no transactions!)
                  edn/read io!
                  ;; Add the type prior to upserting
                  (assoc :hecuba/type typ)))})

  :handle-created ; do this upon a successful POST
  (fn [{{routes :jig.bidi/routes route-params :route-params} :request id :hecuba/id}]
    (ring-response ; Liberator, we're returning an actual Ring map!
     ;; We must return the new resource path in the HTTP Location
     ;; header. Liberator doesn't do this for us, and clients might need
     ;; to know where the new resource is located.
     ;; See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2
     {:headers {"Location" (path-for routes item-resource (assoc route-params :hecuba/id id))}})))

(defn render-item-html
  "Render an item as HTML"
  [item]
  (html
   [:body
    [:h1 (:hecuba/name item)]
    [:pre (pr-str item)]]))

;; REST resource for individual items.
(defresource item-resource [querier]
  :allowed-methods #{:get}
  :available-media-types base-media-types

  :exists? ; iff the item exists, we bind it to ::item in order to save
           ; an extra query later on - this is a common Liberator
           ; pattern
  (fn [{{{id :hecuba/id} :route-params body :body routes :jig.bidi/routes} :request}]
    (when-let [itm (item querier id)] {::item itm}))

  :handle-ok
  (fn [{item ::item {mime :media-type} :representation}]
    (case mime
      "text/html" (render-item-html item)
      ;; The default is to let Liberator render our data
      item)))
