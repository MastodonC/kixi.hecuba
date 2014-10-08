(ns kixi.hecuba.data.measurements.aliases
  (:require [clj-time.format :as tf]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [kixi.hecuba.data.measurements.core :refer (transpose)]
            [kixi.hecuba.data.measurements.upload :refer (date-parser)]))

(defn from-file [filename date-format]
  (let [blank-row?    (fn [cells] (every? #(re-matches #"\s*" %) cells))
        date-parser   (fn [d] (try ((date-parser date-format) d) (catch Throwable t nil)))
        invalid-date? (complement date-parser)
        header-rows   (with-open [in (io/reader (io/file filename))]
                        (->> in
                             (csv/read-csv)
                             (take-while (comp invalid-date? first))
                             (remove blank-row?)
                             (doall)))]
    (mapv (partial str/join \|) (rest (transpose header-rows)))))
