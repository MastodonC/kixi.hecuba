#!/bin/bash

CONFIG_FILE=/.hecuba.edn

DATA_DIR="/data/hecuba/${HOSTNAME}/"
mkdir -p ${DATA_DIR}

CASSANDRA_SESSION_KEYSPACE=${HECUBA_KEYSPACE?NOT DEFINED}
S3_FILE_BUCKET=${HECUBA_FILE_BUCKET?NOT DEFINED}
S3_STATUS_BUCKET=${HECUBA_STATUS_BUCKET?NOT DEFINED}

echo "DATA_DIR is ${DATA_DIR}"
echo "CASSANDRA_SESSION_KEYSPACE is ${CASSANDRA_SESSION_KEYSPACE}"
echo "S3_FILE_BUCKET is ${S3_FILE_BUCKET}"
echo "S3_STATUS_BUCKET is ${S3_STATUS_BUCKET}"

echo <<EOF > ${CONFIG_FILE}

{
 :cassandra-session {:keyspace :${CASSANDRA_SESSION_KEYSPACE}
 :hecuba-session {:keyspace ${CASSANDRA_SESSION_KEYSPACE}}
 :search-session {:host "${ES01_TCP_PORT_9200_ADDR}" }
 :s3          {:access-key "${AWS_ACCESS_KEY_ID:?NOT DEFINED}"
               :secret-key "${AWS_SECRET_ACCESS_KEY:?NOT DEFINED}"
               :file-bucket "${S3_FILE_BUCKET}"
               :status-bucket "${S3_STATUS_BUCKET}"
               :download-dir "${DATA_DIR}"}
}

EOF

export HOME=/

java -jar -Djava.io.tmpdir=${DATA_DIR} /uberjar.jar
