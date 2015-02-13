package cworks.treefs.server.handler;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpServiceRequest;
import cworks.treefs.domain.TreeFsPath;
import org.vertx.java.core.Handler;

/**
 * Move a path to another path
 * @author comartin
 */
public class MoveService extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(event.path())) {
            String source = UriService.treefsPath(mount, event.path(), "/mv");
            logger.debug("moveService on sourcePath: " + source);
            payload.setString("source", source);
        } else {
            // continue to next handler
            next.handle(null);
        }

        if(event.hasBody()) {
            JsonObject body = event.body();
            payload.merge(body);
        }

        TreeFsPath path = TreeFs.storageManager(client).movePath(
            payload.getString("source"),
            payload.getString("target"),
            payload.getArray("moveOptions")
        );

        if(!TreeFsValidation.isNull(path)) {
            event.response().end(Json.asString(path));
        } else {
            next.handle(new TreeFsException(
                "unable to move source path: " + payload.getString("source") +
                " to target path: " + payload.getString("target")));
        }

    }
}
