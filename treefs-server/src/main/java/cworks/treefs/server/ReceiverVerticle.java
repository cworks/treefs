package cworks.treefs.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

public class ReceiverVerticle extends Verticle {

    public void start() {
        vertx.eventBus().registerHandler("ping-address", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                // Reply to it
                System.out.println("ReceiverVerticle received message: "
                    + message.body() + " on Thread "
                    + Thread.currentThread().getName());
                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) { }
                message.reply("pong!");
            }
        });
    }
}
