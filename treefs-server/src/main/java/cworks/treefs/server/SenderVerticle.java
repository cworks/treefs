package cworks.treefs.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

public class SenderVerticle extends Verticle {

    public void start() {

        // Send a ping message every second
        vertx.setPeriodic(1000, new Handler<Long>() {
            @Override
            public void handle(Long timerID) {
                System.out.println("SenderVerticle: handling request on Thread "
                    + Thread.currentThread().getName());
                vertx.eventBus().send("ping-address", "ping!", new Handler<Message<String>>() {

                    @Override
                    public void handle(Message<String> reply) {
                        System.out.println("SenderVerticle received reply: "
                            + reply.body() + " on Thread "
                            + Thread.currentThread().getName());
                    }
                });
            }
        });

    }

}
