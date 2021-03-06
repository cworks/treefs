package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsPathExistsException;
import cworks.treefs.domain.TreeFsFactory;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpRequest;

import static cworks.treefs.TreeFsValidation.isNull;

/**
 * HttpService that handles folder creation in TreeFs
 * @author comartin
 */
public class CreateFolderHandler extends BasicHttpService {

    @Override
    public void handle(HttpRequest request) {
        TreeFsClient client = request.get("client");
        JsonObject payload = new JsonObject();
        payload.setString("type", "folder");

        if(!isNull(request.path())) {
            String path = UriService.treefsPath(mount, request.path());
            payload.setString("path", path);
        } else {
            return;
        }

        if(request.hasBody()) {
            JsonObject body = request.body();
            payload.merge(body);
        }

        TreeFsFolder folder = null;
        try {
            folder = TreeFsFactory.deserializer().folder(payload.toString());
            folder = TreeFs.storageManager(client).createFolder(folder);
            if(folder != null) {
                request.response().end(folder);
            }
        } catch(TreeFsPathExistsException ex) {
            // 400 Bad Request: path exists and overwrite wasn't provided
            request.response().setStatusCode(400);
            throw new TreeFsException("Path " + ex.path()
                + " exists and overwrite option wasn't provided");
        }

    }
}

