USE hecuba;

ALTER TABLE entities ADD calculated_fields_values map<text, text>;
ALTER TABLE entities ADD calculated_fields_labels map<text, text>;
ALTER TABLE entities ADD calculated_fields_last_calc map<text, timestamp>;
ALTER TABLE sensors ADD actual_annual boolean;
ALTER TABLE sensor_metadata ADD actual_annual_calculation map<text, timestamp>;
