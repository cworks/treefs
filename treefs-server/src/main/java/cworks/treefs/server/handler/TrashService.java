package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsFolderNotEmptyException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.core.HttpServiceRequest;
import cworks.treefs.TreeFsClient;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

/**
 * HttpService that moves a file or folder into the trash
 * @author comartin
 */
public class TrashService extends HttpService {

    @Override
    public void handle(HttpServiceRequest request, Handler<Object> next) {

        TreeFsClient client = request.get("client");
        JsonObject data = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String path = UriService.treefsPath(mount, request.path(), "/trash");
            // String path = UriHandler.treefsPath(mount, request.path());
            data.setString("path", path);
        } else {
            // next HttpService
            next.handle(null);
        }

        try {
            boolean force = Boolean.valueOf(request.getParameter("forceDelete", "false"));
            TreeFs.storageManager(client).trashPath(data.getString("path"), force);
            request.response().end(simpleResponse(
                200, data.getString("path") + " moved to trash."));
        } catch(TreeFsFolderNotEmptyException ex) {
            request.response().setStatusCode(400);
            next.handle(new TreeFsException("path " + data.getString("path")
                + " NOT empty and forceDelete option NOT given.", ex));
        } catch(TreeFsException ex) {
            request.response().setStatusCode(400);
            next.handle(ex);
        }
    }

    String simpleResponse(Integer statusCode, String message) {
        JsonObject response = new JsonObject();
        JsonObject success = new JsonObject();
        success.setNumber("statusCode", statusCode);
        success.setString("message", message);
        response.setObject("success", success);
        return response.asString();
    }
}
