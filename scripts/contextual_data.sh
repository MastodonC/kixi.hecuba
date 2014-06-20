#!/bin/bash
 
# Script to export all contextual data tables from Cassandra using the COPY command.

remote_host=kixi-dse00 # change to ssh config you use to log into a remote box
local_host=vagrant-box  # change to ssh config for your local box

echo exporting data

ssh $remote_host << EOF

if [ -d "tmp_data" ]; then
    rm -rf tmp_data/*
else
    mkdir tmp_data
fi

cd tmp_data

cqlsh << EOD
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
exit
EOF

echo finished exporting data

echo copying from remote to local machine

if [ -d "tmp_data" ]; then
    rm -rf tmp_data/*
else
    mkdir tmp_data
fi
    
scp $remote_host:tmp_data/* tmp_data/

echo importing data to virtual machine
scp tmp_data/* $local_host:

echo importing data from csv files

ssh $local_host << EOF
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
exit
EOF
