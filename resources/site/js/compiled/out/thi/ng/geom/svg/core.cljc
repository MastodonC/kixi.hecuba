(ns thi.ng.geom.svg.core
  (:require
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.utils :as gu]
   [thi.ng.geom.core.vector :as v :refer [vec2]]
   [thi.ng.geom.core.matrix :as mat :refer [M32]]
   [thi.ng.dstruct.core :as d]
   [thi.ng.math.core :as m]
   [thi.ng.strf.core :as f]
   [thi.ng.color.core :as col]
   #?(:clj [hiccup.core :refer [html]])))

(def stroke-round {:stroke-linecap "round" :stroke-linejoin "round"})
(def xml-preamble "<?xml version=\"1.0\"?>\n")

(def ^:dynamic *ff* (f/float 2))
(def ^:dynamic *fmt-vec* (fn [p] (str (*ff* (first p)) "," (*ff* (nth p 1)))))
(def ^:dynamic *fmt-percent* (fn [x] (str (int (* x 100)) "%")))

(def ^:dynamic *fmt-matrix* ["matrix(" *ff* "," *ff* "," *ff* "," *ff* "," *ff* "," *ff* ")"])

(def point-seq-format2 [*fmt-vec* " " *fmt-vec*])
(def point-seq-format3 [*fmt-vec* " " *fmt-vec* " " *fmt-vec*])
(def point-seq-format4 [*fmt-vec* " " *fmt-vec* " " *fmt-vec* " " *fmt-vec*])

(defn point-seq-format
  [n]
  (case (int n)
    1 [*fmt-vec*]
    2 point-seq-format2
    3 point-seq-format3
    4 point-seq-format4
    (->> *fmt-vec*
         (repeat n)
         (interpose " "))))

(def path-segment-formats
  {:M ["M" *fmt-vec* " "]
   :m ["m" *fmt-vec* " "]
   :L ["L" *fmt-vec* " "]
   :l ["l" *fmt-vec* " "]
   :C ["C" *fmt-vec* " " *fmt-vec* " " *fmt-vec* " "]
   :c ["c" *fmt-vec* " " *fmt-vec* " " *fmt-vec* " "]
   :A ["A" *fmt-vec* " " *ff* " " str " " str " " *fmt-vec* " "]
   :a ["a" *fmt-vec* " " *ff* " " str " " str " " *fmt-vec* " "]
   :Z ["Z"]
   :z ["z"]})

(defprotocol PSVGConvert
  (as-svg [_ opts]))

#?(:clj
   (defn serialize
     ^String [svg] (str xml-preamble (html {:mode :xml} svg))))

#?(:clj
   (defn serialize-as-byte-array
     ^bytes [svg] (.getBytes (serialize svg) "UTF-8")))

(defn color-attrib
  [attribs id id2 f]
  (if-let [att (attribs id)]
    (if (string? att)
      attribs
      (-> attribs (dissoc id) (assoc id2 (apply f att))))
    attribs))
(defn matrix-attrib
  [attribs id]
  (if-let [mat (attribs id)]
    (if (string? mat)
      attribs
      (let [[a c e b d f] mat]
        (assoc attribs id (apply f/format *fmt-matrix* [a b c d e f]))))
    attribs))
(defn filter-attribs
  [attribs]
  (loop [acc (transient attribs), ks (keys attribs)]
    (if ks
      (recur
       (if (= "__" (subs (name (first ks)) 0 2)) (dissoc! acc (first ks)) acc)
       (next ks))
      (persistent! acc))))

(defn svg-attribs
  [attribs base]
  (if (seq attribs)
    (-> (filter-attribs attribs)
        (color-attrib :stroke :stroke col/rgba->css)
        (color-attrib :stroke-hsv :stroke col/hsva->css)
        (color-attrib :fill :fill col/rgba->css)
        (color-attrib :fill-hsv :fill col/hsva->css)
        (matrix-attrib :transform)
        (into base))
    base))

(defn svg
  [attribs & body]
  [:svg
   (svg-attribs
    attribs
    {"xmlns" "http://www.w3.org/2000/svg"
     "xmlns:xlink" "http://www.w3.org/1999/xlink"
     "version" "1.1"})
   body])

(defn defs
  [& defs]
  [:defs defs])

(defn gradient-stop
  [f [pos col]]
  (let [col (if (string? col) col (apply f col))]
    [:stop {:offset (*fmt-percent* pos) :stop-color col}]))

(defn linear-gradient-rgb
  [id attribs & stops]
  [:linearGradient
   (assoc attribs :id id)
   (map #(gradient-stop col/rgba->css %) stops)])

(defn radial-gradient-rgb
  [id attribs & stops]
  [:radialGradient
   (assoc attribs :id id)
   (map #(gradient-stop col/rgba->css %) stops)])

(defn linear-gradient-hsv
  [id attribs & stops]
  [:linearGradient
   (assoc attribs :id id)
   (map #(gradient-stop col/hsva->css %) stops)])

(defn radial-gradient-hsv
  [id attribs & stops]
  [:radialGradient
   (assoc attribs :id id)
   (map #(gradient-stop col/hsva->css %) stops)])

(defn group
  [attribs & body]
  [:g (svg-attribs attribs nil) body])

(defn path
  [segments & [attribs]]
  [:path
   (svg-attribs
    attribs
    {:d (apply f/format
               (mapcat (comp path-segment-formats first) segments)
               (mapcat rest segments))})])
(defn text
  [[x y] txt & [attribs]]
  [:text
   (svg-attribs attribs {:x (*ff* x) :y (*ff* y)})
   txt])

(defn circle
  [[x y] radius & [attribs]]
  [:circle
   (svg-attribs
    attribs
    {:cx (*ff* x) :cy (*ff* y) :r radius})])

(defn arc
  [center radius theta1 theta2 great? ccw? & [attribs]]
  (let [radius (vec2 radius)
        p (g/+ (vec2 center) (g/as-cartesian (vec2 (v/x radius) theta1)))
        q (g/+ (vec2 center) (g/as-cartesian (vec2 (v/y radius) theta2)))]
    (path [[:M p] [:A radius 0 (if great? 1 0) (if ccw? 1 0) q]] attribs)))

(defn rect
  [[x y] w h & [attribs]]
  [:rect
   (svg-attribs
    attribs
    {:x (*ff* x) :y (*ff* y) :width w :height h})])

(defn line
  [[ax ay] [bx by] & [attribs]]
  [:line
   (svg-attribs
    attribs
    {:x1 (*ff* ax) :y1 (*ff* ay) :x2 (*ff* bx) :y2 (*ff* by)})])

(defn line-decorated
  [p q start end & [attribs]]
  (list
   (line p q attribs)
   (if start (start q p 0 attribs))
   (if end   (end p q 0 attribs))))

(defn line-strip
  [points & [attribs]]
  [:polyline
   (svg-attribs
    attribs
    {:fill "none"
     :points (apply f/format (point-seq-format (count points)) points)})])

(defn line-strip-decorated
  [points start seg end & [attribs]]
  (let [n (dec (count points))]
    (list
     (line-strip points attribs)
     (if start (start (points 1) (points 0) 0 attribs))
     (if seg   (map-indexed (fn [i [p q]] (seg p q i attribs)) (d/successive-nth 2 points)))
     (if end   (end (points (dec n)) (peek points) n attribs)))))

(defn polygon
  [points & [attribs]]
  [:polygon
   (svg-attribs
    attribs
    {:points (apply f/format (point-seq-format (count points)) points)})])

(defn instance
  [id & [attribs]]
  [:use (svg-attribs attribs {"xlink:href" (str "#" id)})])

(defn arrow-head
  [len theta solid? & [opts]]
  (fn [p q idx & [attribs]]
    (let [q (vec2 q)
          d (g/normalize (g/- q p) len)]
      (list
       ((if solid? polygon line-strip)
        [(g/- q (g/rotate d (- theta))) q (g/- q (g/rotate d theta))]
          (merge attribs opts))))))

(defn line-label
  [& [{:keys [__rotate? __offset] :as opts}]]
  (let [opts (-> opts
                 (dissoc :__rotate? :__offset)
                 (update-in [:text-anchor] #(or % "middle")))]
    (fn [p q idx & [attribs]]
      (if-let [label (get-in attribs [:__label idx])]
        (let [p (vec2 p)
              m (g/+ (g/mix p q) __offset)
              opts (if __rotate?
                     (assoc opts
                       :transform (str "rotate("
                                       (m/degrees (g/heading (g/normal (g/- p q))))
                                       " " (m 0) " " (m 1) ")"))
                     opts)]
          (list (text m label (merge (dissoc attribs :__label) opts))))))))

(defn comp-decorators
  [& fns]
  (fn [p q idx & [attribs]]
    (reduce
     (fn [acc f] (concat acc (f p q idx attribs))) () fns)))
