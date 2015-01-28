package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.security.TreeFsSecurity;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import org.vertx.java.core.Handler;

/**
 * HttpService that handles verifying ownership and access to a file-system in TreeFs
 * @author comartin
 */
public class FileSystemHandler extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        String path = event.path();
        TreeFsClient client = event.get("client");
        String fs = UriHandler.fileSystem(mount, path);
        if(!TreeFsSecurity.hasFileSystem(client, fs)) {
            next.handle(new TreeFsException("client " + client.id()
                + " does not own a filesystem named " + fs));
        } else {
            next.handle(null);
        }
    }
}
