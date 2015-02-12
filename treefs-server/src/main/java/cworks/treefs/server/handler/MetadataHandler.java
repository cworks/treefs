package cworks.treefs.server.handler;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpServiceRequest;
import org.vertx.java.core.Handler;

import java.util.Map;

/**
 * HttpService that returns metadata associated with a path in TreeFs
 *
 * @author comartin
 */
public class MetadataHandler extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        TreeFsClient client = event.get("client");

        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(event.path())) {
            String path = UriHandler.treefsPath(mount, event.path(), "/meta");
            payload.setString("path", path);
        } else {
            // try next HttpService
            next.handle(null);
        }

        // retrieve metadata for the given path if it exists, otherwise send 404 response
        Map<String, Object> metadata = TreeFs.storageManager(client)
            .retrieveMetadata(payload.getString("path"));
        if(!TreeFsValidation.isNull(metadata)) {
            event.response().end(Json.asString(metadata));
        } else {
            // No metadata found
            event.response().setStatusCode(404).end();
        }
    }
}
