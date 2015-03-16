package cworks.treefs.server.json;

import cworks.treefs.server.core.BasicHttpService;
import cworks.treefs.server.core.HttpRequest;

public abstract class JsonService extends BasicHttpService {

    /**
     * Creates a JsonRequest from HttpRequest and handles directing json response
     *  
     * @param request
     */
    @Override
    public void handle(final HttpRequest request) {

        JsonRequest jsonRequest = new JsonRequest(request);
        before(jsonRequest);
        handle(jsonRequest);
        after(jsonRequest);
    }
    
    protected void before(final JsonRequest request) {
        System.out.println("JsonService.before: " + request.id());
    }
    
    protected void after(final JsonRequest request) {
        System.out.println("JsonService.after: " + request.id());
    }
    
    public abstract void handle(final JsonRequest request);
}
