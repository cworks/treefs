package cworks.treefs.server.core;

import cworks.treefs.TreeFs;
import cworks.treefs.TreeFsException;
import cworks.treefs.TreeFsValidation;
import cworks.treefs.security.TreeFsSecurity;
import cworks.treefs.TreeFsClient;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;

public class HeaderParser extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        MultiMap headers = event.headers();
        String clientId = headers.get(TreeFs.clientHeader());
        if(TreeFsValidation.isNullOrEmpty(clientId)) {
            next.handle(new TreeFsException("Cannot find " + TreeFs.clientHeader() + " header."));
        }
        final TreeFsClient client = TreeFs.client(clientId);
        if(TreeFsValidation.isNull(client)) {
            next.handle(new TreeFsException("Cannot create client: " + clientId));
        }

        if(!TreeFsSecurity.isAuthorized(client)) {
            next.handle(new TreeFsException("Cannot verify authorization for client: " + client.id()));
        } else {
            event.put("client", client);
            next.handle(null);
        }
    }
}
