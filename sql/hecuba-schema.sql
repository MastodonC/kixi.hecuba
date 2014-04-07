CREATE KEYSPACE test WITH replication = {
  'class': 'NetworkTopologyStrategy',
  'datacenter1': '1'
};

USE test;

CREATE TABLE daily_rollups (
  device_id text,
  type text,
  "timestamp" timestamp,
  value text,
  PRIMARY KEY (device_id, type, "timestamp")
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE data_sets (
  entity_id text,
  name text,
  members text,
  PRIMARY KEY (entity_id, name)
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE devices (
  id text PRIMARY KEY,
  description text,
  entity_id text,
  location text,
  metadata text,
  metering_point_id text,
  name text,
  parent_id text,
  privacy text
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE INDEX devices_entity_id_idx_1 ON devices (entity_id);

CREATE TABLE difference_series (
  device_id text,
  type text,
  month int,
  "timestamp" timestamp,
  value text,
  PRIMARY KEY (device_id, type, month, "timestamp")
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE entities (
  id text PRIMARY KEY,
  address_country text,
  address_county text,
  address_region text,
  address_street_two text,
  csv_uploads list<text>,
  documents list<text>,
  name text,
  notes list<text>,
  photos list<text>,
  project_id text,
  property_code text,
  property_data text,
  retrofit_completion_date text,
  user_id text
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE INDEX entities_project_id_idx_1 ON entities (project_id);

CREATE TABLE hourly_rollups (
  device_id text,
  type text,
  year int,
  "timestamp" timestamp,
  value text,
  PRIMARY KEY (device_id, type, year, "timestamp")
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE measurements (
  device_id text,
  type text,
  month int,
  "timestamp" timestamp,
  error text,
  metadata text,
  value text,
  PRIMARY KEY (device_id, type, month, "timestamp")
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE programmes (
  id text PRIMARY KEY,
  created_at text,
  description text,
  home_page_text text,
  lead_organisations text,
  lead_page_text text,
  leaders text,
  name text,
  public_access text,
  updated_at text
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE projects (
  id text PRIMARY KEY,
  created_at text,
  description text,
  name text,
  organisation text,
  programme_id text,
  project_code text,
  project_type text,
  type_of text,
  updated_at text
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE INDEX projects_programme_id_idx_1 ON projects (programme_id);

CREATE TABLE sensor_metadata (
  device_id text,
  type text,
  difference_series text,
  median_calc_check text,
  mislabelled text,
  mislabelled_sensors_check text,
  rollups text,
  spike_check text,
  PRIMARY KEY (device_id, type)
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE INDEX sensor_metadata_mislabelled_idx ON sensor_metadata (mislabelled);

CREATE TABLE sensors (
  device_id text,
  type text,
  accuracy text,
  corrected_unit text,
  correction text,
  correction_factor text,
  correction_factor_breakdown text,
  errors int,
  events int,
  max text,
  median double,
  min text,
  period text,
  resolution text,
  status text,
  unit text,
  synthetic boolean,
  PRIMARY KEY (device_id, type)
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE INDEX sensors_status_idx ON sensors (status);
CREATE INDEX synthetic_sensors_idx ON sensors (synthetic);

CREATE TABLE user_sessions (
  id text,
  "timestamp" timestamp,
  user text,
  PRIMARY KEY (id, "timestamp")
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE users (
  id text PRIMARY KEY,
  hash text,
  salt text,
  username text
) WITH
  bloom_filter_fp_chance=0.010000 AND
  caching='KEYS_ONLY' AND
  comment='' AND
  dclocal_read_repair_chance=0.000000 AND
  gc_grace_seconds=864000 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};
