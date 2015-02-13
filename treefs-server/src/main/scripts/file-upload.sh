#!/bin/bash
source ./config.sh
finished() {
	echo >&2 "$@"
    exit 1
}

[ "$#" -eq 2 ] || finished "Path and filename argument required, $# provided"
treefspath=$1
treefsfilename=$2
curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefspath \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"$treefsfilename\", \"metadata\":{\"directedBy\":\"Jared Hess\", \"starring\":\"Jack Black\", \"releaseDate\":\"06/16/2006\", \"runningTime\":\"92 minutes\", \"budget\":\"$35 million\", \"boxOffice\":\"$99 million\"}}" \
-F "filename=@"$treefsfilename
