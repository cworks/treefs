package cworks.treefs.server.core;

public class Delay {

    int seconds = 1; // sec

    private Delay(int seconds) {
        this.seconds = seconds;
    }

    public void start() {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public void startThenThrow() {
        try {
            Thread.sleep(seconds * 1000);
            throw new RuntimeException(seconds + "second delay is over");
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    public static Delay create(String delay) {
        int d = Integer.valueOf(delay);
        Delay instance = new Delay(d);
        return instance;
    }


}
