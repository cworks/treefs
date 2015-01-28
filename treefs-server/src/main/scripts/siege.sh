#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST --data '{"siegeVal":1000000, "siegeId":1}' \
http://$address:$port/siege
