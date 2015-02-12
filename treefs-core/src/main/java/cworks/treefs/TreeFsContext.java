package cworks.treefs;

import java.util.HashMap;
import java.util.Map;

class TreeFsContext {

    private static final TreeFsContext INSTANCE = new TreeFsContext();

    private Map config = null;
    private Map env    = null;

    private TreeFsContext() {
        this.config = new HashMap();
        this.env    = new HashMap();
    }

    public static TreeFsContext context() {
        return INSTANCE;
    }

    public void config(Map config) {
        if(this.config.size() > 1000) {
            // just to protect against insanity
            return;
        }
        this.config.putAll(config);
    }

    public void env(Map env) {
        if(this.env.size() > 1000) {
            // just to protect against insanity
            return;
        }
        this.env.putAll(env);
    }

    public String envString(String envvar) {
        String val = (String)this.env.get(envvar);
        return val;
    }

    public String configString(String configKey) {
        String val = (String)this.config.get(configKey);
        return val;
    }
}
