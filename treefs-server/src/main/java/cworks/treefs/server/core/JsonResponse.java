package cworks.treefs.server.core;

import cworks.json.Json;
import cworks.json.JsonArray;
import cworks.json.JsonElement;
import cworks.json.JsonObject;
import org.vertx.java.core.http.HttpServerResponse;

import java.util.Map;

public class JsonResponse extends HttpResponse {

    /**
     * Create a JsonResponse wrapping the vertx HttpServerResponse
     *
     * @param response vertx response
     * @param context  vertx context variables
     */
    public JsonResponse(HttpServerResponse response, Map<String, Object> context) {
        super(response, context);
        setContentType("application/json");
    }

    public void end(Object data) {
        if(isPrettyEnabled()) {
            super.end(Json.asPrettyString(data));
        } else {
            super.end(Json.asString(data));
        }
    }

    public void end(Object data, String enc) {
        if(isPrettyEnabled()) {
            super.end(Json.asPrettyString(data));
        } else {
            super.end(Json.asString(data));
        }
    }

    public void jsonp(JsonElement json) {
        jsonp("callback", json);
    }

    public void jsonp(String callback, JsonElement json) {

        if (callback == null) {
            // treat as normal json response
            end(json);
            return;
        }

        String body = null;

        if (json != null) {
            if (json.isArray()) {
                JsonArray jsonArray = json.asArray();
                body = jsonArray.asString();
            } else if (json.isObject()) {
                JsonObject jsonObject = json.asObject();
                body = jsonObject.asString();
            }
        }

        jsonp(callback, body);
    }

    public void jsonp(String body) {
        jsonp("callback", body);
    }

    public void jsonp(String callback, String body) {

        if (callback == null) {
            // treat as normal json response
            setContentType("application/json", "UTF-8");
            end(body);
            return;
        }

        if (body == null) {
            body = "null";
        }

        // replace special chars
        body = body.replaceAll("\\u2028", "\\\\u2028").replaceAll("\\u2029", "\\\\u2029");

        // content-type
        setContentType("text/javascript", "UTF-8");
        String cb = callback.replaceAll("[^\\[\\]\\w$.]", "");
        end(cb + " && " + cb + "(" + body + ");");
    }

    /**
     * Return true if pretty is enabled in the context otherwise false.
     * @return
     */
    private boolean isPrettyEnabled() {

        if(this.context == null) {
            return false;
        }

        if(this.context.get("json.pretty") == null) {
            return false;
        }

        return Boolean.valueOf(this.context.get("json.pretty").toString());
    }
}
