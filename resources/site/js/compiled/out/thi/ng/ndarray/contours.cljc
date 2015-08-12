(ns thi.ng.ndarray.contours
  (:require
   [thi.ng.ndarray.core :as nd]
   #?(:cljs [thi.ng.typedarrays.core :as a])))

(defn level-crossing
  [offset a b level]
  (let [da (- a level)
        db (- b level)]
    (if-not (= (>= da 0.0) (>= db 0.0))
      (+ offset (+ 0.5 (* 0.5 (/ (+ da db) (- da db))))))))

(defn level-crossings1d
  [mat shape level]
  (for [x (range (dec (if (number? shape) shape (first shape))))
        :let [x' (level-crossing x (nd/get-at mat x) (nd/get-at mat (inc x)) level)]
        :when x']
    x'))

(defn level-crossings2d-x
  ([mat level]
   (level-crossings2d-x mat (nd/shape mat) level))
  ([mat [sy sx] level]
   (mapcat
    (fn [y] (map #(vector y %) (level-crossings1d (nd/pick mat y nil) sx level)))
    (range sy))))

(defn level-crossings2d-y
  ([mat level]
   (level-crossings2d-y mat (nd/shape mat) level))
  ([mat [sy sx] level]
   (mapcat
    (fn [x] (map #(vector % x) (level-crossings1d (nd/pick mat nil x) sy level)))
    (range sx))))

(defn level-crossings2d
  ([mat level]
   (level-crossings2d mat (nd/shape mat) level))
  ([mat shape level]
   (concat
    (level-crossings2d-x mat shape level)
    (level-crossings2d-y mat shape level))))

(defn level-crossings3d-x
  ([mat level]
   (level-crossings3d-x mat (nd/shape mat) level))
  ([mat [sz sy sx] level]
   (mapcat
    (fn [z] (map #(cons z %) (level-crossings2d-x (nd/pick mat z nil nil) [sy sx] level)))
    (range sz))))

(defn level-crossings3d-y
  ([mat level]
   (level-crossings3d-y mat (nd/shape mat) level))
  ([mat [sz sy sx] level]
   (mapcat
    (fn [z] (map #(cons z %) (level-crossings2d-y (nd/pick mat z nil nil) [sy sx] level)))
    (range sz))))

(defn level-crossings3d-z
  ([mat level]
   (level-crossings3d-z mat (nd/shape mat) level))
  ([mat [sz sy sx] level]
   (mapcat
    (fn [x] (map #(conj % x) (level-crossings2d-y (nd/pick mat nil nil x) [sz sy] level)))
    (range sx))))

(defn level-crossings3d
  ([mat level]
   (level-crossings3d mat (nd/shape mat) level))
  ([mat shape level]
   (concat
    (level-crossings3d-x mat shape level)
    (level-crossings3d-y mat shape level)
    (level-crossings3d-z mat shape level))))

(def edge-index-2d
  [nil [2 0] [1 0] [1 0]
   [0 0] nil [0 0] [0 0]
   [3 0] [2 0] nil [1 0]
   [3 0] [2 0] [3 0] nil])

(def next-edges-2d
  [[-1 0] [0 1] [1 0] [0 -1]])

(defn set-border-2d
  [mat x]
  (let [[h w] (nd/shape mat)
        h' (dec h)
        w' (dec w)
        l  (nd/pick mat nil 0)
        r  (nd/pick mat nil w')
        t  (nd/pick mat 0 nil)
        b  (nd/pick mat h' nil)]
    (loop [i w']
      (when (>= i 0)
        (nd/set-at t i x)
        (nd/set-at b i x)
        (recur (dec i))))
    (loop [i h']
      (when (>= i 0)
        (nd/set-at l i x)
        (nd/set-at r i x)
        (recur (dec i))))
    mat))

(defn encode-crossings-2d
  [src isoval]
  (let [out  (nd/ndarray :int8 (#?(:clj byte-array :cljs a/int8) (nd/size src)) (nd/shape src))
        iso? (fn [y x m] (if (< (nd/get-at src y x) isoval) m 0))]
    (loop [pos (nd/position-seq (nd/truncate-h src -1 -1))]
      (if pos
        (let [[y x] (first pos)
              x' (inc x)
              y' (inc y)]
          (nd/set-at
           out y x
           (-> (iso? y x 0x08)
               (bit-or (iso? y  x' 0x04))
               (bit-or (iso? y' x' 0x02))
               (bit-or (iso? y' x  0x01))))
          (recur (next pos)))
        out))))

(defn mean-cell-value-2d
  [src y x]
  (* (+ (+ (nd/get-at src y x) (nd/get-at src y (inc x)))
        (+ (nd/get-at src (inc y) x) (nd/get-at src (inc y) (inc x))))
     0.25))

(defn process-saddle5
  [src y x iso from]
  (if (> (mean-cell-value-2d src y x) iso)
    (if (== 3 from) [2 0x04] [0 0x01])
    (if (== 3 from) [0 0x0d] [2 0x07])))

(defn process-saddle10
  [src y x iso from]
  (if (> (mean-cell-value-2d src y x) iso)
    (if (== 0 from) [3 0x02] [1 0x08])
    (if (== 2 from) [3 0x0b] [1 0x0e])))

(defn mix2d
  [src y1 x1 y2 x2 iso]
  (let [a (nd/get-at src y1 x1)
        b (nd/get-at src y2 x2)]
    (if (== a b) 0 (/ (- a iso) (- a b)))))

(defn contour-vertex-2d
  [src y x to iso]
  (let [x' (inc x) y' (inc y)]
    (case (int to)
      0 [y (+ x (mix2d src y x y x' iso))]
      1 [(+ y (mix2d src y x' y' x' iso)) x']
      2 [y' (+ x (mix2d src y' x y' x' iso))]
      3 [(+ y (mix2d src y x y' x iso)) x]
      nil)))

(defn find-contours-2d
  [src isoval]
  (let [[h' w']  (nd/shape src)
        h'       (dec h')
        w'       (dec w')
        coded    (encode-crossings-2d src isoval)
        contours (volatile! (transient []))]
    (loop [pos  (nd/position-seq coded)
           curr (transient [])
           to   nil
           p    nil]
      (if pos
        (let [from to
              [y x] (if p p (first pos))]
          (if (or (>= x w') (>= y h'))
            (recur (next pos) curr to nil)
            (let [id         (nd/get-at coded y x)
                  [to clear] (case (int id)
                               5 (process-saddle5 src y x isoval from)
                               10 (process-saddle10 src y x isoval from)
                               (edge-index-2d (int id)))
                  curr       (if (and (nil? from) to (pos? (count curr)))
                               (do (vswap! contours conj! (persistent! curr))
                                   (transient []))
                               curr)]
              (when clear
                (nd/set-at coded y x clear))
              (if (and to (>= to 0))
                (let [vertex  (contour-vertex-2d src y x to isoval)
                      [oy ox] (next-edges-2d to)]
                  (recur (next pos) (conj! curr vertex) to [(+ y oy) (+ x ox)]))
                (recur (next pos) curr to nil)))))
        (persistent! (conj! @contours (persistent! curr)))))))
