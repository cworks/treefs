package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.TreeFsPathExistsException;
import net.cworks.treefs.domain.TreeFsFactory;
import net.cworks.treefs.domain.TreeFsFolder;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import static net.cworks.treefs.TreeFsValidation.isNull;
import static net.cworks.json.Json.Json;

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
            String path = UriHandler.treefsPath(mount, event.path());
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
                String encoded = Json().toJson(folder);
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

