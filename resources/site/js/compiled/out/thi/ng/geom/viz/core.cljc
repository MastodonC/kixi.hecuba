(ns thi.ng.geom.viz.core
  (:require
   [thi.ng.geom.core :as g]
   [thi.ng.geom.core.vector :as v :refer [vec2 vec3]]
   [thi.ng.geom.core.utils :as gu]
   [thi.ng.geom.svg.core :as svg]
   [thi.ng.ndarray.core :as nd]
   [thi.ng.ndarray.contours :as contours]
   [thi.ng.math.core :as m]
   [thi.ng.strf.core :as f]))

(defn value-mapper
  [scale-x scale-y] (fn [[x y]] [(scale-x x) (scale-y y)]))

(defn value-transducer
  [{:keys [cull-domain cull-range scale-x scale-y project shape item-pos]}]
  (let [mapper   (value-mapper scale-x scale-y)
        item-pos (or item-pos identity)]
    (cond->       (map (juxt item-pos identity))
      cull-domain (comp (filter #(m/in-range? cull-domain (ffirst %))))
      :always     (comp (map (fn [[p i]] [(mapper p) i])))
      cull-range  (comp (filter #(m/in-range? cull-range (peek (first %)))))
      project     (comp (map (fn [[p i]] [(project p) i])))
      shape       (comp (map shape)))))

(defn process-points
  [{:keys [x-axis y-axis project]} {:keys [values item-pos shape]}]
  (let [[ry1 ry2] (:range y-axis)]
    (->> (if item-pos
           (sort-by (comp first item-pos) values)
           (sort-by first values))
         (sequence
          (value-transducer
           {:cull-domain (:domain x-axis)
            :cull-range  (if (< ry1 ry2) [ry1 ry2] [ry2 ry1])
            :item-pos    item-pos
            :scale-x     (:scale x-axis)
            :scale-y     (:scale y-axis)
            :project     project
            :shape       shape})))))
(defn points->path-segments
  [[p & more]] (reduce #(conj % [:L %2]) [[:M p]] more))

(defn polar-projection
  [origin]
  (let [o (vec2 origin)]
    (fn [[x y]] (g/+ o (g/as-cartesian (vec2 y x))))))

(defn value-formatter
  [prec]
  (let [fmt [(f/float prec)]]
    (fn [x] (f/format fmt x))))

(defn format-percent
  [x] (str (int (* x 100)) "%"))

(defn uniform-domain-points
  "Given a vector of domain bounds and a collection of data values
  (without domain position), produces a lazy-seq of 2-element vectors
  representing the values of the original coll uniformly spread over
  the full domain range, with each of the form: [domain-pos value]."
  [[d1 d2] values]
  (map
   (fn [t v] [(m/mix d1 d2 t) v])
   (m/norm-range (dec (count values)))
   values))
(def domain-bounds-x #(gu/axis-bounds 0 %))

(def domain-bounds-y #(gu/axis-bounds 1 %))

(def domain-bounds-z #(gu/axis-bounds 2 %))

(defn total-domain-bounds
  [f & colls]
  (transduce
   (map f)
   (completing (fn [[aa ab] [xa xb]] [(min aa xa) (max ab xb)]))
   [m/INF+ m/INF-] colls))
(defn value-domain-bounds
  [mat]
  (let [vals (seq mat)]
    [(reduce min vals) (reduce max vals)]))

(defn linear-scale
  [domain range]
  (fn [x] (m/map-interval x domain range)))
(defn log
  [base]
  (let [lb (Math/log base)]
    #(/ (cond
          (pos? %) (Math/log %)
          (neg? %) (- (Math/log (- %)))
          :else 0)
        lb)))

(defn log-scale
  [base [d1 d2 :as domain] [r1 r2 :as range]]
  (let [log* (log base)
        d1l  (log* d1)
        dr   (- (log* d2) d1l)]
    (fn [x] (m/mix r1 r2 (/ (- (log* x) d1l) dr)))))
(defn lens-scale
  [focus strength [d1 d2] [r1 r2]]
  (let [dr (- d2 d1)
        f  (/ (- focus d1) dr)]
    (fn [x] (m/mix-lens r1 r2 (/ (- x d1) dr) f strength))))

(defn axis-common*
  [{:keys [visible major-size minor-size format attribs label label-dist]
    :or {format (value-formatter 2), visible true
         major-size 10, minor-size 5}
    :as spec}]
  (assoc spec
         :visible    visible
         :major-size major-size
         :minor-size minor-size
         :format     format
         :attribs      (merge
                      {:stroke "black"}
                      attribs)
         :label      (merge
                      {:fill "black"
                       :stroke "none"
                       :font-family "Arial"
                       :font-size 10
                       :text-anchor "middle"}
                      label)
         :label-dist (or label-dist (+ 10 major-size))))
(defn lin-tick-marks
  [[d1 d2] delta]
  (let [dr (- d2 d1)
        d1' (m/roundto d1 delta)]
    (filter #(m/in-range? d1 d2 %) (range d1' (+ d2 delta) delta))))

(defn linear-axis
  [{:keys [domain range major minor] :as spec}]
  (let [major' (if major (lin-tick-marks domain major))
        minor' (if minor (lin-tick-marks domain minor))
        minor' (if (and major' minor')
                 (filter (complement (set major')) minor')
                 minor')]
    (-> spec
        (assoc
         :scale (linear-scale domain range)
         :major major'
         :minor minor')
        (axis-common*))))
(defn log-ticks-domain
  [base d1 d2]
  (let [log* (log base)] [(m/floor (log* d1)) (m/ceil (log* d2))]))

(defn log-tick-marks-major
  [base [d1 d2]]
  (let [[d1l d2l] (log-ticks-domain base d1 d2)]
    (->> (for [i (range d1l (inc d2l))]
           (if (>= i 0)
             (* (/ 1 base) (Math/pow base i))
             (* (/ 1 base) (- (Math/pow base (- i))))))
         (filter #(m/in-range? d1 d2 %)))))

(defn log-tick-marks-minor
  [base [d1 d2]]
  (let [[d1l d2l] (log-ticks-domain base d1 d2)
        ticks (if (== 2 base) [0.75] (range 2 base))]
    (->> (for [i (range d1l (inc d2l)) j ticks]
           (if (>= i 0)
             (* (/ j base) (Math/pow base i))
             (* (/ j base) (- (Math/pow base (- i))))))
         (filter #(m/in-range? d1 d2 %)))))

(defn log-axis
  [{:keys [base domain range] :or {base 10} :as spec}]
  (-> spec
      (assoc
       :scale (log-scale base domain range)
       :major (log-tick-marks-major base domain)
       :minor (log-tick-marks-minor base domain))
      (axis-common*)))
(defn lens-axis
  [{:keys [domain range focus strength major minor]
    :or {strength 0.5} :as spec}]
  (let [major' (if major (lin-tick-marks domain major))
        minor' (if minor (lin-tick-marks domain minor))
        minor' (if (and major' minor')
                 (filter (complement (set major')) minor')
                 minor')
        focus  (or focus (/ (apply + domain) 2.0))]
    (-> spec
        (assoc
         :scale    (lens-scale focus strength domain range)
         :major    major'
         :minor    minor'
         :focus    focus
         :strength strength)
        (axis-common*))))

(defn svg-triangle-up
  [w]
  (let [h (* w (Math/sin m/THIRD_PI))
        w (* 0.5 w)]
    (fn [[[x y]]] (svg/polygon [[(- x w) (+ y h)] [(+ x w) (+ y h)] [x y]]))))

(defn svg-triangle-down
  [w]
  (let [h (* w (Math/sin m/THIRD_PI))
        w (* 0.5 w)]
    (fn [[[x y]]] (svg/polygon [[(- x w) (- y h)] [(+ x w) (- y h)] [x y]]))))

(defn svg-square
  [r] (let [d (* r 2.0)] (fn [[[x y]]] (svg/rect [(- x r) (- y r)] d d))))

(defn labeled-rect-horizontal
  [{:keys [h r label fill min-width base-line]}]
  (let [r2 (* -2 r)
        h2 (* 0.5 h)]
    (fn [[[ax ay :as a] [bx :as b] item]]
      (svg/group
       {}
       (svg/rect
        [(- ax r) (- ay h2)] (- bx ax r2) h
        {:fill (fill item) :rx r :ry r})
       (if (< min-width (- bx ax))
         (svg/text [ax (+ base-line ay)] (label item)))))))

(defn svg-line-plot
  [v-spec d-spec]
  (svg/line-strip (map first (process-points v-spec d-spec)) (:attribs d-spec)))
(defn svg-area-plot
  [{:keys [y-axis project] :as v-spec} {:keys [res] :as d-spec}]
  (let [ry1     (first (:range y-axis))
        points  (map first (process-points (assoc v-spec :project vec2) d-spec))
        p       (vec2 (first (last points)) ry1)
        q       (vec2 (ffirst points) ry1)
        points  (concat points (map (partial g/mix p q) (m/norm-range (or res 1))))]
    (svg/polygon (map project points) (:attribs d-spec))))
(defn svg-radar-plot
  [v-spec {:keys [shape] :or {shape svg/polygon} :as d-spec}]
  (shape (map first (process-points v-spec d-spec)) (:attribs d-spec)))
(defn svg-radar-plot-minmax
  [v-spec
   {:keys [item-pos-min item-pos-max shape]
    :or   {shape #(svg/path (concat % [[:Z]] %2 [[:Z]]) %3)}
    :as   d-spec}]
  (let [min-points (->> (assoc d-spec :item-pos (or item-pos-min (fn [i] (take 2 i))))
                        (process-points v-spec)
                        (map first)
                        (points->path-segments))
        max-points (->> (assoc d-spec :item-pos (or item-pos-max (fn [i] [(first i) (nth i 2)])))
                        (process-points v-spec)
                        (map first)
                        (points->path-segments))]
    (shape max-points min-points (assoc (:attribs d-spec) :fill-rule "evenodd"))))
(defn svg-scatter-plot
  [v-spec {:keys [attribs shape] :as d-spec}]
  (->> (assoc d-spec :shape (or shape (fn [[p]] (svg/circle p 3))))
       (process-points v-spec)
       (apply svg/group attribs)))
(defn svg-bar-plot
  [{:keys [x-axis y-axis project] :or {project vec2}}
   {:keys [values attribs shape item-pos interleave offset bar-width]
    :or   {shape      (fn [a b _] (svg/line a b))
           item-pos   identity
           interleave 1
           bar-width  0
           offset     0}}]
  (let [domain  (:domain x-axis)
        base-y  ((:scale y-axis) (first (:domain y-axis)))
        mapper  (value-mapper (:scale x-axis) (:scale y-axis))
        offset  (+ (* -0.5 (* interleave bar-width)) (* (+ offset 0.5) bar-width))]
    (->> values
         (sequence
          (comp
           (map (juxt item-pos identity))
           (filter #(m/in-range? domain (ffirst %)))
           (map
            (fn [[p i]]
              (let [[ax ay] (mapper p)
                    ax (+ ax offset)]
                (shape (project [ax ay]) (project [ax base-y]) i))))))
         (svg/group attribs))))
(defn svg-heatmap
  [{:keys [x-axis y-axis project]}
   {:keys [matrix value-domain clamp palette palette-scale attribs shape]
    :or {value-domain [0.0 1.0]
         palette-scale linear-scale
         shape #(svg/polygon [%1 %2 %3 %4] {:fill %5})}
    :as d-spec}]
  (let [scale-x   (:scale x-axis)
        scale-y   (:scale y-axis)
        pmax      (dec (count palette))
        scale-v   (palette-scale value-domain [0 pmax])]
    (svg/group
     attribs
     (for [p (nd/position-seq matrix)
           :let [[y x] p
                 v (nd/get-at matrix y x)]
           :when (or clamp (m/in-range? value-domain v))]
       (shape
        (project [(scale-x x) (scale-y y)])
        (project [(scale-x (inc x)) (scale-y y)])
        (project [(scale-x (inc x)) (scale-y (inc y))])
        (project [(scale-x x) (scale-y (inc y))])
        (palette (m/clamp (int (scale-v v)) 0 pmax)))))))
(defn matrix-2d
  [w h values] (nd/ndarray :float32 values [h w]))

(defn contour-matrix
  [w h values]
  (contours/set-border-2d (matrix-2d w h values) -1e9))

(defn contour->svg
  [scale-x scale-y project]
  (fn [attribs contour]
    (let [contour (map (fn [[y x]] [(scale-x x) (scale-y y)]) contour)]
      (svg/polygon (map project contour) attribs))))

(defn svg-contour-plot
  [{:keys [x-axis y-axis project]}
   {:keys [matrix attribs levels palette palette-scale value-domain contour-attribs]
    :or   {value-domain    [0.0 1.0]
           palette         [[1 1 1]]
           palette-scale   linear-scale
           contour-attribs (constantly nil)}}]
  (let [pmax       (dec (count palette))
        scale-v    (palette-scale value-domain [0 pmax])
        contour-fn (contour->svg (:scale x-axis) (:scale y-axis) project)]
    (svg/group
     attribs
     (map
      (fn [iso]
        (let [c-attribs (contour-attribs (palette (m/clamp (int (scale-v iso)) 0 pmax)))]
          (svg/group
           {}
           (map
            (partial contour-fn c-attribs)
            (contours/find-contours-2d matrix iso)))))
      (sort levels)))))
(defn overlap? [[a b] [c d]] (and (<= a d) (>= b c)))

(defn compute-row-stacking
  [item-range coll]
  (reduce
   (fn [grid x]
     (let [r (item-range x)]
       (loop [[row & more] grid idx 0]
         (if (or (nil? row) (not (some #(overlap? r (item-range %)) row)))
           (update-in grid [idx] #(conj (or % []) x))
           (recur more (inc idx))))))
   [] coll))

(defn process-interval-row
  [item-range mapper [d1 d2]]
  (fn [i row]
    (map
     (fn [item]
       (let [[a b] (item-range item)]
         [(mapper [(max d1 a) i]) (mapper [(min d2 b) i]) item]))
     row)))

(defn svg-stacked-interval-plot
  [{:keys [x-axis y-axis]}
   {:keys [values attribs shape item-range offset]
    :or   {shape (fn [[a b]] (svg/line a b))
           item-range identity
           offset 0}}]
  (let [scale-x (:scale x-axis)
        scale-y (:scale y-axis)
        domain  (:domain x-axis)
        mapper  (value-mapper scale-x scale-y)]
    (->> values
         (filter #(overlap? domain (item-range %)))
         (sort-by (comp first item-range))
         (compute-row-stacking item-range)
         (mapcat (process-interval-row item-range mapper domain) (range offset 1e6))
         (map shape)
         (svg/group attribs))))


(defn svg-axis*
  [{:keys [major minor attribs label]} axis tick1-fn tick2-fn label-fn]
  (svg/group
   (merge {:stroke "#000"} attribs)
   (map tick1-fn major)
   (map tick2-fn minor)
   (svg/group (merge {:stroke "none"} label) (map label-fn major))
   axis))

(defn svg-x-axis-cartesian
  [{:keys [scale major-size minor-size label-dist pos format] [r1 r2] :range
    :as spec}]
  (svg-axis*
   spec (svg/line [r1 pos] [r2 pos])
   #(let [x (scale %)] (svg/line [x pos] [x (+ pos major-size)]))
   #(let [x (scale %)] (svg/line [x pos] [x (+ pos minor-size)]))
   #(let [x (scale %)] (svg/text [x (+ pos label-dist)] (format %)))))

(defn svg-y-axis-cartesian
  [{:keys [scale major-size minor-size label-dist pos format] [r1 r2] :range
    :as spec}]
  (svg-axis*
   spec (svg/line [pos r1] [pos r2])
   #(let [y (scale %)] (svg/line [pos y] [(- pos major-size) y]))
   #(let [y (scale %)] (svg/line [pos y] [(- pos minor-size) y]))
   #(let [y (scale %)] (svg/text [(- pos label-dist) y] (format %)))))
(defn select-ticks
  [axis minor?] (if minor? (concat (:minor axis) (:major axis)) (:major axis)))

(defn svg-axis-grid2d-cartesian
  [x-axis y-axis {:keys [attribs minor-x minor-y]}]
  (let [[x1 x2] (:range x-axis)
        [y1 y2] (:range y-axis)
        scale-x (:scale x-axis)
        scale-y (:scale y-axis)]
    (svg/group
     (merge {:stroke "#ccc" :stroke-dasharray "1 1"} attribs)
     (if (:visible x-axis)
       (map #(let [x (scale-x %)] (svg/line [x y1] [x y2])) (select-ticks x-axis minor-x)))
     (if (:visible y-axis)
       (map #(let [y (scale-y %)] (svg/line [x1 y] [x2 y])) (select-ticks y-axis minor-y))))))

(defn svg-plot2d-cartesian
  [{:keys [x-axis y-axis grid data] :as opts}]
  (let [opts (assoc opts :project vec2)]
    (svg/group
     {}
     (if grid (svg-axis-grid2d-cartesian x-axis y-axis grid))
     (map (fn [spec] ((:layout spec) opts spec)) data)
     (if (:visible x-axis) (svg-x-axis-cartesian x-axis))
     (if (:visible y-axis) (svg-y-axis-cartesian y-axis)))))
(defn svg-x-axis-polar
  [{{:keys [scale major-size minor-size label-dist pos format] [r1 r2] :range
     :or {format (value-formatter 2)}} :x-axis
     project :project o :origin :as spec}]
  (svg-axis*
   (:x-axis spec)
   (if (:circle spec)
     (svg/circle o pos {:fill "none"})
     (svg/arc o pos r1 r2 (> (m/abs-diff r1 r2) m/PI) true {:fill "none"}))
   #(let [x (scale %)]
      (svg/line (project [x pos]) (project [x (+ pos major-size)])))
   #(let [x (scale %)]
      (svg/line (project [x pos]) (project [x (+ pos minor-size)])))
   #(let [x (scale %)]
      (svg/text (project [x (+ pos label-dist)]) (format %) {:stroke "none"}))))

(defn svg-y-axis-polar
  [{{:keys [scale major-size minor-size label-dist pos format]
     [r1 r2] :range :or {format (value-formatter 2)}} :y-axis
     project :project :as spec}]
  (let [a (project [pos r1])
        b (project [pos r2])
        nl (g/normalize (g/normal (g/- a b)) label-dist)
        n1 (g/normalize nl major-size)
        n2 (g/normalize nl minor-size)]
    (svg-axis*
     (:y-axis spec) (svg/line a b)
     #(let [y (scale %) p (project [pos y])]
        (svg/line p (g/+ p n1)))
     #(let [y (scale %) p (project [pos y])]
        (svg/line p (g/+ p n2)))
     #(let [y (scale %) p (project [pos y])]
        (svg/text (g/+ p nl) (format %) {:stroke "none"})))))
(defn svg-axis-grid2d-polar
  [{:keys [x-axis y-axis origin circle project] {:keys [attribs minor-x minor-y]} :grid}]
  (let [[x1 x2] (:range x-axis)
        [y1 y2] (:range y-axis)
        scale-x (:scale x-axis)
        scale-y (:scale y-axis)
        great?  (> (m/abs-diff x1 x2) m/PI)]
    (svg/group
     (merge {:stroke "#ccc" :stroke-dasharray "1 1"} attribs)
     (if (:visible x-axis)
       (map
        #(let [x (scale-x %)] (svg/line (project [x y1]) (project [x y2])))
        (select-ticks x-axis minor-x)))
     (if (:visible y-axis)
       (map
        #(let [y (scale-y %)]
           (if circle
             (svg/circle origin y {:fill "none"})
             (svg/arc origin y x1 x2 great? true {:fill "none"})))
        (select-ticks y-axis minor-y))))))

(defn svg-plot2d-polar
  [{:keys [x-axis y-axis grid data origin] :as opts}]
  (let [opts (assoc opts :project (polar-projection origin))]
    (svg/group
     {}
     (if grid (svg-axis-grid2d-polar opts))
     (map (fn [spec] ((:layout spec) opts spec)) data)
     (if (:visible x-axis) (svg-x-axis-polar opts))
     (if (:visible y-axis) (svg-y-axis-polar opts)))))
