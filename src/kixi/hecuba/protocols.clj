;; Copyright © 2014, Mastodon C Ltd. All Rights Reserved.
(ns kixi.hecuba.protocols)

(defprotocol Commander
  (upsert! [_ type payload])
  (update! [_ type column payload where])
  (delete! [_ type id]))

(defprotocol Querier
  (item [_ type id])
  (items
    [_ type]
    [_ type where])
  (authorized? [_ props]))
