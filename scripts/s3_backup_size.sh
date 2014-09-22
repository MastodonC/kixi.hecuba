#!/bin/sh

AWS_PROFILE=backup
BACKUP_BUCKET=hecuba-backups

aws --profile ${AWS_PROFILE} s3 ls s3://${BACKUP_BUCKET} --recursive  | awk 'BEGIN {total=0}!/(Bucket: |Prefix: |LastWriteTime|^$|--)/{total+=$3}END{print total/1024/1024" MB"}'
