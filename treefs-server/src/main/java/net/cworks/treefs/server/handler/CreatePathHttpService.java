package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.TreeFsPathExistsException;
import net.cworks.treefs.domain.TreeFsFactory;
import net.cworks.treefs.domain.TreeFsFile;
import net.cworks.treefs.domain.TreeFsFolder;
import net.cworks.treefs.domain.TreeFsPath;
import net.cworks.treefs.server.core.FileUpload;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

import static net.cworks.treefs.TreeFsValidation.isNull;
import static net.cworks.treefs.TreeFsValidation.isNullOrEmpty;
import static net.cworks.json.Json.Json;

/**
 * Service to create a path in TreeFs
 * @author comartin
 */
public class CreatePathHttpService extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        TreeFsClient client = event.get("client");

        JsonObject data = new JsonObject();

        if(!isNull(event.path())) {
            String path = UriHandler.treefsPath(mount, event.path());
            data.setString("path", path);
        } else {
            // continue to next handler because we need a path
            next.handle(null);
        }

        TreeFsPath treefsPath = null;

        try {
            if(isNull(event.files()) || event.files().size() < 1) {
                // no files sent, create path only
                treefsPath = createPath(client, event, data);
            } else {
                // create path with content
                treefsPath = createPathWithContent(client, event, data);
            }

            if(treefsPath != null) {
                String encoded = Json().toJson(treefsPath);
                event.response().end(encoded);
            }

        } catch(TreeFsPathExistsException ex) {
            // 400 Bad Request: path exists and overwrite wasn't provided
            event.response().setStatusCode(400);
            next.handle(new TreeFsException("Path " + ex.path()
                + " exists and overwrite option wasn't provided", ex));
        } catch(TreeFsException ex) {
            event.response().setStatusCode(400);
            next.handle(new TreeFsException("Create Path " + data.getString("path") + " failed", ex));
        }


    }

    TreeFsPath createPath(TreeFsClient client, HttpServiceRequest request, JsonObject data) {

        data.setString("type", "folder");

        if(request.hasBody()) {
            JsonObject body = request.body();
            data.merge(body);
        }

        TreeFsFolder folder = TreeFsFactory.deserializer()
            .folder(data.toString());

        folder = TreeFs.storageManager(client)
            .createFolder(folder);

        return folder;
    }

    TreeFsPath createPathWithContent(TreeFsClient client, HttpServiceRequest request, JsonObject data) {

        FileUpload upload = null;
        data.setString("type", "file");
        //
        // --NOTE-- For now this HttpService only handles the first file
        //
        Iterator it = request.files().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry uploadInfo = (Map.Entry) it.next();
            upload = (FileUpload) uploadInfo.getValue();
            break; // see --NOTE--
        }

        //
        // if request contains a 'file' field use it to override the default filename
        // this allows clients to override the default filename
        //
        JsonObject fileInfo = null;
        if (!isNull(request.formAttributes())) {
            String file = request.formAttributes().get("file");
            if (!isNullOrEmpty(file)) {
                fileInfo = new JsonObject(file);
                if (isNullOrEmpty(fileInfo.getString("name"))) {
                    fileInfo = null;
                }
            }
        }

        //
        // if request did not contain a 'file' field then use default filename given in
        // multi-part upload request.
        //
        if(isNull(fileInfo)) {
            data.setString("name", upload.filename());
        } else {
            data.merge(fileInfo);
        }

        //
        // path to source file that has been uploaded into a temp location
        // now we need to move it into TreeFs...
        //
        Path tempFile = Paths.get(upload.path());
        TreeFsFile treefsFile = null;
        try {
            treefsFile = TreeFsFactory.deserializer().file(data.toString());
            // check for overwrite option
            boolean overwrite = Boolean.valueOf(request.params().get("overwrite"));
            treefsFile = TreeFs.storageManager(client).createFile(tempFile, treefsFile, overwrite);
        } finally {
            try { Files.deleteIfExists(tempFile); } catch (IOException e) { }
        }

        return treefsFile;

    }
}
