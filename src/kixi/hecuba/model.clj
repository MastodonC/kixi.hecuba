(ns kixi.hecuba.model)

(defprotocol Store
  (add-project! [_ id details])
  (get-project [_ id])
  (list-projects [_]))
