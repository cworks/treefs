#!/bin/bash
source ./config.sh
curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
http://$address:$port/$treefsroot/test2?delay=5
