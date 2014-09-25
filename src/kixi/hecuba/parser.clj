(ns kixi.hecuba.parser
  (:require [clojure.java.io :as io]))

(defn normalize-line-endings! [input output]
  (with-open [in  (io/reader input)
              out (java.io.PrintWriter. output)]
	(doseq [line (line-seq in)]
	  (.println out line))))
