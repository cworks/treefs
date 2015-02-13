package cworks.treefs.server.handler;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsPathExistsException;
import cworks.treefs.domain.TreeFsFactory;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpServiceRequest;
import org.vertx.java.core.Handler;

import static cworks.treefs.TreeFsValidation.isNull;

/**
 * HttpService that handles folder creation in TreeFs
 * @author comartin
 */
public class CreateFolderHandler extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        payload.setString("type", "folder");

        if(!isNull(event.path())) {
            String path = UriService.treefsPath(mount, event.path());
            payload.setString("path", path);
        } else {
            // continue to next handler
            next.handle(null);
        }

        if(event.hasBody()) {
            JsonObject body = event.body();
            payload.merge(body);
        }

        TreeFsFolder folder = null;
        try {
            folder = TreeFsFactory.deserializer().folder(payload.toString());
            folder = TreeFs.storageManager(client).createFolder(folder);
            if(folder != null) {
                String encoded = Json.asString(folder);
                event.response().end(encoded);
            }
        } catch(TreeFsPathExistsException ex) {
            // 400 Bad Request: path exists and overwrite wasn't provided
            event.response().setStatusCode(400);
            next.handle(new TreeFsException("Path " + ex.path()
                + " exists and overwrite option wasn't provided"));
        } catch(TreeFsException ex) {
            next.handle(ex);
        }

    }
}

