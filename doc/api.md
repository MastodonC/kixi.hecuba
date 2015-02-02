# hecuba API

The hecuba API is based on the
[AMON format](https://github.com/mastodonc/amon).

It is a REST like API that has a number of resources that can be
retrieved with GET, created with POST, changed with POST or PUT and
deleted with DELETE.

# Security, Authentication and Authorisation

The API is secured by cookies or Basic HTTP Authentication.

There are further authorisation checks that are done on a per user
basis and depend upon the users roles, which can be public, user on a
project or programme, project manager on a project, programme manager
on a programme or admin.

A single user account can have different roles on different projects
and programmes.

# Resources

The hecuba API is primarily about managing resources. The primary
resources in the system are:

* Programmes
* Projects
* Entities
* Profiles
* Devices
* Measurements

The fields shown in the replies below are indicative. More fields may
be returned by the resources.

## Programmes

### GET

#### Index

##### Example request

```
GET http://:hostname/4/programmes/
Accept-Encoding: application/json
Authorization: :auth-token
```

##### Example response

```json
[
  {
    "created_at": "2014-08-20 15:56:19",
    "href": "/4/programmes/c94a2f01d89708fb406fed83665ccb1c36e441a5",
    "updated_at": "2014-08-20 15:56:19",
    "leaders": null,
    "programme_id": "<programme uuid>",
    "name": "A programme name",
    "lead_organisations": "An organisation",
    "home_page_text": "Some text",
    "public_access": "true",
    "lead_page_text": null,
    "projects": "/4/programmes/c94a2f01d89708fb406fed83665ccb1c36e441a5/projects/",
    "description": "Some more text"
  }
]
```

#### An individual resource

##### Example request

```
GET http://:hostname/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd
Accept-Encoding: application/json
Authorization: :auth-token
```

##### Example response

Response 200 OK

```json
{
    "description": null,
    "projects": "/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd/projects/",
    "lead_page_text": null,
    "public_access": null,
    "home_page_text": null,
    "lead_organisations": null,
    "admin": true,
    "editable": true,
    "name": "TEST102",
    "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
    "leaders": null,
    "updated_at": null,
    "created_at": null
}
```
### Edit Programme

#### Request

```
PUT http://:hostname/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

Response 201 Created

```json
{
    "description": "Some programme text",
    "lead_page_text": "Someone else again",
    "home_page_text": "More text",
    "lead_organisations": "My Org",
    "name": "AAA Programme",
    "programme_id": "50bdc27d-e6b9-409c-83db-54cf71794de8",
    "leaders": ""
}
```

## Projects

### Get Projects

#### Request

```
GET http://:hostname/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd/projects
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

Response 200

```json
[
    {
        "description": null,
        "properties": "/4//",
        "organisation": null,
        "name": "TESTPROJECT",
        "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
        "project_type": null,
        "updated_at": null,
        "type_of": null,
        "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
        "project_code": null,
        "href": "/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd/projects/8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
        "created_at": null
    }
]
```

### Create Project

#### Request

```
POST http://:hostname/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd/projects/
Accept-Encoding: application/json
Authorization: :auth-token
Content-Type: application/json

{"name":"TESTPROJECT","programme_id":"9b7ff9c2-f82f-4528-9934-c72eba3bb8fd"}
```

#### Response

Response 201 Created

```json
{
    "location": "/4/projects/8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
    "status": "OK",
    "version": "4"
}
```

### Get Project

#### Request

```
GET http://:hostname/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd/projects/8e9972b9-a90f-44bc-89ce-2bb7b2b281ab
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

```json
Response 200
{
    "description": null,
    "organisation": null,
    "name": "TESTPROJECT",
    "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
    "project_type": null,
    "updated_at": null,
    "type_of": null,
    "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
    "project_code": null,
    "created_at": null
}
```

### Edit Project

#### Request

```
PUT http://:hostname/4/programmes/9b7ff9c2-f82f-4528-9934-c72eba3bb8fd/projects/8e9972b9-a90f-44bc-89ce-2bb7b2b281ab
Accept-Encoding: application/json
Authorization: :auth-token

{
    "description": "My Project",
    "organisation": "My Sub Org",
    "name": "My Project",
    "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
    "project_type": "New Build",
    "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
    "project_code": "MP001"
}
```

#### Response

Response 201 Created

## Entities

### Get Entities

#### Request

```
GET http://:hostname/4/projects/8e9972b9-a90f-44bc-89ce-2bb7b2b281ab/entities
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

Response 200

```json
{
    "total_hits": 0,
    "page": 0,
    "entities": []
}
```

After Creating Properties

```json
{
    "total_hits": 2,
    "page": 0,
    "entities": [
        {
            "public_access": null,
            "editable": true,
            "programme_name": "AAA Programme",
            "property_code": "PROP01",
            "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
            "documents": [],
            "photos": [],
            "devices": [],
            "profiles": [],
            "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
            "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
            "project_name": "My Project"
        }...
    ]
}
```

### Create entity

#### Request

```
POST http://:hostname/4/entities/
Accept-Encoding: application/json
Authorization: :auth-token
Content-Type: application/json

{"project_id":"8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
 "property_code":"PROP02"}
```

#### Response

Response 201

```json
{
    "headers": {
        "Location": "/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82"
    },
    "body": {
        "location": "/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
        "status": "OK",
        "version": "4"
    }
}
```

### Get entity

#### Request
```
GET http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

Response 200

```json
{
    "public_access": null,
    "editable": true,
    "programme_name": "AAA Programme",
    "property_code": "PROP01",
    "programme_id": "9b7ff9c2-f82f-4528-9934-c72eba3bb8fd",
    "documents": [],
    "photos": [],
    "devices": [],
    "profiles": [],
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
    "project_name": "My Project"
}
```

### Edit entity

#### Request

```
PUT http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82
Accept-Encoding: application/json
Authorization: :auth-token

{
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "project_id": "8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
    "property_code": "PROP01",
    "property_data": {"address_street":"Example Flat, 1 Fake House",
                      "address_street_two":"Fake Road",
                      "address_city":"Example Town",
                      "address_code":"POSTAL CODE",
                      "address_country":"England",
                      "project_summary":"Summary Text",
                      "built_form":"Flat - mid floor",
                      "other_notes":"Other Notes",
                      "design_strategy":"Design Strategy",
                      "construction_start_date":"Jan-08",
                      "property_type":"Flat",
                      "project_phase":"Phase 2 (In-use and post occupancy)",
                      "project_team":"Project Team Notes",
                      "terrain":"Dense Urban",
                      "monitoring_policy":"Monitoring Policy Notes",
                      "monitoring_hierarchy":"3 - whole house monitoring with sub metering & occupant evaluation"}
}
```

#### Response

201 Created

### Delete Entity

#### Create the entity to delete

```
POST http://:hostname/4/entities/
Accept-Encoding: application/json
Authorization: :auth-token
Content-Type: application/json

{"project_id":"8e9972b9-a90f-44bc-89ce-2bb7b2b281ab",
 "property_code":"TOBEDELETED"}
```

Response 201

```json
{
    "headers": {
        "Location": "/4/entities/4c82c5c4-a10e-43d3-8052-ce979ef7ac6a"
    },
    "body": {
        "location": "/4/entities/4c82c5c4-a10e-43d3-8052-ce979ef7ac6a",
        "status": "OK",
        "version": "4"
    }
}
```
#### Delete request

##### Request

```
DELETE http://:hostname/4/entities/4c82c5c4-a10e-43d3-8052-ce979ef7ac6a
Accept-Encoding: application/json
Authorization: :auth-token
```
##### Response

Response 204 No Content
HTTP/1.1 204 No Content
Content-Type: text/plain
Content-Length: 0
Server: http-kit
Date: Mon, 02 Feb 2015 11:36:56 GMT

## Profiles

### Create Profile

#### Request

```
POST http://localhost:8010/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
Accept-Encoding: application/json
Authorization: :auth-token

{
    "profile_data": {
        "event_type": "Planned",
    },
    "timestamp": "2014-01-01T00:00:00.000Z",
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82"
}
```

#### Response

Response 200

```json
{
    "thermal_images": [],
    "profile_data": {
        "event_type": "Planned"
    },
    "small_hydros": [],
    "wind_turbines": [],
    "ventilation_systems": [],
    "walls": [],
    "photovoltaics": [],
    "roofs": [],
    "conservatories": [],
    "chps": [],
    "solar_thermals": [],
    "storeys": [],
    "heat_pumps": [],
    "airflow_measurements": [],
    "extensions": [],
    "low_energy_lights": [],
    "heating_systems": [],
    "profile_id": "f561a7f9-f6c1-478f-b03e-8d1a04dc37da",
    "floors": [],
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "roof_rooms": [],
    "door_sets": [],
    "timestamp": "",
    "biomasses": [],
    "hot_water_systems": [],
    "window_sets": []
}
```

#### Tariff Examples

##### Gas and Electricity Tariffs

```
POST http://localhost:8010/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
Accept-Encoding: application/json
Authorization: :auth-token

{
    "profile_data": {
        "event_type": "Tariff",
    },
    "timestamp": "2014-04-01T00:00:00.000Z",
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "tariff" : [{"type": "Gas Mains",
                 "cost_per_kwh" : 0.0425,
                 "daily_standing_charge": 0.1644,
                 "annual_discount": 5.00},
                {"type": "Electricity Standard",
                 "cost_per_kwh" : 0.13,
                 "daily_standing_charge": 0.2192,
                 "annual_discount": 5.00}]
}
```

##### Electricity Time of Use Tariff

```
POST http://localhost:8010/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
Accept-Encoding: application/json
Authorization: :auth-token

{
    "profile_data": {
        "event_type": "Tariff",
    },
    "timestamp": "2014-04-01T00:00:00.000Z",
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "tariff" : [{"type": "Electricity Time of Use",
                  "cost_per_kwh_on_peak" : 0.13,
                 "cost_per_kwh_off_peak" : 0.13,
                 "daily_standing_charge": 0.2192,
                 "annual_discount": 10.00,
                 "offpeak_periods": [{"start": "00:00", "end": "05:00"},
                                    {"start": "22:00", "end": "23:59"}]}]
}
```


#### Request

```
GET http://localhost:8010/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/f561a7f9-f6c1-478f-b03e-8d1a04dc37da
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

### Edit Profiles

#### Request

```
PUT http://localhost:8010/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/f561a7f9-f6c1-478f-b03e-8d1a04dc37da
Accept-Encoding: application/json
Authorization: :auth-token

{
    "profile_data": {
        "event_type": "Planned",
        "timestamp":""
    },
    "timestamp": "2014-01-01T00:00:00.000Z",
    "profile_id": "f561a7f9-f6c1-478f-b03e-8d1a04dc37da",
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82"
}
```

#### Response

Response 200

```json
{
    "thermal_images": [],
    "profile_data": {
        "event_type": "Planned"
    },
    "small_hydros": [],
    "wind_turbines": [],
    "ventilation_systems": [],
    "walls": [],
    "photovoltaics": [],
    "roofs": [],
    "conservatories": [],
    "chps": [],
    "solar_thermals": [],
    "storeys": [],
    "heat_pumps": [],
    "airflow_measurements": [],
    "extensions": [],
    "low_energy_lights": [],
    "heating_systems": [],
    "profile_id": "f561a7f9-f6c1-478f-b03e-8d1a04dc37da",
    "floors": [],
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "roof_rooms": [],
    "door_sets": [],
    "timestamp": "",
    "biomasses": [],
    "hot_water_systems": [],
    "window_sets": []
}
```

### Get profiles index

#### Request

```
GET http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

### Get profile resource

#### Request

```
GET http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/profiles/f561a7f9-f6c1-478f-b03e-8d1a04dc37da
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Response

Response 200

```json
{
    "thermal_images": [],
    "profile_data": {
        "event_type": "Planned",
        "timestamp": ""
    },
    "small_hydros": [],
    "wind_turbines": [],
    "ventilation_systems": [],
    "walls": [],
    "photovoltaics": [],
    "roofs": [],
    "conservatories": [],
    "chps": [],
    "solar_thermals": [],
    "storeys": [],
    "heat_pumps": [],
    "airflow_measurements": [],
    "extensions": [],
    "low_energy_lights": [],
    "heating_systems": [],
    "profile_id": "f561a7f9-f6c1-478f-b03e-8d1a04dc37da",
    "floors": [],
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "roof_rooms": [],
    "door_sets": [],
    "timestamp": "2014-01-01T00:00:00+0000",
    "biomasses": [],
    "hot_water_systems": [],
    "window_sets": []
}
```

## Devices

### Create device

#### Request

```
POST http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/devices/
Accept-Encoding: application/json
Authorization: :auth-token
Content-Type: application/json

{
  "readings": [
    {
      "user_metadata": {
        "foo": "bar"
      },
      "unit": "kWh",
      "period": "CUMULATIVE",
      "accuracy": "0.1",
      "resolution": "60",
      "type": "electricityConsumption"
    }
  ],
  "description": "Utility Meters",
  "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82"
}
```

#### Response

Response 201

```json
{
    "location": "/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/devices/cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
    "status": "OK",
    "version": "4"
}
```

### GET devices

#### Request

```
GET http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/devices/
Accept-Encoding: application/json
Authorization: :auth-token
```

#### Responses

##### 200 when empty

```json
[]
```

##### After creating Cumulative Sensor

```json
[
    {
        "description": "Utility Meters",
        "readings": [
            {
                "min": null,
                "unit": "kWh",
                "user_metadata": null,
                "accuracy": "0.1",
                "sensor_id": "6fbcadc9-1726-44dc-9896-01d30d7c5719",
                "frequency": null,
                "corrected_unit": null,
                "type": "electricityConsumption_differenceSeries",
                "correction_factor": null,
                "upper_ts": null,
                "correction": null,
                "resolution": "60",
                "alias": null,
                "median": 0.0,
                "status": null,
                "max": null,
                "lower_ts": null,
                "correction_factor_breakdown": null,
                "period": "PULSE",
                "synthetic": true,
                "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
                "actual_annual": false
            },
            {
                "min": null,
                "unit": "kWh",
                "user_metadata": {
                    "foo": "bar"
                },
                "accuracy": "0.1",
                "sensor_id": "b09e97fc-1e16-4609-bf7f-b28730845c69",
                "frequency": null,
                "corrected_unit": null,
                "type": "electricityConsumption",
                "correction_factor": null,
                "upper_ts": null,
                "correction": null,
                "resolution": "60",
                "alias": null,
                "median": 0.0,
                "status": null,
                "max": null,
                "lower_ts": null,
                "correction_factor_breakdown": null,
                "period": "CUMULATIVE",
                "synthetic": false,
                "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
                "actual_annual": false
            }
        ],
        "editable": true,
        "name": null,
        "privacy": null,
        "metering_point_id": null,
        "parent_id": null,
        "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
        "synthetic": false,
        "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
        "location": null,
        "metadata": null
    }
]
```

### Get Device

#### Request
```
GET http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/devices/cf62ea45-7cf8-431b-9bdc-265ad5a42d01
Accept-Encoding: application/json
Authorization: :auth-token
```
#### Response

Response 200

```json
{
    "description": "Utility Meters",
    "readings": [
        {
            "min": null,
            "unit": "kWh",
            "user_metadata": null,
            "accuracy": "0.1",
            "sensor_id": "6fbcadc9-1726-44dc-9896-01d30d7c5719",
            "frequency": null,
            "corrected_unit": null,
            "type": "electricityConsumption_differenceSeries",
            "correction_factor": null,
            "upper_ts": null,
            "correction": null,
            "resolution": "60",
            "alias": null,
            "median": 0.0,
            "status": null,
            "max": null,
            "lower_ts": null,
            "correction_factor_breakdown": null,
            "period": "PULSE",
            "synthetic": true,
            "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
            "actual_annual": false
        },
        {
            "min": null,
            "unit": "kWh",
            "user_metadata": {
                "foo": "bar"
            },
            "accuracy": "0.1",
            "sensor_id": "b09e97fc-1e16-4609-bf7f-b28730845c69",
            "frequency": null,
            "corrected_unit": null,
            "type": "electricityConsumption",
            "correction_factor": null,
            "upper_ts": null,
            "correction": null,
            "resolution": "60",
            "alias": null,
            "median": 0.0,
            "status": null,
            "max": null,
            "lower_ts": null,
            "correction_factor_breakdown": null,
            "period": "CUMULATIVE",
            "synthetic": false,
            "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
            "actual_annual": false
        }
    ],
    "name": null,
    "privacy": null,
    "metering_point_id": null,
    "parent_id": null,
    "entity_id": "2d7d6785-d3d2-4c98-9de9-b5a92fd27a82",
    "synthetic": false,
    "device_id": "cf62ea45-7cf8-431b-9bdc-265ad5a42d01",
    "location": null,
    "metadata": null
}
```

## Measurements

### Get daily rollups

```
GET http://:hostname/4/entities/1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3/devices/5f10b63931593f90a1a08729889a0842deda818c/daily_rollups/CO2?startDate=2012-07-05%2000:00:00&endDate=2014-06-16%2000:00:00
Accept-Encoding: application/json
Authorization: :auth-token
```

### Get hourly rollups

```
GET http://:hostname/4/entities/1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3/devices/5f10b63931593f90a1a08729889a0842deda818c/hourly_rollups/CO2?startDate=2014-01-23%2000:00:00&endDate=2014-01-24%2000:00:00
Accept-Encoding: application/json
Authorization: :auth-token
```

### Get raw measurements

```
GET http://:hostname/4/entities/1d3f8fbcd69bdc40aa6f8b0df1323b44100d99c3/devices/5f10b63931593f90a1a08729889a0842deda818c/measurements/CO2?startDate=2014-01-23%2000:00:00&endDate=2014-01-24%2000:00:00
Accept-Encoding: application/json
Authorization: :auth-token
```

### POST raw measurements

#### Request

```
POST http://:hostname/4/entities/2d7d6785-d3d2-4c98-9de9-b5a92fd27a82/devices/cf62ea45-7cf8-431b-9bdc-265ad5a42d01/measurements/
Accept-Encoding: application/json
Authorization: :auth-token
Content-Type: application/json

{
  "measurements": [
    {
      "value": "0.5",
      "timestamp": "2014-05-12T10:30:00Z",
      "type": "electricityConsumption"
    }
  ]
}
```

#### Response

Response 202 Accepted

```
Accepted
```
