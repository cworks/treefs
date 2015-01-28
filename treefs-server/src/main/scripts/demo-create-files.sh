#!/bin/bash
source ./config.sh

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_libre.pdf\", \"metadata\":{\"directedBy\":\"Jared Hess\", \"starring\":\"Jack Black\", \"releaseDate\":\"06/16/2006\", \"runningTime\":\"92 minutes\", \"budget\":\"$35 million\", \"boxOffice\":\"$99 million\"}}" \
-F "filename=@nacho_libre.pdf"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_libre.pdf\", \"metadata\":{\"file_type\":\"application/pdf\"}}" \
-F "filename=@nacho_libre.pdf"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_eggs.png\", \"metadata\":{\"file_type\":\"image/png\"}}" \
-F "filename=@nacho_eggs.png"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_fight.pptx\", \"metadata\":{\"file_type\":\"application/vnd.ms-powerpoint\"}}" \
-F "filename=@nacho_fight.pptx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"corn.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@corn.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"eagles_eggs.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@eagles_eggs.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_libre.zip\", \"metadata\":{\"file_type\":\"application/zip\"}}" \
-F "filename=@nacho_libre.zip"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"ouch.docx\", \"metadata\":{\"file_type\":\"application/msword\"}}" \
-F "filename=@ouch.docx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"hooray_nacho.xlsx\", \"metadata\":{\"file_type\":\"application/vnd.ms-excel\"}}" \
-F "filename=@hooray_nacho.xlsx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_hero.jpg\", \"metadata\":{\"file_type\":\"image/jpeg\"}}" \
-F "filename=@nacho_hero.jpg"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"encarnacion.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@encarnacion.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"corn.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@corn.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"ramses.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@ramses.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_1/n3_2/n4_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"eagles_eggs.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@eagles_eggs.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_fight.pptx\", \"metadata\":{\"file_type\":\"application/vnd.ms-powerpoint\"}}" \
-F "filename=@nacho_fight.pptx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_fight.pptx\", \"metadata\":{\"file_type\":\"application/vnd.ms-powerpoint\"}}" \
-F "filename=@nacho_fight.pptx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"religious_man.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@religious_man.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_eggs.png\", \"metadata\":{\"file_type\":\"image/png\"}}" \
-F "filename=@nacho_eggs.png"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_libre.pdf\", \"metadata\":{\"file_type\":\"application/pdf\"}}" \
-F "filename=@nacho_libre.pdf"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"ouch.docx\", \"metadata\":{\"file_type\":\"application/msword\"}}" \
-F "filename=@ouch.docx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"hooray_nacho.xlsx\", \"metadata\":{\"file_type\":\"application/vnd.ms-excel\"}}" \
-F "filename=@hooray_nacho.xlsx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"religious_man.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@religious_man.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"corn.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@corn.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_2/n4_2 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"ouch.docx\", \"metadata\":{\"file_type\":\"application/msword\"}}" \
-F "filename=@ouch.docx"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_libre.pdf\", \"metadata\":{\"file_type\":\"application/pdf\"}}" \
-F "filename=@nacho_libre.pdf"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_eggs.png\", \"metadata\":{\"file_type\":\"image/png\"}}" \
-F "filename=@nacho_eggs.png"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"religious_man.txt\", \"metadata\":{\"file_type\":\"text/plain\"}}" \
-F "filename=@religious_man.txt"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_eggs.png\", \"metadata\":{\"file_type\":\"image/png\"}}" \
-F "filename=@nacho_eggs.png"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"nacho_libre.pdf\", \"metadata\":{\"file_type\":\"application/pdf\"}}" \
-F "filename=@nacho_libre.pdf"

curl -v --dump-header headers.txt \
-X POST http://$address:$port/$treefsroot/$fsid/demo/n1_1/n2_2/n3_3/n4_1 \
-H "treefs-client: $clientid" \
-H "Accept: application/json" \
-F "file={\"name\":\"ouch.docx\", \"metadata\":{\"file_type\":\"application/msword\"}}" \
-F "filename=@ouch.docx"













