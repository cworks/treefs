package cworks.treefs.server.conf;

import cworks.json.Json;
import cworks.json.JsonObject;

public class Settings {

    private static JsonObject settings = Json.object().build();

    public static void enable(String setting) {
        settings.setBoolean(setting, true);
    }

    public static void disable(String setting) {
        settings.setBoolean(setting, false);
    }

    public static boolean enabled(String setting) {
        return settings.getBoolean(setting);
    }

    public static boolean disabled(String setting) {
        return !settings.getBoolean(setting);
    }

    public static String asString() {
        return Json.asPrettyString(settings);
    }
}
