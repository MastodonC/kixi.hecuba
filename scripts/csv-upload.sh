#!/bin/bash

BASE_URL="http://www.getembed.com"
TARGET_URL="${BASE_URL}/4/measurements/for-entity"

usage() { echo -e \
    "Usage: $0 -u <username> -p <password> -e <entity id> [-d <directory>] [-m <file-match-pattern>] [-v] [-n]\n\n" \
    "Mandatory:\n" \
    "\t -u embed username.\n" \
    "\t -p embed password.\n" \
    "\t -e entity (property) to which measurements should be uploaded.\n\n" \
    "Optional:\n" \
    "\t -d the directory to find files, defaults to current directory.\n" \
    "\t -m pattern to match files, defaults to '*.csv'.\n" \
    "\t -v verbose logging.\n" \
    "\t -n dry run (don't actually make any calls).\n" 1>&2; exit 1; }

match="*.csv"
directory="."

while getopts e:u:p:d:hvn o; do
    case "$o"
        e)      entity="${OPTARG}";;
        u)      username="${OPTARG}";;
        p)      password="${OPTARG}";;
        d)      directory="${OPTARG}";;
        m)      match="${OPTARG}";;
        v)      verbose=1;;
        n)      dry_run=1;;
        *)      usage;;
    esac
done

shift $(( ${OPTIND} - 1 ))

if [ -z "${username}" -o -z "${password}" -o -z "${entity}" ]; then
    usage;
fi

if [ ! -z "${verbose}" ] ;then
    verbose="-vvv"
fi

if [ ! -z "${dry_run}" ]; then
    curl_prefix="echo "
fi

CURL="${curl_prefix} curl ${verbose} -s -L --post302 -u ${username}:${password}"

# jiggery pokery to handle spaces in files names.
OLDIFS=$IFS
IFS=$'\n'
files=($(find "${directory}" -maxdepth 1 -name "${match}"))
IFS=${OLDIFS}

flen=${#files[@]}

for (( i=0; i < ${flen}; i++ )); do
    file=${files[$i]}
    form_args="-F 'data=@${file};type=text/csv'"
    echo "Uploading $file"
    $CURL -X POST ${form_args} "${TARGET_URL}/${entity}/"
done
