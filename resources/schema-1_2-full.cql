
CREATE KEYSPACE kixi WITH replication = {'class':'SimpleStrategy', 'replication_factor':1};

use kixi;


CREATE TABLE devices (
  device_id uuid,
  description text,
  parent_id uuid,
  entity_id uuid,
  location text,
  metadata text,
  sensors list<text>,

  privacy text,
  meteringPointId text,

  PRIMARY KEY (device_id)
  );

CREATE INDEX ON devices (entity_id);


CREATE TABLE entities (
    entity_id       uuid PRIMARY KEY,
    project_id      uuid,
    user_id         uuid,

    property_data   text,
    documents       list<text>,
    notes           list<text>,
    csv_uploads     list<text>,
    photos          list<text>,

    devices set<uuid>
    );

CREATE INDEX ON entities (project_id);


CREATE TABLE profiles (
    profile_id uuid,
    entity_id uuid,
    profile_data    text,

    thermal_images  list<text>,
    storeys         list<text>,
    walls           list<text>,
    roofs           list<text>,
    floors          list<text>,
    roof_rooms      list<text>,
    window_sets     list<text>,
    door_sets       list<text>,
    extensions      list<text>,
    conservatories  list<text>,
    wind_turbines   list<text>,
    small_hydros    list<text>,
    photovoltaics   list<text>,
    solar_thermals  list<text>,
    heat_pumps      list<text>,
    chps            list<text>,
    heating_systems list<text>,
    hot_water_systems list<text>,
    low_energy_lights list<text>,
    ventilation_systems list<text>,
    airflow_measurements list<text>,

    PRIMARY KEY (profile_id)
  );


CREATE TABLE measurements (
    device_id uuid,
    type varchar,
    month varchar,
    timestamp timestamp,

    value varchar,
    error varchar,

    PRIMARY KEY ((device_id, type, month), timestamp)
 );


CREATE TABLE projects (
    project_id      uuid,
    programme_id    uuid,
    name            varchar,
    description     text,
    project_code    varchar,
    organisation    varchar,
    project_type    varchar,
    type_of         varchar,

    PRIMARY KEY (project_id)
    );

CREATE INDEX ON projects(programme_id);


CREATE TABLE programmes (
     programme_id       uuid,
     name               varchar,
     description        text,
     public_access      boolean,
     lead_organisations set<varchar>,
     home_page_text     text,

     PRIMARY KEY (programme_id)
   );


CREATE TABLE permissions (
    resource_id     varchar,
    resource_type   uuid,
    user_id         varchar,
    role            varchar,

    PRIMARY KEY ((resource_id, resource_type), user_id)
 );


CREATE TABLE users (
    user_id         varchar,

    first_name      varchar,
    last_name       varchar,
    phone_number    varchar,
    organisation    varchar,
    super_admin     boolean,

    crypted_password    varchar,
    password_salt       varchar,
    persistence_token   varchar,
    perishable_token    varchar,

    login_count         int,
    failed_login_count  int,
    last_request_at     timestamp,
    current_login_at    timestamp,
    current_login_ip    varchar,
    last_login_ip       varchar,
    activated_at        timestamp,
    terms_and_conditions boolean,
    created_at          timestamp,

    PRIMARY KEY (user_id)
   );
