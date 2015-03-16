package cworks.treefs.server.handler;

import cworks.treefs.TreeFsException;
import cworks.treefs.security.TreeFsSecurity;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.TreeFsClient;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

/**
 * HttpService that handles verifying ownership and access to a file-system in TreeFs
 * @author comartin
 */
public class FileSystemService extends HttpService {

    @Override
    public void handle(HttpRequest event, Handler<HttpService> next) {
        String path = event.path();
        TreeFsClient client = event.get("client");
        String fs = UriService.fileSystem(mount, path);
        if(!TreeFsSecurity.hasFileSystem(client, fs)) {
            next.handle(new TreeFsException("client " + client.id()
                + " does not own a filesystem named " + fs));
        } else {
            next.handle(null);
        }
    }
}
