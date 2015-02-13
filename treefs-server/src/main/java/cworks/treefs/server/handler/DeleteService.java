package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpServiceRequest;
import org.vertx.java.core.Handler;

import static cworks.treefs.TreeFsValidation.isNull;

/**
 * HttpService that handles permanently deleting folders and files from TreeFs
 * @author comartin
 */
public class DeleteService extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!isNull(event.path())) {
            String path = UriService.treefsPath(mount, event.path());
            payload.setString("path", path);
        } else {
            // continue to next handler
            next.handle(null);
        }

        TreeFs.storageManager(client).deletePath(payload.getString("path"));
        event.response().setStatusCode(200).end();
    }

}
