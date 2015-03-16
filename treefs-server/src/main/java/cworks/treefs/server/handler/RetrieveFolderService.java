package cworks.treefs.server.handler;

import cworks.json.JsonObject;
import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.domain.TreeFsFolder;
import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpRequest;

/**
 * HttpService that handles retrieving information about a folder in TreeFs
 * @author comartin
 */
public class RetrieveFolderService extends BasicHttpService {
    
    @Override
    public void handle(HttpRequest request) {

        TreeFsClient client = request.get("client");
        JsonObject payload = new JsonObject();
        if(!TreeFsValidation.isNull(request.path())) {
            String path = UriService.treefsPath(mount, request.path());
            payload.setString("path", path);
        } else {
            return;
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
            throw new TreeFsException("unable to retrieve folder: "
                + payload.getString("path"));
        }
    }



}
