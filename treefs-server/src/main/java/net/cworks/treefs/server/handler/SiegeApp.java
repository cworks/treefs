package net.cworks.treefs.server.handler;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.platform.Verticle;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SiegeApp extends Verticle {

    private static final Random rand = new Random(System.currentTimeMillis());

    private static final DecimalFormat format = new DecimalFormat("##.######");

    @Override
    public void start() {

        vertx.eventBus().registerHandler("siegeapp", new Handler<Message<Long>>() {
            @Override
            public void handle(Message<Long> message) {
                long t1 = System.currentTimeMillis();

                Long siegeVal = message.body();
                List<Integer> numbers = new ArrayList<Integer>();
                for(int i = 0; i < siegeVal; i++) {
                    numbers.add(nextNumber(0, Integer.MAX_VALUE));
                }

                Collections.sort(numbers);

                long t2 = System.currentTimeMillis();
                double duration = (t2 - t1) / 1000.0;


                message.reply(format.format(duration));
            }
        });

    }

    private static int nextNumber(int low, int high) {
        int n = rand.nextInt(high - low) + low;
        return n;
    }
}
