#!/bin/bash
source ./config.sh
curl -v -X POST http://$address:$port/$treefsroot/$fsid/file-upload-test/content \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F 'file={"name":"apple.txt","metadata":{"comment":"An apple a day keeps the Dr. away"}}' \
-F "filename=@mas_rec.txt" &

curl -v -X POST http://$address:$port/$treefsroot/$fsid/file-upload-test/content \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F 'file={"name":"banana.txt","metadata":{"comment":"Monkey Monkey Monkey"}}' \
-F "filename=@mas_rec.txt" &

curl -v -X POST http://$address:$port/$treefsroot/$fsid/file-upload-test/content \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F 'file={"name":"strawberry.txt","metadata":{"comment":"Strawberry shortcake please"}}' \
-F "filename=@mas_rec.txt" &

curl -v -X POST http://$address:$port/$treefsroot/$fsid/file-upload-test/content \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F 'file={"name":"orange.txt","metadata":{"comment":"Dreamscicle is my friend"}}' \
-F "filename=@mas_rec.txt" &

# wait for all uploads to complete
wait