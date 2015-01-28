package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.TreeFsFolderNotEmptyException;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import static net.cworks.treefs.TreeFsValidation.isNull;

/**
 * HttpService that moves a file or folder into the trash
 * @author comartin
 */
public class TrashHandler extends HttpService {

    @Override
    public void handle(HttpServiceRequest request, Handler<Object> next) {

        TreeFsClient client = request.get("client");
        JsonObject data = new JsonObject();
        if(!isNull(request.path())) {
            String path = UriHandler.treefsPath(mount, request.path(), "/trash");
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
