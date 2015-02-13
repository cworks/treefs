package cworks.treefs.server.handler;

import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpServiceRequest;
import org.vertx.java.core.Handler;

public class NullService extends HttpService {

    @Override
    public void handle(HttpServiceRequest request, Handler<Object> next) {
        next.handle(null);
    }
}
