package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import static net.cworks.treefs.TreeFsValidation.isNull;

/**
 * HttpService that handles permanently deleting folders and files from TreeFs
 * @author comartin
 */
public class DeleteHandler extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!isNull(event.path())) {
            String path = UriHandler.treefsPath(mount, event.path());
            payload.setString("path", path);
        } else {
            // continue to next handler
            next.handle(null);
        }

        TreeFs.storageManager(client).deletePath(payload.getString("path"));
        event.response().setStatusCode(200).end();
    }

}
