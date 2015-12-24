#!/bin/sh

CASSANDRA_DATA_DIR=/raid0/cassandra/data/hecuba
directory=$(nodetool snapshot hecuba | awk '/directory/{print $3}')

# We assume it's worked if it tells us the directory
if [ -z "${directory}" ]; then
   echo "Problem getting snapshot directory"
   exit 1;
fi

# We only want the snapshots dirs
# For filters rationale see http://docs.aws.amazon.com/cli/latest/reference/s3/index.html#use-of-exclude-and-include-filters
aws --profile backup \
    s3 sync \
    --exclude '*' \
    --include '*/snapshots/*' \
    ${CASSANDRA_DATA_DIR} \
    s3://hecuba-backups/$(hostname)/
