(ns kixi.hecuba.cljs
  (:require
   [bidi.bidi :refer (->Files)]
   [modular.bidi :refer (new-bidi-routes)])
  )

(defn make-routes [config]
  (let [output-dir (:output-dir config)]
    ["" (->Files {:dir "target/cljs"
                  :mime-types {"map" "application/javascript"}})]))

(defn new-cljs-routes [config]
  (-> config make-routes (new-bidi-routes :context "/cljs/")))
