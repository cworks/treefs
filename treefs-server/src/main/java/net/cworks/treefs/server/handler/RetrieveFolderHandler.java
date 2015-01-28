package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.domain.TreeFsFolder;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import static net.cworks.treefs.TreeFsValidation.isNull;
import static net.cworks.json.Json.Json;

/**
 * HttpService that handles retrieving information about a folder in TreeFs
 * @author comartin
 */
public class RetrieveFolderHandler extends HttpService {
    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        TreeFsClient client = event.get("client");
        JsonObject payload = new JsonObject();
        if(!isNull(event.path())) {
            String path = UriHandler.treefsPath(mount, event.path());
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
            event.response().end(Json().toJson(folder));
        } else {
            next.handle(new TreeFsException("unable to retrieve folder: "
                + payload.getString("path")));
        }
    }



}
