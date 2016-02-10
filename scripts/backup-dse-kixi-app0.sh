#!/bin/bash

outdir=$(mktemp -d)

trap "rm -rf ${outdir}" QUIT EXIT TERM

echo "Logging to ${outdir}"

# -t 0 disables timeouts, we should probably have a sensible
# value of this, but it's tricky to come up with one :-)
parallel-ssh \
     -o ${outdir} \
     -t 0 \
     -x "-i ${HOME}/.ssh/id_dse_backup_rsa" \
     -H kixi-dse00 \
     -H kixi-dse01 \
     -H kixi-dse02 \
     /usr/local/bin/backup-dse.sh
