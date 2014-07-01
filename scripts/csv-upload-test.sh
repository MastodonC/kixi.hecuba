#!/bin/bash

EXAMPLES=${0%/*}/../examples

USERNAME=support@example.com
PASSWORD=password

BASE_URL="http://localhost:8010"
TARGET_URL="${BASE_URL}/4/measurements/"

HEADERS=$(mktemp)

CURL="curl -s -L -u ${USERNAME}:${PASSWORD}"
trap "rm -f ${HEADERS}" EXIT QUIT TERM

for file in ${EXAMPLES}/csv-upload/*.csv
do
    echo "Posting $file"
    $CURL -o /dev/null --dump-header $HEADERS -X POST -F "data=@$file;type=text/csv" $TARGET_URL
    LOCATION=$(awk '/Location:/{print $2}' $HEADERS)
    TRY=0
    until $( $CURL -v "${BASE_URL}${LOCATION}" )|| [ $TRY -eq 4 ]; do
       echo "Polling ${LOCATION} for status"
       sleep $(( TRY++ ))
   done

done
