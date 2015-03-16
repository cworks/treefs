package cworks.treefs.server.handler;

import cworks.treefs.server.conf.Settings;
import cworks.treefs.server.core.HttpRequest;
import cworks.treefs.server.core.HttpService;
import org.vertx.java.core.Handler;

public class SettingsService extends HttpService {

    @Override
    public void handle(HttpRequest request, Handler<HttpService> next) {

        System.out.println("SettingsService: " + request.path());
        System.out.println("Settings: " + Settings.asString());
    }
}
