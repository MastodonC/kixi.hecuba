ALTER TABLE partitioned_measurements RENAME type TO sensor_id;
ALTER TABLE hourly_rollups RENAME type TO sensor_id;
ALTER TABLE daily_rollups RENAME type TO sensor_id;
ALTER TABLE sensor_metadata RENAME type TO sensor_id;
ALTER TABLE sensors RENAME type TO sensor_id;
ALTER TABLE datasets ADD sensor_id text;

ALTER TABLE sensors ADD type text;
CREATE INDEX sensors_type_idx ON sensors (type);