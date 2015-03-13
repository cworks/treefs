package cworks.treefs.server.json;

import cworks.json.JsonObject;
import cworks.treefs.server.core.HttpRequest;

public class JsonRequest extends HttpRequest {

    protected JsonResponse response;

    public JsonRequest(final HttpRequest request) {
        super(request);
        this.response = new JsonResponse(request.response(), isPretty());
    }

    /**
     * Write the json content to response and set status code to 200:ok 
     * @param content json content
     */
    public void response(final JsonObject content) {
        response.asJson(content);
    }
    
    public void response(final Object content) {
        response.end(content);
    }

    /**
     * Write the json error to response and set status code to error value
     * @param ex exception
     * @param status http error status
     */
    public void error(final Exception ex, int status) {
        response.asJson(ex, status);
    }
    
    protected Boolean isPretty() {
        return Boolean.valueOf(this.getParameter("pretty", Boolean.toString(false)));
    }
}
