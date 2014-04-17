(ns kixi.hecuba.webutil
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [cheshire.core :refer (decode decode-stream encode)]
   [camel-snake-kebab :as csk]
   [hiccup.core :refer (html)]
   [kixi.hecuba.security :as sec]
   [clojure.string :as string]
   [clojure.pprint :refer (pprint)]
   [clojure.walk :refer (postwalk)]
   [liberator.core :as liberator]))

(defprotocol Body
  (read-edn-body [body])
  (read-json-body [body]))

(extend-protocol Body
  String
  (read-edn-body [body] (edn/read-string body))
  (read-json-body [body] (decode body keyword))
  org.httpkit.BytesInputStream
  (read-edn-body [body] (io! (edn/read (java.io.PushbackReader. (io/reader body)))))
  (read-json-body [body] (io! (decode-stream (io/reader body)))))

(defn uuid [] (java.util.UUID/randomUUID))

(def sha1-regex #"[0-9a-z-]+")

(defn stringify-values [m]
  (into {} (for [[k v] m] [k (str v)])))

(defn authorized? [querier typ]
  (fn [{{route-params :route-params :as req} :request}]
    (or
     (sec/authorized-with-basic-auth? req querier)
     (sec/authorized-with-cookie? req querier))))

;; Note: whether we need to stringify the values, I'm not sure.
(defn ->shallow-kebab-map
  "Turn the keys of the given map into kebab-case keywords."
  [m]
  (reduce-kv (fn [s k v] (conj s [(csk/->kebab-case-keyword k) v])) {} m))

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
     (map? %) (reduce-kv (fn [s k v] (conj s [(csk/->camelCaseString k) v])) {} %)
     :otherwise %)
   x))

(defmulti decode-body :content-type :default "application/json")

(defmethod decode-body "application/json" [{body :body}] (some-> body read-json-body ->shallow-kebab-map))
(defmethod decode-body "application/edn" [{body :body}] (some-> body read-edn-body ->shallow-kebab-map))

(defmulti render-items :content-type :default :unknown)
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

(defmethod render-items "application/edn" [_ items]
  (pr-str (vec items)))

(defmethod render-items "application/json" [_ items]
  (map #(-> %
             downcast-to-json
             camelify
             encode) items))

(defmulti render-item :content-type :default :unknown)
(defmethod render-item :unknown [_ item]
   ;; If content type is unknown return it to liberator unchanged and
  ;; liberator may render it
  item)

(defmethod render-item "application/html" [_ item]
  (html
   [:body
    [:h1 (:name item)]
    [:pre (with-out-str
            (pprint item))]]))

(defmethod render-item "application/edn" [_ item] (pr-str item))

(defmethod render-item "application/json" [_ item] (-> item downcast-to-json camelify encode))

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

(defn get-month-partition-key
  "Returns integer representation of year and month from java.util.Date"
  [t] (Integer/parseInt (.format (java.text.SimpleDateFormat. "yyyyMM") t)))

(defn get-year-partition-key
  "Returns integer representation of year from java.util.Date"
  [t] (Integer/parseInt (.format (java.text.SimpleDateFormat. "yyyy") t)))

(defn db-timestamp
  "Returns java.util.Date from String timestamp."
  [t] (.parse (java.text.SimpleDateFormat.  "yyyy-MM-dd'T'HH:mm:ss") t))

(defn routes-from [ctx]
  (get-in ctx [:request :modular.bidi/routes]))
