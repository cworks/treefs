package cworks.treefs.server.json;

import cworks.json.JsonArray;
import cworks.json.JsonElement;
import cworks.json.JsonObject;
import cworks.treefs.server.core.HttpResponse;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.Map;

public class JsonHttpResponse extends HttpResponse {

    /**
     * Create a JsonHttpResponse from vertx HttpServerResponse
     *
     * @param response vertx response
     * @param context  vertx context variables
     */
    public JsonHttpResponse(HttpServerResponse response, Map<String, Object> context) {
        super(response, context);
        setContentType("application/json", "UTF-8");
    }

    /**
     * End the response by writing JSON into body and setting application/json contentType header
     * @param json JSON element to serialize
     */
    public void end(JsonElement json) {
        if (json.isArray()) {
            JsonArray jsonArray = json.asArray();
            end(jsonArray.asString());
        } else if (json.isObject()) {
            JsonObject jsonObject = json.asObject();
            end(jsonObject.asString());
        }
    }
}
