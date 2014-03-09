(ns kixi.hecuba.data.paginate
  "Methods to paginate through measurements."
  (:require [kixi.hecuba.protocols :refer (upsert! update! delete! item items)]
            [kixi.hecuba.data.misc :as m]))

(defn measurement-batch
  [commander querier table where paginate-key per-page last-key sensor funct]
  (let [measurements (items querier table where paginate-key per-page last-key)]
    (when-not (empty? measurements)
      (funct commander querier sensor measurements)
      (m/to-timestamp (:timestamp (last measurements))))))

;; TODO batch is 10 measurements for testing purposes. Change it.
(defn paginate
  "Given a sensor, where clause and a function, paginate through measurements applying this function to each batch.
  Returns true if there were any measurements matching where clause, and false otherwise."
  [commander querier sensor table where funct]
  (let [first-result    (items querier table where :timestamp 10)]
    (if (not (empty? first-result))
      (let [last-timestamp  (m/to-timestamp (:timestamp (last first-result)))]
        (loop [last-key last-timestamp]
          (when-not (nil? last-key)
            (recur (measurement-batch commander querier table (conj where {:month (m/get-month-partition-key last-key)})
                                      :timestamp 10 last-key sensor funct))))
        true)
      false)))
