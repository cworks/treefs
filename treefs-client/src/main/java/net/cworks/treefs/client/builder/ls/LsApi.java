package net.cworks.treefs.client.builder.ls;

import net.cworks.treefs.domain.TreeFsPath;

import java.util.List;

interface LsApi {
    void depth(int n);

    TreeFsPath fetch();

    List<TreeFsPath> fetchList();

    void filesOnly();

    void foldersOnly();

    void fromFs(String fs);

    void glob(String pattern);

    void recursive();
}
