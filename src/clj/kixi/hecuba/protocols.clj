;; Copyright © 2014, Mastodon C Ltd. All Rights Reserved.
(ns kixi.hecuba.protocols)

(defprotocol Cassandra
  (-execute [session query opts])
  (-prepare-statement [session statement])
  (-execute-prepared [session query opts])
  (-execute-chan [session query opts]))

(defprotocol Elasticsearch
  (-upsert [this index mapping-type id doc])
  (-search [this index mapping-type args])
  (-delete [this index mapping-type id])
  (-get-by-id [this index mapping-type id]))
