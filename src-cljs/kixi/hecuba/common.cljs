(ns kixi.hecuba.common
  (:require [clojure.string :as str])
  )

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
