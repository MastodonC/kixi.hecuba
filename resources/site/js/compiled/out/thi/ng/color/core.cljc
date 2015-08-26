(ns thi.ng.color.core
  #?(:cljs
  (:require-macros
   [thi.ng.math.macros :as mm]))
  (:require
   [thi.ng.math.core :as m :refer [PI TWO_PI]]
   [thi.ng.strf.core :as f]
   #?(:clj [thi.ng.math.macros :as mm])))

(def THIRD (/ 1.0 3))
(def TWO_THIRD (/ 2.0 3))
(def SIXTH (/ 1.0 6))
(def INV8BIT (/ 1.0 255))

(defn with-alpha
  [col a] (if a (conj col a) col))

(defn rgb->hsv
  ([rgb]
   (apply rgb->hsv rgb))
  ([r g b]
   (let [v (max r g b)
         d (- v (min r g b))
         s (if (zero? v) 0.0 (/ d v))
         h (if (zero? s)
             0.0
             (condp == v
               r (mm/subdiv g b d)
               g (+ 2.0 (mm/subdiv b r d))
               (+ 4.0 (mm/subdiv r g d))))
         h (/ h 6.0)]
     [(if (neg? h) (inc h) h) s v]))
  ([r g b a]
   (conj (rgb->hsv r g b) a)))

(defn hsv->rgb
  ([hsv]
   (apply hsv->rgb hsv))
  ([h s v]
   (if (m/delta= 0.0 s)
     [v v v]
     (let [h (rem (* h 6.0) 6.0)
           i (int h)
           f (- h i)
           p (* v (- 1.0 s))
           q (* v (- 1.0 (* s f)))
           t (* v (- 1.0 (mm/subm 1.0 f s)))]
       (case i
         0 [v t p]
         1 [q v p]
         2 [p v t]
         3 [p q v]
         4 [t p v]
         [v p q]))))
  ([h s v a]
   (conj (hsv->rgb h s v) a)))

(defn rgb->hsl
  ([rgb]
   (apply rgb->hsl rgb))
  ([r g b]
   (let [f1 (min r g b)
         f2 (max r g b)
         l  (mm/addm f1 f2 0.5)
         d  (- f2 f1)]
     (if (zero? d)
       [0.0 0.0 l]
       (let [s  (if (< l 0.5) (/ d (+ f1 f2)) (/ d (- (- 2.0 f2) f1)))
             d2 (* 0.5 d)
             dr (mm/adddiv (mm/subm f2 r SIXTH) d2 d)
             dg (mm/adddiv (mm/subm f2 g SIXTH) d2 d)
             db (mm/adddiv (mm/subm f2 b SIXTH) d2 d)
             h  (condp == f2
                  r (- db dg)
                  g (- (+ THIRD dr) db)
                  (- (+ TWO_THIRD dg) dr))
             h  (if (neg? h) (inc h) (if (>= h 1.0) (dec h) h))]
         [h s l]))))
  ([r g b a]
   (conj (rgb->hsl r g b) a)))

(defn- hsl-hue->rgb
  [f1 f2 h]
  (let [h (if (neg? h) (inc h) (if (>= h 1.0) (dec h) h))]
    (cond
      (< h SIXTH)     (m/mix f1 f2 (* 6.0 h))
      (< h 0.5)       f2
      (< h TWO_THIRD) (m/mix f1 f2 (mm/subm TWO_THIRD h 6.0))
      :else           f1)))

(defn hsl->rgb
  ([hsla]
   (apply hsl->rgb hsla))
  ([h s l]
   (if (zero? s)
     [l l l]
     (let [f2 (if (< l 0.5) (* l (inc s)) (- (+ l s) (* l s)))
           f1 (- (* 2.0 l) f2)]
       [(m/clamp (hsl-hue->rgb f1 f2 (+ h THIRD)) 0.0 1.0)
        (m/clamp (hsl-hue->rgb f1 f2 h) 0.0 1.0)
        (m/clamp (hsl-hue->rgb f1 f2 (- h THIRD)) 0.0 1.0)])))
  ([h s l a]
   (conj (hsl->rgb h s l) a)))

(defn hue->rgb
  [h]
  (let [h (* 6.0 h)]
    [(m/clamp (dec (m/abs (- h 3.0))) 0.0 1.0)
     (m/clamp (- 2.0 (m/abs (- h 2.0))) 0.0 1.0)
     (m/clamp (- 2.0 (m/abs (- h 4.0))) 0.0 1.0)]))

(defn rgb->hcv
  ([rgba]
   (apply rgb->hcv rgba))
  ([r g b]
   (let [[px py pz pw] (if (< g b) [b g -1.0 TWO_THIRD] [g b 0.0 (- THIRD)])
         [qx qy qz qw] (if (< r px) [px py pw r] [r py pz px])
         c (- qx (min qw qy))
         h (m/abs (+ (/ (- qw qy) (mm/madd 6.0 c 1e-10)) qz))]
     [(m/clamp h 0.0 1.0) (m/clamp c 0.0 1.0) (m/clamp qx 0.0 1.0)]))
  ([r g b a]
   (conj (rgb->hcv r g b) a)))

(defn rgb->hcy
  ([rgba]
   (apply rgb->hcy rgba))
  ([r g b]
   (let [[h c v] (rgb->hcv r g b)
         y (mm/madd 0.299 r 0.587 g 0.114 b)]
     (if (zero? c)
       [h c y]
       (let [[r' g' b'] (hue->rgb h)
             z (mm/madd 0.299 r' 0.587 g' 0.114 b')]
         (if (> (- y z) 1e-5)
           [h (m/clamp (* c (mm/subdiv 1.0 z 1.0 y)) 0.0 1.0) y]
           [h (m/clamp (* c (/ z y)) 0.0 1.0) y])))))
  ([r g b a]
   (conj (rgb->hcy r g b) a)))

(defn hcy->rgb
  ([hcya]
   (apply hcy->rgb hcya))
  ([h c y]
   (let [[r g b] (hue->rgb h)
         z (mm/madd 0.299 r 0.587 g 0.114 b)
         c' (if (< y z) (* c (/ y z)) (if (< z 1.0) (* c (mm/subdiv 1.0 y 1.0 z)) c))]
     [(m/clamp (mm/submadd r z c' y) 0.0 1.0)
      (m/clamp (mm/submadd g z c' y) 0.0 1.0)
      (m/clamp (mm/submadd b z c' y) 0.0 1.0)]))
  ([h c y a]
   (conj (hcy->rgb h c y) a)))

(defn ycbcr->rgb
  ([ycbcra]
   (apply ycbcr->rgb ycbcra))
  ([y cb cr]
   (let [cb' (- cb 0.5)
         cr' (- cr 0.5)]
     [(m/clamp (mm/madd cr' 1.402 y) 0.0 1.0)
      (m/clamp (- y (mm/madd cb' 0.34414 cr' 0.71414)) 0.0 1.0)
      (m/clamp (mm/madd cb' 1.772 y) 0.0 1.0)]))
  ([y cb cr a]
   (conj (ycbcr->rgb y cb cr) a)))

(defn rgb->ycbcr
  ([rgba]
   (apply rgb->ycbcr rgba))
  ([r g b]
   [(m/clamp (mm/madd 0.299 r 0.587 g 0.114 b) 0.0 1.0)
    (m/clamp (+ (- (- 0.5 (* 0.16874 r)) (* 0.33126 g)) (* 0.5 b)) 0.0 1.0)
    (m/clamp (- (- (+ 0.5 (* 0.5 r)) (* 0.418688 g)) (* 0.081312 b)) 0.0 1.0)])
  ([r g b a]
   (conj (rgb->ycbcr r g b) a)))

(defn rgb->yuv
  ([rgba]
   (apply rgb->yuv rgba))
  ([r g b]
   [(mm/madd 0.299 r 0.587 g 0.114 b)
    (mm/madd -0.1473 r -0.28886 g 0.436 b)
    (mm/madd 0.615 r -0.51499 g -0.10001 b)])
  ([r g b a]
   (conj (rgb->yuv r g b) a)))

(defn yuv->rgb
  ([yuva]
   (apply yuv->rgb yuva))
  ([y u v]
   [(m/clamp (mm/madd 1.13983 v y) 0.0 1.0)
    (m/clamp (- y (mm/madd 0.39465 u 0.5806 v)) 0.0 1.0)
    (m/clamp (mm/madd 2.03211 u y) 0.0 1.0)])
  ([y u v a]
   (conj (yuv->rgb y u v) a)))

(defn cmyk->rgb
  ([cmyk]
   (apply cmyk->rgb cmyk))
  ([c m y k]
   [(- 1.0 (min 1.0 (+ c k)))
    (- 1.0 (min 1.0 (+ m k)))
    (- 1.0 (min 1.0 (+ y k)))])
  ([c m y k a]
   (conj (cmyk->rgb c m y k) a)))

(defn rgb->cmyk
  ([rgb]
   (apply rgb->cmyk rgb))
  ([r g b]
   (let [c (- 1.0 r)
         m (- 1.0 g)
         y (- 1.0 b)
         k (min (min c m) y)]
     [(max (- c k) 0.0)
      (max (- m k) 0.0)
      (max (- y k) 0.0)
      (max k 0.0)]))
  ([r g b a]
   (conj (rgb->cmyk r g b) a)))

(defn- cie1931-gamma-correct
  [x]
  (m/clamp
   (if (< x 0.0031308)
     (* 12.92 x)
     (mm/msub 1.055 (Math/pow x (/ 2.4)) 0.055))
   0.0 1.0))

(defn cie1931->rgb
  ([xyz]
   (apply cie1931->rgb xyz))
  ([x y z]
   (mapv
    cie1931-gamma-correct
    [(mm/madd 3.2406 x -1.5372 y -0.4986 z)
     (mm/madd -0.9689 x 1.8758 y 0.0415 z)
     (mm/madd 0.0557 x -0.2040 y 1.0570 z)]))
  ([x y z a]
   (conj (cie1931->rgb x y z) a)))

(defn rgba->int
  ([rgba]
   (apply rgba->int rgba))
  ([r g b]
   (bit-or
    (bit-or
     (-> r (* 255) int (bit-shift-left 16))
     (-> g (* 255) int (bit-shift-left 8)))
    (-> b (* 255) int)))
  ([r g b a]
   (bit-or (rgba->int r g b) (bit-shift-left (int (* a 255)) 24))))

(defn int->rgba
  [int32]
  [(* INV8BIT (bit-and (bit-shift-right int32 16) 255))
   (* INV8BIT (bit-and (bit-shift-right int32 8) 255))
   (* INV8BIT (bit-and int32 255))
   (* INV8BIT (bit-and (unsigned-bit-shift-right int32 24) 255))])

(def ^:private hex6 ["#" (f/hex 6)])

(defn int->hex
  [i] (f/format hex6 (bit-and i 0xffffff)))

(defn rgba->css
  ([rgba] (apply rgba->css rgba))
  ([r g b]
   (f/format hex6 (rgba->int r g b)))
  ([r g b a]
   (if (< a 1.0)
     (let [r (* 255 r) g (* 255 g) b (* 255 b)]
       (str "rgba(" (int r) "," (int g) "," (int b) "," (max 0.0 a) ")"))
     (f/format hex6 (rgba->int r g b)))))

(defn hsva->css
  ([hsva]
   (apply hsva->css hsva))
  ([h s v]
   (apply rgba->css (hsv->rgb h s v)))
  ([h s v a]
   (apply rgba->css (conj (hsv->rgb h s v) a))))

(defn hsla->css
  ([hsla]
   (apply hsla->css hsla))
  ([h s l]
   (let [h (int (* h 360)) s (int (* s 100)) l (int (* l 100))]
     (str "hsl(" h "," s "," l ")")))
  ([h s l a]
   (let [h (int (* h 360)) s (int (* s 100)) l (int (* l 100))]
     (str "hsla(" h "," s "%," l "%," a ")"))))

(defn hex->rgba
  [hex]
  (let [hex (if (= \# (first hex)) (subs hex 1) hex)
        rgba (if (== 3 (count hex))
               (let [[r g b] hex]
                 (int->rgba (f/parse-int (str r r g g b b) 16 0)))
               (int->rgba (f/parse-int hex 16 0)))]
    (if (and rgba (< (count hex) 7))
      (assoc rgba 3 1.0)
      rgba)))

(defn- parse-channel-val
  [^String c]
  (if (pos? (.indexOf c "%"))
    (* 0.01 (f/parse-float (subs c 0 (dec (count c)))))
    (* INV8BIT (f/parse-int c 10 0))))

(defn css->rgba
  [css]
  (if (= \# (first css))
    (hex->rgba css)
    (let [[[_ mode a b c d :as col]] (re-seq #"(rgb|hsl)a?\((\d+%?),(\d+%?),(\d+%?),?([0-9\.]+)?\)" css)]
      (if mode
        (if (#{"rgb" "rgba"} mode)
          (conj (mapv parse-channel-val [a b c]) (f/parse-float d 1.0))
          (let [h (/ (f/parse-float a) 360.0)
                s (parse-channel-val b)
                l (parse-channel-val c)]
            (conj (hsl->rgb h s l) (f/parse-float d 1.0))))))))

(def red first)

(def green #(nth % 1))

(def blue #(nth % 2))

(def hue first)

(def saturation #(nth % 1))

(def brightness #(nth % 2))

(def lightness #(nth % 2))

(def cyan first)

(def magenta #(nth % 1))

(def yellow #(nth % 2))

(def black #(nth % 3))

(def alpha #(nth % (if (== 4 (count %)) 3 4) 1))

(def hues
  (zipmap
   [:red :orange :yellow :lime :green :teal :cyan :azure :blue :purple :magenta]
   (map #(/ % 360.0) (range 0 360 30))))

(def primary-hues
  (select-keys hues [:red :yellow :green :cyan :blue :magenta]))

(defn closest-hue
  ([h] (closest-hue h hues))
  ([h hues]
     (first
      (reduce
       (fn [[h' d'] [k v]]
         (let [d (min (m/abs-diff h v) (m/abs-diff (dec h) v))]
           (if (< d d') [k d] [h' d'])))
       [nil 1e6] hues))))

(defn hue-rgb
  [rgb] (first (apply rgb->hsv rgb)))

(defn saturation-rgb
  [[r g b]]
  (let [v (max r g b)
        d (- v (min r g b))]
    (if (zero? v) 0.0 (/ d v))))

(defn brightness-rgb
  [[r g b]] (max r g b))

(defn luminance-rgb
  [[r g b]] (mm/madd 0.299 r 0.587 g 0.114 b))

(defn rotate-hue-hsv
  "Returns new HSV color with its hue rotated by theta (in radians)."
  [[h s v] theta]
  (let [h (+ h (/ (rem theta TWO_PI) TWO_PI))]
    [(cond (neg? h) (inc h) (>= h 1.0) (dec h) :default h) s v]))

(defn adjust-saturation-hsv
  [hsv x]
  (update-in hsv [1] #(m/clamp (+ x %) 0.0 1.0)))

(defn adjust-brightness-hsv
  [hsv x]
  (update-in hsv [2] #(m/clamp (+ x %) 0.0 1.0)))

(defn rotate-hue-rgb
  [rgb theta] (apply hsv->rgb (rotate-hue-hsv (apply rgb->hsv rgb) theta)))

(defn adjust-saturation-rgb
  [rgb x]
  (let [[h s v] (apply rgb->hsv rgb)]
    (hsv->rgb h (m/clamp (+ x s) 0.0 1.0) v)))

(defn adjust-brightness-rgb
  [rgb x]
  (let [[h s v] (apply rgb->hsv rgb)]
    (hsv->rgb h s (m/clamp (+ x v) 0.0 1.0))))

(defn adjust-alpha
  [col x]
  (update-in col [3] #(m/clamp (+ x (or % 1)) 0.0 1.0)))

(defn gamma-rgba
  [[r g b & [a]] e]
  (let [rgb' (mapv #(Math/pow % e) [r g b])]
    (if a (conj rgb' a) rgb')))

(defn complementary-hsv
  "Returns new HSV color with its hue rotated by 180 degrees."
  [hsv] (rotate-hue-hsv hsv PI))

(defn complementary-rgb
  [rgb] (rotate-hue-rgb rgb PI))

(defn invert-rgb
  [[r g b]] [(- 1.0 r) (- 1.0 g) (- 1.0 b)])

(defn invert-hsv
  [hsv] (->> hsv (apply hsv->rgb) (invert-rgb) (apply rgb->hsv)))

(defn dist-rgb
  [rgb1 rgb2]
  (let [dr (- (first rgb1) (first rgb2))
        dg (- (nth rgb1 1) (nth rgb2 1))
        db (- (nth rgb1 2) (nth rgb2 2))]
    (Math/sqrt (mm/madd dr dr dg dg db db))))

(defn dist-hsv
  [[ha sa va] [hb sb vb]]
  (let [ha (* TWO_PI ha)
        hb (* TWO_PI hb)
        dh (- (* sa (Math/cos ha)) (* sb (Math/cos hb)))
        ds (- (* sa (Math/sin ha)) (* sb (Math/sin hb)))
        dv (- va vb)]
    (Math/sqrt (mm/madd dh dh ds ds dv dv))))
(defn blend-rgb
  [rgb1 rgb2 t]
  [(mm/mix (first rgb1) (first rgb2) t)
   (mm/mix (nth rgb1 1) (nth rgb2 1) t)
   (mm/mix (nth rgb1 2) (nth rgb2 2) t)])

(defn blend-hsv
  [hsv1 hsv2 t]
  [(let [h1 (first hsv1)
         h2 (first hsv2)
         hd (m/abs-diff h1 h2)]
     (if (> hd 0.5)
       (if (> h2 h1)
         (rem (mm/mix (inc h1) h2 t) 1.0)
         (rem (mm/mix h1 (inc h2) t) 1.0))
       (mm/mix h1 h2 t)))
   (mm/mix (nth hsv1 1) (nth hsv2 1) t)
   (mm/mix (nth hsv1 2) (nth hsv2 2) t)])

(defn blend-rgba
  [[dr dg db da] [sr sg sb sa]]
  (let [sa (or sa 1.0)
        da (or da 1.0)
        da' (mm/subm 1.0 sa da)
        a'  (+ sa da')
        ia' (/ 1.0 a')]
    [(* ia' (mm/madd sr sa dr da'))
     (* ia' (mm/madd sg sa dg da'))
     (* ia' (mm/madd sb sa db da'))
     a']))
