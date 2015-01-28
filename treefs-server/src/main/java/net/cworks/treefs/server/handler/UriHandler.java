package net.cworks.treefs.server.handler;

import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.server.TreeFsServer;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import org.vertx.java.core.Handler;

import static net.cworks.treefs.TreeFsValidation.isNull;
import static net.cworks.treefs.TreeFsValidation.isNullOrEmpty;

/**
 * HttpService to check that URI meets TreeFs requirements, also contains general package-private
 * utility method for use by other HttpServices in this package.
 * @author comartin
 */
public class UriHandler extends HttpService {
    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {
        if(isNull(event.path())) {
            next.handle(404);
        }
        if(!event.path().startsWith(TreeFsServer.TREEFS_ROOT)) {
            next.handle(404);
        }
        next.handle(null);
    }

    /**
     * Remove treefs root from request URI
     * @param uri
     * @return
     */
    static String removeMount(String mount, String uri) {
        return uri.substring(mount.length() + 1);
    }

    /**
     * Return the treefsPath from path that contains an operation (i.e. copy, move, metadata)
     * @param mount
     * @param path
     * @param op
     * @return
     */
    static String treefsPath(String mount, String path, String op) {
        if(isNullOrEmpty(path)) {
            throw new TreeFsException("path cannot be null or empty");
        }
        String fs = removeMount(mount, path);
        String[] parts = fs.split("/");
        String treefsPath = null;
        if(parts != null && parts.length > 0) {
            treefsPath = fs.substring(parts[0].length() + 1);
        }

        if(isNullOrEmpty(op)) {
            return treefsPath;
        } else {
            return treefsPath.replace(op, "");
        }
    }

    /**
     * Return the treefsPath from path that does not contain an operation
     * @param mount
     * @param path
     * @return
     */
    static String treefsPath(String mount, String path) {
        String treefsPath = treefsPath(mount, path, null);
        return treefsPath;
    }

    /**
     * Take a path and pulls out the fileSystem path parameter
     * @param mount /treefs
     * @param path /treefs/fileSys/...
     * @return
     */
    static String fileSystem(String mount, String path) {
        if(isNullOrEmpty(path)) {
            throw new TreeFsException("path cannot be null or empty");
        }
        String fs = removeMount(mount, path);
        String[] parts = fs.split("/");
        if(parts != null && parts.length > 0) {
            fs = parts[0];
        }
        return fs;
    }
}
