package cworks.treefs.client.builder.folder;

import cworks.treefs.client.Config;
import cworks.treefs.client.builder.framework.FluentInvoker;

public class FolderBuilder {

    private Config config = null;

    /**
     *
     * @param config
     */
    private FolderBuilder(Config config) {
        this.config = config;
    }

    public static FolderBuilder folderApi(Config config) {
        return new FolderBuilder(config);
    }


    /**
     * Start building a new Folder in TreeFs
     * @param path
     * @return
     */
    public Start newFolder(String path) {
        if (path == null) {
            throw new IllegalArgumentException("I can't create null folders silly bird.");
        }

        FolderApi folderOp = new Folder(path, config);

        FluentInvoker invoker = new FluentInvoker(folderOp, Start.class);
        return invoker.proxy();
    }
}
