-Dtreefs.home=/data/s3

MetadataHandler:
Doesn't make sense to get metadata from TreeFs (unless we maintain all metadata in TreeFs) then turn
around and get it from S3.  Thus this request cannot really be asynchronous because TreeFs must wait
for S3 to return metadata.

TrashHandler:
TreeFs can trash the folder and/or file, return to client then forward trash request to S3 at
a later point in time.

UploadHandler:
TreeFs can upload the folder and/or file into TreeFs, return to client then forward request to S3 at
a later point in time, presumably as soon as possible.

CopyHandler:
TreeFs can copy the folder and/or file, return to client then forward copy request to S3 at a
later point in time, presumably as soon as possible.

DownloadHandler:
Doesn't make sense to get the file from TreeFs because the latest version of a file is not in
TreeFs, rather in S3.  Thus this request cannot really be asynchronous because TreeFs must wait
for S3 to return the file before it can return to client.

CreateFolderHandler:
TreeFs can create the folder, return to client then forward create folder request to S3 at
a later point in time, presumably as soon as possible.

RetrieveFolderHandler:
Doesn't make sense to get folder information from TreeFs (unless we maintain that info for all
folders in TreeFs), then turn around and get it from S3.  This this request cannot really be
asynchronous because TreeFs must wait for S3 to return folder information.

DeleteHandler:
TreeFs can accept the delete request, return to client then forward delete request to S3 at a
later point in time, presumably as soon as possible.

