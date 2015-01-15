(ns kixi.hecuba.hash)

(defn- get-hash [data]
  (.digest (java.security.MessageDigest/getInstance "sha1") (.getBytes data) ))

(defn- get-hash-str [data-bytes]
  (apply str
         (map
          #(.substring
            (Integer/toString
                     (+ (bit-and % 0xff) 0x100) 16) 1)
          data-bytes)))

(defn sha1 [data]
  (get-hash-str (get-hash data)))
