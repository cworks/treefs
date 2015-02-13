#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

[ "$#" -eq 2 ] || finished "Path and Depth argument required, $# provided"
treefspath=$1
treefsdepth=$2
curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
http://$address:$port/$treefspath?depth=$treefsdepth
