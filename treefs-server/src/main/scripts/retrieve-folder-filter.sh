#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

[ "$#" -eq 3 ] || finished "Path, Filter, Depth argument required, $# provided"
treefspath=$1
treefsfilter=$2
treefsdepth=$3
curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
"http://$address:$port/$treefspath?filter=$treefsfilter&depth=$treefsdepth"
