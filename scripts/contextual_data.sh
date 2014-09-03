#!/bin/bash

# Script to export all contextual data tables from Cassandra using the COPY command.

remote_host=kixi-dse00 # change to ssh config you use to log into a remote box

echo exporting data from $remote_host.
echo This requires /etc/hosts on $remote_host to have a suitable entry

tmp_dir=$(mktemp -d)
remote_tunnel_port=19160 #TODO allocate better, this could conflict.

trap "rm -rf $tmp_dir; pkill -f -- "ssh.*${remote_tunnel_port}";" QUIT TERM EXIT

cd ${tmp_dir}

echo Starting ssh tunnel to ${remote_host}

ssh -N -f -o ExitOnForwardFailure=yes -L ${remote_tunnel_port}:${remote_host}:9160 ${remote_host}

echo Exporting data to ${tmp_dir}

cqlsh localhost ${remote_tunnel_port} << EOD
use hecuba;
COPY profiles TO 'profiles.csv';
COPY programmes TO 'programmes.csv';
COPY projects TO 'projects.csv';
COPY entities TO 'entities.csv';
COPY devices TO 'devices.csv';
COPY sensors TO 'sensors.csv';
COPY sensor_metadata TO 'sensor_metadata.csv';
exit;
EOD

echo Finished exporting data

echo Killing ssh tunnel to ${remote_host}

# FIXME could kill the wrong thing...
pkill -f -- "ssh.*${remote_tunnel_port}";

echo Importing data to VM

cqlsh << EOD
use test;
COPY profiles FROM 'profiles.csv';
COPY programmes FROM 'programmes.csv';
COPY projects FROM 'projects.csv';
COPY entities FROM 'entities.csv';
COPY devices FROM 'devices.csv';
COPY sensors FROM 'sensors.csv';
COPY sensor_metadata FROM 'sensor_metadata.csv';
exit;
EOD

echo Finished importing data

echo Dont forget to refresh Elastic Search...
