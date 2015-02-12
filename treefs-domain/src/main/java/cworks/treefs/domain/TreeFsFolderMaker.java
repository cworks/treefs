package cworks.treefs.domain;

import cworks.treefs.common.dt.ISO8601DateParser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeFsFolderMaker {
    protected String name;
    protected String path;
    protected String description;
    protected String createdBy;
    protected String createdAt;
    protected Map<String, Object> metadata;
    protected List<TreeFsPath> paths;

    /**
     * Use TreeFsFactory to create
     */
    protected TreeFsFolderMaker() {
        metadata = new HashMap<>();
        paths = new ArrayList<>();
    }

    public TreeFsFolderMaker withName(String name) {
        this.name = name;
        return this;
    }

    public TreeFsFolderMaker withDescription(String description) {
        this.description = description;
        return this;
    }

    public TreeFsFolderMaker withPath(String path) {
        this.path = path;
        return this;
    }

    public TreeFsFolderMaker withPath(Path path) {
        this.path = path.toString();
        return this;
    }

    public TreeFsFolderMaker withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public TreeFsFolderMaker withCreatedAt(Date createdAt) {
        this.createdAt = ISO8601DateParser.toString(createdAt);
        return this;
    }

    public TreeFsFolderMaker withMetadata(String tag, Object value) {
        this.metadata.put(tag, value);
        return this;
    }

    public TreeFsFolderMaker withMetadata(Map<String, Object> metadata) {
        this.metadata.putAll(metadata);
        return this;
    }

    public TreeFsFolderMaker addFile(Path path) {
        TreeFsPath treefsPath = TreeFsFileMaker.newFile()
            .withPath(path)
            .withName(path.getFileName().toString()).make();
        this.paths.add(treefsPath);
        return this;
    }

    public TreeFsFolderMaker addFile(TreeFsFile file) {
        this.paths.add(file);
        return this;
    }

    public TreeFsFolderMaker addFolder(Path path) {
        TreeFsPath treefsPath = TreeFsFolderMaker.newFolder()
            .withPath(path)
            .withName(path.getFileName().toString()).make();
        this.paths.add(treefsPath);
        return this;
    }

    public TreeFsFolderMaker addFolder(TreeFsFolder folder) {
        this.paths.add(folder);
        return this;
    }

    public TreeFsFolderMaker addPath(Path path) {

        TreeFsPath treefsPath = TreeFsPathMaker.newPath()
            .withPath(path)
            .withName(path.getFileName().toString()).make();
        this.paths.add(treefsPath);
        return this;
    }

    /**
     * Create a TreeFsFolderMaker with which to define a <code>TreeFsFolder</code> instance
     * @return
     */
    static TreeFsFolderMaker newFolder() {
        return new TreeFsFolderMaker();
    }

    /**
     * Perform validation here
     * @return
     */
    public TreeFsFolder make() {

        TreeFsFolder folder = new TreeFsFolder();
        if(name != null) folder.name(name);
        if(description != null) folder.description(description);
        if(path != null) folder.path(path);
        if(createdAt != null) folder.createdAt(createdAt);
        if(createdBy != null) folder.createdBy(createdBy);
        if(metadata != null && metadata.size() > 0) folder.metadata(metadata);
        // TODO fix setting of paths
        if(paths != null && paths.size() > 0) folder.paths(paths);
        return folder;
    }


}
