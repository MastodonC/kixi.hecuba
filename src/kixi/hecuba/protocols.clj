;; Copyright © 2014, Mastodon C Ltd. All Rights Reserved.
(ns kixi.hecuba.protocols)

(defprotocol Cassandra
  (-execute [session query opts])
  (-execute-async [session query opts])
  (-execute-chan [session query opts]))
