package net.cworks.treefs.client.builder.ls;

import net.cworks.treefs.client.Config;
import net.cworks.treefs.client.builder.framework.FluentInvoker;

public class LsBuilder {

    /**
     * Config instance is required because it contains information needed by treefs-server
     */
    private Config config = null;

    /**
     * Use create method
     * @param config
     */
    private LsBuilder(Config config) {
        this.config = config;
    }

    public static LsBuilder lsApi(Config config) {
        return new LsBuilder(config);
    }

    /**
     * Public ls operation builder.
     * @param path
     * @return
     */
    public Start ls(String path) {
        if (path == null) {
            throw new IllegalArgumentException("I can't list a null path silly monkey.");
        }

        LsApi lsOp = new Ls(path, config);

        FluentInvoker invoker = new FluentInvoker(lsOp, Start.class);
        return invoker.proxy();
    }


}

