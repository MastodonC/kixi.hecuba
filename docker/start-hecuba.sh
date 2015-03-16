#!/bin/bash

DATA_DIR="/data/hecuba/${HOSTNAME}/"
mkdir -p ${DATA_DIR}

java -jar -Djava.io.tmpdir=${DATA_DIR} /uberjar.jar
