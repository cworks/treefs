package net.cworks.treefs.client.builder.folder;

import net.cworks.treefs.domain.TreeFsPath;

public interface FolderApi {

    TreeFsPath create();

    void addMeta(String k, Object v);

    void overwrite();
}
