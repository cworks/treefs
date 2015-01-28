package net.cworks.treefs.server.worker;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Worker extends Verticle {

    @Override
    public void start() {

        vertx.eventBus().registerHandler("worker", new Handler<Message<JsonObject>>() {

            @Override
            public void handle(Message<JsonObject> message) {
                JsonObject responseData = new JsonObject();
                responseData.putString("message", "Hi from Worker :-)");
                message.reply(responseData);
            }
        });
    }


}
