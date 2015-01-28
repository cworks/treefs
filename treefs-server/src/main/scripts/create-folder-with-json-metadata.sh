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
-X POST --data '{"name":"jsonwithmeta", "description":"A folder of random stuff", "metadata":{"someString":"Happy Happy Happy", "someNumber":100, "someArray":["apples", "bananas", "blueberries"], "someObject": {"name":"Chuck Norris", "Occupation":"Bustin Heads"}}}' \
http://$address:$port/$treefsroot/$fsid/$treefspath
