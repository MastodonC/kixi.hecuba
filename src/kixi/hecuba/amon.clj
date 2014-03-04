(ns kixi.hecuba.amon
  (:require
   [ring.middleware.cookies :refer (wrap-cookies)]
   [bidi.bidi :refer (->Redirect ->WrapMiddleware path-for)]
   [camel-snake-kebab :as csk :refer (->kebab-case-keyword ->camelCaseString)]
   [cheshire.core :refer (decode decode-stream encode)]
   [clj-time.core :as t]
   [kixi.hecuba.webutil :refer (read-edn-body read-json-body)]
   [clj-time.format :as tf]
   [clj-time.coerce :as tc]
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.pprint :refer (pprint)]
   [clojure.string :as string]
   [clojure.walk :refer (postwalk)]
   [hiccup.core :refer (html)]
   [jig.bidi :refer (add-bidi-routes)]
   [kixi.hecuba.protocols :refer (upsert! delete! item items)]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.security :as sec]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)]
   jig)
  (:import
   (jig Lifecycle)
   (java.util UUID)))

(defn authorized? [querier typ]
  (fn [{{route-params :route-params :as req} :request}]
    (or
     (sec/authorized-with-basic-auth? req querier)
     (sec/authorized-with-cookie? req querier))))

(defn downcast-to-json
  "For JSON serialization we need to widen the types contained in the structure."
  [x]
  (postwalk
   #(cond
     (instance? java.util.UUID %) (str %)
     (keyword? %) (name %)
     (or (coll? %) (string? %)) %
     (nil? %) nil
     :otherwise (throw (ex-info (format "No JSON type for %s"
                                        (type %))
                                {:value %
                                 :type (type %)})))
   x))

(defn camelify
  "For JSON serialization we need to widen the types contained in the structure."
  [x]
  (postwalk
   #(cond
     (map? %) (reduce-kv (fn [s k v] (conj s [(->camelCaseString k) v])) {} %)
     :otherwise %)
   x))

(defmacro die [msg & params]
  `(throw (ex-info (format ~msg ~@params) {})))

;; Note: whether we need to stringify the values, I'm not sure.
(defn ->shallow-kebab-map
  "Turn the keys of the given map into kebab-case keywords."
  [m]
  (reduce-kv (fn [s k v] (conj s [(->kebab-case-keyword k) v])) {} m))

(defn render-items-html
  "Render some HTML for the text/html representation. Mostly this is for
  debug, to be able to check the data without too much UI logic
  involved."
  [items]
  (let [fields (remove #{:name :id :href :type :parent :parent-href :children-href}
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
                [:th "Children"]
                (for [k fields] [:th (string/replace (csk/->Snake_case_string k) "_" " ")])
                (when DEBUG [:th "Debug"])]]
              [:tbody
               (for [p items]
                 [:tr
                  [:td [:a {:href (:href p)} (:name p)]]
                  [:td [:a {:href (:parent-href p)} "Parent"]]
                  [:td [:a {:href (:children-href p)} "Children"]]
                  (for [k fields] [:td (let [d (k p)] (if (coll? d) (apply str (interpose ", " d)) (str d)))])
                  (when DEBUG [:td (pr-str p)])])]]]))))

(defn render-item-html
  "Render an item as HTML"
  [item]
  (html
   [:body
    [:h1 (:name item)]
    [:pre (with-out-str
            (pprint item))]]))

;; Resources

(defresource programmes [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :programme)

  :handle-ok
  (fn [{{mime :media-type} :representation {routes :jig.bidi/routes} :request}]
    (let [items (->> (items querier :programme)
                    (map #(-> %
                              (assoc :projects (path-for routes (:projects @handlers) :programme-id (:id %)))
                              ;; TODO Rename :href to :self
                              (assoc :href (path-for routes (:programme @handlers) :programme-id (:id %)))))
                    )]
      (case mime
        "text/html" (render-items-html  items)
        ;; Liberator's default rendering of application/edn seems wrong
        ;; (it wraps the data in 'clojure.lang.PersistentArrayMap', so
        ;; we override it here.
        "application/edn" (pr-str (vec items))
        ;; The default is to let Liberator render our data
        items)))

  :post!
  (fn [{{body :body} :request}]
    {:programme-id (upsert! commander :programme (-> body read-edn-body ->shallow-kebab-map))})

  :handle-created
  (fn [{id :programme-id {routes :jig.bidi/routes} :request}]
    (let [location (path-for routes (:programme @handlers) :programme-id id)]
      (ring-response {:headers {"Location" location}}))))

(defresource programme [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :programme)

  :exists?
  (fn [{{{programme-id :programme-id} :route-params} :request}]
    (when-let [item (item querier :programme programme-id)]
      (prn "Programme id for looking up programme: " programme-id)
      (prn "Programme found: " item)
      {::item item}))

  :handle-ok
  (fn [{item ::item {mime :media-type} :representation {routes :jig.bidi/routes} :request}]
    (let [item (assoc item :projects (path-for routes (:projects @handlers) :programme-id (:id item)))]
      (case mime
        "text/html" (html
                     [:body
                      [:h1 (:name item)]
                      [:pre (with-out-str
                              (pprint item))]
                      [:p [:a {:href (:projects item)} "Projects"]]
                      ])
        "application/edn" (pr-str item)
        ;; The default is to let Liberator render our data
        item))))

(defresource projects [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :project)

  :handle-ok
  (fn [{{mime :media-type} :representation {routes :jig.bidi/routes route-params :route-params} :request}]
    (let [coll (->> (if (:programme-id route-params)
                       (items querier :project route-params)
                       (items querier :project))
                    (map #(-> %
                              (assoc :properties (path-for routes (:properties @handlers) :project-id (:id %)))
                              (assoc :href (path-for routes (:project @handlers) :project-id (:id %))))))]

      (case mime
        "text/html" (render-items-html coll)
        ;; Liberator's default rendering of application/edn seems wrong
        ;; (it wraps the data in 'clojure.lang.PersistentArrayMap', so
        ;; we override it here.
        "application/edn" (pr-str (vec coll))
        ;; The default is to let Liberator render our data
        coll)))

  :post!
  (fn [{{body :body} :request}]
    {:project-id (upsert! commander :project (-> body read-edn-body ->shallow-kebab-map))})

  :handle-created
  (fn [{id :project-id {routes :jig.bidi/routes} :request}]
    (let [location (path-for routes (:project @handlers) :project-id id)]
      (ring-response {:headers {"Location" location}}))))

(defresource project [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types ["text/html" "application/json" "application/edn"]
  :known-content-type? #{"application/edn"}
  :authorized? (authorized? querier :project)

  :exists?
  (fn [{{{project-id :project-id} :route-params} :request}]
    (when-let [item (item querier :project project-id)]
      {::item item}))

  :handle-ok
  (fn [{item ::item {mime :media-type} :representation {routes :jig.bidi/routes} :request}]
    (let [item (assoc item :properties (path-for routes (:properties @handlers) :project-id (:id item)))]
      (case mime
        "text/html" (html
                     [:body
                      [:h1 (:name item)]
                      [:pre (with-out-str
                              (pprint item))]
                      [:p [:a {:href (:properties item)} "Properties"]]])
        "application/edn" (pr-str item)
        ;; The default is to let Liberator render our data
        item))))

;; Properties are entites. But these resource lets you list them by project and provides more information.
(defresource properties [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json" "application/edn" "text/html"}
  :authorized? (authorized? querier :property)

  :handle-ok
  (fn [{{mime :media-type} :representation {routes :jig.bidi/routes route-params :route-params} :request}]
      (let [coll (->> (if (:project-id route-params)
                      (items querier :entity route-params)
                      (items querier :entity))
                      (map #(-> %
                                (assoc :device-ids (map :id (items querier :device {:entity-id (:id %)})))
                                (assoc :href (path-for routes (:entity @handlers) :entity-id (:id %))))))
            scoll (sort-by :address-street-two coll)]

      (case mime
        "text/html" (render-items-html scoll)
        "application/json" (encode (map camelify (map downcast-to-json scoll)))
        scoll))))

;; AMON API compliant from here down

;; Top level /entities in AMON API
(defresource entities [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :entity)

  :post!
  (fn [{{body :body} :request}]
    (let [entity (-> body read-json-body ->shallow-kebab-map)]
      {:entity-id (upsert! commander :entity entity)}))

  :handle-created
  (fn [{{routes :jig.bidi/routes} :request id :entity-id}]
    (let [location (path-for routes (:entity @handlers) :entity-id id)]
      (when-not location
        (throw (ex-info "No path resolved for Location header"
                        {:entity-id id})))
      (ring-response {:headers {"Location" location}}))))

(defresource entity [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :delete}
  :available-media-types #{"application/json"}
  :authorized? (authorized? querier :entity)

  :exists?
  (fn [{{{id :entity-id} :route-params} :request}]
    (when-let [item (item querier :entity id)]
      {::item item}
      #_(throw (ex-info (format "Cannot find item of id %s")))))

  :handle-ok
  (fn [{item ::item {mime :media-type} :representation {routes :jig.bidi/routes route-params :route-params} :request}]
    (let [item (assoc item :device-ids (map :id (items querier :device route-params)))]
      (case mime
        "text/html" (html
                     [:body
                      [:h1 "Entity: "(:name item)]
                      [:pre (with-out-str
                              (pprint item))]
                      [:ul
                       (for [id (:devices-ids item)]
                         [:li id])]])
        "application/json" (-> item downcast-to-json camelify encode))))

  :delete! (fn [{{id :id :as i} ::item}]
             (delete! commander :entity id)))

(defresource devices [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :device)

  :exists?
  (fn [{{{entity-id :entity-id} :route-params} :request}]
    (not (nil? (item querier :entity entity-id))))

  :malformed?
  (fn [{{body :body {entity-id :entity-id} :route-params} :request}]
    (let [body (-> body read-json-body ->shallow-kebab-map)]
      ;; We need to assert a few things
      (if
          (or
           (not= (:entity-id body) entity-id))
        true                 ; it's malformed, game over
        [false {:body body}] ; it's not malformed, return the body now we've read it
        )))

  :post!
  (fn [{{{entity-id :entity-id} :route-params} :request body :body}]
    (let [device-id (upsert! commander :device (-> body
                                                   (dissoc :readings)
                                                   (update-in [:location] encode)))]
      (doseq [reading (:readings body)]
        (upsert! commander :sensor (->shallow-kebab-map (assoc reading :device-id device-id)))
        (upsert! commander :sensor-metadata (->shallow-kebab-map {:device-id device-id :type (get-in reading ["type"])})))
      {:device-id device-id}))

  :handle-created
  (fn [{{routes :jig.bidi/routes {entity-id :entity-id} :route-params} :request device-id :device-id}]
    (let [location
          (path-for routes (:device @handlers)
                    :entity-id entity-id
                    :device-id device-id)]
      (when-not location (throw (ex-info "No path resolved for Location header"
                                         {:entity-id entity-id
                                          :device-id device-id})))
      (ring-response {:headers {"Location" location}}))))

(defresource device [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :authorized? (authorized? querier :device)

  :exists? (fn [{{{:keys [entity-id device-id]} :route-params} :request}]
             (when-let [item (item querier :device device-id)]
               {::item item}))
  :handle-ok (fn [{item ::item}]
               #_(prn "item is " item)
               (-> item
                   (assoc :readings (items querier :sensor {:device-id (:id item)}))
                   (update-in [:location] decode)
                   downcast-to-json camelify encode)))

(defn get-month-partition-key [t]
  (Integer/parseInt (format "%4d%02d" (t/year t) (t/month t))))

(defresource measurements [{:keys [commander querier]} handlers]
  :allowed-methods #{:post}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)

  :post! (fn [{{body :body {:keys [device-id]} :route-params} :request}]
           (doseq [measurement (-> body read-json-body ->shallow-kebab-map :measurements)]
             (let [t        (tf/parse (:date-time-no-ms tf/formatters) (get measurement "timestamp"))
                   m2       {:device-id device-id
                             :type (get measurement "type")
                             :timestamp (tc/to-date t)
                             :value (get measurement "value")
                             :error (get measurement "error")
                             :month (get-month-partition-key t)
                             :metadata "{}"}]
               (->> m2
                   (v/validate commander querier)
                   (upsert! commander :measurement))
               ))
           (println "Measurements added!"))
  :handle-created (fn [_] (ring-response {:status 202 :body "Accepted"})))

;; Handlers and Routes

(defn make-handlers [opts]
  (let [p (promise)]
    @(deliver p {:programmes (programmes opts p)
                 :programme (programme opts p)

                 :projects (projects opts p)
                 :allprojects (projects opts p)
                 :project (project opts p)

                 :properties (properties opts p)
                 :entities (entities opts p)
                 :entity (entity opts p)

                 :devices (devices opts p)
                 :device (device opts p)
                 :measurements (measurements opts p)

                 })))

(def sha1-regex #"[0-9a-f]+")

(defn make-routes [handlers]
  ;; AMON API here
  ["/" (->WrapMiddleware
        [
         ["programmes/" (:programmes handlers)]
         ["programmes" (->Redirect 307 (:programmes handlers))]
         [["programmes/" [sha1-regex :programme-id]] (:programme handlers)]
         [["programmes/" [sha1-regex :programme-id] "/projects"] (:projects handlers)]

         ["projects/" (:allprojects handlers)]
         ["projects" (->Redirect 307 (:allprojects handlers))]
         [["projects/" [sha1-regex :project-id]] (:project handlers)]
         [["projects/" [sha1-regex :project-id] "/properties"] (:properties handlers)]

         ["entities/" (:entities handlers)]
         ["entities" (->Redirect 307 (:entities handlers))]
         [["entities/" [sha1-regex :entity-id]] (:entity handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices"] (:devices handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id]] (:device handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements"] (:measurements handlers)]
         ]
        wrap-cookies)])


(deftype ApiServiceV3 [config]
  Lifecycle
  (init [_ system] system)
  (start [_ system]
    ;; The reason we need to put this into the start (rather than init)
    ;; phase is that commander and querier and sometimes not bound until
    ;; the start phase (they in turn depend on various side-effects,
    ;; such as C* schema creation). Eventually we won't have a different
    ;; init and start phase.
    (let [handlers (make-handlers (select-keys system [:commander :querier]))]
      (-> system
          (assoc :amon/handlers handlers)
          (add-bidi-routes config (make-routes handlers)))))
  (stop [_ system] system))


;; TODO Entity PUTs
;; TODO Device PUTs
;; TODO Measurement GETs
