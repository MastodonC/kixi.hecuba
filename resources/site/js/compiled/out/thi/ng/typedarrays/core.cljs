(ns thi.ng.typedarrays.core)

(defn int8
  "Creates a native Int8Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Int8Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Int8Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn uint8
  "Creates a native Uint8Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Uint8Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Uint8Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn uint8-clamped
  "Creates a native Uint8ClampedArray of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Uint8ClampedArray. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Uint8ClampedArray. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn int16
  "Creates a native Int16Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Int16Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Int16Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn uint16
  "Creates a native Uint16Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Uint16Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Uint16Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn int32
  "Creates a native Int32Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Int32Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Int32Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn uint32
  "Creates a native Uint32Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Uint32Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Uint32Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn float32
  "Creates a native Float32Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Float32Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Float32Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn float64
  "Creates a native Float64Array of the given size or from `coll`."
   [size-or-coll]
   (if (number? size-or-coll)
     (js/Float64Array. size-or-coll)
     (let [len (count size-or-coll)
           buf (js/Float64Array. len)]
       (loop [i 0, coll size-or-coll]
         (when (< i len)
           (aset buf i (first coll))
           (recur (inc i) (next coll))))
       buf)))

(defn typed-arrays-supported?
  "Returns true if JS runtime supports typed arrays"
  [] (not (nil? (aget js/window "ArrayBuffer"))))

(def ^:private array-types
  {"[object Uint8Array]"        :uint8
   "[object Uint8ClampedArray]" :uint8-clamped
   "[object Uint16Array]"       :uint16
   "[object Uint32Array]"       :uint32
   "[object Int8Array]"         :int8
   "[object Int16Array]"        :int16
   "[object Int32Array]"        :int32
   "[object Float32Array]"      :float32
   "[object Float64Array]"      :float64})

(defn array-type
  [x] (if (array? x) :array (array-types (str x))))

(defn typed-array?
  "Returns truthy value if the given arg is a typed array instance"
  [x]
  (cond
    (sequential? x) false
    (number? x)     false
    :else (array-types (str x))))
