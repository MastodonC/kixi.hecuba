drop keyspace test;

CREATE KEYSPACE test WITH replication = {
  'class': 'SimpleStrategy',
  'replication_factor': '1'
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
  index_interval=128 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE datasets (
  entity_id text,
  name text,
  members set<text>,
  operation text,
  type text,
  device_id text,
  PRIMARY KEY (entity_id, name)
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

CREATE TABLE devices (
  id text,
  description text,
  entity_id text,
  location text,
  metadata text,
  metering_point_id text,
  name text,
  parent_id text,
  privacy text,
  synthetic boolean,
  user_id text,
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

CREATE INDEX devices_entity_id_idx_1 ON devices (entity_id);

CREATE INDEX synthetic_devices_idx ON devices (synthetic);

CREATE TABLE entities (
  id text,
  address_country text,
  address_county text,
  address_region text,
  address_street_two text,
  csv_uploads list<text>,
  devices map<text, text>,
  documents list<text>,
  metering_point_ids text,
  name text,
  notes list<text>,
  photos list<text>,
  project_id text,
  property_code text,
  property_data text,
  retrofit_completion_date text,
  user_id text,
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
  index_interval=128 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE measurements (
  device_id text,
  type text,
  month int,
  "timestamp" timestamp,
  error text,
  metadata map<text, text>,
  value text,
  PRIMARY KEY (device_id, type, month, "timestamp")
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

CREATE TABLE profiles (
  id text,
  airflow_measurements list<text>,
  chps list<text>,
  conservatories list<text>,
  door_sets list<text>,
  entity_id text,
  extensions list<text>,
  floors list<text>,
  heat_pumps list<text>,
  heating_systems list<text>,
  hot_water_systems list<text>,
  low_energy_lights list<text>,
  photovoltaics list<text>,
  profile_data text,
  roof_rooms list<text>,
  roofs list<text>,
  small_hydros list<text>,
  solar_thermals list<text>,
  storeys list<text>,
  thermal_images list<text>,
  "timestamp" timestamp,
  user_id text,
  ventilation_systems list<text>,
  walls list<text>,
  wind_turbines list<text>,
  window_sets list<text>,
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
  compression={'sstable_compression': 'LZ4Compressor'};

CREATE TABLE programmes (
  id text,
  created_at text,
  description text,
  home_page_text text,
  lead_organisations text,
  lead_page_text text,
  leaders text,
  name text,
  public_access text,
  updated_at text,
  user_id text,
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

CREATE TABLE projects (
  id text,
  created_at text,
  description text,
  name text,
  organisation text,
  programme_id text,
  project_code text,
  project_type text,
  type_of text,
  updated_at text,
  user_id text,
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

CREATE INDEX projects_programme_id_idx_1 ON projects (programme_id);

CREATE TABLE sensor_metadata (
  device_id text,
  type text,
  difference_series map<text, timestamp>,
  median_calc_check map<text, timestamp>,
  mislabelled text,
  mislabelled_sensors_check map<text, timestamp>,
  rollups map<text, timestamp>,
  spike_check map<text, timestamp>,
  lower_ts timestamp,
  upper_ts timestamp,
  PRIMARY KEY (device_id, type)
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
  frequency text,
  max text,
  median double,
  min text,
  period text,
  resolution text,
  status text,
  synthetic boolean,
  unit text,
  user_id text,
  PRIMARY KEY (device_id, type)
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
  index_interval=128 AND
  read_repair_chance=0.100000 AND
  replicate_on_write='true' AND
  populate_io_cache_on_flush='false' AND
  default_time_to_live=0 AND
  speculative_retry='99.0PERCENTILE' AND
  memtable_flush_period_in_ms=0 AND
  compaction={'class': 'SizeTieredCompactionStrategy'} AND
  compression={'sstable_compression': 'SnappyCompressor'};

CREATE TABLE users (
  id text,
  hash text,
  salt text,
  username text,
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

CREATE INDEX users_idx ON users (username);
