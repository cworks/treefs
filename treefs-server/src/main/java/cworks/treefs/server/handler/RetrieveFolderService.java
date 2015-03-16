package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

/**
 * HttpService that handles retrieving information about a folder in TreeFs
 * @author comartin
 */
public class RetrieveFolderService extends HttpService {
    @Override
    public void handle(HttpRequest request, Handler<HttpService> next) {

        TreeFsClient client = request.get("client");
        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String path = UriService.treefsPath(mount, request.path());
            payload.setString("path", path);
        } else {
            next.handle(null);
        }

        String depth = request.getParameter("depth", "-1");
        String filter = request.getParameter("filter", null);
        String foldersOnly = request.getParameter("foldersOnly", "false");
        String filesOnly = request.getParameter("filesOnly", "false");
        String recursive = request.getParameter("recursive", "false");

        payload.setNumber("depth", Integer.parseInt(depth));
        payload.setString("filter", filter);
        payload.setBoolean("foldersOnly", Boolean.parseBoolean(foldersOnly));
        payload.setBoolean("filesOnly", Boolean.parseBoolean(filesOnly));
        payload.setBoolean("recursive", Boolean.parseBoolean(recursive));

        TreeFsFolder folder = TreeFs.storageManager(client).retrieveFolder(
            payload.getString("path"), payload);

        if(folder != null) {
            request.response().end(folder);
        } else {
            next.handle(new TreeFsException("unable to retrieve folder: "
                + payload.getString("path")));
        }
    }



}
