package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.domain.TreeFsPath;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

/**
 * Move a path to another path
 * @author comartin
 */
public class MoveService extends HttpService {

    @Override
    public void handle(HttpRequest request, Handler<Object> next) {

        TreeFsClient client = request.get("client");
        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String source = UriService.treefsPath(mount, request.path(), "/mv");
            logger.debug("moveService on sourcePath: " + source);
            payload.setString("source", source);
        } else {
            // continue to next handler
            next.handle(null);
        }

        if(request.hasBody()) {
            JsonObject body = request.body();
            payload.merge(body);
        }

        TreeFsPath path = TreeFs.storageManager(client).movePath(
            payload.getString("source"),
            payload.getString("target"),
            payload.getArray("moveOptions")
        );

        if(!TreeFsValidation.isNull(path)) {
            request.response().end(path);
        } else {
            next.handle(new TreeFsException(
                "unable to move source path: " + payload.getString("source") +
                " to target path: " + payload.getString("target")));
        }

    }
}
