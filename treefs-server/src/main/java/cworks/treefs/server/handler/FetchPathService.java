package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsType;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.domain.TreeFsPath;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

/**
 * HttpService that fetches files and folders from TreeFs.
 * event input:
 * 1. event.context
 * 2. event.path
 * 3. event.parameters
 * 4. event.response
 *
 * @author corbett
 */
public class FetchPathService extends HttpService {

    /**
     * Called by the HttpModule framework
     * @param request
     * @param next
     */
    @Override
    public void handle(HttpRequest request, Handler<HttpService> next) {

        TreeFsClient client = request.get("client");
        JsonObject data = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String path = UriService.treefsPath(mount, request.path());
            data.setString("path", path);
        } else {
            next.handle(null);
        }

        TreeFsType treeFsType = TreeFs.storageManager(client).typeOf(data.getString("path"));
        switch(treeFsType) {
            case FOLDER: {
                TreeFsPath treefsPath = fetchFolder(client, request, data);
                if(!TreeFsValidation.isNull(treefsPath)) {
                    request.response().end(treefsPath);
                }
                break;
            }
            case FILE: {
                fetchFile(client, request, data);
                break;
            }
            default: {
                // path was not found in treefs
                request.response().setStatusCode(404).end();
                break;
            }
        }

    }

    /**
     * package-private method used to pull back a file for the given client
     * @param client treefs client
     * @param request api request for a file
     * @param data parameters from the request
     */
    void fetchFile(TreeFsClient client, HttpRequest request, JsonObject data) {

        request.response()
            .sendFile(TreeFs.storageManager(client)
            .retrieveFile(data.getString("path")));

    }

    /**
     * package-private method used to pull back a folder for the given client
     * @param client treefs client
     * @param request api request for a folder
     * @param data parameters from the request
     * @return TreeFsFolder instance
     */
    TreeFsPath fetchFolder(TreeFsClient client, HttpRequest request, JsonObject data) {

        String depth       = request.getParameter("depth", "-1");
        String filter      = request.getParameter("filter", null);
        String foldersOnly = request.getParameter("foldersOnly", "false");
        String filesOnly   = request.getParameter("filesOnly", "false");
        String recursive   = request.getParameter("recursive", "false");

        data.setNumber("depth", Integer.parseInt(depth));
        data.setString("filter", filter);
        data.setBoolean("foldersOnly", Boolean.parseBoolean(foldersOnly));
        data.setBoolean("filesOnly", Boolean.parseBoolean(filesOnly));
        data.setBoolean("recursive", Boolean.parseBoolean(recursive));

        TreeFsFolder folder = TreeFs.storageManager(client).retrieveFolder(
            data.getString("path"), data);

        return folder;
    }
}
