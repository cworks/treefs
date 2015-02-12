package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.core.HttpServiceRequest;
import cworks.treefs.TreeFsClient;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

/**
 * Download a file from TreeFs
 * @author comartin
 */
public class DownloadHandler extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(event.path())) {
            String path = UriHandler.treefsPath(mount, event.path(), "/content");
            payload.setString("path", path);
        } else {
            // to next service
            next.handle(null);
        }

        /*
         * send file to client...
         */
        event.response().sendFile(TreeFs.storageManager(client)
            .retrieveFile(payload.getString("path")));
    }
}
