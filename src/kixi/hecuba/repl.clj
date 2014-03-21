(ns kixi.hecuba.repl
  "Useful functions for interacting with the pipeline from the repl."
  (:require [kixi.hecuba.scheduler :as s]
            [kixi.hecuba.pipeline  :refer [submit-item]]
            [modular               :refer (system)]))

(defmacro defreplmethods
  [name & options]
  `(let [options# (apply hash-map '~options)]
     (defn 
       ~name
       []
       (submit-item (-> system :pipeline :head)
                    (merge options#)))))

(defreplmethods rollups-> :dest :calculated-datasets :type :rollups)
(defreplmethods difference-series-> :dest :calculated-datasets :type :difference-series)
(defreplmethods median-calculation-> :dest :data-quality :type :median-calculation)
(defreplmethods mislabelled-check-> :dest :data-quality :type :mislabelled-sensors)
(defreplmethods median-spike-check-> :dest :data-quality :type :spike-check)
