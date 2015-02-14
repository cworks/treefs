package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

import java.util.Map;

/**
 * HttpService that returns metadata associated with a path in TreeFs
 *
 * @author comartin
 */
public class MetadataService extends HttpService {

    @Override
    public void handle(HttpRequest request, Handler<Object> next) {
        TreeFsClient client = request.get("client");

        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String path = UriService.treefsPath(mount, request.path(), "/meta");
            payload.setString("path", path);
        } else {
            // try next HttpService
            next.handle(null);
        }

        // retrieve metadata for the given path if it exists, otherwise send 404 response
        Map<String, Object> metadata = TreeFs.storageManager(client)
            .retrieveMetadata(payload.getString("path"));
        if(!TreeFsValidation.isNull(metadata)) {
            request.response().end(metadata);
        } else {
            // No metadata found
            request.response().setStatusCode(404).end();
        }
    }
}
