(ns thi.ng.geom.core.utils
  #?(:cljs
     (:require-macros
      [thi.ng.math.macros :as mm]))
  (:require
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v :refer [vec2 vec3 V2 V3]]
   [thi.ng.dstruct.core :as d]
   [thi.ng.math.core :as m :refer [*eps*]]
   [clojure.core.reducers :as r]
   #?(:clj [thi.ng.math.macros :as mm])))

(declare tri-area3)

(defn closest-point-coeff
  [p a b]
  (let [d (g/- b a)]
    (/ (g/dot (g/- p a) d) (g/mag-squared d))))

(defn closest-point-on-line
  [p a b]
  (g/mix a b (closest-point-coeff p a b)))

(defn closest-point-on-segment
  [p a b]
  (let [t (closest-point-coeff p a b)]
    (if (<= t 0.0) a (if (>= t 1.0) b (g/mix a b t)))))

(defn closest-point-on-segments
  [p segments]
  (transduce
   (map #(apply closest-point-on-segment p %))
   (completing
    (fn [a q]
      (let [d' (g/dist-squared p q)]
        (if (< d' (a 1)) [q d'] a))))
   [nil m/INF+]
   segments))

(defn closest-line-between
  [a1 b1 a2 b2]
  (let [p43 (g/- b2 a2)]
    (if-not (m/delta= V3 p43 *eps*)
      (let [p21 (g/- b1 a1)]
        (if-not (m/delta= V3 p21 *eps*)
          (let [p13   (g/- a1 a2)
                d1343 (g/dot p13 p43)
                d4321 (g/dot p43 p21)
                d1321 (g/dot p13 p21)
                d4343 (g/dot p43 p43)
                d2121 (g/dot p21 p21)
                d     (mm/msub d2121 d4343 d4321 d4321)]
            (if (m/delta= 0.0 d *eps*)
              {:type :parallel}
              (let [n  (mm/msub d1343 d4321 d1321 d4343)
                    ua (/ n d)
                    ub (/ (mm/madd d4321 ua d1343) d4343)]
                {:type :intersect
                 :a    (g/madd p21 ua a1)
                 :b    (g/madd p43 ub a2)
                 :ua   ua
                 :ub   ub}))))))))

(defn dist*
  [rf]
  (fn [c points]
    (->> points
         (transduce (map #(g/dist-squared c %)) rf)
         (Math/sqrt))))

(def min-dist (dist* min))
(def max-dist (dist* max))

(defn arc-length-index
  [points]
  (->> points
       (d/successive-nth 2)
       (transduce
        (map #(g/dist (% 0) (% 1)))
        (completing (fn [a d] (conj a (+ (peek a) d))))
        [0])))

(defn arc-length
  [points]
  (d/reduce-pairs + g/dist points))

(defn centroid
  [[x & xs :as coll]]
  (case (count coll)
    0 nil
    1 x
    2 (g/mix x (first xs))
    (let [s (/ 1.0 (count coll))
          f (fn [x _] (* x s))]
      (g/reduce-vector x + f xs))))

(defn center
  ([c' coll]
     (center (centroid coll) c' coll))
  ([c c' coll]
     (let [d (g/- c' c)] (mapv #(g/+ % d) coll))))

(defn scale-size
  ([s coll] (scale-size (centroid coll) s coll))
  ([c s coll] (mapv #(g/madd (g/- % c) s c) coll)))

(defn bounds*
  [zero [x & xs :as coll]]
  (let [c (count coll)]
    (cond
     (> c 1) (let [p (g/reduce-vector x min xs)
                   q (g/reduce-vector x max xs)]
               [p (g/- q p)])
     (= c 1) [x zero]
     :default nil)))

(defn bounding-rect
  [points]
  (bounds* V2 points))

(defn bounding-box
  [points]
  (bounds* V3 points))

(defn radial-bounds
  [ctor c r-or-points]
  [(ctor c)
   (if (coll? r-or-points)
     (max-dist c r-or-points)
     r-or-points)])

(defn axis-bounds
  [axis coll]
  (let [xs (mapv #(nth % axis) coll)]
    [(reduce min xs) (reduce max xs)]))

(defn axis-range
  [axis coll]
  (- (apply - (axis-bounds axis coll))))

(defn delta-contains
  [points p eps]
  (some #(m/delta= p % eps) points))

(defn from-barycentric
  [points weights]
  (reduce g/+ (map g/* points weights)))

(defn point-at*
  [points idx total t i]
  (let [ct (* t total)
        i (int (loop [i i] (if (>= ct (idx i)) (recur (inc i)) i)))
        i1 (dec i)
        pi (idx i1)]
    [(g/mix (nth points i1) (nth points i)
            (mm/subdiv ct pi (idx i) pi))
     i]))

(defn point-at
  ([t points] (point-at t points nil))
  ([t points idx]
     (when (m/in-range? 0.0 1.0 t)
       (let [n (count points)
             t (double t)]
         (cond
          (== 0 n) nil
          (== 1 n) (first points)
          (== 1.0 t) (last points)
          :default
          (let [idx (or idx (arc-length-index points))]
            (first (point-at* points idx (peek idx) t 1))))))))

(defn sample-uniform*
  [udist include-last? points]
  (let [idx (arc-length-index points)
        total (peek idx)
        delta (/ udist total)
        samples (loop [t 0.0, i 1, acc []]
                  (if (< t 1.0)
                    (let [[p i] (point-at* points idx total t i)]
                      (recur (+ t delta) (int i) (conj acc p)))
                    acc))]
    (if include-last?
      (conj samples (last points))
      samples)))

(defn sample-uniform
  [udist include-last? points]
  (let [n (count points)]
    (cond
     (== 0 n) nil
     (== 1 n) [(first points)]
     :default (sample-uniform* udist include-last? points))))

(defn sample-segment-with-res
  [a b res include-b?]
  (let [ls (for [t (m/norm-range res)] (g/mix a b t))]
    (if include-b? ls (butlast ls))))

(defn map-bilinear
  "Takes a seq of 4 points in ccw order and 2D vector of normalized UV
  coordinates. Applies bilinear interpolation to compute point within
  the rect: U is coord along AB/DC edge, V along BC/AD"
  [[a b c d] [u v]]
  (g/mix a b d c u v))
(defn map-trilinear
  "Takes a seq of 8 points defining a cuboid and vector of normalized
  UVW coordinates. Applies trilinear interpolation to compute point
  within the cuboid: U is coord along AD/BC edge, V along AE/BF, W
  along AB/DC (see above diagram)"
  [[a b c d e f g h] [u v w]]
  (g/mix (g/mix a d e h u v) (g/mix b c f g u v) w))

(defn tessellate-with-point
  ([points] (tessellate-with-point (centroid points) points))
  ([c points]
     (->> [(first points)]
          (concat points)
          (d/successive-nth 2)
          (mapv #(vector c (% 0) (% 1))))))

(defn tessellate-with-first
  [points]
  (if (> (count points) 3)
    (let [[v0 & more] points]
      (mapv (fn [[a b]] [v0 a b]) (d/successive-nth 2 more)))
    [points]))

(defn tessellate-tri-with-midpoints
  [[a b c]]
  (let [ab (g/mix a b)
        bc (g/mix b c)
        ca (g/mix c a)]
    [[a ab ca] [ab b bc] [bc c ca] [ab bc ca]]))

(defn tessellate-3
  [points]
  (condp = (count points)
    3 [points]
    4 (let [[a b c d] points] [[a b c] [a c d]])
    (tessellate-with-point points)))

(defn tessellate-max4
  [points]
  (if (<= (count points) 4)
    [points]
    (tessellate-with-point points)))

(defn ortho-normal
  ([[a b c]] (g/normalize (g/cross (g/- b a) (g/- c a))))
  ([a b] (g/normalize (g/cross a b)))
  ([a b c] (g/normalize (g/cross (g/- b a) (g/- c a)))))

(defn norm-sign2
  [[ax ay] [bx by] [cx cy]]
  (- (mm/subm bx ax cy ay) (mm/subm cx ax by ay)))

(defn norm-sign3
  [a b c] (g/mag (g/cross (g/- b a) (g/- c a))))

(defn tri-area2
  [a b c] (* 0.5 (norm-sign2 a b c)))

(defn tri-area3
  [a b c] (* 0.5 (norm-sign3 a b c)))

(defn clockwise2?
  [a b c] (neg? (norm-sign2 a b c)))

(defn clockwise3?
  [a b c n] (pos? (g/dot (g/cross (g/- b a) (g/- c a)) n)))

(defn triangle-barycentric-coords
  ([[a b c] p]
     (triangle-barycentric-coords a b c p (g/- b a) (g/- c a)))
  ([a b c p]
     (triangle-barycentric-coords a b c p (g/- b a) (g/- c a)))
  ([a b c p u v]
     (let [w (g/- p a)
           uu (g/mag-squared u)
           vv (g/mag-squared v)
           uv (g/dot u v)
           wu (g/dot w u)
           wv (g/dot w v)
           denom (/ 1.0 (mm/msub uv uv uu vv))
           s (* denom (mm/msub uv wv vv wu))
           t (* denom (mm/msub uv wu uu wv))]
       [(- 1.0 (+ s t)) s t])))

(defn point-in-triangle2?
  [p a b c]
  (if (clockwise2? a b c)
    (and (>= (norm-sign2 a c p) 0.0)
         (>= (norm-sign2 b a p) 0.0)
         (>= (norm-sign2 c b p) 0.0))
    (and (>= (norm-sign2 b c p) 0.0)
         (>= (norm-sign2 a b p) 0.0)
         (>= (norm-sign2 c a p) 0.0))))

(defn point-in-triangle3?
  [p a b c]
  (let [u (g/- b a)
        v (g/- c a)
        n (ortho-normal u v)
        cl (- (g/dot n p) (g/dot n a))]
    (if (m/delta= 0.0 cl)
      (let [[u v w] (triangle-barycentric-coords a b c p u v)]
        (and (>= u 0.0) (>= w 0.0) (m/in-range? 0.0 1.0 v))))))

(defn- tessellating-transducer
  [f]
  (comp
   (mapcat tessellate-with-first)
   (map f)))

(def ^:private area-xf
  (tessellating-transducer #(->> % (apply tri-area3) m/abs)))

(def ^:private volume-xf
  (tessellating-transducer #(g/dot (% 0) (g/cross (% 1) (% 2)))))

(defn total-area-3d
  [faces] (transduce area-xf + faces))

(defn total-volume
  [faces] (/ (transduce volume-xf + faces) 6.0))
