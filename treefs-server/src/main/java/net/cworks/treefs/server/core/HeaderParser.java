package net.cworks.treefs.server.core;

import net.cworks.treefs.TreeFs;
import net.cworks.treefs.TreeFsClient;
import net.cworks.treefs.TreeFsException;
import net.cworks.treefs.security.TreeFsSecurity;
import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;

import static net.cworks.treefs.TreeFsValidation.isNull;
import static net.cworks.treefs.TreeFsValidation.isNullOrEmpty;

public class HeaderParser extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        MultiMap headers = event.headers();
        String clientId = headers.get(TreeFs.clientHeader());
        if(isNullOrEmpty(clientId)) {
            next.handle(new TreeFsException("Cannot find " + TreeFs.clientHeader() + " header."));
        }
        final TreeFsClient client = TreeFs.client(clientId);
        if(isNull(client)) {
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
