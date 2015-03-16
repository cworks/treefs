package cworks.treefs.server.handler;

import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.server.TreeFsVerticle;
import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpException;
import cworks.treefs.server.core.HttpRequest;

/**
 * HttpService to check that URI meets TreeFs requirements, also contains general package-private
 * utility method for use by other HttpServices in this package.
 *
 * @author comartin
 */
public class UriService extends BasicHttpService {
    @Override
    public void handle(HttpRequest event) {
        if(TreeFsValidation.isNull(event.path())) {
            throw new HttpException(404);
        }
        if(!event.path().startsWith(TreeFsVerticle.TREEFS_ROOT)) {
            throw new HttpException(404);
            
        }
    }

    /**
     * Remove treefs root from request URI
     * @param uri
     * @return
     */
    static String removeMount(String mount, String uri) {
        return uri.substring(mount.length());
    }

    /**
     * Return the treefsPath from path that contains an operation (i.e. copy, move, metadata)
     * @param mount
     * @param path
     * @param op
     * @return
     */
    static String treefsPath(String mount, String path, String op) {
        if(TreeFsValidation.isNullOrEmpty(path)) {
            throw new TreeFsException("path cannot be null or empty");
        }
        String fs = removeMount(mount, path);
        if(TreeFsValidation.isNullOrEmpty(op)) {
            return fs;
        } else {
            return fs.replace(op, "");
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
        if(TreeFsValidation.isNullOrEmpty(path)) {
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
