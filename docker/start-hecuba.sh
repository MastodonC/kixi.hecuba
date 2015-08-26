#!/bin/bash

source /etc/mastodonc/docker-functions

CONFIG_FILE=/.hecuba.edn

DATA_DIR="/data/hecuba/${HOSTNAME}/"
mkdir -p ${DATA_DIR}

CASSANDRA_KEYSPACE_NAME=${HECUBA_CASSANDRA_KEYSPACE_NAME?NOT DEFINED}
S3_FILE_BUCKET=${HECUBA_S3_FILE_BUCKET?NOT DEFINED}
S3_STATUS_BUCKET=${HECUBA_S3_STATUS_BUCKET?NOT DEFINED}
ES_SEARCH_SESSION_NAME=${HECUBA_ES_SEARCH_SESSION_NAME:-hecuba}

contact_points=()
for i in $(seq 255)
do
    cassandra_name=$(printf "CASSANDRA%02d" ${i})
    cassandra_addr_name="${cassandra_name}_PORT_9042_TCP_ADDR"

    [ ! -z "${!cassandra_addr_name}" ] && contact_points+=("\"${!cassandra_addr_name}\"")
done

echo "DATA_DIR is ${DATA_DIR}"
echo "CASSANDRA_KEYSPACE_NAME is ${CASSANDRA_KEYSPACE_NAME}"
echo "S3_FILE_BUCKET is ${S3_FILE_BUCKET}"
echo "S3_STATUS_BUCKET is ${S3_STATUS_BUCKET}"
echo "ES_SEARCH_SESSION_NAME is ${ES_SEARCH_SESSION_NAME}"
echo "CASSANDRA_CLUSTER_CONTACT_POINTS is ${contact_points}"

cat <<EOF > ${CONFIG_FILE}
{
 :hecuba-session {:keyspace :${CASSANDRA_KEYSPACE_NAME}}
 :cassandra-cluster {:contact-points [${contact_points[@]}]}
 :search-session {:host "${ES01_TCP_PORT_9200_ADDR}" :name "${ES_SEARCH_SESSION_NAME}"}
 :s3          {:access-key "${AWS_ACCESS_KEY_ID:?NOT DEFINED}"
               :secret-key "${AWS_SECRET_ACCESS_KEY:?NOT DEFINED}"
               :file-bucket "${S3_FILE_BUCKET}"
               :status-bucket "${S3_STATUS_BUCKET}"
               :download-dir "${DATA_DIR}"}
}
EOF

ensure_rsyslogd_running && \
    ensure_jstatd_running && \
    java -jar -Djava.io.tmpdir=${DATA_DIR} -Duser.home=/ /uberjar.jar
