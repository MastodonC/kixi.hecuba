#!/bin/bash

CASSANDRA_BASE=/raid0/cassandra

# Should be setup in ~/.aws/config
BACKUP_PROFILE=backup

CASSANDRA_DATA=${CASSANDRA_BASE}/data

HECUBA_BASE=${CASSANDRA_DATA}/hecuba

S3_BUCKET="s3://hecuba-backups"

S3_DESTINATION="${S3_BUCKET}/$(hostname)"

SNAPSHOTNAME="hecuba_$(date +%Y%m%d%H%M)"

echo "Making snapshot ${SNAPSHOTNAME}"

nodetool snapshot -t ${SNAPSHOTNAME}

echo "Backing up to S3"

directories=$(find ${HECUBA_BASE} -path "*/snapshots/${SNAPSHOTNAME}")

includes=""
for d in ${directories}
do
    src_tail=${d#${CASSANDRA_DATA}}
    aws --profile ${BACKUP_PROFILE} s3 sync ${d} ${S3_DESTINATION}/${SNAPSHOTNAME}${src_tail}
done

