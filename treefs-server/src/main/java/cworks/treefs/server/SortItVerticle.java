package cworks.treefs.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SortItVerticle extends Verticle {

    private static final Random rand = new Random(System.currentTimeMillis());

    public void start() {
        vertx.eventBus().registerHandler("sort-it", new Handler<Message<String>>() {
            @Override
            public void handle(Message<String> message) {
                List<Integer> nums = new ArrayList<Integer>(100);
                for (int i = 0; i < 100; i++) {
                    nums.add(rand.nextInt());
                }
                Collections.sort(nums);
                message.reply(nums.toString());
            }
        });
    }
}
