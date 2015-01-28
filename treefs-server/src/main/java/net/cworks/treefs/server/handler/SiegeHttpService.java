package net.cworks.treefs.server.handler;

import net.cworks.treefs.server.core.HttpService;
import net.cworks.treefs.server.core.HttpServiceRequest;
import net.cworks.json.JsonObject;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;

import static net.cworks.json.Json.Json;

public class SiegeHttpService extends HttpService {

    @Override
    public void handle(final HttpServiceRequest request, final Handler<Object> next) {

        JsonObject data = request.body();
        final Long siegeVal = data.getLong("siegeVal", 10000);
        final Long siegeId = data.getLong("siegeId", -1);
        vertx.eventBus().send("siegeapp", siegeVal, new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> reply) {
                System.out.println("siegeApp siegeVal=" + siegeVal + " duration=" + reply.body());
                JsonObject response = Json().object()
                    .number("siegeVal", siegeVal)
                    .number("siegeId", siegeId)
                    .string("siegeDuration", reply.body()).build();
                request.response().setStatusCode(200).end(response.asString());
                request.response().close();
            }
        });
    }
}
