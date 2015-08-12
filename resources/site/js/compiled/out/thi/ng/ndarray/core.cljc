(ns thi.ng.ndarray.core
  #?(:cljs
     (:require-macros
[thi.ng.math.macros :as mm]
[thi.ng.ndarray.macros :refer [def-ndarray]]))
  (:require
   #?@(:clj
 [[thi.ng.math.macros :as mm]
  [thi.ng.ndarray.macros :refer [def-ndarray]]]
 :cljs
 [[thi.ng.typedarrays.core :as a]])
   [thi.ng.math.core :as m]))

(defprotocol PNDArray
  (data [_]
    "Returns the backing data array.")
  (data-type [_]
    "Returns the ndarray's type id (keyword).")
  (dimension [_]
    "Returns the ndarray's dimension.")
  (shape [_]
    "Returns a vector of the ndarray's sizes in each dimension.")
  (stride [_]
    "Returns a vector of the ndarray's strides in each dimension.")
  (offset [_]
    "Returns the ndarray view's start index in the backing data array")
  (size [_]
    "Returns the element count of an ndarray view")
  (extract [_]
    "Creates a new backing array of only values in given ndarray view
    and returns new ndarray of same shape, but with strides reset to
    defaults order.")
  (index-at [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Returns the global index into the backing array for given
    position in an ndarray view.")
  (index-pos [_ i]
    "Computes relative position in an ndarray view from given absolute
    array index.")
  (index-seq [_]
    "Returns a lazy seq of all array indices in an ndarray view.")
  (position-seq [_]
    "Returns a lazy seq of all position vectors in an ndarray view.")
  (get-at [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Returns value at given position in an ndarray view (without bounds check,
    assumes position is safe).")
  (get-at-safe [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Returns value at given position in an ndarray view (with bounds
    check)")
  (get-at-index [_ i]
    "Returns value at given global index in an ndarray view's backing
    array.")
  (set-at [_ x v] [_ x y v] [_ x y z v] [_ x y z w v]
    "Sets backing data array at given position in an ndarray view to
    new value v (without bounds check). Returns same NDArray instance.")
  (set-at-safe [_ x v] [_ x y v] [_ x y z v] [_ x y z w v]
    "Sets backing array at given position in an ndarray view to
    new value v (with bounds check). Returns same NDArray instance.")
  (set-at-index [_ i v]
    "Sets backing data array at given global index to new value
    v (without bounds check). Returns same NDArray instance.")
  (update-at [_ x f] [_ x y f] [_ x y z f] [_ x y z w f]
    "Applies function f to given position in an ndarray view and sets
    backing array at same position to the return value of f. The
    function f itself accepts m args: first the n coordinates of the
    position and the value at this position")
  (update-at-safe [_ x f] [_ x y f] [_ x y z f] [_ x y z w f]
    "Same as `update-at` but with bounds check.")
  (update-at-index [_ i f]
    "Applies function f to given global index in an ndarray's view
    backing array and sets it to the return value of f. The
    function f itself accepts 2 args: the supplied array index
    and the array's value at this index")
  (truncate-h [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Returns a new ndarray of same type with its shape truncated at
    top end. Negative args are considered offsets from current shape.
    nil values keep shape in that dimension. Does not mutate backing
    array.")
  (truncate-l [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Returns a new ndarray of same type with its shape truncated at
    lower end, effectively shifting its start index/offset towards the
    center of the view. Does not mutate backing array.")
  (transpose [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Returns a new ndarray view with stride order/axes swapped as per
    given vector. Does not mutate backing array.")
  (step [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Returns new ndarray view with stride steps/direction changed as
    per given vector. Values >1 result in skipping of items in that
    dimension, negative value flip direction, `nil` keeps current
    stride for that dimension. Does not mutate backing array.")
  (pick [_ x] [_ x y] [_ x y z] [_ x y z w]
    "Generalized getter. Accepts n args (e.g. 3 for a 3D ndarray),
    each selecting a dimension slice (nil skips a dimension). Returns
    new ndarray view of selection, or if selecting in all dimensions,
    returns array value at that point. Does not mutate backing array."))

(defn order
  [coll] (->> coll (map vector (range)) (sort-by peek) (mapv first)))

(defn shape->stride
  [shape]
  (->> shape
       reverse
       (reduce #(conj % (* %2 (first %))) '(1))
       (drop 1)
       (vec)))

(def ctor-registry (atom {}))

#?(:clj
   (do
     (def-ndarray 1 nil     nil        :generic to-array aget aset true)
     (def-ndarray 1 boolean "booleans" :boolean boolean-array aget aset true)
     (def-ndarray 1 byte    "bytes"    :int8    byte-array aget aset true)
     (def-ndarray 1 short   "shorts"   :int16   short-array aget aset true)
     (def-ndarray 1 int     "ints"     :int32   int-array aget aset true)
     (def-ndarray 1 long    "longs"    :int64   long-array aget aset true)
     (def-ndarray 1 float   "floats"   :float32 float-array aget aset true)
     (def-ndarray 1 double  "doubles"  :float64 double-array aget aset true)

     (def-ndarray 2 nil     nil        :generic to-array aget aset true)
     (def-ndarray 2 boolean "booleans" :boolean boolean-array aget aset true)
     (def-ndarray 2 byte    "bytes"    :int8    byte-array aget aset true)
     (def-ndarray 2 short   "shorts"   :int16   short-array aget aset true)
     (def-ndarray 2 int     "ints"     :int32   int-array aget aset true)
     (def-ndarray 2 long    "longs"    :int64   long-array aget aset true)
     (def-ndarray 2 float   "floats"   :float32 float-array aget aset true)
     (def-ndarray 2 double  "doubles"  :float64 double-array aget aset true)

     (def-ndarray 3 nil     nil        :generic to-array aget aset true)
     (def-ndarray 3 boolean "booleans" :boolean boolean-array aget aset true)
     (def-ndarray 3 byte    "bytes"    :int8    byte-array aget aset true)
     (def-ndarray 3 short   "shorts"   :int16   short-array aget aset true)
     (def-ndarray 3 int     "ints"     :int32   int-array aget aset true)
     (def-ndarray 3 long    "longs"    :int64   long-array aget aset true)
     (def-ndarray 3 float   "floats"   :float32 float-array aget aset true)
     (def-ndarray 3 double  "doubles"  :float64 double-array aget aset true)

     (def-ndarray 4 nil     nil        :generic to-array aget aset true)
     (def-ndarray 4 boolean "booleans" :boolean boolean-array aget aset true)
     (def-ndarray 4 byte    "bytes"    :int8    byte-array aget aset true)
     (def-ndarray 4 short   "shorts"   :int16   short-array aget aset true)
     (def-ndarray 4 int     "ints"     :int32   int-array aget aset true)
     (def-ndarray 4 long    "longs"    :int64   long-array aget aset true)
     (def-ndarray 4 float   "floats"   :float32 float-array aget aset true)
     (def-ndarray 4 double  "doubles"  :float64 double-array aget aset true)
     )
   :cljs
   (do
     (def-ndarray 1 nil nil :generic to-array aget aset false)
     (def-ndarray 1 nil nil :uint8 a/uint8 aget aset false)
     (def-ndarray 1 nil nil :uint8-clamped a/uint8-clamped aget aset false)
     (def-ndarray 1 nil nil :uint16 a/uint16 aget aset false)
     (def-ndarray 1 nil nil :uint32 a/uint32 aget aset false)
     (def-ndarray 1 nil nil :int8 a/int8 aget aset false)
     (def-ndarray 1 nil nil :int16 a/int16 aget aset false)
     (def-ndarray 1 nil nil :int32 a/int32 aget aset false)
     (def-ndarray 1 nil nil :float32 a/float32 aget aset false)
     (def-ndarray 1 nil nil :float64 a/float64 aget aset false)

     (def-ndarray 2 nil nil :generic to-array aget aset false)
     (def-ndarray 2 nil nil :uint8 a/uint8 aget aset false)
     (def-ndarray 2 nil nil :uint8-clamped a/uint8-clamped aget aset false)
     (def-ndarray 2 nil nil :uint16 a/uint16 aget aset false)
     (def-ndarray 2 nil nil :uint32 a/uint32 aget aset false)
     (def-ndarray 2 nil nil :int8 a/int8 aget aset false)
     (def-ndarray 2 nil nil :int16 a/int16 aget aset false)
     (def-ndarray 2 nil nil :int32 a/int32 aget aset false)
     (def-ndarray 2 nil nil :float32 a/float32 aget aset false)
     (def-ndarray 2 nil nil :float64 a/float64 aget aset false)

     (def-ndarray 3 nil nil :generic to-array aget aset false)
     (def-ndarray 3 nil nil :uint8 a/uint8 aget aset false)
     (def-ndarray 3 nil nil :uint8-clamped a/uint8-clamped aget aset false)
     (def-ndarray 3 nil nil :uint16 a/uint16 aget aset false)
     (def-ndarray 3 nil nil :uint32 a/uint32 aget aset false)
     (def-ndarray 3 nil nil :int8 a/int8 aget aset false)
     (def-ndarray 3 nil nil :int16 a/int16 aget aset false)
     (def-ndarray 3 nil nil :int32 a/int32 aget aset false)
     (def-ndarray 3 nil nil :float32 a/float32 aget aset false)
     (def-ndarray 3 nil nil :float64 a/float64 aget aset false)

     (def-ndarray 4 nil nil :generic to-array aget aset false)
     (def-ndarray 4 nil nil :uint8 a/uint8 aget aset false)
     (def-ndarray 4 nil nil :uint8-clamped a/uint8-clamped aget aset false)
     (def-ndarray 4 nil nil :uint16 a/uint16 aget aset false)
     (def-ndarray 4 nil nil :uint32 a/uint32 aget aset false)
     (def-ndarray 4 nil nil :int8 a/int8 aget aset false)
     (def-ndarray 4 nil nil :int16 a/int16 aget aset false)
     (def-ndarray 4 nil nil :int32 a/int32 aget aset false)
     (def-ndarray 4 nil nil :float32 a/float32 aget aset false)
     (def-ndarray 4 nil nil :float64 a/float64 aget aset false)
     ))

(defn ndarray
  ([type data]
   (ndarray type data [(count data)]))
  ([type data shape]
   (let [{:keys [ctor data-ctor]} (get-in @ctor-registry [(count shape) type])]
     (if ctor
       (ctor (if (sequential? data) (data-ctor data) data) 0 (shape->stride shape) shape)
       (throw (new #?(:clj IllegalArgumentException :cljs js/Error)
                   (str "Can't create ndarray for: " type " " data)))))))
