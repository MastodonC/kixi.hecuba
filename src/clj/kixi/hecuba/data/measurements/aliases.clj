(ns kixi.hecuba.data.measurements.aliases
  (:require [clj-time.format :as tf]
            [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [kixi.hecuba.data.measurements.core :refer (transpose)]
            [kixi.hecuba.data.measurements.upload :refer (date-parser-fn parse-header-rows)]))

(defn from-file [filename date-format]
  (let [date-parser (date-parser-fn date-format)
        header-rows (with-open [in (io/reader (io/file filename))]
                      (->> in
                           (csv/read-csv)
                           (parse-header-rows date-parser)
                           (doall)))]
    (mapv (partial str/join \|) (rest (transpose header-rows)))))
