(ns kixi.hecuba.common
  (:require [clojure.string   :as str]
            [cljs-time.core   :as t]
            [cljs-time.format :as tf]
            [cljs-time.coerce :as tc]
            [goog.userAgent :as agent]
            [om.core :as om :include-macros true]))

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

(defn unparse-date [timestamp f]
  (when-not (nil? timestamp)
    (let [date (tc/from-date timestamp)]
      (tf/unparse (tf/formatter f) date))))

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

(defn error-row [data]
  [:div.row
   [:div.col-md-12.text-center
    [:p.lead {:style {:padding-top 30}}
     "There has been an error. Please contact " [:a {:href "mailto:support@mastodonc.com"} "support@mastodonc.com"]]
    [:p "Error Code: " (:error-status data) " Message: " (:error-text data)]]])

(defn no-data-row [data]
  [:div.row [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "No data available for this selection."]]])

(defn fetching-row [data]
  [:div.row [:div.col-md-12.text-center [:p.lead {:style {:padding-top 30}} "Fetching data for selection." ]]])

(defn handle-change [owner table key e]
  (let [value (.-value (.-target e))]
    (om/set-state! owner [table key] value)))

;; TOFIX probably should move to bootstrap ns
(defn text-input-control [owner data table key label]
  [:div.form-group
   [:label.control-label.col-md-2 {:for (name key)} label]
   [:div.col-md-10
    [:input {:defaultValue (get data key "")
             :on-change #(handle-change owner table key %1)
             :class "form-control"
             :type "text"}]]])

(defn static-text [data key label]
  [:div.form-group
   [:label.control-label.col-md-2 {:for (name key)} label]
   [:p {:class "form-control-static col-md-10"} (get data key "")]])
