(ns thi.ng.color.gradients
  #?(:cljs
  (:require-macros
   [thi.ng.math.macros :as mm]))
  (:require
   [thi.ng.math.core :as m :refer [PI TWO_PI]]
   [thi.ng.color.core :as col]
   #?(:clj [thi.ng.math.macros :as mm])))

(def cosine-schemes
  {:rainbow1 [[0.5 0.5 0.5] [0.5 0.5 0.5] [1.0 1.0 1.0] [0 0.3333 0.6666]]
   :rainbow2 [[0.5 0.5 0.5] [0.666 0.666 0.666] [1.0 1.0 1.0] [0 0.3333 0.6666]]
   :rainbow3 [[0.5 0.5 0.5] [0.75 0.75 0.75] [1.0 1.0 1.0] [0 0.3333 0.6666]]
   :rainbow4 [[0.5 0.5 0.5] [1 1 1] [1.0 1.0 1.0] [0 0.3333 0.6666]]
   :yellow-magenta-cyan [[1 0.5 0.5] [0.5 0.5 0.5] [0.75 1.0 0.6666] [0.8 1.0 0.4]]
   :orange-blue [[0.5 0.5 0.5] [0.5 0.5 0.5] [0.8 0.8 0.5] [0 0.2 0.5]]
   :green-magenta [[0.6666 0.5 0.5] [0.5 0.6666 0.5] [0.6666 0.666 0.5] [0.25 0.0 0.5]]
   :green-red [[0.5 0.5 0] [0.5 0.5 0] [0.5 0.5 0] [0.5 0.0 0]]
   :green-cyan [[0.0 0.5 0.5] [0 0.5 0.5] [0.0 0.3333 0.5] [0.0 0.6666 0.5]]
   :yellow-red [[0.5 0.5 0] [0.5 0.5 0] [0.1 0.5 0] [0.0 0.0 0]]
   :blue-cyan [[0.0 0.5 0.5] [0 0.5 0.5] [0.0 0.45 0.3333] [0.0 0.5 0.6666]]
   :red-blue [[0.5 0 0.5] [0.5 0 0.5] [0.5 0 0.5] [0 0 0.5]]})
(defn cosine-gradient-color
  [offset amp fmod phase t]
  (mapv
   (fn [a b c d] (m/clamp (+ a (* b (Math/cos (* TWO_PI (+ (* c t) d))))) 0 1))
   offset amp fmod phase))

(defn cosine-gradient
  "Takes a length n and 4 cosine coefficients (for colors usually
  3-element vectors) and produces vector of n new vectors, with each
  of its elements defined by an AM & FM cosine wave and clamped to
  the [0 1] interval."
  [n offset amp fmod phase]
  (mapv (partial cosine-gradient-color offset amp fmod phase) (m/norm-range (dec n))))
