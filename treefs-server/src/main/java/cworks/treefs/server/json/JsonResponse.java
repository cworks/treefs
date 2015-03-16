package cworks.treefs.server.json;

import cworks.json.Json;
import cworks.json.JsonArray;
import cworks.json.JsonElement;
import cworks.json.JsonObject;
import cworks.treefs.server.core.HttpResponse;

public class JsonResponse extends HttpResponse {

    private boolean isPretty;
    
    public JsonResponse(HttpResponse response) {
        this(response, false);
    }

    public JsonResponse(HttpResponse response, boolean pretty) {
        super(response);
        setContentType("application/json");
        isPretty(pretty);
    }
    
    public void asPrettyJson(JsonObject content) {
        JsonObject okWrapper = Json.object()
            .object("response", content)
            .number("status", 200).build();
        
        super.setStatusCode(200);
        super.end(Json.asPrettyString(okWrapper));
    }

    public void asJson(JsonObject content) {
        if(isPretty()) {
            asPrettyJson(content);
        } else {
            JsonObject okWrapper = Json.object()
                .object("response", content)
                .number("status", 200).build();

            super.setStatusCode(200);
            super.end(Json.asString(okWrapper));
        }
    }

    public void asJson(Exception ex, int status) {
        JsonObject errorWrapper = Json.object()
            .string("error", ex.getMessage())
            .number("status", status).build();
        super.setStatusCode(status);
        if (isPretty()) {
            super.end(Json.asPrettyString(errorWrapper));
        } else {
            super.end(Json.asString(errorWrapper));
        }
    }
    
    public void end(Object data) {
        if(isPretty()) {
            super.end(Json.asPrettyString(data));
        } else {
            super.end(Json.asString(data));
        }
    }

    public void end(Object data, String enc) {
        if(isPretty()) {
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

    private void isPretty(boolean pretty) {
        this.isPretty = pretty;
    }

    private boolean isPretty() {
        if(isPretty) {
            return true;
        }

        if(this.context == null) {
            return false;
        }

        if(this.context.get("json.pretty") == null) {
            return false;
        }

        return Boolean.valueOf(this.context.get("json.pretty").toString());
    }


}
