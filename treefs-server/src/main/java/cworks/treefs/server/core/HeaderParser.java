package cworks.treefs.server.core;

import org.vertx.java.core.Handler;

public class HeaderParser extends HttpService {

    @Override
    public void handle(HttpServiceRequest event, Handler<Object> next) {

        next.handle(null);
    }
}
