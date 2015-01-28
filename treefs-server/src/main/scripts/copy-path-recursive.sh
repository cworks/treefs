#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

[ "$#" -eq 2 ] || finished "source & target Path argument required, $# provided"
sourcePath=$1
targetPath=$2

curl -i -v \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST --data "{\"target\":\"$targetPath\", \"copyOptions\":[\"recursive\"]}" \
http://$address:$port/$treefsroot/$fsid/$sourcePath/cp
