;; Copyright © 2014, Mastodon C Ltd. All Rights Reserved.
(ns kixi.hecuba.protocols)

(defprotocol Commander
  (upsert! [_ type payload])
  (update! [_ type payload where])
  (delete!
    [_ type where]
    [_ type column where]))

(defprotocol Querier
  (item [_ type id])
  (items
    [_ type]
    [_ type where]
    [_ type where limit]
    [_ type where paginate-key per-page]
    [_ type where paginate-key per-page last-key]))

(defprotocol Cassandra
  (-execute [session query opts])
  (-execute-async [session query opts])
  (-execute-chan [session query opts]))
