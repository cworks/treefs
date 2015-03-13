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
    public void handle(HttpRequest request, Handler<Object> next) {

        JsonRequest jsonRequest = new JsonRequest(request);
        handle(jsonRequest);
    }
    
    public abstract void handle(JsonRequest request);
}
