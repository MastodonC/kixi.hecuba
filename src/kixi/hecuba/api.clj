(ns kixi.hecuba.api
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [cheshire.core :refer (decode decode-stream encode)]
            [cheshire.generate :refer (add-encoder)]
            [clojure.edn :as edn]
            [cemerick.friend :as friend]
            [clojure.tools.logging :as log]
            [hiccup.core :refer (html)]
            [clojure.string :as string]
            [clojure.pprint :refer (pprint)]))

(defprotocol Body
  (read-edn-body [body])
  (read-json-body [body])
  (read-csv-body [body]))

(extend-protocol Body
  String
  (read-edn-body  [body] (edn/read-string body))
  (read-json-body [body] (decode body keyword))
  (read-csv-body  [body] (csv/read-csv body))
  org.httpkit.BytesInputStream
  (read-edn-body  [body] (io! (edn/read (java.io.PushbackReader. (io/reader body)))))
  (read-json-body [body] (io! (decode-stream (io/reader body) keyword)))
  (read-csv-body  [body] (io! (csv/read-csv (io/reader body)))))

(add-encoder java.util.UUID
             (fn [c jsonGenerator]
               (.writeString jsonGenerator (str c))))
(add-encoder java.util.Date
             (fn [c jsonGenerator]
               (.writeString jsonGenerator (str c))))
(add-encoder clojure.lang.Keyword
             (fn [c jsonGenerator]
               (.writeString jsonGenerator (name c))))

;; FIXME these update-stringified-list don't work in the expected way all the time.
;; in particular if the selector points to a non existant (nested) attribute.
(defn update-stringified-list [body selector]
  (update-in body
             [selector]
             #(when % (map encode %))))

(defn update-stringified-lists [body selectors]
  (let [extant-selectors (keep (partial get-in body) [selectors])]
    (reduce update-stringified-list body extant-selectors)))

(defn stringify-values [m]
  (into {} (for [[k v] m] [k (str v)])))

(defn authorized? [store]
  (fn [ctx]
    (let [friend-id (-> ctx :request :session ::friend/identity)]
      (log/debugf "Friend ID in authorized?: %s" friend-id)
      true)))

(defn allowed? [store]
  (fn [ctx]
    (let [friend-id (-> ctx :request :session ::friend/identity)]
      (log/debugf "Friend ID in allowed?: %s" friend-id)
      true)))

(defmulti decode-body :content-type :default "application/json")

(defmethod decode-body "application/json" [{body :body}] (some-> body read-json-body))
(defmethod decode-body "application/edn"  [{body :body}] (some-> body read-edn-body))
(defmethod decode-body "text/csv"         [{body :body}] (some-> body read-csv-body))

(defmulti render-items (comp :media-type :representation) :default :unknown)

(defmethod render-items :unknown [_ items]
  ;; If content type is unknown return it to liberator unchanged and
  ;; liberator may render it
  items)

(defmethod render-items "application/html" [_ items]
  (let [fields (remove #{:href :type :parent}
                       (distinct (mapcat keys items)))]
    (let [DEBUG false]
      (html [:body
             [:h2 "Fields"]
             [:ul (for [k fields] [:li (name k)])]
             [:h2 "Items"]
             [:table
              [:thead
               [:tr
                [:th "Name"]
                (for [k fields] [:th (string/replace k "_" " ")])
                (when DEBUG [:th "Debug"])]]
              [:tbody
               (for [p items]
                 [:tr
                  [:td [:a {:href (:href p)} (:name p)]]
                  (for [k fields] [:td (let [d (k p)] (if (coll? d) (apply str (interpose ", " d)) (str d)))])
                  (when DEBUG [:td (pr-str p)])])]]]))))

(defmethod render-items "application/edn" [_ items]
  (pr-str (vec items)))

(defmethod render-items "application/json" [_ items]
  (encode items))

(defmethod render-items "text/csv" [_ items]
  (if (map? (first items))
    (let [headers (into [] (map name (keys (first items))))]
      (with-out-str
        (csv/write-csv *out* [headers] :newline :cr+lf :separator \,)
        (csv/write-csv *out* (map vals items) :newline :cr+lf :separator \,)))
    (do
      (with-out-str
        (csv/write-csv *out* items :newline :cr+lf :separator \,)))))

(defmulti render-item (fn [ctx & _] (get-in ctx [:representation :media-type])) :default :unknown)

(defmethod render-item :unknown render-item-unknown [ctx item]
  ;; If content type is unknown return it to liberator unchanged and
  ;; liberator may render it
  (log/error "RENDERING UNKNOWN!!!" ctx)
  item)

(defmethod render-item "application/html" render-item-application-html [_ item]
  (html
   [:body
    [:h1 (:name item)]
    [:pre (with-out-str
            (pprint item))]]))

(defmethod render-item "application/edn" render-item-application-edn [_ item] (pr-str item))

(defmethod render-item "application/json" render-item-application-json [_ item] (encode item))

(defmethod render-item "text/csv" render-item-text-csv [_ item]
  (let [headers ["name" "value"]]
    (with-out-str
      (csv/write-csv *out* [headers] :newline :cr+lf :separator \,)
      (csv/write-csv *out* item :newline :cr+lf :separator \,))))

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

(defn request-method-from-context [& args]
  (let [ctx (last args)]
    (get-in ctx [:request :request-method])))

(defn media-type-from-context [& args]
  (some-> (last args)
      (get-in [:representation :media-type])
      (string/split #";\s*")
      first))

(defn content-type-from-context
  "Returns the first part of the content type."
  [& args]
  (some-> (last args)
          (get-in [:request :content-type])
          (string/split #";\s*")
          first))

(defn enrich-media-uris [entity file-bucket key]
  (let [path->uri (fn [x]
                    (-> x
                         (dissoc :path)
                         (assoc :uri (str "https://" file-bucket ".s3.amazonaws.com/" (:path x)))))]
    (update-in entity [key] #(mapv path->uri %))))

(defn maybe-representation-override-in-url [ctx]
  (when (= "csv" (get-in ctx [:request :query-params "type"]))
    (assoc-in ctx [:representation :media-type] "text/csv")))

(defn headers-content-disposition [filename]
  {"Content-Disposition" (str "attachment; filename=" filename)})

;; from https://github.com/weavejester/medley/blob/master/src/medley/core.cljx Thx @weavejester
(defn dissoc-in
  "Dissociate a value in a nested assocative structure, identified by a sequence
of keys. Any collections left empty by the operation will be dissociated from
their containing structures."
  [m ks]
  (if-let [[k & ks] (seq ks)]
    (if (seq ks)
      (let [v (dissoc-in (get m k) ks)]
        (if (empty? v)
          (dissoc m k)
          (assoc m k v)))
      (dissoc m k))
    m))
