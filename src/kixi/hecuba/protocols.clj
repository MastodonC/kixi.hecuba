(ns kixi.hecuba.protocols)

(defprotocol Commander
  (upsert! [_ payload]))

(defprotocol Querier
  (item [_ id])
  (items [_] [_ where])

)
