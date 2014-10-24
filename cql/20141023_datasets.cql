DROP TABLE datasets;

CREATE TABLE datasets (
  id text,
  entity_id text,
  device_id text,
  name text,
  operation text,
  operands list<text>,
  PRIMARY KEY (id)
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  index_interval=128 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE INDEX datasets_entity_id_idx ON datasets (entity_id);
CREATE INDEX datasets_device_id_idx ON datasets (device_id);