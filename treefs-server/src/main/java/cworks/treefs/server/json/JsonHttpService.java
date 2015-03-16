package cworks.treefs.server.json;

import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

public abstract class JsonHttpService extends HttpService {

    /**
     * Creates a JsonRequest from HttpRequest and handles directing json response
     *  
     * @param request
     * @param next
     */
    @Override
    public void handle(final HttpRequest request, final Handler<HttpService> next) {

        JsonRequest jsonRequest = new JsonRequest(request);

        pre(jsonRequest);
        handle(jsonRequest);
        post(jsonRequest);
    }
    
    protected void pre(final JsonRequest request) {
        
        
    }
    
    protected void post(final JsonRequest request) {

    }
    
    public abstract void handle(final JsonRequest request);
}
