#!/bin/sh

weave run 10.87.0.100/24 \
      --name kixi.hecuba01 \
      -e HECUBA_S3_FILE_BUCKET=hecuba-test \
      -e HECUBA_S3_STATUS_BUCKET=hecuba-test-status \
      -e HECUBA_CASSANDRA_KEYSPACE_NAME=hecuba_test \
      -e CASSANDRA01_PORT_9042_TCP_ADDR=10.87.0.10 \
      -e CASSANDRA01_PORT_9042_TCP_PORT=9042 \
      -e ES01_TCP_PORT_9200_ADDR=10.87.0.20 \
      -e ES01_TCP_PORT_9200_PORT=9200 \
      -e AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID?NOT DEFINED} \
      -e AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY?NOT DEFINED} \
      -p 8010:8010 \
      -p 4001:4001 \
      mastodonc/kixi.hecuba
