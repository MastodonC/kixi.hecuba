#!/bin/sh

ES_HOST=localhost
ES_PORT=9200

ES_URI_BASE="http://${ES_HOST}:${ES_PORT}"

ES_SRC_BASE="${0%/*}/../elasticsearch"

echo "Deleting existing entities"
curl -X DELETE ${ES_URI_BASE}/entities
echo -e "\nDONE"

echo "Creating entities"
curl -X POST --data-binary "@${ES_SRC_BASE}/entities.json" ${ES_URI_BASE}/entities
echo -e "\nDONE"

echo "Creating entities mapping"
curl -X PUT --data-binary "@${ES_SRC_BASE}/entities-mapping.json" ${ES_URI_BASE}/entities/entity/_mapping
echo -e "\nDONE"
