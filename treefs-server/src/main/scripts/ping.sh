#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

pretty=false
if [ $1 == "pretty" ]
then
    pretty=true
fi

curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
http://$address:$port/_ping?pretty=$pretty
