package cworks.treefs.server.core;

import org.vertx.java.core.Handler;

public abstract class BasicHttpService extends HttpService {
    
    @Override
    public void handle(HttpRequest request, Handler<HttpService> next) {
        before(request);
        handle(request);
        after(request);
        next.handle(null); // continue
    }
    
    protected void before(HttpRequest request) {
        System.out.println("BasicHttpService.before: " + request.id());
    }
    
    public abstract void handle(HttpRequest request);
    
    protected void after(HttpRequest request) {
        System.out.println("BasicHttpService.after: " + request.id());
    }
}
