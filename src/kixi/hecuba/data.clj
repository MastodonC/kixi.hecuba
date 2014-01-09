(ns kixi.hecuba.data
  (:require
   [clojure.core.async :refer (put!)])
  (:import
   (kixi.hecuba.protocols Commander Querier)))

(defrecord CanonicalPayloadEncoder [ch]
  Commander
  (upsert! [_ id details]
    (put! ch {:id id :details details})))
