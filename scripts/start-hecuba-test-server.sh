#!/bin/sh

weave launch

weave run 10.87.0.10/24 \
     --name cassandra01 \
     --hostname cassandra01 \
     -e CASSANDRA_LISTEN_ADDRESS=10.87.0.10 \
     -P \
     -v /home/ubuntu:/shared \
     mastodonc/cassandra

weave run 10.87.0.20/24 \
     --name es01 \
     --hostname es01 \
     -e ELASTICSEARCH_CLUSTER_NAME=hecuba \
     -P \
     mastodonc/elasticsearch
