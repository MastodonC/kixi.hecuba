(ns kixi.hecuba.number
  )

(defn numbers-as-strings? [& strings]
  (every? #(re-find #"^-?\d+(?:\.\d+)?$" %) strings))

(defn valid-number? [x]
  (if x
    (numbers-as-strings? x)
    true))
