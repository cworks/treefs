package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsPathExistsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.domain.TreeFsFactory;
import cworks.treefs.domain.TreeFsFile;
import cworks.treefs.server.core.FileUpload;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

/**
 * HttpService than handles uploading files into TreeFs
 * @author comartin
 */
public class UploadService extends HttpService {

    public void handle(final HttpRequest request, Handler<Object> next) {

        FileUpload upload = null;

        try {
            TreeFsClient client = request.get("client");

            if (TreeFsValidation.isNull(request.files()) || request.files().size() < 1) {
                next.handle(new TreeFsException("UploadHander needs a file to upload silly bird"));
            }

            //
            // *NOTE* For now this HttpService only handles the first file
            //
            Iterator it = request.files().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry uploadInfo = (Map.Entry) it.next();
                upload = (FileUpload) uploadInfo.getValue();
                break; // see *NOTE*
            }

            JsonObject payload = null;

            //
            // if request contains a 'file' field use it to override the default filename
            // this allows clients to override the default filename
            //
            if (!TreeFsValidation.isNull(request.formAttributes())) {
                String file = request.formAttributes().get("file");
                if (!TreeFsValidation.isNullOrEmpty(file)) {
                    payload = new JsonObject(file);
                    if (TreeFsValidation.isNullOrEmpty(payload.getString("name"))) {
                        payload = null;
                    }
                }
            }

            //
            // if request did not contain a 'file' field then use default filename given in
            // multi-part upload request.
            //
            if (TreeFsValidation.isNull(payload)) {
                payload = new JsonObject();
                payload.setString("name", upload.filename());
            }

            /*
             * set the relative path for this upload
             */
            String path = UriService.treefsPath(mount, request.path(), "/content");
            payload.setString("path", path);

            /**
             * path to source file that has been uploaded into a temp location
             * now we need to move it into TreeFs...
             */
            Path source = Paths.get(upload.path());
            TreeFsFile treefsFile = null;
            try {
                payload.setString("type", "file");
                treefsFile = TreeFsFactory.deserializer().file(payload.toString());
                // check for overwrite option
                boolean overwrite = Boolean.valueOf(request.params().get("overwrite"));
                treefsFile = TreeFs.storageManager(client).createFile(source, treefsFile, overwrite);
                if (treefsFile != null) {

                    String filepath = TreeFs.storageManager(client).retrieveFile(
                            treefsFile.path() + File.separator + treefsFile.name());
                    payload.setString("filepath", filepath);

                    request.response().end(treefsFile);
                }
            } finally {
                try {
                    Files.deleteIfExists(source);
                } catch (IOException e) { }
            }
        } catch(TreeFsPathExistsException ex) {
            // 400 Bad Request: path exists and overwrite wasn't provided
            request.response().setStatusCode(400);
            next.handle(new TreeFsException(
                "Path " + ex.path() + " exists and overwrite option wasn't provided"));
        } catch(TreeFsException ex) {
            if(TreeFsValidation.isNull(upload)) {
                next.handle(ex);
            } else {
                next.handle(new TreeFsException("File Upload " + upload.filename() + " failed", ex));
            }
        }
    }
}
