package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsFolderNotEmptyException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpRequest;

/**
 * HttpService that moves a file or folder into the trash
 * @author comartin
 */
public class TrashService extends BasicHttpService {

    @Override
    public void handle(HttpRequest request) {

        TreeFsClient client = request.get("client");
        JsonObject data = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String path = UriService.treefsPath(mount, request.path(), "/trash");
            // String path = UriHandler.treefsPath(mount, request.path());
            data.setString("path", path);
        } else {
            return;
        }

        try {
            boolean force = Boolean.valueOf(request.getParameter("forceDelete", "false"));
            TreeFs.storageManager(client).trashPath(data.getString("path"), force);
            request.response().end(simpleResponse(
                200, data.getString("path") + " moved to trash."));
        } catch(TreeFsFolderNotEmptyException ex) {
            request.response().setStatusCode(400);
            throw new TreeFsException("path " + data.getString("path")
                + " NOT empty and forceDelete option NOT given.", ex);
        } catch(TreeFsException ex) {
            request.response().setStatusCode(400);
            throw ex;
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
