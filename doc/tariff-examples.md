# Authentication

Authentication is done using username and password with Basic HTTP
Authentication.

# Getting All Entities for a Project

Just replace *project-uuid* with the UUID for your project.

## Request

```
GET http://:hostname/4/projects/*project-uuid*/entities
Accept-Encoding: application/json
Authorization: :auth-token
```

## Response

```json
{
  "entities": [
    {
      "project_name": "My Project",
      "metering_point_ids": null,
      "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
      "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
      "profiles": [
        {
          "window_sets": [],
          "hot_water_systems": [],
          "biomasses": [],
          "timestamp": "2014-02-01T00:00:00+0000",
          "door_sets": [],
          "roof_rooms": [],
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "floors": [],
          "profile_id": "d55c4947-471e-48e1-962e-ae2991efa5dd",
          "user_id": "bruce@mastodonc.com",
          "heating_systems": [],
          "low_energy_lights": [],
          "extensions": [],
          "airflow_measurements": [],
          "heat_pumps": [],
          "storeys": [],
          "solar_thermals": [],
          "chps": [],
          "conservatories": [],
          "roofs": [],
          "photovoltaics": [],
          "walls": [],
          "ventilation_systems": [],
          "wind_turbines": [],
          "small_hydros": [],
          "profile_data": {
            "tariff": {
              "type": "electricity",
              "cost_per_kWh": 0.13,
              "annual_lump_sum_discount": 5,
              "daily_standing_charge": 0.1644
            },
            "event_type": "Tariff"
          },
          "thermal_images": []
        },
        {
          "window_sets": [],
          "hot_water_systems": [],
          "biomasses": [],
          "timestamp": "2014-02-01T00:00:00+0000",
          "door_sets": [],
          "roof_rooms": [],
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "floors": [],
          "profile_id": "85a41a2b-c946-4444-9a13-2696af877b21",
          "user_id": "bruce@mastodonc.com",
          "heating_systems": [],
          "low_energy_lights": [],
          "extensions": [],
          "airflow_measurements": [],
          "heat_pumps": [],
          "storeys": [],
          "solar_thermals": [],
          "chps": [],
          "conservatories": [],
          "roofs": [],
          "photovoltaics": [],
          "walls": [],
          "ventilation_systems": [],
          "wind_turbines": [],
          "small_hydros": [],
          "profile_data": {
            "tariff": {
              "type": "electricity",
              "cost_per_kWh": 0.13,
              "annual_lump_sum_discount": 5.0,
              "daily_standing_charge": 0.2192
            },
            "event_type": "Tariff"
          },
          "thermal_images": []
        },
        {
          "window_sets": [],
          "hot_water_systems": [],
          "biomasses": [],
          "timestamp": "2014-02-01T00:00:00+0000",
          "door_sets": [],
          "roof_rooms": [],
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "floors": [],
          "profile_id": "52999e2a-c649-48d9-ba58-46063547bda6",
          "user_id": "bruce@mastodonc.com",
          "heating_systems": [],
          "low_energy_lights": [],
          "extensions": [],
          "airflow_measurements": [],
          "heat_pumps": [],
          "storeys": [],
          "solar_thermals": [],
          "chps": [],
          "conservatories": [],
          "roofs": [],
          "photovoltaics": [],
          "walls": [],
          "ventilation_systems": [],
          "wind_turbines": [],
          "small_hydros": [],
          "profile_data": {
            "tariff": {
              "type": "electricity_time_of_use",
              "cost_per_on_peak_kWh": 0.15,
              "off_peak_periods": [
                {
                  "end": "05:00",
                  "start": "00:00"
                },
                {
                  "end": "23:59",
                  "start": "22:00"
                }
              ],
              "cost_per_off_peak_kWh": 0.06,
              "annual_lump_sum_discount": 10.0,
              "daily_standing_charge": 0.2192
            },
            "event_type": "Tariff"
          },
          "thermal_images": []
        },
        {
          "window_sets": [],
          "hot_water_systems": [],
          "biomasses": [],
          "timestamp": "2014-02-01T00:00:00+0000",
          "door_sets": [],
          "roof_rooms": [],
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "floors": [],
          "profile_id": "a0d5ea04-d427-45d4-affc-709e1c6ca6c1",
          "user_id": "bruce@mastodonc.com",
          "heating_systems": [],
          "low_energy_lights": [],
          "extensions": [],
          "airflow_measurements": [],
          "heat_pumps": [],
          "storeys": [],
          "solar_thermals": [],
          "chps": [],
          "conservatories": [],
          "roofs": [],
          "photovoltaics": [],
          "walls": [],
          "ventilation_systems": [],
          "wind_turbines": [],
          "small_hydros": [],
          "profile_data": {
            "tariff": {
              "type": "gas",
              "annual_lump_sum_discount": 5,
              "daily_standing_charge": 0.1644
            },
            "event_type": "Tariff"
          },
          "thermal_images": []
        },
        {
          "window_sets": [],
          "hot_water_systems": [],
          "biomasses": [],
          "timestamp": "2014-02-01T00:00:00+0000",
          "door_sets": [],
          "roof_rooms": [],
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "floors": [],
          "profile_id": "489697bb-9485-4322-b25f-026d755c74a3",
          "user_id": "bruce@mastodonc.com",
          "heating_systems": [],
          "low_energy_lights": [],
          "extensions": [],
          "airflow_measurements": [],
          "heat_pumps": [],
          "storeys": [],
          "solar_thermals": [],
          "chps": [],
          "conservatories": [],
          "roofs": [],
          "photovoltaics": [],
          "walls": [],
          "ventilation_systems": [],
          "wind_turbines": [],
          "small_hydros": [],
          "profile_data": {
            "tariff": {
              "type": "gas",
              "cost_per_kWh": 0.0425,
              "annual_lump_sum_discount": 5,
              "daily_standing_charge": 0.1644
            },
            "event_type": "Tariff"
          },
          "thermal_images": []
        },
        {
          "window_sets": [],
          "hot_water_systems": [],
          "biomasses": [],
          "timestamp": "2014-01-01T00:00:00+0000",
          "door_sets": [],
          "roof_rooms": [],
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "floors": [],
          "profile_id": "f561a7f9-f6c1-478f-b03e-8d1a04dc37da",
          "user_id": "bruce@mastodonc.com",
          "heating_systems": [],
          "low_energy_lights": [],
          "extensions": [],
          "airflow_measurements": [],
          "heat_pumps": [],
          "storeys": [],
          "solar_thermals": [],
          "chps": [],
          "conservatories": [],
          "roofs": [],
          "photovoltaics": [],
          "walls": [],
          "ventilation_systems": [],
          "wind_turbines": [],
          "small_hydros": [],
          "profile_data": {
            "event_type": "Planned"
          },
          "thermal_images": []
        },
        {
          "window_sets": [],
          "hot_water_systems": [],
          "biomasses": [],
          "timestamp": "2014-02-01T00:00:00+0000",
          "door_sets": [],
          "roof_rooms": [],
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "floors": [],
          "profile_id": "90efc150-92a5-4054-b52e-69369b87cd50",
          "user_id": "bruce@mastodonc.com",
          "heating_systems": [],
          "low_energy_lights": [],
          "extensions": [],
          "airflow_measurements": [],
          "heat_pumps": [],
          "storeys": [],
          "solar_thermals": [],
          "chps": [],
          "conservatories": [],
          "roofs": [],
          "photovoltaics": [],
          "walls": [],
          "ventilation_systems": [],
          "wind_turbines": [],
          "small_hydros": [],
          "profile_data": {
            "event_type": "Post"
          },
          "thermal_images": []
        }
      ],
      "devices": [
        {
          "metadata": null,
          "location": null,
          "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
          "synthetic": false,
          "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
          "parent_id": null,
          "metering_point_id": null,
          "privacy": null,
          "name": null,
          "readings": [
            {
              "actual_annual": false,
              "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
              "synthetic": true,
              "period": "PULSE",
              "correction_factor_breakdown": null,
              "lower_ts": null,
              "max": null,
              "status": null,
              "median": 0.0,
              "alias": null,
              "resolution": "60",
              "correction": null,
              "upper_ts": null,
              "correction_factor": null,
              "type": "electricityConsumption_differenceSeries",
              "corrected_unit": null,
              "frequency": null,
              "sensor_id": "6fbcadc9-1726-44dc-9896-01d30d7c5719",
              "accuracy": "0.1",
              "user_metadata": null,
              "unit": "kWh",
              "min": null
            },
            {
              "actual_annual": false,
              "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
              "synthetic": false,
              "period": "CUMULATIVE",
              "correction_factor_breakdown": null,
              "lower_ts": "2014-05-12T10:30:00.000Z",
              "max": null,
              "status": null,
              "median": 0.0,
              "alias": null,
              "resolution": "60",
              "correction": null,
              "upper_ts": "2014-05-12T10:30:00.000Z",
              "correction_factor": null,
              "type": "electricityConsumption",
              "corrected_unit": null,
              "frequency": null,
              "sensor_id": "b09e97fc-1e16-4609-bf7f-b28730845c69",
              "accuracy": "0.1",
              "user_metadata": {
                "foo": "bar"
              },
              "unit": "kWh",
              "min": null
            }
          ],
          "description": "Utility Meters"
        }
      ],
      "notes": [],
      "address_region": null,
      "photos": [],
      "documents": [],
      "address_country": null,
      "calculated_fields_values": null,
      "calculated_fields_labels": null,
      "calculated_fields_last_calc": null,
      "csv_uploads": [],
      "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
      "property_code": "PROP01",
      "programme_name": "AAA Programme",
      "name": null,
      "retrofit_completion_date": null,
      "property_data": {
        "monitoring_hierarchy": "3 - whole house monitoring with sub metering & occupant evaluation",
        "address_city": "Example Town",
        "address_street": "Example Flat, 1 Fake House",
        "monitoring_policy": "Monitoring Policy Notes",
        "terrain": "Dense Urban",
        "address_country": "England",
        "project_team": "Project Team Notes",
        "technology_icons": {
          "solid_wall_insulation": null,
          "heat_pumps": false,
          "solar_thermals": false,
          "chps": false,
          "photovoltaics": false,
          "cavity_wall_insulation": null,
          "ventilation_systems": false,
          "wind_turbines": false,
          "small_hydros": false
        },
        "project_phase": "Phase 2 (In-use and post occupancy)",
        "address_code": "POSTAL CODE",
        "property_type": "Flat",
        "construction_start_date": "Jan-08",
        "design_strategy": "Design Strategy",
        "other_notes": "Other Notes",
        "address_street_two": "Fake Road",
        "built_form": "Flat - mid floor",
        "project_summary": "Summary Text"
      },
      "editable": true,
      "address_street_two": null,
      "public_access": null,
      "address_county": null
    },
    {
      "project_name": "My Project",
      "metering_point_ids": null,
      "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
      "entity_id": "55e2e5ab-983c-4640-9f9d-823b057cc232",
      "profiles": [],
      "devices": [],
      "notes": [],
      "address_region": null,
      "photos": [],
      "documents": [],
      "address_country": null,
      "calculated_fields_values": null,
      "calculated_fields_labels": null,
      "calculated_fields_last_calc": null,
      "csv_uploads": [],
      "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
      "property_code": "PROP02",
      "programme_name": "AAA Programme",
      "name": null,
      "retrofit_completion_date": null,
      "property_data": null,
      "editable": true,
      "address_street_two": null,
      "public_access": null,
      "address_county": null
    }
  ],
  "page": 0,
  "total_hits": 2
}
```

# Creating a Tariff in a Profile

Tariffs are added as profiles to existing entities.

All of the creation events will return something similar to

```json
{
  "version": "4",
  "status": "OK",
  "location": [
    "/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/9e758949-6cbc-4abe-b241-bf5eacf15f02"
  ]
}
// POST http://HOSTNAME/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
// HTTP/1.1 201 Created
// Location: /4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/9e758949-6cbc-4abe-b241-bf5eacf15f02
// Vary: Accept, Accept-Charset
// Content-Type: application/json;charset=UTF-8
// Content-Length: 139
// Server: http-kit
// Date: Wed, 01 Apr 2015 10:17:49 GMT
// Request duration: 0.454462s
```

## Create Gas Tariff Profile

URL and Header

```
POST http://HOSTNAME/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
Accept-Encoding: application/json
Authorization: :auth-token
```

POST Body

```json
{
    "profile_data": {
        "event_type": "Tariff",
        "tariff": {
                        "type": "gas",
                        "cost_per_kWh": 0.0425,
                        "daily_standing_charge": 0.1644,
                        "annual_lump_sum_discount": 5.0
                  }
    },
    "timestamp": "2014-02-01T00:00:00.000Z",
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82"

}
```

## Create Simple Electricity Tariff Profile

URL and Header

```
POST http://HOSTNAME/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
Accept-Encoding: application/json
Authorization: :auth-token
```

POST Body

```json
{
    "profile_data": {
        "event_type": "Tariff",
        "tariff": {
                        "type": "electricity",
                        "cost_per_kWh": 0.13,
                        "daily_standing_charge": 0.2192,
                        "annual_lump_sum_discount": 5.0
                  }
    },
    "timestamp": "2014-02-01T00:00:00.000Z",
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82"

}
```

## Create Time of Use Electricity Tariff Profile

URL and Header

```
POST http://HOSTNAME/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
Accept-Encoding: application/json
Authorization: :auth-token
```

POST Body

```json
{
    "profile_data": {
        "event_type": "Tariff",
        "tariff": {
                        "type": "electricity_time_of_use",
                        "daily_standing_charge": 0.2192,
                        "cost_per_on_peak_kWh": 0.15,
                        "cost_per_off_peak_kWh": 0.06,
                        "annual_lump_sum_discount": 10.0,
                        "off_peak_periods": [
                                                {"start": "00:00", "end": "05:00"},
                                                {"start": "22:00", "end": "23:59"}
                                            ]

                  }
    },
    "timestamp": "2014-02-01T00:00:00.000Z",
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82"

}
```

# Get Measurements

From each entity you can get the device and sensor ids for both the
raw data that has been entered and they calculated data for the
Tariff, min, max, average and other series. Getting the measurements
will look like the following calls.

## Get raw measurements

```
GET http://:hostname/4/entities/1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3/devices/5f10b63931593f90a1a08729889a0842deda818c/measurements/CO2?startDate=2014-01-23%2000:00:00&endDate=2014-01-24%2000:00:00
Accept-Encoding: application/json
Authorization: :auth-token
```

Response

```json
{
  "measurements": [
    {
      "value": 470,
      "timestamp": "2014-01-23T13:50:00+0000",
      "sensor_id": "CO2"
    },
    {
      "value": 470,
      "timestamp": "2014-01-23T13:55:00+0000",
      "sensor_id": "CO2"
    },
    {
      "value": 470,
      "timestamp": "2014-01-23T14:00:00+0000",
      "sensor_id": "CO2"
    },...
  ]
}
// GET http://localhost:8010/4/entities/1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3/devices/5f10b63931593f90a1a08729889a0842deda818c/measurements/CO2?startDate=2014-01-23%2000:00:00&endDate=2014-01-24%2000:00:00
// HTTP/1.1 200 OK
// Vary: Accept, Accept-Charset
// Content-Type: application/json;charset=UTF-8
// Content-Length: 8781
// Server: http-kit
// Date: Wed, 01 Apr 2015 10:29:49 GMT
// Request duration: 0.347246s
```

You can also get daily and hourly rollups by adding daily_rollups or
hourly_rollups to the URL as shown in the examples below. The returned
data looks exactly like the raw data, but at a different time
granularity.

Daily rollups

```
GET http://HOSTNAME/4/entities/1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3/devices/5f10b63931593f90a1a08729889a0842deda818c/daily_rollups/CO2?startDate=2012-07-05%2000:00:00&endDate=2014-06-16%2000:00:00
Accept-Encoding: application/json
Authorization: :auth-token
```

Hourly rollups
```
GET http://HOSTNAME/4/entities/1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3/devices/5f10b63931593f90a1a08729889a0842deda818c/hourly_rollups/CO2?startDate=2014-01-23%2000:00:00&endDate=2014-01-24%2000:00:00
Accept-Encoding: application/json
Authorization: :auth-token
```
