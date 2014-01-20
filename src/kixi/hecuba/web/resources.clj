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
  (let [fields (remove #{:hecuba/name :hecuba/id :hecuba/href :hecuba/type :hecuba/parent :hecuba/parent-href}
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
                [:th "Parent"]
                (for [k fields] [:th (string/replace (csk/->Snake_case_string k) "_" " ")])
                (when DEBUG [:th "Debug"])]]
              [:tbody
               (for [p items]
                 [:tr
                  [:td [:a {:href (:hecuba/href p)} (:hecuba/name p)]]
                  [:td [:a {:href (:hecuba/parent-href p)} "Parent"]]
                  (for [k fields] [:td (str (k p))])
                  (when DEBUG [:td (pr-str p)])])]]]))))

;; Now for the Liberator resources. Check the Liberator website for more
;; info: http://clojure-liberator.github.io/liberator/

;; REST resource for items (plural) - .
(defresource items-resource [typ item-resource parent-resource {:keys [querier commander]}]
  ;; acts as both an index of existing items and factory for new ones
  :allowed-methods #{:get :post}

  :exists? true                  ; This 'factory' resource ALWAYS exists
  :available-media-types base-media-types

  :handle-ok                            ; do this upon a successful GET
  (fn [{{mime :media-type} :representation {routes :jig.bidi/routes route-params :route-params} :request}]
    (let [items
          (->>
           {:hecuba/type typ} ; form a 'where' clause starting with the type
           (merge route-params)         ; adding any route-params
           (items querier)              ; to query items
           ;; which are adorned with hrefs, throwing bidi's path-for all the kv pairs we have!
           (map #(assoc %
                   :hecuba/href (apply path-for routes item-resource (apply concat %))
                   :hecuba/parent-href (path-for routes parent-resource :hecuba/id (:hecuba/parent %)))))]

      (case mime
        "text/html" (render-items-html items)
        ;; Liberator's default rendering of application/edn seems wrong
        ;; (it wraps the data in 'clojure.lang.PersistentArrayMap', so
        ;; we override it here.
        "application/edn" (pr-str (vec items))

        ;; The default is to let Liberator render our data
        items)))

  :post!                   ; enact the 'side-effect' of creating an item
  (fn [{{body :body} :request}]
    {:hecuba/id                  ; add this entry to Liberator's context
     (upsert! commander
              (-> body
                  ;; Prepare to read the body
                  io/reader (java.io.PushbackReader.)
                  ;; Read the body (can't repeat this so no transactions!)
                  edn/read io!
                  ;; Add the type prior to upserting
                  (assoc :hecuba/type typ)))})

  :handle-created                       ; do this upon a successful POST
  (fn [{{routes :jig.bidi/routes route-params :route-params} :request id :hecuba/id}]
    (ring-response      ; Liberator, we're returning an actual Ring map!
     ;; We must return the new resource path in the HTTP Location
     ;; header. Liberator doesn't do this for us, and clients might need
     ;; to know where the new resource is located.
     ;; See http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html#sec10.2.2
     {:headers {"Location"
                (->> id                ; essentially the id
                     ;; but keep existing route params
                     (assoc route-params :hecuba/id)
                     ;; flatten into the keyword param form
                     (apply concat)
                     ;; ask bidi to return the Location URI
                     (apply path-for routes item-resource))}})))

(defn render-item-html
  "Render an item as HTML"
  [item children]
  (html
   [:body
    [:h1 (:hecuba/name item)]
    (when-let [parent-href (:hecuba/parent-href item)]
      [:p [:a {:href parent-href} "Parent"]])
    (when (not (empty? children))
      [:h2 "Children"]
      [:ul
       (for [child children]
         [:li [:a {:href (:hecuba/href child)} (:hecuba/name child)]])])
    [:pre (pr-str item)]]))

;; REST resource for individual items.

;; Delivers REST verbs onto a single (particular) entity, like a
;; particular project or particular house.

;; The parent resource is the handler used by bidi to generate hrefs to
;; this resources parent container. For example, a house is owned by a
;; project.

;; The child resource is the reverse, used by bidi to generate hrefs to
;; any entities this item contains. The child resource is given as a
;; promise, since it cannot be known when constructing handlers top-down.

(defresource item-resource [parent-resource child-resource-p {:keys [querier]}]
  :allowed-methods #{:get}
  :available-media-types base-media-types

  :exists?  ; iff the item exists, we bind it to ::item in order to save
                                        ; an extra query later on - this is a common Liberator
                                        ; pattern
  (fn [{{{id :hecuba/id} :route-params body :body routes :jig.bidi/routes} :request}]
    (when-let [itm (item querier id)]
      {::item (assoc itm
                :hecuba/parent-href
                (when parent-resource
                  (path-for routes parent-resource :hecuba/id (:hecuba/parent itm))))
       ::children
       (map
        #(when child-resource-p
           (assoc % :hecuba/href (path-for routes @child-resource-p :hecuba/id (:hecuba/id %))))
        (items querier {:hecuba/parent id}))}))

  :handle-ok
  (fn [{item ::item children ::children {mime :media-type} :representation}]
    (case mime
      "text/html" (render-item-html item children)
      ;; The default is to let Liberator render our data
      {:item item
       :children children})))
