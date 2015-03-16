package cworks.treefs.server.core;

import org.vertx.java.core.Handler;

public class HeaderParser extends HttpService {

    @Override
    public void handle(HttpRequest request, Handler<HttpService> next) {

        // add x-powered-by header is enabled
        Boolean poweredBy = request.get("x-powered-by");
        if (poweredBy != null && poweredBy) {
            request.response().putHeader("x-powered-by", "TreeFs");
        }
        
        next.handle(null);
    }
}
