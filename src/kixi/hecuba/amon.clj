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
   [kixi.hecuba.protocols :refer (upsert! delete! item items)]
   [kixi.hecuba.data.validate :as v]
   [kixi.hecuba.data.misc :as misc]
   [kixi.hecuba.security :as sec]
   [liberator.core :refer (defresource)]
   [liberator.representation :refer (ring-response)])
  (:import
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
  (let [fields (remove #{:href :type :parent}
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
                  [:td [:a {:href (:href p)} (:name p)]]
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
  :allowed-methods #{:get :post}
  :available-media-types #{"text/html" "application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :device)

  :exists?
  (fn [{{{entity-id :entity-id} :route-params
        method :request-method} :request}]
    (let [{id :id :as entity} (item querier :entity entity-id)]
      (case method
        :post (not (nil? entity))
        :get (let [items (items querier :device {:entity-id id})]
               {::items items}))))

  :malformed?
  (fn [{{body :body
        {entity-id :entity-id} :route-params
        method :request-method} :request}]
    (case method
      :post (let [body (-> body read-json-body ->shallow-kebab-map)]
              ;; We need to assert a few things
              (if
                  (or
                   (not= (:entity-id body) entity-id))
                true                    ; it's malformed, game over
                [false {:body body}] ; it's not malformed, return the body now we've read it
                ))
      false))

  :post!
  (fn [{{{entity-id :entity-id} :route-params} :request body :body}]
    (let [device-id (upsert! commander :device (-> body
                                                   (dissoc :readings)
                                                   (update-in [:location] encode)))]
      (doseq [reading (:readings body)]
        ;; Initialise new sensor
        ;; TODO Find a better place/way of doing this
        (let [sensor (-> reading
                         (assoc :device-id device-id)
                         (assoc :errors 0)
                         (assoc :events 0)
                         (assoc :median 0)
                         )]
          (upsert! commander :sensor (->shallow-kebab-map sensor))
          (upsert! commander :sensor-metadata (->shallow-kebab-map {:device-id device-id :type (get-in reading ["type"])}))))
      {:device-id device-id}))

  :handle-ok
  (fn [{items ::items {mime :media-type} :representation {routes :jig.bidi/routes route-params :route-params} :request :as request}]
    (case mime
      "text/html" (html
                   [:body
                    [:h1 "Entity: "(:name items)]
                    [:pre (with-out-str
                            (pprint items))]
                    [:ul
                     (for [{:keys [entity-id id]} items]
                       [:li (str "#" entity-id "#" id)])]])
      "application/json" (->> items
                              (map #(update-in % [:location] decode))
                              downcast-to-json
                              camelify
                              encode)
      ))

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
               ;; We need to assoc in the device-id, that's what the
               ;; AMON API requires in the JSON body, ultimately.
               {::item (-> item
                           (assoc :device-id device-id)
                           (dissoc :id))}))
  :handle-ok (fn [{item ::item}]
               (-> item
                   ;; These are the device's sensors.
                   (assoc :readings (items querier :sensor (select-keys item [:device-id])))
                   ;; Note: We are NOT showing measurements here, in
                   ;; contradiction to the AMON API.  There is a
                   ;; duplication (or ambiguity) in the AMON API whereby
                   ;; measurements can be retuned from both the device
                   ;; representation and a sub-resource
                   ;; (measurements). We are only implementing the
                   ;; sub-resource, since most clients requiring a
                   ;; device's details will suffer from these resource
                   ;; representations being bloated with measurements.
                   ;; Specifially, we are keeping the following line commented :-
                   ;; (assoc :measurements (items querier :measurement {:device-id (:id item)}))
                   (update-in [:location] decode)
                   downcast-to-json camelify encode)))

(defresource sensor-metadata [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"text/html" "application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :device)

  :exists? (fn [{{{:keys [entity-id device-id]} :route-params} :request}]
             (when-let [items (items querier :sensor-metadata {:device-id device-id})]
               {::items items}))

  :handle-ok
  (fn [{items ::items {mime :media-type} :representation {routes :jig.bidi/routes route-params :route-params} :request :as request}]
    items
         ;downcast-to-json
         ;camelify
         ;encode
         ))

(defn get-month-partition-key
  "Returns integer representation of year and month from java.util.Date"
  [t] (Integer/parseInt (.format (java.text.SimpleDateFormat. "yyyyMM") t)))

(defn db-timestamp
  "Returns java.util.Date from String timestamp."
  [t] (.parse (java.text.SimpleDateFormat.  "yyyy-MM-dd'T'HH:mm:ss") t))

(defn assoc-conj
  "Associate a key with a value in a map. If the key already exists in the map,
  a vector of values is associated with the key."
  [map key val]
  (assoc map key
    (if-let [cur (get map key)]
      (if (vector? cur)
        (conj cur val)
        [cur val])
      val)))

(defn decode-query-params
  [params]
  (reduce
   (fn [m param]
     (if-let [[k v] (string/split param #"=" 2)]
       (assoc-conj m k v)
       m))
   {}
   (string/split params #"&")))

(defresource measurements-slice [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)

  :handle-ok (fn [{{{:keys [device-id reading-type]} :route-params query-string :query-string}
                   :request {mime :media-type} :representation}]
               (let [decoded-params (decode-query-params query-string)
                     formatter      (java.text.SimpleDateFormat. "dd-MM-yyyy HH:mm")
                     start-date     (.parse formatter (string/replace (get decoded-params "startDate") "%20" " "))
                     end-date       (.parse formatter (string/replace (get decoded-params "endDate") "%20" " "))
                     measurements   (items querier :measurement [:device-id device-id
                                                                 :type reading-type
                                                                 :month (get-month-partition-key start-date)
                                                                 :timestamp [>= start-date]
                                                                 :timestamp [<= end-date]])]
                 (case mime
                   "text/html" (html
                                [:body
                                 [:table
                                  (for [m measurements]
                                    [:tr
                                     [:td
                                      [:pre (with-out-str (pprint m))]]])]])
                   "application/json" (->> measurements downcast-to-json camelify encode)))))

(defresource measurements [{:keys [commander querier]} handlers]
  :allowed-methods #{:post :get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)

  :post! (fn [{{body :body {:keys [device-id]} :route-params} :request}]
           (doseq [measurement (-> body read-json-body ->shallow-kebab-map :measurements)]
             (let [t        (db-timestamp (get measurement "timestamp"))
                   m2       {:device-id device-id
                             :type (get measurement "type")
                             :timestamp t
                             :value (get measurement "value")
                             :error (get measurement "error")
                             :month (get-month-partition-key t)
                             :metadata "{}"}]
               (->> m2
                   (v/validate commander querier)
                   (upsert! commander :measurement))
               )))

  :handle-ok (fn [{{{:keys [device-id]} :route-params} :request {mime :media-type} :representation}]
               (let [measurements (items querier :measurement {:device-id device-id})]
                 (case mime
                   "text/html" (html
                                [:body
                                 [:table
                                  (for [m measurements]
                                    [:tr
                                     [:td
                                      [:pre (with-out-str (pprint m))]]])]])
                   "application/json" (->> measurements downcast-to-json camelify encode))))

  :handle-created (fn [_] (ring-response {:status 202 :body "Accepted"})))

(defresource sensors-by-property [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :known-content-type? #{"application/json"}
  :authorized? (authorized? querier :measurement)
  :handle-ok (fn [{{{:keys [entity-id]} :route-params} :request {mime :media-type} :representation}]
               (let [devices (items querier :device {:entity-id entity-id})
                     sensors (mapcat (fn [{:keys [id location]}]
                                       (map #(assoc % :location (decode location)) (items querier :sensor {:device-id id}))) devices)]
                 (case mime
                   "text/html" (html
                                [:body
                                 [:table
                                  (for [s sensors]
                                    [:tr
                                     [:td
                                      [:pre (with-out-str (pprint s))]]])]])
                   "application/json" (->> sensors downcast-to-json camelify encode)))))

(defresource measurements-by-reading [{:keys [commander querier]} handlers]
  :allowed-methods #{:get}
  :available-media-types #{"application/json"}
  :authorized? (authorized? querier :measurement)

  :handle-ok (fn [{{{:keys [device-id sensor-type timestamp]} :route-params} :request {mime :media-type} :representation}]
               (let [measurement (first (items querier :measurement {:device-id device-id :type sensor-type :timestamp timestamp}))]
                 (->> measurement downcast-to-json camelify encode))))

(defresource datasets [{:keys [commander querier]} handlers]
  :allowed-methods #{:get :post}
  :available-media-types #{"application/edn"}
  :authorized? (authorized? querier :datasets)
  :post! (fn [{{body :body {:keys [entity-id]} :route-params} :request}]
           (upsert! commander :dataset (-> body read-edn-body ->shallow-kebab-map)))

  :handle-ok (fn [{{{:keys [entity-id]} :route-params} :request {mime :media-type} :representation}]
               (let [data-sets (items querier :data-set-id {:entity-id entity-id})]
                 (case mime
                   "text/html" (html
                                [:body
                                 [:table
                                  (for [ds data-sets]
                                    [:tr
                                     [:td
                                      [:pre (with-out-str (pprint ds))]]])]])
                   "application/edn" (pr-str data-sets)))))

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
                 :sensor-metadata (sensor-metadata opts p)
                 :measurements (measurements opts p)
                 :measurement (measurements-by-reading opts p)
                 :measurement-slice (measurements-slice opts p)
                 :sensors-by-property (sensors-by-property opts p)
                 :datasets (datasets opts p)})))


(def sha1-regex #"[0-9a-f-]+")

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
         [["entities/" [sha1-regex :entity-id] "/datasets"] (:datasets handlers)]
         [["entities/" [sha1-regex :entity-id] "/sensors"] (:sensors-by-property handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices"] (:devices handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id]] (:device handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/metadata"] (:sensor-metadata handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements"] (:measurements handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements/" :reading-type] (:measurement-slice handlers)]
         [["entities/" [sha1-regex :entity-id] "/devices/" [sha1-regex :device-id] "/measurements/" :reading-type "/" :timestamp] (:measurement handlers)]
         ]
        wrap-cookies)])


#_(deftype ApiServiceV3 [config]
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
