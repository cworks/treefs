package cworks.treefs.server.handler;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.core.HttpServiceRequest;
import cworks.treefs.TreeFsClient;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

/**
 * HttpService that handles retrieving information about a folder in TreeFs
 * @author comartin
 */
public class RetrieveFolderService extends HttpService {
    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(event.path())) {
            String path = UriService.treefsPath(mount, event.path());
            payload.setString("path", path);
        } else {
            next.handle(null);
        }

        String depth = event.getParameter("depth", "-1");
        String filter = event.getParameter("filter", null);
        String foldersOnly = event.getParameter("foldersOnly", "false");
        String filesOnly = event.getParameter("filesOnly", "false");
        String recursive = event.getParameter("recursive", "false");

        payload.setNumber("depth", Integer.parseInt(depth));
        payload.setString("filter", filter);
        payload.setBoolean("foldersOnly", Boolean.parseBoolean(foldersOnly));
        payload.setBoolean("filesOnly", Boolean.parseBoolean(filesOnly));
        payload.setBoolean("recursive", Boolean.parseBoolean(recursive));

        TreeFsFolder folder = TreeFs.storageManager(client).retrieveFolder(
            payload.getString("path"), payload);

        if(folder != null) {
            event.response().end(Json.asString(folder));
        } else {
            next.handle(new TreeFsException("unable to retrieve folder: "
                + payload.getString("path")));
        }
    }



}
