#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

[ "$#" -eq 2 ] || finished "Path argument and saveAsFile name required, $# provided"
treefspath=$1
saveAsFile=$2

curl --dump-header headers.txt -o $saveAsFile \
-H "Accept: application/json" \
-H "treefs-client: $clientid" \
http://$address:$port/$treefsroot/$fsid/$treefspath
