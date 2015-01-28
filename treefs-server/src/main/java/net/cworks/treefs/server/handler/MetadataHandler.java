package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import java.util.Map;

import static net.cworks.treefs.TreeFsValidation.isNull;
import static net.cworks.json.Json.Json;

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
        if(!isNull(event.path())) {
            String path = UriHandler.treefsPath(mount, event.path(), "/meta");
            payload.setString("path", path);
        } else {
            // try next HttpService
            next.handle(null);
        }

        // retrieve metadata for the given path if it exists, otherwise send 404 response
        Map<String, Object> metadata = TreeFs.storageManager(client)
            .retrieveMetadata(payload.getString("path"));
        if(!isNull(metadata)) {
            event.response().end(Json().toJson(metadata));
        } else {
            // No metadata found
            event.response().setStatusCode(404).end();
        }
    }
}
