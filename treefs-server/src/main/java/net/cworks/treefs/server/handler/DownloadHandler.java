package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import static net.cworks.treefs.TreeFsValidation.isNull;

/**
 * Download a file from TreeFs
 * @author comartin
 */
public class DownloadHandler extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!isNull(event.path())) {
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
