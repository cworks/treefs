package cworks.treefs.server.handler;

import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpRequest;
import org.vertx.java.core.Handler;

public class PlaceHolderService extends HttpService {

    @Override
    public void handle(HttpRequest request, Handler<HttpService> next) {
        System.out.println("PlaceHolderService called: " + request.toString());
        next.handle(null);
    }
}
