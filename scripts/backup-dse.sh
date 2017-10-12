#!/bin/bash

CASSANDRA_DATA_DIR="/raid0/cassandra/data/hecuba"
IP=$(curl -s http://169.254.169.254/latest/meta-data/local-ipv4)
LOCAL_DAYS=3
REMOTE_DAYS=7
AWS_CMD="aws --profile backup"
BUCKET="s3://hecuba-backup"

function tag-date {
    local date=$(date --date="$1 days ago" +%Y%m%d)
    echo ${date}
}

function snapshot {
    nodetool snapshot -t $(tag-date 0) hecuba
}

function backup-snapshot-to-aws {
    $AWS_CMD \
        s3 sync \
        --exclude '*' \
        --include "*/snapshots/$(tag-date 0)/*" \
        ${CASSANDRA_DATA_DIR} \
        ${BUCKET}/${IP}/$(tag-date 0)/
}

function clear-local-snapshot {
    nodetool clearsnapshot -t $(tag-date $LOCAL_DAYS) hecuba
}

function clear-remote-snapshot {
    $AWS_CMD s3 rm ${BUCKET}/${IP}/$(tag-date $REMOTE_DAYS) --recursive
}

snapshot && \
    backup-snapshot-to-aws && \
    clear-local-snapshot && \
    clear-remote-snapshot
