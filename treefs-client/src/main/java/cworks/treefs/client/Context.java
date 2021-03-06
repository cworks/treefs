package cworks.treefs.client;

import cworks.treefs.client.builder.folder.Start;
import cworks.treefs.client.builder.TreeFsFileBuilder;

import java.io.File;

import static cworks.treefs.client.builder.folder.FolderBuilder.folderApi;
import static cworks.treefs.client.builder.ls.LsBuilder.lsApi;

public class Context {

    /**
     * TreeFs Client configuration associated with this Context instance
     */
    private Config config = null;

    /**
     * Boot this baby up with the given Config instance...word.
     * @param config
     */
    public Context(Config config) {
        this.config = config;
    }

    /**
     * config API
     * @return
     */
    public Config config() {
        return config;
    }

    /**
     * ls API
     * @param path
     * @return
     */
    public cworks.treefs.client.builder.ls.Start ls(String path) {

        return lsApi(config).ls(path);
    }

    /**
     * newPath API
     * @param path
     * @return
     */
    public Start newPath(String path) {

        return folderApi(config).newFolder(path);
    }

    /**
     * newFile API
     * @param file
     * @return
     */
    public TreeFsFileBuilder.Start newFile(File file) {

        return TreeFsFileBuilder.newFile(file);
    }



}
