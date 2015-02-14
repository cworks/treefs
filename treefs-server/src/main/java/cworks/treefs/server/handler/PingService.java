package cworks.treefs.server.handler;

import cworks.json.Json;
import cworks.json.JsonObject;
import cworks.treefs.server.core.HttpService;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.common.dt.ISO8601DateParser;
import org.vertx.java.core.Handler;

import java.util.Date;

public class PingService extends HttpService {
    @Override
    public void handle(HttpRequest event,
        Handler<Object> next) {

        JsonObject response = Json.object()
            .string("app", "treefs")
            .string("version", "1.0.0")
            .string("time", ISO8601DateParser.toString(new Date()))
            .build();

        event.response().setStatusCode(200)
            .end(response.asString());
        event.response().close();
    }


}
