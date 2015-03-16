package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.domain.TreeFsPath;
import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpRequest;

/**
 * HttpService to copy a source path to a target path
 *
 * @author comartin
 */
public class CopyService extends BasicHttpService {

    @Override
    public void handle(HttpRequest request) {

        TreeFsClient client = request.get("client");
        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String source = UriService.treefsPath(mount, request.path(), "/cp");
            logger.debug("copyHandler on sourcePath: " + source);
            payload.setString("source", source);
        } else {
            return;
        }

        if(request.hasBody()) {
            JsonObject body = request.body();
            payload.merge(body);
        }

        TreeFsPath path = TreeFs.storageManager(client).copyPath(
            payload.getString("source"),
            payload.getString("target"),
            payload.getArray("copyOptions")
        );

        if(!TreeFsValidation.isNull(path)) {
            request.response().end(path);
        } else {
            throw new TreeFsException(
                "unable to copy source path: " + payload.getString("source") +
                " to target path: " + payload.getString("target"));
        }

    }

}
