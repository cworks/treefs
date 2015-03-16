package cworks.treefs.server.handler;

import cworks.treefs.TreeFsClient;
import cworks.treefs.TreeFsException;
import cworks.treefs.security.TreeFsSecurity;
import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpRequest;

/**
 * HttpService that handles verifying ownership and access to a file-system in TreeFs
 * @author comartin
 */
public class FileSystemService extends BasicHttpService {

    @Override
    public void handle(HttpRequest event) {
        String path = event.path();
        TreeFsClient client = event.get("client");
        String fs = UriService.fileSystem(mount, path);
        if(!TreeFsSecurity.hasFileSystem(client, fs)) {
            throw new TreeFsException("client " + client.id()
                + " does not own a filesystem named " + fs);
        }
    }
}
