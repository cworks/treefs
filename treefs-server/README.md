# =============================================================================
# Welcome to the land of TreeFs server
# =============================================================================

1. Background
2. StorageProvider interface
3. Overview
4. SmartVault and Box.com status
5. Demo
6. Code (dev/arch only)
7. And...

# =============================================================================
# Demo run sheet
# =============================================================================

1. Create folders
    1.1. Create one folder using create-folder.sh
        create-folder.sh hello/chuck
    1.2. Create demo folders using demo-create-folders.sh
2. Create files
    2.1. Create one file using file-upload.sh
        file-upload.sh hello/chuck Chuck_Norris.jpg
    2.2. Create demo files using demo-upload-files.sh
3. List and filter folders
    3.1. List one of the demo folders with varying depth (0,1,2,3)
        3.1.1. retrieve-folder-depth.sh demo 0
        3.1.2. retrieve-folder-depth.sh demo 1
        3.1.3. retrieve-folder-depth.sh demo 2
        3.1.4. retrieve-folder-depth.sh demo/n1_1 1
    3.2. Filter demo folders using retrieve-folder-filter.sh
        3.2.1. retrieve-folder-filter.sh demo/n1_1 "*.pdf" 1
4. Get a file
    4.1. Retrieve a file from TreeFs using file-download.sh
        file-download.sh demo/n1_1/nacho_libre.pdf mynacho.pdf
        file-download.sh demo/n1_1/n2_1/n3_2/n4_1/encarnacion.txt encar.txt
5. Get metadata from file/folder path-metadata.sh
    5.1. path-metadata.sh demo/nacho_libre.pdf
    5.2. path-metadata.sh demo/n1_1/n2_1/eagles_eggs.txt
6. Copy a folder and/or file copy-path.sh
    6.1. Copy a file (first create new folder)
        create-folder.sh hello/newplace
        copy-path.sh demo/n1_1/n2_1/nacho_libre.zip hello/newplace
    6.2. Copy a folder into a folder: copy-path.sh demo/n1_1 hello
    6.3. Copy folder content into a folder: copy-path-recursive.sh demo/n1_1/n2_1 hello
7. Trash stuff trash.sh
    trash.sh demo/n1_1/n2_1/n3_1/hooray_nacho.xlsx
    trash.sh demo/n1_1/n2_1/n3_1/ouch.docx
    trash.sh demo/n1_1/n2_1/n3_1
8. Delete a path delete.sh
    delete-path.sh demo/n1_1/n2_1/n3_1/hooray_nacho.xlsx
    delete-path.sh demo/n1_1/n2_1/n3_1

# =============================================================================
# Configuring treefs-server
# =============================================================================
treefs-server is a vertx application (see www.vertx.io) that runs on the server side of treefs.  Its
purpose in life is to handle store and retrieval operations from TreeFs-clients.

# =============================================================================
# Notes on TIME_WAIT(s)
# =============================================================================
A TCP connection is specified by the tuple (source IP, source port, destination IP, destination port).

The reason why there is a TIME_WAIT state following session shutdown is because there may still
be live packets out in the network on their way to you (or from you which may solicit a response of some sort).
If you were to re-create that same tuple and one of those packets showed up, it would be treated as a valid packet
for your connection (and probably cause an error due to sequencing).

So the TIME_WAIT time is generally set to double the packets maximum age.
This value is the maximum age your packets will be allowed to get to before the network discards them.

That guarantees that, before you're allowed to create a connection with the same tuple,
all the packets belonging to previous incarnations of that tuple will be dead.

# =============================================================================
# Startup script options
# =============================================================================
#VERTX_OPTS="$VERTX_OPTS -Dorg.vertx.logger-delegate-factory-class-name=org.vertx.java.core.logging.impl.Log4jLogDelegateFactory "
#VERTX_OPTS="$VERTX_OPTS -Dlog4j.configuration=$TREEFS_MOD/log4j.properties"
