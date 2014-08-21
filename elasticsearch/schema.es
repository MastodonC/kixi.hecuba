POST http://localhost:9200/entities
{
  "settings" : {}
}

PUT http://localhost:9200/entities/entity/_mapping
{
  "entity" : {
    "properties" : {
      "address" : {
        "type" : "string"
      },
      "address_region" : {
        "type" : "string"
      },
      "age" : {
        "type" : "string"
      },
      "bedroom_count" : {
        "type" : "string"
      },
      "built_form" : {
        "type" : "string"
      },
      "chps" : {
        "type" : "boolean"
      },
      "entity_id" : {
        "type" : "string"
      },
      "full_entity" : {
        "properties" : {
          "address_country" : {
            "type" : "string"
          },
          "address_street_two" : {
            "type" : "string"
          },
          "calculated_fields_labels" : {
            "type" : "string"
          },
          "calculated_fields_last_calc" : {
            "type" : "string"
          },
          "calculated_fields_values" : {
            "type" : "string"
          },
          "devices" : {
            "properties" : {
              "description" : {
                "type" : "string"
              },
              "device_id" : {
                "type" : "string"
              },
              "entity_id" : {
                "type" : "string"
              },
              "location" : {
                "properties" : {
                  "latitude" : {
                    "type" : "string"
                  },
                  "longitude" : {
                    "type" : "string"
                  },
                  "name" : {
                    "type" : "string"
                  }
                }
              },
              "metadata" : {
                "properties" : {
                  "customer_ref" : {
                    "type" : "string"
                  },
                  "foo" : {
                    "type" : "string"
                  },
                  "location" : {
                    "type" : "string"
                  },
                  "mpid" : {
                    "type" : "string"
                  },
                  "passivrole" : {
                    "type" : "string"
                  },
                  "placement" : {
                    "type" : "string"
                  },
                  "sensor_name" : {
                    "type" : "string"
                  },
                  "serial_no" : {
                    "type" : "string"
                  },
                  "site_name" : {
                    "type" : "string"
                  }
                }
              },
              "metering_point_id" : {
                "type" : "string"
              },
              "name" : {
                "type" : "string"
              },
              "parent_id" : {
                "type" : "string"
              },
              "privacy" : {
                "type" : "string"
              },
              "readings" : {
                "properties" : {
                  "accuracy" : {
                    "type" : "string"
                  },
                  "actual_annual" : {
                    "type" : "string"
                  },
                  "alias" : {
                    "type" : "string"
                  },
                  "device_id" : {
                    "type" : "string"
                  },
                  "lower_ts" : {
                    "type" : "string"
                  },
                  "max" : {
                    "type" : "string"
                  },
                  "median" : {
                    "type" : "string"
                  },
                  "min" : {
                    "type" : "string"
                  },
                  "period" : {
                    "type" : "string"
                  },
                  "resolution" : {
                    "type" : "string"
                  },
                  "status" : {
                    "type" : "string"
                  },
                  "synthetic" : {
                    "type" : "string"
                  },
                  "type" : {
                    "type" : "string"
                  },
                  "unit" : {
                    "type" : "string"
                  },
                  "upper_ts" : {
                    "type" : "string"
                  },
                  "user_metadata" : {
                    "properties" : {
                      "passivrole" : {
                        "type" : "string"
                      }
                    }
                  }
                }
              },
              "synthetic" : {
                "type" : "string"
              }
            }
          },
          "documents" : {
            "properties" : {
              "attachable_id" : {
                "type" : "string"
              },
              "attachable_type" : {
                "type" : "string"
              },
              "content_type" : {
                "type" : "string"
              },
              "file_name" : {
                "type" : "string"
              },
              "file_size" : {
                "type" : "string"
              },
              "id" : {
                "type" : "string"
              },
              "name" : {
                "type" : "string"
              },
              "privacy" : {
                "type" : "string"
              },
              "token" : {
                "type" : "string"
              }
            }
          },
          "entity_id" : {
            "type" : "string"
          },
          "notes" : {
            "properties" : {
              "body" : {
                "type" : "string"
              },
              "created_at" : {
                "type" : "string"
              },
              "id" : {
                "type" : "string"
              },
              "notable_id" : {
                "type" : "string"
              },
              "notable_type" : {
                "type" : "string"
              }
            }
          },
          "photos" : {
            "properties" : {
              "path" : {
                "type" : "string"
              }
            }
          },
          "profiles" : {
            "properties" : {
              "biomasses" : {
                "properties" : {
                  "biomass_type" : {
                    "type" : "string"
                  },
                  "capacity" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "est_annual_generation" : {
                    "type" : "string"
                  },
                  "est_percentage_requirement_met" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_mcs_no" : {
                    "type" : "string"
                  },
                  "mcs_no" : {
                    "type" : "string"
                  },
                  "model" : {
                    "type" : "string"
                  },
                  "percentage_efficiency_from_spec" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "chps" : {
                "properties" : {
                  "capacity_elec" : {
                    "type" : "string"
                  },
                  "capacity_thermal" : {
                    "type" : "string"
                  },
                  "chp_type" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "est_annual_generation" : {
                    "type" : "string"
                  },
                  "est_percentage_exported" : {
                    "type" : "string"
                  },
                  "est_percentage_thermal_requirement_met" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_mcs_no" : {
                    "type" : "string"
                  },
                  "mcs_no" : {
                    "type" : "string"
                  },
                  "model" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "conservatories" : {
                "properties" : {
                  "area" : {
                    "type" : "string"
                  },
                  "conservatory_type" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "double_glazed" : {
                    "type" : "string"
                  },
                  "glazed_perimeter" : {
                    "type" : "string"
                  },
                  "height" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "door_sets" : {
                "properties" : {
                  "area" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "door_type" : {
                    "type" : "string"
                  },
                  "door_type_other" : {
                    "type" : "string"
                  },
                  "frame_type" : {
                    "type" : "string"
                  },
                  "frame_type_other" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "location" : {
                    "type" : "string"
                  },
                  "percentage_glazing" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "uvalue" : {
                    "type" : "string"
                  }
                }
              },
              "entity_id" : {
                "type" : "string"
              },
              "extensions" : {
                "properties" : {
                  "age" : {
                    "type" : "string"
                  },
                  "construction_date" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "floors" : {
                "properties" : {
                  "construction" : {
                    "type" : "string"
                  },
                  "construction_other" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "floor_type" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "insulation_product" : {
                    "type" : "string"
                  },
                  "insulation_thickness_one" : {
                    "type" : "string"
                  },
                  "insulation_thickness_two" : {
                    "type" : "string"
                  },
                  "insulation_type" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "uvalue" : {
                    "type" : "string"
                  },
                  "uvalue_derived" : {
                    "type" : "string"
                  }
                }
              },
              "heat_pumps" : {
                "properties" : {
                  "capacity" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "cop" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "depth" : {
                    "type" : "string"
                  },
                  "dhw" : {
                    "type" : "string"
                  },
                  "est_annual_generation" : {
                    "type" : "string"
                  },
                  "est_percentage_dhw_requirement_met" : {
                    "type" : "string"
                  },
                  "est_percentage_requirement_met" : {
                    "type" : "string"
                  },
                  "geology" : {
                    "type" : "string"
                  },
                  "heat_pump_type" : {
                    "type" : "string"
                  },
                  "heat_source_type" : {
                    "type" : "string"
                  },
                  "heat_source_type_other" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_mcs_no" : {
                    "type" : "string"
                  },
                  "make_model" : {
                    "type" : "string"
                  },
                  "mcs_no" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "spf" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "heating_systems" : {
                "properties" : {
                  "bed_index" : {
                    "type" : "string"
                  },
                  "boiler_type" : {
                    "type" : "string"
                  },
                  "boiler_type_other" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "controls" : {
                    "type" : "string"
                  },
                  "controls_make_and_model" : {
                    "type" : "string"
                  },
                  "controls_other" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "efficiency" : {
                    "type" : "string"
                  },
                  "efficiency_derivation" : {
                    "type" : "string"
                  },
                  "emitter" : {
                    "type" : "string"
                  },
                  "fan_flue" : {
                    "type" : "string"
                  },
                  "fuel" : {
                    "type" : "string"
                  },
                  "heat_delivery" : {
                    "type" : "string"
                  },
                  "heat_delivery_source" : {
                    "type" : "string"
                  },
                  "heat_source" : {
                    "type" : "string"
                  },
                  "heat_transport" : {
                    "type" : "string"
                  },
                  "heating_system" : {
                    "type" : "string"
                  },
                  "heating_system_other" : {
                    "type" : "string"
                  },
                  "heating_system_solid_fuel" : {
                    "type" : "string"
                  },
                  "heating_system_solid_fuel_other" : {
                    "type" : "string"
                  },
                  "heating_system_type" : {
                    "type" : "string"
                  },
                  "heating_system_type_other" : {
                    "type" : "string"
                  },
                  "heating_type" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "inspection_date" : {
                    "type" : "string"
                  },
                  "inspector" : {
                    "type" : "string"
                  },
                  "inspector_engineers_name" : {
                    "type" : "string"
                  },
                  "inspector_registration_number" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_engineers_name" : {
                    "type" : "string"
                  },
                  "installer_registration_number" : {
                    "type" : "string"
                  },
                  "make_and_model" : {
                    "type" : "string"
                  },
                  "open_flue" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "trvs_on_emitters" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "use_hours_per_week" : {
                    "type" : "string"
                  }
                }
              },
              "hot_water_systems" : {
                "properties" : {
                  "controls_same_for_all_zones" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "cylinder_capacity" : {
                    "type" : "string"
                  },
                  "cylinder_capacity_other" : {
                    "type" : "string"
                  },
                  "cylinder_insulation_thickness" : {
                    "type" : "string"
                  },
                  "cylinder_insulation_thickness_other" : {
                    "type" : "string"
                  },
                  "cylinder_insulation_type" : {
                    "type" : "string"
                  },
                  "cylinder_insulation_type_other" : {
                    "type" : "string"
                  },
                  "cylinder_thermostat" : {
                    "type" : "string"
                  },
                  "dhw_type" : {
                    "type" : "string"
                  },
                  "fuel" : {
                    "type" : "string"
                  },
                  "fuel_other" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "immersion" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "low_energy_lights" : {
                "properties" : {
                  "bed_index" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "light_type" : {
                    "type" : "string"
                  },
                  "light_type_other" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "proportion" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "photovoltaics" : {
                "properties" : {
                  "area" : {
                    "type" : "string"
                  },
                  "capacity" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "efficiency" : {
                    "type" : "string"
                  },
                  "est_annual_generation" : {
                    "type" : "string"
                  },
                  "est_percentage_exported" : {
                    "type" : "string"
                  },
                  "est_percentage_requirement_met" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_mcs_no" : {
                    "type" : "string"
                  },
                  "inverter_make_model" : {
                    "type" : "string"
                  },
                  "inverter_mcs_no" : {
                    "type" : "string"
                  },
                  "inverter_type" : {
                    "type" : "string"
                  },
                  "make_model" : {
                    "type" : "string"
                  },
                  "mcs_no" : {
                    "type" : "string"
                  },
                  "orientation" : {
                    "type" : "string"
                  },
                  "percentage_roof_covered" : {
                    "type" : "string"
                  },
                  "performance" : {
                    "type" : "string"
                  },
                  "photovoltaic_type" : {
                    "type" : "string"
                  },
                  "photovoltaic_type_other" : {
                    "type" : "string"
                  },
                  "pitch" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "profile_data" : {
                "properties" : {
                  "air_tightness_assessor" : {
                    "type" : "string"
                  },
                  "air_tightness_equipment" : {
                    "type" : "string"
                  },
                  "air_tightness_performed_on" : {
                    "type" : "string"
                  },
                  "air_tightness_rate" : {
                    "type" : "string"
                  },
                  "airtightness_and_ventilation_strategy" : {
                    "type" : "string"
                  },
                  "annual_heating_load" : {
                    "type" : "string"
                  },
                  "annual_space_heating_requirement" : {
                    "type" : "string"
                  },
                  "appliances_strategy" : {
                    "type" : "string"
                  },
                  "bedroom_count" : {
                    "type" : "string"
                  },
                  "ber" : {
                    "type" : "string"
                  },
                  "best_u_value_for_doors" : {
                    "type" : "string"
                  },
                  "best_u_value_for_floors" : {
                    "type" : "string"
                  },
                  "best_u_value_for_other" : {
                    "type" : "string"
                  },
                  "best_u_value_for_roof" : {
                    "type" : "string"
                  },
                  "best_u_value_for_walls" : {
                    "type" : "string"
                  },
                  "best_u_value_for_windows" : {
                    "type" : "string"
                  },
                  "best_u_value_party_walls" : {
                    "type" : "string"
                  },
                  "category" : {
                    "type" : "string"
                  },
                  "cellar_basement_issues" : {
                    "type" : "string"
                  },
                  "co_heating_assessor" : {
                    "type" : "string"
                  },
                  "co_heating_equipment" : {
                    "type" : "string"
                  },
                  "co_heating_loss" : {
                    "type" : "string"
                  },
                  "co_heating_performed_on" : {
                    "type" : "string"
                  },
                  "completeness" : {
                    "type" : "string"
                  },
                  "conservation_issues" : {
                    "type" : "string"
                  },
                  "construction_time_new_build" : {
                    "type" : "string"
                  },
                  "controls_strategy" : {
                    "type" : "string"
                  },
                  "design_guidance" : {
                    "type" : "string"
                  },
                  "draught_proofing" : {
                    "type" : "string"
                  },
                  "draught_proofing_location" : {
                    "type" : "string"
                  },
                  "dwelling_u_value_other" : {
                    "type" : "string"
                  },
                  "electricity_meter_type" : {
                    "type" : "string"
                  },
                  "electricity_storage_present" : {
                    "type" : "string"
                  },
                  "estimated_cost_new_build" : {
                    "type" : "string"
                  },
                  "event_type" : {
                    "type" : "string"
                  },
                  "external_perimeter" : {
                    "type" : "string"
                  },
                  "fabric_energy_efficiency" : {
                    "type" : "string"
                  },
                  "final_cost_new_build" : {
                    "type" : "string"
                  },
                  "flat_floor_heat_loss_type" : {
                    "type" : "string"
                  },
                  "flat_floor_position" : {
                    "type" : "string"
                  },
                  "flat_floors_in_block" : {
                    "type" : "string"
                  },
                  "flat_heat_loss_corridor" : {
                    "type" : "string"
                  },
                  "flat_heat_loss_corridor_other" : {
                    "type" : "string"
                  },
                  "flat_length_sheltered_wall" : {
                    "type" : "string"
                  },
                  "footprint" : {
                    "type" : "string"
                  },
                  "footprint " : {
                    "type" : "string"
                  },
                  "frame_type" : {
                    "type" : "string"
                  },
                  "frame_type_other" : {
                    "type" : "string"
                  },
                  "glazing_area_glass_only" : {
                    "type" : "string"
                  },
                  "glazing_area_percentage" : {
                    "type" : "string"
                  },
                  "gross_internal_area" : {
                    "type" : "string"
                  },
                  "habitable_rooms" : {
                    "type" : "string"
                  },
                  "heat_loss_parameter_hlp" : {
                    "type" : "string"
                  },
                  "heat_storage_present" : {
                    "type" : "string"
                  },
                  "heated_habitable_rooms" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "inadequate_heating" : {
                    "type" : "string"
                  },
                  "innovation_approaches" : {
                    "type" : "string"
                  },
                  "intention_ofpassvhaus" : {
                    "type" : "string"
                  },
                  "intervention_completion_date" : {
                    "type" : "string"
                  },
                  "intervention_description" : {
                    "type" : "string"
                  },
                  "intervention_start_date" : {
                    "type" : "string"
                  },
                  "lighting_strategy" : {
                    "type" : "string"
                  },
                  "mains_gas" : {
                    "type" : "string"
                  },
                  "modelling_software_methods_used" : {
                    "type" : "string"
                  },
                  "moisture_condensation_mould_strategy" : {
                    "type" : "string"
                  },
                  "multiple_glazing_area_percentage" : {
                    "type" : "string"
                  },
                  "multiple_glazing_type" : {
                    "type" : "string"
                  },
                  "multiple_glazing_type_other" : {
                    "type" : "string"
                  },
                  "multiple_glazing_u_value" : {
                    "type" : "string"
                  },
                  "number_of_storeys" : {
                    "type" : "string"
                  },
                  "occupancy_18_to_60" : {
                    "type" : "string"
                  },
                  "occupancy_over_60" : {
                    "type" : "string"
                  },
                  "occupancy_total" : {
                    "type" : "string"
                  },
                  "occupancy_under_18" : {
                    "type" : "string"
                  },
                  "occupant_change" : {
                    "type" : "string"
                  },
                  "onsite_days" : {
                    "type" : "string"
                  },
                  "onsite_days_new_build" : {
                    "type" : "string"
                  },
                  "open_fireplaces" : {
                    "type" : "string"
                  },
                  "orientation" : {
                    "type" : "string"
                  },
                  "overheating_cooling_strategy" : {
                    "type" : "string"
                  },
                  "passive_solar_strategy" : {
                    "type" : "string"
                  },
                  "pipe_lagging" : {
                    "type" : "string"
                  },
                  "planning_considerations" : {
                    "type" : "string"
                  },
                  "primary_energy_requirement" : {
                    "type" : "string"
                  },
                  "profile_air_in_summer" : {
                    "type" : "string"
                  },
                  "profile_air_in_winter" : {
                    "type" : "string"
                  },
                  "profile_bus_report_url" : {
                    "type" : "string"
                  },
                  "profile_bus_summary_index" : {
                    "type" : "string"
                  },
                  "profile_comfort" : {
                    "type" : "string"
                  },
                  "profile_design" : {
                    "type" : "string"
                  },
                  "profile_health" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "profile_image_to_visitors" : {
                    "type" : "string"
                  },
                  "profile_lightning" : {
                    "type" : "string"
                  },
                  "profile_needs" : {
                    "type" : "string"
                  },
                  "profile_noise" : {
                    "type" : "string"
                  },
                  "profile_productivity" : {
                    "type" : "string"
                  },
                  "profile_temperature_in_summer" : {
                    "type" : "string"
                  },
                  "profile_temperature_in_winter" : {
                    "type" : "string"
                  },
                  "property_id" : {
                    "type" : "string"
                  },
                  "renewable_contribution_elec" : {
                    "type" : "string"
                  },
                  "renewable_contribution_heat" : {
                    "type" : "string"
                  },
                  "roof_rooms_present" : {
                    "type" : "string"
                  },
                  "sap_assessor" : {
                    "type" : "string"
                  },
                  "sap_performed_on" : {
                    "type" : "string"
                  },
                  "sap_rating" : {
                    "type" : "string"
                  },
                  "sap_regulations_date" : {
                    "type" : "string"
                  },
                  "sap_software" : {
                    "type" : "string"
                  },
                  "sap_version_issue" : {
                    "type" : "string"
                  },
                  "sap_version_year" : {
                    "type" : "string"
                  },
                  "sealed_fireplaces" : {
                    "type" : "string"
                  },
                  "space_heating_requirement" : {
                    "type" : "string"
                  },
                  "ter" : {
                    "type" : "string"
                  },
                  "thermal_bridging_strategy" : {
                    "type" : "string"
                  },
                  "total_area" : {
                    "type" : "string"
                  },
                  "total_budget" : {
                    "type" : "string"
                  },
                  "total_budget_new_build" : {
                    "type" : "string"
                  },
                  "total_envelope_area" : {
                    "type" : "string"
                  },
                  "total_rooms" : {
                    "type" : "string"
                  },
                  "total_volume" : {
                    "type" : "string"
                  },
                  "used_passivehaus_principles" : {
                    "type" : "string"
                  },
                  "ventilation_approach" : {
                    "type" : "string"
                  },
                  "ventilation_approach_other" : {
                    "type" : "string"
                  },
                  "water_saving_strategy" : {
                    "type" : "string"
                  }
                }
              },
              "profile_id" : {
                "type" : "string"
              },
              "roof_rooms" : {
                "properties" : {
                  "age" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "insulation_date" : {
                    "type" : "string"
                  },
                  "insulation_placement" : {
                    "type" : "string"
                  },
                  "insulation_product" : {
                    "type" : "string"
                  },
                  "insulation_thickness_one" : {
                    "type" : "string"
                  },
                  "insulation_thickness_one_other" : {
                    "type" : "string"
                  },
                  "insulation_thickness_two" : {
                    "type" : "string"
                  },
                  "insulation_thickness_two_other" : {
                    "type" : "string"
                  },
                  "insulation_type" : {
                    "type" : "string"
                  },
                  "location" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "uvalue" : {
                    "type" : "string"
                  },
                  "uvalue_derived" : {
                    "type" : "string"
                  }
                }
              },
              "roofs" : {
                "properties" : {
                  "construction" : {
                    "type" : "string"
                  },
                  "construction_other" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "insulation_date" : {
                    "type" : "string"
                  },
                  "insulation_location_one" : {
                    "type" : "string"
                  },
                  "insulation_location_one_other" : {
                    "type" : "string"
                  },
                  "insulation_location_two" : {
                    "type" : "string"
                  },
                  "insulation_location_two_other" : {
                    "type" : "string"
                  },
                  "insulation_product" : {
                    "type" : "string"
                  },
                  "insulation_thickness_one" : {
                    "type" : "string"
                  },
                  "insulation_thickness_one_other" : {
                    "type" : "string"
                  },
                  "insulation_thickness_two" : {
                    "type" : "string"
                  },
                  "insulation_thickness_two_other" : {
                    "type" : "string"
                  },
                  "insulation_type" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "roof_type" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "uvalue" : {
                    "type" : "string"
                  },
                  "uvalue_derived" : {
                    "type" : "string"
                  }
                }
              },
              "small_hydros" : {
                "properties" : {
                  "capacity" : {
                    "type" : "string"
                  },
                  "est_annual_generation" : {
                    "type" : "string"
                  },
                  "hydro_type" : {
                    "type" : "string"
                  },
                  "make_model" : {
                    "type" : "string"
                  }
                }
              },
              "solar_thermals" : {
                "properties" : {
                  "area" : {
                    "type" : "string"
                  },
                  "capacity" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "est_annual_generation" : {
                    "type" : "string"
                  },
                  "est_percentage_requirement_met" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_mcs_no" : {
                    "type" : "string"
                  },
                  "make_model" : {
                    "type" : "string"
                  },
                  "mcs_no" : {
                    "type" : "string"
                  },
                  "orientation" : {
                    "type" : "string"
                  },
                  "pitch" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "solar_type" : {
                    "type" : "string"
                  },
                  "solar_type_other" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "storeys" : {
                "properties" : {
                  "created_at" : {
                    "type" : "string"
                  },
                  "heat_loss_w_per_k" : {
                    "type" : "string"
                  },
                  "heat_requirement_kwth_per_year" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "storey" : {
                    "type" : "string"
                  },
                  "storey_type" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  }
                }
              },
              "thermal_images" : {
                "properties" : {
                  "attachable_id" : {
                    "type" : "string"
                  },
                  "attachable_type" : {
                    "type" : "string"
                  },
                  "content_type" : {
                    "type" : "string"
                  },
                  "file_name" : {
                    "type" : "string"
                  },
                  "file_size" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "name" : {
                    "type" : "string"
                  },
                  "privacy" : {
                    "type" : "string"
                  },
                  "token" : {
                    "type" : "string"
                  }
                }
              },
              "timestamp" : {
                "type" : "string"
              },
              "user_id" : {
                "type" : "string"
              },
              "ventilation_systems" : {
                "properties" : {
                  "approach" : {
                    "type" : "string"
                  },
                  "approach_other" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "controls" : {
                    "type" : "string"
                  },
                  "controls_other" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "ductwork_type" : {
                    "type" : "string"
                  },
                  "ductwork_type_other" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_engineers_name" : {
                    "type" : "string"
                  },
                  "installer_registration_number" : {
                    "type" : "string"
                  },
                  "manual_control_location" : {
                    "type" : "string"
                  },
                  "manufacturer" : {
                    "type" : "string"
                  },
                  "mechanical_with_heat_recovery" : {
                    "type" : "string"
                  },
                  "operational_settings" : {
                    "type" : "string"
                  },
                  "operational_settings_other" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "total_installed_area" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "ventilation_type" : {
                    "type" : "string"
                  },
                  "ventilation_type_other" : {
                    "type" : "string"
                  }
                }
              },
              "walls" : {
                "properties" : {
                  "area" : {
                    "type" : "string"
                  },
                  "construction" : {
                    "type" : "string"
                  },
                  "construction_other" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "insulation" : {
                    "type" : "string"
                  },
                  "insulation_date" : {
                    "type" : "string"
                  },
                  "insulation_product" : {
                    "type" : "string"
                  },
                  "insulation_thickness" : {
                    "type" : "string"
                  },
                  "insulation_type" : {
                    "type" : "string"
                  },
                  "location" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "uvalue" : {
                    "type" : "string"
                  },
                  "wall_type" : {
                    "type" : "string"
                  }
                }
              },
              "wind_turbines" : {
                "properties" : {
                  "capacity" : {
                    "type" : "string"
                  },
                  "commissioning_date" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "est_annual_generation" : {
                    "type" : "string"
                  },
                  "est_percentage_exported" : {
                    "type" : "string"
                  },
                  "est_percentage_requirement_met" : {
                    "type" : "string"
                  },
                  "height_above_canopy" : {
                    "type" : "string"
                  },
                  "height_above_canpoy" : {
                    "type" : "string"
                  },
                  "hub_height" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "installer" : {
                    "type" : "string"
                  },
                  "installer_mcs_no" : {
                    "type" : "string"
                  },
                  "inverter_make_model" : {
                    "type" : "string"
                  },
                  "inverter_mcs_no" : {
                    "type" : "string"
                  },
                  "inverter_type" : {
                    "type" : "string"
                  },
                  "make_model" : {
                    "type" : "string"
                  },
                  "mcs_no" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "turbine_type" : {
                    "type" : "string"
                  },
                  "turbine_type_other" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "wind_speed" : {
                    "type" : "string"
                  },
                  "wind_speed_info_source" : {
                    "type" : "string"
                  },
                  "wind_speed_info_source_other" : {
                    "type" : "string"
                  }
                }
              },
              "window_sets" : {
                "properties" : {
                  "area" : {
                    "type" : "string"
                  },
                  "created_at" : {
                    "type" : "string"
                  },
                  "frame_type" : {
                    "type" : "string"
                  },
                  "frame_type_other" : {
                    "type" : "string"
                  },
                  "id" : {
                    "type" : "string"
                  },
                  "location" : {
                    "type" : "string"
                  },
                  "percentage_glazing" : {
                    "type" : "string"
                  },
                  "profile_id" : {
                    "type" : "string"
                  },
                  "updated_at" : {
                    "type" : "string"
                  },
                  "uvalue" : {
                    "type" : "string"
                  },
                  "window_type" : {
                    "type" : "string"
                  }
                }
              }
            }
          },
          "programme_id" : {
            "type" : "string"
          },
          "project_id" : {
            "type" : "string"
          },
          "property_code" : {
            "type" : "string"
          },
          "property_data" : {
            "properties" : {
              "address_city" : {
                "type" : "string"
              },
              "address_code" : {
                "type" : "string"
              },
              "address_code_masked" : {
                "type" : "string"
              },
              "address_country" : {
                "type" : "string"
              },
              "address_county" : {
                "type" : "string"
              },
              "address_region" : {
                "type" : "string"
              },
              "address_street" : {
                "type" : "string"
              },
              "address_street_two" : {
                "type" : "string"
              },
              "age" : {
                "type" : "string"
              },
              "built_form" : {
                "type" : "string"
              },
              "built_form_other" : {
                "type" : "string"
              },
              "completeness" : {
                "type" : "string"
              },
              "conservation_area" : {
                "type" : "string"
              },
              "construction_date" : {
                "type" : "string"
              },
              "construction_start_date" : {
                "type" : "string"
              },
              "created_at" : {
                "type" : "string"
              },
              "degree_day_region" : {
                "type" : "string"
              },
              "description" : {
                "type" : "string"
              },
              "design_strategy" : {
                "type" : "string"
              },
              "energy_strategy" : {
                "type" : "string"
              },
              "entity_completeness_6m" : {
                "type" : "string"
              },
              "fuel_poverty" : {
                "type" : "string"
              },
              "id" : {
                "type" : "string"
              },
              "latitude" : {
                "type" : "string"
              },
              "listed" : {
                "type" : "string"
              },
              "locked" : {
                "type" : "string"
              },
              "longitude" : {
                "type" : "string"
              },
              "monitoring_hierarchy" : {
                "type" : "string"
              },
              "monitoring_policy" : {
                "type" : "string"
              },
              "other_notes" : {
                "type" : "string"
              },
              "ownership" : {
                "type" : "string"
              },
              "photo_content_type" : {
                "type" : "string"
              },
              "photo_file_name" : {
                "type" : "string"
              },
              "photo_file_size" : {
                "type" : "string"
              },
              "photo_updated_at" : {
                "type" : "string"
              },
              "practical_completion_date" : {
                "type" : "string"
              },
              "project_id" : {
                "type" : "string"
              },
              "project_phase" : {
                "type" : "string"
              },
              "project_summary" : {
                "type" : "string"
              },
              "project_team" : {
                "type" : "string"
              },
              "property_code" : {
                "type" : "string"
              },
              "property_type" : {
                "type" : "string"
              },
              "property_value" : {
                "type" : "string"
              },
              "property_value_basis" : {
                "type" : "string"
              },
              "retrofit_completion_date" : {
                "type" : "string"
              },
              "retrofit_start_date" : {
                "type" : "string"
              },
              "technology_icons" : {
                "type" : "string"
              },
              "terrain" : {
                "type" : "string"
              },
              "updated_at" : {
                "type" : "string"
              },
              "uuid" : {
                "type" : "string"
              }
            }
          },
          "public_access" : {
            "type" : "string"
          },
          "user_id" : {
            "type" : "string"
          }
        }
      },
      "heat_pumps" : {
        "type" : "boolean"
      },
      "heating_type" : {
        "type" : "string"
      },
      "photovoltaics" : {
        "type" : "boolean"
      },
      "programme_id" : {
        "type" : "string"
      },
      "project_id" : {
        "type" : "string"
      },
      "project_name" : {
        "type" : "string"
      },
      "project_organisation" : {
        "type" : "string"
      },
      "project_team" : {
        "type" : "string"
      },
      "property_code" : {
        "type" : "string"
      },
      "property_type" : {
        "type" : "string"
      },
      "public_access" : {
        "type" : "string"
      },
      "small_hydros" : {
        "type" : "boolean"
      },
      "solar_thermals" : {
        "type" : "boolean"
      },
      "type_of" : {
        "type" : "string"
      },
      "ventilation_systems" : {
        "type" : "boolean"
      },
      "walls_construction" : {
        "type" : "string"
      },
      "wind_turbines" : {
        "type" : "boolean"
      }
    }
  }
 }
