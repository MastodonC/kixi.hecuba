(ns kixi.hecuba.common
  (:require [clojure.string   :as str]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [goog.userAgent :as agent]
            [om.core :as om :include-macros true]
            [ajax.core :refer (POST PUT transform-opts ajax-request edn-format)]))

(when (not agent/IE)
  (enable-console-print!))

(defn find-first [pred coll]
  (first (filter pred coll)))

(defn find-first-index [pred coll]
  (first (keep-indexed #(when (pred %2) %1) coll)))

(defn index-of
  "Find index of the first occurrence of v in s"
  [v s]
  (find-first-index #(= v %) s))

(defn map-replace
  "Take a string template containing :keyword elements and replace those keywords
   with the value corresponding to the looking of that keyword in the supplied m

   e.g.

   (map-replace \"/foo/:k1/bar/:k2\" {:k1 \"10\" :k2 \"20\"})
   ;=> \"/foo/10/bar/20\""
  [template m]
  (let [s (reduce-kv
           (fn [ret k v]
             (str/replace ret
                          (str k)
                          v))
           template m)]
    (when (not (index-of \: s)) s)))

(defn interval
  [start-date end-date]
  (let [formatter (tf/formatter "yyyy-MM-dd HH:mm:ss")
        start     (tf/parse formatter start-date)
        end       (tf/parse formatter end-date)
        interval  (t/in-minutes (t/interval start end))]
    (cond
     (<= interval 1440) :raw
     (and (> interval 1440) (< interval 20160)) :hourly_rollups
     (>= interval 20160) :daily_rollups)))

(defmulti unparse-date (fn [timestamp f] (type timestamp)))

(defmethod unparse-date js/Date [timestamp f]
  (when-not (nil? timestamp)
    (let [date (tc/from-date timestamp)]
      (tf/unparse (tf/formatter f) date))))

(defmethod unparse-date js/String [timestamp f]
  (when-not (nil? timestamp)
    (let [parsed (tf/parse (tf/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ") timestamp)
          date   (tc/to-date parsed)]
      (unparse-date date f))))

(defmethod unparse-date :default [timestamp f]
  "")

(defn unparse-date-str [timestamp f]
  (when-not (nil? timestamp)
    (let [parsed (tf/parse (tf/formatter "yyyy-MM-dd'T'HH:mm:ss.SSSZ") timestamp)
          date   (tc/to-date parsed)]
      (unparse-date date f))))

(defn log [& msgs]
  (when (or (and agent/GECKO
                 (agent/isVersionOrHigher 30))
            (and agent/WEBKIT
                 (agent/isVersionOrHigher 537)))
    (apply println msgs)))

;; our banner is 50px so we need to tweak the scrolling
(defn fixed-scroll-to-element [element]
  (let [rect (-> (.getElementById js/document element)
                 .getBoundingClientRect)
        top (.-top rect)]
    (.scrollBy js/window 0 (- top 50))))

(defn scroll-to-element [element]
  (-> (.getElementById js/document element)
      .scrollIntoView))


(defn row-for [{:keys [selected data]}]
  (find-first #(= (:id %) selected) data))

(defn title-for [cursor & {:keys [title-key] :or {title-key :slug}}]
  (let [row (row-for cursor)]
    (get-in row (if (vector? title-key) title-key (vector title-key)))))

(defn now->str []
  (tf/unparse (tf/formatter "yyyy-MM-dd HH:mm:ss") (t/now)))

(defn post-resource
  ([url resource handler]
    (post-resource url resource handler
                   (fn [{:keys [status status-text]}]
                     (log "status: " status "status-text: " status-text))))
  ([url resource handler error-handler]
    (when resource
      (POST url {:content-type "application/json"
                 :handler handler
                 :error-handler error-handler
                 :params resource}))))

(defn put-resource
  ([url resource handler]
    (put-resource url resource handler
                  (fn [{:keys [status status-text]}]
                    (log "status: " status "status-text: " status-text))))
  ([url resource handler error-handler]
    (when resource
      (PUT url {:content-type "application/json"
                :handler handler
                :error-handler error-handler
                :params resource}))))

;; FIXME: Our own version until we migrate to the latest cljs-ajax
(defn DELETE
  "accepts the URI and an optional map of options, options include:
  :handler - the handler function for successful operation
             should accept a single parameter which is the deserialized
             response
  :error-handler - the handler function for errors, should accept a map
                   with keys :status and :status-text
  :format - the format for the request
  :response-format - the format for the response
  :params - a map of parameters that will be sent with the request"
  [uri & [opts]]
  (ajax-request uri "DELETE" (transform-opts opts)))

(defn delete-resource
  ([url handler]
     (delete-resource url handler
                      (fn [{:keys [status status-text]}]
                        (log "status: " status "status-text: " status-text))))
  ([url handler error-handler]
     (when (seq url)
       (DELETE url {:content-type "application/edn"
                    :format (edn-format)
                    :handler handler
                    :error-handler error-handler}))))

(defn location-col [location]
  (let [{:keys [name longitude latitude]} location]
    [:div
     (when name [:p "Name: " name])
     (when latitude [:p "Latitude: " latitude])
     (when longitude [:p "Longitude: " longitude])]))

(defn assoc-if [m k v]
  (if v
    (assoc m k v)
    m))

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(defmulti tech-icon (fn [[k v]] k))
(defmethod tech-icon :ventilation_systems [[_ v]]
  (when v
    [:img {:src "/images/icons/mechanical_ventilation_with_heat_recovery.png"}]))
(defmethod tech-icon :photovoltaics [[_ v]]
  (when v
    [:img {:src "/images/icons/solar_pv.png"}]))
(defmethod tech-icon :solar_thermals [[_ v]]
  (when v
    [:img {:src "/images/icons/solar_thermal.png" }]))
(defmethod tech-icon :wind_turbines [[_ v]]
  (when v
    [:img {:src "/images/icons/wind_turbine.png"}]))
(defmethod tech-icon :small_hydros [[_ v]]
  (when v
    [:img {:src "/images/icons/hydroelectricity.png"}]))
(defmethod tech-icon :heat_pumps [[_ v]]
  (when v
    [:img {:src "/images/icons/air_source_heat_pump.png"}]))
(defmethod tech-icon :chps [[_ v]]
  (when v
    [:img {:src "/images/icons/micro_chp.png"}]))
(defmethod tech-icon :solid_wall_insulation [[_ v]]
  (when v
    [:img {:src "/images/icons/solid_wall_insulation.png"}]))
(defmethod tech-icon :cavity_wall_insulation [[_ v]]
  (when v
    [:img {:src "/images/icons/cavity_wall_insulation.png"}]))
