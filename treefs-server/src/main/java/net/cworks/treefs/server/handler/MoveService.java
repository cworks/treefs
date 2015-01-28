package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.domain.TreeFsPath;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import static net.cworks.treefs.TreeFsValidation.isNull;
import static net.cworks.json.Json.Json;

/**
 * Move a path to another path
 * @author comartin
 */
public class MoveService extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!isNull(event.path())) {
            String source = UriHandler.treefsPath(mount, event.path(), "/mv");
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

        if(!isNull(path)) {
            event.response().end(Json().toJson(path));
        } else {
            next.handle(new TreeFsException(
                "unable to move source path: " + payload.getString("source") +
                " to target path: " + payload.getString("target")));
        }

    }
}
