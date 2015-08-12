(ns thi.ng.math.simplexnoise
  #?(:cljs (:require-macros [thi.ng.math.macros :as mm]))
  (:require
   #?(:clj [thi.ng.math.macros :as mm])
   [thi.ng.math.core :as m]))

;; adjusted scale factor to produce -1.0 .. +1.0 result interval
(def ^:const NORM (/ 1.0 0.7923811))

;; Permutation table

(def P
  (->> [151 160 137 91 90 15 131 13 201 95 96 53 194 233 7 225 140 36
        103 30 69 142 8 99 37 240 21 10 23 190 6 148 247 120 234 75 0
        26 197 62 94 252 219 203 117 35 11 32 57 177 33 88 237 149 56
        87 174 20 125 136 171 168 68 175 74 165 71 134 139 48 27 166
        77 146 158 231 83 111 229 122 60 211 133 230 220 105 92 41 55
        46 245 40 244 102 143 54 65 25 63 161 1 216 80 73 209 76 132
        187 208 89 18 169 200 196 135 130 116 188 159 86 164 100 109
        198 173 186 3 64 52 217 226 250 124 123 5 202 38 147 118 126
        255 82 85 212 207 206 59 227 47 16 58 17 182 189 28 42 223 183
        170 213 119 248 152 2 44 154 163 70 221 153 101 155 167 43 172
        9 129 22 39 253 19 98 108 110 79 113 224 232 178 185 112 104
        218 246 97 228 251 34 242 193 238 210 144 12 191 179 162 241
        81 51 145 235 249 14 239 107 49 192 214 31 181 199 106 157 184
        84 204 176 115 121 50 45 127 4 150 254 138 236 205 93 222 114
        67 29 24 72 243 141 128 195 78 66 215 61 156 180]
       (repeat 2)
       (apply into)))

(def G
  [1.0 1.0 0.0 0.0  -1.0 1.0 0.0 0.0
   1.0 -1.0 0.0 0.0 -1.0 -1.0 0.0 0.0
   1.0 0.0 1.0 0.0 -1.0 0.0 1.0 0.0
   1.0 0.0 -1.0 0.0 -1.0 0.0 -1.0 0.0
   0.0 1.0 1.0 0.0 0.0 -1.0 1.0 0.0
   0.0 1.0 -1.0 0.0 0.0 -1.0 -1.0 0.0
   1.0 1.0 0.0 0.0 -1.0 1.0 0.0 0.0
   0.0 -1.0 1.0 0.0 0.0 -1.0 -1.0 0.0])

(defn smooth
  [t] (* t (* t (* t (+ (* t (- (* t 6.0) 15.0)) 10.0)))))

(defn gradient1
  [i x] (* x (G (* (bit-and (P i) 15) 4))))

(defn gradient2
  [i j x y]
  (let [idx (* (bit-and (P (+ i (P j))) 15) 4)]
    (mm/madd x (G idx) y (G (inc idx)))))

(defn gradient3
  [i j k x y z]
  (let [idx (* (bit-and (P (+ i (P (+ j (P k))))) 15) 4)]
    (mm/madd x (G idx) y (G (inc idx)) z (G (+ idx 2)))))

(defn noise1
  [x]
  (let [xf (m/floor x)
        f  (- x xf)
        ix (bit-and xf 255)]
    (* (mm/mix
        (gradient1 ix f)
        (gradient1 (inc ix) (dec f))
        (smooth f))
       NORM)))

(defn noise2
  [x y]
  (let [pfx  (m/floor x) pfy (m/floor y)
        fpx  (- x pfx) fpy (- y pfy)
        ipx  (bit-and pfx 255) ipy (bit-and pfy 255)
        t    (smooth fpx)
        ipx' (inc ipx) ipy' (inc ipy)
        fpx' (dec fpx) fpy' (dec fpy)]
    (* (mm/mix
        (gradient2 ipx  ipy  fpx  fpy)
        (gradient2 ipx' ipy  fpx' fpy)
        (gradient2 ipx  ipy' fpx  fpy')
        (gradient2 ipx' ipy' fpx' fpy')
        t (smooth fpy))
       NORM)))

(defn noise3
  [x y z]
  (let [pfx  (m/floor x)       pfy  (m/floor y)       pfz  (m/floor z)
        fpx  (- x pfx)         fpy  (- y pfy)         fpz  (- z pfz)
        ipx  (bit-and pfx 255) ipy  (bit-and pfy 255) ipz  (bit-and pfz 255)
        ipx' (inc ipx)         ipy' (inc ipy)         ipz' (inc ipz)
        fpx' (dec fpx)         fpy' (dec fpy)         fpz' (dec fpz)
        t    (smooth fpx)      t2 (smooth fpy)]
    (* (mm/mix
        (gradient3 ipx  ipy  ipz  fpx  fpy  fpz)  ; 000
        (gradient3 ipx' ipy  ipz  fpx' fpy  fpz)  ; 100
        (gradient3 ipx  ipy' ipz  fpx  fpy' fpz)  ; 010
        (gradient3 ipx' ipy' ipz  fpx' fpy' fpz)  ; 110
        (gradient3 ipx  ipy  ipz' fpx  fpy  fpz') ; 001
        (gradient3 ipx' ipy  ipz' fpx' fpy  fpz') ; 101
        (gradient3 ipx  ipy' ipz' fpx  fpy' fpz') ; 011
        (gradient3 ipx' ipy' ipz' fpx' fpy' fpz') ; 111
        t t2 (smooth fpz))
       NORM)))
