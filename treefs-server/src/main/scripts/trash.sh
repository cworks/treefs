#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

[ "$#" -eq 1 ] || finished "Path argument required, $# provided"
treefspath=$1

#
# clean demo, first by moving demo to trash then deleting
#
curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X DELETE http://$address:$port/$treefsroot/$fsid/$treefspath/trash?forceDelete=true
