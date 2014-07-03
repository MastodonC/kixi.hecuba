(ns kixi.hecuba.data.programmes
  (:require [clojure.tools.logging :as log]
            [qbits.hayt :as hayt]
            [kixi.hecuba.storage.db :as db]))

(defn get-by-id
  ([session id]
     (first (db/execute session
                        (hayt/select :programmes
                                     (hayt/where [[= :id id]]))))))

(defn get-all
  ([session]
     (->> (db/execute session (hayt/select :programmes)))))
