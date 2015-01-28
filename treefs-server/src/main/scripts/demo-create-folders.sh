#!/bin/bash
source ./config.sh

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2/n4_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2/n4_2

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_2

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3/n4_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_2

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_2/n2_1

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_2/n2_2

curl \
-H "Accept: application/json" \
-H "Content-Type: application/json" \
-H "treefs-client: $clientid" \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_3


















