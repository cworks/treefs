package cworks.treefs.server.handler;

import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.security.TreeFsSecurity;
import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpServiceRequest;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;

/**
 * Pretty simple at this point.  Verify the client is
 * authorized to hang out with TreeFs.
 */
public class AuthorizationService extends HttpService {

    /**
     * Authorize the client or terminal request by throwing an exception
     * which will be propagated to client.
     * @param request
     * @param next
     */
    @Override
    public void handle(HttpServiceRequest request, Handler<Object> next) {

        // check for required treefs-client header
        MultiMap headers = request.headers();
        String clientId = headers.get(TreeFs.clientHeader());

        if(TreeFsValidation.isNullOrEmpty(clientId)) {
            next.handle(new TreeFsException("Cannot find required header: " + TreeFs.clientHeader()));
        }

        if(!TreeFsSecurity.isAuthorized(clientId)) {
            next.handle(new TreeFsException("Cannot verify treefs authorization for client: " + clientId));
        } else {
            request.put("client", TreeFs.client(clientId));
            next.handle(null);
        }
    }
}
