package net.cworks.treefs.server.handler;

import net.cworks.treefs.common.dt.ISO8601DateParser;
import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;

import java.util.Date;

import static net.cworks.json.Json.Json;

public class PingService extends HttpService {
    @Override
    public void handle(HttpServiceRequest event,
        Handler<Object> next) {

        JsonObject response = Json().object()
            .string("app", "treefs")
            .string("version", "1.0.0")
            .string("time", ISO8601DateParser.toString(new Date()))
            .build();

        event.response().setStatusCode(200)
            .end(response.asString());
        event.response().close();
    }


}
