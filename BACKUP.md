# Backup

Backup strategy is as described [here](https://docs.datastax.com/en/cassandra/2.0/cassandra/operations/ops_backup_takes_snapshot_t.html)

# Backup installation

copy `scripts/backup-dse-kixi-app0.sh` to `kixi-app0:/usr/local/bin/backup-dse.sh`
copy `etc/cron.d/backup-dse` to `kixi-app0:/etc/cron.d/backup-dse`

copy `scripts/backup-dse.sh` to `kixi-dse{00,01,02}:/usr/local/bin/backup-dse.sh`
