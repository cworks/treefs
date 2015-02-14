package cworks.treefs.server.core;

import cworks.treefs.server.worker.Work;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;

public class EventBusService extends HttpService {

    private Work worker = null;

    public EventBusService(Work worker) {
        this.worker = worker;
    }

    /**
     * Pull out data from request thats needed to perform work and send event to endpoint
     * to perform work then handle response hear.
     *
     * @param request
     * @param next
     */
    public void handle(final HttpRequest request, final Handler<Object> next) {

        JsonObject data = new JsonObject();
        vertx.eventBus().send("worker", data, new Handler<Message<JsonObject>>() {

            @Override
            public void handle(Message<JsonObject> reply) {
                request.response().setStatusCode(200).end(reply.body().encode());
                request.response().close();
            }
        });


    }

}
