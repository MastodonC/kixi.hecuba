(ns etl.url)


(def urls {:programme      "http://127.0.0.1:8010/4/programmes/"
           :project        "http://127.0.0.1:8010/4/programmes/6216349fb60ada047e5218dbe7efd68f6f937862/projects/"
           :entity         "http://127.0.0.1:8010/4/entities/"
           :device-1       "http://127.0.0.1:8010/4/entities/821e6367f385d82cc71b2afd9dc2df3b2ec5b81c/devices/"
           :device-2       "http://127.0.0.1:8010/4/entities/14366c761c74592b9926e851bae8a64ece7239ff/devices/"
           :device-3       "http://127.0.0.1:8010/4/entities/9ac7f5635832d843dda594f58525239263ffdd37/devices/"
           :measurement-1  "http://127.0.0.1:8010/4/entities/821e6367f385d82cc71b2afd9dc2df3b2ec5b81c/devices/8c077c2c3eac472d153886244e7b8aa6cad6a7e7/measurements/"
           :measurement-2  "http://127.0.0.1:8010/4/entities/14366c761c74592b9926e851bae8a64ece7239ff/devices/fe5ab5bf19a7265276ffe90e4c0050037de923e2/measurements/"
           :measurement-3  "http://127.0.0.1:8010/4/entities/9ac7f5635832d843dda594f58525239263ffdd37/devices/b4f0c7e2b15ba9636f3fb08379cc4b3798a226bb/measurements/"
           :measurement-4  "http://127.0.0.1:8010/4/entities/9ac7f5635832d843dda594f58525239263ffdd37/devices/268e93a5249c24482ac1519b77f6a45f36a6231d/measurements/"})

(def resources {:device-1 {:entity_id "821e6367f385d82cc71b2afd9dc2df3b2ec5b81c"
                           :description "External air temperature sensor"
                           :readings [{:type "electricityConsumption"
                                       :unit "kWh"
                                       :resolution "60"
                                       :period "PULSE"}]}
                :device-2 {:entity_id "14366c761c74592b9926e851bae8a64ece7239ff"
                           :description "GasMeterPulse"
                           :readings [{:type "gasConsumption"
                                       :unit "m^3"
                                       :resolution "60"
                                       :period "PULSE"}]}
                :device-3 {:entity_id "9ac7f5635832d843dda594f58525239263ffdd37"
                           :description "Heat meter (overall)"
                           :readings [{:type "interpolatedHeatConsumption"
                                       :unit ""
                                       :resolution "60"
                                       :period "CUMULATIVE"}]}
                :device-4 {:entity_id "9ac7f5635832d843dda594f58525239263ffdd37"
                           :description "Heat pump electricity meter"
                           :readings [{:type "interpolatedElectricityConsumption"
                                       :unit ""
                                       :resolution "60"
                                       :period "CUMULATIVE"}]}})
