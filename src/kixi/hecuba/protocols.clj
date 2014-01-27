(ns kixi.hecuba.protocols)

(defprotocol Commander
  (upsert! [_ payload])
  (delete! [_ id]))

(defprotocol Querier
  (item [_ id])
  (items [_] [_ where])

)
