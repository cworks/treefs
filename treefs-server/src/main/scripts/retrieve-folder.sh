#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

[ "$#" -eq 1 ] || finished "Path argument required, $# provided"
treefspath=$1
curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
http://$address:$port/$fsid/$treefspath
