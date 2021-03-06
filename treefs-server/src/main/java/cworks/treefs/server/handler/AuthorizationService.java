package cworks.treefs.server.handler;

import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.security.TreeFsSecurity;
import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpRequest;
import org.vertx.java.core.MultiMap;

/**
 * Pretty simple at this point.  Verify the client is
 * authorized to hang out with TreeFs.
 */
public class AuthorizationService extends BasicHttpService {

    /**
     * Authorize the client or terminal request by throwing an exception
     * which will be propagated to client.
     * @param request
     */
    @Override
    public void handle(HttpRequest request) {

        // check for required treefs-client header
        MultiMap headers = request.headers();
        String clientId = headers.get(TreeFs.clientHeader());

        if(TreeFsValidation.isNullOrEmpty(clientId)) {
            throw new TreeFsException("Cannot find required header: " + TreeFs.clientHeader());
        }

        if(!TreeFsSecurity.isAuthorized(clientId)) {
            throw new TreeFsException("Cannot verify treefs authorization for client: " + clientId);
        } 
        
        request.put("client", TreeFs.client(clientId));
    }
}
